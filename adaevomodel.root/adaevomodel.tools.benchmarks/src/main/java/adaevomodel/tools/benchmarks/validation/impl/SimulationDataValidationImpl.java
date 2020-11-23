package adaevomodel.tools.benchmarks.validation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.DoubleStream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.ml.distance.EarthMoversDistance;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.pcm.headless.shared.data.results.MeasuringPointType;

import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import adaevomodel.tools.benchmarks.config.EValidationComponent;
import adaevomodel.tools.benchmarks.config.IBenchmarkConfiguration;
import adaevomodel.tools.benchmarks.validation.IValidationComponent;
import adaevomodel.tools.benchmarks.validation.results.SimulationDataValidationMetrics;
import adaevomodel.tools.benchmarks.validation.results.SimulationDataValidationResults;
import adaevomodel.tools.benchmarks.validation.results.SimulationDataValidationSummary;
import adaevomodel.tools.evaluation.StandaloneValidationDataGenerator;
import dmodel.designtime.monitoring.records.PCMContextRecord;

public class SimulationDataValidationImpl implements IValidationComponent {
	private static final int SIMULATIONS_PER_ITERATION = 1;

	@Override
	public boolean active(Set<EValidationComponent> set) {
		return set.contains(EValidationComponent.SIMULATION_COMPARE_SERVICES_BACKWARD)
				|| set.contains(EValidationComponent.SIMULATION_COMPARE_SERVICES_CROSS)
				|| set.contains(EValidationComponent.SIMULATION_COMPARE_SERVICES_FORWARD);
	}

	@Override
	public Object getValidationData(List<InMemoryPCM> pcms,
			List<Pair<List<PCMContextRecord>, List<PCMContextRecord>>> monitoring, IBenchmarkConfiguration config) {
		StandaloneValidationDataGenerator validationDataGenerator = new StandaloneValidationDataGenerator(
				config.provideSimulationConfiguration());

		SimulationDataValidationResults results = new SimulationDataValidationResults();
		if (config.getValidationComponents().contains(EValidationComponent.SIMULATION_COMPARE_SERVICES_FORWARD)) {
			generateMetricsForward(pcms, monitoring, config, validationDataGenerator, results);
		}

		if (config.getValidationComponents().contains(EValidationComponent.SIMULATION_COMPARE_SERVICES_BACKWARD)) {
			generateMetricsBackward(pcms, monitoring, config, validationDataGenerator, results);
		}

		if (config.getValidationComponents().contains(EValidationComponent.SIMULATION_COMPARE_SERVICES_CROSS)) {
			generateMetricsCross(pcms, monitoring, config, validationDataGenerator, results);
		}

		return results;
	}

	private void generateMetricsCross(List<InMemoryPCM> pcms,
			List<Pair<List<PCMContextRecord>, List<PCMContextRecord>>> monitoring, IBenchmarkConfiguration config,
			StandaloneValidationDataGenerator validationDataGenerator, SimulationDataValidationResults results) {
		SimulationDataValidationMetrics current = null;
		for (int i = 0; i < pcms.size(); i++) {
			if (i != 0) {
				InMemoryPCM pcm = pcms.get(i);
				List<PCMContextRecord> monitoringData = new ArrayList<>(monitoring.get(i - 1).getLeft());
				SimulationDataValidationMetrics metrics = generateMetrics(pcm, monitoringData, validationDataGenerator,
						config);
				if (current == null) {
					current = metrics;
				} else {
					current.inherit(metrics);
				}
			}
		}
		results.getSummaries().put("cross", SimulationDataValidationSummary.from(current));
	}

	private void generateMetricsBackward(List<InMemoryPCM> pcms,
			List<Pair<List<PCMContextRecord>, List<PCMContextRecord>>> monitoring, IBenchmarkConfiguration config,
			StandaloneValidationDataGenerator validationDataGenerator, SimulationDataValidationResults results) {
		SimulationDataValidationMetrics current = null;
		for (int i = 1; i < pcms.size(); i++) {
			if (i != 0) {
				InMemoryPCM curr = pcms.get(i);
				for (int u = i - 1; u > 0; u--) {
					InMemoryPCM pcm = pcms.get(u);

					InMemoryPCM pcmToUse = pcm.copyDeep();
					pcmToUse.setRepository(curr.copyDeep().getRepository());

					List<PCMContextRecord> monitoringData = new ArrayList<>(monitoring.get(u - 1).getLeft());
					SimulationDataValidationMetrics metrics = generateMetrics(pcmToUse.copyDeep(), monitoringData,
							validationDataGenerator, config);
					if (current == null) {
						current = metrics;
					} else {
						current.inherit(metrics);
					}
				}
			}
		}
		results.getSummaries().put("backward", SimulationDataValidationSummary.from(current));
	}

