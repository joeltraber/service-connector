/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.stabilit.sc.app.client.netty.tcp;

import java.io.ByteArrayInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.stabilit.sc.io.EncoderDecoderFactory;
import com.stabilit.sc.io.IEncoderDecoder;
import com.stabilit.sc.io.SCMP;
import com.stabilit.sc.msg.IClientListener;
import com.stabilit.sc.pool.IPoolConnection;

@ChannelPipelineCoverage("one")
public class NettyTcpClientResponseHandler extends SimpleChannelUpstreamHandler {

	private final BlockingQueue<ChannelBuffer> answer = new LinkedBlockingQueue<ChannelBuffer>();

	private IClientListener callback = null;
	private IPoolConnection conn;
	private boolean sync = false;
	private IEncoderDecoder encoderDecoder = EncoderDecoderFactory.newInstance();

	/**
	 * @param scListener
	 * @param conn
	 */
	public NettyTcpClientResponseHandler(IClientListener scListener, IPoolConnection conn) {
		this.callback = scListener;
		this.conn = conn;
	}

	public void setCallback(IClientListener callback) {
		this.callback = callback;
	}

	public IClientListener getCallback() {
		return callback;
	}

	public ChannelBuffer getMessageSync() {
		sync = true;
		ChannelBuffer response;
		boolean interrupted = false;
		for (;;) {
			try {
				// take() wartet bis Message in Queue kommt!
				response = answer.take();
				sync = false;
				break;
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}

		if (interrupted) {
			Thread.currentThread().interrupt();
		}
		return response;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		ChannelBuffer chBuffer = (ChannelBuffer) e.getMessage();
		if (sync) {
			answer.offer(chBuffer);
		} else {
			byte[] buffer = chBuffer.array();
			ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
			SCMP ret = new SCMP();
			encoderDecoder.decode(bais, ret);
			this.callback.messageReceived(conn, ret);
		}
	}
}
