package actor;

import java.util.concurrent.TimeoutException;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import main.NNJobMessage;
import utility.NNOperationTypes;

public class Router extends AbstractActor {
	  private ActorRef workProcessorRouter;
	  private ActorRef nnMaster;
	  private int routeeReturns;

	  public static Props props(ActorRef workProcessorRouter) {
	    return Props.create(Router.class, workProcessorRouter);
	  }
	  
	  public Router(ActorRef workProcessorRouter) {
	        this.workProcessorRouter = workProcessorRouter;
			routeeReturns = 0;
	  }
	  
	  @Override
	    public Receive createReceive() {
	        return receiveBuilder()
	        	.match(NNJobMessage.class, this::initiateJob)
				.match(String.class, this::initiateTesting)
	        	.build();
	  }
	  
	  private void initiateTesting(String s) {
		  System.out.println("Datashard finished! In initiateTesting");
		//  routeeReturns++;
	///	  if(routeeReturns == 1)
	//	  	nnMaster.tell(new NNOperationTypes.Predict(), self());
		  //else if(routeeReturns == numRoutees)
		  	// return weights to master
	  }

	  private void initiateJob(NNJobMessage nnmsg) throws TimeoutException, InterruptedException {
		  // NNMaster actor creation
		  nnMaster = getContext().actorOf(Props.create(NNMaster.class, workProcessorRouter), "nn_master" + nnmsg.getPayload());
		  nnMaster.tell(nnmsg, self());
	  }
}