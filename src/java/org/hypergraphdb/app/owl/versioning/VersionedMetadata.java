package org.hypergraphdb.app.owl.versioning;

import java.util.List;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.versioning.change.VBranchRenameChange;

/**
 * <p>
 * Manages metadata about a versioned object. Metadata is special in that
 * change history needs to be preserved just like for the regular versioned
 * object, but it is hidden from the user and there is no notion of revisions
 * etc. The history is needed because each change is a transaction that potentially
 * has to be propagated to other peers. When a user set the working
 * copy of a versioned to a certain revision, this should not modify the metadata
 * view at all. That is, branches and labels should remain the same regardless 
 * of where in the main revision graph one decides to work. And in that respect,
 * branches and labels behave similarly.  
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class VersionedMetadata<T extends Versioned<T>>
{
	private HyperGraph graph;
	private T versioned;
	private HGHandle [] metachanges;
	
	private void initialize()
	{
		
	}
	
	public VersionedMetadata(HyperGraph graph, T versioned)
	{
		
	}
	
	public HGHandle createBranch(String name, String user)
	{
		return graph.add(new Branch(name, 
									versioned.getAtomHandle(), 
									user, 
									System.currentTimeMillis()));
	}
	
	/**
	 * <p>
	 * Lookup a revision branch for this versioned ontology by name.
	 * </p> 
	 * @param name The name of the branch.
	 * @return The {@link Branch} instance of <code>null</code> if not found.
	 */
	public Branch findBranch(String name)
	{
		return graph.getOne(hg.and(hg.type(Branch.class), 
								   hg.eq("name", name), 
								   hg.eq("versioned", versioned)));
	}
	
	/**
	 * Change the name of this branch. For proper revisiont tracking,
	 * this method must be used instead of {@link #setName(String)} which
	 * is only a bean setter to be used for DB persistence.
	 * @param newname The new branch name. The only restriction is that 
	 * the new name be unique for the versioned object.
	 * @return <code>this</code>
	 */
	public Branch renameBranch(final Branch branch, final String newname)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Branch>(){
			public Branch call()
			{
				if (graph.findOne(hg.and(hg.type(Branch.class), 
						hg.eq("name", newname), 
						hg.eq("versioned", branch.getVersioned()))) != null)
					throw new IllegalArgumentException("Duplicate branch name '" + newname + "'.");
				VBranchRenameChange<T> change = new VBranchRenameChange<T>();
				change.setBranchHandle(branch.getAtomHandle());
				change.setNewname(newname);
				change.setHyperGraph(graph);
//				changes().add(change);
				change.apply(versioned);
				return branch;
			}
		});
	}	
}