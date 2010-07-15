/*
 *-----------------------------------------------------------------------------*
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
 *-----------------------------------------------------------------------------*
/*
/**
 * 
 */
package com.stabilit.scm.common.service;


/**
 * @author JTraber
 */
public class SCMessage implements ISCMessage {

	private String messageInfo;
	private Boolean compressed;
	private Object data;
	private String sessionId;

	public SCMessage() {
		this.messageInfo = null;
		this.compressed = null;
		this.data = null;
		this.sessionId = null;
	}

	public SCMessage(Object data) {
		this();
		this.data = data;
	}

	@Override
	public void setMessageInfo(String messageInfo) {

		this.messageInfo = messageInfo;
	}

	@Override
	public String getMessageInfo() {
		return messageInfo;
	}

	@Override
	public Boolean isCompressed() {
		return compressed;
	}

	@Override
	public void setCompressed(Boolean compressed) {
		this.compressed = compressed;
	}

	@Override
	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public Object getData() {
		return this.data;
	}

	@Override
	public String getSessionId() {
		return this.sessionId;
	}

	@Override
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public boolean isFault() {
		return false;
	}
}
