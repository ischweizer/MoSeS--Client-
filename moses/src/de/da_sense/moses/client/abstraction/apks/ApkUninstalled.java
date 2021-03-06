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
package de.da_sense.moses.client.abstraction.apks;

import java.net.UnknownHostException;

import org.json.JSONException;

import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestUninstalledAPK;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.Executable;
import de.da_sense.moses.client.service.helpers.HookTypesEnum;
import de.da_sense.moses.client.service.helpers.MessageTypesEnum;
import de.da_sense.moses.client.util.Log;

public class ApkUninstalled {

	private class APKUninstalledTaskExecutor implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			if (e instanceof UnknownHostException || e instanceof JSONException) {
				Log.d("MoSeS.LOGIN", "No internet connection present (or DNS problems.)");
			} else
				Log.d("MoSeS.LOGIN", "FAILURE: " + e.getClass().toString() + " " + e.getMessage());
		}

		@Override
		public void postExecution(String s) {
			Log.d("MoSeS.APK_INSTALLED", "Confirmation received: notified server about uninstalled apk: " + s);
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				// Don't care once more...
			} else {
				handleException(c.e);
			}
		}
	};

	/**
	 * Notifies the server of an uninstalled APK.
	 * @param appID The ID of the APK
	 */
	public ApkUninstalled(final String appID) {
		// TODO: handle service == null
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(HookTypesEnum.POST_LOGIN_SUCCESS,
					MessageTypesEnum.REQUEST_UNINSTALLED_APK, new Executable() {

						@Override
						public void execute() {
							Log.d("MoSeS.APK", "Sending information to server that app was uninstalled: " + appID);
							new RequestUninstalledAPK(new APKUninstalledTaskExecutor(), MosesService.getInstance()
									.getSessionID(), appID).send();
						}
					});
	}
}
