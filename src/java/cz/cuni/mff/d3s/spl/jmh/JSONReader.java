package cz.cuni.mff.d3s.spl.jmh;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunBuilder;
import cz.cuni.mff.d3s.spl.data.DataSnapshotBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import javax.json.*;

/**
 * Created by petr on 28.6.16.
 */
public class JSONReader {
	/**
	 * Read data from proposed JMH JSON format
	 *
	 * @param file
	 * @return
	 */
	public static Map<String, DataSource> readRevision(File file) {
		Map<String, DataSource> result = new HashMap<>();

		try {
			JsonReader jsonReader = Json.createReader(new FileInputStream(file));
			JsonArray jsonst = jsonReader.readArray();

			for (JsonValue benchmark : jsonst) {
				Map.Entry<String, DataSource> benchmarkData = getBenchmarkData((JsonObject)benchmark);
				result.put(benchmarkData.getKey(), benchmarkData.getValue());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

	private static Map.Entry<String, DataSource> getBenchmarkData(JsonObject benchmark) {
		String benchmarkName = benchmark.getString("benchmark");
		JsonObject primaryMetric = benchmark.getJsonObject("primaryMetric");
		JsonArray rawData = primaryMetric.getJsonArray("rawData");
		DataSource data = parseRawData(rawData);
		return new AbstractMap.SimpleEntry<>(benchmarkName, data);
	}

	private static DataSource parseRawData(JsonArray rawData) {
		BenchmarkRunBuilder run = new BenchmarkRunBuilder();

		// for each sample
		for (JsonValue sample : rawData) {
			JsonArray value = (JsonArray) sample;

			int valueCount = value.getInt(1);
			for (int i = 0; i < valueCount; i++) {
				//run.addSamples((long)value.getJsonNumber(0).doubleValue());
				run.addSamples(Math.round(value.getJsonNumber(0).doubleValue()));
			}
		}

		BenchmarkRun benchmarkRun = run.create();
		DataSnapshotBuilder builder = new DataSnapshotBuilder();
		builder.addRun(benchmarkRun);
		return new JsonDataSource(builder);
	}
}

class JsonDataSource implements DataSource {
	private DataSnapshotBuilder builder;

	public JsonDataSource(DataSnapshotBuilder builder) {
		this.builder = builder;
	}

	@Override
	public DataSnapshot makeSnapshot() {
		return builder.create();
	}

	@Override
	public DataSnapshot makeSnapshot(int skip) {
		return builder.create(skip);
	}

	@Override
	public DataSnapshot makeSnapshot(double skip) {
		return builder.create(skip);
	}
}