package cz.cuni.mff.d3s.spl.data.readers;

import cz.cuni.mff.d3s.spl.*;
import cz.cuni.mff.d3s.spl.data.Revision;
import cz.cuni.mff.d3s.spl.utils.Factory;

import java.io.File;
import java.util.*;

/**
 * Data reader from formats like JMH JSON. Generic type provides
 * revision reader from specific format variant.
 */
public class JsonDataReader<T extends RevisionReader> implements DataReader {
	/**
	 * Revision reader instance.
	 */
	private T reader;

	/**
	 * Constructor which creates revision reader instance.
	 *
	 * @param readerFactory Factory for creating instance of revision reader.
	 *                      The instance must be of the same type as generic
	 *                      type T.
	 */
	public JsonDataReader(Factory<T> readerFactory) {
		reader = readerFactory.getInstance();
	}

	/**
	 * Reads multiple revision data from files. Requires one argument,
	 * directory where are all revisions, one per file. Revision name
	 * is set as corresponding filename. Each revision file should contain
	 * data for all measured benchmarks/methods/... with names. RevisionReader
	 * of T type should preserve these names. Revisions are ordered by their
	 * filesystem modification time.
	 *
	 * @param args Array of 1 string with path to root directory of measured data.
	 * @return Map with name of measured unit as key and list of revisions for that
	 *          unit as value.
	 */
	@Override
	public Map<String, List<Revision>> readData(String[] args) {
		if (args.length != 1) {
			// TODO: throw some exception
			System.exit(1);
		}

		Map<String, List<Revision>> data = new HashMap<>();

		File dir = new File(args[0]);
		File[] files = dir.listFiles();
		Arrays.sort(files, new FileComparator());

		for (File file : files) {
			System.out.printf("Reading data from %s revision...", file.getName());

			Map<String, DataSource> revisionData = null;
			try {
				revisionData = reader.readRevision(file);
			} catch (RevisionReader.ReaderException e) {
				// TODO: throw some exception
				e.printStackTrace();
				System.exit(2);
			}

			for (Map.Entry<String, DataSource> benchmark : revisionData.entrySet()) {
				if (!data.containsKey(benchmark.getKey())) {
					data.put(benchmark.getKey(), new LinkedList<>());
				}
				data.get(benchmark.getKey()).add(new Revision(file.getName(), benchmark.getValue()));
			}

			System.out.println(" ok");
		}

		return data;
	}

	/**
	 * Compare files by their filesystem modification time. Newer file
	 * is less than older file.
	 */
	private static class FileComparator implements Comparator<File> {
		@Override
		public int compare(File x, File y) {
			long xModified = x.lastModified();
			long yModified = y.lastModified();

			if (xModified == yModified) {
				return 0;
			} else if (xModified > yModified) {
				return 1;
			} else {
				return -1;
			}
		}
	}
}
