package adaevomodel.tools.benchmarks.init;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import adaevomodel.tools.benchmarks.json.JsonBenchmarkData;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class BenchmarkDataProvider {
	public static final File BASE_PATH = new File("data");

	public static boolean prepareBenchmarkData(JsonBenchmarkData data) {
		if (!BASE_PATH.exists()) {
			BASE_PATH.mkdirs();
		}
		if (new File(BASE_PATH, data.getPath()).exists()) {
			return true; // already exists
		}

		try {
			File tempFile = File.createTempFile("benchmark", ".zip");
			boolean b1 = downloadBenchmarkData(data.getDataUrl(), tempFile);
			if (b1 == false)
				return false;
			boolean b2 = unzipBenchmarkData(tempFile, new File(BASE_PATH, data.getPath()));
			tempFile.delete();
			return b2;
		} catch (IOException e) {
			return false;
		}
	}

	private static boolean unzipBenchmarkData(File file, File basePath) {
		try {
			new ZipFile(file).extractAll(basePath.getAbsolutePath());
			return true;
		} catch (ZipException e) {
			return false;
		}
	}

	private static boolean downloadBenchmarkData(String url, File file) {
		try {
			FileUtils.copyURLToFile(new URL(url), file);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
