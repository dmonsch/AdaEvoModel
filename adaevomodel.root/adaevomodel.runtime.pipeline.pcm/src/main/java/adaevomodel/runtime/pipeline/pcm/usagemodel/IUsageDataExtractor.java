package adaevomodel.runtime.pipeline.pcm.usagemodel;

import java.util.List;

import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import adaevomodel.base.core.facade.pcm.IRepositoryQueryFacade;
import adaevomodel.base.core.facade.pcm.ISystemQueryFacade;
import adaevomodel.base.shared.structure.Tree;
import dmodel.designtime.monitoring.records.ServiceCallRecord;

public interface IUsageDataExtractor {

	public List<UsageScenario> extract(List<Tree<ServiceCallRecord>> callSequences, IRepositoryQueryFacade repository,
			ISystemQueryFacade system);

}
