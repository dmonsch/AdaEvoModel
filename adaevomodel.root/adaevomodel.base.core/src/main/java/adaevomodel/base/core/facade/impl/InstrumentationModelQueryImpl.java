package adaevomodel.base.core.facade.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import adaevomodel.base.core.ISpecificModelProvider;
import adaevomodel.base.core.facade.IInstrumentationModelQueryFacade;
import dmodel.base.models.inmodel.InstrumentationMetamodel.InstrumentationModel;
import dmodel.base.models.inmodel.InstrumentationMetamodel.ServiceInstrumentationPoint;

/**
 * Implementation according to {@link IInstrumentationModelQueryFacade}. This
 * implementation uses no caches because it is not critical for the performance
 * of the pipeline.
 * 
 * @author David Monschein
 *
 */
@Component
public class InstrumentationModelQueryImpl implements IInstrumentationModelQueryFacade {
	/**
	 * Used to access the {@link InstrumentationModel}.
	 */
	@Autowired
	private ISpecificModelProvider modelProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<ServiceInstrumentationPoint> getFineGrainedInstrumentedServices() {
		return modelProvider.getInstrumentation().getPoints().stream().filter(instr -> {
			return instr.isActive() && instr.getActionInstrumentationPoints().stream().anyMatch(ac -> ac.isActive());
		}).collect(Collectors.toSet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getInstrumentedActionIds() {
		Set<String> ret = Sets.newHashSet();
		getFineGrainedInstrumentedServices().stream().forEach(service -> {
			ret.add(service.getService().getId());
			service.getActionInstrumentationPoints().stream().forEach(action -> {
				if (action.isActive()) {
					ret.add(action.getAction().getId());
				}
			});
		});
		modelProvider.getInstrumentation().getPoints().forEach(s -> ret.add(s.getService().getId()));

		return ret;
	}

}
