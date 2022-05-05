package main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.transfer.Sigmoid;
import org.neuroph.util.TransferFunctionType;

import java.util.ArrayList;
import java.util.List;
import com.typesafe.config.Config;

import actor.ClusterListener;
import actor.Master;
import utility.configs; 


public class MainMaster{	
	public static void main(String[] args) {
		System.out.println("In MainMaster");
		String port = args[0];
        Config config = configs.getConfig(port, "master", "master.conf");
		String clusterName = config.getString("clustering.cluster.name");
		
		ActorSystem system = ActorSystem.create(clusterName, config);
		System.out.println("Created master actor system");
		
		system.actorOf(Props.create(ClusterListener.class), "cluster-listener-master");
      
        ActorRef master = system.actorOf(Props.create(Master.class), "master");
        
       // master.tell(new MasterWorkerProtocol.RegisterWorkerRegion("2550", master), master);
    	FiniteDuration interval = Duration.create(5, TimeUnit.SECONDS);
        Timeout timeout = new Timeout(Duration.create(10, TimeUnit.SECONDS));
        ExecutionContext ec = system.dispatcher();
        //AtomicInteger counter = new AtomicInteger();
        
        // Build the NNJobMessage
        DataSet trainingSet = new DataSet(2, 1);
		trainingSet.addRow(new DataSetRow(new double[] {0, 0}, new double[] {0}));
		trainingSet.addRow(new DataSetRow(new double[] {0, 1}, new double[] {1}));
		trainingSet.addRow(new DataSetRow(new double[] {1, 0}, new double[] {1}));
		trainingSet.addRow(new DataSetRow(new double[] {1, 1}, new double[] {0}));
		trainingSet.addRow(new DataSetRow(new double[] {0, 0}, new double[] {0}));
		trainingSet.addRow(new DataSetRow(new double[] {0, 1}, new double[] {1}));
		trainingSet.addRow(new DataSetRow(new double[] {1, 0}, new double[] {1}));
		trainingSet.addRow(new DataSetRow(new double[] {1, 1}, new double[] {0}));
		trainingSet.addRow(new DataSetRow(new double[] {0, 0}, new double[] {0}));
		trainingSet.addRow(new DataSetRow(new double[] {0, 1}, new double[] {1}));
		trainingSet.addRow(new DataSetRow(new double[] {1, 0}, new double[] {1}));
		trainingSet.addRow(new DataSetRow(new double[] {1, 1}, new double[] {0}));
		trainingSet.addRow(new DataSetRow(new double[] {0, 0}, new double[] {0}));
		trainingSet.addRow(new DataSetRow(new double[] {0, 1}, new double[] {1}));
		trainingSet.addRow(new DataSetRow(new double[] {1, 0}, new double[] {1}));
		trainingSet.addRow(new DataSetRow(new double[] {1, 1}, new double[] {0}));
		
		ArrayList<Integer> layerDimensions = new ArrayList<>(List.of(2, 2, 2, 1));
		Sigmoid sigmoid = null;
        /*
         system.scheduler().schedule(interval, interval, () -> Patterns.ask(master, new NNJobMessage(trainingSet, 10, layerDimensions, 0.1), timeout)
        		.onComplete(result -> {
                    System.out.println(result);
                    return CompletableFuture.completedFuture(result);
                }, ec)
        		, ec);
        */
		
		system.scheduler().scheduleOnce(interval, master, new NNJobMessage("XOR_task1", trainingSet, 5, sigmoid, layerDimensions, 0.1), system.dispatcher(), null);
	//	system.scheduler().scheduleOnce(interval, master, new NNJobMessage("XOR_task2", trainingSet, 5, sigmoid, layerDimensions, 0.1), system.dispatcher(), null);
	}
}