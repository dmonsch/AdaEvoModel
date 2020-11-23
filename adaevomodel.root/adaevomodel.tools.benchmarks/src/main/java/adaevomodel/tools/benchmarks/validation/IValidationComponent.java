package adaevomodel.tools.benchmarks.validation;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.tools.benchmarks.config.EValidationComponent;
import adaevomodel.tools.benchmarks.config.IBenchmarkConfiguration;
import dmodel.designtime.monitoring.records.PCMContextRecord;

public interface IValidationComponent {

	public boolean active(Set<EValidationComponent> set);

	public Object getValidationData(List<InMemoryPCM> pcms,
			List<Pair<List<PCMContextRecord>, List<PCMContextRecord>>> monitoring, IBenchmarkConfiguration config);

}
