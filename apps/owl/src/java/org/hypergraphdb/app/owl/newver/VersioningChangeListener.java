package org.hypergraphdb.app.owl.newver;

import java.util.List;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

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
		repo.getGraph().getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				for (OWLOntologyChange c : changes)
				{
					HGHandle ontoHandle = repo.getGraph().getHandle(c.getOntology());
					if (ontoHandle == null)
						continue;
					if (repo.isVersioned(ontoHandle))
					{
						VersionedOntology vo = repo.versioned(ontoHandle);
						VOWLChange vc = VOWLChangeFactory.create(c, repo.getGraph());
						vo.changes().add(vc);
					}
				}
				return null;
			}
		});
	}
}