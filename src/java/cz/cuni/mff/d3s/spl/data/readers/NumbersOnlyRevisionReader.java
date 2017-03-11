package cz.cuni.mff.d3s.spl.data.readers;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.DataReader.ReaderException;
import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.data.BenchmarkRunBuilder;
import cz.cuni.mff.d3s.spl.data.BuilderDataSource;
import cz.cuni.mff.d3s.spl.data.DataSnapshotBuilder;
import cz.cuni.mff.d3s.spl.utils.Factory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Reader for text files with one integer number per line.
 *
 * This reader expects multiple files (each is separate benchmark run)
 * with long numbers in binary form. There're no data about naming,
 * so it's expected that all data are from the same benchmark
 * and results are returned with the key "default".
 */
public class NumbersOnlyRevisionReader implements RevisionReader {
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
				BenchmarkRun run = readNumbersOnlyData(new FileInputStream(file)).create();
				snapshotBuilder.addRun(run);
			} catch (FileNotFoundException e) {
				throw new ReaderException("File not found: " + e.getMessage());
			}
		}

		HashMap<String, DataSource> result = new HashMap<>();
		result.put("default", new BuilderDataSource(snapshotBuilder));
		return result;
	}

	public static class RevisionFactory implements Factory<NumbersOnlyRevisionReader> {
		@Override
		public NumbersOnlyRevisionReader getInstance() {
			return new NumbersOnlyRevisionReader();
		}
	}

	/** Reads samples from a data stream.
	 *
	 * Expects samples as numbers of long type in input stream. Reading ends when
	 * there's no next number in the stream.
	 *
	 * @param is Input stream with data.
	 * @return Benchmark run with the samples.
	 * @throws ReaderException on reading failure.
	 */
	private BenchmarkRunBuilder readNumbersOnlyData(InputStream is) throws ReaderException {
		BenchmarkRunBuilder run = new BenchmarkRunBuilder();

		Scanner sc = new Scanner(is);
		while (sc.hasNextLong()) {
			run.addSamples(sc.nextLong());
		}
		sc.close();

		return run;
	}
}
