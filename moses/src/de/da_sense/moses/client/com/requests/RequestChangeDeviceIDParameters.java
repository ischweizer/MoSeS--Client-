/*******************************************************************************
 * Copyright 2013
 * Telecooperation (TK) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.da_sense.moses.client.com.requests;

import org.json.JSONException;
import org.json.JSONObject;

import de.da_sense.moses.client.com.NetworkJSON;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.util.Log;

/**
 * This class provides the message to change the devicie id on the server
 * 
 * @author Jaco Hofmann
 * 
 */
public class RequestChangeDeviceIDParameters {
	public static boolean parameterSetOnServer(JSONObject j) throws JSONException {
		if (j.getString("STATUS").equals("SUCCESS")) {
			return true;
		}
		return false;
	}

	private JSONObject j;

	private ReqTaskExecutor e;

	public RequestChangeDeviceIDParameters(ReqTaskExecutor e, boolean force, String deviceid, String old_deviceid, String sessionID) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "CHANGE_DEVICE_ID");
			j.put("SESSIONID", sessionID);
			j.put("FORCE", force);
			j.put("DEVICEID", deviceid);
			j.put("OLD_DEVICEID", old_deviceid);
		} catch (JSONException ex) {
			e.handleException(ex);
		}
	}

	public void send() {
		Log.d("MoSeS.HARDWARE_ABSTRACTION", j.toString());
		NetworkJSON task = new NetworkJSON();
		NetworkJSON.APIRequest req;
		req = task.new APIRequest();
		req.request = j;
		req.reqTaskExecutor = this.e;
		task.execute(req);
	}
}
