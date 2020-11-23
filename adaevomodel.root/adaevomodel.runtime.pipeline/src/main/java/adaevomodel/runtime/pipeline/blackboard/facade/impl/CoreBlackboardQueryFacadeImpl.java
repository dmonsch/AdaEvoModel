package adaevomodel.runtime.pipeline.blackboard.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adaevomodel.base.core.facade.ICoreBlackboardQueryFacade;
import adaevomodel.base.core.health.HealthState;
import adaevomodel.base.core.state.EPipelineTransformation;
import adaevomodel.base.core.state.ETransformationState;
import adaevomodel.base.core.state.ExecutionMeasuringPoint;
import adaevomodel.base.evaluation.PerformanceEvaluation;
import adaevomodel.runtime.pipeline.blackboard.state.PipelineUIState;

@Component
public class CoreBlackboardQueryFacadeImpl implements ICoreBlackboardQueryFacade {
	@Autowired
	private PipelineUIState uiState;

	@Autowired
	private PerformanceEvaluation performanceEvaluation;

	@Override
	public void reset(boolean hard) {
		if (hard) {
			uiState.reset();
		}
	}

	@Override
	public void updateState(EPipelineTransformation transformation, ETransformationState state) {
		uiState.updateState(transformation, state);
	}

	@Override
	public void trackStartPipelineExecution() {
		performanceEvaluation.enterPipelineExecution();
	}

	@Override
	public void trackEndPipelineExecution(HealthState success) {
		performanceEvaluation.exitPipelineExecution(success);
	}

	@Override
	public void track(ExecutionMeasuringPoint point) {
		performanceEvaluation.trackMeasuringPoint(point);
	}

	@Override
	public void trackRecordCount(int count) {
		performanceEvaluation.trackRecordCount(count);
	}

	@Override
	public void trackPath(boolean b) {
		performanceEvaluation.trackPath(b);
	}

	@Override
	public void trackUsageScenarios(int scenarioCount) {
		performanceEvaluation.trackUsageScenarios(scenarioCount);
	}

}
