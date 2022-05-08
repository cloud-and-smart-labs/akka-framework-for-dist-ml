package actor;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.duration.FiniteDuration;
import utility.Point;
import utility.WorkerProtocol;
import utility.WorkerRegionEvent;
import akka.actor.Address;
import akka.actor.typed.ActorRef;

public class PiCalc extends AbstractActor {
	  private Cluster cluster = Cluster.get(getContext().system());	
	  
	  private ArrayList<Point> points;
	  private String nodeHost;
	  private final ActorSelection master;
	  
	  public PiCalc() {
		  master = getContext().actorSelection("akka://MasterSystem@master:2550/user/master");
	  }
	  
	  private void throwDarts(int size) {
	    for (int i = 0; i < size; i++) {
	    	points.add(Point.genRandPoint());
	    }
	  }
	  
	  private void approximatePi(WorkerProtocol.PiCalcTask sensorDataModelTask) throws UnknownHostException, InterruptedException {
		String nodeHost = getContext().provider().getDefaultAddress().getHost().get();
		System.out.println("Address of node of routee: " + nodeHost);
		
		master.tell(new WorkerRegionEvent.UpdateTable(nodeHost, 1), self());

		this.throwDarts(50);
	    int total = 0; // Keep track of total points thrown.
	    int inside = 0; // Keep track of points inside the circle.
        for (Point p : points) {
            if (p.x * p.x + p.y * p.y <= 1) {
                inside += 1;
            }
            total += 1;
        }
        float pi_val = 4 * ((float) inside) / total;
        
        // Simulating a delay
        // Thread.sleep(4000); - Pauses everything!
        
        System.out.println("In approx pi: " + pi_val);
        sender().tell(new WorkerProtocol.WorkProcessed(pi_val), self());
          
        //master.tell(new WorkerRegionEvent.UpdateTable(nodeHost, -1), self());
        master.tell(new WorkerRegionEvent.JobSimulate(nodeHost), self());
	   }
		  
	  @Override
	  public void preStart() {
		  cluster.subscribe(self(), ClusterEvent.MemberUp.class);
		  points = new ArrayList<Point>();
	  }
	  
	  @Override
	    public void postStop() {
	        cluster.unsubscribe(self());
	    }
	  
	  @Override
	    public Receive createReceive() {
		  System.out.println("Work processor actor received msg");
		  return receiveBuilder()
				  .match(WorkerProtocol.PiCalcTask.class, this::approximatePi)
	              .build();
	    }
	}