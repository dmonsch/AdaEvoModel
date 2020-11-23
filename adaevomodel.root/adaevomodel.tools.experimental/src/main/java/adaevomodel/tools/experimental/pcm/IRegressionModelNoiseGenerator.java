package adaevomodel.tools.experimental.pcm;

import org.palladiosimulator.pcm.core.PCMRandomVariable;

public interface IRegressionModelNoiseGenerator {

	public PCMRandomVariable generateNoise(double[] deviations, int maxSize);

}
