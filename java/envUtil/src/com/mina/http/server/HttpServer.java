/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package com.mina.http.server;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.mina.common.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

public class HttpServer {
	/** Choose your favorite port number. */
	private static final int PORT = 1234;
	
	public static final String VERSION_STRING = "$Revision: 555855 $ $Date: 2007-07-13 12:19:00 +0900 (Fri, 13 Jul 2007) $";

	public static void main(String[] args) throws Exception {
		IoAcceptor acceptor = new SocketAcceptor();
		SocketAcceptorConfig config = new SocketAcceptorConfig();

		config.setReuseAddress(true);
		config.getFilterChain().addLast("protocolFilter",
				new ProtocolCodecFilter(new HttpServerProtocolCodecFactory()));
	//	config.getFilterChain().addLast("logger", new LoggingFilter());

		// Bind
		acceptor.getFilterChain().addLast("executor", new ExecutorFilter(new ThreadPoolExecutor(20,25,1000,TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>())));
		acceptor.bind(new InetSocketAddress("127.0.0.1", PORT), new HttpServerHandler(), config);

		System.out.println("Listening on port " + PORT);
	}
}
