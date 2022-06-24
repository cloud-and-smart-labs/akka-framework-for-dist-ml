package main;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.transfer.Gaussian;
import org.neuroph.core.transfer.Sigmoid;
import org.neuroph.util.TransferFunctionType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.typesafe.config.Config;

import actor.ClusterListener;
import actor.Master;
import utility.configs; 
import org.neuroph.core.transfer.RectifiedLinear;

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
    	FiniteDuration interval = Duration.create(10, TimeUnit.SECONDS);
        Timeout timeout = new Timeout(Duration.create(15, TimeUnit.SECONDS));
        ExecutionContext ec = system.dispatcher();
        
        DataSet trainingSet = DataSet.createFromFile("/root/datasets/train.csv", 10, 1, ",");
		DataSet testSet = DataSet.createFromFile("/root/datasets/test.csv", 10, 1, ",");

		System.out.println("Dataset inited: " + trainingSet.size());
		System.out.println("Dataset inited: " + testSet.size());
        
		ArrayList<Integer> layerDimensions = new ArrayList<>(List.of(10, 16, 8, 7));
		Sigmoid sigmoid = new Sigmoid();
		
		RectifiedLinear rl  = new RectifiedLinear();

		Properties prop = new Properties();
		String fileName = "master.conf";
		try (FileInputStream fis = new FileInputStream(fileName)) {
			prop.load(fis);
		} catch (IOException ex) {
			System.out.println("Could not load conf file!");
		}
		System.out.println("#routees: " + prop.getProperty("akka.actor.deployment./workerRegion/workProcessorRouter.nr-of-instances"));
        
      /*   system.scheduler().schedule(interval, interval, () -> Patterns.ask(master, new NNJobMessage("XOR_task1", trainingSet, 15, sigmoid, layerDimensions, 0.1), timeout)
         		.onComplete(result -> {
                     System.out.println(result);do
                     return CompletableFuture.completedFuture(result);
                 }, ec)
         		, ec);
        */ 
//		system.scheduler().scheduleOnce(interval, master, new NNJobMessage("XOR_task1", trainingSet, 4, sigmoid, layerDimensions, 0.1), system.dispatcher(), null);

		// Forest fire dataset: http://www3.dsi.uminho.pt/pcortez/forestfires/
		system.scheduler().scheduleOnce(interval, master, new NNJobMessage("car_task", trainingSet, testSet, 80, rl, layerDimensions, 0.5, 50), system.dispatcher(), null);
	}
}