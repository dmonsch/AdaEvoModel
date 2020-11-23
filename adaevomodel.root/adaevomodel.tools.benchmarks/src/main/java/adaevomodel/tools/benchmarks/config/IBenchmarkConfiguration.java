package adaevomodel.tools.benchmarks.config;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.tools.benchmarks.json.JsonBenchmarkData;
import adaevomodel.tools.evaluation.SimpleSimulationConfig;
import dmodel.designtime.monitoring.records.PCMContextRecord;

public interface IBenchmarkConfiguration {

	public InMemoryPCM provideIterationModel(int iteration);

	public File provideResultsFile();

	public SimpleSimulationConfig provideSimulationConfiguration();

	public List<String> getTargetServiceIds();

	public Set<EValidationComponent> getValidationComponents();

	public JsonBenchmarkData getBenchmarkData();

	public List<Pair<List<PCMContextRecord>, List<PCMContextRecord>>> getMonitoringData();

}
