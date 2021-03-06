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
package org.serviceconnector.ctrl.util;

public class ServiceConnectorDefinition {

	private String scName;
	private String properyFileName;
	private String logbackFileName;

	public ServiceConnectorDefinition(String scName, String properyFileName, String logbackFileName) {
		this.scName = scName;
		this.properyFileName = properyFileName;
		this.logbackFileName = logbackFileName;
	}

	public String getLogbackFileName() {
		return logbackFileName;
	}

	public String getProperyFileName() {
		return properyFileName;
	}

	public String getName() {
		return this.scName;
	}
}
