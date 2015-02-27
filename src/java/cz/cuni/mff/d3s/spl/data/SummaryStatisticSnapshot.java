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

import cz.cuni.mff.d3s.spl.StatisticSnapshot;

/** Computes statistic snapshot from given samples without remembering them.
 */
public class SummaryStatisticSnapshot implements StatisticSnapshot {
	private long sampleCount;
	private double mean;
	
	public SummaryStatisticSnapshot(Iterable<Long> data) {
		sampleCount = 0;
		double sum = 0;
		for (Long l : data) {
			sum += l;
			sampleCount++;
		}
		if (sampleCount > 0) {
			mean = sum / sampleCount;
		}
	}
	
	public SummaryStatisticSnapshot(long[] data) {
		sampleCount = 0;
		double sum = 0;
		for (long l : data) {
			sum += l;
			sampleCount++;
		}
		if (sampleCount > 0) {
			mean = sum / sampleCount;
		}
	}
	
	@Override
	public double getArithmeticMean() {
		return mean;
	}

	@Override
	public long getSampleCount() {
		return sampleCount;
	}

	@Override
	public long[] getSamples() {
		throw new UnsupportedOperationException("Original samples not available.");
	}

}
