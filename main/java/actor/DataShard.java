package actor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.la4j.Matrix;
import org.la4j.Vector;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.util.TransferFunctionType;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.japi.Procedure;
import akka.pattern.Patterns;
import akka.util.Timeout;
import main.NNJobMessage;
import scala.concurrent.Await;
import scala.concurrent.Future;
import utility.NNOperationTypes;

public class DataShard extends AbstractActor {
	// Receives a partition of training data, activation (to pass on to the layer actors) and a list of PS shard actor refs
	// Creates the layer actors and link them
	// The layers are updated from PS shards after each data point/mini-batch
	// Then initiate forward pass for the next data point/mini-batch
	
	private List<DataSetRow> dataSetPart;
	private TransferFunction activation;
	private ArrayList<ActorRef> parameterShardRefs;
	private ArrayList<ActorRef> layerRefs; 
	private Iterator<DataSetRow> dsIter;
	
	public DataShard() {}
	
	@Override
	public Receive createReceive() {
		System.out.println("DataShard actor received message");
		return receiveBuilder()
		//		.match(NNJobMessage.class, this::createLayerActors)
				.match(NNOperationTypes.Dummy.class, this::dummy)
				.match(NNOperationTypes.WeightUpdate.class, this::fetchWeights)
				.match(NNOperationTypes.DoneUpdatingWeights.class, this::startTraining)
				.match(NNOperationTypes.DataShardParams.class, this::setParameters)
				.match(String.class, this::successMsg)
				.matchAny(this::handleAny)
				.build();
	}
	
	private void handleAny(Object o) {
		System.out.println("Actor received unknown message: " + o.toString());
	}
	
	public void dummy(NNOperationTypes.Dummy d) {
		System.out.println("In dummy!!");
	}
	
	public void setParameters(NNOperationTypes.DataShardParams dsParams) {
		this.dataSetPart = dsParams.dataSetPart;
		this.activation = dsParams.activation;
		this.parameterShardRefs = dsParams.parameterShardRefs;
		dsIter = dataSetPart.iterator();
		createLayerActors();
	}
	
	public void successMsg(String s) {
		System.out.println("Layer actor creation success. ****** " + sender().path());
		sender().tell("success", getSelf());
	}
	
	public void createLayerActors() {
		System.out.println("Creating layer actors!");
		int n = this.parameterShardRefs.size();
		layerRefs = new ArrayList<ActorRef>();
		
		System.out.println("@@@");
		layerRefs.add(getContext().actorOf(Props.create(NNLayer.class, 0, activation, null, null, parameterShardRefs.get(0)), "layer0"));
		System.out.println("???");
		for(int i = 1; i < n-1; i++) {
			System.out.println("Layer: " + i);			
			layerRefs.add(getContext().actorOf(Props.create(NNLayer.class, i, activation, layerRefs.get(i-1), null, parameterShardRefs.get(i)), "layer" + i));
			//System.out.println("Layer " + i + " parent: " + layerRefs.get(i).path().parent());
		}
		layerRefs.add(getContext().actorOf(Props.create(NNLayer.class, n-1, activation, layerRefs.get(n-2), null, parameterShardRefs.get(n-1)), "layer" + (n-1)));
		
		System.out.println("Linking the child actors");
		for(int i = 0; i < n-1; i++) {
			layerRefs.get(i).tell(layerRefs.get(i+1), self());
		}
		getSelf().tell("success", sender());
	
		System.out.println("Init training!");
		getSelf().tell(new NNOperationTypes.WeightUpdate(), getSelf());
	}
	 
	public void fetchWeights(NNOperationTypes.WeightUpdate msg) throws TimeoutException, InterruptedException {
		System.out.println("DS actor fetching current weights");
		Timeout timeout = Timeout.create(Duration.ofSeconds(3));
		for(ActorRef l: layerRefs) {
			Future<Object> future = Patterns.ask(l, new NNOperationTypes.ParameterRequest(), timeout);
			Matrix result = (Matrix) Await.result(future, timeout.duration());
			if(result == null) {
				System.out.println("Current weights could NOT be retrieved!");
				return;
			}
			l.tell(result, self());
		}
		System.out.println("Current weights retrieved successfully.");
		getSelf().tell(new NNOperationTypes.DoneUpdatingWeights(), getSelf());
	}
	
	// Once weights are updated, then forwardProp is initiated
	public void startTraining(NNOperationTypes.DoneUpdatingWeights msg) {
		if(dsIter.hasNext()) {
			DataSetRow ds_row = dsIter.next();
			System.out.println("Current row: " + ds_row);
			Vector x = Vector.fromArray(ds_row.getInput());
			Vector y = Vector.fromArray(ds_row.getDesiredOutput());
			layerRefs.get(0).tell(new NNOperationTypes.ForwardProp(x, y), getSelf());
		}
		System.out.println("One epoch completed");
	}
}