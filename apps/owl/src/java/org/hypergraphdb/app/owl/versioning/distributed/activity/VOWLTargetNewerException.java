package org.hypergraphdb.app.owl.versioning.distributed.activity;


/**
 * VOWLTargetNewerException.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 15, 2012
 */
public class VOWLTargetNewerException extends AbstractVOWLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1137425108655942001L;

	/**
	 * 
	 */
	public VOWLTargetNewerException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public VOWLTargetNewerException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public VOWLTargetNewerException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public VOWLTargetNewerException(Throwable cause) {
		super(cause);
	}

}
