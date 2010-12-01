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
package org.serviceconnector.ctrl.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.TestConstants;
import org.serviceconnector.api.cln.SCMgmtClient;
import org.serviceconnector.log.Loggers;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.util.FileUtility;

public class ProcessesController {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(ProcessesController.class);
	private static final Logger testLogger = Logger.getLogger(Loggers.TEST.getValue());

	private String fs;
	private String userDir;

	public ProcessesController() {
		fs = System.getProperty("file.separator");
		userDir = System.getProperty("user.dir");
	}

	/**
	 * get path of the log directory configured in the log4j file
	 */
	private String getLogDirPath(String log4jProperties) throws Exception {
		// Read & parse properties file.
		Properties properties = new Properties();
		properties.load(new FileInputStream(log4jProperties));
		return userDir + fs + properties.getProperty(TestConstants.logDirectoryToken);
	}

	public ProcessCtx startSC(String log4jSCProperties, String scProperties) throws Exception {

		ProcessCtx proc = new ProcessCtx();

		String scRunableFull = userDir + fs + "target" + fs + TestConstants.scRunable;
		if (FileUtility.notExists(scRunableFull)) {
			throw new Exception("File:" + scRunableFull + " does not exist!");
		}
		proc.setRunableFull(scRunableFull);

		String scPropertiesFull = userDir + fs + "src" + fs + "main" + fs + "resources" + fs + scProperties;
		if (FileUtility.notExists(scPropertiesFull)) {
			throw new Exception("File:" + scPropertiesFull + " does not exist!");
		}
		proc.setPropertyFileName(scProperties);
		proc.setPropertyFileNameFull(scPropertiesFull);

		String log4jFileNameFull = userDir + fs + "src" + fs + "main" + fs + "resources" + fs + log4jSCProperties;
		if (FileUtility.notExists(log4jFileNameFull)) {
			throw new Exception("File:" + log4jFileNameFull + " does not exist!");
		}
		proc.setLog4jFileName(log4jSCProperties);
		proc.setLog4jFileNameFull(log4jFileNameFull);

		String pidFileNameFull = getLogDirPath(log4jFileNameFull) + Constants.PID_FILE_NAME;
		proc.setPidFileNameFull(pidFileNameFull);

		/*
		 * start SC process Args: 
		 * [0] -Dlog4j.configuration=file
		 * [1] log4jProperties 
		 * [2]-jar 
		 * [3] SC runnable 
		 * [4] -sc.configuration
		 */
		String command = "java -Dlog4j.configuration=file:" + log4jFileNameFull + " -jar " + scRunableFull + " -sc.configuration "
				+ scPropertiesFull;

		Process process = Runtime.getRuntime().exec(command);
		proc.setProcess(process);

		int timeout = 10; // seconds
		try {
			FileUtility.waitExists(pidFileNameFull, timeout);
			testLogger.info("SC started");
		} catch (Exception e) {
			testLogger.info(e.getMessage());
			testLogger.info("SC not started within " + timeout + " seconds! Timeout exceeded.");
			throw e;
		}
		return proc;
	}

	public void stopSC(ProcessCtx scProcess) throws Exception {
		int timeout = 10; // seconds
		try {
			if (FileUtility.exists(scProcess.getPidFileNameFull())) {
				SCMgmtClient client = new SCMgmtClient(TestConstants.LOCALHOST, TestConstants.PORT_TCP, ConnectionType.NETTY_TCP);
				client.attach(timeout);
				client.killSC();
				FileUtility.waitNotExists(scProcess.getPidFileNameFull(), timeout);
			}
			testLogger.info("SC stopped");
		} catch (Exception e) {
			testLogger.info(e.getMessage());
			testLogger.info("Cannot stop SC! Timeout exceeded.");
		} finally {
			if (scProcess.isRunning()) {
				scProcess.getProcess().destroy();
				scProcess.getProcess().waitFor();
			}
		}
	}

	/**
	 * Start a server in a new JVM. Parameters controls the server execution.
	 * 
	 * @param serverType
	 *            ("session" or "publish")
	 * @param log4jProperties
	 *            (file name)
	 * @param listenerPort
	 * @param SCport
	 * @param maxSessions
	 * @param maxConnections
	 * @param serviceNames
	 *            (comma delimited list)
	 * @return Process with JVM in which the server is started
	 * @throws Exception
	 */
	public ProcessCtx startServer(String serverType, String log4jSrvProperties, String serverName, int listenerPort, int scPort,
			int maxSessions, int maxConnections, String serviceNames) throws Exception {

		ProcessCtx proc = new ProcessCtx();

		String srvRunableFull = userDir + fs + "target" + fs + TestConstants.serverRunable;
		if (FileUtility.notExists(srvRunableFull)) {
			throw new Exception("File:" + srvRunableFull + " does not exist!");
		}
		proc.setRunableFull(srvRunableFull);

		String log4jFileNameFull = userDir + fs + "src" + fs + "main" + fs + "resources" + fs + log4jSrvProperties;
		if (FileUtility.notExists(log4jFileNameFull)) {
			throw new Exception("File:" + log4jFileNameFull + " does not exist!");
		}
		proc.setLog4jFileName(log4jSrvProperties);
		proc.setLog4jFileNameFull(log4jFileNameFull);

		String pidFileNameFull = getLogDirPath(log4jFileNameFull) + fs + serverName + ".pid";
		proc.setPidFileNameFull(pidFileNameFull);

		/*
		 * start server process Args: 
		 * [0] -Dlog4j.configuration=file 
		 * [1] log4jProperties 
		 * [2] -jar 
		 * [3] server runnable 
		 * [4] serverType ("session" or "publish") 
		 * [5] PID file 
		 * [6] listenerPort 
		 * [7] SC port 
		 * [8] maxSessions 
		 * [9] maxConnections 
		 * [10] serviceNames (comma delimited list)
		 */
		String command = "java -Dlog4j.configuration=file:" + log4jFileNameFull + " -jar " + srvRunableFull + " " + serverType + " "
				+ pidFileNameFull + " " + listenerPort + " " + scPort + " " + maxSessions + " " + maxConnections + " "
				+ serviceNames;
		Process srvProcess = Runtime.getRuntime().exec(command);
		proc.setProcess(srvProcess);
		int timeout = 10;
		try {
			FileUtility.waitExists(pidFileNameFull, timeout);
			testLogger.info("Server started");
		} catch (Exception e) {
			testLogger.info(e.getMessage());
			testLogger.info("Server not started within " + timeout + " seconds! Timeout exceeded.");
			throw e;
		}
		return proc;
	}

	public void stopServer(ProcessCtx srvProcess) throws Exception {
		int timeout = 10; // seconds
		try {
			if (FileUtility.exists(srvProcess.getPidFileNameFull())) {
				srvProcess.getProcess().destroy();
				srvProcess.getProcess().waitFor();

				File pidFile = new File(srvProcess.getPidFileNameFull());
				pidFile.delete();
				FileUtility.waitNotExists(srvProcess.getPidFileNameFull(), timeout);
			}
			testLogger.info("Server stopped");
		} catch (Exception e) {
			testLogger.info(e.getMessage());
			testLogger.info("Cannot stop server! Timeout exceeded.");
		} finally {
			srvProcess.getProcess().destroy(); // just to be sure
			srvProcess.getProcess().waitFor(); // just to be sure
		}
	}
}
