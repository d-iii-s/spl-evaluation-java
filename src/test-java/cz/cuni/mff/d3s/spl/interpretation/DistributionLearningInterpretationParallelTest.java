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
package cz.cuni.mff.d3s.spl.interpretation;

import org.junit.Before;

/** Test for the distribution-learning interpretation.
 *
 * Note that we test only the basic properties and not precise values because
 * the actual values are random and bound to change between runs.
 *
 */
public class DistributionLearningInterpretationParallelTest extends DistributionLearningInterpretationTest {
	@Before
	public void prepareInterpretation() {
		interpretation = new DistributionLearningInterpretationParallel();
	}
}
