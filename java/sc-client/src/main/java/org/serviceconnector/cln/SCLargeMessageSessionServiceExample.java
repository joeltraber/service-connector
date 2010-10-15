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
package org.serviceconnector.cln;

import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCSessionService;


public class SCLargeMessageSessionServiceExample {

	public static void main(String[] args) {
		SCLargeMessageSessionServiceExample.runExample();
	}

	public static void runExample() {
		SCClient sc = null;
		try {
			sc = new SCClient();
			sc.setMaxConnections(100);

			// connects to SC, checks connection to SC
			sc.attach("localhost", 7000);

			SCSessionService sessionServiceA = sc.newSessionService("simulation");
			// creates a session
			sessionServiceA.createSession("sessionInfo", 300, 60);

			SCMessage requestMsg = new SCMessage();
			// set up large buffer
			byte[] buffer = new byte[100000];
			for (int i = 0; i < buffer.length; i++) {
				buffer[i] = (byte) i;
			}
			requestMsg.setData(buffer);
			requestMsg.setCompressed(false);
			SCMessage responseMsg = sessionServiceA.execute(requestMsg);

			System.out.println(responseMsg.getData().toString());
			// deletes the session
			sessionServiceA.deleteSession();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// disconnects from SC
				sc.detach();
			} catch (Exception e) {
				sc = null;
			}
		}
	}
}