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
package com.stabilit.scm.common.net;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import com.stabilit.scm.common.factory.IFactoryable;

/**
 * The Interface IEncoderDecoder. Abstracts EncoderDecoder implementations.
 */
public interface IEncoderDecoder extends IFactoryable {

	/** The Constant UNESCAPED_EQUAL_SIGN_REGEX. */
	public static final String UNESCAPED_EQUAL_SIGN_REGEX = "(.*)(?<!\\\\)=(.*)";
	/** The Constant ESCAPED_EQUAL_SIGN. */
	public static final String ESCAPED_EQUAL_SIGN = "\\=";
	/** The Constant EQUAL_SIGN. */
	public static final String EQUAL_SIGN = "=";
	/** The Constant CHARSET. */
	public static final String CHARSET = "ISO-8859-1"; // TODO ISO 8859-1 (Latin 1) gem�ss doc
	/** The Constant DECODE_REG. */
	public static final Pattern DECODE_REG = Pattern.compile(UNESCAPED_EQUAL_SIGN_REGEX);

	/**
	 * Encode object to output stream.
	 * 
	 * @param os
	 *            the os to fill
	 * @param obj
	 *            the obj to encode
	 * @throws Exception
	 *             the exception
	 */
	public void encode(OutputStream os, Object obj) throws Exception;

	/**
	 * Decode input stream.
	 * 
	 * @param is
	 *            the is to decode
	 * @return the object decoded
	 * @throws Exception
	 *             the exception
	 */
	public Object decode(InputStream is) throws Exception;
}
