package adaevomodel.runtime.pipeline.pcm.repository.adjustment.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.inference.TTest;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.pcm.headless.shared.data.results.MeasuringPointType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import adaevomodel.base.core.config.ConfigurationContainer;
import adaevomodel.base.core.facade.IPCMQueryFacade;
import adaevomodel.base.shared.pcm.distribution.DoubleDistribution;
import adaevomodel.runtime.pipeline.pcm.repository.RepositoryStoexChanges;
import adaevomodel.runtime.pipeline.pcm.repository.adjustment.IRepositoryDerivationAdjustment;
import adaevomodel.runtime.pipeline.validation.data.TimeValueDistribution;
import adaevomodel.runtime.pipeline.validation.data.ValidationData;
import adaevomodel.runtime.pipeline.validation.data.ValidationPoint;
import dmodel.base.models.callgraph.ServiceCallGraph.ServiceCallGraph;
import dmodel.base.models.callgraph.ServiceCallGraph.ServiceCallGraphEdge;
import lombok.Setter;
import lombok.extern.java.Log;

@Component
@Log
// TODO redo that
public class TreeScalingRepositoryDerivationAdjuster implements IRepositoryDerivationAdjustment {
	private Map<String, Double> currentValidationAdjustment = Maps.newHashMap();
	private Map<String, Double> currentValidationAdjustmentGradient = Maps.newHashMap();

	// constant determination intel
	private Map<String, Double> monitoringAverageBefore = Maps.newHashMap();
	private Map<String, Integer> constantDeterminationSizeBefore = Maps.newHashMap();

	@Autowired
	@Setter
	private ConfigurationContainer configuration;

	public RepositoryStoexChanges getConstants(IPCMQueryFacade pcm, ValidationData validation,
			Set<String> fineGrainedInstrumentedServices, Set<String> presentServices) {
		if (!configuration.getVfl().getScaling().isConstantDetermination()) {
			return new RepositoryStoexChanges();
		}

		constantDeterminationSizeBefore.clear();

		Set<String> serviceIdsToAdjust = getServicesToAdjust(pcm, fineGrainedInstrumentedServices, presentServices);

		return this.constantDetermination(validation, serviceIdsToAdjust, pcm);
	}

	@Override
	public RepositoryStoexChanges prepareAdjustments(IPCMQueryFacade pcm, ValidationData validation,
			Set<String> fineGrainedInstrumentedServices, Set<String> presentServices) {
		if (!configuration.getVfl().getScaling().isScalingEnabled()) {
			return new RepositoryStoexChanges();
		}

		Set<String> serviceIdsToAdjust = getServicesToAdjust(pcm, fineGrainedInstrumentedServices, presentServices);

		// Test without
		// this.analyzeServiceChanges(validation, presentServices);

		// RepositoryStoexChanges resultingChanges =
		// this.constantDetermination(validation, serviceIdsToAdjust, pcm);
		this.prepareAdjustment(validation, serviceIdsToAdjust);

		return new RepositoryStoexChanges();
	}

	@Override
	public Map<String, Double> getAdjustments() {
		return currentValidationAdjustment;
	}

	private void analyzeServiceChanges(ValidationData validation, Set<String> presentServices) {
		if (validation == null || validation.isEmpty()) {
			return;
		}

		validation.getValidationPoints().forEach(point -> {
			MeasuringPointType type = point.getMeasuringPoint().getType();
			if (type == MeasuringPointType.ASSEMBLY_OPERATION || type == MeasuringPointType.ENTRY_LEVEL_CALL) {
				if (presentServices.contains(point.getServiceId())) {
					if (point.getMonitoringDistribution().getYValues().size() > 0) {
						double medianMonitoring = new Median().evaluate(
								point.getMonitoringDistribution().getYValues().stream().mapToDouble(d -> d).toArray());

						if (monitoringAverageBefore.containsKey(point.getServiceId())) {
							double valueBefore = monitoringAverageBefore.get(point.getServiceId());
							double increase = 1d - (valueBefore / medianMonitoring);

							if (currentValidationAdjustment.containsKey(point.getServiceId())) {
								currentValidationAdjustment.put(point.getServiceId(),
										currentValidationAdjustment.get(point.getServiceId()) - increase);
							}
						}
						monitoringAverageBefore.put(point.getServiceId(), medianMonitoring);
					}
				}
			}
		});
	}

	private Set<String> getServicesToAdjust(IPCMQueryFacade pcm, Set<String> fineGrainedInstrumentedServices,
			Set<String> presentServices) {
		if (configuration.getVfl().getScaling().isTreeScaling()) {
			// 1. search for services to adjust
			ServiceCallGraph scg = pcm.getRepository().getServiceCallGraph();
			List<ResourceDemandingSEFF> servicesToAdjust = searchNodesToAdjust(scg, fineGrainedInstrumentedServices,
					presentServices);

			// 2. adjust the services
			return servicesToAdjust.stream().map(s -> s.getId()).collect(Collectors.toSet());
		} else {
			return new HashSet<>(presentServices);
		}
	}

