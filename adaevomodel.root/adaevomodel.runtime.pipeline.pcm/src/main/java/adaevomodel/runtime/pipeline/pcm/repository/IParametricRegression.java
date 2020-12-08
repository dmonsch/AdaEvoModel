package adaevomodel.runtime.pipeline.pcm.repository;

import org.palladiosimulator.pcm.core.PCMRandomVariable;

import adaevomodel.runtime.pipeline.pcm.repository.calibration.ExecutionTimesExtractor;

public interface IParametricRegression {

	public PCMRandomVariable performRegression(ExecutionTimesExtractor.RegressionDataset dataset);

}
