/**
 * 
 */
package de.da_sense.moses.client.abstraction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import de.da_sense.moses.client.R;
import de.da_sense.moses.client.com.ConnectionParam;
import de.da_sense.moses.client.com.NetworkJSON.BackgroundException;
import de.da_sense.moses.client.com.ReqTaskExecutor;
import de.da_sense.moses.client.com.requests.RequestChangeDeviceIDParameters;
import de.da_sense.moses.client.com.requests.RequestGetFilter;
import de.da_sense.moses.client.com.requests.RequestGetHardwareParameters;
import de.da_sense.moses.client.com.requests.RequestLogin;
import de.da_sense.moses.client.com.requests.RequestSetFilter;
import de.da_sense.moses.client.com.requests.RequestSetHardwareParameters;
import de.da_sense.moses.client.preferences.MosesPreferences;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.service.helpers.C2DMManager;
import de.da_sense.moses.client.service.helpers.Executable;
import de.da_sense.moses.client.service.helpers.HookTypesEnum;
import de.da_sense.moses.client.service.helpers.MessageTypesEnum;
import de.da_sense.moses.client.util.Log;

/**
 * This class provides basic support for hardware sync with server.
 * 
 * @author Jaco Hofmann, Wladimir Schmidt
 * @author Zijad Maksuti
 * 
 */
public class HardwareAbstraction {

	/** the context */
	private Context context;
	
	/**
	 * This method is used to create HardwareAbstraction
	 * @param c Context
	 */
	public HardwareAbstraction(Context c) {
		context = c;
	}
	
    /**
     * This class is used to store informations of a device
     */
	public class HardwareInfo {
	    /** unique device id*/
		private String deviceID;
		/** sdk version of a device */
		private int sdkbuildversion;
		/** vendor of a device */
		private String vendor;
		/** model of a device */
		private String model;
		/** set of sensors that a device provides */
		private List<Integer> sensors;
		/** mDeviceName */
		private String deviceName;
		
		/**
		 * This method is used to create a hardwareinfo
		 * @param deviceID of a device
		 * @param vendor of a device
		 * @param model of a device
		 * @param sdkbuildversion sd version of a device
		 * @param sensors of a device
		 */
		private HardwareInfo(String deviceID, String deviceName, String vendor, String model, int sdkbuildversion, List<Integer> sensors) {
			super();
			this.deviceID = deviceID;
			this.sdkbuildversion = sdkbuildversion;
			this.sensors = sensors;
			this.vendor = vendor;
			this.model = model;
			this.deviceName = deviceName;
		}
		
		/**
		 * getter method for vendor
		 * @return
		 */
		public String getDeviceVendor() {
			return vendor;
		}
		
		/**
         * getter method for model
         * @return
         */
		public String getDeviceModel() {
			return model;
		}
		
		/**
         * getter method for device id
         * @return
         */
		public String getDeviceID() {
			return deviceID;
		}
		
		/**
         * getter method for sdk version
         * @return
         */
		public String getSdkbuildversion() {
			return String.valueOf(sdkbuildversion);
		}
		
		/**
         * getter method for set of sensors
         * @return
         */
		public List<Integer> getSensors() {
			return sensors;
		}

		/**
		 * @return the deviceName
		 */
		public String getDeviceName() {
			return deviceName;
		}
	}

	/**
	 * This class is used for NetworkJSON calls as a response for a filter call
	 */
	private class ReqClassGetFilter implements ReqTaskExecutor {

	    /**
	     * to handle an exception
         * @param e Exception
	     */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
		}

