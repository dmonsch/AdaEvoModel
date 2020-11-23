package adaevomodel.tools.experimental.pcm.test;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class NeuralNetworkTest {
	public static boolean visualize = true;
	public static final int nEpochs = 1000;
	private static final int nSamples = 250;
	public static final int numInputs = 1;
	private static final int numOutputs = 1;

	public static void main(String[] args) {
		MultiLayerConfiguration network = getDeepDenseLayerNetworkConfiguration();
		final MultiLayerNetwork net = new MultiLayerNetwork(network);
		net.init();
		net.setListeners(new ScoreIterationListener(1));
		DataSet ds = generateDataset();
		for (int i = 0; i < nEpochs; i++) {
			net.fit(ds);
		}
		System.out.println(net.feedForward(Nd4j.create(new double[] { 6 }).reshape(1, 1)));
	}

	private static DataSet generateDataset() {
		double[] x = new double[nSamples];
		double[] y = new double[nSamples];

		for (int i = 0; i < nSamples; i++) {
			x[i] = i;
			y[i] = x[i] * x[i];
		}

		INDArray xa = Nd4j.create(x).reshape(nSamples, 1);
		INDArray ya = Nd4j.create(y).reshape(nSamples, 1);

		return new DataSet(xa, ya);
	}

	private static MultiLayerConfiguration getDeepDenseLayerNetworkConfiguration() {
		final int numHiddenNodes = 256;
		return new NeuralNetConfiguration.Builder().weightInit(WeightInit.NORMAL).updater(new Adam()).list()
				.layer(new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes).activation(Activation.RELU).build())
				.layer(new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes).activation(Activation.RELU)
						.build())
				.layer(new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes).activation(Activation.RELU)
						.build())
				.layer(new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes).activation(Activation.RELU)
						.build())
				.layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.IDENTITY)
						.nIn(numHiddenNodes).nOut(numOutputs).build())
				.build();
	}

}
