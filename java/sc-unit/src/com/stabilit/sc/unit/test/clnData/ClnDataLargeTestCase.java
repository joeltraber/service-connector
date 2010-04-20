/*
 *-----------------------------------------------------------------------------*
 *                            Copyright � 2010 by                              *
 *                    STABILIT Informatik AG, Switzerland                      *
 *                            ALL RIGHTS RESERVED                              *
 *                                                                             *
 * Valid license from STABILIT is required for possession, use or copying.     *
 * This software or any other copies thereof may not be provided or otherwise  *
 * made available to any other person. No title to and ownership of the        *
 * software is hereby transferred. The information in this software is subject *
 * to change without notice and should not be construed as a commitment by     *
 * STABILIT Informatik AG.                                                     *
 *                                                                             *
 * All referenced products are trademarks of their respective owners.          *
 *-----------------------------------------------------------------------------*
 */
/**
 * 
 */
package com.stabilit.sc.unit.test.clnData;

import junit.framework.Assert;

import org.junit.Test;

import com.stabilit.sc.cln.service.SCMPCallFactory;
import com.stabilit.sc.cln.service.SCMPClnDataCall;
import com.stabilit.sc.common.io.SCMP;
import com.stabilit.sc.common.io.SCMPHeaderAttributeKey;
import com.stabilit.sc.common.io.SCMPMsgType;
import com.stabilit.sc.unit.test.SuperSessionTestCase;

/**
 * @author JTraber
 * 
 */
public class ClnDataLargeTestCase extends SuperSessionTestCase {

	@Test
	public void clnDataLargeTest() throws Exception {
		SCMPClnDataCall clnDataCall = (SCMPClnDataCall) SCMPCallFactory.CLN_DATA_CALL.newInstance(client,
				scmpSession);
		clnDataCall.setMessagInfo("message info");
		clnDataCall.setBody("large");
		SCMP scmpReply = clnDataCall.invoke();
		String result = (String) scmpReply.getBody();

		/*********************************** Verify connect response msg **********************************/
		Assert.assertNotNull(scmpReply.getBody());
		Assert.assertEquals(scmpReply.getHeader(SCMPHeaderAttributeKey.MSG_TYPE), SCMPMsgType.CLN_DATA
				.getResponseName());
	}
}
