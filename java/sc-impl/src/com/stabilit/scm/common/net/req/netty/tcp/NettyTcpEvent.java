/*
 *-----------------------------------------------------------------------------*
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
 *-----------------------------------------------------------------------------*
/*
/**
 * 
 */
package com.stabilit.scm.common.net.req.netty.tcp;

import org.jboss.netty.buffer.ChannelBuffer;

import com.stabilit.scm.common.net.req.netty.NettyEvent;

/**
 * The Class NettyTcpEvent. Wraps a successful response of Netty framework. Used to unify the process of catching
 * the response synchronously.
 * 
 * @author JTraber
 */
public class NettyTcpEvent extends NettyEvent {

	/** The buffer. */
	private ChannelBuffer response;

	/**
	 * Instantiates a NettyTcpEvent.
	 * 
	 * @param buffer
	 *            the buffer
	 */
	public NettyTcpEvent(ChannelBuffer buffer) {
		this.response = buffer;
	}

	/** {@inheritDoc} */
	@Override
	public Object getResponse() {
		return response;
	}
}
