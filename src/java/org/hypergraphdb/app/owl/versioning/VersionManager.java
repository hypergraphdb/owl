package org.hypergraphdb.app.owl.versioning;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.algorithms.DefaultALGenerator;
import org.hypergraphdb.algorithms.HGBreadthFirstTraversal;

/**
 * <p>
 * Manage versioned ontologies in a HyperGraphDB ontology repository. Because versioning operations
 * are tied to a user, an instance needs a username to tag new revisions etc.
 * If no username is provided, the system username will be used. 
 * </p>
 * 
 * <p>
 * 
 * TODO - remove this restriction, make sure this is also a lightweight object with
 * no essential state so many can be quickly created at will
 * 
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

	public String defaultBranchName()
	{
		return "master";
	}
	
	/**
	 * for internal use, TODO - hide this better...
	 * @return
	 */
	public HGHandle emptyChangeSetHandle()
	{
		if (emptyChangeSetHandle == null || graph.get(emptyChangeSetHandle) == null)
		{
			emptyChangeSetHandle = graph.getHandleFactory().makeHandle("cfc87b65-35bc-427c-ac33-1c0aa4dd24c8");
			graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
				public Object call()
				{
					ChangeSet<VersionedOntology> changeSet = graph.get(emptyChangeSetHandle);
					if (changeSet == null)
						graph.define(emptyChangeSetHandle, new ChangeSet<VersionedOntology>());
					return null;
				}
			});
		}
		return emptyChangeSetHandle;
	}
	
	/**
	 * for internal use
	 * @param O
	 * @return
	 */
	public VersionManager manualVersioned(HGHandle O) 
	{ 
		isversionedmap.put(O, true);
		return this; 
	}
	
	private VersionedOntology startVersioning(HGHandle ontology)
	{
		HGHandle workingChanges = graph.add(new ChangeSet<VersionedOntology>());				
		VersionedOntology versioned = new VersionedOntology(graph, 
															ontology, 
															graph.getHandleFactory().nullHandle(), 
															workingChanges);
		graph.add(versioned);		
		long now = System.currentTimeMillis();
		Revision bottomRevision = new Revision(versioned.getAtomHandle());
		bottomRevision.user(user);
		bottomRevision.timestamp(now);
		// chicken & egg: we have to have the bottomRevisionHandle available for the 
		// listener in TrackRevisionStructure, so we set it first and do a graph.define here
		// instead of graph.add
		HGHandle bottomRevisionHandle = graph.getHandleFactory().makeHandle();
		versioned.setBottomRevision(bottomRevisionHandle);
		graph.define(bottomRevisionHandle, bottomRevision);				
		Revision initialRevision = new Revision(versioned.getAtomHandle());
		initialRevision.user(user);
		initialRevision.setComment("root");
		initialRevision.timestamp(now);
		HGHandle revisionHandle = graph.add(initialRevision);
		versioned.metadata().createBranch(revisionHandle, defaultBranchName(), user);
		versioned.setRootRevision(revisionHandle);		
		versioned.setCurrentRevision(revisionHandle);
		graph.update(versioned);
		isversionedmap.put(ontology, true);
		return versioned;
	}
	
	/**
	 * <p>
	 * Construct a <code>VersionManager</code> bound to the specified database instance
	 * and representing the given user.
	 * </p>
	 * 
	 * @param graph The HyperGraphDB instance holding the versioned.
	 * @param user The user conducting versioning operation. If <code>null</code>, the
	 * system user (<code>System.getProperty("user.name")</code>) is used.
	 */
	public VersionManager(HyperGraph graph, String user)
	{
		this.graph = graph;
		user(user);
	}
	
	public HyperGraph graph()
	{
		return graph;
	}
	
	public String user()
	{
		return user;
	}
	
	public VersionManager user(String user)
	{
		this.user = user == null ? System.getProperty("user.name") : user;
		return this;
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
			// this test is unfortunately not generic...we need to find a way to make it generic,
			// the abstract Versioned interface doesn't expose a connection to an underlying entity
			// that is being versioned (the 'target' parameter to this method).
			inmap = graph.findOne(hg.and(hg.type(VersionedOntology.class), hg.eq("ontology", target))) != null;
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
				hg.and(hg.type(VersionedOntology.class), 
					   hg.eq("ontology", ontology)));		
		if (versioned != null)
			return versioned;
		else
			return startVersioning(ontology);
	}
			
	/**
	 * <p>
	 * Remove versioning information about an ontology.If there is no currently
	 * versioning in effect, nothing is done.
	 * </p>
	 * @param ontology The {@link HGHandle} of the <code>OWLOntology</code> itself, 
	 * not the {@link VersionedOntology} object.
	 * @return <code>this</code>
	 */
	public VersionManager removeVersioning(final HGHandle ontology)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				if (isVersioned(ontology))
				{
					VersionedOntology vOntology = versioned(ontology);
					HGHandle voHandle = vOntology.getAtomHandle();
					//versioning.printRevisionGraph(vOntology);

					// TODO: how much of this stuff should be left for garbage collection
					// we do want this to be a fast operation since it's going to be user performed.
					// The problem with leaving it to GC is that an immediate (re)clone of the same
					// ontology will hit a conflict with the partially removed, not GC-ed yet one
					// since all HGHandles are the same.
					
					// remove branches
					try (HGSearchResult<HGHandle> rs = graph.find(hg.and(hg.type(Branch.class), hg.eq("versioned", vOntology.getAtomHandle()))))
					{
						graph.remove(rs.next());
					}
					
					// Remove all ChangeSet, making sure they are not linked to from somewhere else.
					HGBreadthFirstTraversal traversal = new HGBreadthFirstTraversal(vOntology.getRootRevision(),
							new DefaultALGenerator(graph,  
												   hg.type(ChangeLink.class), 
												   hg.type(Revision.class), 
												   false,
												   true,
												   true,
												   false));
					while (traversal.hasNext())
					{
						ChangeLink changeLink = graph.get(traversal.next().getFirst());
						// Don't remove change set if it's linked from some other ChangeLink 
						// (theoretically possible, though no use case known at this time).
						if (graph.getIncidenceSet(changeLink.change()).size() > 1)
							continue;
						ChangeSet<VersionedOntology> changeSet = graph.get(changeLink.change());
						changeSet.clear();
						if (!changeLink.change().equals(emptyChangeSetHandle))
							graph.remove(changeLink.change(), false);
					}
					
					List<HGHandle> revisions = graph.findAll(hg.dfs(vOntology.getRootRevision(),
														     		hg.type(ChangeLink.class),
														     		hg.type(Revision.class),
														     		true,
														     		false));
					for (HGHandle revisionHandle : revisions) 
					{						 
						for (String label : vOntology.metadata().labels(revisionHandle))
							vOntology.metadata().unlabel(revisionHandle, label);
						// remove ParentLink links as well, hopefully no issue with ongoing traversal!
						graph.remove(revisionHandle, false); 
					}
					graph.remove(vOntology.getRootRevision(), true);
					graph.remove(voHandle, false);
				}
				return true;
			}
		});		
		isversionedmap.put(ontology, false);		
		return this;
	}
}