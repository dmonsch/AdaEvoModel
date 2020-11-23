package adaevomodel.runtime.pipeline.pcm.repository.estimation;

import adaevomodel.runtime.pipeline.pcm.repository.RepositoryStoexChanges;

public interface ITimelineAnalysis {

	public RepositoryStoexChanges analyze(IResourceDemandTimeline timeline);

}
