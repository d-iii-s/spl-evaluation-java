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
public class JmhJsonRevisionReader implements RevisionReader {

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
						//throw new ReaderException("Duplicate benchmark key: " + benchmarkData.getKey());
						DataSource mergedData = mergeBenchmarkData(benchmarkData.getValue(),
								result.get(benchmarkData.getKey()));
						result.put(benchmarkData.getKey(), mergedData);
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
			} catch (ReaderException e) {
				throw e;
			} catch (Throwable e) {
				throw new ReaderException(e.getMessage());
			}
		}

		return result;
	}

	private DataSource mergeBenchmarkData(DataSource oldValue, DataSource newValue) {
		DataSnapshotBuilder builder = new DataSnapshotBuilder();

		for (int i = 0; i < oldValue.makeSnapshot().getRunCount(); i++) {
			builder.addRun(oldValue.makeSnapshot().getRun(i));
		}
		for (int i = 0; i < newValue.makeSnapshot().getRunCount(); i++) {
			builder.addRun(newValue.makeSnapshot().getRun(i));
		}

		return new BuilderDataSource(builder);
	}

	public static class RevisionFactory implements Factory<JmhJsonRevisionReader> {
		@Override
		public JmhJsonRevisionReader getInstance() {
			return new JmhJsonRevisionReader();
		}
	}

	/**
	 * Parse data for one benchmark.
	 *
	 * @param benchmark Json object (dictionary) with one benchmark data
	 * @return Parsed data
	 */
	private static Map.Entry<String, DataSource> getBenchmarkData(JsonObject benchmark) throws ReaderException {
		String benchmarkName = benchmark.getString("benchmark");
		String benchmarkMode = benchmark.getString("mode");
		String benchmarkParams = "";
		if (benchmark.containsKey("params")) {
			benchmarkParams = getBenchmarkParams(benchmark.getJsonObject("params"));
		}
		String benchmarkEntryKey = String.format("%s@%s%s", benchmarkName, benchmarkMode, benchmarkParams);

		JsonObject primaryMetric = benchmark.getJsonObject("primaryMetric");
		JsonArray rawData = null;
		JsonArray rawDataHistogram = null;
		if (primaryMetric.containsKey("rawData")) {
			rawData = primaryMetric.getJsonArray("rawData");
		} else if (primaryMetric.containsKey("rawDataHistogram")) {
			rawDataHistogram = primaryMetric.getJsonArray("rawDataHistogram");
		}
		DataSource data = parseRawData(rawData, rawDataHistogram);
		return new AbstractMap.SimpleEntry<>(benchmarkEntryKey, data);
	}

	/**
	 * Get params with which the benchmark was run.
	 *
	 * @param params Json object containing all params (the "params" key from JMH json)
	 * @return Params with format "@par1=val1@par2=val2"
	 */
	private static String getBenchmarkParams(JsonObject params) {
		StringBuilder result = new StringBuilder();
		for (String item : params.keySet()) {
			String value = params.getString(item);
			result.append("@");
			result.append(item);
			result.append("=");
			result.append(value);
		}
		return result.toString();
	}

	/**
	 * Parse array of raw data. Actual data are only in one of the arguments
	 * array, second array is null value.
	 *
	 * @param rawData Json array with raw data representation, just numbers.
	 * @param rawDataHistogram Json array with raw data histogram representation,
	 *                         each item is list of two values: actual number and
	 *                         number of observations of this number.
	 * @return Parsed data
	 */
	private static DataSource parseRawData(JsonArray rawData, JsonArray rawDataHistogram) throws ReaderException {
		BenchmarkRunBuilder run = new BenchmarkRunBuilder();

		if (rawData != null) {
			// process rawData

			// for each fork
			for (JsonValue fork : rawData) {
				// for each iteration value
				for (JsonValue value : (JsonArray)fork) {
					run.addSamples(((JsonNumber)value).doubleValue());
				}
			}
		} else if (rawDataHistogram != null) {
			// process rawDataHistogram

			// for each fork
			for (JsonValue fork : rawDataHistogram) {
				// for each iteration
				for (JsonValue iteration : (JsonArray)fork) {
					// for each sample
					for (JsonValue sample : (JsonArray)iteration) {
						JsonArray sampleValue = (JsonArray)sample;

						int valueCount = sampleValue.getInt(1);
						for (int i = 0; i < valueCount; i++) {
							run.addSamples(sampleValue.getJsonNumber(0).doubleValue());
						}
					}
				}
			}
		} else {
			// corrupted invariant
			throw new ReaderException("One of \"rawData\" and \"rawDataHistogram\" must be empty, but not both");
		}

		DataSnapshotBuilder builder = new DataSnapshotBuilder();
		BenchmarkRun benchmarkRun = run.create();
		builder.addRun(benchmarkRun);

		return new BuilderDataSource(builder);
	}
}
