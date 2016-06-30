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

import cz.cuni.mff.d3s.spl.DataSnapshot;
import cz.cuni.mff.d3s.spl.DataSource;

/** Utility class to read data from streams.
 *
 */
public class BuilderDataSource implements DataSource {
	private DataSnapshotBuilder builder;

	public BuilderDataSource(DataSnapshotBuilder builder) {
		this.builder = builder;
	}

	@Override
	public DataSnapshot makeSnapshot() {
		return builder.create();
	}

	@Override
	public DataSnapshot makeSnapshot(int skip) {
		return builder.create(skip);
	}

	@Override
	public DataSnapshot makeSnapshot(double skip) {
		return builder.create(skip);
	}
}
