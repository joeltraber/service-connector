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
package com.stabilit.sc.unit.test;

import org.junit.After;
import org.junit.Before;

import com.stabilit.sc.cln.io.SCMPSession;
import com.stabilit.sc.cln.service.SCMPCallFactory;
import com.stabilit.sc.cln.service.SCMPCreateSessionCall;
import com.stabilit.sc.cln.service.SCMPDeleteSessionCall;

/**
 * @author JTraber
 * 
 */
public abstract class SuperSessionTestCase extends SuperConnectTestCase {

	protected SCMPSession scmpSession = null;

	public SuperSessionTestCase() {
		super();
	}

	@Before
	public void setup() throws Exception {
		super.setup();
		createSession();
	}

	@After
	public void tearDown() throws Exception {
		deleteSession();
		super.tearDown();
	}

	public void createSession() throws Exception {
		SCMPCreateSessionCall createSessionCall = (SCMPCreateSessionCall) SCMPCallFactory.CREATE_SESSION_CALL
				.newInstance(client);

		createSessionCall.setServiceName("simulation");
		createSessionCall.setSessionInfo("SNBZHP - TradingClientGUI 10.2.7");
		scmpSession = createSessionCall.invoke();
	}

	public void deleteSession() throws Exception {
		SCMPDeleteSessionCall deleteSessionCall = (SCMPDeleteSessionCall) SCMPCallFactory.DELETE_SESSION_CALL
				.newInstance(client, scmpSession);
		deleteSessionCall.invoke();
	}
}