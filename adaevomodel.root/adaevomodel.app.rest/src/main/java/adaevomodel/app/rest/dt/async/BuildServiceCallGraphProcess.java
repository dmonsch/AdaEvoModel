package adaevomodel.app.rest.dt.async;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import adaevomodel.base.shared.util.AbstractObservable;
import adaevomodel.designtime.systemextraction.scg.ServiceCallGraphBuilder;
import dmodel.base.models.callgraph.ServiceCallGraph.ServiceCallGraph;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BuildServiceCallGraphProcess extends AbstractObservable<ServiceCallGraph> implements Runnable {
	private ServiceCallGraphBuilder builder;
	private List<String> jarFiles;
	private String basePath;

	@Override
	public void run() {
		File basePathFile = new File(basePath);
		// extract
		ServiceCallGraph result = builder.buildServiceCallGraph(
				jarFiles.stream().map(jf -> new File(basePathFile, jf)).collect(Collectors.toList()));

		super.flood(result);
	}

}
