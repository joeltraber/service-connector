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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.util.Timer;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.net.connection.ConnectionContext;
import org.serviceconnector.net.req.netty.NettyIdleHandler;
import org.serviceconnector.net.res.netty.NettySCMPFrameDecoder;

/**
 * A factory for creating NettyTcpRequesterPipelineFactory objects.
 *
 * @author JTraber
 */
public class NettyTcpRequesterPipelineFactory implements ChannelPipelineFactory {

	/** The Constant LOGGER. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyTcpRequesterPipelineFactory.class);

	/** The timer to observe timeouts. */
	private Timer timer;
	/** The context. */
	private ConnectionContext context;

	/**
	 * Instantiates a new NettyTcpRequesterPipelineFactory.
	 *
	 * @param context the context
	 * @param timer the timer
	 */
	public NettyTcpRequesterPipelineFactory(ConnectionContext context, Timer timer) {
		this.timer = timer;
		this.context = context;
	}

	/** {@inheritDoc} */
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		// logging handler
		pipeline.addLast("LOGGER", new LoggingHandler());
		// responsible for observing idle timeout - Netty
		pipeline.addLast("idleTimeout", new NettyIdleHandler(this.context, this.timer, 0, 0, this.context.getIdleTimeoutSeconds()));
		// responsible for reading until SCMP frame is complete
		pipeline.addLast("framer", new NettySCMPFrameDecoder());
		// executer to run NettyTcpRequesterResponseHandler in own thread
		pipeline.addLast("executor", new ExecutionHandler(AppContext.getSCWorkerThreadPool()));
		// responsible for handle response - Stabilit
		pipeline.addLast("requesterResponseHandler", new NettyTcpRequesterResponseHandler());
		return pipeline;
	}
}
