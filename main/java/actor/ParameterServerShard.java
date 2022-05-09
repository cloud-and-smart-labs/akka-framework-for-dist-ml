package actor;

import akka.actor.AbstractActor;
import main.NNJobMessage;
import utility.NNOperationTypes;

import org.la4j.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

public class ParameterServerShard extends AbstractActor {
	// Receives learningRate (for weights update) and initial random weights.
	// TODO: Some data structure to store the shard weights.
	
	private int ps_id;
	private double learningRate;
	private Matrix weights;
	
	public ParameterServerShard(int ps_id, double learningRate, Matrix weights) {
		System.out.println("########!!!!!!" + weights);
		this.ps_id = ps_id;
		this.learningRate = learningRate;
		this.weights = weights;
	}

	@Override
	public Receive createReceive() {
		System.out.println("ParameterServerShard actor received message");
		return receiveBuilder()
				.match(NNOperationTypes.ParameterRequest.class, this::getLatestParameters)
				.match(NNOperationTypes.Gradient.class, this::updateWeights)
				.build();
	}
	
	public void getLatestParameters(NNOperationTypes.ParameterRequest paramReq) {
		sender().tell(this.weights, getSelf());
	}
	
	public void updateWeights(NNOperationTypes.Gradient g) {
		weights = weights.add(g.getGradient().multiply(learningRate));
	}
}