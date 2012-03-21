package moses.client.abstraction.apks;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;

import moses.client.abstraction.ApkDownloadLinkRequestObserver;
import moses.client.abstraction.ApkMethods;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Abstraction for downloading a single application package downloadedApk, given
 * an {@link ExternalApplication} object (specified only by moses application
 * id).
 * 
 * Supports the {@link Observable} scheme for updates in the download process.
 * See {@link State}.
 * 
 * @author Simon L
 * 
 */
public class ApkDownloadManager extends Observable implements ApkDownloadObserver, ApkDownloadLinkRequestObserver {

	public static enum State {
		JUST_INITIALIZED, DOWNLOAD_LINK_REQUESTED, APK_FILE_REQUESTED_DOWNLOADING, FINISHED, ERROR
	}

	private ExternalApplication app;
	private Context context;
	private String errorMsg;
	private State state;
	private ExternalApplication externalApplicationResult;
	private File downloadedApk;

	/**
	 * Creates this download manager with an observer which will be notified
	 * every time the state of the download process changed (including errors).
	 * 
	 * @param externalApp
	 *            the external Application to download
	 * @param applicationContext
	 *            the context
	 */
	public ApkDownloadManager(ExternalApplication externalApp, Context applicationContext) {
		super();
		this.app = externalApp;
		this.context = applicationContext;
		setState(State.JUST_INITIALIZED);
	}

	/**
	 * start the download process (using a generated file name for the apk
	 * file).
	 */
	public void start() {
		requestUrlForApplication(this.app);
	}

	/**
	 * sets the state of this downloading process aand notifies observers about it
	 * 
	 * @param state
	 */
	private void setState(State state) {
		this.state = state;
		this.setChanged();
		this.notifyObservers(state);
	}

	/**
	 * sets the error state with a message as indicator of what wet wrong
	 * 
	 * @param errorMsg
	 */
	private void setErrorState(String errorMsg) {
		setErrorState(errorMsg, null);
	}

	/**
	 * sets the error state, with a message and a throwable as indicator of what went wrong
	 * 
	 * @param errorMsg error message
	 * @param e throwable that was thrown when the error occured
	 */
	private void setErrorState(String errorMsg, Throwable e) {
		this.errorMsg = errorMsg;
		if (e != null) {
			Log.e("MoSeS.Download", errorMsg, e);
		} else {
			Log.e("MoSeS.Download", errorMsg);
		}

		Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
		setState(State.ERROR);
	}

	private void requestUrlForApplication(ExternalApplication app) {
		setState(State.DOWNLOAD_LINK_REQUESTED);
		ApkMethods.getDownloadLinkFor(app, this);
	}

	private void requestApkDownload(URL url) {
		ApkDownloadTask downloadTask = new ApkDownloadTask(this, url, this.context, generateApkFileNameFor(app));
		downloadTask.setExternalApplicationReference(app);
		setState(State.APK_FILE_REQUESTED_DOWNLOADING);
		downloadTask.execute();
	}

	@Override
	public void apkDownloadLinkRequestFinished(String urlString, ExternalApplication app) {
		// fire download of apk
		try {
			URL url = new URL(urlString);
			requestApkDownload(url);
		} catch (MalformedURLException e) {
			Toast.makeText(this.context, "Server sent malformed url; could not download application: " + urlString,
				Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void apkDownloadLinkRequestFailed(Exception e) {
		String errorMsg = "Downloadlink request failed";
		setErrorState(errorMsg, e);
	}

	@Override
	public void apkDownloadFinished(ApkDownloadTask downloader, File result, ExternalApplication externalAppRef) {
		this.downloadedApk = result;
		this.externalApplicationResult = externalAppRef;
		setState(State.FINISHED);
	}

	@Override
	public void apkDownloadFailed(ApkDownloadTask downloader) {
		String errorMsg = "Download failed";
		setErrorState(errorMsg, downloader.getDownloadException());
	}

	private static String generateApkFileNameFor(ExternalApplication app) {
		return app.getID() + ".apk";
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public State getState() {
		return state;
	}

	public ExternalApplication getExternalApplicationResult() {
		return externalApplicationResult;
	}

	public File getDownloadedApk() {
		return downloadedApk;
	}

}
