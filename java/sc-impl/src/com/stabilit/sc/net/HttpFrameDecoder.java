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
package com.stabilit.sc.net;

import com.stabilit.sc.factory.IFactoryable;

/**
 * The Class HttpFrameDecoder. Decodes a Http frame.
 * 
 * @author JTraber
 */
public class HttpFrameDecoder extends DefaultFrameDecoder {

	/** The Constant CR. */
	private static final byte CR = 13;
	/** The Constant LF. */
	private static final byte LF = 10;

	/**
	 * Instantiates a new http frame decoder.
	 */
	protected HttpFrameDecoder() {
	}

	/** {@inheritDoc} */
	@Override
	public IFactoryable newInstance() {
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public int parseFrameSize(byte[] buffer) throws FrameDecoderException {

		if (buffer == null || buffer.length <= 0) {
			throw new FrameDecoderException("invalid scmp header line");
		}

		int sizeStart = 0;
		int sizeEnd = 0;
		int headerEnd = 0;
		int bytesRead = buffer.length;

		// watch out for Content-Length attribute in http header to evaluate frame size
		// (bytesRead - 3) avoids IndexOutOfBoundException
		label: for (int i = 0; i < (bytesRead - 3); i++) {
			if (buffer[i] == CR && buffer[i + 1] == LF) {
				i += 2;
				if (buffer[i] == CR && buffer[i + 1] == LF) {
					headerEnd = i + 2;
					break label;
				}
				if (buffer[i] == 'C' && buffer[i + 7] == '-' && buffer[i + 8] == 'L' && buffer[i + 14] == ':') {
					sizeStart = i + 16;
					sizeEnd = sizeStart + 1;
					while (sizeEnd < bytesRead) {
						if (buffer[sizeEnd + 1] == CR && buffer[sizeEnd + 2] == LF) {
							break;
						}
						sizeEnd++;
					}
				}
			}
		}
		int contentLength = readInt(buffer, sizeStart, sizeEnd);
		return contentLength + headerEnd;
	}
}
