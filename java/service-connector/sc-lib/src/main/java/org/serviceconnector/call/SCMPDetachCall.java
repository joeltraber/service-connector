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
import org.serviceconnector.scmp.SCMPMsgType;

/**
 * The Class SCMPDetachCall. Call detaches on SCMP level.
 *
 * @author JTraber
 */
public class SCMPDetachCall extends SCMPCallAdapter {

	/** The Constant LOGGER. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(SCMPDetachCall.class);

	/**
	 * Instantiates a new SCMPDetachCall.
	 *
	 * @param req the client to use when invoking call
	 */
	public SCMPDetachCall(IRequester req) {
		super(req);
	}

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.DETACH;
	}
}
