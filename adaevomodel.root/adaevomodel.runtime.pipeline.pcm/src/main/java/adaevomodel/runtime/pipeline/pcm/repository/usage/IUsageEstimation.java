package adaevomodel.runtime.pipeline.pcm.repository.usage;

import java.util.List;
import java.util.Map;

import adaevomodel.runtime.pipeline.pcm.repository.estimation.AbstractTimelineObject;

public interface IUsageEstimation {

	public Map<AbstractTimelineObject, Double> splitUpUsage(double usage, long startInterval, long stopInterval,
			List<AbstractTimelineObject> objects);

}
