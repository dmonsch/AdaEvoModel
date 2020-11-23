package adaevomodel.runtime.pipeline.pcm.resourceenv.finalize;

import adaevomodel.base.core.facade.IPCMQueryFacade;
import adaevomodel.base.core.facade.IRuntimeEnvironmentQueryFacade;
import adaevomodel.base.vsum.facade.ISpecificVsumFacade;
import adaevomodel.runtime.pipeline.pcm.resourceenv.data.EnvironmentData;

public interface IResourceEnvironmentDeduction {

	public void processEnvironmentData(IPCMQueryFacade pcm, IRuntimeEnvironmentQueryFacade rem,
			ISpecificVsumFacade mapping, EnvironmentData data);

}
