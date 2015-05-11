package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.hypergraphdb.app.owl.newver.ChangeSet;
import org.hypergraphdb.app.owl.newver.Revision;
import org.hypergraphdb.app.owl.newver.VersionedOntology;

/**
 * This class represents the toplevel structure of an VOWLXML document. Use this
 * to see, what the parsed versionedOntology contains in relation to the
 * original ontology.
 * 
 * VOWLXMLDocument.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class VOWLXMLDocument
{

	public final static DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, Locale.US);

	private VOWLXMLRenderConfiguration renderConfig;
	private List<Revision> revisions;
	private List<ChangeSet<VersionedOntology>> changesets;

	private OWLOntologyEx revisionData;
	private HGPersistentHandle versionedOntologyID;

	/**
	 * 
	 * @param vo
	 *            the VersionedOntology to merge the parsed results into; may be
	 *            null.
	 * @param onto
	 *            the empty OWLOntoloy to add all revision Data to; may be null.
	 */
	public VOWLXMLDocument(OWLOntologyEx onto)
	{
		setRenderConfig(new VOWLXMLRenderConfiguration());
		setRevisionData(onto);
	}
	
	/**
	 * True if this object represents a full versionedOntology with or without
	 * uncommitted changes. This is the case, if all changesets plus the last
	 * revision data is contained.
	 * 
	 * @return
	 */
	public boolean isCompleteVersionedOntology()
	{
		if (revisions.contains(renderConfig.firstRevision()))
			return false;
		if (!revisions.containsAll(renderConfig.heads()))
			return false;
		return true;
	}
	
	/**
	 * 
	 * @return the revisions
	 */
	public List<Revision> getRevisions()
	{
		return revisions;
	}

	/**
	 * @param revisions
	 *            the revisions to set
	 */
	public void setRevisions(List<Revision> revisions)
	{
		this.revisions = revisions;
	}

	/**
	 * @return the changesets of the versionedOntology or
	 */
	public List<ChangeSet<VersionedOntology>> getChangesets()
	{
		return changesets;
	}

	/**
	 * @param changesets
	 *            the changesets to set
	 */
	public void setChangesets(List<ChangeSet<VersionedOntology>> changesets)
	{
		this.changesets = changesets;
	}

	/**
	 * @return the renderConfig
	 */
	public VOWLXMLRenderConfiguration getRenderConfig()
	{
		return renderConfig;
	}

	/**
	 * @param renderConfig
	 *            the renderConfig to set
	 */
	public void setRenderConfig(VOWLXMLRenderConfiguration renderConfig)
	{
		this.renderConfig = renderConfig;
	}

	/**
	 * @return the revisionData
	 */
	public OWLOntologyEx getRevisionData()
	{
		return revisionData;
	}

	/**
	 * @param revisionData
	 *            the revisionData to set
	 */
	public void setRevisionData(OWLOntologyEx revisionData)
	{
		this.revisionData = revisionData;
	}

	/**
	 * @return the versionedOntologyID
	 */
	public HGPersistentHandle getVersionedOntologyID()
	{
		return versionedOntologyID;
	}

	/**
	 * @param versionedOntologyID
	 *            the versionedOntologyID to set
	 */
	public void setVersionedOntologyID(HGPersistentHandle versionedOntologyID)
	{
		this.versionedOntologyID = versionedOntologyID;
	}
}