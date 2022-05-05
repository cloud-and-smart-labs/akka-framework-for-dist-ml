package utility;

import org.la4j.Matrix;
import org.la4j.Vector;
import org.neuroph.core.transfer.TransferFunction;

public class NNOperations {
	public static Matrix computeGradient(Vector delta, Vector activatedInputs) {
		return delta.outerProduct(activatedInputs);
	}
	
	// hadamard product of delta and inputs
	// activationDerivative(inputs) . weights * activatedInputs
	public static Vector computeDelta(Vector delta, Matrix weights, TransferFunction activation, Vector activatedInputs) {
		Vector actDerivativeOutputs = activatedInputs;
		actDerivativeOutputs.forEach(i -> activation.getDerivative(i));
		
		Vector parentDelta = actDerivativeOutputs.hadamardProduct(delta.multiply(weights));
		return parentDelta;
	}
}