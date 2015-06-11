package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;

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
	private Set<HGHandleHolder> revisionObjects = new HashSet<HGHandleHolder>();
	private Map<ChangeSet<VersionedOntology>, List<VOWLChange>> changeSetMap = new HashMap<ChangeSet<VersionedOntology>, List<VOWLChange>>();
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
			
	public Set<HGHandleHolder> revisionObjects() throws OWLXMLParserException
	{
		return revisionObjects;
	}

	public Map<ChangeSet<VersionedOntology>, List<VOWLChange>> changeSetMap()
	{
		return changeSetMap;
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