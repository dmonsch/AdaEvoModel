package adaevomodel.tools.benchmarks.bugfixing;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import adaevomodel.base.shared.pcm.InMemoryPCM;
import adaevomodel.base.shared.pcm.LocalFilesystemPCM;
import adaevomodel.base.shared.pcm.util.PCMUtils;
import adaevomodel.base.shared.structure.Tree;
import adaevomodel.base.shared.structure.Tree.TreeNode;
import adaevomodel.bridge.monitoring.util.MonitoringDataUtil;
import adaevomodel.runtime.pipeline.pcm.usagemodel.ServiceCallSession;
import adaevomodel.runtime.pipeline.pcm.usagemodel.util.UsageServiceUtil;
import adaevomodel.tools.benchmarks.adapter.SimplePcmQueryFacade;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import dmodel.designtime.monitoring.records.ServiceCallRecord;
import kieker.analysis.exception.AnalysisConfigurationException;

public class SystemModelFixing {

	public static void main(String[] args) throws IllegalStateException, AnalysisConfigurationException {
		String monitoringPath = "/Users/david/Desktop/Dynamic Approach/Final Version/Implementation/git/adaevomodel.root/adaevomodel.tools.benchmarks/kieker-20201204-002206-7789861242900-UTC--KIEKER";
		// monitoringPath = "/Users/david/Desktop/Dynamic Approach/Final
		// Version/Implementation/git/adaevomodel.root/adaevomodel.tools.evaluation/kieker-20201130-114149-151601066906100-UTC--KIEKER";

		PCMUtils.loadPCMModels();

		List<PCMContextRecord> records = MonitoringDataUtil.getMonitoringDataFromFiles(monitoringPath);
		List<ServiceCallRecord> serviceCalls = records.stream().filter(f -> f instanceof ServiceCallRecord)
				.map(f -> (ServiceCallRecord) f).collect(Collectors.toList());
		List<Tree<ServiceCallRecord>> scTrees = MonitoringDataUtil.buildServiceCallTree(serviceCalls);

		LocalFilesystemPCM pcm = new LocalFilesystemPCM();
		pcm.setAllocationModelFile(new File("test_models/allocation_1.allocation"));
		pcm.setRepositoryFile(new File("test_models/repository_1.repository"));
		pcm.setResourceEnvironmentFile(new File("test_models/resourceenv_1.resourceenvironment"));
		pcm.setSystemFile(new File("test_models/system_1.system"));
		pcm.setUsageModelFile(new File("test_models/usage_1.usagemodel"));

		InMemoryPCM mpcm = InMemoryPCM.createFromFilesystem(pcm);

		SimplePcmQueryFacade facade = new SimplePcmQueryFacade(mpcm);

		List<ServiceCallRecord> entryCalls = serviceCalls.stream()
				.filter(e -> UsageServiceUtil.isEntryCall(facade.getRepository(), facade.getSystem(), e))
				.collect(Collectors.toList());
		List<ServiceCallSession> sessions = extractSessions(entryCalls);

		System.out.println(entryCalls.size());

		Set<String> cons_sessions = new HashSet<>();
		for (ServiceCallSession sess : sessions) {
			if (sess.getEntryCalls().size() >= 2) {
				cons_sessions.add(sess.getSessionId());
				System.out.println("------------------------------");
				sess.getEntryCalls().forEach(e -> {
					System.out.println(e.getServiceId());
				});
			}
		}

		System.exit(0);
		for (Tree<ServiceCallRecord> scTree : scTrees) {
			if (cons_sessions.contains(scTree.getRoot().getData().getSessionId())) {
				printTree(scTree);
			}
		}
	}

	private static List<ServiceCallSession> extractSessions(List<ServiceCallRecord> entryCalls) {
		Map<String, List<ServiceCallRecord>> sessionMapping = new HashMap<>();
		for (ServiceCallRecord entryCall : entryCalls) {
			if (!sessionMapping.containsKey(entryCall.getSessionId())) {
				sessionMapping.put(entryCall.getSessionId(), Lists.newArrayList());
			}
			sessionMapping.get(entryCall.getSessionId()).add(entryCall);
		}

		return sessionMapping.entrySet().stream().map(e -> new ServiceCallSession(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}

	private static void printTree(Tree<ServiceCallRecord> tree) {
		printTreeRecursive(tree.getRoot(), 0);
	}

	private static void printTreeRecursive(TreeNode<ServiceCallRecord> root, int i) {
		String spaces = "";
		for (int k = 0; k < i; k++) {
			spaces += " ";
		}
		System.out.println(spaces + "- " + root.getData().getServiceId() + " [" + root.getData().getHostName() + "] -> "
				+ root.getData().getExternalCallId());
		for (TreeNode<ServiceCallRecord> child : root.getChildren()) {
			printTreeRecursive(child, i + 2);
		}
	}

}
