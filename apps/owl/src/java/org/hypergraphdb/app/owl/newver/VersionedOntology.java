package org.hypergraphdb.app.owl.newver;

import java.util.List;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.ChangeSet;

/**
 * <p>
 * Represents an active working copy of a versioned ontology. There may be
 * multiple such active copies within a repository. They are distinguished by
 * different version IRIs.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class VersionedOntology implements Versioned, HGGraphHolder
{
	private HyperGraph graph;
	private HGHandle ontology;
	private HGHandle currentRevision;
	private HGHandle workingChanges;

	private ChangeMark getMarkForRevision(HGHandle revisionHandle)
	{
		List<ChangeMark> L = graph.getAll(hg.apply(hg.targetAt(graph, 1),
										  hg.and(hg.type(RevisionMark.class), 
												 hg.incident(revisionHandle))));
		for (ChangeMark mark : L)
			if (mark.target().equals(ontology))
				return mark;
		return null;
	}

	public VersionedOntology()
	{
	}

	public VersionedOntology(HyperGraph graph, HGHandle ontology, HGHandle currentRevision, HGHandle changes)
	{
		this.graph = graph;
		this.ontology = ontology;
		this.currentRevision = currentRevision;
		this.workingChanges = changes;
	}

	@Override
	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
	}

	public Revision revision()
	{
		return graph.get(currentRevision);
	}

	@Override
	public Revision commit(final String user, final String comment)
	{
		currentRevision = graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>(){
		public HGHandle call()
		{
			ChangeMark mark = flushChanges();
			Revision revision = new Revision();
			revision.setUser(user);
			revision.setComment(comment);
			revision.setTimestamp(System.currentTimeMillis());
			HGHandle revisionHandle = graph.add(revision);
			graph.add(new RevisionMark(revisionHandle, graph.getHandle(mark)));
			graph.add(new MarkParent(revisionHandle, currentRevision));
			return revisionHandle;
		}
		});
		return revision();
	}

	@Override
	public Revision merge(String user, String comment, Revision... revisions)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ChangeMark flushChanges()
	{
		ChangeMark markCurrent = getMarkForRevision(currentRevision);
		ChangeMark newmark = new ChangeMark(ontology, workingChanges);
		newmark.setTimestamp(System.currentTimeMillis());
		HGHandle markHandle = graph.add(newmark);
		graph.add(new MarkParent(markHandle, graph.getHandle(markCurrent)));
		workingChanges = graph.add(new ChangeSet());
		return newmark;
	}

	@Override
	public ChangeSet changes()
	{
		return graph.get(workingChanges);
	}

	public ChangeSet changes(Revision revision)
	{
		ChangeMark mark = getMarkForRevision(graph.getHandle(revision));
		return graph.get(mark.changeset());
	}

	public HGDBOntology ontology()
	{
		return graph.get(ontology);
	}

	public HGHandle getOntology()
	{
		return ontology;
	}

	public void setOntology(HGHandle ontology)
	{
		this.ontology = ontology;
	}

	public HGHandle getCurrentRevision()
	{
		return currentRevision;
	}

	public void setCurrentRevision(HGHandle currentRevision)
	{
		this.currentRevision = currentRevision;
	}

	public HGHandle getWorkingChanges()
	{
		return workingChanges;
	}

	public void setWorkingChanges(HGHandle workingChanges)
	{
		this.workingChanges = workingChanges;
	}
}