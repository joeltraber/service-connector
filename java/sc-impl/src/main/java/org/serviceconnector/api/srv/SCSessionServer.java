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
package org.serviceconnector.api.srv;

import java.security.InvalidParameterException;

import javax.activity.InvalidActivityException;

import org.apache.log4j.Logger;
import org.serviceconnector.Constants;
import org.serviceconnector.call.SCMPCallFactory;
import org.serviceconnector.call.SCMPDeRegisterServerCall;
import org.serviceconnector.call.SCMPRegisterServerCall;
import org.serviceconnector.cmd.CommandFactory;
import org.serviceconnector.cmd.srv.ServerCommandFactory;
import org.serviceconnector.conf.CommunicatorConfig;
import org.serviceconnector.net.connection.ConnectionPool;
import org.serviceconnector.net.req.IRequester;
import org.serviceconnector.net.req.RequesterContext;
import org.serviceconnector.net.req.SCRequester;
import org.serviceconnector.net.res.IResponder;
import org.serviceconnector.net.res.Responder;
import org.serviceconnector.scmp.ISCMPSynchronousCallback;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMessageId;
import org.serviceconnector.service.SCServiceException;
import org.serviceconnector.util.SynchronousCallback;
import org.serviceconnector.util.ValidatorUtility;

/**
 * The Class SCServer. Basic class for any kind of a server which communicates with an SC.
 * 
 * @author JTraber
 */
