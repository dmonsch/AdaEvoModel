package adaevomodel.runtime.pipeline.validation;

import java.util.List;

import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import dmodel.designtime.monitoring.records.PCMContextRecord;

public interface IValidationProcessor {

	ValidationData process(InMemoryPCM instance, List<PCMContextRecord> monitoringData, String taskName);

	void clearSimulationData();

}
