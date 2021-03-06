/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright © 2010 STABILIT Informatik AG, Switzerland                  *
 *                                                                             *
 *  Licensed under the Apache License, Version 2.0 (the "License");            *
 *  you may not use this file except in compliance with the License.           *
 *  You may obtain a copy of the License at                                    *
 *                                                                             *
 *  http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                             *
 *  Unless required by applicable law or agreed to in writing, software        *
 *  distributed under the License is distributed on an "AS IS" BASIS,          *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
 *  See the License for the specific language governing permissions and        *
 *  limitations under the License.                                             *
 *-----------------------------------------------------------------------------*/
package org.serviceconnector.test.unit.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.serviceconnector.api.SCPublishMessage;
import org.serviceconnector.test.unit.SuperUnitTest;

import junit.framework.Assert;

public class APISCPublishMessageTest extends SuperUnitTest {

	/** The Constant LOGGER. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(APISCPublishMessageTest.class);

	private SCPublishMessage message;

	@Override
	@Before
	public void beforeOneTest() throws Exception {
		super.beforeOneTest();
		message = new SCPublishMessage();
	}

	@Override
	@After
	public void afterOneTest() {
		message = null;
		super.afterOneTest();
	}

	/**
	 * Description: Check default values <br>
	 * Expectation: passed, all values are default
	 */
	@Test
	public void t01_constructor() {
		Assert.assertEquals("mask is not null", null, message.getMask());
	}

}
