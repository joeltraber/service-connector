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
package org.serviceconnector.test.system.publish;

import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.SCMessageCallback;
import org.serviceconnector.api.SCService;
import org.serviceconnector.api.SCSubscribeMessage;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCPublishService;
import org.serviceconnector.ctrl.util.ProcessCtx;
import org.serviceconnector.ctrl.util.ProcessesController;
import org.serviceconnector.log.Loggers;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.service.SCServiceException;

public class ReceivePublicationTest {

	/** The Constant testLogger. */
	protected static final Logger testLogger = Logger.getLogger(Loggers.TEST.getValue());
	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(ReceivePublicationTest.class);

	private static boolean messageReceived = false;
	private static ProcessesController ctrl;
	private ProcessCtx scCtx;
	private ProcessCtx srvCtx;
	private SCClient client;
	private SCPublishService service;
	private int threadCount = 0;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		ctrl = new ProcessesController();
	}

	@Before
	public void beforeOneTest() throws Exception {
		threadCount = Thread.activeCount();
		scCtx = ctrl.startSC(TestConstants.log4jSCProperties, TestConstants.SCProperties);
		srvCtx = ctrl.startServer(TestConstants.SERVER_TYPE_PUBLISH, TestConstants.log4jSrvProperties,
				TestConstants.pubServerName1, TestConstants.PORT_LISTENER, TestConstants.PORT_TCP, 100, 10,
				TestConstants.pubServiceName1);
		client = new SCClient(TestConstants.HOST, TestConstants.PORT_TCP, ConnectionType.NETTY_TCP);
		client.attach();
	}

	@After
	public void afterOneTest() throws Exception {
		try {
			service.unsubscribe();
		} catch (Exception e1) {
		}
		service = null;
		try {
			client.detach();
		} catch (Exception e) {
		}
		client = null;
		try {
			ctrl.stopServer(srvCtx);
		} catch (Exception e) {
		}
		srvCtx = null;
		try {
			ctrl.stopSC(scCtx);
		} catch (Exception e) {
		}
		scCtx = null;
		testLogger.info("Number of threads :" + Thread.activeCount() + " created :" + (Thread.activeCount() - threadCount));
	}

	@AfterClass
	public static void afterAllTests() throws Exception {
		ctrl = null;
	}

	/**
	 * Description: receive one message (regular)<br>
	 * Expectation: passes
	 */
	@Test
	public void t01_receive() throws Exception {
		service = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage();
		SCSubscribeMessage subMsgResponse = null;
		MsgCallback cbk = new MsgCallback(service);
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo("publishMessages");
		int nrMessages = 1;
		subMsgRequest.setData(Integer.toString(nrMessages));
		cbk.expectedMessages = nrMessages;
		subMsgResponse = service.subscribe(subMsgRequest, cbk);
		Assert.assertNotNull("the session ID is null", service.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());
		Assert.assertTrue("is not subscribed", service.isSubscribed());
		
		waitForMessage(10);
		Assert.assertEquals("Nr messages does not match", nrMessages, cbk.messageCounter);
		SCMessage response = cbk.response;
		Assert.assertEquals("message body is empty", true, response.getDataLength() > 0);
		
		service.unsubscribe();
		Assert.assertNull("the session ID is not null", service.getSessionId());
	}

	/**
	 * Description: receive 1000 messages<br>
	 * Expectation: passes
	 */
	@Test
	public void t02_receive() throws Exception {
		service = client.newPublishService(TestConstants.pubServiceName1);
		SCSubscribeMessage subMsgRequest = new SCSubscribeMessage();
		SCSubscribeMessage subMsgResponse = null;
		MsgCallback cbk = new MsgCallback(service);
		subMsgRequest.setMask(TestConstants.mask);
		subMsgRequest.setSessionInfo("publishMessages");
		int nrMessages = 1000;
		subMsgRequest.setData(Integer.toString(nrMessages));
		cbk.expectedMessages = nrMessages;
		subMsgResponse = service.subscribe(subMsgRequest, cbk);
		Assert.assertNotNull("the session ID is null", service.getSessionId());
		Assert.assertEquals("message body is not the same length", subMsgRequest.getDataLength(), subMsgResponse.getDataLength());
		Assert.assertEquals("compression is not the same", subMsgRequest.isCompressed(), subMsgResponse.isCompressed());
		Assert.assertTrue("is not subscribed", service.isSubscribed());

		waitForMessage(10);
		Assert.assertEquals("Nr messages does not match", nrMessages, cbk.messageCounter);
		SCMessage response = cbk.response;
		Assert.assertEquals("message body is empty", true, response.getDataLength() > 0);
		
		service.unsubscribe();
		Assert.assertNull("the session ID is not null", service.getSessionId());
	}

	
	
