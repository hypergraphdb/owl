package org.hypergraphdb.app.owl.versioning.distributed.activity;

/**
 * VOWLException.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 15, 2012
 */
public class VOWLException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6193401551261740906L;

	/**
	 * 
	 */
	public VOWLException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public VOWLException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public VOWLException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public VOWLException(Throwable cause) {
		super(cause);
	}
}