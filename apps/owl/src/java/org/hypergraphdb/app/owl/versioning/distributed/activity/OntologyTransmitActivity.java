package org.hypergraphdb.app.owl.versioning.distributed.activity;

import java.util.UUID;

import org.hypergraphdb.app.owl.versioning.distributed.ClientCentralizedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.PeerDistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.ServerCentralizedOntology;
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
	public static final String KEY_LAST_MATCHING_REVISION = "LastMatchingRevision";
	public static final String KEY_DISTRIBUTION_MODE = "DistributionMode";
	public static final String VALUE_DISTRIBUTION_MODE_CLIENT_SERVER = "ClientServer";
	public static final String VALUE_DISTRIBUTION_MODE_PEER = "Peer";
	public static final String KEY_LAST_REQUESTED_REVISION = "LastRequestedRevision";

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

	public static String getDistributionModeFor(DistributedOntology dOnto)
	{
		if (dOnto instanceof PeerDistributedOntology)
		{
			return VALUE_DISTRIBUTION_MODE_PEER;
		}
		else if (dOnto instanceof ClientCentralizedOntology)
		{
			return VALUE_DISTRIBUTION_MODE_CLIENT_SERVER;
		}
		else if (dOnto instanceof ServerCentralizedOntology)
		{
			return VALUE_DISTRIBUTION_MODE_CLIENT_SERVER;
		}
		else
		{
			throw new IllegalStateException("Cannot determine valid distribution mode based on distributed ontology. Was:  "
					+ dOnto);
		}
	}

	/**
	 * This will throw a meaningful Exceptions, if the push mode does not match
	 * the distributed ontology found on the target.
	 * 
	 * @param dvoAtTarget
	 *            the distributed ontology that exists on target
	 * @param distributionMode
	 *            the distribution mode (requested by the client)
	 */
	protected void targetAssertDistributionModeMatches(DistributedOntology dvoAtTarget, String distributionMode)
	{
		if (distributionMode.equals(VALUE_DISTRIBUTION_MODE_CLIENT_SERVER))
		{
			if (!(dvoAtTarget instanceof ServerCentralizedOntology))
			{
				throw new RuntimeException(new VOWLSourceTargetConflictException(
						"Target distributed ontology not appropriate for client/server distributionMode."));
			}
		}
		else if (distributionMode.equals(VALUE_DISTRIBUTION_MODE_PEER))
		{
			if (!(dvoAtTarget instanceof PeerDistributedOntology))
			{
				throw new RuntimeException(new VOWLSourceTargetConflictException(
						"Target distributed ontology not appropriate for peer2peer distributionMode mode."));
			}
		}
		else
		{
			throw new IllegalArgumentException("Distribution mode not recognized on target: " + distributionMode
					+ " Only C/S and peer are available.");
		}
	}

}