	private List<ResourceDemandingSEFF> searchNodesToAdjust(ServiceCallGraph scg, Set<String> fineGrained,
			Set<String> present) {
		return scg.getNodes().stream().filter(n -> {
			List<ServiceCallGraphEdge> outgoingEdges = scg.getOutgoingEdges().get(n);
			return (outgoingEdges == null || outgoingEdges.size() == 0 || outgoingEdges.stream().allMatch(nn -> {
				return !fineGrained.contains(nn.getTo().getSeff().getId());
			})) && fineGrained.contains(n.getSeff().getId()) && present.contains(n.getSeff().getId());
		}).map(n -> n.getSeff()).collect(Collectors.toList());
	}

	private RepositoryStoexChanges constantDetermination(ValidationData validation, Set<String> servicesToAdjust,
			IPCMQueryFacade pcm) {
		RepositoryStoexChanges result = new RepositoryStoexChanges();
		if (validation == null || validation.isEmpty()) {
			return result;
		}

		validation.getValidationPoints().forEach(point -> {
			MeasuringPointType type = point.getMeasuringPoint().getType();
			if (type == MeasuringPointType.ASSEMBLY_OPERATION || type == MeasuringPointType.ENTRY_LEVEL_CALL) {
				if (servicesToAdjust.contains(point.getServiceId())) {
					log.info("Trying to adjust " + point.getServiceId());
					complexConstantDetermination(point, pcm, result);
				}
			}
		});

		return result;
	}

	private void easyConstantDetermination(ValidationPoint point, IPCMQueryFacade pcm, RepositoryStoexChanges result) {
		double[] monitoringDistribution = point.getMonitoringDistribution().yAxis();
		double[] analysisDistribution = point.getAnalysisDistribution().yAxis();

		if (constantDeterminationSizeBefore.containsKey(point.getServiceId())) {
			if (constantDeterminationSizeBefore.get(point.getServiceId()) > monitoringDistribution.length
					+ analysisDistribution.length) {
				return;
			}
		}

		if (monitoringDistribution.length >= 1 && analysisDistribution.length >= 1) {
			double minMonitoring = Arrays.stream(monitoringDistribution).min().orElse(0d);
			double minAnalysis = Arrays.stream(analysisDistribution).min().orElse(0d);

			this.determineConstant(point.getServiceId(), new double[] { minMonitoring }, new double[] { minAnalysis },
					pcm, result);
		}

		constantDeterminationSizeBefore.put(point.getServiceId(),
				monitoringDistribution.length + analysisDistribution.length);
	}

	private void complexConstantDetermination(ValidationPoint point, IPCMQueryFacade pcm,
			RepositoryStoexChanges result) {
		double[] monitoringDistribution = point.getMonitoringDistribution().yAxis();
		double[] analysisDistribution = point.getAnalysisDistribution().yAxis();

		double[] copyMonitoring = new double[monitoringDistribution.length];
		double[] copyAnalysis = new double[analysisDistribution.length];

		System.arraycopy(monitoringDistribution, 0, copyMonitoring, 0, monitoringDistribution.length);
		System.arraycopy(analysisDistribution, 0, copyAnalysis, 0, analysisDistribution.length);
		Arrays.sort(copyMonitoring);
		Arrays.sort(copyAnalysis);

		if (copyMonitoring.length > 0 && copyAnalysis.length > 0) {
			int nMonitoring = (int) Math.round(((double) copyMonitoring.length)
					* configuration.getVfl().getScaling().getConstantDeterminationProportion());
			int nAnalysis = (int) Math.round(((double) copyAnalysis.length)
					* configuration.getVfl().getScaling().getConstantDeterminationProportion());

			if (nMonitoring >= 2 && nAnalysis >= 2) {
				double[] lowestMonitoring = new double[nMonitoring];
				double[] lowestAnalysis = new double[nAnalysis];

				System.arraycopy(copyMonitoring, 0, lowestMonitoring, 0, nMonitoring);
				System.arraycopy(copyAnalysis, 0, lowestAnalysis, 0, nAnalysis);

				// do the investigation
				double avgAnalysis = StatUtils.mean(copyAnalysis);
				double avgMonitoring = StatUtils.mean(copyMonitoring);

				// significance test
				// TODO also adjust when lower?
				if (avgMonitoring != avgAnalysis && new TTest().tTest(lowestMonitoring, lowestAnalysis, 0.05)) {
					log.info("Constant determination for service '" + point.getServiceId() + "'.");
					determineConstant(point.getServiceId(), lowestMonitoring, lowestAnalysis, pcm, result);
				}
			} else {
				log.warning("Too less monitoring and analysis points inner.");
			}
		} else {
			log.warning("Too less monitoring and analysis points.");
		}
	}

