/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright � 2010 STABILIT Informatik AG, Switzerland                  *
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
package com.stabilit.sc.unit.test.register;

import org.junit.After;
import org.junit.Before;

import com.stabilit.sc.cln.io.SCMPSession;
import com.stabilit.sc.cln.service.SCMPCallFactory;
import com.stabilit.sc.cln.service.SCMPDeRegisterServiceCall;
import com.stabilit.sc.cln.service.SCMPRegisterServiceCall;
import com.stabilit.sc.unit.test.SuperTestCase;

/**
 * @author JTraber
 * 
 */
public abstract class SuperRegisterTestCase extends SuperTestCase {

	/**
	 * @param fileName
	 */
	public SuperRegisterTestCase(String fileName) {
		super(fileName);
	}

	protected SCMPSession scmpSession = null;

	@Before
	public void setup() throws Exception {
		super.setup();
		registerService();
	}

	@After
	public void tearDown() throws Exception {
		deRegisterService();
		super.tearDown();
	}

	public void registerService() throws Exception {
		SCMPRegisterServiceCall registerServiceCall = (SCMPRegisterServiceCall) SCMPCallFactory.REGISTER_SERVICE_CALL
				.newInstance(client);

		registerServiceCall.setServiceName("P01_RTXS_RPRWS1");
		registerServiceCall.setMaxSessions(10);
		registerServiceCall.setMultithreaded(true);
		registerServiceCall.setPortNumber(9100);
		registerServiceCall.invoke();
	}

	public void deRegisterService() throws Exception {
		SCMPDeRegisterServiceCall deRegisterServiceCall = (SCMPDeRegisterServiceCall) SCMPCallFactory.DEREGISTER_SERVICE_CALL
				.newInstance(client);
		deRegisterServiceCall.setServiceName("P01_RTXS_RPRWS1");
		deRegisterServiceCall.invoke();
	}
}