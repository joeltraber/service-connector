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
package org.serviceconnector.net.req.netty;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.Timer;

/**
 * @author JTraber
 */
public class NettyIdleTimeoutHandler extends IdleStateHandler {

	/** The Constant LOGGER. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyIdleTimeoutHandler.class);

	/**
	 * Instantiates a new NETTY idle timeout handler.
	 *
	 * @param timer the timer
	 * @param readerIdleTime the reader idle time
	 * @param writerIdleTime the writer idle time
	 * @param allIdleTime the all idle time
	 * @param unit the unit
	 */
	public NettyIdleTimeoutHandler(Timer timer, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
		super(timer, readerIdleTime, writerIdleTime, allIdleTime, unit);
	}

	@Override
	protected void channelIdle(ChannelHandlerContext ctx, IdleState state, long lastActivityTimeMillis) throws Exception {
		super.channelIdle(ctx, state, lastActivityTimeMillis);

		switch (state) {
			case WRITER_IDLE:
				// ignore writer idle
				return;
			case READER_IDLE:
				// ignore reader idle
				return;
			case ALL_IDLE:
				Channels.fireExceptionCaught(ctx, new IdleTimeoutException("idle timeout. operation - could not be completed."));
				break;
			default:
				break;
		}
	}
}
