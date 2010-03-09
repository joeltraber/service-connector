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
package com.stabilit.sc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.stabilit.sc.app.client.IConnection;
import com.stabilit.sc.service.ServiceCtx;
import com.stabilit.sc.util.SubscribePublishQueue;

/**
 * @author JTraber
 * 
 */
public class SC {

	private static SC instance = new SC();
	private Map<String, ServiceCtx> services;
	private SubscribePublishQueue subPubQueue;

	private SC() {
		services = new ConcurrentHashMap<String, ServiceCtx>();
		subPubQueue = new SubscribePublishQueue();
		// starts subscribe publish list
		// TODO thread irgendwo wieder beenden. oder so
		Thread subPubListThread = new Thread(subPubQueue);
		subPubListThread.start();
	}

	public static SC getInstance() {
		return instance;
	}

	public void registerService(String serviceName, IConnection conn) {
		// TODO achtung doppelte register??
		ServiceCtx serviceCtx = new ServiceCtx(serviceName, conn);
		services.put(serviceName, serviceCtx);
	}

	public ServiceCtx getService(String serviceName) {
		return services.get(serviceName);
	}

	public Map<String, ServiceCtx> getServiceList() {
		return services;
	}

	public SubscribePublishQueue getSubPubQueue() {
		return subPubQueue;
	}
}
