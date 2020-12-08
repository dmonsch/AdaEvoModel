package adaevomodel.runtime.pipeline.pcm.repository.outlier;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import adaevomodel.runtime.pipeline.pcm.repository.IRegressionOutlierDetection;
import adaevomodel.runtime.pipeline.pcm.repository.calibration.ExecutionTimesExtractor.RegressionDataset;

public class NumericOutlierDetection implements IRegressionOutlierDetection {
	private Percentile upperPercentile;
	private Percentile lowerPercentile;

	public NumericOutlierDetection(float percentile) {
		this.upperPercentile = new Percentile(100d - percentile);
		this.lowerPercentile = new Percentile(percentile);
	}

	@Override
	public void filterOutliers(RegressionDataset dataset) {
		double[] values = dataset.getRecords().stream().map(r -> r.getValue()).mapToDouble(d -> d).toArray();
		double valueUpperPercentile = upperPercentile.evaluate(values);
		double valueLowerPercentile = lowerPercentile.evaluate(values);

		dataset.setRecords(dataset.getRecords().stream().filter(r -> {
			return r.getValue() >= valueLowerPercentile && r.getValue() <= valueUpperPercentile;
		}).collect(Collectors.toList()));
	}

	@Override
	public double[] filterOutliers(double[] data) {
		double valueUpperPercentile = upperPercentile.evaluate(data);
		double valueLowerPercentile = lowerPercentile.evaluate(data);
		return Arrays.stream(data).filter(d -> {
			return d >= valueLowerPercentile && d <= valueUpperPercentile;
		}).toArray();
	}

}
