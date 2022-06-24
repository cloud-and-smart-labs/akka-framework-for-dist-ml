package main;

import java.io.Serializable;
import java.util.ArrayList;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.transfer.TransferFunction;

public class NNJobMessage implements Serializable {
	private String payload;
    private DataSet dataset;
	private DataSet testSet;
    private int dataPerReplica;
    private ArrayList<Integer> layerDimensions;
    private double learningRate;
    private TransferFunction activation;
	private int epochs;
    // TODO: Add fields for activation and activationDerivative
    
    
    public NNJobMessage(String payload, DataSet dataset, DataSet testSet, int dataPerReplica, TransferFunction activation, ArrayList<Integer> layerDimensions, double learningRate, int epochs) {
    	this.payload = payload;
        this.dataset = dataset;
        this.dataPerReplica = dataPerReplica;
        this.activation = activation;
        this.layerDimensions = layerDimensions;
        this.learningRate = learningRate;
		this.testSet = testSet;
		this.epochs = epochs;
    }

	public int getNumOfEpoch() {
		return epochs;
	}

	public DataSet getTestData() {
		return testSet;
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