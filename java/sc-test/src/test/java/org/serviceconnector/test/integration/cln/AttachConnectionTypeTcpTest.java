package org.serviceconnector.test.integration.cln;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.security.InvalidParameterException;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.Constants;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.ctrl.util.ProcessesController;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.service.SCServiceException;
import org.serviceconnetor.TestConstants;

public class AttachConnectionTypeTcpTest {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(AttachConnectionTypeTcpTest.class);

	private SCClient client;

	private static ProcessesController ctrl;
	private static Process scProcess;
	// threadCount = Thread.activeCount();

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		ctrl = new ProcessesController();
		try {
			scProcess = ctrl.startSC(TestConstants.log4jSCProperties, TestConstants.SCProperties);
		} catch (Exception e) {
			logger.error("oneTimeSetUp", e);
		}
	}

	@AfterClass
	public static void oneTimeTearDown() throws Exception {
		ctrl.stopSC(scProcess, TestConstants.log4jSCProperties);
		ctrl = null;
		scProcess = null;
	}

	@Before
	public void setUp() throws Exception {
		// threadCount = Thread.activeCount();
		client = null;
	}

	@After
	public void tearDown() throws Exception {
		if (client!=null) {
			try {
				client.detach();
			} catch (Exception e) {	}
			client = null;
		}
		// assertEquals("number of threads", threadCount, Thread.activeCount());
	}

	
	/**
	 * Initialize the client with host, port and default connection type.
	 * @param host
	 * @param port
	 */
	private void testConstructor(String host, int port) {
		this.testConstructor(host, port, ConnectionType.DEFAULT_CLIENT_CONNECTION_TYPE);
	}

	/**
	 * Initialize the client.
	 * @param host
	 * @param port
	 * @param connectionType
	 */
	private void testConstructor(String host, int port, ConnectionType connectionType) {
		client = new SCClient(host, port, connectionType);

		assertEquals("Host ", host, client.getHost());
		assertEquals("port ", port, client.getPort());
		assertEquals("Keep Alive Interval ", Constants.DEFAULT_KEEP_ALIVE_INTERVAL, client.getKeepAliveIntervalInSeconds());
		assertEquals("Attached ", false, client.isAttached());
		assertEquals("max Connections ", Constants.DEFAULT_MAX_CONNECTION_POOL_SIZE, client.getMaxConnections());
		assertEquals("Connection Type ", connectionType.getValue(), client.getConnectionType());
		assertNotNull("Client not created:", client);
	}
	
	/**
	 * Attach with default operationTimeout.
	 */
	private void testAttach() {
		this.testAttach(0, false, null);
	}

	/**
	 * Attach with default operationTimeout and expect the exception. 
	 * @param expectedException 
	 */
	private void testAttach( String expectedException) {
		this.testAttach(0, false, expectedException);
	}

	/**
	 * Attach with operationTimeout and expect no exception.
	 * @param operationTimeout in seconds
	 */
	private void testAttach(int operationTimeout) {
		this.testAttach(operationTimeout, true, null);
	}
	
	/**
	 * Attach with operationTimeout and expect one exception.
	 * 
	 * @param operationTimeout in seconds
	 * @param expectedException
	 */
	private void testAttach(int operationTimeout, String expectedException) {
		this.testAttach(operationTimeout, true, expectedException);
	}

	/**
	 * 
	 * @param operationTimeout
	 * @param useOperationTimeout
	 * @param expectedException
	 */
	private void testAttach(int operationTimeout, boolean useOperationTimeout, String expectedException) {
		try {
			if (useOperationTimeout) {
				client.attach(operationTimeout);
			}
			else {
				client.attach();
			}
			assertEquals("Client Not Attached ", true, client.isAttached());
			assertEquals("Expected exception did not arise, ", expectedException, null);
		} catch (InvalidParameterException ex) {
			assertEquals("InvalidParameterException: ", expectedException, "InvalidParameterException");
		} catch (SCServiceException ex) {
			assertEquals("SCServiceException: ", expectedException, "SCServiceException");
		} catch (Exception ex) {
			assertEquals("Exception: ", expectedException, "InvalidParameterException");
		} 
	}

	/**
	 * Description: Attach client with default host and tcp-port.<br>
	 * Expectation:	Client is attached.
	 */
	@Test
	public void t01_attach() throws Exception {
		this.testConstructor(TestConstants.LOCALHOST, TestConstants.PORT_TCP);
		this.testAttach();
	}

	/**
	 * Description: Attach client with default host and http-port.<br>
	 * Expectation:	Client is not attached and throws Exception.
	 */
	public void t02_attach() throws Exception {
		this.testConstructor(TestConstants.HOST, TestConstants.PORT_HTTP);
		this.testAttach("Exception");
	}

	/**
	 * Description: Attach client with default host and port zero.<br>
	 * Expectation:	Client is not attached and throws SCServiceException.
	 */
	@Test
	public void t03_attach() throws Exception {
		this.testConstructor(TestConstants.HOST, 0);
		this.testAttach("SCServiceException");
	}

	/**
	 * Description: Attach client with default host and port -1.<br>
	 * Expectation:	Client is not attached and throws SCMPValidatorException.
	 */
	@Test
	public void t04_attach() throws Exception {
		this.testConstructor(TestConstants.HOST, -1);
		this.testAttach("InvalidParameterException");
	}

	/**
	 * Description: Attach client with default host and port is set to minimum.<br>
	 * Expectation:	Client is not attached and throws SCServiceException.
	 */
	@Test
	public void t05_attach() throws Exception {
		this.testConstructor(TestConstants.HOST, TestConstants.PORT_MIN);
		this.testAttach("SCServiceException");
	}

	/**
	 * Description: Attach client with default host and port is set to maximum allowed.<br>
	 * Expectation:	Client is not attached and throws SCServiceException.
	 */
	@Test
	public void t06_attach() throws Exception {
		this.testConstructor(TestConstants.HOST, 0xFFFF);
		this.testAttach("SCServiceException");
	}

	/**
	 * Description: Attach client with default host and the port is set to maximum + 1.<br>
	 * Expectation:	Client is not attached and throws SCMPValidatorException.
	 */
	@Test
	public void t07_attach() throws Exception {
		this.testConstructor(TestConstants.HOST, 0xFFFF + 1);
		this.testAttach("InvalidParameterException");
	}

	/**
	 * Description: Attach client with default host and tcp-port.<br>
	 * Expectation:	Client is attached.
	 */
	@Test
	public void t08_attach() throws Exception {
		this.testConstructor(TestConstants.LOCALHOST, TestConstants.PORT_TCP);
		this.testAttach(10);
	}
	
	
	/**
	 * Description: Attach one client two times with default host and tcp-port.<br>
	 * Expectation:	Client is attached but second attach throws SCServiceException
	 */
	@Test
	public void t20_attach() throws Exception {
		this.testConstructor(TestConstants.LOCALHOST, TestConstants.PORT_TCP);
		this.testAttach();
		this.testAttach("SCServiceException");  // second attach throws SCServiceException	
		assertEquals("Client is not attached", true, client.isAttached());
	}
	
	
	
}
