package cz.cuni.mff.d3s.spl.data.readers;

import cz.cuni.mff.d3s.spl.DataReader;
import cz.cuni.mff.d3s.spl.DataSource;

import java.io.File;
import java.util.Map;


/**
 * Provides unified interface to read data revision from files.
 *
 * Each implementation will specify it's semantics in detail,
 * but common idea is that each reader processes data from
 * given file(s) and returns them as a map with method/benchmark
 * name as a key and data as a value.
 */
public interface RevisionReader {

	/**
	 * Read one revision of data from given files. There shouldn't be
	 * more revisions of the same benchmark/method/... data in one file
	 * or you shouldn't pass multiple files containing the same data with
	 * different revisions.
	 *
	 * @param files Input files with raw data
	 * @return Processed data as a map grouped by benchmark/method/...
	 *          If there is no info about name in the data, "default"
	 *          key is used instead.
	 */
	Map<String, DataSource> readRevision(File... files) throws DataReader.ReaderException;
}
