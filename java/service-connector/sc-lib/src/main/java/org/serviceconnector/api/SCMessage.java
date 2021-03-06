/*-----------------------------------------------------------------------------*
 *                                                                             *
 *       Copyright © 2010 STABILIT Informatik AG, Switzerland                  *
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
package org.serviceconnector.api;

import java.util.Date;

import org.serviceconnector.Constants;
import org.serviceconnector.cache.SC_CACHING_METHOD;

/**
 * The Class SCMessage. A SCMessage is the basic transport unit to communicate with a Service Connector.
 *
 * @author JTraber
 */
public class SCMessage {

	/**
	 * The message info. Optional information passed together with the message body that helps to identify the message content without investigating the body.
	 */
	private String messageInfo;
	/**
	 * The session info. Optional information passed by the client to the session server when the session starts.
	 */
	private String sessionInfo;
	/** The compressed - regards data part of the message. */
	private boolean compressed;
	/** The data to transport. */
	private Object data;
	/** The length of transported data. */
	private int dataLength;
	/** The session id - identifies session context of communication. */
	private String sessionId;
	/** The message part size. */
	private int partSize;
	/**
	 * The cache id. Identification agreed by the communicating applications to uniquely identify the cached content. The cacheId is unique per service.
	 */
	private String cacheId;
	/** Marks cached messages. Message comes from a cache. */
	private boolean cached;
	/**
	 * The cache part nr.
	 */
	private String cachePartNr;

	/**
	 * The service name. The service name is an abstract name and represents the logical address of the service. In order to allow message routing the name must be unique in scope
	 * of the entire SC network. Service names must be agreed at the application level and are stored in the SC configuration.
	 */
	private String serviceName;
	/**
	 * The application error code. Numeric value passed between server and the client used to implement error protocol on the application level. Can be set by server whenever it
	 * responds with a message body.
	 */
	private int appErrorCode;
	/**
	 * The application error text.Textual value passed between server and the client used to implement error protocol on the application level. It can be the textual interpretation
	 * of the appErrorCode.
	 */
	private String appErrorText;
	/** The reject flag used to reject a create session / subscribe. */
	private boolean reject;
	/**
	 * The cache expiration date time, format on wire yyyy-MM-dd hh:mm:ss.SSSZ. Sent by the server, it represents the absolute expiration date and time of the message in cache. It
	 * must be set together with cacheId attribute.
	 */
	private Date cacheExpirationDateTime;
	/**
	 * The caching method. Method the message has been cached by the server.<br>
	 * INITIAL: this message is a base message for possible appendix APPEND: this message is an appendix for an initial message REMOVE: this message has removed a cached managed
	 * message NOT_MANAGED: default state for unmanaged (no appends possible) messages
	 */
	private SC_CACHING_METHOD cachingMethod = SC_CACHING_METHOD.NOT_MANAGED;

	/**
	 * Instantiates a new SCMessage.
	 */
	public SCMessage() {
		this.messageInfo = null;
		this.compressed = Constants.DEFAULT_COMPRESSION_FLAG;
		this.data = null;
		this.dataLength = 0;
		this.sessionId = null;
		this.sessionInfo = null;
		this.cacheId = null;
		this.cached = false;
		this.cachePartNr = null;
		this.appErrorCode = Constants.EMPTY_APP_ERROR_CODE;
		this.appErrorText = null;
		this.reject = false;
		this.partSize = Constants.DEFAULT_MESSAGE_PART_SIZE;
	}

	/**
	 * Instantiates a new SC message with byte[] data.
	 *
	 * @param data the data
	 */
	public SCMessage(byte[] data) {
		this();
		this.data = data;
	}

	/**
	 * Instantiates a new SC message with String data.
	 *
	 * @param data the data
	 */
	public SCMessage(String data) {
		this();
		this.data = data;
	}

	/**
	 * Sets the message info.
	 *
	 * @param messageInfo Optional information passed together with the message body that helps to identify the message content without investigating the body.<br />
	 *        Any printable character, length > 0 and < 256 Byte<br />
	 *        Example: SECURITY_MARKET_QUERY
	 */
	public void setMessageInfo(String messageInfo) {
		this.messageInfo = messageInfo;
	}

	/**
	 * Gets the session info.
	 *
	 * @return the session info
	 */
	public String getSessionInfo() {
		return sessionInfo;
	}

	/**
	 * Sets the session info.
	 *
	 * @param sessionInfo Optional information passed together with the message body Any printable character, length > 0 and < 256 Byte<br />
	 */
	public void setSessionInfo(String sessionInfo) {
		this.sessionInfo = sessionInfo;
	}

	/**
	 * Gets the message info.
	 *
	 * @return the message info
	 */
	public String getMessageInfo() {
		return messageInfo;
	}

	/**
	 * Checks if is compressed.
	 *
	 * @return the boolean
	 */
	public boolean isCompressed() {
		return compressed;
	}

	/**
	 * Sets the compressed. Default is true.
	 *
	 * @param compressed Regards the data part of the message.
	 */
	public void setCompressed(boolean compressed) {
		this.compressed = compressed;
	}

