package moses.client.abstraction.apks;

import moses.client.abstraction.ApkMethods;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * References an installed application (additional to ExternalApplication, the
 * package name of the installed application must be specified)
 * 
 * @author Simon L
 * 
 */
public class InstalledExternalApplication extends ExternalApplication {

	private String packageName;

	/**
	 * Creates the reference to the external application by specifying the
	 * package name
	 * 
	 * @param packageName
	 *            the name of the package of the application
	 * @param ID
	 *            the moses id of the application
	 * @param appContext
	 */
	public InstalledExternalApplication(String packageName, String ID) {
		super(ID);
		this.packageName = packageName;
	}

	/**
	 * Creates the instance by adapting an already existing ExternalApplication
	 * reference. This has the advantage of copying already retrieved name and
	 * description over.
	 * 
	 * @param packageName
	 *            the package name of the installed app
	 * @param externalApp
	 *            the preexisting reference that will be adapted
	 */
	public InstalledExternalApplication(String packageName, ExternalApplication externalApp) {
		this(packageName, externalApp.getID());
		if (externalApp.isDescriptionSet()) {
			setDescription(externalApp.getDescription());
		}
		if (externalApp.isNameSet()) {
			setName(externalApp.getName());
		}
	}

	/**
	 * starts the application this object is referencing
	 * 
	 * @param baseActivity
	 * @throws NameNotFoundException
	 *             should only occur if the application was uninstalled after
	 *             the creation of this InstalledExternalApplication instance.
	 */
	public void startApplication(Activity baseActivity) throws NameNotFoundException {
		ApkMethods.startApplication(packageName, baseActivity);
	}

	/**
	 * @return the package name of the referenced application
	 */
	public String getPackageName() {
		return packageName;
	}

	@Override
	public String toString() {
		return packageName;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof InstalledExternalApplication) {
			if (this.getPackageName() == null) return false;
			return this.getPackageName().equals(((InstalledExternalApplication) o).getPackageName());
		} else {
			return false;
		}
	}

}