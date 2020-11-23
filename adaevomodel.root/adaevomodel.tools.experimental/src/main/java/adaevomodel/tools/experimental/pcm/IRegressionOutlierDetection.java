package adaevomodel.tools.experimental.pcm;

import adaevomodel.tools.experimental.pcm.v2.ExecutionTimesExtractor.RegressionDataset;

public interface IRegressionOutlierDetection {
	public void filterOutliers(RegressionDataset dataset);

	public double[] filterOutliers(double[] data);
}
