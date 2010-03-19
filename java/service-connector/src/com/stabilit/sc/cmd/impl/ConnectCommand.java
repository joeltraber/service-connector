package com.stabilit.sc.cmd.impl;

import java.net.SocketAddress;
import java.util.Map;

import com.stabilit.sc.cmd.CommandAdapter;
import com.stabilit.sc.cmd.CommandException;
import com.stabilit.sc.cmd.ICommandValidator;
import com.stabilit.sc.cmd.SCMPCommandException;
import com.stabilit.sc.cmd.SCMPValidatorException;
import com.stabilit.sc.ctx.IRequestContext;
import com.stabilit.sc.factory.IFactoryable;
import com.stabilit.sc.io.IRequest;
import com.stabilit.sc.io.IResponse;
import com.stabilit.sc.io.SCMP;
import com.stabilit.sc.io.SCMPErrorCode;
import com.stabilit.sc.io.SCMPFault;
import com.stabilit.sc.io.SCMPHeaderType;
import com.stabilit.sc.io.SCMPMsgType;
import com.stabilit.sc.io.SCMPReply;
import com.stabilit.sc.registry.ConnectionRegistry;
import com.stabilit.sc.util.Converter;
import com.stabilit.sc.util.DateTime;
import com.stabilit.sc.util.MapBean;

public class ConnectCommand extends CommandAdapter {

	public ConnectCommand() {
		this.commandValidator = new ConnectCommandValidator();
	}

	@Override
	public SCMPMsgType getKey() {
		return SCMPMsgType.CONNECT;
	}

	@Override
	public ICommandValidator getCommandValidator() {
		return super.getCommandValidator();
	}

	@Override
	public void run(IRequest request, IResponse response) throws CommandException {
		IRequestContext requestContext = request.getContext();
		SocketAddress socketAddress = requestContext.getSocketAddress();
		ConnectionRegistry connectionRegistry = ConnectionRegistry.getCurrentInstance();
		// TODO is socketAddress the right thing to save a a unique key?
		
		MapBean<?> mapBean = connectionRegistry.get(socketAddress);
		
		if(mapBean == null) {
			throw new SCMPCommandException(SCMPErrorCode.ALREADY_CONNECTED);		
		}
		connectionRegistry.add(socketAddress, request.getAttributeMapBean());

		SCMPReply scmpReply = new SCMPReply();
		scmpReply.setMessageType(SCMPMsgType.CONNECT.getResponseName());
		scmpReply.setLocalDateTime();
		response.setSCMP(scmpReply);
	}

	@Override
	public IFactoryable newInstance() {
		return this;
	}

	public class ConnectCommandValidator implements ICommandValidator {

		@Override
		public void validate(IRequest request, IResponse response) throws SCMPValidatorException {
			SCMP scmp = request.getSCMP();
			// transform localDateTime into localDateTimeObject
			// Date localDateTimeObject = ???

			Map<String, String> scmpHeader = scmp.getHeader();
			try {
				Integer keepAliveTimeout = Converter.getUnsignedInteger(scmpHeader, "keepAliveTimeout", 0);
				request.setAttribute("keepAliveTimeout", keepAliveTimeout);
				Integer keepAliveInterval = Converter.getUnsignedInteger(scmpHeader, "keepAliveInterval", 0);
				request.setAttribute("keepAliveInterval", keepAliveInterval);
			} catch (Exception e) {
				SCMPValidatorException validatorException =  new SCMPValidatorException();
				validatorException.setMessageType(SCMPMsgType.CONNECT.getResponseName());
				throw validatorException;
			}
		}
	}

}
