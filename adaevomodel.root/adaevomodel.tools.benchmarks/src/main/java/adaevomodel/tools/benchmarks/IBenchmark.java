package adaevomodel.tools.benchmarks;

import adaevomodel.tools.benchmarks.config.IBenchmarkConfiguration;

public interface IBenchmark {

	public void executeBenchmark(IBenchmarkModelProvider modelProvider, IBenchmarkConfiguration configuration);

}
