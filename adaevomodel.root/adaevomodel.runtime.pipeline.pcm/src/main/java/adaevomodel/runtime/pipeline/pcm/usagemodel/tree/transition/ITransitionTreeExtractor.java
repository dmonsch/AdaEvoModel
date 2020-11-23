package adaevomodel.runtime.pipeline.pcm.usagemodel.tree.transition;

import java.util.List;

import adaevomodel.base.core.facade.pcm.IRepositoryQueryFacade;
import adaevomodel.base.core.facade.pcm.ISystemQueryFacade;
import adaevomodel.base.shared.structure.Tree;
import adaevomodel.runtime.pipeline.pcm.usagemodel.ServiceCallSession;
import adaevomodel.runtime.pipeline.pcm.usagemodel.data.UsageServiceCallDescriptor;
import adaevomodel.runtime.pipeline.pcm.usagemodel.tree.DescriptorTransition;

public interface ITransitionTreeExtractor {

	public Tree<DescriptorTransition<UsageServiceCallDescriptor>> extractProbabilityCallTree(
			List<ServiceCallSession> sessions, IRepositoryQueryFacade repository, ISystemQueryFacade system);

}
