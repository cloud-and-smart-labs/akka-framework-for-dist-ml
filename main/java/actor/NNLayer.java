package actor;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.neuroph.core.transfer.TransferFunction;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import utility.NNOperationTypes;
import utility.NNOperations;
import utility.WorkerRegionEvent;

public class NNLayer extends AbstractActor {
	// Receives id, activation (for forward prop), parentLayer ref and corressponding PS shard id
	// First updates the layer weights from PS actor ref
	// Perform forward prop -> multiply activation and layer weights until the current layer doesn't have a child
	// Perform backprop -> compute gradient, compute deltas
	private int ps_id;
	private TransferFunction activation;
	private ActorRef parentRef;
	private ActorRef childRef;
	private ActorRef psShardRef;
	private Basic2DMatrix layerWeights;
	private Vector activatedInput;
	private final ActorSelection master;

	// ps_id required?
	public NNLayer(int ps_id, TransferFunction activation, ActorRef parentRef, ActorRef childRef, ActorRef psShardRef) {
		this.ps_id = ps_id;
		this.activation = activation;
		this.parentRef = parentRef;
		this.childRef = childRef;
		this.psShardRef = psShardRef;
		this.activatedInput = null;
		master = getContext().actorSelection("akka://MasterSystem@master:2550/user/master");
	}
	
	public void setChildRef(ActorRef childRef) {
		this.childRef = childRef;
	}

	@Override
	public Receive createReceive() {
		System.out.println("NNLayer actor received message");
		return receiveBuilder()
				.match(ActorRef.class, this::setChildRef)
				.match(NNOperationTypes.ParameterRequest.class, this::weightsRequest)
				.match(NNOperationTypes.ForwardProp.class, this::forwardProp)
				.match(NNOperationTypes.BackProp.class, this::backProp)
				.match(NNOperationTypes.Predict.class, this::predict)
			//	.match(String.class, this::updateWeights)
				.build();
	}
	
	public void weightsRequest(NNOperationTypes.ParameterRequest paramReq) throws TimeoutException, InterruptedException {
		Timeout timeout = Timeout.create(Duration.ofSeconds(3));
		Future<Object> future = Patterns.ask(psShardRef, paramReq, timeout);
		String result = (String) Await.result(future, timeout.duration());
		if(result.length() > 0) {
			layerWeights = (Basic2DMatrix) Matrix.fromCSV(result);	
			sender().tell(new NNOperationTypes.ParameterResponse(), self());
		}
	}
	
	
	
	public void predict(NNOperationTypes.Predict p) {
		System.out.println("Prediction for the data point: ");
		Vector inputs = p.x;

		if(parentRef != null) {
			System.out.println("Has parent");
			this.activatedInput = NNOperations.applyActivation(inputs, activation);
		}
		else {
			System.out.println("No parent");
			this.activatedInput = inputs;
		}
			
		System.out.println("Activated Input: " + activatedInput) ;
		System.out.println("Layer weights: " + layerWeights);
		
		// vector = vector * matrix
		Vector outputs = this.activatedInput.multiply(layerWeights);
		System.out.println("Output " + outputs) ;
		// Used for backprop
		Vector activatedOutputs = NNOperations.applyActivation(outputs, activation);
		System.out.println("Activated Outputs " + activatedOutputs) ;
		
		if(childRef != null) {
			System.out.println("Has child");
			childRef.tell(new NNOperationTypes.Predict(outputs), getSelf());
		}
		else {

			System.out.println("Done!");
		}
	}

	public void forwardProp(NNOperationTypes.ForwardProp forwardParams) {
		System.out.println("In forward prop");
		Vector inputs = forwardParams.x;
		Vector target = forwardParams.y;
		System.out.println("Inputs: " + inputs + " targets: " +  target);
		
		if(parentRef != null) {
			System.out.println("Has parent");
			this.activatedInput = NNOperations.applyActivation(inputs, activation);
		}
		else {
			System.out.println("No parent");
			this.activatedInput = inputs;
		}
			
		System.out.println("Activated Input: " + activatedInput) ;
		System.out.println("Layer weights: " + layerWeights);
		
		// vector = vector * matrix
		Vector outputs = this.activatedInput.multiply(layerWeights);
		System.out.println("Output " + outputs) ;
		// Used for backprop
		Vector activatedOutputs = NNOperations.applyActivation(outputs, activation);
		System.out.println("Activated Outputs " + activatedOutputs) ;
		
		if(childRef != null) {
			System.out.println("Has child");
			childRef.tell(new NNOperationTypes.ForwardProp(outputs, target), getSelf());
		}
		else {
			System.out.println("No child");
			// Compute error and start backprop
			Vector delta = NNOperations.computeError(activatedOutputs, target);
			
			System.out.println("Delta: " + delta);
			
			// Outer product of delta and activatedInputs  
			Basic2DMatrix gradient = NNOperations.computeGradient(delta, this.activatedInput);
			System.out.println("Gradient: " + gradient);
			
			psShardRef.tell(new NNOperationTypes.Gradient(gradient.toCSV()), getSelf());
			Vector parentDelta = NNOperations.computeDelta(delta, layerWeights, activation, this.activatedInput);
			System.out.println("Parent Delta " + parentDelta);
			parentRef.tell(new NNOperationTypes.BackProp(parentDelta), getSelf());
		}
	}
	
	public void backProp(NNOperationTypes.BackProp backParams) {
		System.out.println("In backprop!");
		Vector childDelta = backParams.childDelta;
		System.out.println("Child Delta " + childDelta);
		
		Matrix gradient = NNOperations.computeGradient(childDelta, this.activatedInput);
		System.out.println("Gradient" + gradient);
		psShardRef.tell(new NNOperationTypes.Gradient(gradient.toCSV()), getSelf());
	
		System.out.println("Layer weights" + layerWeights);
		if(parentRef != null) {
			System.out.println("Has parent");
			Vector parentDelta = NNOperations.computeDelta(childDelta, layerWeights, activation, this.activatedInput);
			System.out.println("Parent Delta " + parentDelta);
			parentRef.tell(new NNOperationTypes.BackProp(parentDelta), getSelf());
		}
		else {
			System.out.println("No parent");
			// -1 table entry here
			String nodeHost = getContext().provider().getDefaultAddress().getHost().get();
			System.out.println("Address of node of routee: " + nodeHost);
			master.tell(new WorkerRegionEvent.UpdateTable(nodeHost, -1), self());
			
			getContext().parent().tell(new NNOperationTypes.WeightUpdate(), getSelf());
		}
	}
}
