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
package org.serviceconnector.test.system.perf;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.ctrl.util.ProcessCtx;
import org.serviceconnector.ctrl.util.ProcessesController;
import org.serviceconnector.log.Loggers;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.test.integration.cln.AttachDetachTest;

public class AttachBenchmarks {

	/** The Constant testLogger. */
	private static final Logger testLogger = Logger.getLogger(Loggers.TEST.getValue());

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(AttachDetachTest.class);

	private static ProcessesController ctrl;
	private static ProcessCtx scCtx;
	private SCClient client;

	@BeforeClass
	public static void beforeAllTests() throws Exception {
		ctrl = new ProcessesController();
		scCtx = ctrl.startSC(TestConstants.log4jSCProperties, TestConstants.SCProperties);
	}

	@Before
	public void beforeOneTest() throws Exception {
		scCtx = ctrl.startSC(TestConstants.log4jSCProperties, TestConstants.SCProperties);
	}

	@After
	public void afterOneTest() throws Exception {
		try {
			client.detach();
		} catch (Exception e) {
		}
		client = null;
	}

	@AfterClass
	public static void afterAllTests() throws Exception {
		try {
			ctrl.stopSC(scCtx);
			scCtx = null;
		} catch (Exception e) {
		}
		ctrl = null;
	}

	/**
	 * Description: attach 10000 times to SC on localhost and tcp-connection type. Measure performance <br>
	 * Expectation: Performance better than 100 cycles/sec.
	 */
	@Test
	public void benchmark_10000_tcp() throws Exception {
		client = new SCClient(TestConstants.HOST, TestConstants.PORT_TCP, ConnectionType.NETTY_TCP);
		int nr = 10000;
		int sleep = 0;
		long start = System.currentTimeMillis();
		for (int i = 0; i < nr; i++) {
			if (((i + 1) % 200) == 0)
				testLogger.info("Attach/detach nr. " + (i + 1) + "...");
			client.attach();
			assertEquals("Client is attached", true, client.isAttached());
			if (sleep > 0)
				Thread.sleep(sleep);
			client.detach();
			assertEquals("Client is detached", false, client.isAttached());
		}
		long stop = System.currentTimeMillis();
		long perf = nr * 1000 / (stop - start);
		testLogger.info(nr + "attach/detach performance : " + perf + " cycles/sec.");
		assertEquals(true, perf > 50);
	}

	/**
	 * Description: attach 10000 times to SC on localhost and http-connection type. Measure performance <br>
	 * Expectation: Performance better than 100 cycles/sec.
	 */
	@Test
	public void benchmark_10000_http() throws Exception {
		client = new SCClient(TestConstants.HOST, TestConstants.PORT_HTTP, ConnectionType.NETTY_HTTP);
		int nr = 10000;
		int sleep = 0;
		long start = System.currentTimeMillis();
		for (int i = 0; i < nr; i++) {
			if (((i + 1) % 200) == 0)
				testLogger.info("Attach/detach nr. " + (i + 1) + "...");
			client.attach();
			assertEquals("Client is attached", true, client.isAttached());
			if (sleep > 0)
				Thread.sleep(sleep);
			client.detach();
			assertEquals("Client is detached", false, client.isAttached());
		}
		long stop = System.currentTimeMillis();
		long perf = nr * 1000 / (stop - start);
		testLogger.info(nr + "attach/detach performance : " + perf + " cycles/sec.");
		assertEquals(true, perf > 50);
	}

}