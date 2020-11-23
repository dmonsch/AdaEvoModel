package adaevomodel.runtime.pipeline.entry.transformation;

import adaevomodel.base.core.state.EPipelineTransformation;
import adaevomodel.base.core.state.ExecutionMeasuringPoint;
import adaevomodel.base.core.state.ValidationSchedulePoint;
import adaevomodel.base.shared.pipeline.PortIDs;
import adaevomodel.runtime.pipeline.AbstractIterativePipelinePart;
import adaevomodel.runtime.pipeline.annotation.InputPort;
import adaevomodel.runtime.pipeline.annotation.InputPorts;
import adaevomodel.runtime.pipeline.annotation.OutputPort;
import adaevomodel.runtime.pipeline.annotation.OutputPorts;
import adaevomodel.runtime.pipeline.blackboard.RuntimePipelineBlackboard;
import adaevomodel.runtime.pipeline.data.PartitionedMonitoringData;
import adaevomodel.runtime.pipeline.inm.transformation.InstrumentationModelTransformation;
import adaevomodel.runtime.pipeline.pcm.router.FinalValidationTask;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import lombok.extern.java.Log;

@Log
public class PrePipelineValidationTask extends AbstractIterativePipelinePart<RuntimePipelineBlackboard> {

	public PrePipelineValidationTask() {
		super(ExecutionMeasuringPoint.T_VALIDATION_1, EPipelineTransformation.PRE_VALIDATION);
	}

	@InputPorts({ @InputPort(PortIDs.T_VAL_PRE) })
	@OutputPorts({
			@OutputPort(to = InstrumentationModelTransformation.class, id = PortIDs.T_PRE_VAL_IMM, async = false),
			@OutputPort(to = FinalValidationTask.class, id = PortIDs.T_RAW_FINAL_VALIDATION, async = false) })
	public PartitionedMonitoringData<PCMContextRecord> prePipelineValidation(
			PartitionedMonitoringData<PCMContextRecord> recs) {
		super.trackStart();

		log.info("Start simulation of the current models.");
		// simulate using all monitoring data
		getBlackboard().getValidationQuery().process(getBlackboard().getPcmQuery().getRaw(), recs.getAllData(),
				ValidationSchedulePoint.PRE_PIPELINE);

		// finish
		super.trackEnd();

		// pass data
		return recs;
	}

}
