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
package org.serviceconnector.call;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.serviceconnector.net.req.IRequester;
import org.serviceconnector.scmp.ISCMPMessageCallback;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.util.DateTimeUtility;

/**
 * The Class SCMPAttachCall. Call attaches on SCMP level.
 *
 * @author JTraber
 */
public class SCMPAttachCall extends SCMPCallAdapter {

	/** The Constant LOGGER. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SCMPAttachCall.class);

	/**
	 * Instantiates a new SCMPAttachCall.
	 *
	 * @param req the requester to use when invoking call
	 */
	public SCMPAttachCall(IRequester req) {
		super(req);
	}

	/** {@inheritDoc} */
	@Override
	public void invoke(ISCMPMessageCallback scmpCallback, int timeoutMillis) throws Exception {
		this.setVersion(SCMPMessage.SC_VERSION.toString());
		this.setLocalDateTime(DateTimeUtility.getCurrentTimeZoneMillis());
		super.invoke(scmpCallback, timeoutMillis);
	}

	/**
	 * Sets the version.
	 *
	 * @param version the new version
	 */
	private void setVersion(String version) {
		this.requestMessage.setHeader(SCMPHeaderAttributeKey.SC_VERSION, version);
	}

	/**
	 * Sets the local date time.
	 *
	 * @param localDateTime the new local date time
	 */
	private void setLocalDateTime(String localDateTime) {
		this.requestMessage.setHeaderCheckNull(SCMPHeaderAttributeKey.LOCAL_DATE_TIME, localDateTime);
	}

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.ATTACH;
	}
}
