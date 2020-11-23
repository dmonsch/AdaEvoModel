package adaevomodel.runtime.pipeline;

import adaevomodel.base.core.state.EPipelineTransformation;
import adaevomodel.base.core.state.ETransformationState;
import adaevomodel.base.core.state.ExecutionMeasuringPoint;
import adaevomodel.runtime.pipeline.blackboard.RuntimePipelineBlackboard;

public abstract class AbstractIterativePipelinePart<B extends RuntimePipelineBlackboard> {
	private B blackboard;

	private ExecutionMeasuringPoint measuringPoint;
	private EPipelineTransformation transformation;

	public AbstractIterativePipelinePart(ExecutionMeasuringPoint measuringPoint,
			EPipelineTransformation transformation) {
		this.measuringPoint = measuringPoint;
		this.transformation = transformation;
	}

	protected void trackStart() {
		if (transformation != null) {
			blackboard.getQuery().updateState(transformation, ETransformationState.RUNNING);
		}

		if (measuringPoint != null) {
			blackboard.getQuery().track(measuringPoint);
		}
	}

	protected void trackEnd() {
		if (transformation != null) {
			blackboard.getQuery().updateState(transformation, ETransformationState.FINISHED);
		}

		if (measuringPoint != null) {
			blackboard.getQuery().track(measuringPoint);
		}
	}

	public B getBlackboard() {
		return blackboard;
	}

	public void setBlackboard(B blackboard) {
		this.blackboard = blackboard;
	}
}
