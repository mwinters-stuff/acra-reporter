package nz.org.winters.appspot.acrareporter.shared;

import java.io.Serializable;

public class NotLoggedInException extends Exception implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 991337848176003513L;

	public NotLoggedInException() {
		super();
	}

	public NotLoggedInException(String message) {
		super(message);
	}

}
