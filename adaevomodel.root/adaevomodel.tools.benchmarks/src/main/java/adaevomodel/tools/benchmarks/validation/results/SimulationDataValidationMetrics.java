package adaevomodel.tools.benchmarks.validation.results;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.Data;

@Data
public class SimulationDataValidationMetrics {

	private Map<String, List<Double>> wassersteins;
	private Map<String, List<Double>> kstests;

	public SimulationDataValidationMetrics() {
		this.wassersteins = Maps.newHashMap();
		this.kstests = Maps.newHashMap();
	}

	public void calculateMedians() {
		generateMedian(wassersteins);
		generateMedian(kstests);
	}

	public void addWS(String service, double value) {
		addToMap(wassersteins, value, service);
	}

	public void addKS(String service, double value) {
		addToMap(kstests, value, service);
	}

	public void inherit(SimulationDataValidationMetrics other) {
		mergeMaps(wassersteins, other.wassersteins);
		mergeMaps(kstests, other.kstests);
	}

	private void generateMedian(Map<String, List<Double>> map) {
		Sets.newHashSet(map.keySet()).stream().forEach(k -> {
			map.put(k, Lists.newArrayList(getMedian(map.get(k))));
		});
	}

	private double getMedian(List<Double> values) {
		Collections.sort(values);

		if (values.size() % 2 == 1)
			return values.get((values.size() + 1) / 2 - 1);
		else {
			double lower = values.get(values.size() / 2 - 1);
			double upper = values.get(values.size() / 2);

			return (lower + upper) / 2.0;
		}
	}

	private void mergeMaps(Map<String, List<Double>> own, Map<String, List<Double>> other) {
		other.entrySet().forEach(es -> {
			if (own.containsKey(es.getKey())) {
				own.get(es.getKey()).addAll(es.getValue());
			} else {
				own.put(es.getKey(), es.getValue());
			}
		});
	}

	private void addToMap(Map<String, List<Double>> map, double value, String service) {
		if (map.containsKey(service)) {
			map.get(service).add(value);
		} else {
			map.put(service, Lists.newArrayList(value));
		}
	}

}
