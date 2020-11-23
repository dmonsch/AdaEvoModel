package adaevomodel.tools.evaluation;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import adaevomodel.base.shared.pcm.util.PCMUtils;
import org.pcm.headless.api.client.ISimulationResultListener;
import org.pcm.headless.api.client.PCMHeadlessClient;
import org.pcm.headless.api.client.SimulationClient;
import org.pcm.headless.shared.data.ESimulationType;
import org.pcm.headless.shared.data.config.HeadlessSimulationConfig;
import org.pcm.headless.shared.data.results.AbstractMeasureValue;
import org.pcm.headless.shared.data.results.DoubleMeasureValue;
import org.pcm.headless.shared.data.results.InMemoryResultRepository;
import org.pcm.headless.shared.data.results.LongMeasureValue;
import org.pcm.headless.shared.data.results.PlainDataMeasure;
import org.pcm.headless.shared.data.results.PlainDataSeries;

import com.google.common.collect.Sets;

import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.runtime.pipeline.validation.data.TimeValueDistribution;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import adaevomodel.runtime.pipeline.validation.data.ValidationPoint;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import lombok.extern.java.Log;

@Log
public class StandaloneValidationDataGenerator {
	private static final long TIMEOUT_VFL = 120000;
	private static final Set<String> SWAP_SET = Sets.newHashSet("_sefjUeJCEeO6l86uYUhhyw");

	private PCMHeadlessClient client;
	private SimpleSimulationConfig config;
	private MonitoringDataEnrichment enrichment;

	public StandaloneValidationDataGenerator(SimpleSimulationConfig config) {
		PCMUtils.loadPCMModels();

		this.client = new PCMHeadlessClient(buildBackendUrl(config.getUrl(), config.getPort()));
		this.config = config;
		this.enrichment = new MonitoringDataEnrichment();
	}

	public ValidationData extractValidationData(InMemoryPCM pcm, List<PCMContextRecord> monitoringData) {
		client.clear();
		InMemoryResultRepository analysis = simulateBlocking(pcm, "Standalone validation.");
		ValidationData data = new ValidationData();

		// start with simulation data
		List<ValidationPoint> validationPoints = analysis.getValues().stream().map(v -> {
			return ValidationPoint.builder().measuringPoint(v.getKey().getPoint())
					.id(v.getKey().getDesc().getId() + "-" + String.join("-", v.getKey().getPoint().getSourceIds()))
					.metricDescription(v.getKey().getDesc())
					.analysisDistribution(transformAnalysisData(v.getKey().getDesc().getId(), v.getValue())).build();
		}).collect(Collectors.toList());

		// add monitoring data
		enrichment.enrichWithMonitoringData(pcm, validationPoints, monitoringData);

		// set it
		data.setValidationPoints(validationPoints);

		// & finally return
		return data;
	}

	private TimeValueDistribution transformAnalysisData(String metricId, List<PlainDataSeries> series) {
		TimeValueDistribution res = new TimeValueDistribution();
		for (int serNumber = 0; serNumber < series.size(); serNumber++) {
			PlainDataSeries ser = series.get(serNumber);
			boolean isX = serNumber % 2 == 0;
			for (PlainDataMeasure measure : ser.getMeasures()) {
				if (isX) {
					res.addValueX(toDoubleValue(measure.getV()));
				} else {
					res.addValueY(toDoubleValue(measure.getV()));
				}
			}
		}

		if (SWAP_SET.contains(metricId)) {
			res.swapAxis();
		}

		return res;
	}

	private double toDoubleValue(AbstractMeasureValue v) {
		if (v instanceof DoubleMeasureValue) {
			return ((DoubleMeasureValue) v).getV();
		} else if (v instanceof LongMeasureValue) {
			return Long.valueOf(((LongMeasureValue) v).getV()).doubleValue();
		}
		return Double.NaN;
	}

	private InMemoryResultRepository simulateBlocking(InMemoryPCM pcm, String name) {
		// start simulation
		CountDownLatch signal = new CountDownLatch(1);
		ResultValueWrapper wrapper = new ResultValueWrapper();

		this.simulate(pcm, name, res -> {
			wrapper.setValue(res);
			signal.countDown();
		});

		try {
			signal.await();
		} catch (InterruptedException e) {
			log.warning("Thread interrupted while waiting on the simulation restults.");
		}

		return wrapper.isSet() ? wrapper.res : null;
	}

	private void simulate(InMemoryPCM pcm, String name, ISimulationResultListener listener) {
		try {
			// set properties
			SimulationClient simulationClient = client.prepareSimulation();
			if (simulationClient != null) {
				simulationClient.setSimulationConfig(HeadlessSimulationConfig.builder().type(ESimulationType.SIMUCOM)
						.experimentName(name).repetitions(1).maximumMeasurementCount(config.getMeasurements())
						.simulationTime(config.getSimulationTime()).build());
				simulationClient.setRepository(pcm.getRepository());
				simulationClient.setSystem(pcm.getSystem());
				simulationClient.setResourceEnvironment(pcm.getResourceEnvironmentModel());
				simulationClient.setAllocation(pcm.getAllocationModel());
				simulationClient.setUsageModel(pcm.getUsageModel());

				// transitive closure & sync
				simulationClient.createTransitiveClosure();
				simulationClient.sync();

				// start simulation
				simulationClient.executeSimulation(listener, TIMEOUT_VFL * 20);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String buildBackendUrl(String url, int port) {
		StringBuilder res = new StringBuilder();
		if (!url.startsWith("http://")) {
			res.append("http://");
		}
		res.append(url);
		res.append(":");
		res.append(port);
		res.append("/");

		return res.toString();
	}

	private class ResultValueWrapper {
		private InMemoryResultRepository res = null;

		void setValue(InMemoryResultRepository result) {
			res = result;
		}

		boolean isSet() {
			return res != null;
		}
	}

}
