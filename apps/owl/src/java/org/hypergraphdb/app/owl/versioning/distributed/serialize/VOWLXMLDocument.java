package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;

/**
 * This class represents the toplevel structure of an VOWLXML document.
 * Use this to see, what the parsed versionedOntology contains in relation 
 * to the original ontology. 
 * 
 * VOWLXMLDocument.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class VOWLXMLDocument {
	
    public final static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.US);

    private VOWLXMLRenderConfiguration renderConfig;
	//private VersionedOntology versionedOntology;
	private List<Revision> revisions;
	private List<ChangeSet> changesets;
	
	private OWLOntologyEx revisionData;
	private int headRevisionIndex;
	private HGPersistentHandle versionedOntologyID;
	
	/**
	 * 
	 * @param vo the VersionedOntology to merge the parsed results into; may be null.
	 * @param onto the empty OWLOntoloy to add all revision Data to; may be null.
	 */
	public VOWLXMLDocument(OWLOntologyEx onto) {
		setRenderConfig(new VOWLXMLRenderConfiguration());
		//setVersionedOntology(vo);
		setRevisionData(onto);
	}
	
	public boolean isValid() {
		int nrOfRevisionsConfigured;
		if (renderConfig.getFirstRevisionIndex() != -1) {
			//we have at least 1 revision
			nrOfRevisionsConfigured = renderConfig.getLastRevisionIndex() - renderConfig.getFirstRevisionIndex() + 1;
		} else {
			nrOfRevisionsConfigured = 0;
		}
		if (revisions.size() != nrOfRevisionsConfigured) {
			System.err.println("revisions.size() != nrOfRevisionsConfigured");
			System.err.println("" + revisions.size() + " != " + nrOfRevisionsConfigured);
			return false;
		}
		if (renderConfig.isUncommittedChanges()) {
			if (changesets.size() != nrOfRevisionsConfigured) {
				System.err.println("changesets.size() != nrOfRevisionsConfigured");
				System.err.println("" + revisions.size() + " != " + nrOfRevisionsConfigured);
				return false;
			}
		} else {
			if (changesets.size() != nrOfRevisionsConfigured - 1) {
				System.err.println("changesets.size() != nrOfRevisionsConfigured - 1 (no uncommmitted)");
				System.err.println("" + revisions.size() + " != " + nrOfRevisionsConfigured);
				return false;
			}
		}
		return true;
	}

	/**
	 * True if this object represents a full versionedOntology with or without uncommitted changes.
	 * This is the case, if all changesets plus the last revision data is contained.
	 * @return
	 */
	public boolean isCompleteVersionedOntology() {
		return renderConfig.getFirstRevisionIndex() == 0
		&& (renderConfig.getLastRevisionIndex() >= headRevisionIndex)
		&& renderConfig.isLastRevisionData();
	}
	
//	/**
//	 * Will be null, if no 
//	 * @return the versionedOntology
//	 */
//	public VersionedOntology getVersionedOntology() {
//		return versionedOntology;
//	}
//	/**
//	 * Also sets revisions and changesets. 
//	 * @param versionedOntology the versionedOntology to set
//	 */
//	public void setVersionedOntology(VersionedOntology versionedOntology) {
//		this.versionedOntology = versionedOntology;
//	}
	/**
	 * 
	 * @return the revisions
	 */
	public List<Revision> getRevisions() {
		return revisions;
	}

	/**
	 * @param revisions the revisions to set
	 */
	public void setRevisions(List<Revision> revisions) {
		this.revisions = revisions;
	}

	/**
	 * @return the changesets of the versionedOntology or   
	 */
	public List<ChangeSet> getChangesets() {
		return changesets;
	}

	/**
	 * @param changesets the changesets to set
	 */
	public void setChangesets(List<ChangeSet> changesets) {
		this.changesets = changesets;
	}

	/**
	 * @return the renderConfig
	 */
	public VOWLXMLRenderConfiguration getRenderConfig() {
		return renderConfig;
	}
	/**
	 * @param renderConfig the renderConfig to set
	 */
	public void setRenderConfig(VOWLXMLRenderConfiguration renderConfig) {
		this.renderConfig = renderConfig;
	}
	/**
	 * @return the revisionData
	 */
	public OWLOntologyEx getRevisionData() {
		return revisionData;
	}
	/**
	 * @param revisionData the revisionData to set
	 */
	public void setRevisionData(OWLOntologyEx revisionData) {
		this.revisionData = revisionData;
	}
	/**
	 * @return the headRevisionIndex
	 */
	public int getHeadRevisionIndex() {
		return headRevisionIndex;
	}
	/**
	 * @param headRevisionIndex the headRevisionIndex to set
	 */
	public void setHeadRevisionIndex(int headRevisionIndex) {
		this.headRevisionIndex = headRevisionIndex;
	}

	/**
	 * @return the versionedOntologyID
	 */
	public HGPersistentHandle getVersionedOntologyID() {
		return versionedOntologyID;
	}

	/**
	 * @param versionedOntologyID the versionedOntologyID to set
	 */
	public void setVersionedOntologyID(HGPersistentHandle versionedOntologyID) {
		this.versionedOntologyID = versionedOntologyID;
	}	
}