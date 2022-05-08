package utility;

import org.la4j.Matrix;
import org.la4j.Vector;
import org.neuroph.core.transfer.TransferFunction;

public class NNOperations {
	public static Matrix computeGradient(Vector delta, Vector activatedInputs) {
		return delta.outerProduct(activatedInputs).transpose();
	}
	
	// hadamard product of delta and inputs
	// activationDerivative(inputs) . weights * activatedInputs
	public static Vector computeDelta(Vector delta, Matrix weights, TransferFunction activation, Vector activatedInputs) {
		Vector actDerivativeOutputs = activatedInputs;
		actDerivativeOutputs.forEach(i -> activation.getDerivative(i));
		
		System.out.println("Act derivative outputs: " + actDerivativeOutputs);
		System.out.println("Delta dimensions: " + delta.length() + " weights dimns: " + weights.rows() + ", " + weights.columns());
		
		Vector parentDelta = actDerivativeOutputs.hadamardProduct(delta.multiply(weights.transpose()));
		return parentDelta.toColumnMatrix().toColumnVector();
	}
	
	public static Vector applyActivation(Vector x, TransferFunction activation) {
		double[] actOuts = new double[x.length()];
		int i = 0;
		for(double val: x) {
			actOuts[i] = activation.getOutput(val);
			i++;
		}
		return Vector.fromArray(actOuts);
	}
	
	public static Vector computeError(Vector x, Vector y) {
		Vector diff =  x.subtract(y);
		double[] error = new double[x.length()];
		
		int i = 0;
		for(double d: diff) {
			error[i] = d * d;
			i++;
		}
		return Vector.fromArray(error);
	}
	
}