		/**
		 * to post an execution
         * @param s the JSONObject as String
		 */
		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestGetFilter.parameterAcquiredFromServer(j)) {
					JSONArray filter = j.getJSONArray("FILTER");
					if (MosesService.getInstance() != null)
						MosesService.getInstance().setFilter(filter);
				} else {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Parameters NOT retrieved successfully! Server returned negative response");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}
		
		/**
		 * to update an execution
         * @param c BackgroundException
		 */
		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	/**
	 * Get all available sensors from the operating system.
	 * 
	 * @return All available sensors on this device
	 */
	public static List<Sensor> getSensors() {
		if (MosesService.getInstance() != null) {
			List<Sensor> sensors = new ArrayList<Sensor>();
			SensorManager s = (SensorManager) MosesService.getInstance().getSystemService(Context.SENSOR_SERVICE);
			for (Sensor sen : s.getSensorList(Sensor.TYPE_ALL))
				sensors.add(sen);
			return sensors;
		} else {
			return null;
		}
	}

	/** ProgressDialog that describes the actual status of the network connection */
	private ProgressDialog gethwprogressdialog = null;
	/** handler for the runnables */
	private Handler handler = new Handler();

	/**
	 * This class is used for NetworkJSON calls as a response for a sensors call
	 */
	private class ReqClassGetHWParams implements ReqTaskExecutor {

	    /**
         * to handle an exception
         * @param e Exception
         */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
			// change the status of the network connection
			gethwprogressdialog.setMessage(MosesService.getInstance().getString(R.string.hwInfo_errorMessage));
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					gethwprogressdialog.dismiss();
				}
			}, 2000);
		}

		/**
         * to post an execution
         * @param s the JSONObject as String
         */
		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				Context с = MosesService.getInstance().getApplicationContext();
				j = new JSONObject(s);
				if (RequestGetHardwareParameters.parameterAcquiredFromServer(j)) {
					// TODO: think to combine that strings to one parameterized
					StringBuffer sb = new StringBuffer(256);
					// add the hardware parameters to sb
					sb.append(с.getString(R.string.hwInfo_parametersRetrievedSuccessfully));
					sb.append("\n").append(с.getString(R.string.deviceID_text2)).append(" ").append(j.get("DEVICEID"));
					sb.append("\n").append(с.getString(R.string.hwInfo_androidVersion)).append(j.get("ANDVER"));
					JSONArray sensors = j.getJSONArray("SENSORS");
					sb.append("\n").append(с.getString(R.string.hwInfo_sensors)).append("\n");
					// adding sensors to sb
					for (int i = 0; i < sensors.length(); i++) {
						sb.append("\n");
						sb.append(SensorsEnum.values()[sensors.getInt(i)]);
					}
					Log.d("MoSeS.HARDWARE_ABSTRACTION", sb.toString());
					AlertDialog ad = new AlertDialog.Builder(context).create();
					// prepare a dialog for this information
					ad.setCancelable(false); // This blocks the 'BACK' button
					ad.setIcon(R.drawable.ic_launcher);
					ad.setMessage(sb.toString());
					ad.setButton(DialogInterface.BUTTON_POSITIVE, с.getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					gethwprogressdialog.dismiss();
					// show the dialog
					ad.show();
				} else {
					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Parameters NOT retrieved successfully from server! :(");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		/**
         * to update an execution
         * @param c BackgroundException
         */
		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	/**
	 * This class is used for NetworkJSON calls as a request for filter
	 */
	private class ReqClassSetFilter implements ReqTaskExecutor {

	    /**
         * to handle an exception
         * @param e Exception
         */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE SETTING FILTER: " + e.getMessage());
		}

		/**
         * to post an execution
         * @param s the JSONObject as String
         */
		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestSetFilter.filterSetOnServer(j)) {
					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Filter set successfully, server returned positive response");
				} else {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Filter NOT set successfully! Server returned negative response");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		/**
         * to update an execution
         * @param c BackgroundException
         */
		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	/**
     * This class is used for NetworkJSON calls as a request to update the hardware parameters of a device
     */
	private class ReqClassUpdateHWParams implements ReqTaskExecutor {

	    /**
         * to handle an exception
         * @param e Exception
         */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
		}

		/**
         * to post an execution
         * @param s the JSONObject as String
         */
		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				Log.d("MoSeS.HARDWARE_ABSTRACTION", "Received: " + s);
				j = new JSONObject(s);
				// if this message contains SUCCESS as value of STATUS which
				// represents that the status of updating the device id
				if (j.getString("STATUS").equals("SUCCESS")) {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Updated device id successfully, server returned positive response");
					MosesService.getInstance().executeLoggedIn(
							HookTypesEnum.POST_LOGIN_SUCCESS_PRIORITY,
							MessageTypesEnum.REQUEST_SET_HARDWARE_PARAMETERS,
							new Executable() {
								@Override
								public void execute() {
									syncDeviceInformation();
								}
							});
				} else {
					// if the session id is invalid
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Update device id FAILED! Invalid session id.");
					MosesService.getInstance()
							.noOnSharedPreferenceChanged(true);
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		/**
         * to update an execution
         * @param c BackgroundException
         */
		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}

	/**
     * This class is used for NetworkJSON calls as a request to set the hardware parameters of a device
     */
	private class ReqClassSetHWParams implements ReqTaskExecutor {

	    /**
         * to handle an exception
         * @param e Exception
         */
		@Override
		public void handleException(Exception e) {
			Log.d("MoSeS.HARDWARE_ABSTRACTION", "FAILURE: " + e.getMessage());
		}

		/**
         * to post an execution
         * @param s the JSONObject as String
         */
		@Override
		public void postExecution(String s) {
			JSONObject j = null;
			try {
				Log.d("MoSeS.HARDWARE", "SetHWParams request response: " + s);
				j = new JSONObject(s);
				// if the parameters already been set on server
				if (RequestSetHardwareParameters.parameterSetOnServer(j)) {
					Log.d("MoSeS.HARDWARE_ABSTRACTION",
							"Parameters set successfully, server returned positive response");
					// sending the current C2DM of this device
					C2DMManager.sendCurrentC2DM();
					MosesService.getInstance().uploadFilter();
				}else {
				    // if the session id is invalid
					Log.d("MoSeS.HARDWARE_ABSTRACTION", "Parameters NOT set successfully! Invalid session id.");
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		/**
         * to update an execution
         * @param c BackgroundException
         */
		@Override
		public void updateExecution(BackgroundException c) {
			if (c.c == ConnectionParam.EXCEPTION) {
				handleException(c.e);
			}
		}
	}


	/**
	 * This method sends a Request to the website for obtaining the filter
	 * stored for this device
	 */
	public void getFilter() {
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(HookTypesEnum.POST_LOGIN_SUCCESS, MessageTypesEnum.REQUEST_GET_FILTER,
					new Executable() {
						@Override
						public void execute() {
							final RequestGetFilter rGetFilter = new RequestGetFilter(new ReqClassGetFilter(),
									RequestLogin.getSessionID(), extractDeviceIdFromSharedPreferences());
							rGetFilter.send();
						}
					});
	}

	/**
	 * This method reads the sensor list stored for the device on the server
	 */
	public void getHardwareParameters() {
		// *** SENDING GET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(HookTypesEnum.POST_LOGIN_SUCCESS,
					MessageTypesEnum.REQUEST_GET_HARDWARE_PARAMETERS, new Executable() {
						@Override
						public void execute() {
							gethwprogressdialog = new ProgressDialog(context);
							gethwprogressdialog.setTitle("Hardware Informations");
							gethwprogressdialog.setMessage("Retrieving...");
							gethwprogressdialog.show();
							new RequestGetHardwareParameters(new ReqClassGetHWParams(), RequestLogin.getSessionID(),
									extractDeviceIdFromSharedPreferences()).send();
						}
					});
	}

	/**
	 * This method sends a set_filter Request to the website
	 */
	public void setFilter(final String filter) {
		// *** SENDING GET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
		if (MosesService.getInstance() != null)
			MosesService.getInstance().executeLoggedIn(HookTypesEnum.POST_LOGIN_SUCCESS, MessageTypesEnum.REQUEST_SET_FILTER,
					new Executable() {
						@Override
						public void execute() {
							RequestSetFilter rSetFilter = new RequestSetFilter(new ReqClassSetFilter(), RequestLogin
									.getSessionID(), extractDeviceIdFromSharedPreferences(), filter);
							rSetFilter.send();
						}
					});
	}

	/**
	 * This method reads the sensors currently chosen by the user
	 * @return the actual Hardwareinfo
	 */
	private HardwareInfo retrieveHardwareParameters() {
		// *** SENDING SET_HARDWARE_PARAMETERS REQUEST TO SERVER ***//
		LinkedList<Integer> sensors = new LinkedList<Integer>();
		SensorManager s = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		for (Sensor sen : s.getSensorList(Sensor.TYPE_ALL)) {
			sensors.add(sen.getType());
		}
		return new HardwareInfo(extractDeviceIdFromSharedPreferences(), extractDeviceNameFromSharedPreferences(), Build.MANUFACTURER, Build.MODEL, Build.VERSION.SDK_INT, sensors);
	}

	/**
	 * to get device id that has been stored in the shared preferences
	 * @return device id as String
	 */
	public static String extractDeviceIdFromSharedPreferences() {
		String deviceid = "";
		MosesService mosesService = MosesService.getInstance();
		if (mosesService != null)
			deviceid = PreferenceManager.getDefaultSharedPreferences(mosesService).getString(
					MosesPreferences.PREF_DEVICEID, "");
		return deviceid;
	}
	
	/**
	 * Returns the name of the device stored in the shared preferences
	 * @return the name of the device or null if it is not stored in the shared preferences
	 */
	public static String extractDeviceNameFromSharedPreferences() {
		String deviceid = null;
		if (MosesService.getInstance() != null)
			deviceid = PreferenceManager.getDefaultSharedPreferences(MosesService.getInstance()).getString(
					MosesPreferences.PREF_DEVICENAME, null);
		return deviceid;
	}

	/**
	 * to synchronize the server with the actual information of this device
	 * @param force boolean
	 */
	public void syncDeviceInformation() {
		if (MosesService.getInstance() != null) {
			MosesService.getInstance().executeLoggedIn(HookTypesEnum.POST_LOGIN_SUCCESS_PRIORITY,
					MessageTypesEnum.REQUEST_SET_HARDWARE_PARAMETERS, new Executable() {
						@Override
						public void execute() {
							HardwareInfo hwInfo = retrieveHardwareParameters();
							new RequestSetHardwareParameters(new ReqClassSetHWParams(), hwInfo, RequestLogin
									.getSessionID()).send();
						}
					});
		}
	}
}
