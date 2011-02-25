/*
 * Copyright � 2010 STABILIT Informatik AG, Switzerland *
 * *
 * Licensed under the Apache License, Version 2.0 (the "License"); *
 * you may not use this file except in compliance with the License. *
 * You may obtain a copy of the License at *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0 *
 * *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS, *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and *
 * limitations under the License. *
 */
package org.serviceconnector.srv;

import java.lang.reflect.Method;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.TestConstants;
import org.serviceconnector.TestUtil;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.srv.SCServer;
import org.serviceconnector.api.srv.SCSessionServer;
import org.serviceconnector.api.srv.SCSessionServerCallback;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.log.SessionLogger;
import org.serviceconnector.util.FileUtility;

public class TestSessionServer extends TestStatefulServer {

	static {
		TestStatefulServer.logger = Logger.getLogger(TestSessionServer.class);
	}

	/**
	 * Main method if you like to start in debug mode.
	 * 
	 * @param args
	 *            [0] serverName<br>
	 *            [1] listenerPort<br>
	 *            [2] SC port<br>
	 *            [3] maxSessions<br>
	 *            [4] maxConnections<br>
	 *            [5] connectionType ("netty.tcp" or "netty.http")<br>
	 *            [6] serviceNames (comma delimited list)<br>
	 *            [7] nics (comma separated list)<br>
	 */
	public static void main(String[] args) throws Exception {
		logger.log(Level.OFF, "TestSessionServer is starting ...");
		for (int i = 0; i < args.length; i++) {
			logger.log(Level.OFF, "args[" + i + "]:" + args[i]);
		}
		TestSessionServer server = new TestSessionServer();
		server.setServerName(args[0]);
		server.setListenerPort(Integer.parseInt(args[1]));
		server.setPort(Integer.parseInt(args[2]));
		server.setMaxSessions(Integer.parseInt(args[3]));
		server.setMaxConnections(Integer.parseInt(args[4]));
		server.setConnectionType(args[5]);
		server.setServiceNames(args[6]);
		server.setNics(args[7]);
		server.run();
	}

	@Override
	public void run() {
		List<String> nics = new ArrayList<String>();
		String[] nicsStrings = this.nicsStrings.split(",");
		for (String nicString : nicsStrings) {
			nics.add(nicString);
		}

		SCServer sc = new SCServer(TestConstants.HOST, this.port, nics, this.listenerPort, this.connectionType);
		try {
			sc.setKeepAliveIntervalSeconds(10);
			sc.setImmediateConnect(true);
			sc.startListener();

			String[] serviceNames = this.serviceNames.split(",");
			for (String serviceName : serviceNames) {
				SCSessionServer server = sc.newSessionServer(serviceName);
				SCSessionServerCallback cbk = new SrvCallback(server);
				try {
					server.register(10, this.maxSessions, this.maxConnections, cbk);
				} catch (Exception e) {
					logger.error("runSessionServer", e);
					server.deregister();
				}
			}
			FileLock pidLock = FileUtility.createPIDfileAndLock(FileUtility.getLogPath() + fs + this.serverName + ".pid");
			// add exit handler
			try {
				this.addExitHandler(FileUtility.getLogPath() + fs + this.serverName + ".pid", pidLock);
			} catch (SCMPValidatorException e1) {
				logger.fatal("unable to get path to pid file", e1);
			}
			logger.log(Level.OFF, "TestSessionServer is running ...");
			// server.destroy();
		} catch (Exception e) {
			logger.error("runSessionServer", e);
		} finally {
			// sc.stopListener();
		}
	}

	/**
	 * Callback handling all server events
	 * 
	 * @author JTrnka
	 */
	class SrvCallback extends SCSessionServerCallback {

		public SrvCallback(SCSessionServer server) {
			super(server);
		}

		@Override
		public SCMessage createSession(SCMessage request, int operationTimeoutMillis) {
			SCMessage response = request;
			String sessionInfo = request.getSessionInfo();
			if (sessionInfo != null) {
				// watch out for kill server message
				if (sessionInfo.equals(TestConstants.killServerCmd)) {
					logger.log(Level.OFF, "Kill request received, exiting ...");
					response.setReject(true);
					KillThread<SCSessionServer> kill = new KillThread<SCSessionServer>(this.scSessionServer);
					kill.start();
					// watch out for reject request
				} else if (sessionInfo.equals(TestConstants.rejectCmd)) {
					response.setReject(true);
					response.setAppErrorCode(TestConstants.appErrorCode);
					response.setAppErrorText(TestConstants.appErrorText);
				}
			}
			SessionLogger.logCreateSession(this.getClass().getName(), request.getSessionId(), -1);
			return response;
		}

		@Override
		public void deleteSession(SCMessage request, int operationTimeoutMillis) {
			SessionLogger.logDeleteSession(this.getClass().getName(), request.getSessionId());
		}

		@Override
		public void abortSession(SCMessage request, int operationTimeoutMillis) {
			SessionLogger.logAbortSession(this.getClass().getName(), request.getSessionId());
		}

		@Override
		public SCMessage execute(SCMessage request, int operationTimeoutMillis) {
			// watch out for method to call passed in messageInfo
			SCMessage response = request;
			String methodName = request.getMessageInfo();
			if (methodName != null) {
				if (methodName.equals(TestConstants.raiseExceptionCmd)) {
					throw new NullPointerException("raised for test purposes");
				}
				try {
					Method method = this.getClass().getMethod(methodName, SCMessage.class, int.class);
					response = (SCMessage) method.invoke(this, request, operationTimeoutMillis);
					return response;
				} catch (Exception e) {
					logger.warn("method " + methodName + " not found on server");
				}
			}
			// return empty message
			return new SCMessage();
		}

