package adaevomodel.runtime.pipeline.pcm.repository;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import adaevomodel.base.core.facade.IPCMQueryFacade;
import adaevomodel.base.core.facade.IRuntimeEnvironmentQueryFacade;
import adaevomodel.base.vsum.facade.ISpecificVsumFacade;
import adaevomodel.runtime.pipeline.pcm.repository.adjustment.IRepositoryDerivationAdjustment;
import adaevomodel.runtime.pipeline.pcm.repository.branch.impl.BranchEstimationImpl;
import adaevomodel.runtime.pipeline.pcm.repository.core.ResourceDemandEstimatorAlternative;
import adaevomodel.runtime.pipeline.pcm.repository.loop.impl.LoopEstimationImpl;
import adaevomodel.runtime.pipeline.pcm.repository.model.IResourceDemandEstimator;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import dmodel.designtime.monitoring.records.ServiceCallRecord;
import lombok.Setter;
import lombok.extern.java.Log;

/**
 * 
 * @author David Monschein
 *
 */
@Service
@Log
public class RepositoryDerivation {
	@Autowired
	@Setter
	private ISpecificVsumFacade mappingFacade;

	@Autowired
	@Setter
	private IRuntimeEnvironmentQueryFacade remQuery;

	@Autowired
	@Setter
	private IRepositoryDerivationAdjustment adjuster;

	private final LoopEstimationImpl loopEstimation;
	private final BranchEstimationImpl branchEstimation;

	public RepositoryDerivation() {
		this.loopEstimation = new LoopEstimationImpl();
		this.branchEstimation = new BranchEstimationImpl();
	}

	public RepositoryStoexChanges calibrateRepository(List<PCMContextRecord> data, IPCMQueryFacade pcm,
			ValidationData validation, Set<String> toPrepare) {
		try {
			Set<String> presentServices = data.stream().filter(f -> f instanceof ServiceCallRecord)
					.map(ServiceCallRecord.class::cast).map(s -> s.getServiceId()).collect(Collectors.toSet());

			RepositoryStoexChanges preChanges = adjuster.prepareAdjustments(pcm, validation, toPrepare,
					presentServices);

			MonitoringDataSet monitoringDataSet = new MonitoringDataSet(data, mappingFacade, remQuery,
					pcm.getAllocation(), pcm.getRepository());

			// TODO integrate loop and branch estimation

			IResourceDemandEstimator estimation = new ResourceDemandEstimatorAlternative(pcm);
			estimation.prepare(monitoringDataSet);
			RepositoryStoexChanges result = estimation.derive(adjuster.getAdjustments());
			result.inherit(preChanges);

			log.info("Finished calibration of internal actions.");
			log.info("Finished repository calibration.");

			return result;
		} catch (Exception e) {
			log.info("Calibration failed.");
			log.log(Level.INFO, "Calibrate Repository failed.", e);

			return null;
		}

	}

}
