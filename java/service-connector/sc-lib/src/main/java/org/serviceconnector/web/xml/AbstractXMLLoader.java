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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.serviceconnector.Constants;
import org.serviceconnector.SCVersion;
import org.serviceconnector.api.cln.SCClient;
import org.serviceconnector.conf.ListenerConfiguration;
import org.serviceconnector.conf.RemoteNodeConfiguration;
import org.serviceconnector.conf.WebConfiguration;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.ConnectionType;
import org.serviceconnector.net.connection.ConnectionContext;
import org.serviceconnector.net.connection.ConnectionPool;
import org.serviceconnector.net.connection.IConnection;
import org.serviceconnector.net.req.IRequester;
import org.serviceconnector.net.res.IResponder;
import org.serviceconnector.net.res.ResponderRegistry;
import org.serviceconnector.registry.PublishMessageQueue;
import org.serviceconnector.server.CascadedSC;
import org.serviceconnector.server.Server;
import org.serviceconnector.server.StatefulServer;
import org.serviceconnector.service.CascadedFileService;
import org.serviceconnector.service.FileService;
import org.serviceconnector.service.Service;
import org.serviceconnector.service.SubscriptionMask;
import org.serviceconnector.util.DateTimeUtility;
import org.serviceconnector.util.Statistics;
import org.serviceconnector.util.SystemInfo;
import org.serviceconnector.web.IWebRequest;
import org.serviceconnector.web.WebSession;
import org.serviceconnector.web.WebUtil;
import org.serviceconnector.web.cmd.WebCommandException;
import org.serviceconnector.web.ctx.WebContext;

/**
 * The Class AbstractXMLLoader.
 */
public abstract class AbstractXMLLoader implements IXMLLoader {

