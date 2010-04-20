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
import com.stabilit.sc.common.io.SCMP;
import com.stabilit.sc.common.io.SCMPHeaderAttributeKey;
import com.stabilit.sc.common.io.SCMPMsgType;
import com.stabilit.sc.common.util.DateTimeUtility;

/**
 * @author JTraber
 * 
 */
public class SCMPConnectCall extends SCMPCallAdapter {

	public SCMPConnectCall() {
		this(null);
	}

	public SCMPConnectCall(IClient client) {
		this.client = client;
	}

	@Override
	public SCMP invoke() throws Exception {
		this.setLocalDateTime(DateTimeUtility.getCurrentTimeZoneMillis());
		super.invoke();
		return this.result;
	}

	@Override
	public ISCMPCall newInstance(IClient client) {
		return new SCMPConnectCall(client);
	}

	public void setVersion(String version) {
		call.setHeader(SCMPHeaderAttributeKey.SC_VERSION, version);
	}

	public void setCompression(boolean compression) {
		call.setHeader(SCMPHeaderAttributeKey.COMPRESSION, compression);
	}

	private void setLocalDateTime(String localDateTime) {
		call.setHeader(SCMPHeaderAttributeKey.LOCAL_DATE_TIME, localDateTime);
	}
	
	public void setKeepAliveTimeout(int keepAliveTimeout) {
		call.setHeader(SCMPHeaderAttributeKey.KEEP_ALIVE_TIMEOUT, keepAliveTimeout);
	}

	public void setKeepAliveInterval(int keepAliveInterval) {
		call.setHeader(SCMPHeaderAttributeKey.KEEP_ALIVE_INTERVAL, keepAliveInterval);
	}
	
	@Override
	public SCMPMsgType getMessageType() {
		return SCMPMsgType.CONNECT;
	}
}
