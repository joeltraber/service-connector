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
package com.stabilit.scm.common.net.res.nio;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.stabilit.scm.common.net.EncoderDecoderFactory;
import com.stabilit.scm.listener.ConnectionPoint;
import com.stabilit.scm.net.IEncoderDecoder;
import com.stabilit.scm.scmp.ResponseAdapter;
import com.stabilit.scm.scmp.SCMPMessage;

/**
 * The Class NioTcpResponse is responsible for writing a response to a socketChannel. Encodes scmp to a Tcp frame.
 * Based on Nio.
 */
public class NioTcpResponse extends ResponseAdapter {

	/** The socket channel. */
	private SocketChannel socketChannel;
	/** The encoder decoder. */
	private IEncoderDecoder encoderDecoder;

	/**
	 * Instantiates a new nio tcp response.
	 * 
	 * @param socketChannel
	 *            the socket channel
	 */
	public NioTcpResponse(SocketChannel socketChannel) {
		this.scmp = null;
		this.socketChannel = socketChannel;
	}

	/**
	 * Gets the buffer. Encodes the scmp.
	 * 
	 * @return the buffer
	 * @throws Exception
	 *             the exception
	 */
	public byte[] getBuffer() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		this.encoderDecoder = EncoderDecoderFactory.getCurrentEncoderDecoderFactory().newInstance(this.scmp);
		encoderDecoder.encode(baos, scmp);
		baos.close();
		byte[] buf = baos.toByteArray();
		return buf;
	}

	/** {@inheritDoc} */
	@Override
	public void setSCMP(SCMPMessage scmp) {
		if (scmp == null) {
			return;
		}
		scmp.setIsReply(true);
		this.scmp = scmp;
	}

	/** {@inheritDoc} */
	@Override
	public void write() throws Exception {
		byte[] byteWriteBuffer = this.getBuffer();
		ByteBuffer buffer = ByteBuffer.wrap(byteWriteBuffer);
		ConnectionPoint.getInstance().fireWrite(this, this.socketChannel.socket().getLocalPort(),
				byteWriteBuffer);
		this.socketChannel.write(buffer);
	}
}
