package adaevomodel.tools.benchmarks.validation.results;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Data;

@Data
public class SimulationDataValidationSummary {

	private Map<String, List<StatisticalMetricsSummary>> metrics;

	public static SimulationDataValidationSummary from(SimulationDataValidationMetrics metrics) {
		SimulationDataValidationSummary summary = new SimulationDataValidationSummary();
		Map<String, List<StatisticalMetricsSummary>> init = Maps.newHashMap();
		inheritMetricSet(metrics.getKstests(), init, "kstest");
		inheritMetricSet(metrics.getWassersteins(), init, "wasserstein");
		summary.metrics = init;
		return summary;
	}

	private static void inheritMetricSet(Map<String, List<Double>> values,
			Map<String, List<StatisticalMetricsSummary>> output, String metricName) {
		values.entrySet().forEach(e -> {
			StatisticalMetricsSummary summary = buildSummary(e.getValue(), metricName);
			if (output.containsKey(e.getKey())) {
				output.get(e.getKey()).add(summary);
			} else {
				output.put(e.getKey(), Lists.newArrayList(summary));
			}
		});
	}

	private static StatisticalMetricsSummary buildSummary(List<Double> value, String metricName) {
		double[] copyArray = new double[value.size()];
		for (int i = 0; i < value.size(); i++) {
			copyArray[i] = value.get(i);
		}

		Percentile q1_percentile = new Percentile(25d);
		Percentile q2_percentile = new Percentile(50d);
		Percentile q3_percentile = new Percentile(75d);
		Mean mean = new Mean();

		StatisticalMetricsSummary sum = new StatisticalMetricsSummary();
		sum.setMetricName(metricName);
		sum.setQ1(q1_percentile.evaluate(copyArray));
		sum.setMedian(q2_percentile.evaluate(copyArray));
		sum.setQ3(q3_percentile.evaluate(copyArray));
		sum.setMin(Collections.min(value));
		sum.setMax(Collections.max(value));
		sum.setAvg(mean.evaluate(copyArray));

		return sum;
	}

	@Data
	private static class StatisticalMetricsSummary {
		private String metricName;

		private double Q1;
		private double median;
		private double Q3;
		private double min;
		private double max;
		private double avg;
	}

}
