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
package com.stabilit.scm.unit.cln.api;

import org.junit.Before;
import org.junit.Test;

import com.stabilit.scm.cln.service.ISessionService;
import com.stabilit.scm.cln.service.SCMessage;
import com.stabilit.scm.common.service.IServiceConnector;
import com.stabilit.scm.common.service.ServiceConnectorFactory;
import com.stabilit.scm.unit.test.SetupTestCases;

public class ClnAPISessionTestCase {

	@Before
	public void setUp() {
		SetupTestCases.setupAll();
	}
	
	@Test
	public void testClnAPI() throws Exception {
		
		IServiceConnector sc = null;
		try {
			sc = ServiceConnectorFactory.newInstance("localhost", 8080);
			sc.setAttribute("keepAliveInterval", 60);
			sc.setAttribute("keepAliveTimeout", 10);

			// connects to SC, starts observing connection
			sc.connect();
					
			ISessionService sessionServiceA = sc.newSessionService("simulation");
			sessionServiceA.setSessionInfo("sessionInfo");
			sessionServiceA.createSession();
			
			SCMessage requestMsg = new SCMessage();
			byte[] buffer = new byte[1024];
			requestMsg.setData(buffer);
			requestMsg.setCompression(false);
			requestMsg.setMessageInfo("test");
			SCMessage responseMsg = sessionServiceA.execute(requestMsg);

			System.out.println(responseMsg);

			// deletes the session
			sessionServiceA.deleteSession();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// disconnects from SC
				sc.disconnect();
			} catch (Exception e) {
				sc = null;
			}
		}
	}
}
