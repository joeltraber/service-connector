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
package com.stabilit.sc.unit.test.echo;

import junit.framework.Assert;

import org.junit.Test;

import com.stabilit.sc.cln.service.ISCMPCall;
import com.stabilit.sc.cln.service.SCMPCallFactory;
import com.stabilit.sc.cln.service.SCMPClnEchoCall;
import com.stabilit.sc.common.scmp.SCMP;
import com.stabilit.sc.common.scmp.SCMPBodyType;
import com.stabilit.sc.common.scmp.SCMPHeaderAttributeKey;
import com.stabilit.sc.common.scmp.SCMPMsgType;
import com.stabilit.sc.unit.test.session.SuperSessionTestCase;

public class SrvEchoLargeTestCase extends SuperSessionTestCase {

	/**
	 * @param fileName
	 */
	public SrvEchoLargeTestCase(String fileName) {
		super(fileName);
	}

	protected Integer index = null;

//	@Test
	public void invokeTwoPartsTest() throws Exception {

		SCMPClnEchoCall echoCall = (SCMPClnEchoCall) SCMPCallFactory.CLN_ECHO_CALL.newInstance(client,
				scmpSession);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 19000; i++) {
			sb.append(i);
		}
		echoCall.setBody(sb.toString());
		echoCall.setMaxNodes(2);
		SCMP result = echoCall.invoke();
		/*************************** verify echo session **********************************/
		Assert.assertEquals(sb.toString(), result.getBody());
		Assert.assertEquals(SCMPBodyType.text.getName(), result.getHeader(SCMPHeaderAttributeKey.BODY_TYPE));
		Assert.assertEquals("3/3", result.getHeader(SCMPHeaderAttributeKey.MESSAGE_ID));
		Assert.assertEquals(sb.length() + "", result.getHeader(SCMPHeaderAttributeKey.BODY_LENGTH));
		Assert.assertEquals(SCMPMsgType.CLN_ECHO.getResponseName(), result.getMessageType());
		Assert.assertNotNull(result.getSessionId());
	}

//	@Test
	public void invokeMorePartsTest() throws Exception {
		SCMPClnEchoCall echoCall = (SCMPClnEchoCall) SCMPCallFactory.CLN_ECHO_CALL.newInstance(client,
				scmpSession);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 89840; i++) {
			sb.append(i);
		}
		echoCall.setBody(sb.toString());
		echoCall.setMaxNodes(2);
		SCMP result = echoCall.invoke();
		/*************************** verify echo session **********************************/
		Assert.assertEquals(sb.toString(), result.getBody());
		Assert.assertEquals(SCMPBodyType.text.getName(), result
				.getHeader(SCMPHeaderAttributeKey.BODY_TYPE));
		Assert.assertEquals("3/15", result.getHeader(SCMPHeaderAttributeKey.MESSAGE_ID));
		Assert.assertEquals(sb.length() + "", result.getHeader(SCMPHeaderAttributeKey.BODY_LENGTH));
		Assert.assertEquals(SCMPMsgType.CLN_ECHO.getResponseName(), result.getMessageType());
		Assert.assertNotNull(result.getSessionId());
	}
	
	@Test
	public void invokeUnknownEndPartsTest() throws Exception {
		SCMPClnEchoCall echoCall = (SCMPClnEchoCall) SCMPCallFactory.CLN_ECHO_CALL.newInstance(client,
				scmpSession);
		ISCMPCall groupCall = echoCall.openGroup();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 10; i++) {
			sb.append(i);
			groupCall.setBody(String.valueOf(i));
			echoCall.setMaxNodes(2);
			groupCall.invoke();
		}
		SCMP res = groupCall.closeGroup(); // send REQ (no body content)
		Assert.assertEquals(sb.toString(), res.getBody());
		Assert.assertEquals(SCMPBodyType.text.getName(), res.getHeader(SCMPHeaderAttributeKey.BODY_TYPE));
//	TODO	Assert.assertEquals("1/10", res.getHeader(SCMPHeaderAttributeKey.MESSAGE_ID));
		Assert.assertEquals(sb.length() + "", res.getHeader(SCMPHeaderAttributeKey.BODY_LENGTH.getName()));
		Assert.assertEquals(SCMPMsgType.CLN_ECHO.getResponseName(), res.getMessageType());
		Assert.assertNotNull(res.getSessionId());
	}
	
	@Test
	public void invokeUnknownEndLargePartsTest() throws Exception {
		SCMPClnEchoCall echoCall = (SCMPClnEchoCall) SCMPCallFactory.CLN_ECHO_CALL.newInstance(client,
				scmpSession);
		ISCMPCall groupCall = echoCall.openGroup();
		StringBuilder sb = new StringBuilder();
		StringBuilder expected = new StringBuilder();
		for(int i = 0; i < 19000; i++) {
			sb.append(i);
		}
		int max = 1;
		for (int i = 0; i < max; i++) {
			expected.append(sb.toString());
			groupCall.setBody(sb.toString());
			echoCall.setMaxNodes(2);
			groupCall.invoke();
		}
		SCMP res = groupCall.closeGroup(); // send REQ (no body content)
		Assert.assertEquals(expected.toString(), res.getBody());
		Assert.assertEquals(SCMPBodyType.text.getName(), res.getHeader(SCMPHeaderAttributeKey.BODY_TYPE));
		//TODO Assert.assertEquals("1/" + (max * 4), res.getHeader(SCMPHeaderAttributeKey.MESSAGE_ID));
		Assert.assertEquals(expected.length() + "", res.getHeader(SCMPHeaderAttributeKey.BODY_LENGTH.getName()));
		Assert.assertEquals(SCMPMsgType.CLN_ECHO.getResponseName(), res.getMessageType());
		Assert.assertNotNull(res.getSessionId());
	}
}