public class SCSessionServer implements ISCSessionServer {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SCSessionServer.class);

	/** Identifies low level component to use for communication default for severs is {netty.tcp}. */
	private String conType;
	/** The srv service registry. */
	protected SrvServiceRegistry srvServiceRegistry;
	/** The message id. */
	private SCMPMessageId msgId;
	/** The server listening state. */
	private boolean listening;
	/** The responder. */
	private IResponder responder;
	// fields for register server
	protected ISCMPSynchronousCallback callback;
	/** The immediate connect. */
	private boolean immediateConnect;
	/** The keep alive interval. */
	private int keepAliveIntervalInSeconds;
	/** The local server host. */
	private String localServerHost;
	/** The local server port. */
	private int localServerPort;

	public SCSessionServer() {
		this.listening = false;
		this.conType = Constants.DEFAULT_SERVER_CON;
		// attributes for registerServer
		this.immediateConnect = true;
		this.keepAliveIntervalInSeconds = Constants.DEFAULT_KEEP_ALIVE_INTERVAL;
		this.localServerHost = null;
		this.localServerPort = -1;
		this.responder = null;
		this.msgId = new SCMPMessageId();
		this.srvServiceRegistry = SrvServiceRegistry.getCurrentInstance();
		this.callback = new SrvServerCallback();

		CommandFactory commandFactory = CommandFactory.getCurrentCommandFactory();
		if (commandFactory == null) {
			CommandFactory.setCurrentCommandFactory(new ServerCommandFactory());
		}
	}

	/** {@inheritDoc} */
	@Override
	public int getKeepAliveIntervalInSeconds() {
		return this.keepAliveIntervalInSeconds;
	}

	/** {@inheritDoc} */
	@Override
	public int getMaxSessions(String serviceName) {
		return this.srvServiceRegistry.getSrvService(serviceName).getMaxSessions();
	}

	/** {@inheritDoc} */
	@Override
	public int getMaxConnections(String serviceName) {
		return this.srvServiceRegistry.getSrvService(serviceName).getMaxConnections();
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void registerServer(String scHost, int scPort, String serviceName, int maxSessions,
			int maxConnections, ISCServerCallback scCallback) throws Exception {
		if (this.listening == false) {
			throw new InvalidActivityException("listener should first be started before register service is allowed.");
		}
		if (scHost == null) {
			throw new InvalidParameterException("host must be set.");
		}
		ValidatorUtility.validateInt(0, scPort, 0xFFFF, SCMPError.HV_WRONG_PORTNR);
		ValidatorUtility.validateStringLength(1, serviceName, 32, SCMPError.HV_WRONG_SERVICE_NAME);
		ValidatorUtility.validateAllowedCharacters(serviceName, SCMPError.HV_WRONG_SERVICE_NAME);
		ValidatorUtility.validateInt(1, maxSessions, SCMPError.HV_WRONG_MAX_SESSIONS);
		ValidatorUtility.validateInt(1, maxConnections, 1024, SCMPError.HV_WRONG_MAX_SESSIONS);
		ValidatorUtility.validateInt(1, maxConnections, maxSessions, SCMPError.HV_WRONG_MAX_SESSIONS);
		if (scCallback == null) {
			throw new InvalidParameterException("callback must be set");
		}
		// register called first time - initialize connection pool & requester
		ConnectionPool connectionPool = new ConnectionPool(scHost, scPort, this.conType,
				this.keepAliveIntervalInSeconds);
		// register server only needs one connection
		connectionPool.setMaxConnections(1);
		IRequester requester = new SCRequester(new RequesterContext(connectionPool, this.msgId));

		SCMPRegisterServerCall registerServerCall = (SCMPRegisterServerCall) SCMPCallFactory.REGISTER_SERVER_CALL
				.newInstance(requester, serviceName);

		registerServerCall.setMaxSessions(maxSessions);
		registerServerCall.setMaxConnections(maxConnections);
		registerServerCall.setPortNumber(this.localServerPort);
		registerServerCall.setImmediateConnect(this.immediateConnect);
		registerServerCall.setKeepAliveInterval(this.keepAliveIntervalInSeconds);
		try {
			registerServerCall.invoke(callback, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS
					* Constants.SEC_TO_MILLISEC_FACTOR);
		} catch (Exception e) {
			connectionPool.destroy();
			throw new SCServiceException("register server failed", e);
		}
		SCMPMessage reply = this.callback.getMessageSync();
		if (reply.isFault()) {
			connectionPool.destroy();
			throw new SCServiceException("register server failed : "
					+ reply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
		}
		// creating srvService & adding to registry
		SrvService srvService = new SrvService(serviceName, maxSessions, maxConnections, requester, scCallback);
		this.srvServiceRegistry.addSrvService(serviceName, srvService);
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void deregisterServer(String serviceName) throws Exception {
		if (this.srvServiceRegistry.containsKey(serviceName) == false) {
			// sc server not registered - deregister not necessary
			return;
		}
		IRequester req = null;
		try {
			// remove srvService from registry
			SrvService srvService = this.srvServiceRegistry.removeSrvService(serviceName);
			req = srvService.getRequester();
			SCMPDeRegisterServerCall deRegisterServerCall = (SCMPDeRegisterServerCall) SCMPCallFactory.DEREGISTER_SERVER_CALL
					.newInstance(req, serviceName);
			try {
				deRegisterServerCall.invoke(this.callback, Constants.DEFAULT_OPERATION_TIMEOUT_SECONDS
						* Constants.SEC_TO_MILLISEC_FACTOR);
			} catch (Exception e) {
				throw new SCServiceException("deregister server failed", e);
			}
			SCMPMessage reply = this.callback.getMessageSync();
			if (reply.isFault()) {
				throw new SCServiceException("deregister server failed : "
						+ reply.getHeader(SCMPHeaderAttributeKey.SC_ERROR_TEXT));
			}
		} finally {
			// destroy connection pool
			req.getContext().getConnectionPool().destroy();
		}
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void startListener(String host, int port, int keepAliveIntervalInSeconds) throws Exception {
		if (this.listening == true) {
			throw new InvalidActivityException("listener is already started not allowed to start again.");
		}
		CommunicatorConfig respConfig = new CommunicatorConfig(SCSessionServer.class.getSimpleName());
		respConfig.setConnectionType(this.conType);

		if (host == null) {
			throw new InvalidParameterException("host must be set.");
		}
		ValidatorUtility.validateInt(0, port, 0xFFFF, SCMPError.HV_WRONG_PORTNR);
		ValidatorUtility.validateInt(0, keepAliveIntervalInSeconds, 3600, SCMPError.HV_WRONG_KEEPALIVE_INTERVAL);

		this.keepAliveIntervalInSeconds = keepAliveIntervalInSeconds;
		this.localServerHost = host;
		this.localServerPort = port;
		respConfig.setHost(host);
		respConfig.setPort(port);

		responder = new Responder(respConfig);
		try {
			responder.create();
			responder.startListenAsync();
		} catch (Exception ex) {
			this.keepAliveIntervalInSeconds = 0;
			this.localServerHost = null;
			this.localServerPort = 0;
			this.listening = false;
			logger.error("startListener", ex);
			throw ex;
		}
		this.listening = true;
	}

	/** {@inheritDoc} */
	@Override
	public void destroyServer() {
		if (this.listening == false) {
			// server is not listening
			return;

		}
		this.listening = false;
		this.responder.stopListening();
		this.responder.destroy();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isListening() {
		return this.listening;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isRegistered(String serviceName) {
		return this.srvServiceRegistry.containsKey(serviceName);
	}

	/** {@inheritDoc} */
	@Override
	public void setImmediateConnect(boolean immediateConnect) {
		this.immediateConnect = immediateConnect;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isImmediateConnect() {
		return this.immediateConnect;
	}

	/** {@inheritDoc} */
	@Override
	public String getSCHost(String serviceName) {
		return this.srvServiceRegistry.getSrvService(serviceName).getRequester().getContext().getConnectionPool()
				.getHost();
	}

	/** {@inheritDoc} */
	@Override
	public int getSCPort(String serviceName) {
		return this.srvServiceRegistry.getSrvService(serviceName).getRequester().getContext().getConnectionPool()
				.getPort();
	}

	/** {@inheritDoc} */
	@Override
	public String getHost() {
		return this.localServerHost;
	}

	/** {@inheritDoc} */
	@Override
	public int getPort() {
		return this.localServerPort;
	}

	/**
	 * Gets the connection type. Default {netty.tcp}
	 * 
	 * @return the connection type in use
	 */
	@Override
	public String getConnectionType() {
		return this.conType;
	}

	/**
	 * Sets the connection type. Should only be used if you really need to change low level technology careful.
	 * 
	 * @param conType
	 *            the new connection type, identifies low level communication technology
	 */
	public void setConnectionType(String conType) {
		this.conType = conType;
	}

	/**
	 * The Class SrvServerCallback.
	 */
	protected class SrvServerCallback extends SynchronousCallback {
		// nothing to implement in this case - everything is done by super-class
	}
}
