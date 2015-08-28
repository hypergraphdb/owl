package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGLink;

/**
 * <p>
 * A <code>RevisionMark</code> connects the {@link ChangeRecord} that 
 * led to the creation of a given Revision. Many change sets can be 
 * applied on a versioned object without necessarily creating a new revision (for 
 * example because they are associated with the revision of a composite project).
 * This link is used to link the one flushing operation (i.e. the creation of a ChangeRecord)
 * for a given versioned object that also creates a new {@link Revision}. 
 * </p>
 * <p>
 * Note that we cannot store the revision mark as part of the {@link Revision} itself because
 * a given revision may also have multiple marks associated with it when it is a revision
 * of a complex versioned object (i.e. a project comprised of multiple modules). When we have
 * a project, each of its modules carries separate change sets and change marks so when we
 * create a new revision for the project, we have to specify all of the change marks that apply
 * to it, one per individual module.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class RevisionMark implements HGLink, HGHandleHolder
{
	private HGHandle thisHandle;
	private HGHandle hRevision;
	private HGHandle hChangeRecord;
	
	public RevisionMark()
	{
	}
	
	public RevisionMark(HGHandle...handles)
	{
		assert handles.length == 2;		
		hRevision = handles[0];
		hChangeRecord = handles[1];
	}
	
	public RevisionMark changeRecord(HGHandle changeRecord)
	{
		this.hChangeRecord = changeRecord;
		return this;
	}
	
	public RevisionMark revision(HGHandle revision)
	{
		this.hRevision = revision;
		return this;
	}
	
	public HGHandle changeRecord()
	{
		return hChangeRecord;
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
		return (i == 0) ? hRevision : hChangeRecord;
	}

	
	@Override
	public HGHandle getAtomHandle()
	{
		return thisHandle;
	}

	@Override
	public void setAtomHandle(HGHandle handle)
	{
		this.thisHandle = handle;
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i == 0)
			hRevision = handle;
		else if (i == 1)
			hChangeRecord = handle;
		else
			throw new IllegalArgumentException("target index " + i);
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((thisHandle == null) ? 0 : thisHandle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RevisionMark other = (RevisionMark) obj;
		if (thisHandle == null)
		{
			if (other.thisHandle != null)
				return false;
		}
		else if (!thisHandle.equals(other.thisHandle))
			return false;
		return true;
	}	
	
	public String toString()
	{
		return "RevisionMark[" + this.hRevision+ ", " + this.hChangeRecord + "]"; 
	}
	
}