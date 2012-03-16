package org.hypergraphdb.app.owl.versioning.distributed.activity;


/**
 * VOWLSourceTargetConflictException.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 15, 2012
 */
public class VOWLSourceTargetConflictException extends AbstractVOWLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2734912790210221622L;

	/**
	 * 
	 */
	public VOWLSourceTargetConflictException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public VOWLSourceTargetConflictException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public VOWLSourceTargetConflictException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public VOWLSourceTargetConflictException(Throwable cause) {
		super(cause);
	}

}
