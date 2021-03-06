/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright © 2010 STABILIT Informatik AG, Switzerland                  *
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

package org.serviceconnector.web.xml;

import java.io.File;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;

import org.serviceconnector.Constants;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.api.cln.SCFileService;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.registry.ServiceRegistry;
import org.serviceconnector.service.CascadedFileService;
import org.serviceconnector.service.FileService;
import org.serviceconnector.service.Service;
import org.serviceconnector.util.DateTimeUtility;
import org.serviceconnector.util.DumpUtility;
import org.serviceconnector.util.FileUtility;
import org.serviceconnector.util.SystemInfo;
import org.serviceconnector.web.IWebRequest;
import org.serviceconnector.web.WebUtil;
import org.serviceconnector.web.cmd.XMLLoaderFactory;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

/**
 * The Class AjaxMaintenanceXMLLoader.
 */
public class AjaxMaintenanceXMLLoader extends AbstractXMLLoader {

	/** {@inheritDoc} */
	@Override
	public boolean isText() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void loadBody(XMLStreamWriter writer, IWebRequest request) throws Exception {
		String action = request.getParameter("action");
		writer.writeStartElement("date");
		Date current = this.writeXMLDate(writer, request);
		writer.writeEndElement();
		if (action == null) {
			throw new InvalidParameterException("action parameter missing");
		}
		if ("sc_property_download".equals(action)) {
			loadPropertyDownloadBody(writer, request);
			return;
		}
		if ("sc_logs_upload".equals(action)) {
			loadLogfileUploadBody(writer, request, current);
			return;
		}
		if ("sc_dump_list".equals(action)) {
			loadDumpListBody(writer, request, null); // load all dumps
			return;
		}
		throw new InvalidParameterException("action parameter is invalid or unknown (action=" + action + ")");
	}

	/**
	 * load body data for property files download action.
	 *
	 * @param writer the writer
	 * @param request the request
	 * @throws Exception the exception
	 */
	private void loadPropertyDownloadBody(XMLStreamWriter writer, IWebRequest request) throws Exception {
		String serviceName = request.getParameter("service");
		if (serviceName == null) {
			throw new InvalidParameterException("service parameter missing");
		}
		// load file services and the file list
		ServiceRegistry serviceRegistry = AppContext.getServiceRegistry();
		writer.writeStartElement("service");
		Service service = serviceRegistry.getService(serviceName);
		this.writeBean(writer, service);
		if (service instanceof FileService || service instanceof CascadedFileService) {
			SCClient client = null;
			// try to connect client
			client = connectClientToService(service);
			SCFileService scFileService = client.newFileService(serviceName);
			List<String> fileList = scFileService.listFiles();
			writer.writeStartElement("files");
			for (String fileName : fileList) {
				writer.writeStartElement("file");
				writer.writeCData(fileName);
				writer.writeEndElement();
			}
			writer.writeEndElement();
			if (client != null) {
				client.detach();
			}
		}
		writer.writeEndElement(); // close service tag
		// load current configuration directory
		String configFileName = SystemInfo.getConfigFileName();
		URL resourceURL = WebUtil.getResourceURL(configFileName);
		if (resourceURL != null) {
			writer.writeStartElement("resource");
			writer.writeStartElement("url");
			writer.writeCData(resourceURL.toString());
			writer.writeEndElement(); // close url tag
			File file = new File(resourceURL.getFile());
			String parent = file.getParent();
			if (parent != null) {
				File parentFile = new File(parent);
				File[] files = parentFile.listFiles();
				if (files != null) {
					writer.writeStartElement("files");
					for (File file2 : files) {
						if (file2.isFile()) {
							writer.writeStartElement("file");
							String name = file2.getName();
							if (name != null) {
								writer.writeCData(name);
							}
							writer.writeEndElement(); // close file tag
						}
					}
					writer.writeEndElement(); // close files tag
				}
			}
			writer.writeEndElement(); // close resource tag
		}
	}

