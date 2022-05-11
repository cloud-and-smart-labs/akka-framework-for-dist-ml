package main;

import java.io.Serializable;
import java.util.ArrayList;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.transfer.TransferFunction;
import org.neuroph.util.TransferFunctionType;

public class NNJobMessage implements Serializable {
	private String payload;
    private DataSet dataset;
    private int dataPerReplica;
    private ArrayList<Integer> layerDimensions;
    private double learningRate;
    private TransferFunction activation;
    // TODO: Add fields for activation and activationDerivative
    
    
    public NNJobMessage(String payload, DataSet dataset, int dataPerReplica, TransferFunction activation, ArrayList<Integer> layerDimensions, double learningRate) {
    	this.payload = payload;
        this.dataset = dataset;
        this.dataPerReplica = dataPerReplica;
        this.activation = activation;
        this.layerDimensions = layerDimensions;
        this.learningRate = learningRate;
    }
       
    public TransferFunction getActivation() {
		return activation;
	}

	public String getPayload() {
		return payload;
	}

	public int getDataPerReplica() {
		return dataPerReplica;
	}

	public ArrayList<Integer> getLayerDimensions() {
		return layerDimensions;
	}

	public double getLearningRate() {
		return learningRate;
	}

	public DataSet getDataset() {
    	return dataset;
    }
}