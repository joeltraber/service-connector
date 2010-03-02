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
package com.stabilit.sc.serviceserver;

import com.stabilit.sc.context.ClientApplicationContext;
import com.stabilit.sc.exception.ScConnectionException;
import com.stabilit.sc.msg.ISCClientListener;
import com.stabilit.sc.pool.ConnectionPool;
import com.stabilit.sc.pool.IPoolConnection;
import com.stabilit.sc.service.ConnectionCtx;
import com.stabilit.sc.service.ServiceCtx;

/**
 * @author JTraber
 *
 */
public class ServiceServer {
	
	protected ConnectionPool pool;
	
	/** The response handler. */
	protected Class<? extends ISCClientListener> scListenerClass;
	
	/** The connection context. */
	private ConnectionCtx connectionCtx;

	/** The service context. */
	private ServiceCtx serviceCtx;
	
	protected ClientApplicationContext ctx;

	private String serviceName;
	
	protected ServiceServer(String serviceName, Class<? extends ISCClientListener> serviceHandler, ClientApplicationContext ctx) {
		this.serviceCtx = new ServiceCtx(serviceName);
		this.ctx = ctx;
		this.scListenerClass = serviceHandler;
		ConnectionPool.init(1);
		this.pool = ConnectionPool.getInstance();
	}

	public void connect(int timeout, ConnectionCtx connectionCtx) throws ScConnectionException {
		this.connectionCtx = connectionCtx;
		IPoolConnection conn = pool.borrowConnection(ctx, scListenerClass);
		conn.releaseConnection();
	}

	public String getServiceName() {
		return serviceName;
	}
}
