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
package org.serviceconnector.cmd.srv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.serviceconnector.cmd.FlyweightCommandFactory;
import org.serviceconnector.cmd.ICommand;

/**
 * A factory for creating UnitServerCommand objects. Unifies all commands used by publish and session server.
 *
 * @author JTraber
 */
public class ServerCommandFactory extends FlyweightCommandFactory {

	/** The Constant LOGGER. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerCommandFactory.class);

	/**
	 * Instantiates a new server command factory.
	 */
	public ServerCommandFactory() {
		ICommand srvCreateSessionCommand = new SrvCreateSessionCommand();
		this.addCommand(srvCreateSessionCommand.getKey(), srvCreateSessionCommand);
		ICommand srvDeleteSessionCommand = new SrvDeleteSessionCommand();
		this.addCommand(srvDeleteSessionCommand.getKey(), srvDeleteSessionCommand);
		ICommand srvExecuteCommand = new SrvExecuteCommand();
		this.addCommand(srvExecuteCommand.getKey(), srvExecuteCommand);
		ICommand srvAbortSessionCommand = new SrvAbortSessionCommand();
		this.addCommand(srvAbortSessionCommand.getKey(), srvAbortSessionCommand);
		ICommand srvAbortSubscriptionCommand = new SrvAbortSubscriptionCommand();
		this.addCommand(srvAbortSubscriptionCommand.getKey(), srvAbortSubscriptionCommand);

		ICommand srvSubscribeCommand = new SrvSubscribeCommand();
		this.addCommand(srvSubscribeCommand.getKey(), srvSubscribeCommand);
		ICommand srvUnsubscribeCommand = new SrvUnsubscribeCommand();
		this.addCommand(srvUnsubscribeCommand.getKey(), srvUnsubscribeCommand);
		ICommand srvChangeSubscriptionCommand = new SrvChangeSubscriptionCommand();
		this.addCommand(srvChangeSubscriptionCommand.getKey(), srvChangeSubscriptionCommand);
	}
}
