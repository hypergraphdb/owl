package org.hypergraphdb.app.owl.versioning;

import java.util.Date;

/**
 * Revision represents the first ontology or a revised version of the ontology.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 18, 2012
 */
public class Revision extends RevisionID implements /* HGHandleHolder , */ VersioningObject {
	
	public static final long TIMESTAMP_UNDEFINED = Long.MIN_VALUE; 
	public static final String USER_ANONYMOUS = null; 
	
	private Date timeStamp;
	private String user; 
	private String revisionComment;
//	boolean hasRevisionData;
	//private HGHandle handle;

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

	public String getRevisionComment() {
		return revisionComment;
	}	
	
	/**
	 * @param comment the comment to set.
	 */
	public void setRevisionComment(String comment) {
		this.revisionComment = comment;
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

//	/* (non-Javadoc)
//	 * @see org.hypergraphdb.HGHandleHolder#getAtomHandle()
//	 */
//	@Override
//	public HGHandle getAtomHandle() {
//		return handle;
//	}

//	/* (non-Javadoc)
//	 * @see org.hypergraphdb.HGHandleHolder#setAtomHandle(org.hypergraphdb.HGHandle)
//	 */
//	@Override
//	public void setAtomHandle(HGHandle handle) {
//		this.handle = handle;
//		
//	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.VersioningObject#accept(org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor)
	 */
	@Override
	public void accept(VOWLObjectVisitor visitor) {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + 7 * ((user != null? user.hashCode(): 0) 
				+ (timeStamp != null? timeStamp.hashCode(): 0) 
				+ (revisionComment != null? revisionComment.hashCode(): 0)); 
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
		    return true;
		}
		if (anObject instanceof Revision) {
			Revision other = (Revision) anObject;
			return super.equals(other) 
				&& equalsInclNull(user, other.user)
				&& equalsInclNull(timeStamp, other.timeStamp)
				&& equalsInclNull(revisionComment, other.revisionComment);
		} else {
			return false;
		}
	}

	protected boolean equalsInclNull(Object thiso, Object other) {
		return (thiso == null && other == null)
			|| (thiso != null && thiso.equals(other));
	}	
}