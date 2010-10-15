package org.serviceconnector.cln;

import org.apache.log4j.Logger;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCSessionService;
import org.serviceconnector.ctrl.util.TestConstants;


public class StartSessionClient extends Thread {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(StartSessionClient.class);

	private String methodName;

	public static void main(String[] args) {
		try {
			StartSessionClient client = new StartSessionClient(args[0]);
			client.start();
		} catch (Exception e) {
			logger.error("incorrect parameters", e);
		}
	}

	public StartSessionClient(String methodName) {
		this.methodName = methodName;
	}

	@Override
	public void run() {
		SCClient client = new SCClient();

		try {
			client.attach(TestConstants.HOST, TestConstants.PORT_HTTP);

			if (getMethodName() == "createSession_whiteSpaceSessionInfo_sessionIdIsNotEmpty") {
				SCSessionService sessionService = client
						.newSessionService(TestConstants.serviceName);
				sessionService.createSession(" ", 300, 60);
				sessionService.deleteSession();

			} else if (getMethodName() == "createSession_arbitrarySpaceSessionInfo_sessionIdIsNotEmpty") {
				SCSessionService sessionService = client
						.newSessionService(TestConstants.serviceName);
				sessionService.createSession("The quick brown fox jumps over a lazy dog.", 300, 60);
				sessionService.deleteSession();

			} else if (getMethodName() == "createSession_arbitrarySpaceSessionInfoDataOneChar_sessionIdIsNotEmpty") {
				SCSessionService sessionService = client
						.newSessionService(TestConstants.serviceName);
				sessionService.createSession("The quick brown fox jumps over a lazy dog.", 300, 10,
						"a");
				sessionService.deleteSession();
				
			} else if (getMethodName() == "createSession_256LongSessionInfoData60kBByteArray_sessionIdIsNotEmpty") {
				SCSessionService sessionService = client
						.newSessionService(TestConstants.serviceName);
				sessionService.createSession(TestConstants.stringLength256, 300, 10,
						new byte[TestConstants.dataLength60kB]);
				sessionService.deleteSession();

			} else if (getMethodName() == "deleteSession_beforeCreateSession_noSessionId") {
				SCSessionService sessionService = client
						.newSessionService(TestConstants.serviceName);
				sessionService.deleteSession();

			} else if (getMethodName() == "deleteSession_afterValidNewSessionService_noSessionId") {
				SCSessionService sessionService = client
						.newSessionService(TestConstants.serviceName);
				sessionService.createSession("sessionInfo", 300, 60);
				sessionService.deleteSession();

			} else if (getMethodName() == "createSession_rejectTheSessionThenCreateValidSessionThenExecuteAMessage_passes") {
				SCSessionService sessionService = client
						.newSessionService(TestConstants.serviceName);

				try {
					sessionService.createSession("sessionInfo", 300, 10, "reject");
				} catch (Exception e) {
				}
				sessionService.createSession("sessionInfo", 300, 10);

				sessionService.execute(new SCMessage());
				sessionService.deleteSession();
				
			} else if (getMethodName() == "execute_messageData1MBArray_returnsTheSameMessageData") {
				SCSessionService sessionService = client.newSessionService(TestConstants.serviceName);
				sessionService.createSession("sessionInfo", 300, 60);

				SCMessage message = new SCMessage(new byte[TestConstants.dataLength1MB]);
				message.setCompressed(false);

				sessionService.execute(message);
				sessionService.deleteSession();
				
			} else if (getMethodName() == "createSessionExecuteDeleteSession_twice_6MessagesArrive") {
				SCSessionService sessionService = client.newSessionService(TestConstants.serviceName);

				sessionService.createSession("sessionInfo", 300, 60);
				sessionService.execute(new SCMessage(new byte[128]));
				sessionService.deleteSession();
				
				sessionService.createSession("sessionInfo", 300, 60);
				sessionService.execute(new SCMessage(new byte[128]));
				sessionService.deleteSession();

			} else if (getMethodName() == "echo_waitFor3EchoMessages_5MessagesArrive") {
				SCSessionService sessionService = client.newSessionService(TestConstants.serviceName);
				sessionService.createSession("sessionInfo", 2, 1);
				Thread.sleep(6000);
				sessionService.deleteSession();
			}

		} catch (Exception e) {
			logger.error("run", e);
		} finally {
			try {
				client.detach();
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
}