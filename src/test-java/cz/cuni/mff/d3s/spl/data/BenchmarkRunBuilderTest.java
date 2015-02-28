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

import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.d3s.spl.BenchmarkRun;
import cz.cuni.mff.d3s.spl.tests.TestUtils;

public class BenchmarkRunBuilderTest {
	
	private BenchmarkRunBuilder builder;
	
	@Before
	public void setUp() {
		builder = new BenchmarkRunBuilder();
	}
	
	@Test
	public void emptyRun() {
		TestUtils.assertBenchmarkRun(builder.create());
	}
	
	@Test
	public void fewValues() {
		builder.addSamples(0);
		builder.addSamples(1, 2);
		
		TestUtils.assertBenchmarkRun(builder.create(), 0, 1, 2);
	}
	
	@Test
	public void createdRunsAreImmutable() {
		builder.addSamples(0, 1, 2);
		BenchmarkRun run1 = builder.create();
		TestUtils.assertBenchmarkRun(run1, 0, 1, 2);

		builder.addSamples(3, 4, 5);
		BenchmarkRun run2 = builder.create();
		
		TestUtils.assertBenchmarkRun(run1, 0, 1, 2);
		TestUtils.assertBenchmarkRun(run2, 0, 1, 2, 3, 4, 5);
	}
}
