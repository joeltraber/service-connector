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
package test.stabilit.sc.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.stabilit.sc.net.EncoderDecoderFactory;
import com.stabilit.sc.net.IEncoderDecoder;
import com.stabilit.sc.scmp.SCMPMessage;
import com.stabilit.sc.scmp.SCMPBodyType;
import com.stabilit.sc.scmp.SCMPFault;
import com.stabilit.sc.scmp.SCMPHeaderAttributeKey;
import com.stabilit.sc.scmp.SCMPHeadlineKey;
import com.stabilit.sc.scmp.SCMPMsgType;
import com.stabilit.sc.scmp.SCMPReply;
import com.stabilit.sc.scmp.internal.SCMPPart;

public class LargeMessageEncoderDecoderTest {

	private EncoderDecoderFactory coderFactory = EncoderDecoderFactory.getCurrentEncoderDecoderFactory();
	private SCMPHeadlineKey headKey;
	private SCMPMsgType msgType;
	private SCMPBodyType bodyType;
	private String msgID;
	private String bodyLength;
	private String body;
	private SCMPMessage encodeScmp;

	@Before
	public void setUp() {
		this.headKey = SCMPHeadlineKey.REQ;
		this.msgType = SCMPMsgType.ECHO_SC;
		this.bodyType = SCMPBodyType.binary;
		this.msgID = "1";
		this.bodyLength = "12";
		this.body = "hello world!";

		encodeScmp = new SCMPMessage();
		encodeScmp.setHeader(SCMPHeaderAttributeKey.BODY_TYPE, bodyType.getName());
		encodeScmp.setHeader(SCMPHeaderAttributeKey.MESSAGE_ID, msgID);
		encodeScmp.setHeader(SCMPHeaderAttributeKey.MSG_TYPE, msgType.getRequestName());
		encodeScmp.setBody(body.getBytes());
	}

	@Test
	public void decodeREQTest() {
		String requestString = headKey.name() + " /s=69& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "bodyLength=" + bodyLength + "\n" + "msgType="
				+ msgType.getRequestName() + "\n\n" + body + "\n";

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMP(message);
	}

	@Test
	public void decodeRESTest() {
		headKey = SCMPHeadlineKey.RES;
		String requestString = headKey.name() + " /s=69& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "bodyLength=" + bodyLength + "\n" + "msgType="
				+ msgType.getRequestName() + "\n\n" + body + "\n";

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMP(message);
	}

	@Test
	public void decodeEXCTest() {
		headKey = SCMPHeadlineKey.EXC;
		String requestString = headKey.name() + " /s=69& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "bodyLength=" + bodyLength + "\n" + "msgType="
				+ msgType.getRequestName() + "\n\n" + body + "\n";

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		if (message.isFault() == false)
			Assert.fail("scmp should be of type fault");
		verifySCMP(message);
	}

	@Test
	public void decodeUNDEFTest() {
		String requestString = "garbage /s=69& SCMP/1.0\n" + "bodyType=" + bodyType.getName() + "\n"
				+ "messageID=" + msgID + "\n" + "bodyLength=" + bodyLength + "\n" + "msgType="
				+ msgType.getRequestName() + "\n\n" + body + "\n";

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		try {
			coder.decode(is);
			Assert.fail("Should throw exception");
		} catch (Exception e) {
		}
	}

	@Test
	public void decodePRQTest() {
		headKey = SCMPHeadlineKey.PRQ;
		String requestString = headKey.name() + " /s=69& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "bodyLength=" + bodyLength + "\n" + "msgType="
				+ msgType.getRequestName() + "\n\n" + body + "\n";

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMP(message);
		if (message.isPart() == false)
			Assert.fail("scmp should be of type part");
	}

	@Test
	public void decodePRSTest() {
		headKey = SCMPHeadlineKey.PRS;
		String requestString = headKey.name() + " /s=69& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "bodyLength=" + bodyLength + "\n" + "msgType="
				+ msgType.getRequestName() + "\n\n" + body + "\n";

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMP(message);
		if (message.isPart() == false)
			Assert.fail("scmp should be of type part");
	}

	@Test
	public void decodeBodyTypesTest() {
		String requestString = headKey.name() + " /s=69& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "bodyLength=" + bodyLength + "\n" + "msgType="
				+ msgType.getRequestName() + "\n\n" + body + "\n";

		byte[] buffer = requestString.getBytes();
		InputStream is = new ByteArrayInputStream(buffer);
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		SCMPMessage message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMP(message);

		bodyType = SCMPBodyType.text;
		requestString = headKey.name() + " /s=69& SCMP/1.0\n" + "bodyType=" + bodyType.getName() + "\n"
				+ "messageID=" + msgID + "\n" + "bodyLength=" + bodyLength + "\n" + "msgType="
				+ msgType.getRequestName() + "\n\n" + body + "\n";

		buffer = requestString.getBytes();
		is = new ByteArrayInputStream(buffer);
		coder = coderFactory.newInstance(new SCMPPart());

		message = null;
		try {
			message = (SCMPMessage) coder.decode(is);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		verifySCMPStringBody(message);
	}

	@Test
	public void encodeREQTest() {
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		String expectedString = headKey.name() + " /s=71& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "msgType=" + msgType.getRequestName() + "\n"
				+ "bodyLength=" + bodyLength + "\n\n" + body;

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeScmp);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}

