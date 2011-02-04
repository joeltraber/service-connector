/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright � 2010 STABILIT Informatik AG, Switzerland                  *
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

import java.io.ByteArrayInputStream;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.serviceconnector.ctx.AppContext;
import org.serviceconnector.log.ConnectionLogger;
import org.serviceconnector.net.IEncoderDecoder;
import org.serviceconnector.net.req.netty.IdleTimeoutException;
import org.serviceconnector.scmp.ISCMPMessageCallback;
import org.serviceconnector.scmp.SCMPMessage;
import org.serviceconnector.util.Statistics;

/**
 * The Class NettyTcpRequesterResponseHandler.
 * 
 * @author JTraber
 */
public class NettyTcpRequesterResponseHandler extends SimpleChannelUpstreamHandler {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(NettyTcpRequesterResponseHandler.class);
	/** The scmp callback. */
	private ISCMPMessageCallback scmpCallback;
	/** The pending request. */
	private volatile boolean pendingRequest;

	/**
	 * Instantiates a new netty tcp requester response handler.
	 */
	public NettyTcpRequesterResponseHandler() {
		this.scmpCallback = null;
		this.pendingRequest = false;
	}

	/**
	 * Sets the callback.
	 * 
	 * @param callback
	 *            the new callback
	 */
	public void setCallback(ISCMPMessageCallback callback) {
		this.scmpCallback = callback;
		this.pendingRequest = true;
	}

	/** {@inheritDoc} */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (this.pendingRequest) {
			this.pendingRequest = false;
			// set up responderRequestHandlerTask to take care of the request
			NettyTcpRequesterResponseHandlerTask responseHandlerTask = new NettyTcpRequesterResponseHandlerTask((ChannelBuffer) e
					.getMessage());
			AppContext.getExecutor().submit(responseHandlerTask);
			return;
		}
		// unsolicited input, message not expected - race condition
		logger.error("unsolicited input, message not expected, no reply was outstanding!");
	}

	/** {@inheritDoc} */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Throwable th = e.getCause();
		if (th instanceof Exception) {
			Exception ex = (Exception) th;
			if (this.pendingRequest) {
				this.pendingRequest = false;
				NettyTcpRequesterErrorHandlerTask errorHandler = new NettyTcpRequesterErrorHandlerTask(ex);
				AppContext.getExecutor().submit(errorHandler);
				return;
			}
			if (ex instanceof IdleTimeoutException) {
				// idle timed out no pending request outstanding - ignore exception
				return;
			}
		}
		if (th instanceof java.io.IOException) {
			logger.warn(th); // regular disconnect causes this expected exception
		} else {
			logger.error("Response error", th);
		}
	}

	/**
	 * The Class NettyTcpRequesterResponseHandlerTask. Is responsible for processing a response. It has to be a new thread because
	 * of NETTY threading concept.
	 * A worker thread owns a channel pipeline. If block the thread nothing will be sent on that channel.
	 * More information about this issue: http://www.jboss.org/netty/community.html#nabble-td5441049
	 */
	private class NettyTcpRequesterResponseHandlerTask implements Runnable {

		private ChannelBuffer channelBuffer;

		public NettyTcpRequesterResponseHandlerTask(ChannelBuffer channelBuffer) {
			this.channelBuffer = channelBuffer;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			SCMPMessage ret = null;
			try {
				byte[] buffer = new byte[channelBuffer.readableBytes()];
				channelBuffer.readBytes(buffer);
				Statistics.getInstance().incrementTotalMessages(buffer.length);
				if (ConnectionLogger.isEnabledFull()) {
					ConnectionLogger.logReadBuffer(this.getClass().getSimpleName(), "", -1, buffer, 0, buffer.length);
				}
				ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
				IEncoderDecoder encoderDecoder = AppContext.getEncoderDecoderFactory().createEncoderDecoder(buffer);
				ret = (SCMPMessage) encoderDecoder.decode(bais);
				NettyTcpRequesterResponseHandler.this.scmpCallback.receive(ret);
			} catch (Throwable th) {
				logger.error("receive message", th);
				if ((th instanceof Exception) == false) {
					try {
						NettyTcpRequesterResponseHandler.this.scmpCallback.receive((Exception) th);
					} catch (Throwable th1) {
						logger.error("receive exception", th);
					}
				}
			}
		}
	}

	private class NettyTcpRequesterErrorHandlerTask implements Runnable {

		private Exception exception;

		public NettyTcpRequesterErrorHandlerTask(Exception exception) {
			this.exception = exception;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			logger.error("receive exception", exception);
			try {
				NettyTcpRequesterResponseHandler.this.scmpCallback.receive(exception);
			} catch (Throwable th) {
				logger.error("receive exception", th);
			}
		}
	}
}