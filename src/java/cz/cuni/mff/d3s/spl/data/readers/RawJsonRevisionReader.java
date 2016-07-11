package cz.cuni.mff.d3s.spl.data.readers;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunBuilder;
import cz.cuni.mff.d3s.spl.data.BuilderDataSource;
import cz.cuni.mff.d3s.spl.data.DataSnapshotBuilder;
import cz.cuni.mff.d3s.spl.utils.Factory;
import cz.cuni.mff.d3s.spl.DataReader.ReaderException;

import javax.json.*;
import javax.json.stream.JsonParsingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Reader for new JSON format of JMH generated data.
 *
 * This reader can read revision from one or multiple files. Typical
 * use case is only one file with array of all benchmarks, but multiple
 * files with data from the same revision can be passed as well. In
 * this file format each benchmark has a name in source file, so it's
 * the key in the results. Note, that benchmark name must be unique
 * across all provided files!
 */
public class RawJsonRevisionReader implements RevisionReader {

	/**
	 * Read one revision from given files. There are multiple benchmark
	 * data in each file and benchmark names in different files must be
	 * unique. File format is new JMH JSON format.
	 *
	 * @param files Input files with raw data
	 * @return Processed data with benchmark names as keys with corresponding values
	 * @throws ReaderException On reading or parsing error
	 */
	@Override
	public Map<String, DataSource> readRevision(File... files) throws ReaderException {
		Map<String, DataSource> result = new HashMap<>();

		for (File file : files) {
			try {
				JsonReader jsonReader = Json.createReader(new FileInputStream(file));
				JsonArray benchmarks = jsonReader.readArray();

				for (JsonValue benchmark : benchmarks) {
					Map.Entry<String, DataSource> benchmarkData = getBenchmarkData((JsonObject) benchmark);
					if (result.containsKey(benchmarkData.getKey())) {
						throw new ReaderException("Duplicate benchmark key: " + benchmarkData.getKey());
					}
					result.put(benchmarkData.getKey(), benchmarkData.getValue());
				}

			} catch (FileNotFoundException e) {
				throw new ReaderException("File not found: " + e.getMessage());
			} catch (JsonParsingException e) {
				throw new ReaderException("Error parsing file: " + file.getName() +
						" at location: " + e.getLocation().toString());
			} catch (JsonException e) {
				throw new ReaderException("Json error: " + e.getMessage());
			}
		}

		return result;
	}

	public static class RevisionFactory implements Factory<RawJsonRevisionReader> {
		@Override
		public RawJsonRevisionReader getInstance() {
			return new RawJsonRevisionReader();
		}
	}

	/**
	 * Parse data for one benchmark.
	 *
	 * @param benchmark Json object (dictionary) with one benchmark data
	 * @return Parsed data
	 */
	private static Map.Entry<String, DataSource> getBenchmarkData(JsonObject benchmark) {
		String benchmarkName = benchmark.getString("benchmark");
		JsonObject primaryMetric = benchmark.getJsonObject("primaryMetric");
		JsonArray rawData = primaryMetric.getJsonArray("rawData");
		DataSource data = parseRawData(rawData);
		return new AbstractMap.SimpleEntry<>(benchmarkName, data);
	}

	/**
	 * Parse array of raw data. Each item is list of two values:
	 * actual number and number of observations of this number.
	 *
	 * @param rawData Json array with raw data representation
	 * @return Parsed data
	 */
	private static DataSource parseRawData(JsonArray rawData) {
		BenchmarkRunBuilder run = new BenchmarkRunBuilder();

		// for each sample
		for (JsonValue sample : rawData) {
			JsonArray sampleValue = (JsonArray) sample;

			int valueCount = sampleValue.getInt(1);
			for (int i = 0; i < valueCount; i++) {
				//run.addSamples((long)value.getJsonNumber(0).doubleValue());
				run.addSamples(Math.round(sampleValue.getJsonNumber(0).doubleValue()));
			}
		}

		DataSnapshotBuilder builder = new DataSnapshotBuilder();
		BenchmarkRun benchmarkRun = run.create();
		builder.addRun(benchmarkRun);

		return new BuilderDataSource(builder);
	}
}
