package actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.la4j.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.neuroph.core.data.DataSet;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.ActorRef;
import main.NNJobMessage;
import utility.NNOperationTypes;

public class NNMaster extends AbstractActor {
	// Receives paramters like dataset, activation, etc. 
	// Responsible for creating data-shard and parameter-server-shard actors. Initiates the process on the routees.
	ArrayList<ActorRef> psRefs;
	ArrayList<ActorRef> dataShrdRefs;
	
	public static Props props() {
        return Props.create(NNMaster.class);
    }
	
	@Override
	public Receive createReceive() {
		System.out.println("NNMaster actor received message");
		return receiveBuilder()
				.match(NNJobMessage.class, this::createActors)
				.match(NNOperationTypes.Ready.class, this::getLatestWeights)
				.match(String.class, this::successMsg)
				.build();
	}
	
	public void successMsg(String msg) {
		System.out.println("Data and PS actor creation success");
		sender().tell(msg, getSelf());
	}

	public void createActors(NNJobMessage nnmsg) {
		List<DataSet> splitDataSets = new ArrayList<DataSet>();
		
		// TODO: Split according to number of routees
		splitDataSets = nnmsg.getDataset().split(4, 4, 4);
		// (int)nnmsg.getDataset().size()/nnmsg.getDataPerReplica() 
		System.out.println("Number of datasets: " + splitDataSets.size());
		
		// PS shard actors
		int n = nnmsg.getLayerDimensions().size();
		psRefs = new ArrayList<ActorRef>();
		System.out.println("Layer Dimensions!! " + nnmsg.getLayerDimensions());
		
		for(int i = 0; i < n - 1; i++) {
			int rows = nnmsg.getLayerDimensions().get(i);
			int cols = nnmsg.getLayerDimensions().get(i+1);
			Random r = new Random();		
			System.out.println("Creating PS shard actor for between " + i + " and " + (i+1));
			psRefs.add(getContext().actorOf(Props.create(ParameterServerShard.class, i, nnmsg.getLearningRate(), Matrix.random(rows, cols, r)), "ps" + i));
		}
		
		// DataShard actors
		dataShrdRefs = new ArrayList<ActorRef>();
		int c = 1;
		for(DataSet ds: splitDataSets) {
			System.out.println("@@@ Data shard " + c);	
			dataShrdRefs.add(getContext().actorOf(Props.create(DataShard.class, c, ds, nnmsg.getActivation(), psRefs), "dataShard" + c));
			c++;
		}
	}
	
	public void getLatestWeights(NNOperationTypes.Ready r) {
		System.out.println("Get latest weights from PS");
		for(ActorRef dsRef: dataShrdRefs) {
			dsRef.tell(NNOperationTypes.WeightUpdate.class, getSelf());
		}
	}
}