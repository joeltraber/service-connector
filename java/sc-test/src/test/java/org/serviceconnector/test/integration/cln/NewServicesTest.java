package org.serviceconnector.test.integration.cln;

import static org.junit.Assert.assertEquals;

import java.security.InvalidParameterException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCFileService;
import org.serviceconnector.api.cln.SCPublishService;
import org.serviceconnector.api.cln.SCSessionService;
import org.serviceconnector.ctrl.util.ProcessesController;
import org.serviceconnector.ctrl.util.TestConstants;

public class NewServicesTest {
	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(NewServicesTest.class);

	private int threadCount = 0;
	private SCClient client;
	private static Process scProcess;

	private static ProcessesController ctrl;

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		ctrl = new ProcessesController();
		try {
			scProcess = ctrl.startSC(TestConstants.log4jSC0Properties, TestConstants.scProperties0);
		} catch (Exception e) {
			logger.error("oneTimeSetUp", e);
		}
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		ctrl.stopProcess(scProcess, TestConstants.log4jSC0Properties);
		ctrl = null;
		scProcess = null;
	}

	@Before
	public void setUp() throws Exception {
		client = new SCClient();
		client.attach(TestConstants.HOST, TestConstants.PORT_HTTP);
		threadCount = Thread.activeCount();
	}

	@After
	public void tearDown() throws Exception {
		client.detach();
		client = null;
		assertEquals("number of threads", threadCount, Thread.activeCount());
	}

	@Test(expected = InvalidParameterException.class)
	public void newSessionService_NullParam_throwsInvalidParamException() throws Exception {
		client.newSessionService(null);
	}

	@Test
	public void newSessionService_emptyStringParam_returnsSCSessionService() throws Exception {
		assertEquals(true, client.newSessionService("") instanceof SCSessionService);
		this.threadCount += 1; // one thread for session timeout in SessionService

	}

	@Test
	public void newSessionService_whiteSpaceStringParam_returnsSCSessionService() throws Exception {
		assertEquals(true, client.newSessionService(" ") instanceof SCSessionService);
	}

	@Test
	public void newSessionService_ArbitraryStringParam_returnsSCSessionService() throws Exception {
		assertEquals(true,
				client.newSessionService("The quick brown fox jumps over a lazy dog.") instanceof SCSessionService);
	}

	@Test
	public void newSessionService_validServiceName_returnsSCSessionService() throws Exception {
		assertEquals(true, client.newSessionService(TestConstants.serviceName) instanceof SCSessionService);
	}

	@Test
	public void newSessionService_validDisabledServiceName_returnsSCSessionService() throws Exception {
		assertEquals(true,
				client.newSessionService(TestConstants.serviceNameSessionDisabled) instanceof SCSessionService);
	}

	@Test
	public void newSessionService_twice_returnsSCSessionService() throws Exception {
		assertEquals(true, client.newSessionService(TestConstants.serviceName) instanceof SCSessionService);
		assertEquals(true, client.newSessionService(TestConstants.serviceName) instanceof SCSessionService);
	}

	@Test
	public void newSessionService_twiceDifferentServiceName_returnsSCSessionService() throws Exception {
		assertEquals(true, client.newSessionService(TestConstants.serviceName) instanceof SCSessionService);
		assertEquals(true, client.newSessionService(TestConstants.serviceNameAlt) instanceof SCSessionService);
	}

	@Test
	public void newSessionService_1000TimesDifferentServiceName_returnsSCSessionService() throws Exception {
		for (int i = 0; i < 500; i++) {
			assertEquals(true, client.newSessionService(TestConstants.serviceName) instanceof SCSessionService);
			assertEquals(true, client.newSessionService(TestConstants.serviceNameAlt) instanceof SCSessionService);
		}
	}

	@Test(expected = InvalidParameterException.class)
	public void newPublishService_NullParam_throwsInvalidParamException() throws Exception {
		client.newPublishService(null);
	}

	@Test
	public void newPublishService_emptyStringParam_returnsSCPublishService() throws Exception {
		assertEquals(true, client.newPublishService("") instanceof SCPublishService);
	}

	@Test
	public void newPublishService_whiteSpaceStringParam_returnsSCPublishService() throws Exception {
		assertEquals(true, client.newPublishService(" ") instanceof SCPublishService);
	}

	@Test
	public void newPublishService_ArbitraryStringParam_returnsSCPublishService() throws Exception {
		assertEquals(true,
				client.newPublishService("The quick brown fox jumps over a lazy dog.") instanceof SCPublishService);
	}

	@Test
	public void newPublishService_serviceNameInSCPropertiesNotPublish_returnsSCPublishService() throws Exception {
		assertEquals(true, client.newPublishService(TestConstants.serviceName) instanceof SCPublishService);
	}

	@Test
	public void newPublishService_serviceNameInSCPropertiesPublish_returnsSCPublishService() throws Exception {
		assertEquals(true, client.newPublishService(TestConstants.serviceName) instanceof SCPublishService);
	}

	@Test
	public void newPublishService_validDisabledServiceName_returnsSCPublishService() throws Exception {
		assertEquals(true,
				client.newPublishService(TestConstants.serviceNameSessionDisabled) instanceof SCPublishService);
	}

	@Test
	public void newPublishService_twice_returnsSCPublishService() throws Exception {
		assertEquals(true, client.newPublishService(TestConstants.serviceName) instanceof SCPublishService);
		assertEquals(true, client.newPublishService(TestConstants.serviceName) instanceof SCPublishService);
	}

	@Test
	public void newPublishService_twiceDifferentServiceName_returnsSCPublishService() throws Exception {
		assertEquals(true, client.newPublishService(TestConstants.serviceName) instanceof SCPublishService);
		assertEquals(true, client.newPublishService(TestConstants.serviceNameAlt) instanceof SCPublishService);
	}

	@Test
	public void newPublishService_1000TimesDifferentServiceName_returnsSCPublishService() throws Exception {
		for (int i = 0; i < 500; i++) {
			assertEquals(true, client.newPublishService(TestConstants.serviceName) instanceof SCPublishService);
			assertEquals(true, client.newPublishService(TestConstants.serviceNameAlt) instanceof SCPublishService);
		}
	}

	@Test(expected = InvalidParameterException.class)
	public void newFileService_NullParam_throwsInvalidParamException() throws Exception {
		client.newFileService(null);
	}

	@Test
	public void newFileService_emptyStringParam_returnsSCFileService() throws Exception {
		assertEquals(true, client.newFileService("") instanceof SCFileService);
	}

	@Test
	public void newFileService_whiteSpaceStringParam_returnsSCFileService() throws Exception {
		assertEquals(true, client.newFileService(" ") instanceof SCFileService);
	}

	@Test
	public void newFileService_ArbitraryStringParam_returnsSCFileService() throws Exception {
		assertEquals(true, client.newFileService("The quick brown fox jumps over a lazy dog.") instanceof SCFileService);
	}

	@Test
	public void newFileService_validServiceName_returnsSCFileService() throws Exception {
		assertEquals(true, client.newFileService(TestConstants.serviceName) instanceof SCFileService);
	}

	@Test
	public void newFileService_validDisabledServiceName_returnsSCFileService() throws Exception {
		assertEquals(true, client.newFileService(TestConstants.serviceNameSessionDisabled) instanceof SCFileService);
	}

	@Test
	public void newFileService_twice_returnsSCFileService() throws Exception {
		assertEquals(true, client.newFileService(TestConstants.serviceName) instanceof SCFileService);
		assertEquals(true, client.newFileService(TestConstants.serviceName) instanceof SCFileService);
	}

	@Test
	public void newFileService_twiceDifferentServiceName_returnsSCFileService() throws Exception {
		assertEquals(true, client.newFileService(TestConstants.serviceName) instanceof SCFileService);
		assertEquals(true, client.newFileService(TestConstants.serviceNameAlt) instanceof SCFileService);
	}

	@Test
	public void newFileService_1000TimesDifferentServiceName_returnsSCFileService() throws Exception {
		for (int i = 0; i < 500; i++) {
			assertEquals(true, client.newFileService(TestConstants.serviceName) instanceof SCFileService);
			assertEquals(true, client.newFileService(TestConstants.serviceNameAlt) instanceof SCFileService);
		}
	}
}