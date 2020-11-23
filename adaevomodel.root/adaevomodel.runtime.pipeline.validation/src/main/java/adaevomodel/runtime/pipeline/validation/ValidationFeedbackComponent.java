package adaevomodel.runtime.pipeline.validation;

import java.util.List;

import org.pcm.headless.shared.data.results.InMemoryResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import adaevomodel.base.core.health.AbstractHealthStateComponent;
import adaevomodel.base.core.health.HealthState;
import adaevomodel.base.core.health.HealthStateObservedComponent;
import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import adaevomodel.runtime.pipeline.validation.data.ValidationMetricValue;
import adaevomodel.runtime.pipeline.validation.data.metric.IValidationMetric;
import adaevomodel.runtime.pipeline.validation.eval.ValidationDataExtractor;
import adaevomodel.runtime.pipeline.validation.simulation.HeadlessPCMSimulator;
import dmodel.designtime.monitoring.records.PCMContextRecord;

@Component
public class ValidationFeedbackComponent extends AbstractHealthStateComponent implements IValidationProcessor {
	@Autowired
	private HeadlessPCMSimulator simulator;

	@Autowired
	private ValidationDataExtractor extractor;

	@Autowired
	private List<IValidationMetric<?>> metrics;

	private boolean workingBefore = false;
	private boolean initial = true;

	public ValidationFeedbackComponent() {
		super(HealthStateObservedComponent.VALIDATION_CONTROLLER, HealthStateObservedComponent.CONFIGURATION);
	}

	@Scheduled(fixedRate = 10000L, initialDelay = 0)
	public void checkAvailability() {
		if (checkPreconditions()) {
			boolean reachable = simulator.isReachable();
			if (reachable && !workingBefore) {
				super.removeAllProblems();
				super.updateState();
				workingBefore = true;
				initial = false;
			} else if (!reachable && (workingBefore || initial)) {
				super.reportError(
						"Headless PCM simulator is not reachable. Please check your configuration and/or the availability of the headless simulator.");
				super.updateState();
				workingBefore = false;
				initial = false;
			}
		}
	}

	@Override
	public ValidationData process(InMemoryPCM instance, List<PCMContextRecord> monitoringData, String taskName) {
		// 1. simulate it
		InMemoryResultRepository analysisResults = simulator.simulateBlocking(instance, taskName);
		if (analysisResults == null) {
			return new ValidationData();
		}

		// 2. enrich with data
		ValidationData preparedData = extractor.extractValidationData(analysisResults, instance, monitoringData);

		// 3. derive metrics
		preparedData.getValidationPoints().stream().forEach(valPoint -> {
			metrics.forEach(metric -> {
				if (metric.isTarget(valPoint)) {
					ValidationMetricValue result = metric.calculate(valPoint);
					if (result != null)
						valPoint.getMetricValues().add(result);
				}
			});
		});

		return preparedData;
	}

	@Override
	public void clearSimulationData() {
		simulator.clearAllSimulationData();
	}

	@Override
	protected void onMessage(HealthStateObservedComponent source, HealthState state) {
		// nothing to do here because no deps
	}
}