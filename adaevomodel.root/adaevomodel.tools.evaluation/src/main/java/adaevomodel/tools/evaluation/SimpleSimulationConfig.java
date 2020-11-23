package adaevomodel.tools.evaluation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimpleSimulationConfig {

	private String url;
	private int port;

	private int measurements;
	private int simulationTime;

}
