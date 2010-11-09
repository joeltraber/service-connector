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
package org.serviceconnector.log;

import java.util.Formatter;

import org.apache.log4j.Logger;

public class SubscriptionLogger {

	private static final Logger subscriptionLogger = Logger.getLogger(Loggers.SUBSCRIPTION.getValue());
	private static final SubscriptionLogger instance = new SubscriptionLogger();
	
	private static String SUBSCRIBE_STR = "session:%s - subscribing to:%s - with mask:%s";
	private static String CHANGE_SUBSCRIBE_STR = "session:%s - subscribed to:%s - new mask:%s";
	private static String UNSUBSCRIBE_STR = "session:%s - unsubscribing from:%s";

	/**
	 * Private constructor for singleton use.
	 */
	private SubscriptionLogger() {
	}

	public static SubscriptionLogger getInstance() {
		return SubscriptionLogger.instance;
	}

	/**
	 * @param serviceName
	 * @param sessionId
	 * @param mask
	 */
	public synchronized void logSubscribe(String serviceName, String sessionId, String mask) {
		if (subscriptionLogger.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(SUBSCRIBE_STR, sessionId, serviceName, mask);
			subscriptionLogger.debug(format.toString());
			format.close();
		}
	}

	/**
	 * @param serviceName
	 * @param sessionId
	 * @param mask
	 */
	public synchronized void logChangeSubscribe(String serviceName, String sessionId, String mask) {
		if (subscriptionLogger.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(CHANGE_SUBSCRIBE_STR, sessionId, serviceName, mask);
			subscriptionLogger.debug(format.toString());
			format.close();
		}
	}

	/**
	 * @param serviceName
	 * @param sessionId
	 */
	public synchronized void logUnsubscribe(String serviceName, String sessionId) {
		if (subscriptionLogger.isTraceEnabled()) {
			Formatter format = new Formatter();
			format.format(UNSUBSCRIBE_STR, sessionId, serviceName);
			subscriptionLogger.debug(format.toString());
			format.close();
		}
	}

	/**
	 * @return
	 */
	public boolean isEnabled() {
		return subscriptionLogger.isTraceEnabled();
	}
}