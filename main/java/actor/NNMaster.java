package actor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import org.la4j.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import akka.actor.ActorRef;
import main.NNJobMessage;
import scala.concurrent.Await;
import scala.concurrent.Future;
import utility.NNOperationTypes;

public class NNMaster extends AbstractActor {
	// Receives paramters like dataset, activation, etc. 
	// Responsible for creating data-shard and parameter-server-shard actors. Initiates the process on the routees.
	ArrayList<ActorRef> psRefs;
	ArrayList<ActorRef> dataShrdRefs;
	private int dsActCreationCount = 0;
	
	public static Props props() {
        return Props.create(NNMaster.class);
    }
	
	@Override
	public Receive createReceive() {
		System.out.println("NNMaster actor received message");
		return receiveBuilder()
				.match(NNJobMessage.class, this::createActors)
				.match(NNOperationTypes.Ready.class, this::getLatestWeights)
				//.match(String.class, this::successMsg)
				.build();
	}
	
	/*public String successMsg(String msg) {
		dsActCreationCount--;
		System.out.println("# layer actors left:" + dsActCreationCount);
		if(dsActCreationCount == 0) { 
			System.out.println("Data and PS actor creation success");
			System.out.println("####: " + getSelf().path());
		//	getSelf().tell(msg, sender());
			return("success");
		}
		return("failure");
	}*/
	
	public List<List<DataSetRow>> splitDataSet(NNJobMessage nnmsg) {
		DataSet dataset = nnmsg.getDataset();
		List<List<DataSetRow>> dsSplits = new ArrayList<List<DataSetRow>>();

		int sizeOfSplit = nnmsg.getDataPerReplica();
		int numOfSplits = (int)nnmsg.getDataset().size()/sizeOfSplit;
		
		int j = 0;
		while(j < numOfSplits) {
			dsSplits.add(dataset.subList(j*sizeOfSplit, j*sizeOfSplit + sizeOfSplit));
			j += 1;
		}
		System.out.println(dsSplits);
		return dsSplits;
	}

	public void createActors(NNJobMessage nnmsg) throws TimeoutException, InterruptedException {
		List<List<DataSetRow>> splitDataSets = new ArrayList<List<DataSetRow>>();
		
		// TODO: Split according to number of routees
		splitDataSets = splitDataSet(nnmsg);
		
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
		Timeout timeout = Timeout.create(Duration.ofSeconds(5));
		for(List<DataSetRow> ds: splitDataSets) {
			//System.out.println("Dataset empty? " + ds.isEmpty());
			System.out.println("@@@ Data shard " + c);
			ActorRef dsRef = getContext().actorOf(Props.create(DataShard.class, c, ds, nnmsg.getActivation(), psRefs), "dataShard" + c);
			dataShrdRefs.add(dsRef);
			Future<Object> future = Patterns.ask(dsRef, nnmsg, timeout);
			String result = (String) Await.result(future, timeout.duration());
			System.out.println("The results##########: " + result);
			if(result != "success") 
			    return;	  
			c++;
		}
		System.out.println("Data and PS actor creation success");
		sender().tell("success", getSelf());
	}
	
	public void getLatestWeights(NNOperationTypes.Ready r) {
		System.out.println("Get latest weights from PS");
		for(ActorRef dsRef: dataShrdRefs) {
			dsRef.tell(new NNOperationTypes.WeightUpdate(), getSelf());
		}
	}
}