package adaevomodel.runtime.pipeline.blackboard.facade;

import adaevomodel.base.core.health.HealthStateProblemSeverity;

public interface IPipelineHealthQueryFacade {

	public long reportProblem(HealthStateProblemSeverity severity, String message);

	public void removeProblem(long id);

}
