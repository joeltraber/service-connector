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

package org.serviceconnector.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.serviceconnector.ctx.AppContext;

public class HttpClientUploadUtility {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(HttpClientUploadUtility.class);

	private String uri;
	private String contentType;
	private HttpClient client;
	private PostMethod httpMethod;

	public HttpClientUploadUtility(String uri) {
		this(uri, null);
	}

	public HttpClientUploadUtility(String uri, String contentType) {
		this.uri = uri;
		this.contentType = contentType;
		this.client = new HttpClient();
		this.httpMethod = new PostMethod(this.uri);
		this.httpMethod.setContentChunked(true);
	}

	public void upload(InputStream is) throws IOException {
		this.httpMethod.setRequestEntity(new InputStreamRequestEntity(is));
		this.client.executeMethod(this.httpMethod);
		this.httpMethod.releaseConnection();
		if (this.httpMethod.getStatusCode() != HttpStatus.SC_OK) {
			throw new IOException("Http Client Upload failure: " + this.httpMethod.getStatusLine().toString());
		}
	}

	public UploadRunnable startUpload() throws IOException {
		CircularByteBuffer cbb = new CircularByteBuffer();
		this.httpMethod.setRequestEntity(new InputStreamRequestEntity(cbb.getInputStream()));
		UploadRunnable uploadRunnable = new UploadRunnable(cbb);
		Future<Integer> submit = AppContext.getExecutor().submit(uploadRunnable);
		uploadRunnable.future = submit;
		return uploadRunnable;
	}

	public class UploadRunnable implements Callable<Integer> {
		private CircularByteBuffer cbb;
		private Future<Integer> future;

		private UploadRunnable(CircularByteBuffer cbb) {
			this.cbb = cbb;
			this.future = null;
		}

		public OutputStream getOutputStream() {
			return this.cbb.getOutputStream();
		}

		public Integer close() throws Exception {
			this.cbb.getOutputStream().close();
			return this.future.get(5, TimeUnit.SECONDS);
		}

		@Override
		public Integer call() {
			try {
				HttpClientUploadUtility.this.client.executeMethod(HttpClientUploadUtility.this.httpMethod);
				Integer statusCode = HttpClientUploadUtility.this.httpMethod.getStatusCode();
				HttpClientUploadUtility.this.httpMethod.releaseConnection();				
				return statusCode;
			} catch (Exception e) {
				logger.error(e.toString());
				return -1;
			}
		}
	}

}