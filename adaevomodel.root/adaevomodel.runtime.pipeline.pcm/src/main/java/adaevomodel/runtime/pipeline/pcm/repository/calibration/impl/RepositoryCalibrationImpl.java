package adaevomodel.runtime.pipeline.pcm.repository.calibration.impl;

import java.util.List;
import java.util.Set;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.springframework.stereotype.Component;

import adaevomodel.base.core.facade.IPCMQueryFacade;
import adaevomodel.runtime.pipeline.pcm.repository.IRegressionOutlierDetection;
import adaevomodel.runtime.pipeline.pcm.repository.IRepositoryCalibration;
import adaevomodel.runtime.pipeline.pcm.repository.calibration.ExecutionTimesExtractor;
import adaevomodel.runtime.pipeline.pcm.repository.calibration.ExecutionTimesExtractor.RegressionDataset;
import adaevomodel.runtime.pipeline.pcm.repository.calibration.GeneralizationAwareRegression;
import adaevomodel.runtime.pipeline.pcm.repository.calibration.RepositoryStoexChanges;
import adaevomodel.runtime.pipeline.pcm.repository.outlier.NumericOutlierDetection;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import lombok.extern.java.Log;

@Component
@Log
public class RepositoryCalibrationImpl implements IRepositoryCalibration {
	private ExecutionTimesExtractor executionTimesExtractor;
	private GeneralizationAwareRegression regression;
	private IRegressionOutlierDetection outlierDetection;

	public RepositoryCalibrationImpl() {
		executionTimesExtractor = new ExecutionTimesExtractor();
		regression = new GeneralizationAwareRegression();
		outlierDetection = new NumericOutlierDetection(2.5f);
	}

	@Override
	public RepositoryStoexChanges calibrateRepository(List<PCMContextRecord> data, IPCMQueryFacade pcm,
			ValidationData validation, Set<String> fineGraindInstrumentedServiceIds) {
		log.info("Start resource demand calibration.");
		RepositoryStoexChanges derivedChanges = new RepositoryStoexChanges();

		log.info("Extract datasets.");
		for (RegressionDataset dataset : executionTimesExtractor.extractDatasets(data)) {
			try {
				log.info("Filter outliers.");
				outlierDetection.filterOutliers(dataset);
				log.info("Perform regression.");
				PCMRandomVariable nStoexExpression = regression.performRegression(dataset);
				derivedChanges.put(dataset.getActionId(), nStoexExpression);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		log.info("Finished resource demand calibration.");

		return derivedChanges;
	}

}
