package adaevomodel.tools.experimental.pcm.v2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.beust.jcommander.internal.Maps;
import com.google.common.collect.Lists;

import adaevomodel.bridge.monitoring.util.ServiceParametersWrapper;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import dmodel.designtime.monitoring.records.ResponseTimeRecord;
import dmodel.designtime.monitoring.records.ServiceCallRecord;
import lombok.Data;

public class ExecutionTimesExtractor {
	private static final double NANO_TO_MS = 1000000;

	public List<RegressionDataset> extractDatasets(List<PCMContextRecord> data) {
		// get parameters
		Map<String, Map<String, Double>> serviceExecutionParameterMapping = Maps.newHashMap();
		for (PCMContextRecord rec : data) {
			if (rec instanceof ServiceCallRecord) {
				ServiceCallRecord sc = (ServiceCallRecord) rec;
				Map<String, Object> parameters = ServiceParametersWrapper.buildFromJson(sc.getParameters())
						.getParameters();
				Map<String, Double> numberParameters = Maps.newHashMap();
				parameters.entrySet().forEach(e -> {
					if (e.getValue() instanceof Number) {
						numberParameters.put(e.getKey(), ((Number) e.getValue()).doubleValue());
					}
				});
				serviceExecutionParameterMapping.put(sc.getServiceExecutionId(), numberParameters);
			}
		}

		// build dataset
		Map<String, RegressionDataset> output = Maps.newHashMap();
		for (PCMContextRecord rec : data) {
			if (rec instanceof ResponseTimeRecord) {
				ResponseTimeRecord rtr = (ResponseTimeRecord) rec;
				if (serviceExecutionParameterMapping.containsKey(rtr.getServiceExecutionId())) {
					if (!output.containsKey(rtr.getInternalActionId())) {
						output.put(rtr.getInternalActionId(), new RegressionDataset(rtr.getInternalActionId()));
					}
					output.get(rtr.getInternalActionId()).records
							.add(Pair.of(serviceExecutionParameterMapping.get(rtr.getServiceExecutionId()),
									((double) rtr.getStopTime() - rtr.getStartTime()) / NANO_TO_MS));
				}
			}
		}

		return output.entrySet().stream().map(e -> e.getValue()).collect(Collectors.toList());
	}

	@Data
	public static class RegressionDataset {
		private List<Pair<Map<String, Double>, Double>> records;
		private String actionId;

		private RegressionDataset(String actionId) {
			this.records = Lists.newArrayList();
			this.actionId = actionId;
		}
	}

}
