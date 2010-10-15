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
package org.serviceconnector.cmd.sc;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.cmd.SCMPCommandException;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.log.SubscriptionLogger;
import org.serviceconnector.net.connection.ConnectionPoolBusyException;
import org.serviceconnector.registry.SubscriptionQueue;
import org.serviceconnector.scmp.HasFaultResponseException;
import org.serviceconnector.scmp.IRequest;
import org.serviceconnector.scmp.IResponse;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.service.Server;
import org.serviceconnector.service.Subscription;
import org.serviceconnector.util.ValidatorUtility;

/**
 * The Class ClnUnsubscribeCommand. Responsible for validation and execution of unsubscribe command. Allows
 * unsubscribing from a publish service.
 */
public class ClnUnsubscribeCommand extends CommandAdapter {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(ClnUnsubscribeCommand.class);
	/** The Constant subscriptionLogger. */
	private final static SubscriptionLogger subscriptionLogger = SubscriptionLogger.getInstance();

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getKey() {
		return SCMPMsgType.CLN_UNSUBSCRIBE;
	}

	/** {@inheritDoc} */
	@Override
	public void run(IRequest request, IResponse response) throws Exception {
		SCMPMessage reqMessage = request.getMessage();
		String subscriptionId = reqMessage.getSessionId();
		this.subscriptionRegistry.getSubscription(subscriptionId);

		// lookup session and checks properness
		Subscription subscription = this.getSubscriptionById(subscriptionId);
		// looks up subscription queue and stops publish mechanism
		SubscriptionQueue<SCMPMessage> subscriptionQueue = this.getSubscriptionQueueById(subscriptionId);
		subscriptionQueue.unsubscribe(subscriptionId);
		String serviceName = reqMessage.getHeader(SCMPHeaderAttributeKey.SERVICE_NAME);
		subscriptionLogger.logUnsubscribe(serviceName, subscriptionId);
		// delete entry from session registry
		this.subscriptionRegistry.removeSubscription(subscription);

		// unsubscribe on backend server
		Server server = subscription.getServer();

		CommandCallback callback;
		int oti = reqMessage.getHeaderInt(SCMPHeaderAttributeKey.OPERATION_TIMEOUT);
		int tries = (int) ((oti * Constants.OPERATION_TIMEOUT_MULTIPLIER) / Constants.WAIT_FOR_CONNECTION_INTERVAL_MILLIS);
		// Following loop implements the wait mechanism in case of a busy connection pool
		int i = 0;
		do {
			callback = new CommandCallback(true);
			try {
				server.unsubscribe(reqMessage, callback, oti - (i * Constants.WAIT_FOR_CONNECTION_INTERVAL_MILLIS));
				// no exception has been thrown - get out of wait loop
				break;
			} catch (ConnectionPoolBusyException ex) {
				if (i >= (tries - 1)) {
					// only one loop outstanding - don't continue throw current exception
					server.removeSession(subscription);
					SCMPCommandException scmpCommandException = new SCMPCommandException(SCMPError.SC_ERROR,
							"no free connection on server for service " + reqMessage.getServiceName());
					scmpCommandException.setMessageType(this.getKey());
					throw scmpCommandException;
				}
			} catch (Exception ex) {
				server.removeSession(subscription);
				throw ex;
			}
			// sleep for a while and then try again
			Thread.sleep(Constants.WAIT_FOR_CONNECTION_INTERVAL_MILLIS);
		} while (++i < tries);

		SCMPMessage reply = callback.getMessageSync();
		// no specific error handling in case of fault - everything is done anyway
		server.removeSession(subscription);
		reply.removeHeader(SCMPHeaderAttributeKey.SESSION_ID);
		reply.setIsReply(true);
		reply.setMessageType(this.getKey());
		response.setSCMP(reply);
	}

	/** {@inheritDoc} */
	@Override
	public void validate(IRequest request) throws Exception {
		SCMPMessage message = request.getMessage();
		try {
			// messageId
			String messageId = (String) message.getHeader(SCMPHeaderAttributeKey.MESSAGE_ID);
			if (messageId == null || messageId.equals("")) {
				throw new SCMPValidatorException(SCMPError.HV_WRONG_MESSAGE_ID, "messageId must be set");
			}
			// serviceName
			String serviceName = message.getServiceName();
			if (serviceName == null || serviceName.equals("")) {
				throw new SCMPValidatorException(SCMPError.HV_WRONG_SERVICE_NAME, "serviceName must be set");
			}
			// operation timeout
			String otiValue = message.getHeader(SCMPHeaderAttributeKey.OPERATION_TIMEOUT.getValue());
			ValidatorUtility.validateInt(10, otiValue, 3600000, SCMPError.HV_WRONG_OPERATION_TIMEOUT);
			// sessionId
			String sessionId = message.getSessionId();
			if (sessionId == null || sessionId.equals("")) {
				throw new SCMPValidatorException(SCMPError.HV_WRONG_SESSION_ID, "sessionId must be set");
			}
		} catch (HasFaultResponseException ex) {
			// needs to set message type at this point
			ex.setMessageType(getKey());
			throw ex;
		} catch (Throwable ex) {
			logger.error("validate", ex);
			SCMPValidatorException validatorException = new SCMPValidatorException();
			validatorException.setMessageType(getKey());
			throw validatorException;
		}
	}
}