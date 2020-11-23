package adaevomodel.tools.experimental.pcm;

import org.palladiosimulator.pcm.core.PCMRandomVariable;

import adaevomodel.tools.experimental.pcm.v2.ExecutionTimesExtractor;

public interface IParametricRegression {

	public PCMRandomVariable performRegression(ExecutionTimesExtractor.RegressionDataset dataset);

}
