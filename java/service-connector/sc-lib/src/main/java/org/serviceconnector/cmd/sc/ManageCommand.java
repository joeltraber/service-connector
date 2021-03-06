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
package org.serviceconnector.cmd.sc;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.serviceconnector.Constants;
import org.serviceconnector.cache.SCCache;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.req.IRequest;
import org.serviceconnector.net.res.IResponderCallback;
import org.serviceconnector.net.res.IResponse;
import org.serviceconnector.scmp.HasFaultResponseException;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMessageFault;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.service.Service;
import org.serviceconnector.util.URLString;
import org.serviceconnector.util.ValidatorUtility;

/**
 * The Class ManageCommand. Responsible for validation and execution of manage command. Manage command is used to enable/disable services.
 *
 * @author JTraber
 */
public class ManageCommand extends CommandAdapter {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ManageCommand.class);

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getKey() {
		return SCMPMsgType.MANAGE;
	}

	/** {@inheritDoc} */
	@Override
	public void run(IRequest request, IResponse response, IResponderCallback responderCallback) throws Exception {
		SCMPMessage reqMsg = request.getMessage();
		String bodyString = (String) reqMsg.getBody();
		String ipAddress = reqMsg.getHeader(SCMPHeaderAttributeKey.IP_ADDRESS_LIST);

		URLString urlRequestString = new URLString();
		urlRequestString.parseRequestURLString(bodyString);
		String callKey = urlRequestString.getCallKey();

		// set up response - SCMP Version request
		SCMPMessage scmpReply = new SCMPMessage(reqMsg.getSCMPVersion());
		scmpReply.setIsReply(true);
		scmpReply.setMessageType(getKey());
		InetAddress localHost = InetAddress.getLocalHost();
		scmpReply.setHeader(SCMPHeaderAttributeKey.IP_ADDRESS_LIST, localHost.getHostAddress());

		String serviceName = urlRequestString.getParamValue(Constants.SERVICE_NAME);

		// kill command
		if ((ipAddress.equals(localHost.getHostAddress())) && Constants.CC_CMD_KILL.equalsIgnoreCase(callKey)) {
			// kill request is allowed from localhost only!
			LOGGER.info("SC stopped by kill console command");
			response.setSCMP(scmpReply);
			responderCallback.responseCallback(request, response);
			// wait 300 milliseconds until the response has been sent
			Thread.sleep(300);
			System.exit(0);
		}

		// other commands
		if (Constants.CC_CMD_DUMP.equalsIgnoreCase(callKey)) {
			AppContext.dump();
			response.setSCMP(scmpReply);
			responderCallback.responseCallback(request, response);
			return;
		}

		if (Constants.CC_CMD_CLEAR_CACHE.equalsIgnoreCase(callKey)) {
			SCCache cache = AppContext.getSCCache();
			cache.clearAll();
			response.setSCMP(scmpReply);
			responderCallback.responseCallback(request, response);
			return;
		}

		if (Constants.CC_CMD_ENABLE.equalsIgnoreCase(callKey)) {
			// enable services
			boolean success = this.modifyStateOfServices(true, serviceName);
			if (success == false) {
				LOGGER.debug("service=" + serviceName + " not found");
				// SCMP Version request
				scmpReply = new SCMPMessageFault(reqMsg.getSCMPVersion(), SCMPError.SERVICE_NOT_FOUND, serviceName);
			}
			response.setSCMP(scmpReply);
			responderCallback.responseCallback(request, response);
			return;
		}

		if (Constants.CC_CMD_DISABLE.equalsIgnoreCase(callKey)) {
			// enable services
			boolean success = this.modifyStateOfServices(false, serviceName);
			if (success == false) {
				LOGGER.debug("service=" + serviceName + " not found");
				// SCMP Version request
				scmpReply = new SCMPMessageFault(reqMsg.getSCMPVersion(), SCMPError.SERVICE_NOT_FOUND, serviceName);
			}
			response.setSCMP(scmpReply);
			responderCallback.responseCallback(request, response);
			return;
		}
		LOGGER.error("wrong manage command body=" + bodyString); // body has bad syntax
		// SCMP Version request
		scmpReply = new SCMPMessageFault(reqMsg.getSCMPVersion(), SCMPError.V_WRONG_MANAGE_COMMAND, bodyString);
		response.setSCMP(scmpReply);
		// initiate responder to send reply
		responderCallback.responseCallback(request, response);
	}

	/**
	 * Modify state of all services.
	 *
	 * @param enable the enable
	 * @param serviceNameRegex the service name regex
	 * @return true, if successful
	 */
	private boolean modifyStateOfServices(boolean enable, String serviceNameRegex) {
		boolean ret = false;
		Service[] services = this.serviceRegistry.getServices();
		try {
			for (Service service : services) {
				if (service.getName().matches(serviceNameRegex)) {
					LOGGER.info("set service=" + service.getName() + " state enable=" + enable);
					service.setEnabled(enable);
					ret = true;
				}
			}
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public void validate(IRequest request) throws Exception {
		try {
			SCMPMessage message = request.getMessage();
			// ipAddressList mandatory
			String ipAddressList = message.getHeader(SCMPHeaderAttributeKey.IP_ADDRESS_LIST);
			ValidatorUtility.validateIpAddressList(ipAddressList);
		} catch (HasFaultResponseException ex) {
			// needs to set message type at this point
			ex.setMessageType(getKey());
			throw ex;
		} catch (Throwable th) {
			LOGGER.error("validation error", th);
			SCMPValidatorException validatorException = new SCMPValidatorException();
			validatorException.setMessageType(getKey());
			throw validatorException;
		}
	}
}
