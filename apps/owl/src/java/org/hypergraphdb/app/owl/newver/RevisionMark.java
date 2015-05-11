package org.hypergraphdb.app.owl.newver;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

/**
 * <p>
 * A <code>RevisionMark</code> connects the {@link ChangeSet} that 
 * led to the creation of a given Revision. Many change sets can be 
 * applied on a revision without necessarily creating a new revision (for 
 * example because they are associated with the revision of a composite project).
 * This link is used to link the one flushing operation (i.e. the creation of a ChangeMark)
 * got a given versioned object that also creates a new {@link Revision}. 
 * </p>
 * <p>
 * Note that we cannot store the revision mark as part of the {@link Revision} itself because
 * a given revision may also have multiple marks associated with it when it is a revision
 * of a complex versioned object (i.e. a project comprised of multiple modules). When we have
 * a project, each of its modules carries separate change sets and change marks so when we
 * create a new revision for the project, we have to specify all of the change marks that apply
 * to it, one per individual module.
 * </p>
 * @author Borislav Iordanov
 *
 */
public class RevisionMark implements HGLink
{
	private HGHandle hRevision;
	private HGHandle hMark;
	
	public RevisionMark(HGHandle...handles)
	{
		assert handles.length == 2;		
		hRevision = handles[0];
		hMark = handles[1];
	}
	
	public HGHandle mark()
	{
		return hMark;
	}
	
	public HGHandle revision()
	{
		return hRevision;
	}
	
	@Override
	public int getArity()
	{
		return 2;
	}

	@Override
	public HGHandle getTargetAt(int i)
	{
		return (i == 0) ? hRevision : hMark;
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i == 0)
			hRevision = handle;
		else if (i == 1)
			hMark = handle;
		else
			throw new IllegalArgumentException("target index " + i);
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
	}
}