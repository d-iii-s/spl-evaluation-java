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

import org.junit.Test;

import cz.cuni.mff.d3s.spl.tests.TestUtils;

public class RingDataSourceTest {	
	
	@Test
	public void singleRunSingleSample() {
		RingDataSource src = RingDataSource.create(0, 1, 1);
		
		src.startRun();
		src.addSamples(5);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 5 }
		});
		
		src.addSamples(6, 7, 8);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 8 }
		});
		
		src.startRun();
		src.addSamples(9, 10, 11);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 11 }
		});
	}
	
	@Test
	public void singleRunUnlimitedSamples() {
		RingDataSource src = RingDataSource.createWithLimitedNumberOfRuns(1);
		
		src.startRun();
		src.addSamples(5);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 5 }
		});
		
		src.addSamples(6, 7, 8);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 5, 6, 7, 8 }
		});
		
		src.startRun();
		src.addSamples(9, 10, 11);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 9, 10, 11 }
		});
	}
	
	@Test
	public void twoRunsUnlimitedSamples() {
		RingDataSource src = RingDataSource.createWithLimitedNumberOfRuns(2);
		
		src.startRun();
		src.addSamples(5);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 5 }
		});
		
		src.addSamples(6, 7, 8);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 5, 6, 7, 8 }
		});
		
		src.startRun();
		src.addSamples(9, 10, 11);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 5, 6, 7, 8 },
			new double[] { 9, 10, 11 }
		});
		
		src.startRun();
		src.addSamples(12, 13);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 9, 10, 11 },
			new double[] { 12, 13 }
		});
	}
	
	@Test
	public void twoRunsTwoEpochsUnlimitedSamples() {
		RingDataSource src = RingDataSource.create(2, 2, Integer.MAX_VALUE);
		
		src.startRun();
		src.addSamples(5, 10);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 5, 10 }
		});
		
		src.startRun();
		src.addSamples(11, 12, 13);
		
		src.startRun();
		src.addSamples(14, 16, 18, 20);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 11, 12, 13 },
			new double[] { 14, 16, 18, 20 }
		});
		
		src.startEpoch();
		
		src.startRun();
		src.addSamples(21, 22);
		
		src.startRun();
		src.addSamples(23, 24);
		
		src.startRun();
		src.addSamples(25, 27, 29);
		
		TestUtils.assertDataSnapshot(src.makeSnapshot(), new double[][] {
			new double[] { 23, 24 },
			new double[] { 25, 27, 29 },
			null,
			new double[] { 11, 12, 13 },
			new double[] { 14, 16, 18, 20 }
		});
	}
}
