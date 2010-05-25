/*
 *-----------------------------------------------------------------------------*
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
 *-----------------------------------------------------------------------------*
/*
/**
 * 
 */
package com.stabilit.sc.examples;

import com.stabilit.sc.cln.call.SCMPCallFactory;
import com.stabilit.sc.cln.call.SCMPClnDataCall;
import com.stabilit.sc.cln.call.SCMPConnectCall;
import com.stabilit.sc.cln.call.SCMPDisconnectCall;
import com.stabilit.sc.cln.client.ClientFactory;
import com.stabilit.sc.cln.client.IClient;
import com.stabilit.sc.cln.config.ClientConfig;
import com.stabilit.sc.cln.scmp.SCMPClientSession;
import com.stabilit.sc.scmp.SCMPMessage;

/**
 * @author JTraber
 */
public class SCApiExample {

	private String fileName;
	private ClientConfig config = null;
	private IClient client = null;
	private SCMPClientSession scmpSession = null;

	public void runExample() throws Exception {

		config = new ClientConfig();
		config.load(fileName);
		ClientFactory clientFactory = new ClientFactory();
		client = clientFactory.newInstance(config.getClientConfig());
		client.connect(); // physical connect

		// virtual connect
		SCMPConnectCall connectCall = (SCMPConnectCall) SCMPCallFactory.CONNECT_CALL.newInstance(client);
		connectCall.setCompression(false);
		connectCall.setKeepAliveTimeout(30);
		connectCall.setKeepAliveInterval(360);
		connectCall.invoke();

		this.scmpSession = new SCMPClientSession(client, "simulation", "Session Info");
		this.scmpSession.createSession();

		// data call - session is stored inside client!!
		SCMPClnDataCall clnDataCall = (SCMPClnDataCall) SCMPCallFactory.CLN_DATA_CALL.newInstance(client);
		clnDataCall.setMessagInfo("message info");
		clnDataCall.setRequestBody("hello world body!");
		SCMPMessage scmpReply = clnDataCall.invoke();

		// response
		System.out.println(scmpReply.getBody());

		this.scmpSession.deleteSession();

		// virtual disconnect
		SCMPDisconnectCall disconnectCall = (SCMPDisconnectCall) SCMPCallFactory.DISCONNECT_CALL.newInstance(client);
		disconnectCall.invoke();

		client.disconnect(); // physical disconnect
		client.destroy();
	}
}
