package adaevomodel.runtime.pipeline.blackboard.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import adaevomodel.base.core.health.HealthStateManager;
import adaevomodel.base.core.health.HealthStateObservedComponent;
import adaevomodel.base.core.health.HealthStateProblem;
import adaevomodel.base.core.health.HealthStateProblemSeverity;
import adaevomodel.runtime.pipeline.blackboard.facade.IPipelineHealthQueryFacade;

@Component
public class PipelineHealthQueryFacadeImpl implements IPipelineHealthQueryFacade {

	@Autowired
	private HealthStateManager healthStateManager;

	@Override
	public long reportProblem(HealthStateProblemSeverity severity, String message) {
		return healthStateManager.addProblem(HealthStateProblem.builder().description(message).severity(severity)
				.source(HealthStateObservedComponent.PIPELINE).build());
	}

	@Override
	public void removeProblem(long id) {
		healthStateManager.removeProblem(id);
	}

}
