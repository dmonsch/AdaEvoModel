package adaevomodel.tools.benchmarks.validation.results;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.Data;

@Data
public class SimulationDataValidationResults {

	private Map<String, SimulationDataValidationSummary> summaries;

	public SimulationDataValidationResults() {
		this.summaries = Maps.newHashMap();
	}

}
