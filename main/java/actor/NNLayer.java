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
	private TransferFunction activation;
	private ActorRef parentRef;
	private ActorRef childRef;
	private ActorRef psShardRef;
	private Matrix layerWeights;
	private Vector activatedInput;

	// ps_id required?
	public NNLayer(int ps_id, TransferFunction activation, ActorRef parentRef, ActorRef childRef, ActorRef psShardRef) {
		this.ps_id = ps_id;
		this.activation = activation;
		this.parentRef = parentRef;
		this.childRef = childRef;
		this.psShardRef = psShardRef;
		this.activatedInput = null;
	}
	
	public void setChildRef(ActorRef childRef) {
		this.childRef = childRef;
	}

	@Override
	public Receive createReceive() {
		System.out.println("NNLayer actor received message");
		return receiveBuilder()
				.match(ActorRef.class, this::setChildRef)
				.match(NNOperationTypes.ParameterRequest.class, this::weightsRequest)
				.match(NNOperationTypes.ForwardProp.class, this::forwardProp)
				.match(NNOperationTypes.BackProp.class, this::backProp)
				.match(Matrix.class, this::updateWeights)
				.build();
	}
	
	// TODO: Method to get current layer weights. Send message of NNOperationTypes.ParameterRequest type to PS actor ref.
	public void weightsRequest(NNOperationTypes.ParameterRequest paramReq) {
		psShardRef.tell(paramReq, sender());
	}
	
	public void updateWeights(Matrix w) {
		layerWeights = w;
		System.out.println("Weights in layer updated " + w);
	}
	
	public void forwardProp(NNOperationTypes.ForwardProp forwardParams) {
		System.out.println("In forward prop");
		Vector inputs = forwardParams.x;
		Vector target = forwardParams.y;
		System.out.println("Inputs: " + inputs + " targets: " +  target);
		
		if(parentRef != null) {
			System.out.println("Has parent");
			this.activatedInput = NNOperations.applyActivation(inputs, activation);
		}
		else {
			System.out.println("No parent");
			this.activatedInput = inputs;
		}
			
		System.out.println("Activated Input: " + activatedInput) ;
		System.out.println("Layer weights: " + layerWeights);
		
		// vector = vector * matrix
		Vector outputs = this.activatedInput.multiply(layerWeights);
		System.out.println("Output " + outputs) ;
		// Used for backprop
		Vector activatedOutputs = NNOperations.applyActivation(outputs, activation);
		System.out.println("Activated Outputs " + activatedOutputs) ;
		
		if(childRef != null) {
			System.out.println("Has child");
			childRef.tell(new NNOperationTypes.ForwardProp(outputs, target), getSelf());
		}
		else {
			System.out.println("No child");
			// Compute error and start backprop
			Vector delta = NNOperations.computeError(activatedOutputs, target);
			
			System.out.println("Delta: " + delta);
			
			// Outer product of delta and activatedInputs  
			Matrix gradient = NNOperations.computeGradient(delta, this.activatedInput);
			System.out.println("Gradient: " + gradient);
			
			psShardRef.tell(new NNOperationTypes.Gradient(gradient), getSelf());
			Vector parentDelta = NNOperations.computeDelta(delta, layerWeights, activation, this.activatedInput);
			System.out.println("Parent Delta " + parentDelta);
			parentRef.tell(new NNOperationTypes.BackProp(parentDelta), getSelf());
		}
	}
	
	public void backProp(NNOperationTypes.BackProp backParams) {
		System.out.println("In backprop!");
		Vector childDelta = backParams.childDelta;
		System.out.println("Child Delta " + childDelta);
		
		Matrix gradient = NNOperations.computeGradient(childDelta, this.activatedInput);
		System.out.println("Gradient" + gradient);
		psShardRef.tell(new NNOperationTypes.Gradient(gradient), getSelf());
		
		System.out.println("Layer weights" + layerWeights);
		if(parentRef != null) {
			System.out.println("Has parent");
			Vector parentDelta = NNOperations.computeDelta(childDelta, layerWeights, activation, this.activatedInput);
			System.out.println("Parent Delta " + parentDelta);
			parentRef.tell(new NNOperationTypes.BackProp(parentDelta), getSelf());
		}
		else {
			System.out.println("No parent");
			getContext().parent().tell(new NNOperationTypes.WeightUpdate(), getSelf());
		}
	}
}
