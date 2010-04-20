/*
 *-----------------------------------------------------------------------------*
 *                            Copyright � 2010 by                              *
 *                    STABILIT Informatik AG, Switzerland                      *
 *                            ALL RIGHTS RESERVED                              *
 *                                                                             *
 * Valid license from STABILIT is required for possession, use or copying.     *
 * This software or any other copies thereof may not be provided or otherwise  *
 * made available to any other person. No title to and ownership of the        *
 * software is hereby transferred. The information in this software is subject *
 * to change without notice and should not be construed as a commitment by     *
 * STABILIT Informatik AG.                                                     *
 *                                                                             *
 * All referenced products are trademarks of their respective owners.          *
 *-----------------------------------------------------------------------------*
 */
/**
 * 
 */
package com.stabilit.sc.cln.service;

import com.stabilit.sc.cln.client.IClient;
import com.stabilit.sc.common.io.SCMPHeaderAttributeKey;
import com.stabilit.sc.common.io.SCMPMsgType;

/**
 * @author JTraber
 * 
 */
public class SCMPRegisterServiceCall extends SCMPCallAdapter {
	
	public SCMPRegisterServiceCall() {
		this(null);
	}

	public SCMPRegisterServiceCall(IClient client) {
		this.client = client;
	}

	@Override
	public ISCMPCall newInstance(IClient client) {
		return new SCMPRegisterServiceCall(client);
	}
	
	public void setServiceName(String serviceName) {
		call.setHeader(SCMPHeaderAttributeKey.SERVICE_NAME, serviceName);
	}
	
	public void setMaxSessions(int maxSessions) {
		call.setHeader(SCMPHeaderAttributeKey.MAX_SESSIONS, maxSessions);
	}
	
	public void setMultithreaded(boolean multiThreaded) {
		call.setHeader(SCMPHeaderAttributeKey.MULTI_THREADED, multiThreaded);
	}
	
	public void setPortNumber(int portNumber) {
		call.setHeader(SCMPHeaderAttributeKey.PORT_NR, portNumber);
	}	
	
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.REGISTER_SERVICE;
	}
}
