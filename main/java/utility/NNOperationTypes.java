package utility;

import java.io.Serializable;
import java.util.ArrayList;

import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.matrix.dense.Basic2DMatrix;
import org.neuroph.core.data.DataSetRow;
import org.neuroph.core.transfer.TransferFunction;

import akka.actor.ActorRef;

public interface NNOperationTypes {
	class ParameterResponse implements NNOperationTypes, Serializable { }
	class ParameterRequest implements NNOperationTypes, Serializable {
    }
	
	class Gradient implements NNOperationTypes, Serializable {
		// Changing to CSV format string because of Serialization issues. Will convert back to matrix on PSShard.
		private String gradient;
		public Gradient(String gradient) {
			this.gradient = gradient;
		}
		public String getGradient() {
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

	class Predict implements NNOperationTypes, Serializable {
		public Vector x;
		public Predict(Vector x) {
			this.x = x;
		}
	}
	
	class WeightUpdate implements NNOperationTypes, Serializable {}
	class DoneUpdatingWeights implements NNOperationTypes, Serializable {}
	class Dummy implements NNOperationTypes, Serializable {}
	
	public class DataShardParams implements NNOperationTypes, Serializable {
		public ArrayList<DataSetRow> dataSetPart;
		public TransferFunction activation;
		public ArrayList<ActorRef> parameterShardRefs;
		public int d_id;
		
		public DataShardParams(int d_id, ArrayList<DataSetRow> dataSetPart, TransferFunction activation, ArrayList<ActorRef> parameterShardRefs) {
			this.d_id = d_id;
			this.dataSetPart = dataSetPart;
			this.activation = activation;
			this.parameterShardRefs = parameterShardRefs;
		}
	}
	
}