		// ==================================================================================
		// methods invoked by name (passed in messageInfo)

		// send back the same message
		public SCMessage echoMessage(SCMessage request, int operationTimeoutMillis) {
			// do not log! it is used for performance benchmarks
			return request;
		}

		// send back an application error
		public SCMessage echoAppError(SCMessage request, int operationTimeoutMillis) {
			request.setAppErrorCode(TestConstants.appErrorCode);
			request.setAppErrorText(TestConstants.appErrorText);
			return request;
		}

		// send back an application error code only
		public SCMessage echoAppError1(SCMessage request, int operationTimeoutMillis) {
			request.setAppErrorCode(TestConstants.appErrorCode);
			return request;
		}

		// send back application error text only
		public SCMessage echoAppError2(SCMessage request, int operationTimeoutMillis) {
			request.setAppErrorText(TestConstants.appErrorText);
			return request;
		}

		// send back missing application error code
		public SCMessage echoAppError3(SCMessage request, int operationTimeoutMillis) {
			request.setAppErrorCode(Constants.EMPTY_APP_ERROR_CODE);
			return request;
		}

		// send back application error code = 0
		public SCMessage echoAppError4(SCMessage request, int operationTimeoutMillis) {
			request.setAppErrorCode(0);
			return request;
		}

		// sleep for time defined in the body and send back the same message
		public SCMessage sleep(SCMessage request, int operationTimeoutMillis) {
			String dataString = (String) request.getData();
			int millis = Integer.parseInt(dataString);
			try {
				logger.info("Sleeping " + millis + "ms");
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				logger.warn("sleep interrupted " + e.getMessage());
			} catch (Exception e) {
				logger.error("sleep error", e);
			}
			return request;
		}

		// send back a large response
		public SCMessage largeResponse(SCMessage request, int operationTimeoutMillis) {
			String largeString = TestUtil.getLargeString();
			request.setData(largeString);
			return request;
		}

		// send back a 10MB large response
		public SCMessage largeResponse10MB(SCMessage request, int operationTimeoutMillis) {
			String largeString = TestUtil.get10MBString();
			request.setData(largeString);
			return request;
		}

		// causes caching response message
		public SCMessage cache(SCMessage request, int operationTimeoutMillis) {
			Calendar time = Calendar.getInstance();
			String dataString = (String) request.getData();

			logger.info("cache call");
			if (dataString.equals("cidNoCed")) {
				logger.info("cidNoCed");
				// reply without setting CacheExpirationDateTime
				return request;
			} else if (dataString.startsWith("cacheFor2Sec")) {
				logger.info("cacheFor2Sec");
				time.add(Calendar.SECOND, 2);
				request.setCacheExpirationDateTime(time.getTime());
			} else if (dataString.startsWith("cacheFor1Hour")) {
				logger.info("cacheFor1Hour");
				time.add(Calendar.HOUR_OF_DAY, 1);
				request.setCacheExpirationDateTime(time.getTime());
			} else if (dataString.startsWith("cacheFor2Hour")) {
				logger.info("cacheFor2Hour");
				time.add(Calendar.HOUR_OF_DAY, 2);
				request.setCacheExpirationDateTime(time.getTime());
			} else if (dataString.startsWith("refreshCache700")) {
				logger.info("refreshCache700");
				time.add(Calendar.HOUR_OF_DAY, 1);
				request.setCacheExpirationDateTime(time.getTime());
				request.setCacheId("700");
			} else if (dataString.startsWith("cacheLargeMessageFor1Hour")) {
				logger.info("cacheLargeMessageFor1Hour");
				time.add(Calendar.HOUR_OF_DAY, 1);
				request.setCacheExpirationDateTime(time.getTime());
				String largeMessage = TestUtil.getLargeString();
				request.setData(largeMessage);
			} else if (dataString.startsWith("cache10MBString1Hour")) {
				logger.info("cache10MBMessageFor1Hour");
				time.add(Calendar.HOUR_OF_DAY, 1);
				request.setCacheExpirationDateTime(time.getTime());
				String largeMessage = TestUtil.get10MBString();
				request.setData(largeMessage);
			} else if (dataString.startsWith("cacheExpired1Hour")) {
				logger.info("cacheExpired1Hour");
				time.add(Calendar.HOUR_OF_DAY, -1);
				request.setCacheExpirationDateTime(time.getTime());
			} else if (dataString.startsWith("cacheServerReplyOther")) {
				logger.info("cacheServerReplyOther");
				time.add(Calendar.HOUR_OF_DAY, 1);
				request.setCacheExpirationDateTime(time.getTime());
				String cacheId = request.getCacheId();
				int iCacheId = Integer.parseInt(cacheId);
				iCacheId += 100;
				request.setCacheId(String.valueOf(iCacheId));
			} else if (dataString.startsWith("cacheTimeoutReply")) {
				logger.info("cacheTimeoutReply");
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
				request.setCacheExpirationDateTime(time.getTime());
			} else {
				logger.info("cache no special key");
				// no special key, we set default expiration time to 1 hour, otherwise SC will not accept the message for its cache
				time.add(Calendar.HOUR_OF_DAY, 1);
				request.setCacheExpirationDateTime(time.getTime());
			}
			return request;
		}
	}
}
