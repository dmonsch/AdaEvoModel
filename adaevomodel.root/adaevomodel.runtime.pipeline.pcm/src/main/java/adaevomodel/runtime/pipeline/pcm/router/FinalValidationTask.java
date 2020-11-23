package adaevomodel.runtime.pipeline.pcm.router;

import adaevomodel.base.core.state.EPipelineTransformation;
import adaevomodel.base.core.state.ExecutionMeasuringPoint;
import adaevomodel.base.core.state.ValidationSchedulePoint;
import adaevomodel.base.shared.pipeline.PortIDs;
import adaevomodel.runtime.pipeline.AbstractIterativePipelinePart;
import adaevomodel.runtime.pipeline.annotation.InputPort;
import adaevomodel.runtime.pipeline.annotation.InputPorts;
import adaevomodel.runtime.pipeline.blackboard.RuntimePipelineBlackboard;
import adaevomodel.runtime.pipeline.data.PartitionedMonitoringData;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import lombok.extern.java.Log;

@Log
public class FinalValidationTask extends AbstractIterativePipelinePart<RuntimePipelineBlackboard> {

	public FinalValidationTask() {
		super(ExecutionMeasuringPoint.T_VALIDATION_4, EPipelineTransformation.T_VALIDATION3);
	}

	@InputPorts({ @InputPort(PortIDs.T_RAW_FINAL_VALIDATION), @InputPort(PortIDs.T_FINAL_VALIDATION) })
	public ValidationData validateFinal(PartitionedMonitoringData<PCMContextRecord> recs) {
		super.trackStart();

		log.info("Start simulation of the current models.");
		// simulate with validation data
		getBlackboard().getValidationQuery().process(getBlackboard().getPcmQuery().getRaw(), recs.getValidationData(),
				ValidationSchedulePoint.FINAL);

		// finish
		super.trackEnd();

		return getBlackboard().getValidationResultsQuery().get(ValidationSchedulePoint.FINAL);
	}

}