	private void generateMetricsForward(List<InMemoryPCM> pcms,
			List<Pair<List<PCMContextRecord>, List<PCMContextRecord>>> monitoring, IBenchmarkConfiguration config,
			StandaloneValidationDataGenerator validationDataGenerator, SimulationDataValidationResults results) {
		SimulationDataValidationMetrics current = null;
		for (int i = 0; i < pcms.size() - 1; i++) {
			if (i != 0) {
				InMemoryPCM curr = pcms.get(i);
				for (int u = i + 1; u < pcms.size(); u++) {
					InMemoryPCM pcm = pcms.get(u);

					InMemoryPCM pcmToUse = pcm.copyDeep();
					pcmToUse.setRepository(curr.copyDeep().getRepository());

					List<PCMContextRecord> monitoringData = new ArrayList<>(monitoring.get(u - 1).getLeft());
					SimulationDataValidationMetrics metrics = generateMetrics(pcmToUse.copyDeep(), monitoringData,
							validationDataGenerator, config);
					if (current == null) {
						current = metrics;
					} else {
						current.inherit(metrics);
					}
				}
			}
		}
		results.getSummaries().put("forward", SimulationDataValidationSummary.from(current));
	}

	private SimulationDataValidationMetrics generateMetrics(InMemoryPCM pcm, List<PCMContextRecord> monitoringData,
			StandaloneValidationDataGenerator validationDataGenerator, IBenchmarkConfiguration config) {
		SimulationDataValidationMetrics metrics = new SimulationDataValidationMetrics();
		for (int i = 0; i < SIMULATIONS_PER_ITERATION; i++) {
			ValidationData data = validationDataGenerator.extractValidationData(pcm, monitoringData);
			data.getValidationPoints().forEach(vp -> {
				MeasuringPointType type = vp.getMeasuringPoint().getType();
				if (type == MeasuringPointType.ASSEMBLY_OPERATION || type == MeasuringPointType.ENTRY_LEVEL_CALL) {
					if (config.getTargetServiceIds().contains(vp.getServiceId())) {
						double[] x = vp.getMonitoringDistribution().yAxis();
						double[] y = vp.getAnalysisDistribution().yAxis();

						double kstest = new KolmogorovSmirnovTest().kolmogorovSmirnovStatistic(x, y);
						double wasserstein = wsDistance(x, y, false);

						metrics.addKS(vp.getServiceId(), kstest);
						metrics.addWS(vp.getServiceId(), wasserstein);
					}
				}
			});
		}
		metrics.calculateMedians();
		return metrics;
	}

	private double wsDistance(double[] a, double[] b, boolean normed) {
		Arrays.sort(a);
		Arrays.sort(b);

		double minA = DoubleStream.of(a).min().getAsDouble();
		double minB = DoubleStream.of(b).min().getAsDouble();
		double maxA = DoubleStream.of(a).max().getAsDouble();
		double maxB = DoubleStream.of(b).max().getAsDouble();

		int minAB = (int) Math.floor(Math.min(minA, minB));
		int maxAB = (int) Math.ceil(Math.min(maxA, maxB));
		double[] transA = new double[maxAB - minAB];
		double[] transB = new double[maxAB - minAB];
		int currentPos = 0;

		while (currentPos < transA.length) {
			int currentLower = minAB + currentPos;
			int currentUpper = minAB + currentPos + 1;

			transA[currentPos] = (double) DoubleStream.of(a).filter(d -> d >= currentLower && d < currentUpper).count()
					/ (double) a.length;
			transB[currentPos++] = (double) DoubleStream.of(b).filter(d -> d >= currentLower && d < currentUpper)
					.count() / (double) b.length;
		}

		return new EarthMoversDistance().compute(transA, transB) / (normed ? (transA.length - 1) : 1d);

		// for ks test
		// return new KolmogorovSmirnovTest().kolmogorovSmirnovStatistic(a, b);
	}

}
