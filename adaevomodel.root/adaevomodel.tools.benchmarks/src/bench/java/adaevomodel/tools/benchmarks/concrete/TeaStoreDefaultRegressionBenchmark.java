package adaevomodel.tools.benchmarks.concrete;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.pcm.headless.api.util.ModelUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import adaevomodel.base.core.config.ConfigurationContainer;
import adaevomodel.base.core.config.ScalingConfiguration;
import adaevomodel.base.core.config.ValidationFeedbackLoopConfiguration;
import adaevomodel.base.core.facade.IPCMQueryFacade;
import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.base.shared.pcm.LocalFilesystemPCM;
import adaevomodel.bridge.monitoring.util.MonitoringDataUtil;
import adaevomodel.runtime.pipeline.pcm.repository.RepositoryDerivation;
import adaevomodel.runtime.pipeline.pcm.repository.adjustment.impl.TreeScalingRepositoryDerivationAdjuster;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import adaevomodel.tools.benchmarks.IBenchmarkModelProvider;
import adaevomodel.tools.benchmarks.adapter.SimplePcmQueryFacade;
import adaevomodel.tools.benchmarks.adapter.SimpleRemAndMappingFacade;
import adaevomodel.tools.benchmarks.config.EValidationComponent;
import adaevomodel.tools.benchmarks.config.IBenchmarkConfiguration;
import adaevomodel.tools.benchmarks.impl.CalibrationBenchmarkImpl;
import adaevomodel.tools.benchmarks.init.BenchmarkDataProvider;
import adaevomodel.tools.benchmarks.json.JsonBenchmarkContainer;
import adaevomodel.tools.benchmarks.json.JsonBenchmarkData;
import adaevomodel.tools.evaluation.SimpleSimulationConfig;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import kieker.analysis.exception.AnalysisConfigurationException;
import lombok.extern.java.Log;

@Log
public class TeaStoreDefaultRegressionBenchmark {
	private ObjectMapper mapper = new ObjectMapper();
	private TreeScalingRepositoryDerivationAdjuster adjuster;

	public static void main(String[] args) {
		new TeaStoreDefaultRegressionBenchmark().execute();
	}

	private TeaStoreDefaultRegressionBenchmark() {
		adjuster = new TreeScalingRepositoryDerivationAdjuster();
		ConfigurationContainer config = new ConfigurationContainer();
		config.setVfl(new ValidationFeedbackLoopConfiguration());
		config.getVfl().setScaling(new ScalingConfiguration());
		config.getVfl().getScaling().setConstantDetermination(false);
		config.getVfl().getScaling().setFeedbackBasedScaling(false);
		config.getVfl().getScaling().setScalingEnabled(false);

		adjuster.setConfiguration(config);
	}

	// execute benchmark
	private void execute() {
		// load benchmark data
		JsonBenchmarkData teaStoreBenchmarkData;
		try {
			JsonBenchmarkContainer benchmarkContainer = mapper
					.readValue(getClass().getResourceAsStream("/benchmarks.json"), JsonBenchmarkContainer.class);
			teaStoreBenchmarkData = benchmarkContainer.getBenchmarks().get(0);
		} catch (IOException e) {
			log.warning("Failed to load benchmark data.");
			return;
		}

		// execute benchmark
		CalibrationBenchmarkImpl calibrationBenchmark = new CalibrationBenchmarkImpl();
		calibrationBenchmark.executeBenchmark(generateModelProvider(),
				generateBenchmarkConfiguration(teaStoreBenchmarkData));
	}

