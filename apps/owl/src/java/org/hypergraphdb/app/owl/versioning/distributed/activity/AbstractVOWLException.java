package org.hypergraphdb.app.owl.versioning.distributed.activity;

/**
 * AbstractVOWLException.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 15, 2012
 */
public abstract class AbstractVOWLException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6193401551261740906L;

	/**
	 * 
	 */
	public AbstractVOWLException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AbstractVOWLException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public AbstractVOWLException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public AbstractVOWLException(Throwable cause) {
		super(cause);
	}
}