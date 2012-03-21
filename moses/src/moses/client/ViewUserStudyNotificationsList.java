package moses.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import moses.client.service.MosesService;
import moses.client.userstudy.UserStudyNotification;
import moses.client.userstudy.UserstudyNotificationManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Viewing and installing apks from the server
 * 
 * @author Simon L
 */
public class ViewUserStudyNotificationsList extends Activity {

	private static final int showStudyRequestcode = 5;
	private ListView listView;
	private List<UserStudyNotification> externalApps;
	private UserStudyNotification lastStartedDialog;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pendinguserstudieslist);

		initControls();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == showStudyRequestcode) {
			UserStudyNotification app = lastStartedDialog;
			if(resultCode == Activity.RESULT_OK) {
				
			} else {
				
			}
			
			drawUserStudies();
		}
	}
	
	public void studyOnClickHandler(View v) {
		int pos = listView.getPositionForView(v);
		final UserStudyNotification app = externalApps.get(pos);

		Intent intent = new Intent(MosesService.getInstance(), ViewUserStudyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(ViewUserStudyActivity.EXTRA_USER_STUDY_APK_ID,
				app.getApplication().getID());
		
		//TODO: return code management
		lastStartedDialog = app;
		startActivityForResult(intent, showStudyRequestcode);
		
	}

	/**
	 * Inits the controls.
	 */
	private void initControls() {
		if(UserstudyNotificationManager.getInstance() == null) {
			UserstudyNotificationManager.init(this.getApplicationContext());
		}
		drawUserStudies();
	}

	private void drawUserStudies() {
		List<UserStudyNotification> studies = UserstudyNotificationManager.getInstance().getNotifications();
		externalApps = studies;
		populateList(studies);
	}



	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		drawUserStudies();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		drawUserStudies();
	}
	
	private void populateList(List<UserStudyNotification> applications) {
		listView = (ListView) findViewById(R.id.userstudyListListView);
		
		TextView instructionsView = (TextView) findViewById(R.id.userstudyListHeaderInstructions);
		if(instructionsView != null) {
			//TODO: check if there is no notification WITH DATA (or just load all descriptions/names in this activity, too)
			if(applications.size() == 0) {
				instructionsView.setText("No user studies available.");
			} else {
				instructionsView.setText("Click on a user study to see the details.");
			}
		}
		
		List<Map<String, String>> listContent = new LinkedList<Map<String, String>>();
		for(UserStudyNotification app: applications) {
			if(isNotificationToDisplay(app)) {
				HashMap<String, String> rowMap = new HashMap<String, String>();
				rowMap.put("name", app.getApplication().getName());
				rowMap.put("description", app.getApplication().getDescription());
				listContent.add(rowMap);
			}
		}
		SimpleAdapter contentAdapter = new SimpleAdapter( 
			this, 
			listContent,
			R.layout.availablestudieslistitem,
			new String[] { "name","description" },
			new int[] { R.id.apklistitemtext, R.id.apklistitemdescription } );
		
		listView.setAdapter(contentAdapter);
	}

	private static boolean isNotificationToDisplay(UserStudyNotification app) {
		return (app.getApplication().getName()!=null)&&(app.getApplication().getDescription()!=null);
	}

	public static String concatStacktrace(Exception e) {
		String stackTrace = "";
		for (int i = 0; i < e.getStackTrace().length; i++) {
			stackTrace += e.getStackTrace()[i];
		}
		return stackTrace;
	}

}
