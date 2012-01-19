package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HyperGraph;
import org.semanticweb.owlapi.model.OWLOntology;

import sun.util.calendar.BaseCalendar.Date;

/**
 * Revision.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 18, 2012
 */
public class Revision extends RevisionID implements HGGraphHolder {
	
	public static final long TIMESTAMP_UNDEFINED = Date.TIME_UNDEFINED; 
	
	Date timeStamp;
	
	String user;
	
//	boolean hasRevisionData;
		
	HyperGraph graph;

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

//	//public ChangeSet getPreviousChangeSet();
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
	 * @see org.hypergraphdb.HGGraphHolder#setHyperGraph(org.hypergraphdb.HyperGraph)
	 */
	@Override
	public void setHyperGraph(HyperGraph graph) {
		this.graph = graph;
	}
}
