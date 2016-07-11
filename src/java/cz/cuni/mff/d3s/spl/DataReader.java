package cz.cuni.mff.d3s.spl;

import cz.cuni.mff.d3s.spl.data.Revision;

import java.util.List;
import java.util.Map;


/**
 * Provides unified interface to read measured data.
 *
 * Each implementation will specify it's argument semantics. Most
 * of the readers use one of RevisionReaders to fetch single revision
 * data.
 */
public interface DataReader {
	/**
	 * Reads measured data.
	 *
	 * @param args Specific arguments for each reader.
	 * @return Map of benchmark/method/... name ("default" if
	 *          the data doesn't provide custom name) and list
	 *          of data revisions.
	 */
	Map<String, List<Revision>> readData(String[] args);
}
