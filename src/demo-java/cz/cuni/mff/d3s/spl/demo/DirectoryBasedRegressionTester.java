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
package cz.cuni.mff.d3s.spl.demo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.d3s.spl.DataSource;
import cz.cuni.mff.d3s.spl.Formula;
import cz.cuni.mff.d3s.spl.Result;
import cz.cuni.mff.d3s.spl.data.FileDataSource;
import cz.cuni.mff.d3s.spl.formula.SplFormula;
import cz.cuni.mff.d3s.spl.interpretation.WelchTestInterpretation;

/** Detect performance regressions from already measured data.
 * 
 * <p>
 * We expect that benchmark results for each version of the software
 * are stored in a separate directory that can contain multiple runs.
 *
 */
public class DirectoryBasedRegressionTester {
	private static final double SIGNIFICANCE_LEVEL = 0.95;
	
	private static class Revision {
		public String name;
		public DataSource data;
		
		public Revision(String n, DataSource d) {
			name = n;
			data = d;
		}
	}
	
	public static void main(String[] directoryNames) {
		if (directoryNames.length == 0) {
			printUsage();
			System.exit(1);
		}
		
		List<Revision> datas = new ArrayList<>(directoryNames.length);
		
		for (String dirname : directoryNames) {
			File dir = new File(dirname);
			
			System.out.printf("Reading data from %s...", dirname);
			
			DataSource data = FileDataSource.load(dir.listFiles());
			
			System.out.printf(" ok, %d run(s).\n", data.makeSnapshot().getRunCount());
			
			datas.add(new Revision(dir.getName(), data));
		}
		
		Formula improved = SplFormula.create("new < old");
		improved.setInterpretation(new WelchTestInterpretation());
		
		System.out.printf("Looking for regressions and suspicious data...\n");
		
		for (int i = 0; i < datas.size() - 1; i++) {
			improved.bind("old", datas.get(i).data);
			improved.bind("new", datas.get(i + 1).data);
			
			Result result = improved.evaluate(SIGNIFICANCE_LEVEL);
			if (result != Result.TRUE) {
				System.out.printf("Possible regression between %s and %s!\n",
						datas.get(i).name, datas.get(i + 1).name);
			}
		}
	}

	private static void printUsage() {
		final String selfName = DirectoryBasedRegressionTester.class.getName();
		System.out.printf("Usage: java [jvm-opts] %s directory-names\n", selfName);
		System.out.println(" Directories (versions) shall be ordered from oldest");
		System.out.println(" to newest.");
	}
}
