package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.versioning.LabelLink;
import org.hypergraphdb.app.owl.versioning.VChange;
import org.hypergraphdb.app.owl.versioning.Versioned;

public class VAddLabelChange<T extends Versioned<T>> extends VMetadataChange<T>
{
	private HGHandle label, labeled;

	public VAddLabelChange()
	{		
	}
	
	public VAddLabelChange(HGHandle label, HGHandle labeled)
	{		
		this.label = label;
		this.labeled = labeled;
	}
		
	public HGHandle getLabel()
	{
		return label;
	}

	public void setLabel(HGHandle label)
	{
		this.label = label;
	}

	public HGHandle getLabeled()
	{
		return labeled;
	}

	public void setLabeled(HGHandle labeled)
	{
		this.labeled = labeled;
	}

	@Override
	public void apply(T versioned)
	{
		if (isEffective(versioned))
			graph.add(new LabelLink(label, labeled));
	}

	@Override
	public VChange<T> reduce(VChange<T> previous)
	{
		return null;
	}
	
	@Override
	public VChange<T> inverse()
	{
		return new VRemoveLabelChange<T>(label, labeled);
	}

	@Override
	public boolean conflictsWith(VChange<T> other)
	{
		if (! (other instanceof VRemoveLabelChange) )
			return false;
		VRemoveLabelChange<T> removing = (VRemoveLabelChange<T>)other;
		return removing.getLabel().equals(label) && removing.getLabeled().equals(labeled);
	}

	@Override
	public boolean isEffective(T versioned)
	{
		return graph.findOne(hg.and(hg.type(LabelLink.class),
				hg.incident(label),
				hg.incident(labeled))) == null;	
	}

	@Override
	public boolean isIdempotent()
	{
		return true;
	}
}