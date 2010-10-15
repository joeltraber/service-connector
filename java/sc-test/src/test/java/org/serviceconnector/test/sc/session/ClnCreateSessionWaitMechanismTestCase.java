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
package org.serviceconnector.test.sc.session;

import junit.framework.Assert;

import org.junit.Test;
import org.serviceconnector.call.SCMPCallFactory;
import org.serviceconnector.call.SCMPClnCreateSessionCall;
import org.serviceconnector.call.SCMPClnDeleteSessionCall;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.test.sc.SCTest;
import org.serviceconnector.test.sc.attach.SuperAttachTestCase;
import org.serviceconnector.util.SynchronousCallback;

public class ClnCreateSessionWaitMechanismTestCase extends SuperAttachTestCase {

	/**
	 * The Constructor.
	 * 
	 * @param fileName
	 *            the file name
	 */
	public ClnCreateSessionWaitMechanismTestCase(String fileName) {
		super(fileName);
	}

	@Test
	public void waitForConnectionInCreateSession_secondCCSTimesOut() throws Exception {
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "1conn");
		createSessionCall.setSessionInfo("sessionInfo");
		createSessionCall.setEchoIntervalSeconds(100);
		createSessionCall.setRequestBody("wait:2000");
		WaitMechanismCallback callback = new WaitMechanismCallback(true);
		createSessionCall.invoke(callback, 10000);
		
		// to assure second create is not faster
		Thread.sleep(20);
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall1 = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "1conn");
		createSessionCall1.setSessionInfo("sessionInfo");
		createSessionCall1.setEchoIntervalSeconds(100);
		WaitMechanismCallback callback1 = new WaitMechanismCallback(true);
		createSessionCall1.invoke(callback1, 1000);

		SCMPMessage responseMessage = callback.getMessageSync();
		SCMPMessage responseMessage1 = callback1.getMessageSync();

		SCTest.checkReply(responseMessage);
		Assert.assertFalse(responseMessage.isFault());
		Assert.assertTrue(responseMessage1.isFault());
		SCTest.verifyError(responseMessage1, SCMPError.SC_ERROR, "[no free connection on server for service 1conn]",
				SCMPMsgType.CLN_CREATE_SESSION);

		SCMPClnDeleteSessionCall deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL
				.newInstance(this.req, responseMessage.getServiceName(), responseMessage.getSessionId());
		deleteSessionCall.invoke(this.attachCallback, 10000);
		this.attachCallback.getMessageSync();
	}

	@Test
	public void waitForConnectionInCreateSession_secondCCSWaits() throws Exception {
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "1conn");
		createSessionCall.setSessionInfo("sessionInfo");
		createSessionCall.setEchoIntervalSeconds(100);
		createSessionCall.setRequestBody("wait:1000");
		WaitMechanismCallback callback = new WaitMechanismCallback(true);
		createSessionCall.invoke(callback, 10000);

		// to assure second create is not faster
		Thread.sleep(20);
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall1 = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "1conn");
		createSessionCall1.setSessionInfo("sessionInfo");
		createSessionCall1.setEchoIntervalSeconds(100);
		WaitMechanismCallback callback1 = new WaitMechanismCallback(true);
		createSessionCall1.invoke(callback1, 10000);

		SCMPMessage responseMessage = callback.getMessageSync();
		SCMPMessage responseMessage1 = callback1.getMessageSync();

		SCTest.checkReply(responseMessage);
		SCTest.checkReply(responseMessage1);

		SCMPClnDeleteSessionCall deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL
				.newInstance(this.req, responseMessage.getServiceName(), responseMessage.getSessionId());
		deleteSessionCall.invoke(this.attachCallback, 10000);
		this.attachCallback.getMessageSync();
		deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL.newInstance(this.req,
				responseMessage1.getServiceName(), responseMessage1.getSessionId());
		deleteSessionCall.invoke(this.attachCallback, 10000);
		this.attachCallback.getMessageSync();
	}
	
	@Test
	public void waitForSessionInCreateSession_secondCCSTimesOut() throws Exception {
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "1sess");
		createSessionCall.setSessionInfo("sessionInfo");
		createSessionCall.setEchoIntervalSeconds(100);
		createSessionCall.setRequestBody("wait:2000");
		WaitMechanismCallback callback = new WaitMechanismCallback(true);
		createSessionCall.invoke(callback, 10000);
		
		// to assure second create is not faster
		Thread.sleep(20);
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall1 = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "1sess");
		createSessionCall1.setSessionInfo("sessionInfo");
		createSessionCall1.setEchoIntervalSeconds(100);
		WaitMechanismCallback callback1 = new WaitMechanismCallback(true);
		createSessionCall1.invoke(callback1, 1000);

		SCMPMessage responseMessage = callback.getMessageSync();
		SCMPMessage responseMessage1 = callback1.getMessageSync();

		SCTest.checkReply(responseMessage);
		Assert.assertFalse(responseMessage.isFault());
		Assert.assertTrue(responseMessage1.isFault());
		SCTest.verifyError(responseMessage1, SCMPError.NO_FREE_SESSION, "[for service 1sess]",
				SCMPMsgType.CLN_CREATE_SESSION);

		SCMPClnDeleteSessionCall deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL
				.newInstance(this.req, responseMessage.getServiceName(), responseMessage.getSessionId());
		deleteSessionCall.invoke(this.attachCallback, 10000);
		this.attachCallback.getMessageSync();
	}
	
	@Test
	public void waitForSessionInCreateSession_secondCCSWaits() throws Exception {
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "1sess");
		createSessionCall.setSessionInfo("sessionInfo");
		createSessionCall.setEchoIntervalSeconds(100);
		createSessionCall.setRequestBody("wait:1000");
		WaitMechanismCallback callback = new WaitMechanismCallback(true);
		createSessionCall.invoke(callback, 10000);

		// to assure second create is not faster
		Thread.sleep(20);
		// sets up a create session call
		SCMPClnCreateSessionCall createSessionCall1 = (SCMPClnCreateSessionCall) SCMPCallFactory.CLN_CREATE_SESSION_CALL
				.newInstance(req, "1sess");
		createSessionCall1.setSessionInfo("sessionInfo");
		createSessionCall1.setEchoIntervalSeconds(100);
		WaitMechanismCallback callback1 = new WaitMechanismCallback(true);
		createSessionCall1.invoke(callback1, 10000);

		SCMPMessage responseMessage = callback.getMessageSync();
		SCMPClnDeleteSessionCall deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL
		.newInstance(this.req, responseMessage.getServiceName(), responseMessage.getSessionId());
		deleteSessionCall.invoke(this.attachCallback, 10000);
		this.attachCallback.getMessageSync();

		SCMPMessage responseMessage1 = callback1.getMessageSync();

		SCTest.checkReply(responseMessage);
		SCTest.checkReply(responseMessage1);

		deleteSessionCall = (SCMPClnDeleteSessionCall) SCMPCallFactory.CLN_DELETE_SESSION_CALL.newInstance(this.req,
				responseMessage1.getServiceName(), responseMessage1.getSessionId());
		deleteSessionCall.invoke(this.attachCallback, 10000);
		this.attachCallback.getMessageSync();
	}

	protected class WaitMechanismCallback extends SynchronousCallback {

		public WaitMechanismCallback(boolean synchronous) {
			this.synchronous = synchronous;
		}
	}
}