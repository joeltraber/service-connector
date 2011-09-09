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
package org.serviceconnector.net.res.netty;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.serviceconnector.Constants;
import org.serviceconnector.net.SCMPFrameDecoder;
import org.serviceconnector.scmp.SCMPError;

/**
 * The Class NettySCMPFrameDecoder. Decodes a SCMP frame.
 * 
 * @author JTraber
 */
public class NettySCMPFrameDecoder extends FrameDecoder {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(NettySCMPFrameDecoder.class);
	/** The scmp frame size. */
	private int scmpFrameSize;

	/**
	 * Instantiates a new NettySCMPFrameDecoder.
	 */
	public NettySCMPFrameDecoder() {
		this.scmpFrameSize = 0;
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		if (this.scmpFrameSize != 0) {
			// headline and frame size has already been decoded
			return this.aggregateFrame(buffer);
		} else {
			// try reading headline & extracting frame size
			this.decodeFrameSizeFromHeadline(buffer);
			if (scmpFrameSize != 0) {
				// headline decoded try aggregate whole frame
				return this.aggregateFrame(buffer);
			}
		}
		return null;
	}

	private void decodeFrameSizeFromHeadline(ChannelBuffer buffer) throws SCMPFrameDecoderException {
		if (buffer.readableBytes() < Constants.SCMP_HEADLINE_SIZE) {
			// not enough bytes in buffer to decode the SCMP headline
			return;
		}
		try {
			byte[] headline = new byte[Constants.SCMP_HEADLINE_SIZE];
			buffer.getBytes(0, headline);
			// parse headline
			scmpFrameSize = SCMPFrameDecoder.parseFrameSize(headline);
		} catch (Exception ex) {
			LOGGER.warn("decode " + ex.getMessage());
			throw new SCMPFrameDecoderException(SCMPError.FRAME_DECODER);
		}
	}

	private ChannelBuffer aggregateFrame(ChannelBuffer buffer) {
		if (buffer.readableBytes() < scmpFrameSize) {
			return null;
		}
		ChannelBuffer channelBuffer = buffer.readBytes(scmpFrameSize);
		// reset the frame size
		this.scmpFrameSize = 0;
		return channelBuffer;
	}
}