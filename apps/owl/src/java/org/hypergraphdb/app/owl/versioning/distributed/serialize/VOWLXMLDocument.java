package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.text.DateFormat;

import java.util.List;
import java.util.Locale;
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
	private String ontologyID;
	private String versionedID;

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

	public String getOntologyID()
	{
		return ontologyID;
	}

	public void setOntologyID(String ontologyID)
	{
		this.ontologyID = ontologyID;
	}

	public String getVersionedID()
	{
		return versionedID;
	}

	public void setVersionedID(String versionedID)
	{
		this.versionedID = versionedID;
	}
}