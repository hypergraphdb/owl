package org.hypergraphdb.app.owl.newver;

import java.util.HashSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;

/**
 * <p>
 * Manage versioned ontologies in a HyperGraphDB ontology repository. Because versioning operations
 * are tied to a user, an instance needs a username to tag new revisions etc.
 * If no username is provided, the system username will be used. 
 * </p>
 * 
 * <p>
 * A <code>VersionManager</code> should not be explicitly created. Rather, it is obtained
 * by the {@link HGDBOntologyManager}.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class VersionManager
{
	private HyperGraph graph;
	private HGHandle emptyChangeSetHandle;
	private String user;
	private Map<HGHandle, Boolean> isversionedmap = new ConcurrentHashMap<HGHandle, Boolean>();
	
	private HGHandle emptyChangeSetHandle()
	{
		if (emptyChangeSetHandle == null)
			emptyChangeSetHandle = graph.getHandleFactory().makeHandle("cfc87b65-35bc-427c-ac33-1c0aa4dd24c8");
		return emptyChangeSetHandle;
	}
	
	private VersionedOntology startVersioning(HGHandle ontology)
	{
		HGHandle workingChanges = graph.add(new ChangeSet<VersionedOntology>());				
		VersionedOntology versioned = new VersionedOntology(graph, 
				  ontology, 
				  graph.getHandleFactory().nullHandle(), 
				  workingChanges);
		graph.add(versioned);		
		HGHandle initialMark = graph.add(new ChangeMark(ontology, emptyChangeSetHandle()));
		Revision initialRevision = new Revision(versioned.getAtomHandle());
		initialRevision.setUser(user);
		initialRevision.setTimestamp(System.currentTimeMillis());
		HGHandle revisionHandle = graph.add(initialRevision);
		graph.add(new RevisionMark(revisionHandle, initialMark));
		versioned.setCurrentRevision(revisionHandle);
		graph.update(versioned);
		isversionedmap.put(ontology, true);
		return versioned;
	}
	
	public VersionManager(HyperGraph graph, String user)
	{
		this.graph = graph;
		setUser(user);
	}
	
	public HyperGraph getGraph()
	{
		return graph;
	}
	
	public String getUser()
	{
		return user;
	}
	
	public void setUser(String user)
	{
		this.user = user == null ? System.getProperty("user.name") : user;
	}
	
	/**
	 * Return <code>true</code> if the given target is under version control.
	 * 
	 * @param target Any of the possible types of target that can be versioned.
	 * See subclasses of {@link Versioned}.
	 */
	public boolean isVersioned(HGHandle target)
	{
		Boolean inmap = this.isversionedmap.get(target);
		if (inmap == null)
		{
			inmap = graph.findOne(hg.and(hg.type(ChangeMark.class),
		  		 hg.orderedLink(target, emptyChangeSetHandle()))) != null;
			this.isversionedmap.put(target, inmap);
		}
		return inmap;
	}
	
	/**
	 * <p>
	 * Return {@link VersionedOntology} instance representing the given
	 * ontology as a versioned artifact. If the ontology is not already
	 * versioned, this method will set it up for versioning. 
	 * </p>
	 * 
	 * @param ontology
	 * @return
	 */
	public VersionedOntology versioned(HGHandle ontology)
	{
		VersionedOntology versioned = graph.getOne(
				hg.and(hg.type(VersionedOntology.class), hg.eq("ontology", ontology)));		
		if (versioned != null)
			return versioned;
		else
			return startVersioning(ontology);
	}
			
	public VersionManager removeVersioning(final HGHandle ontology)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				if (isVersioned(ontology))
				{
					VersionedOntology vOntology = versioned(ontology);
					HGHandle voHandle = graph.getHandle(vOntology);
					// TODO - this is probably not enough, we have to do
					// all the cleanup of change sets, commits, revisions etc.
					graph.remove(voHandle, true);
				}
				return true;
			}
		});		
		isversionedmap.put(ontology, false);		
		return this;
	}
	
//	public Revision createRevision(final String comment, final Versioned...versionedObjects)
//	{
//		return graph.getTransactionManager().transact(new Callable<Revision>() {
//		public Revision call()
//		{
//			Revision rev = new Revision();
//			for (Versioned versioned : versionedObjects)
//			{
//				versioned.commit(user, comment);
//			}
//			graph.add(rev);
//			return rev;			
//		}});
//	}
	
	/**
	 * Return the one {@link Revision} tagged with the specified tag, or null if
	 * no revision has been tagged with that tag. 
	 */
	public Revision revisionWithTag(final String tag)
	{
		return graph.getTransactionManager().transact(new Callable<Revision>() {
		public Revision call()
		{
			
			HGHandle handle = graph.findOne(hg.and(hg.type(TagRevision.class), hg.eq("tag", tag)));
			if (handle == null)
				return null;
			TagRevision tag = graph.get(handle);
			return graph.get(tag.revision());
		}});
	}
	
	/**
	 * Return the set of all revisions tagged with the given label (a possible empty set).
	 */
	public Set<Revision> revisionsWithLabel(final String label)
	{
		return graph.getTransactionManager().transact(new Callable<Set<Revision>>() {
		public Set<Revision> call()
		{
			HashSet<Revision> S = new HashSet<Revision>();
			HGHandle labelHandle = graph.findOne(hg.eq(label));
			if (labelHandle == null)
				return S;
			for (HGHandle handle : graph.findAll(hg.and(hg.type(LabelLink.class), hg.incident(labelHandle))))
			{
				LabelLink labelLink = graph.get(handle);
				S.add((Revision)graph.get(labelLink.atom()));				
			}
			return S;
		}});
		
	}	
}