	private IBenchmarkModelProvider generateModelProvider() {
		List<String> mappings = Lists.newArrayList();
		try {
			mappings = Files.readAllLines(new File("models/teastore/mapping.txt").toPath(), Charset.forName("UTF-8"));
		} catch (IOException e) {
		}
		List<String> fmappings = new ArrayList<>(mappings);

		return new IBenchmarkModelProvider() {

			@Override
			public InMemoryPCM provideModel(InMemoryPCM current, ValidationData validationData,
					List<PCMContextRecord> data) {
				SimpleRemAndMappingFacade facade = new SimpleRemAndMappingFacade(fmappings, current);
				RepositoryDerivation derivation = new RepositoryDerivation();
				derivation.setMappingFacade(facade);
				derivation.setRemQuery(facade);
				derivation.setAdjuster(adjuster);

				IPCMQueryFacade pcmQuery = new SimplePcmQueryFacade(current);

				Set<String> allServices = ModelUtil.getObjects(current.getRepository(), ResourceDemandingSEFF.class)
						.stream().map(seff -> {
							return seff.getId();
						}).collect(Collectors.toSet());

				derivation.calibrateRepository(data, pcmQuery, validationData, allServices)
						.apply(current.getRepository());
				return current;
			}
		};
	}

	private IBenchmarkConfiguration generateBenchmarkConfiguration(JsonBenchmarkData data) {
		return new IBenchmarkConfiguration() {
			@Override
			public SimpleSimulationConfig provideSimulationConfiguration() {
				return SimpleSimulationConfig.builder().measurements(30000).simulationTime(200000).url("127.0.0.1")
						.port(8080).build();
			}

			@Override
			public File provideResultsFile() {
				return new File("results/benchmark_teastore_regression.json");
			}

			@Override
			public InMemoryPCM provideIterationModel(int iteration) {
				LocalFilesystemPCM fpcm = new LocalFilesystemPCM();
				fpcm.setAllocationModelFile(new File("models/teastore/pcm/allocation_" + iteration + ".allocation"));
				fpcm.setRepositoryFile(new File("models/teastore/pcm/repository_" + iteration + ".repository"));
				fpcm.setResourceEnvironmentFile(
						new File("models/teastore/pcm/resourceenv_" + iteration + ".resourceenvironment"));
				fpcm.setSystemFile(new File("models/teastore/pcm/system_" + iteration + ".system"));
				fpcm.setUsageModelFile(new File("models/teastore/pcm/usage_" + iteration + ".usagemodel"));
				return InMemoryPCM.createFromFilesystem(fpcm).copyDeep();
			}

			@Override
			public Set<EValidationComponent> getValidationComponents() {
				return Sets.newHashSet(EValidationComponent.SIMULATION_COMPARE_SERVICES_BACKWARD,
						EValidationComponent.SIMULATION_COMPARE_SERVICES_CROSS,
						EValidationComponent.SIMULATION_COMPARE_SERVICES_FORWARD);
			}

			@Override
			public List<String> getTargetServiceIds() {
				return Lists.newArrayList("_xliLoDVXEeqPG_FgW3bi6Q");
			}

			@Override
			public List<Pair<List<PCMContextRecord>, List<PCMContextRecord>>> getMonitoringData() {
				List<Pair<List<PCMContextRecord>, List<PCMContextRecord>>> ret = Lists.newArrayList();

				File monitoringBasePath = new File(new File(BenchmarkDataProvider.BASE_PATH, data.getPath()),
						"monitoring");
				File trainingBasePath = new File(monitoringBasePath, "training");
				File validationBasePath = new File(monitoringBasePath, "validation");

				File[] subFilesTraining = trainingBasePath.listFiles();
				File[] subFilesValidation = validationBasePath.listFiles();
				for (int i = 0; i < subFilesTraining.length; i++) {
					try {
						ret.add(Pair.of(
								MonitoringDataUtil.getMonitoringDataFromFiles(subFilesTraining[i].getAbsolutePath()),
								MonitoringDataUtil
										.getMonitoringDataFromFiles(subFilesValidation[i].getAbsolutePath())));
					} catch (IllegalStateException | AnalysisConfigurationException e) {
						e.printStackTrace();
					}
				}
				return ret;
			}

			@Override
			public JsonBenchmarkData getBenchmarkData() {
				return data;
			}
		};
	}

}
