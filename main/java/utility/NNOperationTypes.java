package utility;

import java.io.Serializable;
import java.util.ArrayList;

import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic2DMatrix;

import main.NNJobMessage;

public interface NNOperationTypes {
	class ParameterRequest implements NNOperationTypes, Serializable {
    }
	
	class Gradient implements NNOperationTypes, Serializable {
		private Matrix gradient;
		public Gradient(Matrix gradient) {
			this.gradient = gradient;
		}
		public Matrix getGradient() {
			return gradient;
		}
	}
	
	class Ready implements NNOperationTypes, Serializable {}
	class Success implements NNOperationTypes, Serializable {}
	
	class ForwardProp implements NNOperationTypes, Serializable {
		public Vector x;
		public Vector y;
		public ForwardProp(Vector x, Vector y) {
			this.x = x;
			this.y = y;
		}
	}
	
	class BackProp implements NNOperationTypes, Serializable {
		public Vector childDelta;
		public BackProp(Vector childDelta) {
			this.childDelta = childDelta;
		}
	}
	
	class WeightUpdate implements NNOperationTypes, Serializable {}
	class DoneUpdatingWeights implements NNOperationTypes, Serializable {}
}