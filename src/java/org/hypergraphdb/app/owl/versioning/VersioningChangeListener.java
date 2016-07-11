package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

/**
 * <p>
 * This is tracking ontology changes going to the main ontology manager for versioning
 * purposes: each change is recorded in the current working {@link ChangeSet}.
 * </p>
 * 
 * @author borislav
 *
 */
public class VersioningChangeListener implements OWLOntologyChangeListener
{
	private VersionManager repo;

	public VersioningChangeListener(VersionManager repo)
	{
		this.repo = repo;
	}

	@Override
	public void ontologiesChanged(final List<? extends OWLOntologyChange> changes) throws OWLException
	{
		final List<VersionedOntology> ontologies = new ArrayList<VersionedOntology>();
		repo.graph().getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				for (OWLOntologyChange c : changes)
				{
					HGHandle ontoHandle = repo.graph().getHandle(c.getOntology());
					if (ontoHandle == null)
						continue;
					if (repo.isVersioned(ontoHandle))
					{
						VersionedOntology vo = repo.versioned(ontoHandle);
						VOWLChange vc = VOWLChangeFactory.create(c, repo.graph());
						vo.changes().add(vc);
						ontologies.add(vo);
					}
				}
				return null;
			}
		});
		// We'd like to pack the working set changes here, but in order to properly do that, we need 
		// to operate within a working copy that doesn't already include them. Otherwise, the 
		// 'isEffective' method always returns false and all changes get removed. Since a lot of 
		// the OWLAPI in memory structure are not transactional, I'm not sure whether in a single
		// transaction one can rollback working changes so the working ontology appears to the transaction
		// without them, pack the set of changes and only re-apply the ones that are left. 
		//
		// That is way, we can't really do the packing automatically here until transactionality is ensured
		// and properly tested. 
		//
		// However, packing can be done through the UI as a single (b)locking operation. 
//		for (VersionedOntology vo : ontologies)
//			vo.changes().pack(vo);		
	}
}