	/**
	 * load body data for logs file upload action.
	 *
	 * @param writer the writer
	 * @param request the request
	 * @throws Exception the exception
	 */
	private void loadLogfileUploadBody(XMLStreamWriter writer, IWebRequest request, Date current) throws Exception {
		String serviceName = request.getParameter("service");
		if (serviceName == null) {
			throw new InvalidParameterException("service parameter missing");
		}
		// load file services and the file list
		ServiceRegistry serviceRegistry = AppContext.getServiceRegistry();
		writer.writeStartElement("service");
		Service service = serviceRegistry.getService(serviceName);
		this.writeBean(writer, service);
		if (service instanceof FileService || service instanceof CascadedFileService) {
			SCClient client = null;
			// try to connect client
			client = connectClientToService(service);
			SCFileService scFileService = client.newFileService(serviceName);
			List<String> fileList = scFileService.listFiles();
			writer.writeStartElement("files");
			for (String fileName : fileList) {
				if (fileName.startsWith(Constants.LOGS_FILE_NAME)) {
					writer.writeStartElement("file");
					writer.writeCData(fileName);
					writer.writeEndElement();
				}
			}
			writer.writeEndElement();
			if (client != null) {
				client.detach();
			}
		}
		writer.writeEndElement(); // close service tag
		// get logs xml loader from factory
		LogsXMLLoader logsXMLLoader = (LogsXMLLoader) XMLLoaderFactory.getXMLLoader("/logs");
		// load available logs file list for current date (today)
		writer.writeStartElement("logs");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date today = cal.getTime();
		if (current == null) {
			current = today;
		}
		ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		logsXMLLoader.writeLogger(writer, rootLogger, today, current);
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		List<ch.qos.logback.classic.Logger> currentLoggers = loggerContext.getLoggerList();
		for (ch.qos.logback.classic.Logger currentLogger : currentLoggers) {
			Iterator<Appender<ILoggingEvent>> appenders = currentLogger.iteratorForAppenders();
			if (appenders.hasNext()) {
				logsXMLLoader.writeLogger(writer, currentLogger, today, current);
			}
		}
		writer.writeEndElement(); // close logs tag
		// load available dump files for current date
		this.loadDumpListBody(writer, request, current);
	}

	/**
	 * load body data for dump list action. if date is not null, then load dumps for given date only
	 *
	 * @param writer the writer
	 * @param request the request
	 * @throws Exception the exception
	 */
	private void loadDumpListBody(XMLStreamWriter writer, IWebRequest request, Date date) throws Exception {
		String dumpPath = AppContext.getBasicConfiguration().getDumpPath();
		File[] files = DumpUtility.getDumpFiles(dumpPath);
		writer.writeStartElement("dumplist");
		writer.writeStartElement("path");
		if (dumpPath != null) {
			writer.writeCData(dumpPath);
		}
		writer.writeEndElement(); // close path tag
		if (files != null) {
			writer.writeStartElement("files");
			for (File file : files) {
				if (file.isFile()) {
					// check if file belongs to current date
					if (date != null) {
						if (FileUtility.belongsToDate(file, date) == false) {
							continue;
						}
					}
					writer.writeStartElement("file");
					writer.writeStartElement("name");
					String name = file.getName();
					if (name != null) {
						writer.writeCData(name);
					}
					writer.writeEndElement(); // close name tag
					writer.writeStartElement("length");
					writer.writeCData(String.valueOf(file.length()));
					writer.writeEndElement(); // close length tag
					writer.writeStartElement("lastModified");
					Date lastModifiedDate = new Date(file.lastModified());
					writer.writeCData(DateTimeUtility.getDateTimeAsString(lastModifiedDate));
					writer.writeEndElement(); // close last modified tag
					writer.writeEndElement(); // close file tag
				}
			}
			writer.writeEndElement(); // close files tag
		}
		writer.writeEndElement(); // close dumplist tag

		return;
	}

}
