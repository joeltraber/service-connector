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
package org.serviceconnector.api.cln;

import org.serviceconnector.Constants;
import org.serviceconnector.api.SCServiceException;
import org.serviceconnector.call.SCMPReceivePublicationCall;
import org.serviceconnector.log.PerformanceLogger;
import org.serviceconnector.net.req.SCRequester;
import org.serviceconnector.scmp.SCMPError;

public class SCGuardianService extends SCPublishService {

	public SCGuardianService(SCClient scClient, String serviceName, SCRequester requester) {
		super(scClient, serviceName, requester);
	}

	/**
	 * Sends a receive publication (CRP) to the SC. Is only used internally (method visibility). The registered message callback from the client gets informed in case of an error.
	 * Operation timeout for receive publication is (receivePublicationTimeoutSeconds+noDataIntervalSeconds).
	 */
	@Override
	void receivePublication() {
		// 1. checking preconditions and initialize
		if (this.sessionActive == false) {
			return;
		}
		this.requester.getSCMPMsgSequenceNr().incrementAndGetMsgSequenceNr();
		// 2. initialize call & invoke
		SCGuardianCallback callback = new SCGuardianCallback(this, (SCGuardianMessageCallback) this.messageCallback);
		SCMPReceivePublicationCall receivePublicationCall = new SCMPReceivePublicationCall(this.requester, this.serviceName, this.sessionId);
		try {
			PerformanceLogger.begin(this.sessionId);
			receivePublicationCall.invoke(callback, Constants.SEC_TO_MILLISEC_FACTOR * (this.receivePublicationTimeoutSeconds + this.noDataIntervalSeconds));
		} catch (Exception e) {
			PerformanceLogger.end(this.sessionId);
			// inactivate the session
			this.sessionActive = false;
			SCServiceException ex = new SCServiceException("Receive publication failed.");
			ex.setSCErrorCode(SCMPError.BROKEN_SUBSCRIPTION.getErrorCode());
			ex.setSCErrorText(SCMPError.BROKEN_SUBSCRIPTION.getErrorText("Receive publication for service=" + this.serviceName + " failed."));
			this.messageCallback.receive(ex);
			return;
		}
	}
}
