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
package org.serviceconnector.test.system.api.session;

import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.TestMessageCallback;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.SCMessageCallback;
import org.serviceconnector.api.SCService;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCSessionService;
import org.serviceconnector.ctrl.util.ProcessCtx;
import org.serviceconnector.ctrl.util.ProcessesController;
import org.serviceconnector.log.Loggers;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.service.SCServiceException;

@SuppressWarnings("unused")
public class APIExecuteAsynchronousTest {

	/** The Constant testLogger. */
	protected static final Logger testLogger = Logger.getLogger(Loggers.TEST.getValue());

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(APIExecuteAsynchronousTest.class);

	private static boolean messageReceived = false;
	private static ProcessesController ctrl;
	private ProcessCtx scCtx;
	private ProcessCtx srvCtx;
	private SCClient client;
	private SCSessionService service;
	private int threadCount = 0;
	private TestMessageCallback cbk = null;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		ctrl = new ProcessesController();
	}

	@Before
	public void beforeOneTest() throws Exception {
		threadCount = Thread.activeCount();
		scCtx = ctrl.startSC(TestConstants.log4jSCProperties, TestConstants.SCProperties);
		srvCtx = ctrl.startServer(TestConstants.SERVER_TYPE_SESSION, TestConstants.log4jSrvProperties,
				TestConstants.sesServerName1, TestConstants.PORT_LISTENER, TestConstants.PORT_TCP, 100, 10,
				TestConstants.sesServiceName1);
		client = new SCClient(TestConstants.HOST, TestConstants.PORT_TCP, ConnectionType.NETTY_TCP);
		client.attach();
		messageReceived = false;
	}

	@After
	public void afterOneTest() throws Exception {
		try {
			service.deleteSession();
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
		testLogger.info("Number of threads :" + Thread.activeCount() + " created :"
				+ (Thread.activeCount() - threadCount));
	}

	@AfterClass
	public static void afterAllTests() throws Exception {
		ctrl = null;
	}

	/**
	 * Description: exchange of 1 uncompressed message<br>
	 * Expectation: passes
	 */
	@Test
	public void t01_send() throws Exception {
		SCMessage request = new SCMessage(TestConstants.pangram);
		request.setCompressed(false);
		SCMessage response = null;
		service = client.newSessionService(TestConstants.sesServiceName1);
		this.cbk = new TestMessageCallback(service);
		response = service.createSession(request, cbk);
		request.setMessageInfo(TestConstants.echoAppErrorCmd);
		messageReceived = false;
		MsgCallback cbk = new MsgCallback(service);
		service.send(request);
		waitForMessage(10);
		response = cbk.response;
		Assert.assertEquals("message body is not the same length", request.getDataLength(), response.getDataLength());
		Assert.assertEquals("messageInfo is not the same", request.getMessageInfo(), response.getMessageInfo());
		Assert.assertEquals("compression is not the same", request.isCompressed(), response.isCompressed());
		Assert.assertEquals("appErrorCode is not set", TestConstants.appErrorCode, response.getAppErrorCode());
		Assert.assertEquals("appErrorText is not set", TestConstants.appErrorText, response.getAppErrorText());
		service.deleteSession();
	}

	/**
	 * Description: exchange of 1 compressed message<br>
	 * Expectation: passes
	 */
	@Test
	public void t02_sendCompressed() throws Exception {
		SCMessage request = new SCMessage(TestConstants.pangram);
		request.setCompressed(true);
		SCMessage response = null;
		service = client.newSessionService(TestConstants.sesServiceName1);
		this.cbk = new TestMessageCallback(service);
		response = service.createSession(request, cbk);
		request.setMessageInfo(TestConstants.echoCmd);
		messageReceived = false;
		MsgCallback cbk = new MsgCallback(service);
		service.send(request);
		waitForMessage(10);
		response = cbk.response;

		Assert.assertEquals("message body is not the same length", request.getDataLength(), response.getDataLength());
		Assert.assertEquals("messageInfo is not the same", request.getMessageInfo(), response.getMessageInfo());
		Assert.assertEquals("compression is not the same", request.isCompressed(), response.isCompressed());
		service.deleteSession();
	}

	/**
	 * Description: exchange of 1 uncompressed 10MB message<br>
	 * Expectation: passes
	 */
	@Test
	public void t03_sendLarge() throws Exception {
		SCMessage request = new SCMessage(new byte[TestConstants.dataLength1MB]);
		request.setCompressed(false);
		SCMessage response = null;
		service = client.newSessionService(TestConstants.sesServiceName1);
		this.cbk = new TestMessageCallback(service);
		response = service.createSession(new SCMessage(), cbk);
		request.setMessageInfo(TestConstants.echoCmd);
		messageReceived = false;
		MsgCallback cbk = new MsgCallback(service);
		service.send(request);
		waitForMessage(10);
		response = cbk.response;

		Assert.assertEquals("message body is not the same length", request.getDataLength(), response.getDataLength());
		Assert.assertEquals("messageInfo is not the same", request.getMessageInfo(), response.getMessageInfo());
		Assert.assertEquals("compression is not the same", request.isCompressed(), response.isCompressed());
		service.deleteSession();
	}

	/**
	 * Description: exchange of 1 compressed 10MB message<br>
	 * Expectation: passes
	 */
	@Test
	public void t04_sendLargeCompressed() throws Exception { // TODO JOT this test does not work
		SCMessage request = new SCMessage(new byte[TestConstants.dataLength1MB]);
		request.setCompressed(true);
		SCMessage response = null;
		service = client.newSessionService(TestConstants.sesServiceName1);
		this.cbk = new TestMessageCallback(service);
		response = service.createSession(new SCMessage(), cbk);
		request.setMessageInfo(TestConstants.echoCmd);
		messageReceived = false;
		MsgCallback cbk = new MsgCallback(service);
		service.send(request);
		waitForMessage(10);
		response = cbk.response;
		Assert.assertEquals("message body is not the same length", request.getDataLength(), response.getDataLength());
		Assert.assertEquals("messageInfo is not the same", request.getMessageInfo(), response.getMessageInfo());
		Assert.assertEquals("compression is not the same", request.isCompressed(), response.isCompressed());
		service.deleteSession();
	}

	/**
	 * Description: send message before create session<br>
	 * Expectation: throws SCServiceException
	 */
	@Test(expected = SCServiceException.class)
	public void t05_send() throws Exception {
		SCMessage request = new SCMessage(new byte[128]);
		request.setCompressed(false);
		SCMessage response = null;
		service = client.newSessionService(TestConstants.sesServiceName1);
		messageReceived = false;
		MsgCallback cbk = new MsgCallback(service);
		service.send(request);
	}

	/**
	 * Description: send message after session rejection<br>
	 * Expectation: throws SCserviceException
	 */
	@Test(expected = SCServiceException.class)
	public void t06_rejectSession() throws Exception {
		SCMessage request = new SCMessage(TestConstants.pangram);
		request.setCompressed(false);
		SCMessage response = null;
		service = client.newSessionService(TestConstants.sesServiceName1);
		request.setSessionInfo(TestConstants.rejectSessionCmd);
		try {
			this.cbk = new TestMessageCallback(service);
			response = service.createSession(request, cbk);
		} catch (Exception e) {
			// ignore rejection
		}
		request.setMessageInfo(TestConstants.echoCmd);
		messageReceived = false;
		MsgCallback cbk = new MsgCallback(service);
		service.send(request);
	}

	/**
	 * Description: screw up sessionId before message send<br>
	 * Expectation: passes because sessionId is set internally again.
	 */
	@Test
	public void t07_sessionId() throws Exception {
		SCMessage request = new SCMessage(TestConstants.pangram);
		request.setCompressed(false);
		SCMessage response = null;
		service = client.newSessionService(TestConstants.sesServiceName1);
		this.cbk = new TestMessageCallback(service);
		response = service.createSession(request, cbk);
		String sessionId = service.getSessionId();
		request.setMessageInfo(TestConstants.echoCmd);
		request.setSessionId("aaaa0000-bb11-cc22-dd33-eeeeee444444");
		messageReceived = false;
		MsgCallback cbk = new MsgCallback(service);
		service.send(request);
		waitForMessage(10);
		response = cbk.response;
		Assert.assertEquals("sessionId is not the same", sessionId, response.getSessionId());
		Assert.assertEquals("message body is not the same length", request.getDataLength(), response.getDataLength());
		Assert.assertEquals("messageInfo is not the same", request.getMessageInfo(), response.getMessageInfo());
		Assert.assertEquals("compression is not the same", request.isCompressed(), response.isCompressed());
		service.deleteSession();
	}

	/**
	 * Description: operation timeout expired during execution<br>
	 * Expectation: passes, gets back a fault response
	 */
	@Test
	public void t10_operationTimeout() throws Exception {
		SCMessage request = new SCMessage("hallo");
		request.setCompressed(false);
		SCMessage response = null;
		service = client.newSessionService(TestConstants.sesServiceName1);
		this.cbk = new TestMessageCallback(service);
		response = service.createSession(request, cbk);
		request.setMessageInfo(TestConstants.sleepCmd);
		request.setData("5000"); // server will sleep 5000ms
		messageReceived = false;
		MsgCallback cbk = new MsgCallback(service);
		service.send(3, request); // SC oti = 3*0.8*1000 = 2400ms
		waitForMessage(10);
		response = cbk.response;

	}

	/**
	 * Description: operation timeout expired during execution, catch exception and continue after a while<br>
	 * Expectation: passes
	 */
	@Test
	public void t11_operationTimeout() throws Exception {
		SCMessage request = new SCMessage("hallo");
		request.setCompressed(false);
		SCMessage response = null;
		service = client.newSessionService(TestConstants.sesServiceName1);
		this.cbk = new TestMessageCallback(service);
		response = service.createSession(request, cbk);
		request.setMessageInfo(TestConstants.sleepCmd);
		request.setData("5000"); // server will sleep 5000ms
		messageReceived = false;
		MsgCallback cbk = new MsgCallback(service);
		service.send(2, request); // SC oti = 2*0.8*1000 = 1600ms
		waitForMessage(10); // will wait max 10 seconds for response
		response = cbk.response;
		Thread.sleep(5000); // wait for the server

		// second message
		messageReceived = false;
		request.setMessageInfo(TestConstants.echoCmd);
		request.setData("hallo"); // send second message
		service.send(2, request);
		waitForMessage(10); // will wait max 10 seconds for the second response
		response = cbk.response;
		Assert.assertEquals("message body is not the same length", request.getDataLength(), response.getDataLength());
		Assert.assertEquals("messageInfo is not the same", request.getMessageInfo(), response.getMessageInfo());
		Assert.assertEquals("compression is not the same", request.isCompressed(), response.isCompressed());
		service.deleteSession();
	}

	/**
	 * Description: operation timeout expired during execution, catch exception and continue immediately<br>
	 * Expectation: passes
	 */
	@Test
	public void t12_operationTimeout() throws Exception {
		SCMessage request = new SCMessage("hallo");
		request.setCompressed(false);
		SCMessage response = null;
		service = client.newSessionService(TestConstants.sesServiceName1);
		this.cbk = new TestMessageCallback(service);
		response = service.createSession(request, cbk);
		request.setMessageInfo(TestConstants.sleepCmd);
		request.setData("5000"); // server will sleep 5000ms
		messageReceived = false;
		MsgCallback cbk = new MsgCallback(service);
		service.send(2, request); // SC oti = 2*0.8*1000 = 1600ms
		waitForMessage(10); // will wait max 10 seconds for response
		response = cbk.response;

		// second message
		messageReceived = false;
		request.setMessageInfo(TestConstants.echoCmd);
		request.setData("gaga");
		service.send(2, request);
		waitForMessage(10); // will wait max 10 seconds for response
		response = cbk.response;
		Assert.assertEquals("message body is not the same length", request.getDataLength(), response.getDataLength());
		Assert.assertEquals("messageInfo is not the same", request.getMessageInfo(), response.getMessageInfo());
		Assert.assertEquals("compression is not the same", request.isCompressed(), response.isCompressed());

		// third message (synchronous)
		request.setData("abraka-dabra");
		response = service.execute(request);
		Assert.assertEquals("message body is not the same length", request.getDataLength(), response.getDataLength());
		Assert.assertEquals("messageInfo is not the same", request.getMessageInfo(), response.getMessageInfo());
		Assert.assertEquals("compression is not the same", request.isCompressed(), response.isCompressed());
		service.deleteSession();
	}

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

		public MsgCallback(SCService service) {
			super(service);
		}

		@Override
		public void receive(SCMessage msg) {
			response = msg;
			APIExecuteAsynchronousTest.messageReceived = true;
		}

		@Override
		public void receive(Exception e) {
			logger.error("receive error: " + e.getMessage());
			if (e instanceof SCServiceException) {
				SCMPError scError = ((SCServiceException) e).getSCMPError();
				logger.info("SC error code:" + scError.getErrorCode() + " text:" + scError.getErrorText());
			}
			response = null;
			APIExecuteAsynchronousTest.messageReceived = true;
		}
	}

}