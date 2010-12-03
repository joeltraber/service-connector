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
package org.serviceconnector.cln;

import org.apache.log4j.Logger;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.SCMessageCallback;
import org.serviceconnector.api.SCService;
import org.serviceconnector.api.SCSubscribeMessage;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCPublishService;
import org.serviceconnector.api.cln.SCSessionService;
import org.serviceconnector.net.ConnectionType;

public class TestPublishClientFilippe extends Thread {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(TestPublishClientFilippe.class);

	private String methodName;

	public static void main(String[] args) {
		try {
			TestPublishClientFilippe client = new TestPublishClientFilippe(args[0]);
			client.start();
		} catch (Exception e) {
			logger.error("incorrect parameters", e);
		}
	}

	public TestPublishClientFilippe(String methodName) {
		this.methodName = methodName;
	}

	@Override
	public void run() {
		SCClient sc = new SCClient(TestConstants.HOST, TestConstants.PORT_TCP, ConnectionType.NETTY_TCP);

		try {
			sc.attach();

			SCSubscribeMessage subscibeMessage = new SCSubscribeMessage();
			subscibeMessage.setMask(TestConstants.mask);
			subscibeMessage.setSessionInfo("sessionInfo");

			if (getMethodName() == "subscribe_serviceNameValidMaskSameAsInServer_isSubscribedSessionIdExists") {
				SCPublishService service = sc.newPublishService(TestConstants.publishServiceNames);
				service.subscribe(subscibeMessage, new DemoPublishClientCallback(service));
				service.unsubscribe();

			} else if (getMethodName() == "subscribe_timeoutMaxAllowed_isSubscribedSessionIdExists") {
				SCPublishService service = sc.newPublishService(TestConstants.publishServiceNames);
				service.subscribe(3600, subscibeMessage, new DemoPublishClientCallback(service));
				service.unsubscribe();

			} else if (getMethodName() == "changeSubscription_toMaskWhiteSpace_passes") {
				SCPublishService service = sc.newPublishService(TestConstants.publishServiceNames);
				service.subscribe(subscibeMessage, new DemoPublishClientCallback(service));
				subscibeMessage.setMask(" ");
				service.changeSubscription(subscibeMessage);
				service.unsubscribe();

			} else if (getMethodName() == "subscribeUnsubscribe_twice_isSubscribedThenNot") {
				SCPublishService service = sc.newPublishService(TestConstants.publishServiceNames);
				service.subscribe(subscibeMessage, new DemoPublishClientCallback(service));
				service.unsubscribe();
				service.subscribe(subscibeMessage, new DemoPublishClientCallback(service));
				service.unsubscribe();

			} else if (getMethodName() == "changeSubscription_twice_passes") {
				SCPublishService service = sc.newPublishService(TestConstants.publishServiceNames);
				service.subscribe(subscibeMessage, new DemoPublishClientCallback(service));

				service.changeSubscription(subscibeMessage);
				service.changeSubscription(subscibeMessage);

				service.unsubscribe();

			} else if (getMethodName() == "unsubscribe_serviceNameValid_notSubscribedEmptySessionId") {
				SCPublishService service = sc.newPublishService(TestConstants.publishServiceNames);
				service.unsubscribe();

			} else if (getMethodName() == "createSession_rejectTheSessionThenCreateValidSessionThenExecuteAMessage_passes") {
				SCSessionService sessionService = sc.newSessionService(TestConstants.sessionServiceNames);

				try {
					SCMessage scMessage = new SCMessage("reject");
					scMessage.setSessionInfo("sessionInfo");
					sessionService.createSession(10, scMessage);
				} catch (Exception e) {
				}
				SCMessage scMessage = new SCMessage();
				scMessage.setSessionInfo("sessionInfo");
				sessionService.createSession(10, scMessage);

				sessionService.execute(new SCMessage());
				sessionService.deleteSession();

			} else if (getMethodName() == "execute_messageData1MBArray_returnsTheSameMessageData") {
				SCSessionService sessionService = sc.newSessionService(TestConstants.sessionServiceNames);
				SCMessage scMessage = new SCMessage();
				scMessage.setSessionInfo("sessionInfo");
				sessionService.createSession(10, scMessage);

				SCMessage message = new SCMessage(new byte[TestConstants.dataLength1MB]);
				message.setCompressed(false);

				sessionService.execute(message);
				sessionService.deleteSession();

			}

		} catch (Exception e) {
			logger.error("run", e);
		} finally {
			try {
				sc.detach();
			} catch (Exception e) {
				logger.error("run", e);
			}
		}
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getMethodName() {
		return methodName;
	}

	private class DemoPublishClientCallback extends SCMessageCallback {

		public DemoPublishClientCallback(SCService service) {
			super(service);
		}

		@Override
		public void receive(SCMessage reply) {
			logger.info("Publish client received: " + reply.getData());
		}

		@Override
		public void receive(Exception e) {
		}
	}
}
