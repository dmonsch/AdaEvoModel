package adaevomodel.runtime.pipeline.pcm.repository.usage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adaevomodel.runtime.pipeline.pcm.repository.estimation.AbstractTimelineObject;

public class PlainUsageEstimation implements IUsageEstimation {

	@Override
	public Map<AbstractTimelineObject, Double> splitUpUsage(double usage, long startInterval, long stopInterval,
			List<AbstractTimelineObject> objects) {
		Map<AbstractTimelineObject, Double> output = new HashMap<>();

		for (AbstractTimelineObject obj : objects) {
			output.put(obj, 1.0d);
		}

		return output;
	}

}
