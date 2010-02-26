package com.stabilit.sc.app.client;

import java.io.IOException;
import java.net.URL;

import com.stabilit.sc.msg.IMessage;

public interface IClient {

	public String getSessionId();
	
	public void setEndpoint(URL url);
	
	public void connect() throws Exception;

	public void openSession() throws IOException;

	public IMessage sendAndReceive(IMessage job) throws Exception;

	public IMessage receive(ISubscribe subscribeJob) throws Exception;

	public void closeSession() throws IOException;

	public void disconnect() throws Exception;
	
	public void destroy() throws Exception;

}
