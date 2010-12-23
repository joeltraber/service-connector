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
package org.serviceconnector.cmd.srv;

import org.apache.log4j.Logger;
import org.serviceconnector.api.SCMessage;
import org.serviceconnector.api.srv.SrvSessionService;
import org.serviceconnector.cmd.SCMPValidatorException;
import org.serviceconnector.scmp.HasFaultResponseException;
import org.serviceconnector.scmp.IRequest;
import org.serviceconnector.scmp.IResponse;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPHeaderAttributeKey;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.scmp.SCMPMessageSequenceNr;
import org.serviceconnector.scmp.SCMPMsgType;
import org.serviceconnector.util.ValidatorUtility;

/**
 * The Class SrvExecuteCommand. Responsible for validation and execution of server execute command.
 * 
 * @author JTraber
 */
public class SrvExecuteCommand extends SrvCommandAdapter {

	/** The Constant logger. */
	protected final static Logger logger = Logger.getLogger(SrvExecuteCommand.class);

	/**
	 * Instantiates a new SrvExecuteCommand.
	 */
	public SrvExecuteCommand() {
	}

	/** {@inheritDoc} */
	@Override
	public SCMPMsgType getKey() {
		return SCMPMsgType.SRV_EXECUTE;
	}

	/** {@inheritDoc} */
	@Override
	public void run(IRequest request, IResponse response) throws Exception {
		SCMPMessage reqMessage = request.getMessage();
		String serviceName = reqMessage.getServiceName();
		// look up srvService
		SrvSessionService srvService = this.getSrvSessionServiceByServiceName(serviceName);

		// create scMessage
		SCMessage scMessage = new SCMessage();
		scMessage.setData(reqMessage.getBody());
		scMessage.setCompressed(reqMessage.getHeaderFlag(SCMPHeaderAttributeKey.COMPRESSION));
		scMessage.setMessageInfo(reqMessage.getHeader(SCMPHeaderAttributeKey.MSG_INFO));
		scMessage.setCacheId(reqMessage.getCacheId());
		scMessage.setServiceName(reqMessage.getServiceName());

		// inform callback with scMessages
		SCMessage scReply = srvService.getCallback().execute(scMessage,
				Integer.parseInt(reqMessage.getHeader(SCMPHeaderAttributeKey.OPERATION_TIMEOUT)));

		// handling msgSequenceNr
		SCMPMessageSequenceNr msgSequenceNr = SrvCommandAdapter.sessionCompositeRegistry.getSCMPMsgSequenceNr(reqMessage
				.getSessionId());
		msgSequenceNr.incrementMsgSequenceNr();
		// set up reply
		SCMPMessage reply = new SCMPMessage();
		reply.setServiceName(serviceName);
		reply.setSessionId(reqMessage.getSessionId());
		reply.setCacheId(reqMessage.getCacheId());
		// set cache expiration, 1 hour
		if (scReply.getCacheExpirationDateTime() != null) {
			reply.setHeader(SCMPHeaderAttributeKey.CACHE_EXPIRATION_DATETIME, scReply.getCacheExpirationDateTime());

		}
		reply.setHeader(SCMPHeaderAttributeKey.MESSAGE_SEQUENCE_NR, msgSequenceNr.getCurrentNr());
		reply.setMessageType(this.getKey());
		if (scReply != null) {
			reply.setBody(scReply.getData());
			if (scReply.getMessageInfo() != null) {
				reply.setHeader(SCMPHeaderAttributeKey.MSG_INFO, scReply.getMessageInfo());
			}
			if (scReply.isCompressed()) {
				reply.setHeaderFlag(SCMPHeaderAttributeKey.COMPRESSION);
			}
			if (scReply.getAppErrorCode() != -1) {
				reply.setHeader(SCMPHeaderAttributeKey.APP_ERROR_CODE, scReply.getAppErrorCode());
			}
			if (scReply.getAppErrorText() != null) {
				reply.setHeader(SCMPHeaderAttributeKey.APP_ERROR_TEXT, scReply.getAppErrorText());
			}
		}
		response.setSCMP(reply);
	}

	/** {@inheritDoc} */
	@Override
	public void validate(IRequest request) throws Exception {
		SCMPMessage message = request.getMessage();

		try {
			// msgSequenceNr mandatory
			String msgSequenceNr = message.getMessageSequenceNr();
			if (msgSequenceNr == null || msgSequenceNr.equals("")) {
				throw new SCMPValidatorException(SCMPError.HV_WRONG_MESSAGE_SEQUENCE_NR, "msgSequenceNr must be set");
			}
			// sessionId mandatory
			String sessionId = message.getSessionId();
			if (sessionId == null || sessionId.equals("")) {
				throw new SCMPValidatorException(SCMPError.HV_WRONG_SESSION_ID, "sessionId must be set");
			}
			// operation timeout mandatory
			String otiValue = message.getHeader(SCMPHeaderAttributeKey.OPERATION_TIMEOUT.getValue());
			ValidatorUtility.validateInt(10, otiValue, 3600000, SCMPError.HV_WRONG_OPERATION_TIMEOUT);
			// serviceName mandatory
			String serviceName = message.getServiceName();
			if (serviceName == null || serviceName.equals("")) {
				throw new SCMPValidatorException(SCMPError.HV_WRONG_SERVICE_NAME, "serviceName must be set");
			}
			// message info optional
			String messageInfo = message.getHeader(SCMPHeaderAttributeKey.MSG_INFO.getValue());
			ValidatorUtility.validateStringLengthIgnoreNull(1, messageInfo, 256, SCMPError.HV_WRONG_MESSAGE_INFO);
		} catch (HasFaultResponseException ex) {
			// needs to set message type at this point
			ex.setMessageType(getKey());
			throw ex;
		} catch (Throwable th) {
			logger.error("validation error", th);
			SCMPValidatorException validatorException = new SCMPValidatorException();
			validatorException.setMessageType(getKey());
			throw validatorException;
		}
	}
}
