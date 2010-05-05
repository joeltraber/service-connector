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
package com.stabilit.sc.service;

import java.util.Map;

import com.stabilit.sc.cln.client.IClient;
import com.stabilit.sc.cln.service.ISCMPCall;
import com.stabilit.sc.cln.service.SCMPCallAdapter;
import com.stabilit.sc.scmp.SCMP;
import com.stabilit.sc.scmp.SCMPHeaderAttributeKey;
import com.stabilit.sc.scmp.SCMPMsgType;

/**
 * @author JTraber
 * 
 */
public class SCMPSrvCreateSessionCall extends SCMPCallAdapter {

	public SCMPSrvCreateSessionCall() {
		this(null, null);
	}

	public SCMPSrvCreateSessionCall(IClient client, SCMP scmpSession) {
		super(client, scmpSession);
	}
	
	@Override
	public ISCMPCall newInstance(IClient client, SCMP scmpSession) {
		return new SCMPSrvCreateSessionCall(client, scmpSession);
	}
	
	public void setSessionId(String sessionId) {
		call.setHeader(SCMPHeaderAttributeKey.SESSION_ID, sessionId);
	}

	public void setHeader(Map<String, String> header) {
		this.call.setHeader(header);
	}
	
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.SRV_CREATE_SESSION;
	}
}
