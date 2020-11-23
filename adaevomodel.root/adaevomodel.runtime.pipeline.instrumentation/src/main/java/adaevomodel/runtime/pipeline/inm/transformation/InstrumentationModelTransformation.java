package adaevomodel.runtime.pipeline.inm.transformation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.pcm.headless.shared.data.results.MeasuringPointType;
import org.pcm.headless.shared.data.results.PlainMeasuringPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import adaevomodel.base.core.ISpecificModelProvider;
import adaevomodel.base.core.config.ConfigurationContainer;
import adaevomodel.base.core.config.PredicateRuleConfiguration;
import adaevomodel.base.core.state.ExecutionMeasuringPoint;
import adaevomodel.base.core.state.ValidationSchedulePoint;
import adaevomodel.base.shared.pipeline.PortIDs;
import adaevomodel.runtime.pipeline.AbstractIterativePipelinePart;
import adaevomodel.runtime.pipeline.annotation.InputPort;
import adaevomodel.runtime.pipeline.annotation.InputPorts;
import adaevomodel.runtime.pipeline.annotation.OutputPort;
import adaevomodel.runtime.pipeline.annotation.OutputPorts;
import adaevomodel.runtime.pipeline.blackboard.RuntimePipelineBlackboard;
import adaevomodel.runtime.pipeline.data.PartitionedMonitoringData;
import adaevomodel.runtime.pipeline.inm.transformation.predicate.ValidationPredicate;
import adaevomodel.runtime.pipeline.inm.transformation.predicate.config.ELogicalOperator;
import adaevomodel.runtime.pipeline.inm.transformation.predicate.config.ENumericalComparator;
import adaevomodel.runtime.pipeline.inm.transformation.predicate.impl.DoubleMetricValidationPredicate;
import adaevomodel.runtime.pipeline.inm.transformation.predicate.impl.StackedValidationPredicate;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import adaevomodel.runtime.pipeline.validation.data.metric.ValidationMetricType;
import dmodel.base.models.inmodel.InstrumentationMetamodel.ServiceInstrumentationPoint;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import lombok.extern.java.Log;

@Log
@Service
public class InstrumentationModelTransformation extends AbstractIterativePipelinePart<RuntimePipelineBlackboard> {
	private static final Map<String, ValidationMetricType> predicateMetricMapping = new HashMap<String, ValidationMetricType>() {
		/**
		 * Generated Serial UID.
		 */
		private static final long serialVersionUID = 1634402137017012232L;
		{
			put("kstest", ValidationMetricType.KS_TEST);
			put("avg_rel", ValidationMetricType.AVG_DISTANCE_REL);
			put("avg_absolute", ValidationMetricType.AVG_DISTANCE_ABS);
			put("wasserstein", ValidationMetricType.WASSERSTEIN);
			put("median_absolute", ValidationMetricType.MEDIAN_DISTANCE);
		}
	};

	public InstrumentationModelTransformation() {
		super(ExecutionMeasuringPoint.T_INSTRUMENTATION_MODEL, null);
	}

	@Autowired
	private ConfigurationContainer configuration;

	@Autowired
	private ISpecificModelProvider specificModels;

