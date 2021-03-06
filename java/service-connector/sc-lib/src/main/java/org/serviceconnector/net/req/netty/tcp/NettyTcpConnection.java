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
package org.serviceconnector.net.req.netty.tcp;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.util.Timer;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.log.ConnectionLogger;
import org.serviceconnector.net.CommunicationException;
import org.serviceconnector.net.SCMPCommunicationException;
import org.serviceconnector.net.req.netty.NettyConnectionAdpater;
import org.serviceconnector.net.req.netty.NettyOperationListener;
import org.serviceconnector.scmp.ISCMPMessageCallback;
import org.serviceconnector.scmp.SCMPError;
import org.serviceconnector.scmp.SCMPMessage;

/**
 * The Class NettyTcpConnection. Concrete connection implementation with JBoss Netty for Tcp.
 */
public class NettyTcpConnection extends NettyConnectionAdpater {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyTcpConnection.class);

	/**
	 * Instantiates a new NettyTcpConnection.
	 *
	 * @param channelFactory the channel factory
	 * @param timer the timer
	 */
	public NettyTcpConnection(NioClientSocketChannelFactory channelFactory, Timer timer) {
		super(channelFactory, timer);

	}

	/** {@inheritDoc} */
	@Override
	public void connect() throws Exception {
		this.bootstrap = new ClientBootstrap(NettyConnectionAdpater.channelFactory);
		this.pipelineFactory = new NettyTcpRequesterPipelineFactory(this.connectionContext, NettyConnectionAdpater.timer);
		this.bootstrap.setPipelineFactory(this.pipelineFactory);
		this.bootstrap.setOption("connectTimeoutMillis", baseConf.getConnectionTimeoutMillis());
		this.bootstrap.setOption("tcpNoDelay", true);
		if (baseConf.getTcpKeepAliveInitiator() != null) {
			// TCP keep alive is configured - set it!
			this.bootstrap.setOption("keepAlive", baseConf.getTcpKeepAliveInitiator());
		}
		// Start the connection attempt.
		this.remotSocketAddress = new InetSocketAddress(host, port);
		ChannelFuture future = bootstrap.connect(this.remotSocketAddress);
		operationListener = new NettyOperationListener();
		future.addListener(operationListener);
		try {
			this.channel = operationListener.awaitUninterruptibly(baseConf.getConnectionTimeoutMillis()).getChannel();
			// complete remotSocketAddress
			this.remotSocketAddress = (InetSocketAddress) this.channel.getRemoteAddress();
		} catch (CommunicationException ex) {
			LOGGER.error("connect failed to " + this.remotSocketAddress.toString(), ex);
			throw new SCMPCommunicationException(SCMPError.CONNECTION_EXCEPTION, "connect to IP=" + this.remotSocketAddress.toString());
		}
		if (ConnectionLogger.isEnabled()) {
			ConnectionLogger.logConnect(this.getClass().getSimpleName(), this.remotSocketAddress.getHostName(), this.remotSocketAddress.getPort());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void send(SCMPMessage scmp, ISCMPMessageCallback callback) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		encoderDecoder = AppContext.getEncoderDecoderFactory().createEncoderDecoder(scmp);
		encoderDecoder.encode(baos, scmp);

		NettyTcpRequesterResponseHandler handler = channel.getPipeline().get(NettyTcpRequesterResponseHandler.class);
		handler.setCallback(callback);

		ChannelBuffer chBuffer = ChannelBuffers.buffer(baos.size());
		chBuffer.writeBytes(baos.toByteArray());
		channel.write(chBuffer);
		if (ConnectionLogger.isEnabledFull()) {
			ConnectionLogger.logWriteBuffer(this.getClass().getSimpleName(), this.remotSocketAddress.getHostName(), this.remotSocketAddress.getPort(),
					chBuffer.toByteBuffer().array(), 0, chBuffer.toByteBuffer().array().length);
		}
	}

	@Override
	public void setQuietDisconnect() throws Exception {
		// this avoids receiving messages (outstanding replies) in disconnecting procedure
		NettyTcpRequesterResponseHandler handler = channel.getPipeline().get(NettyTcpRequesterResponseHandler.class);
		handler.connectionDisconnect();
	}
}
