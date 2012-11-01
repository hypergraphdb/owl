package org.hypergraphdb.app.owl.model.swrl;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;
import org.semanticweb.owlapi.vocab.SWRLBuiltInsVocabulary;

/**
 * SWRLBuiltInAtomHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLBuiltInAtomHGDB extends SWRLAtomHGDB implements SWRLBuiltInAtom
{
	//private List<SWRLDArgument> args;
	private List<HGHandle> argHs;

	public SWRLBuiltInAtomHGDB(HGHandle... args)
	{
		super(args[0]);
		this.argHs = new ArrayList<HGHandle>();
		this.argHs.addAll(Arrays.asList(args).subList(1, args.length));
	}

	public SWRLBuiltInAtomHGDB(IRI predicate)
	{
		throw new UnsupportedOperationException();
	}
	
	public SWRLBuiltInAtomHGDB(IRI predicate, List<SWRLDArgument> args)
	{
		throw new UnsupportedOperationException();
	}
	
	public SWRLBuiltInAtomHGDB(HGHandle predicate, List<HGHandle> args)
	{
		super(predicate);
		if (args == null) throw new IllegalArgumentException();
		this.argHs = args;
	}

	public IRI getPredicate()
	{
		return (IRI) super.getPredicate();
	}

	/**
	 * Determines if the predicate of this atom is is a core builtin.
	 * 
	 * @return <code>true</code> if this is a core builtin, otherwise
	 *         <code>false</code>
	 */
	public boolean isCoreBuiltIn()
	{
		return SWRLBuiltInsVocabulary.getBuiltIn(getPredicate().toURI()) != null;
	}

	public List<SWRLDArgument> getArguments()
	{
		//Read Transaction
		List<SWRLDArgument> args = new ArrayList<SWRLDArgument>(argHs.size());
		for (HGHandle h : argHs) {
			SWRLDArgument a = getHyperGraph().get(h);
			if (a == null) throw new IllegalStateException("Arg not stored in graph: " + h);
			args.add(a);
		}
		return args;
	}

	public Collection<SWRLArgument> getAllArguments()
	{
		List<SWRLArgument> args = new ArrayList<SWRLArgument>(argHs.size());
		args.addAll(getArguments());
		return args;
	}

	public void accept(OWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public void accept(SWRLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(SWRLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof SWRLBuiltInAtom))
		{
			return false;
		}
		SWRLBuiltInAtom other = (SWRLBuiltInAtom) obj;
		return other.getPredicate().equals(getPredicate())
				&& other.getArguments().equals(getArguments());
	}

	protected int compareObjectOfSameType(OWLObject object)
	{
		SWRLBuiltInAtom other = (SWRLBuiltInAtom) object;
		int diff = getPredicate().compareTo(other.getPredicate());
		if (diff != 0)
		{
			return diff;
		}
		List<SWRLDArgument> otherArgs = other.getArguments();
		List<SWRLDArgument> args = getArguments();
		int i = 0;
		while (i < args.size() && i < otherArgs.size())
		{
			diff = args.get(i).compareTo(otherArgs.get(i));
			if (diff != 0)
			{
				return diff;
			}
			i++;
		}
		return args.size() - otherArgs.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.model.swrl.SWRLAtomHGDB#getArity()
	 */
	@Override
	public int getArity() {
		return super.getArity() + argHs.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.model.swrl.SWRLAtomHGDB#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (i == 0) {
			return super.getTargetAt(i);
		} else {
			return argHs.get(i - 1);
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.model.swrl.SWRLAtomHGDB#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i == 0) {
			super.notifyTargetHandleUpdate(i, handle);
		} else {
			argHs.set(i - 1, handle);
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.model.swrl.SWRLAtomHGDB#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i == 0) {
			super.notifyTargetRemoved(i);
		} else {
			argHs.remove(i - 1);
		}
	}
}