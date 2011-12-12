package moses.client;

import moses.client.com.ConnectionParam;
import moses.client.com.NetworkJSON;
import moses.client.com.NetworkJSON.BackgroundException;
import moses.client.com.ReqTaskExecutor;
import moses.client.com.requests.RequestLogin;
import moses.client.com.requests.RequestLogout;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class MosesActivity extends Activity {
	private EditText txtUname;
	private EditText txtPW;

	private TextView txtSuccess;

	private Button btnconnect;

	private Button btnLogout;
	
	private Button btnExit;

	private CheckBox chkLoginAuto;
	private CheckBox chkSaveUnamePW;

	private SharedPreferences settings;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initControls();
		loadConfig();
		if(chkLoginAuto.isChecked()) connect();
	}

	private void loadConfig() {
		settings = getSharedPreferences("MoSeS.cfg", 0);
		txtUname.setText(settings.getString("uname", ""));
		txtPW.setText(settings.getString("password", ""));
		NetworkJSON.url = settings.getString("url",
				"http://test.jahofmann.de/test.php");
		chkLoginAuto.setChecked(settings.getBoolean("loginauto", false));
		chkSaveUnamePW.setChecked(settings.getBoolean("saveunamepw", false));
	}

	private void initControls() {
		txtUname = (EditText) findViewById(R.id.uname);
		txtPW = (EditText) findViewById(R.id.pword);

		txtSuccess = (TextView) findViewById(R.id.success);

		chkLoginAuto = (CheckBox) findViewById(R.id.loginauto);
		chkSaveUnamePW = (CheckBox) findViewById(R.id.saveunamepw);

		btnconnect = (Button) findViewById(R.id.connect_button);
		btnconnect.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				connect();
			}
		});

		btnLogout = (Button) findViewById(R.id.logout_button);
		btnLogout.setVisibility(View.INVISIBLE);
		btnLogout.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				logout();
			}
		});
		
		btnExit = (Button) findViewById(R.id.exitbutton);
		btnExit.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void connect() {
		SharedPreferences.Editor editor = settings.edit();
		if(chkSaveUnamePW.isChecked()) {
			editor.putString("uname", txtUname.getText().toString());
			editor.putString("password", txtPW.getText().toString());
		}
		editor.putBoolean("loginauto", chkLoginAuto.isChecked());
		editor.putBoolean("saveunamepw", chkSaveUnamePW.isChecked());
		editor.commit();
		RequestLogin r = new RequestLogin(new ReqClass(), txtUname.getText()
				.toString(), txtPW.getText().toString());
		r.send();
	}

	private void logout() {
		String sessionID = RequestLogin.getSessionID();
		RequestLogout rlogout = new RequestLogout(new ReqClassLogout(),
				sessionID);
		rlogout.send();
	}

	private class ReqClass implements ReqTaskExecutor {

		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestLogin.loginValid(j, txtUname.getText().toString())) {
					txtSuccess.setText("SUCCESS");

					txtSuccess.setVisibility(View.INVISIBLE);
					txtUname.setVisibility(View.INVISIBLE);
					txtPW.setVisibility(View.INVISIBLE);

					btnconnect.setVisibility(View.INVISIBLE);
					btnLogout.setVisibility(View.VISIBLE);

				} else {
					txtSuccess.setText("NOT GRANTED: " + j.toString());
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				txtSuccess.setText(c.toString());
			} else {
				handleException(c.e);
			}
		}

		public void handleException(Exception e) {
			txtSuccess.setText("FAILURE: " + e.getMessage());
		}
	}

	private class ReqClassLogout implements ReqTaskExecutor {

		public void postExecution(String s) {
			JSONObject j = null;
			try {
				j = new JSONObject(s);
				if (RequestLogout.logoutValid(j)) {
					txtSuccess.setText("SUCCESSFULLY LOGGED OUT");

					txtSuccess.setVisibility(View.VISIBLE);
					txtUname.setVisibility(View.VISIBLE);
					txtPW.setVisibility(View.VISIBLE);

					btnconnect.setVisibility(View.VISIBLE);
					btnLogout.setVisibility(View.INVISIBLE);

				} else {
					// TODO handling!!
					txtSuccess.setText("LOGOUT WAS REJECTED FROM SERVER"
							+ j.toString());
				}
			} catch (JSONException e) {
				this.handleException(e);
			}
		}

		public void updateExecution(BackgroundException c) {
			if (c.c != ConnectionParam.EXCEPTION) {
				txtSuccess.setText(c.toString());
			} else {
				handleException(c.e);
			}
		}

		public void handleException(Exception e) {
			txtSuccess.setText("FAILURE: " + e.getMessage());
		}
	}
}