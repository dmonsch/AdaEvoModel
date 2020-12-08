package adaevomodel.runtime.pipeline.pcm.repository;

import java.util.List;
import java.util.Set;

import adaevomodel.base.core.facade.IPCMQueryFacade;
import adaevomodel.runtime.pipeline.pcm.repository.calibration.RepositoryStoexChanges;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import dmodel.designtime.monitoring.records.PCMContextRecord;

public interface IRepositoryCalibration {

	public RepositoryStoexChanges calibrateRepository(List<PCMContextRecord> data, IPCMQueryFacade pcm,
			ValidationData validation, Set<String> fineGraindInstrumentedServiceIds);

}
