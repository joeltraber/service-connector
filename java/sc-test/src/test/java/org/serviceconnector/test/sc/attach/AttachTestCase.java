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
package org.serviceconnector.test.sc.attach;

import junit.framework.Assert;

import org.junit.Test;
import org.serviceconnector.call.SCMPAttachCall;
import org.serviceconnector.call.SCMPCallFactory;
import org.serviceconnector.call.SCMPDetachCall;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.test.sc.SCTest;
import org.serviceconnector.test.sc.SuperTestCase;
import org.serviceconnector.util.SynchronousCallback;
import org.serviceconnector.util.ValidatorUtility;



public class AttachTestCase extends SuperTestCase {

	/**
	 * The Constructor.
	 * 
	 * @param parameter
	 *            the parameter
	 */
	public AttachTestCase(String parameter) {
		super(parameter);
	}

	@Test
	public void attach() throws Exception {
		SCMPAttachCall attachCall = (SCMPAttachCall) SCMPCallFactory.ATTACH_CALL.newInstance(req);

		AttachTestCallback callback = new AttachTestCallback();
		attachCall.invoke(callback, 1000);
		SCMPMessage result = callback.getMessageSync();
		SCTest.checkReply(result);
		/*********************************** Verify attach response msg **********************************/
		Assert.assertNull(result.getBody());
		Assert.assertEquals(SCMPMsgType.ATTACH.getValue(), result.getHeader(SCMPHeaderAttributeKey.MSG_TYPE));
		Assert.assertNotNull(ValidatorUtility.validateLocalDateTime(result
				.getHeader(SCMPHeaderAttributeKey.LOCAL_DATE_TIME)));

		SCMPDetachCall detachCall = (SCMPDetachCall) SCMPCallFactory.DETACH_CALL.newInstance(req);
		detachCall.invoke(callback, 1000);
		SCTest.checkReply(callback.getMessageSync());
	}

	private class AttachTestCallback extends SynchronousCallback {
		// nothing to implement in this case - everything is done by super-class
	}
}