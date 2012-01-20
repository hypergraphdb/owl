package org.hypergraphdb.app.owl.versioning;

import java.util.Date;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;


/**
 * Revision represents the first ontology or a revised version of the ontology.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 18, 2012
 */
public class Revision extends RevisionID implements HGHandleHolder {
	
	public static final long TIMESTAMP_UNDEFINED = Long.MIN_VALUE; 
	public static final String USER_ANONYMOUS = null; 
	
	Date timeStamp;
	String user;
//	boolean hasRevisionData;
	HGHandle handle;

	public Revision() {
		//do nothing
	}
	
	public Date getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @param timeStamp the timeStamp to set
	 */
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	
	public String getUser() {
		return user;
	}	
	
	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

//	
//	public OWLOntology getRevisionData() {
//		if (hasRevisionData) {
//			return graph.get(super.getOntologyID());
//		} else {
//			throw new IllegalStateException("No revision data for " + this);
//		}
//	}
	
//	/**
//	 * @return the hasRevisionData
//	 */
//	public boolean isHasRevisionData() {
//		return hasRevisionData;
//	}
//
//	/**
//	 * @param hasRevisionData the hasRevisionData to set
//	 */
//	public void setHasRevisionData(boolean hasRevisionData) {
//		this.hasRevisionData = hasRevisionData;
//	}


	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGHandleHolder#getAtomHandle()
	 */
	@Override
	public HGHandle getAtomHandle() {
		return handle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGHandleHolder#setAtomHandle(org.hypergraphdb.HGHandle)
	 */
	@Override
	public void setAtomHandle(HGHandle handle) {
		this.handle = handle;
		
	}
}
