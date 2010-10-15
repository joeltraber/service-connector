package org.serviceconnector.test.system.perf;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCSessionService;
import org.serviceconnector.ctrl.util.ClientThreadController;
import org.serviceconnector.ctrl.util.ProcessesController;
import org.serviceconnector.ctrl.util.TestConstants;
import org.serviceconnector.log.Loggers;

public class AllPerformanceTests {

	private static final Logger testLogger = Logger.getLogger(Loggers.TEST.getValue());

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(AllPerformanceTests.class);

	private Process scProcess;
	private Process srvProcess;

	private SCClient client;

	private static ProcessesController ctrl;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		ctrl = new ProcessesController();
	}

	@Before
	public void setUp() throws Exception {
		scProcess = ctrl.startSC(TestConstants.log4jSC0Properties, TestConstants.scProperties0);
		srvProcess = ctrl.startServer(TestConstants.sessionSrv, TestConstants.log4jSrvProperties,
				TestConstants.PORT_LISTENER, TestConstants.PORT_TCP, 100, new String[] { TestConstants.serviceName,
						TestConstants.serviceNameAlt });
		client = new SCClient();
		((SCClient) client).setConnectionType("netty.tcp");
		client.attach(TestConstants.HOST, TestConstants.PORT_TCP);
	}

	@After
	public void tearDown() throws Exception {
		try {
			client.detach();
		} catch (Exception e) {
		}
		ctrl.stopProcess(srvProcess, TestConstants.log4jSrvProperties);
		ctrl.stopProcess(scProcess, TestConstants.log4jSC0Properties);
		client = null;
		srvProcess = null;
		scProcess = null;
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		ctrl = null;
	}

	@Test
	public void execute_10000MessagesWith128BytesLongBody_outputsTime() throws Exception {

		SCMessage message = new SCMessage(new byte[128]);

		SCSessionService sessionService = client.newSessionService(TestConstants.serviceName);
		sessionService.createSession("sessionInfo", 300, 60);

		long start = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			sessionService.execute(message);
		}
		long stop = System.currentTimeMillis();

		testLogger.info("Time to execute 10000 messages with 128 byte body was:\t" + (stop - start));
		assertEquals(true, stop - start < 25000);
	}

	// TODO FJU after some time has broken session
	@Test
	public void execute_1000000MessagesWith128BytesLongBody_outputsTime() throws Exception {

		SCMessage message = new SCMessage(new byte[128]);

		SCSessionService sessionService = client.newSessionService(TestConstants.serviceName);
		sessionService.createSession("sessionInfo", 300, 60);

		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++) {
			testLogger.info("Executing message nr. " + (i * 1000));
			for (int j = 0; j < 1000; j++) {
				sessionService.execute(message);
			}
		}
		long stop = System.currentTimeMillis();

		testLogger.info("Time to execute 1000000 messages with 128 byte body was:\t" + (stop - start) + "ms.");
		assertEquals(true, stop - start < 1500000);
	}

	@Test
	public void execute_10MBDataUsingDifferentBodyLength_outputsBestTimeAndBodyLength() throws Exception {
		long previousResult = Long.MAX_VALUE;
		long result = Long.MAX_VALUE - 1;
		int dataLength = 10 * TestConstants.dataLength1MB;
		int messages = 0;

		while (result < previousResult) {
			previousResult = result;
			messages++;

			ClientThreadController clientCtrl = new ClientThreadController(false, true, 1, 1, messages, dataLength
					/ messages);

			result = clientCtrl.perform();

			scProcess = ctrl.restartSC(scProcess, TestConstants.log4jSC0Properties, TestConstants.scProperties0);
			srvProcess = ctrl.restartServer(srvProcess, TestConstants.sessionSrv, TestConstants.log4jSrvProperties,
					TestConstants.PORT_LISTENER, TestConstants.PORT_TCP, 100, new String[] { TestConstants.serviceName,
							TestConstants.serviceNameAlt });
		}

		testLogger.info("Best performance to execute roughly 10MB of data messages was " + previousResult + "ms using "
				+ --messages + " messages of " + dataLength / messages + "B data each.");
		assertEquals(true, previousResult < 25000);
	}

	@Test
	public void execute_10MBDataUsingDifferentBodyLengthStartingFrom100000Messages_outputsBestTimeAndBodyLength()
			throws Exception {
		long previousResult = Long.MAX_VALUE;
		long result = Long.MAX_VALUE - 1;
		int dataLength = 10 * TestConstants.dataLength1MB;
		int messages = 100001;

		while (result < previousResult && messages > 0) {
			previousResult = result;
			messages--;

			ClientThreadController clientCtrl = new ClientThreadController(false, true, 1, 1, messages, dataLength
					/ messages);

			result = clientCtrl.perform();

			scProcess = ctrl.restartSC(scProcess, TestConstants.log4jSC0Properties, TestConstants.scProperties0);
			srvProcess = ctrl.restartServer(srvProcess, TestConstants.sessionSrv, TestConstants.log4jSrvProperties,
					TestConstants.PORT_LISTENER, TestConstants.PORT_TCP, 100, new String[] { TestConstants.serviceName,
							TestConstants.serviceNameAlt });
		}

		testLogger.info("Best performance to execute roughly 10MB of data messages was " + previousResult + "ms using "
				+ ++messages + " messages of " + dataLength / messages + "B data each.");
		assertEquals(true, previousResult < 25000);
	}

	@Test
	public void createSessionDeleteSession_10000Times_outputsTime() throws Exception {

		ClientThreadController clientCtrl = new ClientThreadController(false, true, 1, 10000, 0, 0);
		long result = clientCtrl.perform();
		assertEquals(true, result < 25000);
	}

	@Test
	public void createSessionExecuteDeleteSession_10000ExecuteMessagesDividedInto10ParallelClients_outputsTime()
			throws Exception {
		int threadCount = Thread.activeCount();

		ClientThreadController clientCtrl = new ClientThreadController(false, true, 10, 10, 100, 128);
		long result = clientCtrl.perform();

		testLogger.info("Threads before initializing clients:\t" + threadCount);
		testLogger.info("Threads after execution completed:\t" + Thread.activeCount());
		assertEquals(true, result < 25000);
	}

	@Test
	public void createSessionExecuteDeleteSession_10000ExecuteMessagesSentByOneClient_outputsTime() throws Exception {
		int threadCount = Thread.activeCount();

		ClientThreadController clientCtrl = new ClientThreadController(false, false, 1, 100, 100, 128);
		long result = clientCtrl.perform();

		testLogger.info("Threads before initializing clients:\t" + threadCount);
		testLogger.info("Threads after execution completed:\t" + Thread.activeCount());
		assertEquals(true, result < 25000);
	}

	@Test
	public void createSessionExecuteDeleteSession_roughly100000ExecuteMessagesByParallelClients_outputsBestTimeAndNumberOfClients()
			throws Exception {
		long previousResult = Long.MAX_VALUE;
		long result = Long.MAX_VALUE - 1;
		int clientsCount = 0;

		while (result < previousResult) {
			previousResult = result;
			clientsCount++;

			ClientThreadController clientCtrl = new ClientThreadController(false, true, clientsCount,
					100000 / (1000 * clientsCount), 1000, 128);

			result = clientCtrl.perform();

			scProcess = ctrl.restartSC(scProcess, TestConstants.log4jSC0Properties, TestConstants.scProperties0);
			srvProcess = ctrl.restartServer(srvProcess, TestConstants.sessionSrv, TestConstants.log4jSrvProperties,
					TestConstants.PORT_LISTENER, TestConstants.PORT_TCP, 100, new String[] { TestConstants.serviceName,
							TestConstants.serviceNameAlt });
		}

		testLogger.info("Best performance to execute roughly 100000 messages was " + previousResult + "ms using "
				+ --clientsCount + " parallel clients");
		assertEquals(true, previousResult < 25000);
	}
}