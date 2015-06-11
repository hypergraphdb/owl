package org.hypergraphdb.app.owl.versioning;

import java.util.List;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.newver.VersionManager;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * This is a lightweight object tied to the underlying database. You can create
 * multiple instances of this objects working on the backing database
 * in simultaneously. 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County), Borislav Iordanov
 * @created Jan 18, 2012
 */
public class VHGDBOntologyRepository extends HGDBOntologyRepository // implements OWLOntologyChangeListener
{
	/**
	 * Will print time every 100 changes.
	 */
	private static boolean DBG = false;
	private VersionManager versionManager;
	
	public VHGDBOntologyRepository(String location)
	{
		super(location);
		versionManager = new VersionManager(getHyperGraph(), "fixme-VHDBOntologyRepository");
	}

	public List<VersionedOntology> getVersionControlledOntologies()
	{
		List<VersionedOntology> l = getHyperGraph().getAll(hg.type(VersionedOntology.class));
		return l;
	}

	/**
	 * Returns the Version controlled Ontology or null.
	 * 
	 * @param onto
	 * @return the versioned ontology or null, if not found.
	 */
	public VersionedOntology getVersionControlledOntology(final OWLOntology onto)
	{
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<VersionedOntology>()
		{
			public VersionedOntology call()
			{
				// TODO maybe not loaded here? -> NPE; Check out callers
				HGHandle ontoHandle = getHyperGraph().getHandle(onto);
				if (ontoHandle == null)
				{
					if (DBG)
						System.out.println("NULL for onto " + onto);
					return null;
				}
				else
				{
					HGPersistentHandle ontoPHandle = ontoHandle.getPersistent();
					for (VersionedOntology vo : getVersionControlledOntologies())
					{
						if (vo.getOntology().equals(ontoPHandle))
						{
							return vo;
						}
					}
					return null;
				}
			}
		}, HGTransactionConfig.READONLY);
	}

	/**
	 * Adds version control to the given ontology. A VersionedOntology object
	 * with an initial revision will be created and changes will be recorded.
	 * 
	 * @param o
	 * @param user
	 * @return
	 */
//	public VersionedOntology addVersionControl(final HGDBOntology o, final String user)
//	{
//		final HyperGraph graph = getHyperGraph();
//		if (isVersionControlled(o))
//			throw new IllegalStateException("Ontology already version controlled" + o.getOntologyID());
//		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<VersionedOntology>()
//		{
//			public VersionedOntology call()
//			{
//				VersionedOntology newVO = new VersionedOntology(o, user, graph);
//				graph.add(newVO);
//				return newVO;
//			}
//		});
//	}

	/**
	 * Removes version control.
	 * 
	 * @param vo
	 */
//	public void removeVersionControl(final VersionedOntology vo)
//	{
//		final HyperGraph graph = getHyperGraph();
//		getHyperGraph().getTransactionManager().ensureTransaction(new Callable<Object>()
//		{
//			public Object call()
//			{
//				vo.clear();
//				HGHandle voHandle = graph.getHandle(vo);
//				graph.remove(voHandle, true);
//				return null;
//			}
//		});
//	}
//
//	public boolean isVersionControlled(OWLOntology o)
//	{
//		// TODO optimize this
//		return getVersionControlledOntology(o) != null;
//	}

	//
	// OVERWRITING SUPERCLASS DELETE_ONTOLOGY
	//

	/**
	 * Deletes an ontology from the graph. If it is version controlled, the
	 * associated version controlled ontology will be deleted also.
	 */
	public boolean deleteOntology(final OWLOntologyID ontologyId)
	{
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				HGHandle ontologyHandle = getOntologyHandleByID(ontologyId);
				if (versionManager.isVersioned(ontologyHandle))
					versionManager.removeVersioning(ontologyHandle);
				return VHGDBOntologyRepository.super.deleteOntology(ontologyId);
			}
		});
	}

	// ---------------------------------------------------------------------------------------
	// CHANGE LISTENER IMPLEMENTATION
	// All ontology changes pass through here.
	// ---------------------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLOntologyChangeListener#ontologiesChanged
	 * (java.util.List)
	 */
	private ThreadLocal<Boolean> ignoreChanges = new ThreadLocal<Boolean>();

	public void ignoreChangeEvents(boolean b)
	{
		ignoreChanges.set(b);
	}

	public boolean shouldIgnoreChangeEvents()
	{
		return ignoreChanges.get() != null && ignoreChanges.get();
	}

	// public void ontologiesChanged(final List<? extends OWLOntologyChange>
	// changes) throws OWLException {
	// }
	// ---------------------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLOntologyChangeListener#ontologiesChanged
	 * (java.util.List)
	 */
//	@Override
//	public void ontologiesChanged(final List<? extends OWLOntologyChange> changes) throws OWLException
//	{
//		if (shouldIgnoreChangeEvents())
//			return;
//		getHyperGraph().getTransactionManager().ensureTransaction(new Callable<Object>()
//		{
//			public Object call()
//			{
//				if (DBG_CHANGES)
//					System.out.println("" + new Date() + " VHGDB processes applied changes: " + changes.size());
//				int i = 0;
//				for (OWLOntologyChange c : changes)
//				{
//					if (DBG_CHANGES)
//					{
//						i++;
//						if (i % 100 == 0)
//						{
//							System.out.println("" + new Date() + " VHGDB changes done: " + i);
//						}
//					}
//					if (! (c.getOntology() instanceof HGDBOntology)
//						continue;
//					HGDBOntology hgdbontology = (HGDBOntology)c.getOntology();
//					// get versioned onto
//					if (versionManager.isVersioned(hgdbontology.getAtomHandle()))
//					{
//						VersionedOntology vo = getVersionControlledOntology(c.getOntology());
//						// VOWLChange vc = VOWLChangeFactory.create(c,
//						// getHyperGraph());
//						vo.changes().add(new )
//					}
//				}
//				if (DBG_CHANGES)
//					System.out.println("" + new Date() + " VHGDB changes done: " + i);
//				// forced to use Callable:
//				return null;
//			}
//		});
//	}
}