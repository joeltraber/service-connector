/*
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
 */
package org.serviceconnector.srv;

import org.serviceconnetor.TestConstants;

public class TestServer {

	public static void main(String[] args) {
		String[] arguments = new String[args.length - 1];
		System.arraycopy(args, 1, arguments, 0, args.length - 1);
		
		if (args[0].equals(TestConstants.sessionSrv)) {
			TestSessionServer sessionServer = new TestSessionServer();
			sessionServer.runSessionServer(arguments);
		
		} else if (args[0].equals(TestConstants.publishSrv)) {
			TestPublishServer publishServer = new TestPublishServer();
			publishServer.runPublishServer(arguments);
		}
	}
}
