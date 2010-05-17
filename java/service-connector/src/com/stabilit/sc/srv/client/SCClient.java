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
package com.stabilit.sc.srv.client;

import com.stabilit.sc.cln.client.Client;
import com.stabilit.sc.factory.IFactoryable;
import com.stabilit.sc.listener.PerformancePoint;
import com.stabilit.sc.scmp.SCMPMessage;

/**
 * The Class SCClient. Defines behavior of client in the context of Service Connector.
 * 
 * @author JTraber
 */
public class SCClient extends Client {

	/**
	 * Instantiates a new SCClient.
	 */
	public SCClient() {
	}

	/**
	 * New instance.
	 * 
	 * @return the factoryable
	 */
	@Override
	public IFactoryable newInstance() {
		return new SCClient();
	}

	/**
	 * Send and receive.
	 * 
	 * @param scmp
	 *            the scmp
	 * @return the sCMP
	 * @throws Exception
	 *             the exception
	 */
	@Override
	public SCMPMessage sendAndReceive(SCMPMessage scmp) throws Exception {
		if (PerformancePoint.getInstance().isOn()) {
			try {
				PerformancePoint.getInstance().fireBegin(this, "sendAndReceive");
				return clientConnection.sendAndReceive(scmp);
			} finally {
				PerformancePoint.getInstance().fireEnd(this, "sendAndReceive");
			}
		}
		return clientConnection.sendAndReceive(scmp);
	}
}
