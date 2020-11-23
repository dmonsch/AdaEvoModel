package adaevomodel.runtime.pipeline.blackboard.facade;

import java.util.Comparator;
import java.util.List;

import adaevomodel.base.core.state.ValidationSchedulePoint;
import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import dmodel.designtime.monitoring.records.PCMContextRecord;

public interface IValidationQueryFacade extends Comparator<ValidationData> {

	void process(InMemoryPCM raw, List<PCMContextRecord> data, ValidationSchedulePoint schedulePoint);

}
