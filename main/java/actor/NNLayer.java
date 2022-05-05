package actor;

import java.util.Optional;

import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.vector.functor.VectorFunction;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.util.TransferFunctionType;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import utility.NNOperationTypes;
import utility.NNOperations;

public class NNLayer extends AbstractActor {
	// Receives id, activation (for forward prop), parentLayer ref and corressponding PS shard id
	// First updates the layer weights from PS actor ref
	// Perform forward prop -> multiply activation and layer weights until the current layer doesn't have a child
	// Perform backprop -> compute gradient, compute deltas
	private int ps_id;
	private int d_id;
	private TransferFunction activation;
	private boolean hasParent;
	private boolean hasChild;
	private ActorRef psShardRef;
	private Matrix layerWeights;
	private Vector activatedInput;
	private ActorRef dsRef;

	public NNLayer(int ps_id, int d_id, TransferFunction activation, boolean hasParent, boolean hasChild, ActorRef psShardRef) {
		this.ps_id = ps_id;
		this.d_id = d_id;
		this.activation = activation;
		this.hasParent = hasParent;
		this.hasChild = hasChild;
		this.psShardRef = psShardRef;
		this.activatedInput = null;
		this.dsRef = null;
		
	}
	
	@Override
	public Receive createReceive() {
		System.out.println("NNLayer actor received message");
		return receiveBuilder()
				.match(NNOperationTypes.ParameterRequest.class, this::weightsRequest)
				.match(NNOperationTypes.ForwardProp.class, this::forwardProp)
				.match(NNOperationTypes.BackProp.class, this::backProp)
				.match(Matrix.class, this::updateWeights)
				.build();
	}
	
	// TODO: Method to get current layer weights. Send message of NNOperationTypes.ParameterRequest type to PS actor ref.
	public void weightsRequest(NNOperationTypes.ParameterRequest paramReq) {
		psShardRef.tell(paramReq, getSelf());
		dsRef = sender();
	}
	
	public void updateWeights(Matrix w) {
		layerWeights = w;
		System.out.println("Weights retrived from PS shard: " + w);
		// TODO: Send success message back to datashard actor
		dsRef.tell("success", getSelf());
		
	}
	
	public void forwardProp(NNOperationTypes.ForwardProp forwardParams) {
		Vector inputs = forwardParams.x;
		Vector target = forwardParams.y;
		
		if(hasParent) 
			this.activatedInput = inputs.transform((VectorFunction) activation);
		else
			this.activatedInput = inputs;
		
		// vector = vector * matrix
		Vector outputs = this.activatedInput.multiply(layerWeights);
		// Used for backprop
		Vector activatedOutputs = outputs.transform((VectorFunction) activation);
		
		if(hasChild)
			getSelf().tell(new NNOperationTypes.ForwardProp(outputs, target), getSelf());
		else {
			// Compute error and start backprop
			Vector delta = activatedOutputs.subtract(target);
			// Outer product of delta and activatedInputs  
			Matrix gradient = NNOperations.computeGradient(delta, this.activatedInput);
			
			psShardRef.tell(new NNOperationTypes.Gradient(gradient), getSelf());
			Vector parentDelta = NNOperations.computeDelta(delta, layerWeights, activation, this.activatedInput);			
			getSelf().tell(new NNOperationTypes.BackProp(parentDelta), getSelf());
		}
	}
	
	public void backProp(NNOperationTypes.BackProp backParams) {
		Vector childDelta = backParams.childDelta; 
		Matrix gradient = NNOperations.computeGradient(childDelta, this.activatedInput);
		psShardRef.tell(new NNOperationTypes.Gradient(gradient), getSelf());
		
		if(hasParent) {
			Vector parentDelta = NNOperations.computeDelta(childDelta, layerWeights, activation, this.activatedInput);
			getSelf().tell(new NNOperationTypes.BackProp(parentDelta), getSelf());
		}
		else {
			dsRef.tell(NNOperationTypes.WeightUpdate.class, getSelf());
		}
	}
}
