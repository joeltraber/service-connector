/*
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
 */
package org.serviceconnector.test.system;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.serviceconnector.test.system.api.cln.casc1.APICreateDeleteSessionCasc1Test;
import org.serviceconnector.test.system.api.cln.casc1.APIExecuteAndSendCasc1Test;
import org.serviceconnector.test.system.api.cln.casc1.APIExecuteCacheCasc1Test;
import org.serviceconnector.test.system.api.cln.casc1.APIMultipleClientChangeSubscriptionCasc1Test;
import org.serviceconnector.test.system.api.cln.casc1.APIMultipleClientSubscribeCasc1Test;
import org.serviceconnector.test.system.api.cln.casc1.APIReceivePublicationCasc1Test;
import org.serviceconnector.test.system.api.cln.casc1.APISubscribeUnsubscribeChangeCasc1Test;
import org.serviceconnector.test.system.scmp.casc1.SCMPClnChangeSubscriptionCasc1Test;
import org.serviceconnector.test.system.scmp.casc1.SCMPClnCreateSessionCasc1Test;
import org.serviceconnector.test.system.scmp.casc1.SCMPClnExecuteCasc1Test;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
		// API session tests
		APICreateDeleteSessionCasc1Test.class,
		APIExecuteAndSendCasc1Test.class,


		// API publish tests
		APISubscribeUnsubscribeChangeCasc1Test.class,
		APIReceivePublicationCasc1Test.class,
		APIMultipleClientSubscribeCasc1Test.class,
		APIMultipleClientChangeSubscriptionCasc1Test.class,
		APIExecuteCacheCasc1Test.class,

		// SCMP session test
		SCMPClnCreateSessionCasc1Test.class,
		SCMPClnExecuteCasc1Test.class,
		SCMPClnChangeSubscriptionCasc1Test.class
		})
public class AllCascSystemTests {
}
