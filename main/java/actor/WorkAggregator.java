package actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorRefProvider;
import akka.actor.Props;
import utility.WorkerProtocol;

public class WorkAggregator extends AbstractActor{
	private int numOfModelsToCompute;
	private final ActorRef workerRef;
	//private List<WorkerProtocol.WorkProcessed> jobsProcessed;
	private String nodeHost;
	
	public static Props props(int modelsToCompute, ActorRef workerRef) {
        return Props.create(WorkAggregator.class, modelsToCompute, workerRef);
    }
	
	public WorkAggregator(int numOfModelsToCompute, ActorRef workerRef) {
        this.numOfModelsToCompute = numOfModelsToCompute;
        this.workerRef = workerRef;
//        this.jobsProcessed = new ArrayList<>();
    }

	@Override
	public Receive createReceive() {
		return receiveBuilder()
            .match(WorkerProtocol.WorkProcessed.class, this::handleJobsProcessed)
            .build();
	}
	
	@Override
	public void preStart() {
		ActorRefProvider provider = getContext().provider();
		nodeHost = provider.getDefaultAddress().getHost().get();
	}
	
	private void handleJobsProcessed(WorkerProtocol.WorkProcessed work) {
		//System.out.println("~~~~~~Routee path:  " + getSender().path());
        System.out.println("Work Aggregator >>> Received result for routee");
                
        numOfModelsToCompute -= 1;
        if (numOfModelsToCompute == 0) {    
            System.out.println("Work Aggregator >>> Finished a job >> Sending results back to Master ");
            workerRef.tell(work.getResult(), self());
        }
    }
}