package org.hypergraphdb.app.owl.versioning;

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
					}
				}
				return null;
			}
		});
	}
}