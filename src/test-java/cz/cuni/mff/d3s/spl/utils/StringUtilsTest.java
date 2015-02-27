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
package cz.cuni.mff.d3s.spl.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class StringUtilsTest {

	private List<String> oneMemberList;
	private List<String> twoMemberList;
	private List<String> threeMemberList;
	
	@Before
	public void setUpLists() {
		oneMemberList = new ArrayList<>();
		oneMemberList.add("alpha");
		
		twoMemberList = new ArrayList<>();
		twoMemberList.add("alpha");
		twoMemberList.add("bravo");
		
		threeMemberList = new ArrayList<>();
		threeMemberList.add("alpha");
		threeMemberList.add("bravo");
		threeMemberList.add("charlie");
	}
	
	private void assertJoin(String expected, List<?> list) {
		assertEquals(expected, StringUtils.join(list));
	}
	
	@Test
	public void emptyListJoiningProducesEmptyString() {
		assertJoin("", Collections.EMPTY_LIST);
	}
	
	@Test
	public void testJoiningSingleMemberList() {
		assertJoin("alpha", oneMemberList);
	}
	
	@Test
	public void testJoiningTwoMemberList() {
		assertJoin("alpha,bravo", twoMemberList);
	}
	
	@Test
	public void testJoiningThreeMemberList() {
		assertJoin("alpha,bravo,charlie", threeMemberList);
	}
}
