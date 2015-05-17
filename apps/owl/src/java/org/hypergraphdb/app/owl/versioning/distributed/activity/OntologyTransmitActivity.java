package org.hypergraphdb.app.owl.versioning.distributed.activity;

import java.util.UUID;

import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.workflow.FSMActivity;

/**
 * OntologyTransmitActivity.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 4, 2012
 */
public abstract class OntologyTransmitActivity extends FSMActivity
{
	public static final String KEY_ONTOLOGY_UUID = "OntologyUUID";
	public static final String KEY_REFERENCE_HEADS = "referenceHeads";

	/**
	 * @param thisPeer
	 */
	public OntologyTransmitActivity(HyperGraphPeer thisPeer)
	{
		super(thisPeer);
	}

	/**
	 * @param thisPeer
	 * @param id
	 */
	public OntologyTransmitActivity(HyperGraphPeer thisPeer, UUID id)
	{
		super(thisPeer, id);
	}
}
