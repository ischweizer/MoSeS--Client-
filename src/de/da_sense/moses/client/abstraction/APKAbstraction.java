/**
 * 
 */
package de.da_sense.moses.client.abstraction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestGetListAPK;
import de.da_sense.moses.client.com.requests.RequestLogin;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.EHookTypes;
import de.da_sense.moses.client.service.helpers.EMessageTypes;
import de.da_sense.moses.client.service.helpers.Executable;

/**
 * This class offers methods getting the informations about available APKs from
 * the server, it is also used
 * 
 * @author Zijad Maksuti
 * 
 */

public class APKAbstraction {

	private class ReqClassGetListAPK implements ReqTaskExecutor {

		@Override
		public void handleException(Exception e) {
			infoDialog.setMessage("FAILURE ON REQUESTING LIST OF APKs: "
					+ e.getMessage());
			infoDialog.show();
		}

		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				// TODO handling
				if (RequestGetListAPK.isListRetrieved(j)) {
					// get the informations about available APKs
					StringBuffer sb = new StringBuffer(512);
					sb.append("Request sent successfully, server returned positive response");
					sb.append("\n").append("-List of APKs-").append("\n");

					JSONArray apkInformations = j.getJSONArray("APK_LIST");
					for (int i = 0; i < apkInformations.length(); i++) {
						sb.append("\n");
						JSONObject apkInformation = apkInformations
								.getJSONObject(i);
						sb.append("APK ID: ")
								.append(apkInformation.getString("ID"))
								.append("\n");
						sb.append("NAME: ")
								.append(apkInformation.getString("NAME"))
								.append("\n");
						sb.append("DESCRIPTION: ")
								.append(apkInformation.getString("DESCR"))
								.append("\n");
						sb.append("PARTICIPATED COUNT: ")
								.append(apkInformation.getString("PRTCPTDICNT"))
								.append("\n");
						sb.append("START DATE: ")
								.append(apkInformation.getString("STARTDATE"))
								.append("\n");
						sb.append("END DATE: ")
								.append(apkInformation.getString("ENDDATE"))
								.append("\n");
						sb.append("APK VERSION: ")
								.append(apkInformation.getString("APKVERSION"))
								.append("\n");
					}

					infoDialog.setMessage(sb.toString());
					infoDialog.show();
				} else {
					// TODO handling
					infoDialog
							.setMessage("Request not successfull! Server returned negative response");
					infoDialog.show();
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				infoDialog.setMessage(c.c.toString());
				infoDialog.show();
			} else {
				handleException(c.e);
			}
		}
	}

	private static AlertDialog infoDialog; // used for showing the results

	/**
	 * Constructs a new APKAbstraction which methods can be used for getting
	 * informations about available APKs from the server
	 * 
	 * @param c
	 *            the Context in which the APKAbstraction should operate
	 */
	public APKAbstraction(Context c) {
		infoDialog = new AlertDialog.Builder(c).create();
		infoDialog.setIcon(R.drawable.ic_launcher);
		infoDialog.setTitle("INFO:");
		infoDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ok",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						infoDialog.dismiss();
					}
				});
	}

	/**
	 * This method sends a request to the server in order to get the list of
	 * available APKs (for the specified filter)
	 */
	public void getAPKs() {
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(
					EHookTypes.POSTLOGINSUCCESS,
					EMessageTypes.REQUESTGETLISTAPK, new Executable() {

						@Override
						public void execute() {
							new RequestGetListAPK(new ReqClassGetListAPK(),
									RequestLogin.getSessionID()).send();
						}
					});
	}

}