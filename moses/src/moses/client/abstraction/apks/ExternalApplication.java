package moses.client.abstraction.apks;

/**
 * Reference to an application on the server, referenced by it's MoSeS id
 * 
 * @author Simon L
 *
 */
public class ExternalApplication {
	private String ID;
	
	//lazy loading variables for non-defining attributes 
	volatile private String name;
	volatile private String description;
	
	public String getID() {
		return ID;
	}

	public ExternalApplication(String ID) {
		this.ID = ID;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Returns the name of this external application. If the name was not set, or retrieved yet,
	 * this will be done now. In this case, this method should not be called from the UI thread 
	 * (method blocks).
	 * 
	 * @return 
	 */
	public String getName() {
		if(name != null) {
			return name;
		} else {
			name = retrieveName();
			return getName();
		}
	}

	/**
	 * retrieves the name of this application from the server in case it was not set yet.
	 * @return the name of the application
	 */
	private String retrieveName() {
		//TODO: implement, placeholder
		return "retrieved name for application "+ID;
	}
	
	/**
	 * Returns the description of this external application. If the description was not set, or retrieved yet,
	 * this will be done now. In this case, this method should not be called from the UI thread 
	 * (method blocks).
	 * 
	 * @return 
	 */
	public String getDescription() {
		if(description != null) {
			return description;
		} else {
			description = retrieveDescription();
			return getName();
		}
	}

	/**
	 * retrieves the description of this application from the server in case it was not set yet.
	 * @return the description of the application
	 */
	private String retrieveDescription() {
		//TODO: implement, placeholder
		return "retrieved description for application "+ID;
	}
	
	/**
	 * @return whether the description of the application was already retrieved/set
	 */
	public boolean isDescriptionSet() {
		return description != null;
	}
	
	/**
	 * @return whether the name of the application was already retrieved/set
	 */
	public boolean isNameSet() {
		return name != null;
	}
	
}
