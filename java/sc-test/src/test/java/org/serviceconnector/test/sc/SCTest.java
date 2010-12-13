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
package org.serviceconnector.test.sc;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMessageFault;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.test.sc.attach.AttachTestCase;
import org.serviceconnector.test.sc.attach.DetachTestCase;
import org.serviceconnector.test.sc.connectionPool.ConnectionPoolTestCase;
import org.serviceconnector.test.sc.group.GroupCallTestCase;
import org.serviceconnector.test.sc.manage.ManageTestCase;
import org.serviceconnector.test.sc.operationTimeout.SrvExecuteOTITestCase;
import org.serviceconnector.test.sc.publish.PublishLargeMessagesTestCase;
import org.serviceconnector.test.sc.register.DeRegisterServerTestCase;
import org.serviceconnector.test.sc.register.RegisterServerTestCase;
import org.serviceconnector.test.sc.scVersion.SCVersionToSCTest;
import org.serviceconnector.test.sc.session.ClnCreateSessionTestCase;
import org.serviceconnector.test.sc.session.ClnCreateSessionWaitMechanismTestCase;
import org.serviceconnector.test.sc.session.ClnDeleteSessionTestCase;
import org.serviceconnector.test.sc.session.SessionTimeoutTestCase;
import org.serviceconnector.test.sc.srvExecute.aynch.SrvExecuteAsyncTestCase;
import org.serviceconnector.test.sc.srvExecute.aynch.SrvExecuteLargeAsyncTestCase;
import org.serviceconnector.test.sc.srvExecute.synch.SrvExecuteLargeSyncTestCase;
import org.serviceconnector.test.sc.srvExecute.synch.SrvExecuteSyncTestCase;
import org.serviceconnector.test.sc.subscribe.ClnSubscribeTestCase;
import org.serviceconnector.test.sc.subscribe.ClnSubscribeWaitMechanismTestCase;
import org.serviceconnector.test.sc.subscriptionChange.ClnChangeSubscriptionTestCase;
import org.serviceconnector.test.unit.DecodeSCMPVersionTest;
import org.serviceconnector.test.unit.DefaultEncoderDecoderTest;
import org.serviceconnector.test.unit.DefaultFrameDecoderTest;
import org.serviceconnector.test.unit.HttpFrameDecoderTest;
import org.serviceconnector.test.unit.KeepAliveMessageEncoderDecoderTest;
import org.serviceconnector.test.unit.LargeMessageEncoderDecoderTest;
import org.serviceconnector.test.unit.LinkedQueueTest;
import org.serviceconnector.test.unit.SCMPCompositeTest;
import org.serviceconnector.test.unit.SCMPLargeRequestTest;
import org.serviceconnector.test.unit.SCMPLargeResponseTest;
import org.serviceconnector.test.unit.SCMPVersionTest;
import org.serviceconnector.test.unit.SCVersionTest;
import org.serviceconnector.test.unit.ValidatorUtilityTest;

/**
 * @author JTraber
 */
@RunWith(Suite.class)
@SuiteClasses( { AttachTestCase.class, //
		DetachTestCase.class, // 
		ClnCreateSessionTestCase.class, // 
		ClnDeleteSessionTestCase.class, //
		ClnCreateSessionWaitMechanismTestCase.class, //
		RegisterServerTestCase.class, // 
		DeRegisterServerTestCase.class, // 
		SrvExecuteSyncTestCase.class, // 
		SrvExecuteLargeSyncTestCase.class, // 
		SrvExecuteAsyncTestCase.class,// 
		SrvExecuteLargeAsyncTestCase.class, //
		SrvExecuteOTITestCase.class, //
		ClnSubscribeTestCase.class, //
		ClnSubscribeWaitMechanismTestCase.class, //
		ClnChangeSubscriptionTestCase.class, //
		PublishLargeMessagesTestCase.class, //
		ManageTestCase.class, //
		ConnectionPoolTestCase.class, //  
		SCVersionToSCTest.class, // 
		DecodeSCMPVersionTest.class, // 
		SessionTimeoutTestCase.class, //
		GroupCallTestCase.class, //
		// SCImplTestCases
		DefaultFrameDecoderTest.class,//
		HttpFrameDecoderTest.class, //
		SCMPCompositeTest.class,//
		SCMPLargeRequestTest.class, //
		SCMPLargeResponseTest.class,//
		LargeMessageEncoderDecoderTest.class, //
		KeepAliveMessageEncoderDecoderTest.class,//
		DefaultEncoderDecoderTest.class,//
		ValidatorUtilityTest.class, //
		LinkedQueueTest.class, //
		// SCAPITestCases
		SCVersionTest.class, //
		SCMPVersionTest.class })
public class SCTest {

	private SCTest() {
	}

	public static void verifyError(SCMPMessage result, SCMPError error, String additionalInfo, SCMPMsgType msgType) {
		Assert.assertEquals(msgType.getValue(), result.getHeader(SCMPHeaderAttributeKey.MSG_TYPE));
		// TODO TRN refine SCMPErrors
		/*
		 * text must not be compared! It may be Chinese Assert.assertEquals(error.getErrorText() + additionalInfo,
		 * result.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
		 */
		Assert.assertEquals(error.getErrorCode(), result.getHeader(SCMPHeaderAttributeKey.SC_ERROR_CODE));
	}

	public static Map<String, String> splitStringToMap(String stringToSplit, String entryDelimiter, String keyDelimiter) {
		Map<String, String> map = new HashMap<String, String>();

		String[] rows = stringToSplit.split(entryDelimiter);

		for (String row : rows) {
			String[] keyValue = row.split(keyDelimiter, 2);
			map.put(keyValue[0], keyValue[1]);
		}
		return map;
	}

	public static void assertEqualsUnorderedStringIgnorePorts(String expected, String actual) {
		// actual = actual.replaceAll("127.0.0.1/", "localhost/");
		actual = actual.replaceAll("localhost/\\d*:", "localhost/:");

		Map<String, String> expectedMap = splitStringToMap(expected, "\\|", "\\:");
		Map<String, String> actualMap = splitStringToMap(actual, "\\|", "\\:");

		if (expectedMap.equals(actualMap) == false) {
			System.out.println("actual : " + actual);
			System.out.println("expected : " + expected);
		}
		Assert.assertEquals(expectedMap, actualMap);
	}

	public static Map<String, String> convertInspectStringToMap(String string) {
		Map<String, String> map = new HashMap<String, String>();

		String[] values = string.split("@|&");
		for (int i = 0; i < values.length / 2; i++) {
			map.put(values[i * 2], values[i * 2 + 1]);
		}
		return map;
	}

	public static void checkReply(SCMPMessage message) throws Exception {
		if (message.isFault()) {
			SCMPMessageFault fault = (SCMPMessageFault) message;
			Exception ex = fault.getCause();
			if (ex != null) {
				throw ex;
			}
			throw new Exception(fault.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
		}
	}
}