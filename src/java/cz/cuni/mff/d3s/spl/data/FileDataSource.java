/*
 * Copyright 2015 Charles University in Prague
 * Copyright 2015 Vojtech Horky
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.cuni.mff.d3s.spl.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.DataSource;

/** Utility class to read data from streams.
 *
 */
public class FileDataSource implements DataSource {
	
	private DataSnapshotBuilder snapshotBuilder = null;
	private DataSnapshot previousEpoch = null;
	private List<File> files;

	//
	
	@FunctionalInterface
	private interface FunctionThatThrows<T, R, E extends Throwable> {
	        R apply(T t) throws E;
	}	

	//
	
	/** Create a data source from given files.
	 * 
	 * @param files List of files to read from (sample per line).
	 * @return Data source backed by the files.
	 */
	public static FileDataSource load(File... files) {
		return loadInner (files, x -> x.reload());
	}
	
	/** Create a data source from given files.
	 * 
	 * @param skip Skip this number of samples from the beginning of each file.
	 * @param files List of files to read from (sample per line).
	 * @return Data source backed by the files.
	 */
	public static FileDataSource load(int skip, File... files) {
	    	return loadInner (files, x -> x.reload (skip));
	}
	
	/** Create a data source from given files.
	 * 
	 * @param skip Skip this number of samples from the beginning of each file.
	 * @param files List of files to read from (sample per line).
	 * @return Data source backed by the files.
	 */
	public static FileDataSource load(double skip, File... files) {
	    	return loadInner (files, x -> x.reload (skip));
	}
	
	//
	
	/** Create a data source from given files.
	 * 
	 * @param files List of files to read from (sample per line).
	 * @return Data source backed by the files.
	 */
	public static FileDataSource load(Collection<File> files) {
		return loadInner(files.toArray (new File[0]), x -> x.reload());
	}
	
	/** Create a data source from given files.
	 * 
	 * @param skip Skip this number of samples from the beginning of each file.
	 * @param files List of files to read from (sample per line).
	 * @return Data source backed by the files.
	 */
	public static FileDataSource load(int skip, Collection<File> files) {
		return loadInner(files.toArray (new File[0]), x -> x.reload (skip));
	}

	/** Create a data source from given files.
	 * 
	 * @param skip Skip this number of samples from the beginning of each file.
	 * @param files List of files to read from (sample per line).
	 * @return Data source backed by the files.
	 */
	public static FileDataSource load(double skip, Collection<File> files) {
		return loadInner(files.toArray (new File[0]), x -> x.reload (skip));
	}

	private static FileDataSource loadInner(File [] files, Consumer<FileDataSource> loader) {
	    FileDataSource result = new FileDataSource (files);
	    loader.accept (result);
	    return result;
	}
	
	//
	
	private FileDataSource(File... files) {
		this.files = Arrays.asList(files);
		initBuilder();
	}

	/** {@inheritDoc} */
	@Override
	public DataSnapshot makeSnapshot() {
		return snapshotBuilder.create();
	}
	
	/** Allow to set previous epoch for the data source.
	 * 
	 * @param data Previous epoch data.
	 */
	public void setPreviousEpochData(DataSnapshot data) {
		previousEpoch = data;
	}

	//
	
	/** Reload the values from the same files.
	 */
	public void reload() { reloadInner(x -> BenchmarkRunReader.fromLineOriented (x)); }
	
	/** Reload the values from the same files.
	 * 
	 * @param skip Skip this number of samples from the beginning of each file.
	 */
	public void reload(int skip) { reloadInner(x -> BenchmarkRunReader.fromLineOriented (x, skip)); }
	
	/** Reload the values from the same files.
	 * 
	 * @param skip Skip this percentage of samples from the beginning of each file.
	 */
	public void reload(double skip) { reloadInner(x -> BenchmarkRunReader.fromLineOriented (x, skip)); }

	/** Reload the values from the same files.
	 * 
	 * @param reader Reader to use for the values.
	 */
	private void reloadInner(FunctionThatThrows<FileInputStream, BenchmarkRun, IOException> reader) {
	    initBuilder ();
	    for (File file : files) {
	    	try {
	    		BenchmarkRun run = reader.apply (new FileInputStream(file));
	    		snapshotBuilder.addRun (run);
	    	} catch (FileNotFoundException e) {
	    	} catch (IOException e) {
	    	}
	    }
	}
	
	//
	
	private void initBuilder() {
		snapshotBuilder = new DataSnapshotBuilder();
		snapshotBuilder.setPreviousEpoch(previousEpoch);
	}
}
