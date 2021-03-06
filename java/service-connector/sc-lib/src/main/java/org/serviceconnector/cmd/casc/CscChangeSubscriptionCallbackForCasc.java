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
package org.serviceconnector.cmd.casc;

import org.serviceconnector.log.SubscriptionLogger;
import org.serviceconnector.net.req.IRequest;
import org.serviceconnector.net.res.IResponderCallback;
import org.serviceconnector.net.res.IResponse;
import org.serviceconnector.registry.PublishMessageQueue;
import org.serviceconnector.scmp.ISubscriptionCallback;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.service.IPublishService;
import org.serviceconnector.service.Subscription;
import org.serviceconnector.service.SubscriptionMask;

/**
 * The Class CscChangeSubscriptionCallbackForCasc.
 */
public class CscChangeSubscriptionCallbackForCasc extends CommandCascCallback implements ISubscriptionCallback {

	/** The subscription. */
	private Subscription cascSCSubscription;
	/** The casc sc mask string. */
	private String cascSCMaskString;

	/**
	 * Instantiates a new csc change subscription callback for casc.
	 *
	 * @param request the request
	 * @param response the response
	 * @param callback the callback
	 * @param cascSCSubscription the casc sc subscription
	 * @param cascSCMaksString the casc sc maks string
	 */
	public CscChangeSubscriptionCallbackForCasc(IRequest request, IResponse response, IResponderCallback callback, Subscription cascSCSubscription, String cascSCMaksString) {
		super(request, response, callback);
		this.cascSCSubscription = cascSCSubscription;
		this.cascSCMaskString = cascSCMaksString;
	}

	/**
	 * Receive.
	 *
	 * @param reply the reply {@inheritDoc}
	 */
	@Override
	public void receive(SCMPMessage reply) {
		SCMPMessage reqMessage = request.getMessage();
		String serviceName = reqMessage.getServiceName();

		if (reply.isFault() == false && reply.getHeaderFlag(SCMPHeaderAttributeKey.REJECT_SESSION) == false) {
			if (cascSCSubscription.isCascaded() == true) {
				// update csc subscription id list for cascaded subscription
				cascSCSubscription.addCscSubscriptionId(reqMessage.getSessionId(), new SubscriptionMask(reqMessage.getHeader(SCMPHeaderAttributeKey.MASK)));
			}
			// change subscription for cascaded SC
			PublishMessageQueue<SCMPMessage> queue = ((IPublishService) cascSCSubscription.getService()).getMessageQueue();
			SubscriptionMask cascSCMask = new SubscriptionMask(cascSCMaskString);
			queue.changeSubscription(this.cascSCSubscription.getId(), cascSCMask);
			cascSCSubscription.setMask(cascSCMask);
			SubscriptionLogger.logChangeSubscribe(serviceName, this.cascSCSubscription.getId(), cascSCMaskString);
		}
		// forward reply to client
		reply.setIsReply(true);
		reply.setServiceName(serviceName);
		reply.setMessageType(this.msgType);
		response.setSCMP(reply);
		this.responderCallback.responseCallback(request, response);
	}

	/**
	 * Gets the subscription.
	 *
	 * @return the subscription {@inheritDoc}
	 */
	@Override
	public Subscription getSubscription() {
		return this.cascSCSubscription;
	}

	/** {@inheritDoc} */
	@Override
	public IRequest getRequest() {
		return this.request;
	}
}