	@Test
	public void encodeRESTest() {
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		this.headKey = SCMPHeadlineKey.RES;

		String expectedString = headKey.name() + " /s=71& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "msgType=" + msgType.getRequestName() + "\n"
				+ "bodyLength=" + bodyLength + "\n\n" + body;

		SCMPMessage encodeRes = new SCMPReply();
		encodeRes.setHeader(encodeScmp);
		encodeRes.setBody(body.getBytes());

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeRes);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}

	@Test
	public void encodeEXCTest() {
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		this.headKey = SCMPHeadlineKey.EXC;
		String expectedString = headKey.name() + " /s=71& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "msgType=" + msgType.getRequestName() + "\n"
				+ "bodyLength=" + bodyLength + "\n\n" + body;

		SCMPMessage encodeExc = new SCMPFault();
		encodeExc.setHeader(encodeScmp);
		encodeExc.setBody(body.getBytes());

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeExc);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}

	@Test
	public void encodePRQTest() {
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		this.headKey = SCMPHeadlineKey.PRQ;
		String expectedString = headKey.name() + " /s=71& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "msgType=" + msgType.getRequestName() + "\n"
				+ "bodyLength=" + bodyLength + "\n\n" + body;

		SCMPMessage encodeRes = new SCMPPart();
		encodeRes.setHeader(encodeScmp);
		encodeRes.setBody(body.getBytes());

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeRes);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}

	@Test
	public void encodePRSTest() {
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		this.headKey = SCMPHeadlineKey.PRS;
		String expectedString = headKey.name() + " /s=71& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "msgType=" + msgType.getRequestName() + "\n"
				+ "bodyLength=" + bodyLength + "\n\n" + body;

		SCMPMessage encodeRes = new SCMPPart();
		encodeRes.setIsReply(true);
		encodeRes.setHeader(encodeScmp);
		encodeRes.setBody(body.getBytes());

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeRes);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}

	@Test
	public void encodeBodyTypesTest() {
		IEncoderDecoder coder = coderFactory.newInstance(new SCMPPart());

		String expectedString = headKey.name() + " /s=71& SCMP/1.0\n" + "bodyType=" + bodyType.getName()
				+ "\n" + "messageID=" + msgID + "\n" + "msgType=" + msgType.getRequestName() + "\n"
				+ "bodyLength=" + bodyLength + "\n\n" + body;

		OutputStream os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeScmp);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());

		coder = coderFactory.newInstance(encodeScmp);
		bodyType = SCMPBodyType.text;
		encodeScmp.setHeader(SCMPHeaderAttributeKey.BODY_TYPE, bodyType.getName());

		expectedString = headKey.name() + " /s=69& SCMP/1.0\n" + "bodyType=" + bodyType.getName() + "\n"
				+ "messageID=" + msgID + "\n" + "bodyLength=" + bodyLength + "\n" + "msgType="
				+ msgType.getRequestName() + "\n\n" + body;

		os = new ByteArrayOutputStream();
		try {
			coder.encode(os, encodeScmp);
		} catch (Exception e) {
			Assert.fail("Should not throw exception");
		}
		Assert.assertEquals(expectedString, os.toString());
	}

	private void verifySCMPStringBody(SCMPMessage scmp) {
		Assert.assertEquals(bodyType.getName(), scmp.getHeader(SCMPHeaderAttributeKey.BODY_TYPE));
//		Assert.assertEquals(msgID, scmp.getHeader(SCMPHeaderAttributeKey.MESSAGE_ID));
		Assert.assertEquals(bodyLength, scmp.getHeader(SCMPHeaderAttributeKey.BODY_LENGTH));
		Assert.assertEquals(msgType.getRequestName(), scmp.getHeader(SCMPHeaderAttributeKey.MSG_TYPE));
		Assert.assertEquals(body, scmp.getBody());
	}

	private void verifySCMP(SCMPMessage scmp) {
		Assert.assertEquals(bodyType.getName(), scmp.getHeader(SCMPHeaderAttributeKey.BODY_TYPE));
//		Assert.assertEquals(msgID, scmp.getHeader(SCMPHeaderAttributeKey.MESSAGE_ID));
		Assert.assertEquals(bodyLength, scmp.getHeader(SCMPHeaderAttributeKey.BODY_LENGTH));
		Assert.assertEquals(msgType.getRequestName(), scmp.getHeader(SCMPHeaderAttributeKey.MSG_TYPE));
		Assert.assertEquals(body, new String((byte[]) scmp.getBody()));
	}
}
