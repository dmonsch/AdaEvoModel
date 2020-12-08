package adaevomodel.tools.experimental.pcm.test;

import java.io.File;
import java.util.List;

import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.repository.Repository;

import adaevomodel.base.shared.ModelUtil;
import adaevomodel.bridge.monitoring.util.MonitoringDataUtil;
import adaevomodel.tools.experimental.pcm.IRegressionOutlierDetection;
import adaevomodel.tools.experimental.pcm.RepositoryStoexChanges;
import adaevomodel.tools.experimental.pcm.outlier.NumericOutlierDetection;
import adaevomodel.tools.experimental.pcm.v2.ExecutionTimesExtractor;
import adaevomodel.tools.experimental.pcm.v2.ExecutionTimesExtractor.RegressionDataset;
import adaevomodel.tools.experimental.pcm.v2.GeneralizationAwareRegression;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import kieker.analysis.exception.AnalysisConfigurationException;

public class ExecutionTest {

	public static void main(String[] args) throws IllegalStateException, AnalysisConfigurationException {
		List<PCMContextRecord> data = MonitoringDataUtil.getMonitoringDataFromFiles(
				"/Users/david/Desktop/Dynamic Approach/Final Version/Implementation/git/adaevomodel.root/adaevomodel.tools.benchmarks/data/teastore_bench/monitoring/training/kieker-20200918-174928-864488559527-UTC--KIEKER");
		data.addAll(MonitoringDataUtil.getMonitoringDataFromFiles(
				"/Users/david/Desktop/Dynamic Approach/Final Version/Implementation/git/adaevomodel.root/adaevomodel.tools.benchmarks/data/teastore_bench/monitoring/training/kieker-20200918-175143-998860892919-UTC--KIEKER"));

		ExecutionTimesExtractor extractor = new ExecutionTimesExtractor();
		GeneralizationAwareRegression regr = new GeneralizationAwareRegression();
		IRegressionOutlierDetection outlierDetection = new NumericOutlierDetection(2.5f);

		RepositoryStoexChanges derivedChanges = new RepositoryStoexChanges();

		for (RegressionDataset dataset : extractor.extractDatasets(data)) {
			outlierDetection.filterOutliers(dataset);
			PCMRandomVariable nStoexExpression = regr.performRegression(dataset);
			System.out.println(nStoexExpression.getSpecification());
			derivedChanges.put(dataset.getActionId(), nStoexExpression);
		}

		Repository oldRepository = ModelUtil.readFromFile(new File(
				"/Users/david/Desktop/Dynamic Approach/Final Version/Implementation/git/adaevomodel.root/adaevomodel.tools.benchmarks/models/teastore/pcm/repository_0.repository"),
				Repository.class);
		derivedChanges.apply(oldRepository);
		ModelUtil.saveToFile(oldRepository, "repository_0.repository");

	}

}
