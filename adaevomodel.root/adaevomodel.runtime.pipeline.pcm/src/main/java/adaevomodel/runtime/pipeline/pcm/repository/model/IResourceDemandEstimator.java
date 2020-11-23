package adaevomodel.runtime.pipeline.pcm.repository.model;

import java.util.Map;

import adaevomodel.runtime.pipeline.pcm.repository.MonitoringDataSet;
import adaevomodel.runtime.pipeline.pcm.repository.RepositoryStoexChanges;

public interface IResourceDemandEstimator {
	RepositoryStoexChanges derive(Map<String, Double> currentValidationAdjustment);

	void prepare(MonitoringDataSet data);

}