	@InputPorts({ @InputPort(PortIDs.T_PRE_VAL_IMM) })
	@OutputPorts(@OutputPort(to = ServiceCallTreeBuilder.class, id = PortIDs.T_BUILD_SERVICECALL_TREE, async = false))
	public PartitionedMonitoringData<PCMContextRecord> adjustInstrumentationModel(
			PartitionedMonitoringData<PCMContextRecord> records) {
		ValidationData validation = getBlackboard().getValidationResultsQuery()
				.get(ValidationSchedulePoint.PRE_PIPELINE);
		if (validation.isEmpty()) {
			super.trackStart();
			super.trackEnd();
			return records;
		}

		super.trackStart();

		Set<String> deInstrumentServices = Sets.newHashSet();
		Set<String> instrumentServices = Sets.newHashSet();

		ValidationPredicate validationPredicate = toJavaPredicate(configuration.getValidationPredicates());
		validation.getValidationPoints().forEach(validationPoint -> {
			if (isServiceMeasuringPoint(validationPoint.getMeasuringPoint())) {
				String serviceId = validationPoint.getServiceId();
				if (serviceId != null && validationPoint.getMetricValues().size() > 0) {
					boolean fulfilled = validationPredicate == null || validationPredicate.satisfied(validationPoint);

					if (fulfilled) {
						deInstrumentServices.add(serviceId);
					} else {
						instrumentServices.add(serviceId);
					}
				}
			}
		});

		// if we have a component on multiple hosts
		// and it is not accurate on a single one
		deInstrumentServices.removeAll(instrumentServices);

		// check whether target service is accurate
		if (configuration.getVfl().getTargetServiceId() != null
				&& !configuration.getVfl().getTargetServiceId().isEmpty()) {
			if (deInstrumentServices.contains(configuration.getVfl().getTargetServiceId())) {
				deInstrumentServices.addAll(instrumentServices);
				instrumentServices.clear();
			}
		}

		// perform changes
		deInstrumentServices.forEach(deInstr -> {
			log.info("Enable coarse grained monitoring for service '" + deInstr + "'.");
			changeInstrumentationModel(deInstr, false);
		});

		instrumentServices.forEach(instr -> {
			log.info("Enable fine grained monitoring for service '" + instr + "'.");
			changeInstrumentationModel(instr, true);
		});

		super.trackEnd();

		return records;
	}

	private void changeInstrumentationModel(String deInstr, boolean b) {
		ServiceEffectSpecification seff = getBlackboard().getPcmQuery().getRepository().getServiceById(deInstr);
		Optional<ServiceInstrumentationPoint> instrPoint = getBlackboard().getVsumQuery()
				.getCorrespondingInstrumentationPoint(seff);
		if (instrPoint.isPresent()) {
			instrPoint.get().getActionInstrumentationPoints().forEach(actionPoint -> actionPoint.setActive(b));
		} else {
			instrPoint = specificModels.getInstrumentation().getPoints().stream()
					.filter(p -> p instanceof ServiceInstrumentationPoint).map(ServiceInstrumentationPoint.class::cast)
					.filter(a -> a.getService().getId().equals(deInstr)).findFirst();
			if (instrPoint.isPresent()) {
				instrPoint.get().getActionInstrumentationPoints().forEach(actionPoint -> actionPoint.setActive(b));
			}
		}
	}

	private boolean isServiceMeasuringPoint(PlainMeasuringPoint measuringPoint) {
		if (measuringPoint.getType() == MeasuringPointType.ASSEMBLY_OPERATION
				|| measuringPoint.getType() == MeasuringPointType.ENTRY_LEVEL_CALL) {
			return true;
		}
		return false;
	}

	private ValidationPredicate toJavaPredicate(PredicateRuleConfiguration config) {
		if (config.getCondition() != null) {
			// it is stacked
			ValidationPredicate[] predicates = new ValidationPredicate[config.getRules().size()];

			for (int i = 0; i < config.getRules().size(); i++) {
				predicates[i] = toJavaPredicate(config.getRules().get(i));
			}

			return new StackedValidationPredicate(ELogicalOperator.fromString(config.getCondition()), predicates);
		} else {
			return buildSimpleJavaPredicate(config);
		}
	}

	private ValidationPredicate buildSimpleJavaPredicate(PredicateRuleConfiguration config) {
		ENumericalComparator comparator = ENumericalComparator.fromString(config.getOperator());
		if (comparator != null && predicateMetricMapping.containsKey(config.getId())) {
			return new DoubleMetricValidationPredicate(predicateMetricMapping.get(config.getId()), comparator,
					config.getValue());
		} else {
			log.warning("Metric of type '" + config.getId() + "' or comparator '" + comparator.getName()
					+ "' is not supported yet for the predicate generation.");
		}

		return null;
	}

}
