package adaevomodel.app.rest.health.data;

import adaevomodel.base.core.health.HealthStateObservedComponent;
import adaevomodel.base.core.health.HealthStateProblemSeverity;
import lombok.Data;

@Data
public class JsonComponentHealthStateProblem {

	private HealthStateObservedComponent component;
	private HealthStateProblemSeverity severity;
	private String message;
	private long id;

}
