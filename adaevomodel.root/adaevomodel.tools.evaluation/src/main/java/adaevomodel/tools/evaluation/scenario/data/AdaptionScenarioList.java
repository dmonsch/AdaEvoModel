package adaevomodel.tools.evaluation.scenario.data;

import java.util.List;

import adaevomodel.base.shared.pcm.InMemoryPCM;
import lombok.Data;

@Data
public class AdaptionScenarioList {

	private List<AdaptionScenario> scenarios;
	private InMemoryPCM initialModel;

}
