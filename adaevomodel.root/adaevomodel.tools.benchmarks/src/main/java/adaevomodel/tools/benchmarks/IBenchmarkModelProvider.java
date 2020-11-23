package adaevomodel.tools.benchmarks;

import java.util.List;

import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import dmodel.designtime.monitoring.records.PCMContextRecord;

public interface IBenchmarkModelProvider {

	public InMemoryPCM provideModel(InMemoryPCM current, ValidationData validationData, List<PCMContextRecord> data);

}
