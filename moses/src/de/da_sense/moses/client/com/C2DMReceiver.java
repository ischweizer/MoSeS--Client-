package de.da_sense.moses.client.com;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import de.da_sense.moses.client.abstraction.apks.InstalledExternalApplicationsManager;
import de.da_sense.moses.client.service.helpers.C2DMManager;
import de.da_sense.moses.client.userstudy.UserstudyNotificationManager;
import de.da_sense.moses.client.util.Log;

public class C2DMReceiver extends BroadcastReceiver {

	private static final String C2DM_PUSH_MESSAGTYPE_USERSTUDY = "USERSTUDY";
	private static final String C2DM_PUSH_MESSAGTYPE_UPDATE = "UPDATE";
    private static final String C2DM_PUSH_MESSAGTYPE_QUEST = "QUEST"; // XXX by Ibrahim
	public static final String EXTRAFIELD_USERSTUDY_NOTIFICATION = "UserStudyNotification";
	private static final String C2DN_MESSAGETYPE_FIELD = "MESSAGE";
	private static final String C2DN_USERSTUDY_APKID_FIELD = "APKID";
	private static final String C2DN_UPDATE_APKID_FIELD = "APKID";
    private static final String C2DN_QUEST_APKID_FIELD = "APKID"; // XXX by Ibrahim
    private static final String C2DN_QUEST_CONTENT_FIELD = "CONTENT"; // XXX by Ibrahim
	public static final String EXTRAFIELD_C2DM_ID = "c2dmId";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION")) {
			handleC2DMRegistrationMsgFromGoogle(context, intent);
		} else if (intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE")) {
			handleNotifications(context, intent);
		}
	}

	private static void handleNotifications(Context context, Intent intent) {
		String messagetype = intent.getExtras().getString(C2DN_MESSAGETYPE_FIELD);
		if (messagetype != null) {
			if (messagetype.equals(C2DM_PUSH_MESSAGTYPE_USERSTUDY)) {
				String apkidString = intent.getExtras().getString(C2DN_USERSTUDY_APKID_FIELD);
				if (apkidString != null) {
					Log.i("MoSeS.C2DM", "User study notification received!! APK ID = " + apkidString);
					Log.i("MoSeS.USERSTUDY", "userstudy id incoming: " + apkidString);
					UserstudyNotificationManager.userStudyNotificationArrived(apkidString);
				} else {
					Log.i("MoSeS.C2DM", "User study notification received but bad apkid (null)");
				}
			} else if (messagetype.equals(C2DM_PUSH_MESSAGTYPE_UPDATE)) {
				String apkidString = intent.getExtras().getString(C2DN_UPDATE_APKID_FIELD);
				if (apkidString != null) {
					Log.i("MoSeS.C2DM", "update notification received!! APK ID = " + apkidString);
					Log.i("MoSeS.UPDATE", "update incoming: " + apkidString);
					InstalledExternalApplicationsManager.updateArrived(apkidString);
				} else {
					Log.i("MoSeS.C2DM", "Update notification received but bad apkid (null)");
				}
			} else // XXX by Ibrahim : to handle a questionnaire notification
                if(messagetype.equals(C2DM_PUSH_MESSAGTYPE_QUEST))
                {
                    String apkidString = intent.getExtras().getString(C2DN_QUEST_APKID_FIELD);
                    String contentString = intent.getExtras().getString(C2DN_QUEST_CONTENT_FIELD);
                    if (apkidString != null){
                        Log.i("MoSeS.C2DM", "questionnaire notification received. APK ID = " + apkidString);
                        if (contentString != null)
                        {
                            Log.i("MoSeS.C2DM", "questionnaire notification received!. CONTENT = " + contentString);
                            Log.i("MoSeS.QUEST", "update incoming: " + apkidString + " with content: " + contentString);
//                            UserstudyNotificationManager.questionnaireNotificationArrived(apkidString,contentString);
                            Log.d("MoSeS.C2DM", !contentString.equals("[]") + ", endDateReached = true");
                            InstalledExternalApplicationsManager.getInstance().getAppForId(apkidString).setApkHasQuestOnServer(!contentString.equals("[]"));
                            InstalledExternalApplicationsManager.getInstance().getAppForId(apkidString).setEndDateReached(true);
                        }else
                        {
                            Log.i("MoSeS.C2DM", "questionnaire notification received but bad content (null)");
                        }
                    }else
                    {
                        Log.i("MoSeS.C2DM", "questionnaire notification received but bad apkid (null)");
                    }
                    
                }
                else
                {
                Log.w("MoSeS.C2DM", "Unhandled C2DM Message from type: " + messagetype);
                }
        } else {
            Log.i("MoSeS.C2DM", "Notification received but bad MESSAGE String (null)");
        }

	}

	private static void handleC2DMRegistrationMsgFromGoogle(Context context, Intent intent) {
		String registrationId = intent.getStringExtra("registration_id");
		if (intent.getStringExtra("error") != null) {
			// TODO: handle errors
			// mögliche error-codes:
			// SERVICE_NOT_AVAILABLE The device can't read the response, or
			// there was a 500/503 from the server that can be retried later.
			// The application should use exponential back off and retry.
			// ACCOUNT_MISSING There is no Google account on the phone. The
			// application should ask the user to open the account manager and
			// add a Google account. Fix on the device side.
			// AUTHENTICATION_FAILED Bad password. The application should ask
			// the user to enter his/her password, and let user retry manually
			// later. Fix on the device side.
			// TOO_MANY_REGISTRATIONS The user has too many applications
			// registered. The application should tell the user to uninstall
			// some other applications, let user retry manually. Fix on the
			// device side.
			// INVALID_SENDER The sender account is not recognized.
			// PHONE_REGISTRATION_ERROR
			Log.e("MoSeS.C2DM", "C2DM-error: " + intent.getStringExtra("error"));
		} else if (intent.getStringExtra("unregistered") != null) {
			// unregistration done
		} else if (registrationId != null) {
			Log.d("MoSeS.C2DM", "C2DM-ID arrived");
			notifyAboutC2DMId(registrationId, context);
		}
	}

	// XXX ZM this method is potentially unused
	private static void notifyAboutC2DMId(final String registrationId, Context context) {
		// Intent intent = new Intent(context, MosesService.class);
		// intent.putExtra(EXTRAFIELD_C2DM_ID, registrationId);
		// // this directs to "onStartCommand(Intent, int, int)" in the service
		// context.startService(intent);
		C2DMManager.setC2DMReceiverId(registrationId);
	}
}
