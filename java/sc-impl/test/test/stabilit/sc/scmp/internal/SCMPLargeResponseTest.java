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
package test.stabilit.sc.scmp.internal;

import junit.framework.Assert;

import org.junit.Test;

import com.stabilit.sc.scmp.SCMPMessage;
import com.stabilit.sc.scmp.internal.SCMPCompositeSender;
import com.stabilit.sc.scmp.internal.SCMPSendPart;

/**
 * @author JTraber
 * 
 */
public class SCMPLargeResponseTest extends SCMPMessage {

	@Test
	public void scmpLargeResponseTest() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < 100000; i++) {
			sb.append(i);
		}		
		
		SCMPMessage largeScmp = new SCMPMessage();
		largeScmp.setBody(sb.toString());
		largeScmp.setIsReply(true);
		
		SCMPCompositeSender largeResponse = new SCMPCompositeSender(largeScmp);
		
		int offset = 0;
		while(largeResponse.hasNext()) {
			
			SCMPSendPart responsePart = new SCMPSendPart(largeScmp, offset);
			offset += responsePart.getBodyLength();
			
			SCMPMessage message = largeResponse.getNext();
			Assert.assertEquals(responsePart.getBody().toString(), message.getBody().toString());
			Assert.assertEquals(responsePart.getBodyLength(), message.getBodyLength());
			Assert.assertEquals(responsePart.getBodyOffset(), message.getBodyOffset());
			Assert.assertEquals(responsePart.getBodyType(), message.getBodyType());
		}
		
		SCMPSendPart firstPart = new SCMPSendPart(largeScmp, 0);
		SCMPMessage message = largeResponse.getFirst();
		Assert.assertEquals(firstPart.getBody().toString(), message.getBody().toString());
		Assert.assertEquals(firstPart.getBodyLength(), message.getBodyLength());
		Assert.assertEquals(firstPart.getBodyOffset(), message.getBodyOffset());
		Assert.assertEquals(firstPart.getBodyType(), message.getBodyType());
	}
}
