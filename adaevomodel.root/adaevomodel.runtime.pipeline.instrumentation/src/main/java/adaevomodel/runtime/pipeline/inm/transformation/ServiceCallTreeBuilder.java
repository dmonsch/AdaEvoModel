package adaevomodel.runtime.pipeline.inm.transformation;

import java.util.List;
import java.util.stream.Collectors;

import adaevomodel.base.core.state.ExecutionMeasuringPoint;
import adaevomodel.base.shared.pipeline.PortIDs;
import adaevomodel.base.shared.structure.Tree;
import adaevomodel.bridge.monitoring.util.MonitoringDataUtil;
import adaevomodel.runtime.pipeline.AbstractIterativePipelinePart;
import adaevomodel.runtime.pipeline.annotation.InputPort;
import adaevomodel.runtime.pipeline.annotation.InputPorts;
import adaevomodel.runtime.pipeline.annotation.OutputPort;
import adaevomodel.runtime.pipeline.annotation.OutputPorts;
import adaevomodel.runtime.pipeline.blackboard.RuntimePipelineBlackboard;
import adaevomodel.runtime.pipeline.data.PartitionedMonitoringData;
import adaevomodel.runtime.pipeline.pcm.resourceenv.ResourceEnvironmentTransformation;
import adaevomodel.runtime.pipeline.pcm.router.AccuracySwitch;
import adaevomodel.runtime.pipeline.pcm.system.RuntimeSystemTransformation;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import dmodel.designtime.monitoring.records.ServiceCallRecord;
import lombok.extern.java.Log;

@Log
public class ServiceCallTreeBuilder extends AbstractIterativePipelinePart<RuntimePipelineBlackboard> {

	public ServiceCallTreeBuilder() {
		super(ExecutionMeasuringPoint.T_SERVICE_CALL_TREE, null);
	}

	/* @formatter:off */
	@InputPorts(@InputPort(PortIDs.T_BUILD_SERVICECALL_TREE))
	@OutputPorts({
		@OutputPort(to = ResourceEnvironmentTransformation.class, async = false, id = PortIDs.T_SC_PCM_RESENV),
		@OutputPort(to = RuntimeSystemTransformation.class, async = false, id = PortIDs.T_SC_PCM_SYSTEM),
		@OutputPort(to = AccuracySwitch.class, async = false, id = PortIDs.T_SC_ROUTER)
	})
	/* @formatter:on */
	public List<Tree<ServiceCallRecord>> buildServiceCallTree(PartitionedMonitoringData<PCMContextRecord> records) {
		super.trackStart();

		log.info("Start building of service call trees.");
		List<Tree<ServiceCallRecord>> result = MonitoringDataUtil
				.buildServiceCallTree(records.getAllData().stream().filter(f -> f instanceof ServiceCallRecord)
						.map(ServiceCallRecord.class::cast).collect(Collectors.toList()));

		super.trackEnd();

		return result;
	}

}
