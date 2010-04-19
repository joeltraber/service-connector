package com.stabilit.sc.common.listener;

import java.util.EventListener;

public interface IConnectionListener extends EventListener {

	public void writeEvent(ConnectionEvent connectionEvent) throws Exception;
	
	public void readEvent(ConnectionEvent connectionEvent) throws Exception;

	public void connectEvent(ConnectionEvent connectionEvent) throws Exception;

	public void disconnectEvent(ConnectionEvent connectionEvent) throws Exception;
}
