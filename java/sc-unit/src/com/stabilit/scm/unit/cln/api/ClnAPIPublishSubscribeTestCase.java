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
package com.stabilit.scm.unit.cln.api;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.stabilit.scm.cln.service.ISCMessageCallback;
import com.stabilit.scm.cln.service.SCMessage;
import com.stabilit.scm.common.service.IPublishService;
import com.stabilit.scm.common.service.IServiceConnector;
import com.stabilit.scm.common.service.SCPublishServiceCallbackAdapter;
import com.stabilit.scm.common.service.ServiceConnector;
import com.stabilit.scm.srv.ps.PublishServer;
import com.stabilit.scm.unit.test.SetupTestCases;

public class ClnAPIPublishSubscribeTestCase {

	private int publishedMessageCounter = 0;
	@Before
	public void setUp() {
		SetupTestCases.setupAll();
	}
	
	@Test
	public void testSubscribeUnsubscribe() throws Exception {		
		IServiceConnector sc = null;
		IPublishService publishServiceA = null;
		try {			
			sc = new ServiceConnector("localhost", 8080);		
			sc.setMaxConnections(100);
			
			// connects to SC, checks connection to SC
			sc.attach();
			
			publishServiceA = sc.newPublishService("publish-simulation");		
			ISCMessageCallback callback = new TestPublishCallback(publishServiceA);
			publishServiceA.subscribe("AEC----", callback);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// disconnects from SC
				publishServiceA.unsubscribe();				
				sc.detach();
			} catch (Exception e) {
				sc = null;
			}
		}
	}
	
	
	@Test
	public void testSubscribePublishUnsubscribe() throws Exception {		
		IServiceConnector sc = null;
		IPublishService publishServiceA = null;
		try {			
			sc = new ServiceConnector("localhost", 8080);		
			sc.setMaxConnections(100);
			
			// connects to SC, checks connection to SC
			sc.attach();
			
			publishServiceA = sc.newPublishService("publish-simulation");		
			ISCMessageCallback callback = new TestPublishCallback(publishServiceA);
			publishServiceA.subscribe("AEC----", callback);
			PublishServer.beginPublish();
			while(publishedMessageCounter < 10) {
			    Thread.sleep(1000);
			}
			PublishServer.endPublish();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// disconnects from SC
				publishServiceA.unsubscribe();
				sc.detach();
			} catch (Exception e) {
				sc = null;
			}
		}
	}
	
	class TestPublishCallback extends SCPublishServiceCallbackAdapter {

		public TestPublishCallback(IPublishService service) {
			super(service);
		}

		@Override
		public void callback(SCMessage reply) throws Exception {
			publishedMessageCounter++;
			System.out.println("ClnAPIPublishSubscribeTestCase.TestPublishCallback.callback() counter = " + publishedMessageCounter);
		}

		@Override
		public void callback(Throwable th) {
			Assert.fail(th.toString());
		}		
	}
}
