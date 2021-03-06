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
package org.serviceconnector.srv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.SCServiceException;
import org.serviceconnector.api.srv.SCSessionServer;
import org.serviceconnector.api.srv.SCSessionServerCallback;

public class DemoLargeSessionServer extends DemoSessionServer {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DemoLargeSessionServer.class);

	/**
	 * Main method if you like to start in debug mode.
	 */
	public static void main(String[] args) throws Exception {
		DemoSessionServer sessionServer = new DemoLargeSessionServer();
		sessionServer.run();
	}

	@Override
	public SCSessionServerCallback newSrvCallback(SCSessionServer server) {
		SCSessionServerCallback cbk = new SrvLargeCallback(server);
		return cbk;
	}

	class SrvLargeCallback extends SCSessionServerCallback {

		public SrvLargeCallback(SCSessionServer server) {
			super(server);
		}

		@Override
		public SCMessage createSession(SCMessage request, int operationTimeoutMillis) {
			LOGGER.info("Session created");
			return request;
		}

		@Override
		public void deleteSession(SCMessage request, int operationTimeoutMillis) {
			LOGGER.info("Session deleted");
		}

		@Override
		public void abortSession(SCMessage request, int operationTimeoutMillis) {
			LOGGER.info("Session aborted");
		}

		@Override
		public SCMessage execute(SCMessage request, int operationTimeoutMillis) {
			// we return a large message
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 10000; i++) {
				sb.append("this is a large message\r\n");
			}
			Object data = sb.toString();
			request.setCompressed(false);
			request.setData(data);
			// watch out for kill server message
			if (data.getClass() == String.class) {
				String dataString = (String) data;
				if (dataString.equals("kill server")) {
					KillThread kill = new KillThread(this.scSessionServer);
					kill.start();
				}
			}
			return request;
		}

		@Override
		public void exceptionCaught(SCServiceException ex) {
			LOGGER.error("exception caught");
		}
	}
}
