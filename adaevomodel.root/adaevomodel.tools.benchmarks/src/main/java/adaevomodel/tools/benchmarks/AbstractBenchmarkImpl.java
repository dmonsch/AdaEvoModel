package adaevomodel.tools.benchmarks;

import adaevomodel.tools.benchmarks.init.BenchmarkDataProvider;
import adaevomodel.tools.benchmarks.json.JsonBenchmarkData;
import lombok.extern.java.Log;

@Log
public abstract class AbstractBenchmarkImpl implements IBenchmark {

	protected boolean prepareBenchmark(JsonBenchmarkData data) {
		log.info("Loading dataset.");
		boolean success = BenchmarkDataProvider.prepareBenchmarkData(data);
		if (success) {
			log.info("Successfully loaded benchmark data.");
		} else {
			log.warning("Failed to load benchmark data.");
		}
		return success;
	}

}
