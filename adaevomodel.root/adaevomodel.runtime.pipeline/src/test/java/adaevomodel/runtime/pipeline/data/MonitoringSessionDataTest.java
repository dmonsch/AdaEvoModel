package adaevomodel.runtime.pipeline.data;

import java.util.List;

import adaevomodel.bridge.monitoring.util.MonitoringDataUtil;
import dmodel.designtime.monitoring.records.PCMContextRecord;
import kieker.analysis.exception.AnalysisConfigurationException;

public class MonitoringSessionDataTest {

	public static void main(String[] args) {
		String path = "/Users/david/Desktop/Dynamic Approach/Final Version/Implementation/git/adaevomodel.root/adaevomodel.tools.benchmarks/data/teastore_bench/monitoring/training/kieker-20200918-175356-1132451267659-UTC--KIEKER";
		try {
			List<PCMContextRecord> data = MonitoringDataUtil.getMonitoringDataFromFiles(path);
			SessionPartionedMonitoringData spmd = new SessionPartionedMonitoringData(data, 0.2f);
			System.out.println(spmd.getTrainingData().size());
			System.out.println(spmd.getValidationData().size());
			System.out.println(spmd.getAllData().size());
		} catch (IllegalStateException | AnalysisConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
