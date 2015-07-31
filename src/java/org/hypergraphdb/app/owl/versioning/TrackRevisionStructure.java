package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.event.HGAtomAddedEvent;
import org.hypergraphdb.event.HGAtomRemovedEvent;
import org.hypergraphdb.event.HGEvent;
import org.hypergraphdb.event.HGListener;
import org.hypergraphdb.storage.BAtoHandle;
import org.hypergraphdb.type.HGHandleType;

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
 * 
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
										 true);
	}
	
	public static class AddRevisionOrParentListener implements HGListener 
	{
		public HGListener.Result handle(HyperGraph graph, HGEvent event)
		{
			Object atom = graph.get(((HGAtomAddedEvent)event).getAtomHandle());
			if (atom instanceof Revision)
			{
				Revision revision = (Revision)atom;
				VersionedOntology versioned = graph.get(revision.versioned());				
				revisionChildIndex(graph).addEntry(
						versioned.getBottomRevision().getPersistent(),
						revision.getAtomHandle().getPersistent());
			}
			else if (atom instanceof ParentLink)
			{
				ParentLink parentLink = (ParentLink)atom;
				Object child = parentLink.child();
				Object parent = parentLink.parent();
				if (child instanceof Revision && parent instanceof Revision)
				{
					HGIndex<HGPersistentHandle, HGPersistentHandle> idx = revisionChildIndex(graph);
					VersionedOntology versioned = graph.get(((Revision)child).versioned());
					idx.removeEntry(versioned.getBottomRevision().getPersistent(), 
									parentLink.parent().getPersistent());
					idx.addEntry(parentLink.child().getPersistent(), parentLink.parent().getPersistent());
				}
			}
			return Result.ok;
		}
	}

	public static class RemoveRevisionOrParentListener implements HGListener 
	{
		public HGListener.Result handle(HyperGraph graph, HGEvent event)
		{
			Object atom = graph.get(((HGAtomRemovedEvent)event).getAtomHandle());
			if (atom instanceof Revision)
			{
				Revision revision = (Revision)atom;
				VersionedOntology versioned = graph.get(revision.versioned());				
				revisionChildIndex(graph).removeEntry(versioned.getBottomRevision().getPersistent(), 
													  revision.getAtomHandle().getPersistent());
			}
			else if (atom instanceof ParentLink)
			{
				ParentLink parentLink = (ParentLink)atom;
				Object child = parentLink.child();
				Object parent = parentLink.parent();
				if (child instanceof Revision && parent instanceof Revision)
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
			return Result.ok;
		}
	}
}