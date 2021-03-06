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
package org.serviceconnector.test.integration.scmp;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.serviceconnector.TestCallback;
import org.serviceconnector.TestConstants;
import org.serviceconnector.TestUtil;
import org.serviceconnector.call.SCMPAttachCall;
import org.serviceconnector.call.SCMPDetachCall;
import org.serviceconnector.conf.RemoteNodeConfiguration;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.net.req.SCRequester;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.test.integration.IntegrationSuperTest;
import org.serviceconnector.util.ValidatorUtility;

import junit.framework.Assert;

@RunWith(Parameterized.class)
public class SCMPAttachDetachTest extends IntegrationSuperTest {

	private int port;
	private ConnectionType connectionType;
	private SCRequester requester;

	public SCMPAttachDetachTest(Integer port, ConnectionType connectionType) {
		this.port = port;
		this.connectionType = connectionType;
	}

	@Parameters
	public static Collection<Object[]> getParameters() {
		return Arrays.asList( //
				new Object[] { new Integer(TestConstants.PORT_SC0_TCP), ConnectionType.NETTY_TCP }, //
				new Object[] { new Integer(TestConstants.PORT_SC0_HTTP), ConnectionType.NETTY_HTTP });
	}

	@Override
	@After
	public void afterOneTest() throws Exception {
		try {
			requester.destroy();
		} catch (Exception e) {
		}
		requester = null;
		super.afterOneTest();
	}

	/**
	 * Description: Attach and detach one time to SC on localhost<br>
	 * Expectation: passes
	 */
	@Test
	public void t01_AttachDetach() throws Exception {
		this.requester = new SCRequester(new RemoteNodeConfiguration(TestConstants.RemoteNodeName, TestConstants.HOST, this.port, this.connectionType.getValue(), 0, 0, 1), 0);
		SCMPAttachCall attachCall = new SCMPAttachCall(this.requester);
		TestCallback callback1 = new TestCallback();
		attachCall.invoke(callback1, 10000);
		SCMPMessage result = callback1.getMessageSync(10000);
		TestUtil.checkReply(result);

		Assert.assertNull(result.getBody());
		Assert.assertNull(result.getMessageSequenceNr());
		Assert.assertEquals(SCMPMsgType.ATTACH.getValue(), result.getHeader(SCMPHeaderAttributeKey.MSG_TYPE));
		ValidatorUtility.validateDateTime(result.getHeader(SCMPHeaderAttributeKey.LOCAL_DATE_TIME), SCMPError.HV_WRONG_LDT);

		TestCallback callback2 = new TestCallback();
		SCMPDetachCall detachCall = new SCMPDetachCall(this.requester);
		detachCall.invoke(callback2, 10000);
		result = callback2.getMessageSync(10000);
		TestUtil.checkReply(result);
		Assert.assertNull(result.getBody());
		Assert.assertNull(result.getMessageSequenceNr());
		Assert.assertEquals(SCMPMsgType.DETACH.getValue(), result.getHeader(SCMPHeaderAttributeKey.MSG_TYPE));
	}
}
