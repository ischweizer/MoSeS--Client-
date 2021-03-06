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
package de.da_sense.moses.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.da_sense.moses.client.abstraction.apks.ExternalApplication;
import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplication;
import de.da_sense.moses.client.abstraction.apks.HistoryExternalApplicationsManager;
import de.da_sense.moses.client.service.MosesService;
import de.da_sense.moses.client.util.Log;

/**
 * Represents the Fragment for the History list of the user studies.
 * 
 * @author Sandra Amend, Wladimir Schmidt
 * @author Zijad Maksuti
 * 
 */
public class HistoryFragment extends ListFragment {
	/** saves the current position in the list */
	private int mCurHistPosition = 0;
	/** The current instance is saved in here. */
	private static HistoryFragment thisInstance = null;
	/** a log tag for this class */
	private final static String TAG = "HistoryFragment";
	/** The installed external applications. */
	private List<HistoryExternalApplication> historyUS;

	/** Returns the current instance (singleton) */
	public static HistoryFragment getInstance() {
		return thisInstance;
	}
	
	/**
	 * The activity containing this fragment
	 */
	private Activity mActivity;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		initControls();

		if (savedInstanceState != null) {
			// restore last state
			mCurHistPosition = savedInstanceState.getInt("curChoice", 0);
		}
	}

	/**
	 * Helper method for showing the details of a userstudy.
	 * 
	 * @param index
	 *            the index of the userstudy to show the details for
	 * @param baseActivity
	 *            the base activity
	 * @param startAppClickAction
	 *            runnable for startApp button
	 * @param cancelClickAction
	 */
	protected void showDetails(int index, Activity baseActivity,
			final Runnable startAppClickAction, final Runnable cancelClickAction) {
		if (MosesService.isOnlineOrIsConnecting(mActivity
				.getApplicationContext())) {
			if (getListView() != null) {
				// if we don't have any installed apps on the device an
				// indexOutOfBoundsException gets thrown
				// this only happens on tablets (dual view)
				if (getHistoryUS() != null && getHistoryUS().size() > 0) {
					final HistoryExternalApplication app = getHistoryUS().get(
							index);

					// otherwise launch new activity to display the fragment
					// with selected text
					Intent intent = new Intent();
					intent.setClass(mActivity, DetailActivity.class);
					intent.putExtra("de.da_sense.moses.client.index", index);
					intent.putExtra(WelcomeActivity.KEY_BELONGS_TO,
							DetailFragment.HISTORY);
					intent.putExtra("de.da_sense.moses.client.appname",
							app.getName());
					intent.putExtra("de.da_sense.moses.client.description",
							app.getDescription());
					intent.putExtra(ExternalApplication.KEY_APK_ID,
							app.getID());
					intent.putExtra("de.da_sense.moses.client.apkVersion",
							app.getApkVersion());
					intent.putExtra("de.da_sense.moses.client.startDate",
							app.getStartDateAsString());
					intent.putExtra("de.da_sense.moses.client.endDate",
							app.getEndDateAsString());
					startActivity(intent);

				}
			} else {
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("curChoice", mCurHistPosition);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	/**
	 * Get the installed apps list from the RunningFragment.
	 * 
	 * @return the list of installedExternalApplications
	 */
	public List<HistoryExternalApplication> getHistoryUS() {
		return historyUS;
	}

	/**
	 * Comparator for the history applications.
	 */
	private Comparator<? super HistoryExternalApplication> historyUSListComparator = new Comparator<HistoryExternalApplication>() {
		/**
		 * Comparator for InstalledExternalApplications.
		 * 
		 * @param lhs
		 * @param rhs
		 * @return < 0 if the rhs Date is less than the lhs Date or lhs is null
		 *         and rhs not, 0 if they are equal or both null, > 0 if rhs
		 *         Date is greater or rhs is null and lhs not
		 */
		@Override
		public int compare(HistoryExternalApplication lhs,
				HistoryExternalApplication rhs) {
			return lhs.getID().compareTo(rhs.getID());
		}

	};

	/**
	 * Inits the controls.
	 */
	private void initControls() {
		Log.d(TAG, "initalizing ...");
		refreshHistoryApplications();
	}

	/**
	 * Refresh the list of installed Applications from the user studies.
	 */
	private void refreshHistoryApplications() {
		if (HistoryExternalApplicationsManager.getInstance() == null)
			HistoryExternalApplicationsManager.init(mActivity);
		historyUS = sortForDisplay(new LinkedList<HistoryExternalApplication>(
				HistoryExternalApplicationsManager.getInstance().getApps()));
		populateList(historyUS);
	}

	/**
	 * Sort the list of applications to display them.
	 * 
	 * @param linkedList
	 *            the list to sort
	 * @return the sorted list
	 */
	private List<HistoryExternalApplication> sortForDisplay(
			Collection<HistoryExternalApplication> linkedList) {
		Log.d(TAG, "sorting the list of history applications to display it.");
		if (linkedList == null) {
			throw new RuntimeException("history app list was null");
		}
		List<HistoryExternalApplication> sortedList = new ArrayList<HistoryExternalApplication>(
				linkedList);
		Comparator<? super HistoryExternalApplication> comparator = historyUSListComparator;
		Collections.sort(sortedList, comparator);
		return sortedList;
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshHistoryApplications();
	}

	/**
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		thisInstance = this;
		Log.d(TAG, "onCreate: parentActivity = "
				+ mActivity.getClass().getSimpleName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView about to inflate View");
		View historyFragmentView = inflater.inflate(R.layout.tab_history,
				container, false);
		container.setBackgroundColor(getResources().getColor(
				android.R.color.background_light));

		return historyFragmentView;
	}
	
	

	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
	}

	/**
	 * Shows the given applications in the list.
	 * 
	 * @param applications
	 */
	private void populateList(List<HistoryExternalApplication> applications) {
		// get the app names
		String[] items = new String[applications.size()];
		int counter = 0;
		for (HistoryExternalApplication app : applications) {
			items[counter] = app.getName();
			counter++;
		}

		TextView instructionsView = (TextView) mActivity.findViewById(
				R.id.historyHeaderInstructions);
		if (instructionsView != null) {
			if (applications.size() == 0) {
				instructionsView.setText(R.string.historyList_emptyHint);
			} else {
				instructionsView.setText(R.string.historyList_defaultHint);
			}
		}

		List<Map<String, String>> listContent = new LinkedList<Map<String, String>>();
		for (HistoryExternalApplication app : applications) {
			HashMap<String, String> rowMap = new HashMap<String, String>();
			rowMap.put("name", app.getName());
			rowMap.put("endDate", app.isEndDateSet() ? app.getEndDateAsString()
					: "");
			listContent.add(rowMap);
		}

		MosesListAdapter contentAdapter = new MosesListAdapter(mActivity,
				listContent, R.layout.historyuserstudieslistitem, new String[] {
						"name", "endDate" }, new int[] {
						R.id.historyListItemText, R.id.completedOn });

		setListAdapter(contentAdapter);
	}

	/**
	 * Concatenate the stack trace of an exception to one String.
	 * 
	 * @param e
	 *            the exception to concatenate
	 * @return the concatenated String of the exception
	 */
	public static String concatStacktrace(Exception e) {
		String stackTrace = "";
		for (int i = 0; i < e.getStackTrace().length; i++) {
			stackTrace += e.getStackTrace()[i];
		}
		return stackTrace;
	}
}