	/**
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public Object getData() {
		return this.data;
	}

	/**
	 * Sets the data length.
	 *
	 * @param dataLength the new data length
	 */
	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	/**
	 * Gets the data length.
	 *
	 * @return the data length
	 */
	public int getDataLength() {
		return this.dataLength;
	}

	/**
	 * Gets the session id.
	 *
	 * @return the session id
	 */
	public String getSessionId() {
		return this.sessionId;
	}

	/**
	 * Sets the session id.
	 *
	 * @param sessionId the new session id
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Gets the cache id.
	 *
	 * @return the cache id
	 */
	public String getCacheId() {
		return cacheId;
	}

	/**
	 * Gets the cache part nr.
	 *
	 * @return the cache part nr
	 */
	public String getCachePartNr() {
		return cachePartNr;
	}

	/**
	 * Checks if message has been cached and comes from a cache.
	 *
	 * @return true, if message has been cached
	 */
	public boolean isCached() {
		return this.cached;
	}

	/**
	 * Marks the message as a cached message.
	 *
	 * @param cached the new caching state
	 */
	public void setCached(boolean cached) {
		this.cached = cached;
	}

	/**
	 * Gets the service name.
	 *
	 * @return the service name
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * Sets the service name. Needs to be the same service which is used to send the message. The name will be overwritten at the time the message is sent.
	 *
	 * @param serviceName the new service name
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * Sets the cache id.
	 *
	 * @param cacheId the new cache id
	 */
	public void setCacheId(String cacheId) {
		this.cacheId = cacheId;
	}

	/**
	 * Sets the cache part nr.
	 *
	 * @param cachePartNr the new cache part nr
	 */
	public void setCachePartNr(String cachePartNr) {
		this.cachePartNr = cachePartNr;
	}

	/**
	 * Gets the application error code.
	 *
	 * @return the application error code
	 */
	public int getAppErrorCode() {
		return this.appErrorCode;
	}

	/**
	 * Sets the application error text.
	 *
	 * @param appErrorText the new application error text
	 */
	public void setAppErrorText(String appErrorText) {
		this.appErrorText = appErrorText;
	}

	/**
	 * Gets the application error text.
	 *
	 * @return the application error text
	 */
	public String getAppErrorText() {
		return this.appErrorText;
	}

	/**
	 * Sets the application error code.
	 *
	 * @param appErrorCode the new application error code
	 */
	public void setAppErrorCode(Integer appErrorCode) {
		if (appErrorCode == null) {
			this.appErrorCode = Constants.EMPTY_APP_ERROR_CODE;
		} else {
			this.appErrorCode = appErrorCode;
		}
	}

	/**
	 * Checks if is reject.
	 *
	 * @return true, if is reject
	 */
	public boolean isReject() {
		return this.reject;
	}

	/**
	 * Sets the reject.
	 *
	 * @param reject the new reject
	 */
	public void setReject(boolean reject) {
		this.reject = reject;
	}

	/**
	 * Gets the part size.
	 *
	 * @return the size of the parts in which message will be broken when it is large
	 */
	public int getPartSize() {
		return partSize;
	}

	/**
	 * Sets the part size.
	 *
	 * @param partSize the size in which message will be broken when it is large
	 */
	public void setPartSize(int partSize) {
		// only value smaller then MAX_MESSAGE_SIZE is allowed
		if (partSize < Constants.MAX_MESSAGE_SIZE) {
			this.partSize = partSize;
		} else {
			this.partSize = Constants.MAX_MESSAGE_SIZE;
		}
	}

	/**
	 * Gets the cache expiration date time.
	 *
	 * @return the cache expiration date time
	 */
	public Date getCacheExpirationDateTime() {
		return this.cacheExpirationDateTime;
	}

	/**
	 * Sets the cache expiration date time.
	 *
	 * @param cacheExpirationDateTime the new cache expiration date time
	 */
	public void setCacheExpirationDateTime(Date cacheExpirationDateTime) {
		this.cacheExpirationDateTime = cacheExpirationDateTime;
	}

	/**
	 * Sets the caching method.
	 *
	 * @param cachingMethod the new caching method
	 */
	public void setCachingMethod(SC_CACHING_METHOD cachingMethod) {
		this.cachingMethod = cachingMethod;
	}

	/**
	 * Gets the caching method.
	 *
	 * @return the caching method
	 */
	public SC_CACHING_METHOD getCachingMethod() {
		return this.cachingMethod;
	}

	/**
	 * Checks if message is managed.
	 *
	 * @return true, if message is managed
	 */
	public boolean isManaged() {
		if (this.cachingMethod == SC_CACHING_METHOD.NOT_MANAGED) {
			return false;
		} else {
			return true;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SCMessage [messageInfo=");
		builder.append(messageInfo);
		builder.append(", sessionInfo=");
		builder.append(sessionInfo);
		builder.append(", compressed=");
		builder.append(compressed);
		builder.append(", sessionId=");
		builder.append(sessionId);
		builder.append(", cacheId=");
		builder.append(cacheId);
		builder.append("]");
		return builder.toString();
	}
}
