/*
 * Copyright � 2010 STABILIT Informatik AG, Switzerland *
 * *
 * Licensed under the Apache License, Version 2.0 (the "License"); *
 * you may not use this file except in compliance with the License. *
 * You may obtain a copy of the License at *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0 *
 * *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS, *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and *
 * limitations under the License. *
 */
package org.serviceconnector.test.system.api.cln;

import junit.framework.Assert;

import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.service.SCServiceException;
import org.serviceconnector.test.system.api.APISystemSuperSessionClientTest;

@SuppressWarnings("unused")
public class APIAfterSCAbortOrRestartSessionTest extends APISystemSuperSessionClientTest {

	/**
	 * Description: client has session and gets notified after SC was aborted<br>
	 * Expectation: passes
	 */
	@Test
	public void t01_notifyClientAfterSCcrash() throws Exception {
		SCMessage request = new SCMessage();	
		SCMessage response = null;
		sessionService = client.newSessionService(TestConstants.sesServiceName1);
		msgCallback = new MsgCallback(sessionService);
		sessionService.setEchoIntervalInSeconds(2); // want be notified quickly!
		response = sessionService.createSession(request, msgCallback);
		Assert.assertNotNull("the session ID is null", sessionService.getSessionId());

		ctrl.stopServer(sesSrvCtx); // stop test server now, it cannot be stopped without SC later
		ctrl.stopSC(scCtx);

		msgCallback.waitForMessage(5);
		Assert.assertTrue("error code is not set", msgCallback.getScErrorCode() > 0);
		Assert.assertNotNull("error text the not set", msgCallback.getScErrorText());
	}
}