	private void prepareAdjustment(ValidationData validation, Set<String> servicesToAdjust) {
		if (validation == null || validation.isEmpty()) {
			return;
		}

		validation.getValidationPoints().forEach(point -> {
			MeasuringPointType type = point.getMeasuringPoint().getType();
			if (type == MeasuringPointType.ASSEMBLY_OPERATION || type == MeasuringPointType.ENTRY_LEVEL_CALL) {
				if (point.getServiceId() != null && point.getMonitoringDistribution() != null
						&& servicesToAdjust.contains(point.getServiceId())) {
					// absolute dist
					double monitoringLowest = Arrays.stream(point.getMonitoringDistribution().yAxis()).min().orElse(0d);
					double analysisLowest = Arrays.stream(point.getAnalysisDistribution().yAxis()).min().orElse(0d);

					double[] monitoringNormed = Arrays.stream(point.getMonitoringDistribution().yAxis())
							.map(d -> d - monitoringLowest).toArray();
					double[] analysisNormed = Arrays.stream(point.getAnalysisDistribution().yAxis())
							.map(d -> d - analysisLowest).toArray();

					double valueAbsDist = StatUtils.mean(monitoringNormed) - StatUtils.mean(analysisNormed);

					if (valueAbsDist > 0) {
						adjustService(point.getServiceId(), point.getAnalysisDistribution(),
								point.getMonitoringDistribution(), true);
					} else if (valueAbsDist < 0) {
						adjustService(point.getServiceId(), point.getAnalysisDistribution(),
								point.getMonitoringDistribution(), false);
					}
				}
			}
		});
	}

	private void adjustService(String service, TimeValueDistribution analysis, TimeValueDistribution monitoring,
			boolean scaleUp) {
		if (configuration.getVfl().getScaling().isFeedbackBasedScaling()) {
			adjustDynamic(service, analysis, monitoring, scaleUp);
		} else {
			adjustStatic(service, scaleUp);
		}
	}

	private void adjustDynamic(String service, TimeValueDistribution analysis, TimeValueDistribution monitoring,
			boolean scaleUp) {
		double lowestAnalysis = analysis.getYValues().stream().min(Double::compare).orElse(0d);
		double lowestMonitoring = monitoring.getYValues().stream().min(Double::compare).orElse(0d);

		double[] listAnalysis = analysis.getYValues().stream().mapToDouble(d -> d - lowestAnalysis).toArray();
		double[] listMonitoring = monitoring.getYValues().stream().mapToDouble(d -> d - lowestMonitoring).toArray();
		if (listAnalysis.length > 0 && listMonitoring.length > 0) {
			double medianAnalysis = StatUtils.mean(listAnalysis);
			double medianMonitoring = StatUtils.mean(listMonitoring);

			if (medianAnalysis == 0.0d || medianMonitoring == 0.0d) {
				return;
			}

			log.info("Range for " + service + ": [" + medianMonitoring + " - " + medianAnalysis + "]");

			double dist = medianMonitoring / medianAnalysis;
			log.info("Adjustment factor (" + service + "): " + dist);

			double current = currentValidationAdjustment.containsKey(service)
					? currentValidationAdjustment.get(service) + 1
					: 1d;
			log.info("Resulting factor (" + service + "): " + current);
			currentValidationAdjustment.put(service, current * dist - 1d);
		} else {
			log.warning("Distribution not present.");
		}
	}

	private void adjustStatic(String service, boolean scaleUp) {
		if (!currentValidationAdjustment.containsKey(service)) {
			currentValidationAdjustment.put(service,
					configuration.getVfl().getScaling().getAdjustmentFactor() * (scaleUp ? 1 : -1));
			currentValidationAdjustmentGradient.put(service,
					configuration.getVfl().getScaling().getAdjustmentFactor() * (scaleUp ? 1 : -1));
		} else {
			double adjustmentBefore = currentValidationAdjustment.get(service);
			double currentGradient = currentValidationAdjustmentGradient.get(service);

			double adjustmentNow;
			if (scaleUp && adjustmentBefore < 0 || !scaleUp && adjustmentBefore > 0) {
				adjustmentNow = 0;
				currentValidationAdjustmentGradient.put(service,
						configuration.getVfl().getScaling().getAdjustmentFactor() * (scaleUp ? 1d : -1d));
			} else {
				adjustmentNow = currentGradient
						+ configuration.getVfl().getScaling().getBaseGradient() * Math.signum(currentGradient);
				currentValidationAdjustmentGradient.put(service, adjustmentNow);
			}

			currentValidationAdjustment.put(service, adjustmentNow + adjustmentBefore);
		}
	}

	private void determineConstant(String serviceId, double[] lowestMonitoring, double[] lowestAnalysis,
			IPCMQueryFacade pcm, RepositoryStoexChanges result) {
		// TODO calculation buggy?
		double avgAnalysis = StatUtils.mean(lowestAnalysis);

		DoubleDistribution adjustmentDistr = new DoubleDistribution(5);

		for (double monitoringValue : lowestMonitoring) {
			adjustmentDistr.put(monitoringValue - avgAnalysis);
		}

		// finally put it to the changes
		result.putConstant(serviceId, adjustmentDistr.toStoex());
	}

}
