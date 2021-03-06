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
package org.serviceconnector.net.res.netty;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.serviceconnector.log.ConnectionLogger;
import org.serviceconnector.net.res.ResponseAdapter;

/**
 * The Class NettyHttpResponse is responsible for writing a response to a ChannelBuffer. Encodes scmp to a Http frame. Based on JBoss Netty.
 */
public class NettyHttpResponse extends ResponseAdapter {

	/** The Constant LOGGER. */
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyHttpResponse.class);

	/**
	 * Instantiates a new netty http response.
	 *
	 * @param channel the communication channel
	 */
	public NettyHttpResponse(Channel channel) {
		super(channel);
	}

	/** {@inheritDoc} */
	@Override
	public void write() throws Exception {
		// Build the response object.
		HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		ChannelBuffer buffer = getBuffer();
		httpResponse.setContent(buffer);
		httpResponse.headers().add("Access-Control-Allow-Origin", "*");
		httpResponse.headers().add(HttpHeaders.Names.CONTENT_TYPE, scmp.getBodyType().getMimeType());
		httpResponse.headers().add(HttpHeaders.Names.CACHE_CONTROL, HttpHeaders.Values.NO_CACHE);
		httpResponse.headers().add(HttpHeaders.Names.PRAGMA, HttpHeaders.Values.NO_CACHE);
		httpResponse.headers().set(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buffer.readableBytes()));
		// Write the response.
		channel.write(httpResponse);
		if (ConnectionLogger.isEnabledFull()) {
			ConnectionLogger.logWriteBuffer(this.getClass().getSimpleName(), ((InetSocketAddress) this.channel.getRemoteAddress()).getHostName(),
					((InetSocketAddress) this.channel.getRemoteAddress()).getPort(), buffer.toByteBuffer().array(), 0, buffer.toByteBuffer().array().length);
		}
	}
}
