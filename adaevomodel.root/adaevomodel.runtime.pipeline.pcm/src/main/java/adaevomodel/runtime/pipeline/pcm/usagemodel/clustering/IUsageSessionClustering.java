package adaevomodel.runtime.pipeline.pcm.usagemodel.clustering;

import java.util.List;

import adaevomodel.runtime.pipeline.pcm.usagemodel.ServiceCallSession;

public interface IUsageSessionClustering {

	public List<List<ServiceCallSession>> clusterSessions(List<ServiceCallSession> initial);

}