	/** The Constant XMLSDF. */
	public static final SimpleDateFormat XMLSDF = new SimpleDateFormat("yyyy-MM-dd");

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractXMLLoader.class);

	/** The meta map. */
	private Map<String, String> metaMap;

	/** The meta map list. */
	private List<Map<String, String>> metaMapList;

	/**
	 * Instantiates a new abstract xml loader.
	 */
	public AbstractXMLLoader() {
		this.metaMap = new HashMap<String, String>();
		this.metaMapList = new ArrayList<Map<String, String>>();
	}

	/**
	 * Checks if is text.
	 *
	 * @return true, if is text {@inheritDoc}
	 */
	@Override
	public boolean isText() {
		return false;
	}

	/**
	 * Adds the meta.
	 *
	 * @param name the name
	 * @param value the value {@inheritDoc}
	 */
	@Override
	public void addMeta(String name, String value) {
		this.metaMap.put(name, value);
	}

	/**
	 * Adds the meta.
	 *
	 * @param map the map {@inheritDoc}
	 */
	@Override
	public void addMeta(Map<String, String> map) {
		this.metaMapList.add(map);
	}

	/**
	 * Load body.
	 *
	 * @param writer the writer
	 * @param request the request
	 * @throws Exception the exception
	 */
	@Override
	public abstract void loadBody(XMLStreamWriter writer, IWebRequest request) throws Exception;

	/**
	 * Load body.
	 *
	 * @param writer the writer
	 * @param request the request
	 * @throws Exception the exception
	 */
	@Override
	public void loadBody(Writer writer, IWebRequest request) throws Exception {

	}

	/**
	 * Load.
	 *
	 * @param request the request
	 * @param os the os
	 * @throws Exception the exception {@inheritDoc}
	 */
	@Override
	public final void load(IWebRequest request, OutputStream os) throws Exception {
		if (this.isText()) {
			OutputStreamWriter writer = new OutputStreamWriter(os);
			this.loadBody(writer, request);
			writer.flush();
			return;

		}
		WebSession webSession = request.getSession(false);
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter writer = factory.createXMLStreamWriter(os);
		writer.writeStartDocument();
		writer.writeStartElement("sc-web");
		writer.writeStartElement("head");
		// write url path
		String url = request.getURL();
		if (url != null) {
			int qPos = url.indexOf("?");
			if (qPos >= 0) {
				url = url.substring(0, qPos);
			}
			// check if ajax content call
			String id = request.getParameter("id");
			if (id != null) {
				url = Constants.SLASH + id;
			}
			writer.writeStartElement("meta");
			writer.writeAttribute("path", url);
			writer.writeEndElement(); // close meta tag
		}
		// write query string
		writer.writeStartElement("meta");
		writer.writeAttribute("creation", DateTimeUtility.getCurrentTimeZoneMillis());
		writer.writeEndElement(); // close meta tag
		// write sc version
		writer.writeStartElement("meta");
		writer.writeAttribute("scversion", SCVersion.CURRENT.toString());
		writer.writeEndElement(); // close meta tag
		WebConfiguration webConfiguration = WebContext.getWebConfiguration();
		// write web color schema
		writer.writeStartElement("meta");
		writer.writeAttribute("colorscheme", webConfiguration.getColorScheme());
		writer.writeEndElement(); // close meta tag
		// write sc header prefix
		writer.writeStartElement("meta");
		writer.writeAttribute("headerprefix", webConfiguration.getPageHeaderPrefix());
		writer.writeEndElement(); // close meta tag
		// write sc status
		writer.writeStartElement("meta");
		writer.writeAttribute("scstatus", "success");
		writer.writeEndElement(); // close meta tag
		// write scconfigfile
		writer.writeStartElement("meta");
		writer.writeAttribute("scconfigfile", SystemInfo.getConfigFileName());
		writer.writeEndElement(); // close meta tag
		// write current ip host
		String hostName = InetAddress.getLocalHost().getHostName();
		writer.writeStartElement("meta");
		writer.writeAttribute("hostname", hostName);
		writer.writeEndElement(); // close meta tag
		if (webSession != null) {
			writer.writeStartElement("meta");
			writer.writeAttribute("jsessionid", webSession.getId());
			writer.writeEndElement(); // close meta tag
			writer.writeStartElement("meta");
			writer.writeAttribute("urlencoded", ";sid=" + webSession.getId());
			writer.writeEndElement(); // close meta tag
		}
		for (Entry<String, String> entry : this.metaMap.entrySet()) {
			writer.writeStartElement("meta");
			writer.writeAttribute(entry.getKey(), entry.getValue());
			writer.writeEndElement(); // close meta tag
		}
		for (Map<String, String> map : this.metaMapList) {
			writer.writeStartElement("meta");
			for (Entry<String, String> entry : map.entrySet()) {
				writer.writeAttribute(entry.getKey(), entry.getValue());
			}
			writer.writeEndElement(); // close meta tag
		}
		// write any query params back
		writer.writeStartElement("query");
		Map<String, List<String>> parameterMap = request.getParameterMap();
		for (Entry<String, List<String>> parameter : parameterMap.entrySet()) {
			String name = parameter.getKey();
			if ("password".equals(name)) {
				continue;
			}
			List<String> values = parameter.getValue();
			for (String value : values) {
				writer.writeStartElement("param");
				writer.writeAttribute(name, value);
				writer.writeEndElement(); // close param
			}
		}
		writer.writeEndElement(); // close query
		writer.writeEndElement(); // close head
		writer.writeStartElement("body");
		this.writeSystem(writer);
		this.loadBody(writer, request);
		writer.writeEndElement(); // close body tag
		writer.writeEndElement(); // close root tag sc-web
		writer.writeEndDocument();
		writer.close();
	}

	/**
	 * Write system.
	 *
	 * @param writer the writer
	 * @throws XMLStreamException the xML stream exception
	 */
	public void writeSystem(XMLStreamWriter writer) throws XMLStreamException {
		// write system info
		SystemInfo systemInfo = new SystemInfo();
		writer.writeStartElement("system");
		writer.writeStartElement("info");
		this.writeBean(writer, systemInfo);
		writer.writeEndElement(); // close info tag
		// write web info
		writer.writeStartElement("webinfo");
		this.writeWebInfo(writer);
		writer.writeEndElement(); // end of web info
		// write runtime info
		writer.writeStartElement("runtime");
		this.writeRuntime(writer);
		writer.writeEndElement(); // end of runtime
		// write statistics info
		writer.writeStartElement("statistics");
		this.writeBean(writer, Statistics.getInstance());
		writer.writeEndElement(); // end of statistics
		Properties properties = System.getProperties();
		writer.writeStartElement("properties");
		for (Entry<Object, Object> entry : properties.entrySet()) {
			String name = (String) entry.getKey();
			String value = (String) entry.getValue();
			writer.writeStartElement(name);
			writer.writeCData(value);
			writer.writeEndElement();
		}
		writer.writeEndElement(); // close properties tag
		writer.writeEndElement(); // close system tag

	}

	/**
	 * Write bean.
	 *
	 * @param writer the writer
	 * @param obj the obj
	 * @throws XMLStreamException the xML stream exception
	 */
	public void writeBean(XMLStreamWriter writer, Object obj) throws XMLStreamException {
		if (obj == null) {
			return;
		}
		Method[] methods = obj.getClass().getMethods();
		Set<String> methodSet = new HashSet<String>();
		for (Method method : methods) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (method.getParameterTypes() == null) {
				continue;
			}
			if (parameterTypes.length > 0) {
				continue;
			}
			String name = method.getName();
			if (methodSet.contains(name)) {
				continue;
			}
			methodSet.add(name);
			if (name.startsWith("get") == false && name.startsWith("is") == false) {
				continue;
			}
			if (name.startsWith("get")) {
				name = String.valueOf(name.charAt(3)).toLowerCase() + name.substring(4);
			} else {
				name = String.valueOf(name.charAt(2)).toLowerCase() + name.substring(3);
			}
			if ("class".equals(name)) {
				continue;
			}
			if ("context".equals(name)) {
				if (obj instanceof IConnection) {
					continue;
				}
			}
			if ("connection".equals(name)) {
				if (obj instanceof ConnectionPool) {
					continue;
				}
			}

			try {
				Object value = null;
				try {
					value = method.invoke(obj);
				} catch (Exception e) {
					System.err.println(e);
				}
				if (value == obj) {
					continue;
				}
				if (value != null) {
					if (value instanceof List<?>) {
						writer.writeStartElement(name);
						List<?> list = (List<?>) value;
						writer.writeAttribute("size", list.size() + "");
						Object[] objects = list.toArray(new Object[0]);
						for (Object object : objects) {
							writer.writeStartElement(object.getClass().getSimpleName().toLowerCase());
							if (object instanceof String) {
								writer.writeCData(object.toString());
							} else {
								this.writeBean(writer, object);
							}
							writer.writeEndElement();
						}
						writer.writeEndElement();
						continue;
					}
					if (value instanceof Service) {
						Service service = (Service) value;
						writer.writeStartElement("service");
						writer.writeStartElement("name");
						writer.writeCData(service.getName());
						writer.writeEndElement();
						writer.writeEndElement();
						continue;
					}
					if (value instanceof Server) {
						writer.writeStartElement(name);
						Server server = (Server) value;
						writer.writeStartElement("serverKey");
						writer.writeCData(server.getServerKey());
						writer.writeEndElement();
						writer.writeStartElement("serviceName");
						if (value instanceof StatefulServer) {
							writer.writeCData(((StatefulServer) server).getServiceName());
						} else {
							writer.writeCData("unknown");
						}
						writer.writeEndElement();
						writer.writeStartElement("host");
						writer.writeCData(server.getHost());
						writer.writeEndElement();
						writer.writeStartElement("port");
						writer.writeCData(String.valueOf(server.getPortNr()));
						writer.writeEndElement();
						writer.writeStartElement("socketAddress");
						writer.writeCData(String.valueOf(server.getSocketAddress()));
						writer.writeEndElement();
						writer.writeEndElement();
						continue;
					}
					if (value instanceof IRequester) {
						writer.writeStartElement(name);
						IRequester requester = (IRequester) value;
						writer.writeStartElement("context");
						this.writeBean(writer, requester.getRemoteNodeConfiguration());
						writer.writeEndElement();
						writer.writeEndElement();
						continue;
					}
					if (value instanceof ConnectionPool) {
						ConnectionPool connectionPool = (ConnectionPool) value;
						writer.writeStartElement("connectionPool");
						this.writeBean(writer, connectionPool);
						writer.writeEndElement();
						continue;
					}
					if (value instanceof ConnectionContext) {
						ConnectionContext connectionContext = (ConnectionContext) value;
						writer.writeStartElement("connectionContext");
						this.writeBean(writer, connectionContext);
						writer.writeEndElement();
						continue;
					}
					if (value instanceof IConnection) {
						IConnection connection = (IConnection) value;
						writer.writeStartElement("connection");
						this.writeBean(writer, connection);
						writer.writeEndElement();
						continue;
					}
					if (value instanceof ListenerConfiguration) {
						ListenerConfiguration listenerConfig = (ListenerConfiguration) value;
						writer.writeStartElement(name);
						this.writeBean(writer, listenerConfig);
						writer.writeEndElement();
						continue;
					}
					if (value instanceof RemoteNodeConfiguration) {
						RemoteNodeConfiguration remoteNodeConfiguration = (RemoteNodeConfiguration) value;
						writer.writeStartElement(name);
						this.writeBean(writer, remoteNodeConfiguration);
						writer.writeEndElement();
						continue;
					}
					if (value instanceof SubscriptionMask) {
						writer.writeStartElement("subscriptionMask");
						SubscriptionMask subscriptionMask = (SubscriptionMask) value;
						writer.writeCData(subscriptionMask.getValue());
						writer.writeEndElement();
						continue;
					}
					if (value instanceof PublishMessageQueue<?>) {
						writer.writeStartElement("subscriptionQueue");
						PublishMessageQueue<?> publishMessageQueue = (PublishMessageQueue<?>) value;
						this.writeBean(writer, publishMessageQueue);
						writer.writeEndElement();
						continue;
					}
					writer.writeStartElement(name);
					if (value instanceof Date) {
						writer.writeCData(DateTimeUtility.getDateTimeAsString((Date) value));
					} else if (value instanceof HashMap) {
						writer.writeAttribute("size", ((HashMap<?, ?>) value).size() + "");
						writer.writeCData(value.toString());
					} else {
						writer.writeCData(value.toString());
					}
					writer.writeEndElement();
				} else {
					writer.writeStartElement(name);
					writer.writeEndElement();
				}
			} catch (Exception e) {
				LOGGER.error("writeObject", e);
			}
		}
	}

	/**
	 * Write header map.
	 *
	 * @param writer the writer
	 * @param map the map
	 * @throws XMLStreamException the XML stream exception
	 */
	public void writeHeaderMap(XMLStreamWriter writer, Map<String, String> map) throws XMLStreamException {
		if (map == null) {
			return;
		}
		for (Entry<String, String> entry : map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			writer.writeStartElement("item");
			writer.writeAttribute("name", key);
			if (value == null) {
				writer.writeCData("true");
			} else {
				writer.writeCData(value);

			}
			writer.writeEndElement(); // end of item
		}
	}

	/**
	 * Write web info.
	 *
	 * @param writer the writer
	 * @throws XMLStreamException the xML stream exception
	 */
	public void writeWebInfo(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("sessions");
		int size = WebContext.getWebSessionRegistry().getSize();
		writer.writeCharacters(String.valueOf(size));
		writer.writeEndElement(); // end of sessions
	}

	/**
	 * Write runtime.
	 *
	 * @param writer the writer
	 * @throws XMLStreamException the xML stream exception
	 */
	public void writeRuntime(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("availableProcessors");
		writer.writeCData(String.valueOf(SystemInfo.getAvailableProcessors()));
		writer.writeEndElement(); // end of availableProcessors
		writer.writeStartElement("totalMemory");
		writer.writeCData(String.valueOf(SystemInfo.getTotalMemory() / 1024 / 1024));
		writer.writeEndElement(); // end of totalMemory
		writer.writeStartElement("freeMemory");
		writer.writeCData(String.valueOf(SystemInfo.getFreeMemory() / 1024 / 1024));
		writer.writeEndElement(); // end of freeMemory
		writer.writeStartElement("maxMemory");
		writer.writeCData(String.valueOf(SystemInfo.getMaxMemory() / 1024 / 1024));
		writer.writeEndElement(); // end of maxMemory
		// get thread info
		writer.writeStartElement("threadCount");
		writer.writeCData(String.valueOf(SystemInfo.getThreadCount()));
		writer.writeEndElement(); // end of threadCount
		writer.writeStartElement("daemonThreadCount");
		writer.writeCData(String.valueOf(SystemInfo.getDaemonThreadCount()));
		writer.writeEndElement(); // end of daemonThreadCount
		writer.writeStartElement("peakThreadCount");
		writer.writeCData(String.valueOf(SystemInfo.getPeakThreadCount()));
		writer.writeEndElement(); // end of peakThreadCount
	}

	/**
	 * Write success to xml stream using given message text.
	 *
	 * @param writer the writer
	 * @param msg the msg
	 * @throws Exception the exception
	 */
	public void writeSuccess(XMLStreamWriter writer, String msg) throws Exception {
		writer.writeStartElement("status");
		writer.writeCharacters("success");
		writer.writeEndElement();
		writer.writeStartElement("messages");
		writer.writeStartElement("message");
		writer.writeCharacters(msg);
		writer.writeEndElement(); // message
		writer.writeEndElement(); // messages

	}

	/**
	 * Write failure to xml stream using given message text.
	 *
	 * @param writer the writer
	 * @param msg the msg
	 * @throws Exception the exception
	 */
	public void writeFailure(XMLStreamWriter writer, String msg) throws Exception {
		writer.writeStartElement("status");
		writer.writeCharacters("failure");
		writer.writeEndElement();
		writer.writeStartElement("messages");
		writer.writeStartElement("message");
		writer.writeCharacters(msg);
		writer.writeEndElement(); // message
		writer.writeEndElement(); // messages
	}

	/**
	 * Connect client to service. In case of FileService get first netty tcp endpoint or netty http endpoint if no netty tcp endpoint is available
	 *
	 * @param service the service
	 * @return the sC mgmt client
	 * @throws WebCommandException the web command exception
	 */
	protected SCClient connectClientToService(Service service) throws WebCommandException {
		ResponderRegistry responderRegistry = AppContext.getResponderRegistry();
		if (service instanceof FileService) {
			// get local service connector listener using sc-tcp
			IResponder responder = responderRegistry.getFirstResponderForConnectionType(ConnectionType.NETTY_TCP);
			if (responder != null) {
				ListenerConfiguration myLocalListenerConfiguration = responder.getListenerConfig();
				List<String> networkInterfaces = myLocalListenerConfiguration.getNetworkInterfaces();
				for (String networkInterface : networkInterfaces) {
					String host = networkInterface;
					int port = myLocalListenerConfiguration.getPort();
					try {
						SCClient localClient = new SCClient(host, port, ConnectionType.NETTY_TCP);
						localClient.attach();
						return localClient;
					} catch (Exception e) {
						LOGGER.warn("upload current log files, connect to network interface " + host + " failed. " + e.toString());
					}
				}
			}
			responder = responderRegistry.getFirstResponderForConnectionType(ConnectionType.NETTY_HTTP);
			if (responder != null) {
				ListenerConfiguration myLocalListenerConfiguration = responder.getListenerConfig();
				List<String> networkInterfaces = myLocalListenerConfiguration.getNetworkInterfaces();
				// no netty tcp endpoint is available, try to get netty http
				for (String networkInterface : networkInterfaces) {
					String host = networkInterface;
					int port = myLocalListenerConfiguration.getPort();
					try {
						SCClient localClient = new SCClient(host, port, ConnectionType.NETTY_HTTP);
						localClient.attach();
						return localClient;
					} catch (Exception e) {
						LOGGER.warn("upload current log files, connect to network interface " + host + " and port " + port + " failed. " + e.toString());
					}
				}
			}
			return null;
		}
		if (service instanceof CascadedFileService) {
			CascadedFileService cascadedFileService = (CascadedFileService) service;
			CascadedSC cascadedSC = cascadedFileService.getCascadedSC();
			String host = cascadedSC.getHost();
			int port = cascadedSC.getPortNr();
			String connectionType = cascadedSC.getConnectionType();
			try {
				SCClient localClient = new SCClient(host, port, ConnectionType.getType(connectionType));
				localClient.attach();
				return localClient;
			} catch (Exception e) {
				LOGGER.warn("upload current log files, connect to network interface " + host + " and port " + port + " failed. " + e.toString());
			}
		}
		return null;
	}

	/**
	 * Gets the parameter int.
	 *
	 * @param request the request
	 * @param name the name
	 * @param defaultValue the default value
	 * @return the parameter int
	 */
	protected int getParameterInt(IWebRequest request, String name, int defaultValue) {
		int value = defaultValue;
		try {
			value = Integer.parseInt(request.getParameter(name));
		} catch (Exception e) {
		}
		return value;
	}

	/**
	 * Write paging attributes.
	 *
	 * @param writer the writer
	 * @param request the request
	 * @param size the size
	 * @return the paging
	 * @throws Exception the exception
	 */
	protected Paging writePagingAttributes(XMLStreamWriter writer, IWebRequest request, int size, String prefix) throws Exception {
		Paging paging = new Paging(size);
		int page = this.getParameterInt(request, prefix + "page", 1);
		int site = this.getParameterInt(request, prefix + "site", 1);
		int lastPage = size / Constants.DEFAULT_WEB_PAGE_SIZE;
		if (size % Constants.DEFAULT_WEB_PAGE_SIZE > 0) {
			lastPage += 1;
		}
		int lastSite = lastPage / Constants.DEFAULT_WEB_SITE_SIZE;
		if (lastPage % Constants.DEFAULT_WEB_SITE_SIZE > 0) {
			lastSite += 1;
		}
		int pageSize = Constants.DEFAULT_WEB_PAGE_SIZE;
		int siteSize = Constants.DEFAULT_WEB_SITE_SIZE;
		// check if paging data is correct for given size, check range
		if (lastPage < page || lastSite < site) {
			page = 1;
			site = 1;
			lastPage = size / Constants.DEFAULT_WEB_PAGE_SIZE;
			if (size % Constants.DEFAULT_WEB_PAGE_SIZE > 0) {
				lastPage += 1;
			}
			lastSite = lastPage / Constants.DEFAULT_WEB_SITE_SIZE;
			if (lastPage % Constants.DEFAULT_WEB_SITE_SIZE > 0) {
				lastSite += 1;
			}
			pageSize = Constants.DEFAULT_WEB_PAGE_SIZE;
			siteSize = Constants.DEFAULT_WEB_SITE_SIZE;
		}
		paging.setPage(page);
		paging.setSite(site);
		paging.setLastPage(lastPage);
		paging.setLastSite(lastSite);
		paging.setPageSize(pageSize);
		paging.setSiteSize(siteSize);
		writer.writeAttribute("size", String.valueOf(size));
		writer.writeAttribute("site", String.valueOf(site));
		writer.writeAttribute("page", String.valueOf(page));
		writer.writeAttribute("lastPage", String.valueOf(lastPage));
		writer.writeAttribute("lastSite", String.valueOf(lastSite));
		writer.writeAttribute("pageSize", String.valueOf(pageSize));
		writer.writeAttribute("siteSize", String.valueOf(siteSize));
		return paging;
	}

	protected Date writeXMLDate(XMLStreamWriter writer, IWebRequest request) throws XMLStreamException {
		String dateParameter = request.getParameter("date");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date today = cal.getTime();
		Date current = today;
		if (dateParameter != null) {
			current = WebUtil.getXMLDateFromString(dateParameter);
		}
		if (today.before(current)) {
			current = today;
		}
		// get previous and next date
		String next = WebUtil.getXMLNextDateAsString(current);
		String previous = WebUtil.getXMLPreviousDateAsString(current);
		// set selected date
		writer.writeAttribute("previous", previous);
		writer.writeAttribute("current", WebUtil.getXMLDateAsString(current));
		if (current.before(today)) {
			writer.writeAttribute("next", next);
		}
		return current;
	}

	/**
	 * The Class Paging for internal use only, holds paging data.
	 */
	protected class Paging {

		/** The size. */
		private int size;

		/** The page. */
		private int page;

		/** The site. */
		private int site;

		/** The last page. */
		private int lastPage;

		/** The last site. */
		private int lastSite;

		/** The page size. */
		private int pageSize;

		/** The site size. */
		private int siteSize;

		/**
		 * Instantiates a new paging.
		 */
		public Paging(int size) {
			this.size = size;
			this.page = 0;
			this.site = 0;
			this.lastPage = 0;
			this.lastSite = 0;
			this.pageSize = 0;
			this.siteSize = 0;
		}

		public int getStartIndex() {
			return (this.page - 1) * Constants.DEFAULT_WEB_PAGE_SIZE;
		}

		public int getEndIndex() {
			int endIndex = this.getStartIndex() + Constants.DEFAULT_WEB_PAGE_SIZE;
			if (endIndex > size) {
				endIndex = size;
			}
			return endIndex;
		}

		/**
		 * Gets the size.
		 *
		 * @return the size
		 */
		public int getSize() {
			return size;
		}

		/**
		 * Sets the size.
		 *
		 * @param size the new size
		 */
		public void setSize(int size) {
			this.size = size;
		}

		/**
		 * Gets the page.
		 *
		 * @return the page
		 */
		public int getPage() {
			return page;
		}

		/**
		 * Sets the page.
		 *
		 * @param page the new page
		 */
		public void setPage(int page) {
			this.page = page;
		}

		/**
		 * Gets the site.
		 *
		 * @return the site
		 */
		public int getSite() {
			return site;
		}

		/**
		 * Sets the site.
		 *
		 * @param site the new site
		 */
		public void setSite(int site) {
			this.site = site;
		}

		/**
		 * Gets the last page.
		 *
		 * @return the last page
		 */
		public int getLastPage() {
			return lastPage;
		}

		/**
		 * Sets the last page.
		 *
		 * @param lastPage the new last page
		 */
		public void setLastPage(int lastPage) {
			this.lastPage = lastPage;
		}

		/**
		 * Gets the last site.
		 *
		 * @return the last site
		 */
		public int getLastSite() {
			return lastSite;
		}

		/**
		 * Sets the last site.
		 *
		 * @param lastSite the new last site
		 */
		public void setLastSite(int lastSite) {
			this.lastSite = lastSite;
		}

		/**
		 * Gets the page size.
		 *
		 * @return the page size
		 */
		public int getPageSize() {
			return pageSize;
		}

		/**
		 * Sets the page size.
		 *
		 * @param pageSize the new page size
		 */
		public void setPageSize(int pageSize) {
			this.pageSize = pageSize;
		}

		/**
		 * Gets the site size.
		 *
		 * @return the site size
		 */
		public int getSiteSize() {
			return siteSize;
		}

		/**
		 * Sets the site size.
		 *
		 * @param siteSize the new site size
		 */
		public void setSiteSize(int siteSize) {
			this.siteSize = siteSize;
		}

	}
}
