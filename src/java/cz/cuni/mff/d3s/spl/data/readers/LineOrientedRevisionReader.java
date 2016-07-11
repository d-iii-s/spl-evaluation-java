package cz.cuni.mff.d3s.spl.data.readers;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunBuilder;
import cz.cuni.mff.d3s.spl.data.BuilderDataSource;
import cz.cuni.mff.d3s.spl.data.DataSnapshotBuilder;
import cz.cuni.mff.d3s.spl.utils.Factory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Reader for text files with one integer number per line.
 *
 * This reader expects multiple files (each is separate benchmark run)
 * with one integer (long type) number per line. There're no data about
 * naming, so it's expected that all data are from the same benchmark
 * and results are returned with the key "default".
 */
public class LineOrientedRevisionReader implements RevisionReader {
	/**
	 * Read one revision of data from given files. It's expected that
	 * each file is separate run and all data are from the same benchmark.
	 *
	 * @param files Input files with raw data
	 * @return Processed data with key "default"
	 * @throws ReaderException On reading or parsing error
	 */
	@Override
	public Map<String, DataSource> readRevision(File... files) throws ReaderException {
		DataSnapshotBuilder snapshotBuilder = new DataSnapshotBuilder();
		for (File file : files) {
	    	try {
	    		BenchmarkRun run = readLineOrientedData(new FileInputStream(file)).create();
	    		snapshotBuilder.addRun(run);
	    	} catch (FileNotFoundException e) {
			    throw new ReaderException("File not found: " + e.getMessage());
	    	}
		}

		HashMap<String, DataSource> result = new HashMap<>();
		result.put("default", new BuilderDataSource(snapshotBuilder));
		return result;
	}

	public static class RevisionFactory implements Factory<LineOrientedRevisionReader> {
		@Override
		public LineOrientedRevisionReader getInstance() {
			return new LineOrientedRevisionReader();
		}
	}

	/** Reads samples from a data stream.
	 *
	 * Expects each sample is on a separate line, silently ignores
	 * lines containing something else than positive integer.
	 *
	 * @param is Input stream with data.
	 * @return Benchmark run with the samples.
	 * @throws ReaderException on reading failure.
	 */
	private BenchmarkRunBuilder readLineOrientedData(InputStream is) throws ReaderException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		BenchmarkRunBuilder run = new BenchmarkRunBuilder();
		String line;

		try {
			while ((line = reader.readLine()) != null) {
				// skip empty lines
				if (!line.equals("")) {
					long value = Long.parseLong(line);
					run.addSamples(value);
				}
			}
		} catch (NumberFormatException e) {
			throw new ReaderException("Wrong number format: " + e.getMessage());
		} catch (IOException e) {
			throw new ReaderException("IO error: " + e.getMessage());
		}

		return run;
	}
}
