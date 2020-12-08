package adaevomodel.tools.benchmarks.bugfixing;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import adaevomodel.base.core.config.ConfigurationContainer;
import adaevomodel.base.core.config.ValidationFeedbackLoopConfiguration;
import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.base.shared.pcm.LocalFilesystemPCM;
import adaevomodel.base.shared.pcm.util.PCMUtils;
import adaevomodel.base.shared.structure.Tree;
import adaevomodel.bridge.monitoring.util.MonitoringDataUtil;
import adaevomodel.runtime.pipeline.pcm.usagemodel.tree.TreeBranchExtractor;
import adaevomodel.tools.benchmarks.adapter.SimplePcmQueryFacade;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import dmodel.designtime.monitoring.records.ServiceCallRecord;
import kieker.analysis.exception.AnalysisConfigurationException;

public class UsageModelFixing {

	public static void main(String[] args) throws IllegalStateException, AnalysisConfigurationException {
		PCMUtils.loadPCMModels();

		List<PCMContextRecord> records = MonitoringDataUtil.getMonitoringDataFromFiles(
				"/Users/david/Desktop/Dynamic Approach/Final Version/Implementation/git/adaevomodel.root/adaevomodel.tools.evaluation/kieker-20201130-114149-151601066906100-UTC--KIEKER");
		List<Tree<ServiceCallRecord>> scTrees = MonitoringDataUtil
				.buildServiceCallTree(records.stream().filter(f -> f instanceof ServiceCallRecord)
						.map(f -> (ServiceCallRecord) f).collect(Collectors.toList()));

		LocalFilesystemPCM pcm = new LocalFilesystemPCM();
		pcm.setAllocationModelFile(new File("test_models/allocation_1.allocation"));
		pcm.setRepositoryFile(new File("test_models/repository_1.repository"));
		pcm.setResourceEnvironmentFile(new File("test_models/resourceenv_1.resourceenvironment"));
		pcm.setSystemFile(new File("test_models/system_1.system"));
		pcm.setUsageModelFile(new File("test_models/usage_1.usagemodel"));

		InMemoryPCM mpcm = InMemoryPCM.createFromFilesystem(pcm);

		SimplePcmQueryFacade facade = new SimplePcmQueryFacade(mpcm);

		ConfigurationContainer container = new ConfigurationContainer();
		container.setVfl(new ValidationFeedbackLoopConfiguration());
		TreeBranchExtractor extractor = new TreeBranchExtractor(container);
		List<UsageScenario> scenarios = extractor.extract(scTrees, facade.getRepository(), facade.getSystem());

		System.out.println(scenarios.size());
		System.out.println("____________");
		for (UsageScenario scen : scenarios) {
			System.out.println(scen.getScenarioBehaviour_UsageScenario().getActions_ScenarioBehaviour().size());
		}

	}

}