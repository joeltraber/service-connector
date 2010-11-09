/*-----------------------------------------------------------------------------*
 *                                                                             *
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
 *-----------------------------------------------------------------------------*/
package org.serviceconnector;

import java.io.File;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.serviceconnector.cmd.sc.ServiceConnectorCommandFactory;
import org.serviceconnector.conf.CommunicatorConfig;
import org.serviceconnector.conf.ResponderConfiguration;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.log.ILoggingManagerMXBean;
import org.serviceconnector.log.JMXLoggingManager;
import org.serviceconnector.net.res.IResponder;
import org.serviceconnector.net.res.Responder;
import org.serviceconnector.server.ServerLoader;
import org.serviceconnector.service.SCServiceException;
import org.serviceconnector.service.ServiceLoader;
import org.serviceconnector.util.CommandLineUtil;
import org.serviceconnector.util.Statistics;
import org.serviceconnector.util.SystemInfo;
import org.serviceconnector.web.cmd.sc.ServiceConnectorWebCommandFactory;
import org.serviceconnector.web.ctx.WebContext;

/**
 * The Class SC. Starts the core (responders) of the Service Connector.
 * 
 * @author JTraber
 */
public final class SC {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SC.class);

	/**
	 * Instantiates a new service connector.
	 */
	private SC() {
	}

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws Exception
	 *             the exception
	 */
	public static void main(String[] args) throws Exception {
		String configFileName = CommandLineUtil.getArg(args, Constants.CLI_CONFIG_ARG);
		try {
			SC.run(configFileName);
		} catch (Exception ex) {
			logger.fatal(ex.getMessage(), ex);
			throw ex;
		}
	}

	/**
	 * Run SC responders.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static void run(String configFileName) throws Exception {
		if (configFileName == null) {
			throw new SCServiceException("Configuration file is missing");
		}

		// load the configuration
		ResponderConfiguration configuration = new ResponderConfiguration();
		configuration.load(configFileName);

		// write system information to log
		SystemInfo.setConfigFileName(configFileName);
		SC.writeSystemInfoToLog();
		
		// Initialize service connector command factory
		AppContext appContext = AppContext.getCurrentContext();
		appContext.initContext(new ServiceConnectorCommandFactory());
		WebContext webContext = WebContext.getCurrentContext();
		webContext.initContext(new ServiceConnectorWebCommandFactory());

		// initialize JMX
		SC.initializeJMX();

		// initialize statistics
		Statistics statistics = Statistics.getInstance();
		statistics.setStartupDateTime(new Timestamp(Calendar.getInstance().getTime().getTime()));

		// load servers
		ServerLoader.load(configFileName);
		// load services
		ServiceLoader.load(configFileName);
		// load cache manager
		appContext.getCacheManager().initialize();

		// clean up and initialize cache
		// Cache cache = Cache.initialize();

		// create configured responders / listeners
		List<CommunicatorConfig> responderList = configuration.getResponderConfigList();
		for (CommunicatorConfig respConfig : responderList) {
			IResponder responder = new Responder(respConfig);
			responder.create();
			logger.info("Start listener " + respConfig.getCommunicatorName() + " on " + respConfig.getHost() + ":"
					+ respConfig.getPort());
			responder.startListenAsync();
		}
		if (configuration.writePID()) {
			SC.writePIDFile();
		}
		logger.log(Level.OFF, "Service Connector is running ...");
	}

	/**
	 * Initialize java management interface stuff.
	 */
	private static void initializeJMX() throws Exception {
		AppContext appContext = AppContext.getCurrentContext();
		// Necessary to make access for JMX client available
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName mxbeanNameSessReg = new ObjectName("org.serviceconnector.registry:type=SessionRegistry");
		ObjectName mxbeanNameServiceReg = new ObjectName("org.serviceconnector.registry:type=ServiceRegistry");
		ObjectName mxbeanNameServerReg = new ObjectName("org.serviceconnector.registry:type=ServerRegistry");
		ObjectName mxbeanNameLoggingManager = new ObjectName("org.serviceconnector.logging:type=LoggingManager");

		// Register the Queue Sampler MXBean
		mbs.registerMBean(appContext.getSessionRegistry(), mxbeanNameSessReg);
		mbs.registerMBean(appContext.getServiceRegistry(), mxbeanNameServiceReg);
		mbs.registerMBean(appContext.getServerRegistry(), mxbeanNameServerReg);
		ILoggingManagerMXBean loggingManager = new JMXLoggingManager();
		mbs.registerMBean(loggingManager, mxbeanNameLoggingManager);
	}

	/**
	 * Writes a file. PID of SC gets written in. Is used for testing purpose to verify that SC is running properly.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static void writePIDFile() throws Exception {
		String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
		long pid = Long.parseLong(processName.split("@")[0]);
		FileWriter fw = null;
		try {
			Category rootLogger = logger.getParent();
			Enumeration<?> appenders = rootLogger.getAllAppenders();
			FileAppender fileAppender = null;
			while (appenders.hasMoreElements()) {
				Appender appender = (Appender) appenders.nextElement();
				if (appender instanceof FileAppender) {
					fileAppender = (FileAppender) appender;
					break;
				}
			}
			String fileName = fileAppender.getFile();
			String path = fileName.substring(0, fileName.lastIndexOf("/"));

			File pidFile = new File(path + "/pid.log");
			fw = new FileWriter(pidFile);
			fw.write("pid: " + pid);
			fw.flush();
			fw.close();
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
	}

	/**
	 * Write system info to log.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	private static void writeSystemInfoToLog() throws Exception {
		logger.log(Level.OFF, " ");
		logger.log(Level.OFF, "Service Connector " + SCVersion.CURRENT.toString() + " starting ...");
		logger.log(Level.OFF, "SC configuration: " + SystemInfo.getConfigFileName());
		logger.log(Level.OFF, "Java version: " + SystemInfo.getJavaVersion());
		logger.log(Level.OFF, "VM version: " + SystemInfo.getVmVersion());
		logger.log(Level.OFF, "Local host: " + SystemInfo.getLocalHostId());
		logger.log(Level.OFF, "OS: " + SystemInfo.getOs());
		logger.log(Level.OFF, "OS patch level: " + SystemInfo.getOsPatchLevel());
		logger.log(Level.OFF, "CPU type: " + SystemInfo.getCpuType());
		logger.log(Level.OFF, "User dir: " + SystemInfo.getUserDir());
		logger.log(Level.OFF, "Country setting: " + SystemInfo.getCountrySetting());
		logger.log(Level.OFF, "User timezone: " + SystemInfo.getUserTimezone());
		logger.log(Level.OFF, "UTC Offset: " + SystemInfo.getUtcOffset());
		logger.log(Level.OFF, "Local date: " + SystemInfo.getLocalDate());
		logger.log(Level.OFF, "Available processors: " + SystemInfo.getAvailableProcessors());
		logger.log(Level.OFF, "Max memory: " + SystemInfo.getMaxMemory());
		logger.log(Level.OFF, "Free memory: " + SystemInfo.getFreeMemory());
		logger.log(Level.OFF, "Total memory: " + SystemInfo.getTotalMemory());
		logger.log(Level.OFF, "Available disk memory: " + SystemInfo.getAvailableDiskSpace());
	}
}
