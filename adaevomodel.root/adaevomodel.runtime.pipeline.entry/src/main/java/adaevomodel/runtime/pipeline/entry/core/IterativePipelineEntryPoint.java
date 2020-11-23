package adaevomodel.runtime.pipeline.entry.core;

import java.util.List;
import java.util.stream.Collectors;

import adaevomodel.base.core.state.ExecutionMeasuringPoint;
import adaevomodel.base.shared.pipeline.PortIDs;
import adaevomodel.runtime.pipeline.AbstractIterativePipelinePart;
import adaevomodel.runtime.pipeline.annotation.EntryInputPort;
import adaevomodel.runtime.pipeline.annotation.OutputPort;
import adaevomodel.runtime.pipeline.annotation.OutputPorts;
import adaevomodel.runtime.pipeline.annotation.PipelineEntryPoint;
import adaevomodel.runtime.pipeline.blackboard.RuntimePipelineBlackboard;
import adaevomodel.runtime.pipeline.data.PartitionedMonitoringData;
import adaevomodel.runtime.pipeline.data.SessionPartionedMonitoringData;
import adaevomodel.runtime.pipeline.entry.transformation.PrePipelineValidationTask;
import adaevomodel.runtime.pipeline.pcm.router.AccuracySwitch;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import kieker.common.record.IMonitoringRecord;
import lombok.extern.java.Log;

@PipelineEntryPoint
@Log
public class IterativePipelineEntryPoint extends AbstractIterativePipelinePart<RuntimePipelineBlackboard> {

	public IterativePipelineEntryPoint() {
		super(ExecutionMeasuringPoint.T_PRE_FILTER, null);
	}

	@EntryInputPort
	@OutputPorts({ @OutputPort(id = PortIDs.T_VAL_PRE, to = PrePipelineValidationTask.class, async = false),
			@OutputPort(id = PortIDs.T_RAW_ROUTER, to = AccuracySwitch.class, async = false) })
	public PartitionedMonitoringData<PCMContextRecord> filterMonitoringData(
			PartitionedMonitoringData<IMonitoringRecord> monitoringData) {
		List<IMonitoringRecord> records = monitoringData.getAllData();

		// EVALUATION
		getBlackboard().getQuery().trackStartPipelineExecution();
		getBlackboard().getQuery().trackRecordCount(records.size());

		super.trackStart();
		// original logic
		log.info("Reached entry (size = " + records.size() + ").");
		List<PCMContextRecord> result = records.stream().filter(r -> r instanceof PCMContextRecord)
				.map(PCMContextRecord.class::cast).collect(Collectors.toList());

		super.trackEnd();

		return new SessionPartionedMonitoringData(result, monitoringData.getValidationSplit());
	}

}
