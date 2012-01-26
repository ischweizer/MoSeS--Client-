package moses.client.com.requests;

import moses.client.abstraction.HardwareAbstraction;
import moses.client.com.NetworkJSON;
import moses.client.com.ReqTaskExecutor;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class represents a Request for obtaining the list of avaliable APKs for
 * this device from the Server
 * 
 * @author Simon Leischnig
 * 
 */
public class RequestGetApkInfo {

	/**
	 * Returns true when the server has returned the success-response
	 * 
	 * @param j
	 * @return true when the server has returned a success-response, else false
	 * @throws JSONException
	 */
	public static boolean isInfoRetrived(JSONObject j) throws JSONException {
		return j.getString("MESSAGE").equals("GET_APK_INFO_RESPONSE")
				&& j.getString("STATUS").equals("SUCCESS");
	}

	private JSONObject j;

	ReqTaskExecutor e;

	/**
	 * Generates a new Request for apk info
	 * 
	 * @param e
	 * @param apkId
	 * @param sessionID
	 */
	public RequestGetApkInfo(ReqTaskExecutor e, String apkId, String sessionID) {
		j = new JSONObject();
		this.e = e;
		try {
			j.put("MESSAGE", "GET_APK_INFO");
			j.put("ID", apkId);
			j.put("SESSIONID", sessionID);
			j.put("DEVICEID", HardwareAbstraction.extractDeviceId());
		} catch (JSONException ex) {
			e.handleException(ex);
		}
	}

	public void send() {
		NetworkJSON task = new NetworkJSON();
		NetworkJSON.APIRequest req;
		req = task.new APIRequest();
		req.request = j;
		req.e = this.e;
		task.execute(req);
	}
}
