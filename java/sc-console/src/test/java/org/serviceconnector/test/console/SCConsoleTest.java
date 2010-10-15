package org.serviceconnector.test.console;

import static org.junit.Assert.assertEquals;

import java.security.Permission;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.console.ConsoleCommand;
import org.serviceconnector.console.SCConsole;


public class SCConsoleTest {

	protected static class ExitException extends SecurityException {
		public final int status;

		public ExitException(int status) {
			super("There is no escape!");
			this.status = status;
		}
	}

	private static class NoExitSecurityManager extends SecurityManager {
		@Override
		public void checkPermission(Permission perm) {
			// allow anything.
		}

		@Override
		public void checkPermission(Permission perm, Object context) {
			// allow anything.
		}

		@Override
		public void checkExit(int status) {
			super.checkExit(status);
			throw new ExitException(status);
		}
	}

	@Before
	public void setUp() throws Exception {
		System.setSecurityManager(new NoExitSecurityManager());
	}

	@After
	public void tearDown() throws Exception {
		System.setSecurityManager(null); // or save and restore original
	}

	@Test
	public void main_nullParameters_exitCode1() throws Exception {
		try {
			SCConsole.main(null);
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}

	@Test
	public void main_emptyParameters_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] {});
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}

	@Test
	public void main_emptyStringParameter_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}

	@Test
	public void main_5EmptyStringParameter_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "", "", "", "", "" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_whiteStringParameter_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { " " });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}

	@Test
	public void main_5WhiteStringParameter_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { " ", " ", " ", " ", " " });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_oneCharParameter_exitCode2() throws Exception {
		try {
			SCConsole.main(new String[] { "h" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}

	@Test
	public void main_hostStringNoAccordingHostParameter_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_hostStringEmptyHostParameter_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_hostStringWhiteHostParameter_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", " " });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_hostStringOneCharHostParameter_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "h" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_hostStringPortString_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "-p" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_portStringIsAlsoHostParamPortParamEmptyString_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "-p", "" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_portStringIsAlsoHostParamPortParamWhiteChar_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "-p", " " });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_portStringIsAlsoHostParamPortParamOneChar_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "-p", "h" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_portAndHostParamsSwapped_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-p", "-h", "h" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_notEnoughParams_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "h", "-p", "something" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_notEnoughParamsWrongPositions_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "-p", "h", "something" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_notEnoughParamsWrongPositions1_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "-p", "h", "key=something" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_notEnoughParamsMissingPort_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "h", "-p", "key=something" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_multipleHostCommands_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "-h", "h", "-p", "p", "key=something" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	@Test
	public void main_multipleValidCommandsTooManyParams_exitCode1() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_HTTP, ConsoleCommand.STATE.getKey() + "=something", ConsoleCommand.STATE.getKey() + "=something" });
		} catch (ExitException e) {
			assertEquals(1, e.status);
		}
	}
	
	// SC must be running !!
	@Test
	public void main_validShowCommandPortHTTP_exitCode5State() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_HTTP, ConsoleCommand.STATE.getKey() + "=something" });
		} catch (ExitException e) {
			assertEquals(5, e.status);
		}
	}
	
	// SC must be running !!
	@Test
	public void main_validShowCommandPortMGMT_exitCode5State() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_MGMT, ConsoleCommand.STATE.getKey() + "=something"});
		} catch (ExitException e) {
			assertEquals(5, e.status);
		}
	}
	
	// SC must be running !!
	@Test
	public void main_validShowCommandPortTCP_NonExistingService_exitCode4State() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_TCP, ConsoleCommand.STATE.getKey() + "=something"});
		} catch (ExitException e) {
			assertEquals(4, e.status);
		}
	}
	
	// SC must be running !!
	@Test
	public void main_validShowCommandPortTCP_ServiceSimulation_exitCode0State() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", "127.0.0.1", "-p", TestConstants.PORT_SC_TCP, ConsoleCommand.STATE.getKey() + "=simulation" });
		} catch (ExitException e) {
			assertEquals(0, e.status);
		}
	}
	
	// SC must be running !!
	@Test
	public void main_validEnableCommand_exitCode0State() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_TCP, ConsoleCommand.ENABLE.getKey() + "=simulation" });
		} catch (ExitException e) {
			assertEquals(0, e.status);
		}
		SCClient client = new SCClient();
		client.setConnectionType("netty.tcp");
		client.attach(TestConstants.HOST, Integer.parseInt(TestConstants.PORT_SC_TCP));
		assertEquals(true, client.isServiceEnabled("simulation"));
		client.detach();
	}
	
	// SC must be running !!
	@Test
	public void main_enableDisable_changesState() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_TCP, ConsoleCommand.DISABLE.getKey() + "=simulation" });
		} catch (ExitException e) {
			assertEquals(0, e.status);
		}
		SCClient client = new SCClient();
		client.setConnectionType("netty.tcp");
		client.attach(TestConstants.HOST, Integer.parseInt(TestConstants.PORT_SC_TCP));
		assertEquals(false, client.isServiceEnabled("simulation"));
		
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_TCP, ConsoleCommand.ENABLE.getKey() + "=simulation" });
		} catch (ExitException e) {
			assertEquals(0, e.status);
		}
		assertEquals(true, client.isServiceEnabled("simulation"));
		client.detach();
	}
	
	// SC must be running !!
	@Test
	public void main_validSessionsCommand_exitCode0State() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_TCP, ConsoleCommand.SESSIONS.getKey() + "=simulation" });
		} catch (ExitException e) {
			assertEquals(0, e.status);
		}
	}
	
	// SC must be running !!
	@Test
	public void main_validSessionsCommandOnPublishService_exitCode0State() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_TCP, ConsoleCommand.SESSIONS.getKey() + "=publish-simulation" });
		} catch (ExitException e) {
			assertEquals(0, e.status);
		}
	}
	
	// SC must be running !!
	@Test
	public void main_validSessionsCommandOnNotExistingService_exitCode0State() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_TCP, ConsoleCommand.SESSIONS.getKey() + "=notExistingService" });
		} catch (ExitException e) {
			assertEquals(0, e.status);
		}
	}
	
	// SC must be running !!
	@Test
	public void main_validUndefinedCommandOnNotExistingService_exitCode3State() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_TCP, ConsoleCommand.UNDEFINED.getKey() + "=notExistingService" });
		} catch (ExitException e) {
			assertEquals(3, e.status);
		}
	}
	
	// SC must be running !!
	@Test
	public void main_validUndefinedCommandOnServiceSimulation_exitCode3State() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_TCP, ConsoleCommand.UNDEFINED.getKey() + "=simulation" });
		} catch (ExitException e) {
			assertEquals(3, e.status);
		}
	}
	
	// SC must be running !!
	@Test
	public void zz_last_killSC_exitCode0() throws Exception {
		try {
			SCConsole.main(new String[] { "-h", TestConstants.HOST, "-p", TestConstants.PORT_SC_TCP, ConsoleCommand.KILL.getKey()});
		} catch (ExitException e) {
			assertEquals(0, e.status);
		}
	}
}