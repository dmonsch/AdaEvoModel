package adaevomodel.tools.benchmarks.adapter;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import adaevomodel.base.core.IPcmModelProvider;
import adaevomodel.base.core.facade.IPCMQueryFacade;
import adaevomodel.base.core.facade.pcm.IAllocationQueryFacade;
import adaevomodel.base.core.facade.pcm.IRepositoryQueryFacade;
import adaevomodel.base.core.facade.pcm.IResourceEnvironmentQueryFacade;
import adaevomodel.base.core.facade.pcm.ISystemQueryFacade;
import adaevomodel.base.core.facade.pcm.IUsageQueryFacade;
import adaevomodel.base.core.facade.pcm.impl.AllocationQueryFacadeImpl;
import adaevomodel.base.core.facade.pcm.impl.RepositoryQueryFacadeImpl;
import adaevomodel.base.core.facade.pcm.impl.SystemQueryFacadeImpl;
import adaevomodel.base.core.facade.pcm.impl.UsageQueryFacadeImpl;
import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.runtime.pipeline.blackboard.facade.impl.ResourceEnvironmentQueryFacadeImpl;

public class SimplePcmQueryFacade implements IPCMQueryFacade {
	private InMemoryPCM pcm;

	private IPcmModelProvider modelProvider;
	private RepositoryQueryFacadeImpl repositoryQuery;
	private SystemQueryFacadeImpl systemQuery;
	private AllocationQueryFacadeImpl allocationQuery;
	private UsageQueryFacadeImpl usageQuery;
	private ResourceEnvironmentQueryFacadeImpl resEnvQuery;

	public SimplePcmQueryFacade(InMemoryPCM pcm) {
		this.pcm = pcm;
		this.modelProvider = buildModelProvider(pcm);

		// build facades
		this.repositoryQuery = new RepositoryQueryFacadeImpl();
		this.systemQuery = new SystemQueryFacadeImpl();
		this.allocationQuery = new AllocationQueryFacadeImpl();
		this.usageQuery = new UsageQueryFacadeImpl();
		this.resEnvQuery = new ResourceEnvironmentQueryFacadeImpl();

		repositoryQuery.setPcmModelProvider(this.modelProvider);
		systemQuery.setPcmModelProvider(this.modelProvider);
		systemQuery.setAllocationQuery(this.allocationQuery);
		allocationQuery.setPcmModelProvider(this.modelProvider);
		usageQuery.setPcmModelProvider(this.modelProvider);
		resEnvQuery.setPcmModelProvider(this.modelProvider);

		repositoryQuery.reset(true);
		systemQuery.reset(true);
		allocationQuery.reset(true);
		usageQuery.reset(true);
		resEnvQuery.reset(true);
	}

	@Override
	public IRepositoryQueryFacade getRepository() {
		return this.repositoryQuery;
	}

	@Override
	public ISystemQueryFacade getSystem() {
		return this.systemQuery;
	}

	@Override
	public IAllocationQueryFacade getAllocation() {
		return this.allocationQuery;
	}

	@Override
	public IUsageQueryFacade getUsage() {
		return this.usageQuery;
	}

	@Override
	public IResourceEnvironmentQueryFacade getResourceEnvironment() {
		return this.resEnvQuery;
	}

	@Override
	public InMemoryPCM getRaw() {
		return this.pcm;
	}

	@Override
	public InMemoryPCM getDeepCopy() {
		return this.pcm.copyDeep();
	}

	private IPcmModelProvider buildModelProvider(InMemoryPCM pcm) {
		return new IPcmModelProvider() {

			@Override
			public UsageModel getUsage() {
				return pcm.getUsageModel();
			}

			@Override
			public System getSystem() {
				return pcm.getSystem();
			}

			@Override
			public ResourceEnvironment getResourceEnvironment() {
				return pcm.getResourceEnvironmentModel();
			}

			@Override
			public Repository getRepository() {
				return pcm.getRepository();
			}

			@Override
			public InMemoryPCM getRaw() {
				return pcm;
			}

			@Override
			public Allocation getAllocation() {
				return pcm.getAllocationModel();
			}
		};
	}

}
