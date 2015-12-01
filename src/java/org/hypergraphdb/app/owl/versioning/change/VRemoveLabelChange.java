package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.versioning.LabelLink;
import org.hypergraphdb.app.owl.versioning.Versioned;

public class VRemoveLabelChange<T extends Versioned<T>> extends VMetadataChange<T>
{
	private HGHandle label, labeled;

	public VRemoveLabelChange()
	{		
	}
	
	public VRemoveLabelChange(HGHandle label, HGHandle labeled)
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
		HGHandle link = graph.findOne(hg.and(hg.type(LabelLink.class),
											 hg.incident(label),
											 hg.incident(labeled))); 
		if (link != null)
			graph.remove(link);
	}

	@Override
	public Change<T> reduce(Change<T> previous)
	{
		return null;
	}
	
	@Override
	public Change<T> inverse()
	{
		return new VAddLabelChange<T>(label, labeled);
	}

	@Override
	public boolean conflictsWith(Change<T> other)
	{
		if (! (other instanceof VAddLabelChange) )
			return false;
		VAddLabelChange<T> adding = (VAddLabelChange<T>)other;
		return adding.getLabel().equals(label) && adding.getLabeled().equals(labeled);
	}

	@Override
	public boolean isEffective(T versioned)
	{
		return graph.findOne(hg.and(hg.type(LabelLink.class),
									hg.incident(label),
									hg.incident(labeled))) != null;		
	}

	@Override
	public boolean isIdempotent()
	{
		return true;
	}
}