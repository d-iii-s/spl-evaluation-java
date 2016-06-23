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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import cz.cuni.mff.d3s.spl.BenchmarkRun;

/** Utility class to read data from streams.
 *
 */
public class BenchmarkRunReader {
	
	/** Reads samples from a data stream.
	 * 
	 * @param is Input stream with data.
	 * @return Benchmark run with the samples.
	 */
	public static BenchmarkRun fromNumbersOnly(InputStream is) {
		BenchmarkRunBuilder builder = new BenchmarkRunBuilder();
		
		Scanner sc = new Scanner(is);
		while (sc.hasNextLong()) {
			builder.addSamples(sc.nextLong());
		}
		sc.close();
		
		return builder.create();
	}
	
	/** Reads samples from a data stream.
	 * 
	 * <p>
	 * Expects each sample is on a separate line, silently ignores
	 * lines containing something else than positive integer.
	 * 
	 * @param is Input stream with data.
	 * @return Benchmark run with the samples.
	 * @throws IOException on reading failure.
	 */
	public static BenchmarkRun fromLineOriented(InputStream is) throws IOException {
		BenchmarkRunBuilder run = fromLineOrientedInner(is);
		return run.create();
	}
	
	/** Reads samples from a data stream.
	 * 
	 * Expects each sample is on a separate line, silently ignores
	 * lines containing something else than positive integer.
	 * 
	 * @param is Input stream with data.
	 * @param skip Skip this number of samples from the beginning.
	 * @return Benchmark run with the samples.
	 * @throws IOException on reading failure.
	 */
	public static BenchmarkRun fromLineOriented(InputStream is, int skip) throws IOException {
		BenchmarkRunBuilder run = fromLineOrientedInner(is);
		return run.create(skip);
	}

	/** Reads samples from a data stream.
	 * 
	 * Expects each sample is on a separate line, silently ignores
	 * lines containing something else than positive integer.
	 * 
	 * @param is Input stream with data.
	 * @param skip Skip this percentage of samples from the beginning.
	 * @return Benchmark run with the samples.
	 * @throws IOException on reading failure.
	 */
	public static BenchmarkRun fromLineOriented(InputStream is, double skip) throws IOException {
		BenchmarkRunBuilder run = fromLineOrientedInner(is);
		return run.create(skip);
	}

	/** Reads samples from a data stream.
	 * 
	 * Expects each sample is on a separate line, silently ignores
	 * lines containing something else than positive integer.
	 * 
	 * @param is Input stream with data.
	 * @return Benchmark run with the samples.
	 * @throws IOException on reading failure.
	 */
	private static BenchmarkRunBuilder fromLineOrientedInner(InputStream is) throws IOException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    BenchmarkRunBuilder run = new BenchmarkRunBuilder();
	    String line;
	    while ((line = reader.readLine()) != null) {
	    	try {
	    	    long value = Long.parseLong(line);
				run.addSamples(value);
	    	} catch (NumberFormatException e) {
		    }
	    }
	    return run;
	}
}
