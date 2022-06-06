package actor;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import main.NNJobMessage;
import scala.concurrent.Await;
import scala.concurrent.Future;
import utility.MasterWorkerProtocol;
import utility.NNOperationTypes;
import utility.WorkerProtocol;

public class Router extends AbstractActor {
	  private ActorRef workProcessorRouter;
	  
	  public static Props props(ActorRef workProcessorRouter) {
	    return Props.create(Router.class, workProcessorRouter);
	  }
	  
	  public Router(ActorRef workProcessorRouter) {
	        this.workProcessorRouter = workProcessorRouter;
	  }
	  
	  @Override
	    public Receive createReceive() {
	        return receiveBuilder()
	        	.match(NNJobMessage.class, this::sendSensorDataForProcessing)
	        	.build();
	  }
	  
	  private void sendSensorDataForProcessing(NNJobMessage nnmsg) throws TimeoutException, InterruptedException {
		  System.out.println("Sending job messages to routees!!");
		  ActorRef workAggregator = getContext().actorOf(Props.create(WorkAggregator.class, 4, self()));

		  // NNMaster actor creation
		  ActorRef nnMaster = getContext().actorOf(Props.create(NNMaster.class, workProcessorRouter), "nn_master" + nnmsg.getPayload());
		  nnMaster.tell(nnmsg, self());
	  }
}