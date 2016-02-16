package org.hypergraphdb.app.owl.util;

import java.util.concurrent.Callable;

import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.ChangeLink;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.event.HGAtomAddedEvent;
import org.hypergraphdb.event.HGAtomRemovedEvent;
import org.hypergraphdb.event.HGEvent;
import org.hypergraphdb.event.HGListener;
import org.hypergraphdb.storage.BAtoHandle;
import org.hypergraphdb.type.HGHandleType;
import org.hypergraphdb.util.HGUtils;

/**
 * <p>
 * Contains listener implementations for maintaining a kind of indexing
 * not supported by the HyperGraphDB framework. We want to provide quick
 * access to all revision that have no children (i.e. all head revisions)
 * as well as quick navigation from child to parent revisions. Child to parent
 * would be easy with the TargetToTarget indexer, but indexing childless revisions
 * is much trickier because it's about indexing the "absence" of something, namely
 * the absence of a link. 
 * </p>
 * 
 * <p>
 * This is an implementation level class, not to be used as an API by clients.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class TrackRevisionStructure
{
	public static final String REVISION_CHILD_INDEX = "OWL_REVISION_CHILDREN";
	
	public static HGIndex<HGPersistentHandle, HGPersistentHandle> revisionChildIndex(HyperGraph graph)
	{
		return graph.getStore().getIndex(REVISION_CHILD_INDEX, 
										 BAtoHandle.getInstance(graph.getHandleFactory()), 
										 BAtoHandle.getInstance(graph.getHandleFactory()), 
										 new HGHandleType.HandleComparator(), 
										 null,
										 true);
	}
	
	public static class AddRevisionOrParentListener implements HGListener 
	{
		private HGListener.Result doHandle(HyperGraph graph, HGEvent event)
		{
			Object atom = graph.get(((HGAtomAddedEvent)event).getAtomHandle());
			if (atom instanceof Revision)
			{
				Revision revision = (Revision)atom;
				VersionedOntology versioned = graph.get(revision.versioned());	
				if (versioned == null)
					throw new IllegalArgumentException("BUG: No versioned for revision.");
				revisionChildIndex(graph).addEntry(
						versioned.getBottomRevision().getPersistent(),
						revision.getAtomHandle().getPersistent());		
			}
			else if (atom instanceof ChangeLink)
			{
				ChangeLink parentLink = (ChangeLink)atom;
				Object child = graph.get(parentLink.child());
				Object parent = graph.get(parentLink.parent());
				if (child instanceof Revision && parent instanceof Revision)
				{
					Revision childRevision = (Revision)child;
					Revision parentRevision = (Revision)parent;
					if (HGUtils.eq(childRevision.branchHandle(), parentRevision.branchHandle()))
					{
						HGIndex<HGPersistentHandle, HGPersistentHandle> idx = revisionChildIndex(graph);
						VersionedOntology versioned = graph.get(((Revision)child).versioned());
						idx.removeEntry(versioned.getBottomRevision().getPersistent(), 
										parentLink.parent().getPersistent());
						idx.addEntry(parentLink.child().getPersistent(), parentLink.parent().getPersistent());
					}
				}
			}
			return Result.ok;			
		}
		
		public HGListener.Result handle(final HyperGraph graph, final HGEvent event)
		{
			return graph.getTransactionManager().ensureTransaction(new Callable<HGListener.Result>(){
				public HGListener.Result call()
				{
					return doHandle(graph, event);
				}
			});
		}
	}

	public static class RemoveRevisionOrParentListener implements HGListener 
	{
		public HGListener.Result handle(final HyperGraph graph, final HGEvent event)
		{
			return graph.getTransactionManager().ensureTransaction(new Callable<HGListener.Result>(){
				public HGListener.Result call()
				{
					return doHandle(graph, event);
				}
			});			
		}
		
		private HGListener.Result doHandle(HyperGraph graph, HGEvent event)
		{
			Object atom = graph.get(((HGAtomRemovedEvent)event).getAtomHandle());
			if (atom instanceof Revision)
			{
				Revision revision = (Revision)atom;
				VersionedOntology versioned = graph.get(revision.versioned());				
				revisionChildIndex(graph).removeEntry(versioned.getBottomRevision().getPersistent(), 
													  revision.getAtomHandle().getPersistent());
			}
			else if (atom instanceof ChangeLink)
			{
				ChangeLink parentLink = (ChangeLink)atom;
				Object child = graph.get(parentLink.child());
				Object parent = graph.get(parentLink.parent());
				if (child instanceof Revision && parent instanceof Revision)
				{
					Revision childRevision = (Revision)child;
					Revision parentRevision = (Revision)parent;
					if (HGUtils.eq(childRevision.branchHandle(), parentRevision.branchHandle()))
					{
						HGIndex<HGPersistentHandle, HGPersistentHandle> idx = revisionChildIndex(graph);					
						idx.removeEntry(parentLink.child().getPersistent(), parentLink.parent().getPersistent());
						if (idx.findFirst(parentLink.child().getPersistent()) == null)
						{
							VersionedOntology versioned = graph.get(((Revision)parent).versioned());						
							idx.addEntry(versioned.getBottomRevision().getPersistent(),
										 parentLink.parent().getPersistent());
						}
					}
				}
			}
			return Result.ok;
		}
	}
}