//	@Test
//	public void publish_waitForAMessageToBePublished_incomesAMessage() throws Exception {
//		SCPublishService service = client.newPublishService(TestConstants.pubServiceName1);
//		DemoPublishClientCallback callback = new DemoPublishClientCallback(service);
//		SCSubscribeMessage subscibeMessage = new SCSubscribeMessage();
//		subscibeMessage.setMask(TestConstants.mask);
//		subscibeMessage.setSessionInfo("sessionInfo");
//		service.subscribe(subscibeMessage, callback);
//		for (int i = 0; i < 30; i++) {
//			if (callback.lastMessage == null) {
//				Thread.sleep(100);
//			} else {
//				i = 30;
//			}
//		}
//
//		service.unsubscribe();
//
//		Assert.assertEquals(1, callback.getMessageCounter());
//		Assert.assertEquals(true, callback.getLastMessage().getData().toString().startsWith("publish message nr "));
//		Assert.assertEquals(null, callback.getLastMessage().getMessageInfo());
//		Assert.assertEquals(false, callback.getLastMessage().getSessionId() == null
//				|| callback.getLastMessage().getSessionId().equals(""));
//	}
//
//	@Test
//	public void publish_waitFor2MessagesToBePublished_bodyEndsWithConsequentNumbers() throws Exception {
//		SCPublishService service = client.newPublishService(TestConstants.pubServiceName1);
//		DemoPublishClientCallback callback = new DemoPublishClientCallback(service);
//		SCSubscribeMessage subscibeMessage = new SCSubscribeMessage();
//		subscibeMessage.setMask(TestConstants.mask);
//		subscibeMessage.setSessionInfo("sessionInfo");
//		service.subscribe(subscibeMessage, callback);
//
//		SCMessage firstMessage = null;
//
//		for (int i = 0; i < 60; i++) {
//			if (firstMessage == null && callback.getLastMessage() == null) {
//				Thread.sleep(100);
//			} else if (firstMessage == null) {
//				firstMessage = callback.getLastMessage();
//				callback.setLastMessage(null);
//				Thread.sleep(100);
//			} else if (callback.getLastMessage() == null) {
//				Thread.sleep(100);
//			} else {
//				i = 60;
//			}
//		}
//
//		service.unsubscribe();
//
//		Assert.assertEquals(2, callback.getMessageCounter());
//		Assert.assertEquals(true, firstMessage.getData().toString().startsWith("publish message nr "));
//		Assert.assertEquals(true, callback.getLastMessage().getData().toString().startsWith("publish message nr "));
//		Assert.assertEquals(Integer.parseInt(firstMessage.getData().toString().split(" ")[3]) + 1, Integer.parseInt(callback
//				.getLastMessage().getData().toString().split(" ")[3]));
//		Assert.assertEquals(null, firstMessage.getMessageInfo());
//		Assert.assertEquals(null, callback.getLastMessage().getMessageInfo());
//		Assert.assertEquals(false, firstMessage.getSessionId() == null || callback.getLastMessage().getSessionId().equals(""));
//		Assert.assertEquals(false, firstMessage.getSessionId() == null || callback.getLastMessage().getSessionId().equals(""));
//	}
//
//	@Test
//	public void publish_waitFor20MessagesToBePublished_bodysEndWithConsequentNumbers() throws Exception {
//		SCPublishService service = client.newPublishService(TestConstants.pubServiceName1);
//		DemoPublishClientCallback callback = new DemoPublishClientCallback(service);
//		SCSubscribeMessage subscibeMessage = new SCSubscribeMessage();
//		subscibeMessage.setMask(TestConstants.mask);
//		subscibeMessage.setSessionInfo("sessionInfo");
//		service.subscribe(subscibeMessage, callback);
//
//		SCMessage previousMessage = null;
//		SCMessage newMessage = null;
//		int counter = 0;
//
//		for (int i = 0; i < 600 && counter < 20; i++) {
//			if ((i % 10) == 0)
//				testLogger.info("wait for message cycle:\t" + i + " ...");
//			if (counter == callback.getMessageCounter()) {
//				Thread.sleep(100);
//			} else if (counter < callback.getMessageCounter()) {
//				previousMessage = newMessage;
//				newMessage = callback.getLastMessage();
//				counter++;
//				if (counter > 1) {
//					Assert.assertEquals(Integer.parseInt(previousMessage.getData().toString().split(" ")[3]) + 1, Integer
//							.parseInt(newMessage.getData().toString().split(" ")[3]));
//				}
//			}
//		}
//		Assert.assertEquals("recieved messages", 20, counter);
//	}

	private void waitForMessage(int nrSeconds) throws Exception {
		for (int i = 0; i < (nrSeconds * 10); i++) {
			if (messageReceived) {
				return;
			}
			Thread.sleep(100);
		}
		throw new TimeoutException("No message received within " + nrSeconds + " seconds timeout.");
	}
	
	private class MsgCallback extends SCMessageCallback {
		
		private SCMessage response = null;
		private int messageCounter = 0;
		private int expectedMessages = 0;

		public MsgCallback(SCService service) {
			super(service);
			ReceivePublicationTest.messageReceived = false;
			response = null;
			messageCounter = 0;
			expectedMessages = 0;
		}

		@Override
		public void receive(SCMessage msg) {
			response = msg;
			messageCounter++;
			if ( expectedMessages == messageCounter) {
				ReceivePublicationTest.messageReceived = true;
			}
			if (((messageCounter+1) % 100) == 0) {
				ReceivePublicationTest.testLogger.info("Receiving message nr. " + (messageCounter+1));
			}


		}

		@Override
		public void receive(Exception e) {
			logger.error("receive error: " + e.getMessage());
			if (e instanceof SCServiceException) {
				SCMPError scError = ((SCServiceException) e).getSCMPError();
				logger.info("SC error received code:" + scError.getErrorCode() + " text:" + scError.getErrorText());
			}
			response = null;
			ReceivePublicationTest.messageReceived = true;
		}
	}
}