package adaevomodel.tools.benchmarks.validation.results;

import java.util.List;

import com.google.common.collect.Lists;

import lombok.Data;

@Data
public class ValidationResultsContainer {

	private List<Object> results;

	public ValidationResultsContainer() {
		this.results = Lists.newArrayList();
	}

}
