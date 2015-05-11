package org.hypergraphdb.app.owl.newver;

import java.util.ArrayList;
import java.util.List;

import org.hypergraphdb.app.owl.versioning.change.VChange;

public class ChangeConflict<V extends Versioned<V>>
{
	private ChangeSet<V> baseSet, otherSet; 
	private List<VChange<V>> baseChanges, otherChanges;
	
	public ChangeConflict<V> make(Class<V> type)
	{
		return new ChangeConflict<V>();
	}
	
	public ChangeConflict<V> baseSet(ChangeSet<V> baseSet)
	{
		this.baseSet = baseSet;
		return this;
	}

	public ChangeConflict<V> otherSet(ChangeSet<V> otherSet)
	{
		this.otherSet = otherSet;
		return this;
	}
	
	public ChangeConflict<V> baseChanges(List<VChange<V>>  baseChanges)
	{
		this.baseChanges = baseChanges;
		return this;
	}
	
	public ChangeConflict<V> addBaseChange(VChange<V>  c)
	{
		if (this.baseChanges == null)
			this.baseChanges = new ArrayList<VChange<V>>();
		this.baseChanges.add(c);
		return this;
	}
	
	public ChangeConflict<V> otherChanges(List<VChange<V>>  otherChanges)
	{
		this.otherChanges = otherChanges;
		return this;
	}
	
	public ChangeConflict<V> addOtherChange(VChange<V>  c)
	{
		if (this.otherChanges == null)
			this.otherChanges = new ArrayList<VChange<V>>();
		this.otherChanges.add(c);
		return this;
	}
	
	public ChangeSet<V> baseSet()
	{
		return baseSet;
	}
	
	public ChangeSet<V> otherSet()
	{
		return otherSet;
	}
	
	public List<VChange<V>> baseChanges()
	{
		return baseChanges;
	}
	
	public List<VChange<V>> otherChanges()
	{
		return otherChanges;
	}	
}