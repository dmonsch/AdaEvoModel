package adaevomodel.tools.benchmarks.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.pcm.headless.api.util.ModelUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.tools.benchmarks.AbstractBenchmarkImpl;
import adaevomodel.tools.benchmarks.IBenchmarkModelProvider;
import adaevomodel.tools.benchmarks.config.IBenchmarkConfiguration;
import adaevomodel.tools.benchmarks.validation.IValidationComponent;
import adaevomodel.tools.benchmarks.validation.impl.SimulationDataValidationImpl;
import adaevomodel.tools.benchmarks.validation.results.ValidationResultsContainer;
import adaevomodel.tools.evaluation.StandaloneValidationDataGenerator;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import lombok.extern.java.Log;

@Log
public class CalibrationBenchmarkImpl extends AbstractBenchmarkImpl {
	private static final List<Class<? extends IValidationComponent>> VALIDATION_COMPONENTS = new ArrayList<>() {
		/**
		 * Serial UID.
		 */
		private static final long serialVersionUID = 8049676432716758234L;
		{
			// val components
			add(SimulationDataValidationImpl.class);
		}
	};

	private List<IValidationComponent> validationComponents;

	public CalibrationBenchmarkImpl() {
		super();
		validationComponents = Lists.newArrayList();
		for (Class<? extends IValidationComponent> clz : VALIDATION_COMPONENTS) {
			try {
				validationComponents.add(clz.getConstructor().newInstance());
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				log.warning("Could not create an instance of the '" + clz.getName() + "' validation class.");
			}
		}
	}

	@Override
	public void executeBenchmark(IBenchmarkModelProvider modelProvider, IBenchmarkConfiguration configuration) {
		StandaloneValidationDataGenerator validationDataGenerator = new StandaloneValidationDataGenerator(
				configuration.provideSimulationConfiguration());
		// prepare data
		boolean success = super.prepareBenchmark(configuration.getBenchmarkData());
		if (!success) {
			return;
		}

		// execute benchmark
		log.info("Prepare models.");
		List<InMemoryPCM> pcmModels = Lists.newArrayList(configuration.provideIterationModel(0));
		InMemoryPCM currentPcm = pcmModels.get(0);
		List<Pair<List<PCMContextRecord>, List<PCMContextRecord>>> monitoringData = configuration.getMonitoringData();
		int k = 1;
		for (Pair<List<PCMContextRecord>, List<PCMContextRecord>> data : monitoringData) {
			log.info("Start building a model.");
			InMemoryPCM nextPcm = modelProvider.provideModel(configuration.provideIterationModel(k++),
					validationDataGenerator.extractValidationData(currentPcm, data.getRight()), data.getLeft());
			pcmModels.add(nextPcm);
			currentPcm = nextPcm;
		}
		log.info("Builded models.");

		// save models
		File basePath = new File("results/models/");
		if (!basePath.exists()) {
			basePath.mkdirs();
		}
		for (int i = 0; i < pcmModels.size(); i++) {
			ModelUtil.saveToFile(pcmModels.get(i).getRepository(), new File(basePath, "teastore_" + i + ".repository"));
		}

		// execute validations
		log.info("Process validation.");
		ValidationResultsContainer valResults = new ValidationResultsContainer();
		for (IValidationComponent comp : validationComponents) {
			if (comp.active(configuration.getValidationComponents())) {
				valResults.getResults().add(comp.getValidationData(pcmModels, monitoringData, configuration));
			}
		}
		log.info("Processed validation.");

		// write to output file
		try {
			new ObjectMapper().writeValue(configuration.provideResultsFile(), valResults);
		} catch (IOException e) {
			e.printStackTrace();
			log.warning("Failed to write validation results.");
		}

	}

}
