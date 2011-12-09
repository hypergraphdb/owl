package org.hypergraphdb.app.owl.model.classexpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.HGChangeableLink;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.util.CollectionFactory;

/**
 * OWLObjectOneOfHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 18, 2011
 */
public class OWLObjectOneOfHGDB extends OWLAnonymousClassExpressionHGDB implements OWLObjectOneOf, HGChangeableLink {

	// private Set<OWLIndividual> values;
	private List<HGHandle> valueHandles;

	public OWLObjectOneOfHGDB(HGHandle... args) {
		// no duplicates allowed
		assert (new TreeSet<HGHandle>(Arrays.asList(args)).size() == args.length);
		valueHandles = Arrays.asList(args);
		// valueHandles = new ArrayList<HGHandle>(Arrays.asList(args));
	}

	public OWLObjectOneOfHGDB(Set<? extends HGHandle> values) {
		// TODO check for type OWLIndividual
		// two equal objects should be mapped to the latter, just as in a
		// HashSet.
		this.valueHandles = new ArrayList<HGHandle>(values);
	}

//	/**
//	 * This checks valueHandles for duplicate OWLIndividuals and removes the
//	 * first find. The order of the original valueHandles is not maintained.
//	 */
//	private void enforceSetCondition() {
//		Set<OWLIndividual> uniqueIndividuals = getIndividuals();
//		List<HGHandle> uniqueIndividualHandles = new ArrayList<HGHandle>();
//		for (OWLIndividual i : uniqueIndividuals) {
//			uniqueIndividualHandles.add(getHyperGraph().getHandle(i));
//		}
//		valueHandles = uniqueIndividualHandles;
//	}

	/**
	 * Gets the class expression type for this class expression
	 * 
	 * @return The class expression type
	 */
	public ClassExpressionType getClassExpressionType() {
		return ClassExpressionType.OBJECT_ONE_OF;
	}

	public Set<OWLIndividual> getIndividuals() {
		HyperGraph g = getHyperGraph();
		Set<OWLIndividual> s = new TreeSet<OWLIndividual>();
		for (HGHandle h : valueHandles) {
			s.add((OWLIndividual) g.get(h));
		}
		return CollectionFactory.getCopyOnRequestSet(s);
		// return CollectionFactory.getCopyOnRequestSet(values);
	}

	public boolean isClassExpressionLiteral() {
		return false;
	}

	public OWLClassExpression asObjectUnionOf() {
		if (valueHandles.size() == 1) {
			return this;
		} else {
			Set<OWLClassExpression> ops = new HashSet<OWLClassExpression>();
			for (OWLIndividual ind : getIndividuals()) {
				ops.add(getOWLDataFactory().getOWLObjectOneOf(ind));
			}
			return getOWLDataFactory().getOWLObjectUnionOf(ops);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			if (!(obj instanceof OWLObjectOneOf)) {
				return false;
			}
			return ((OWLObjectOneOf) obj).getIndividuals().equals(getIndividuals());
		}
		return false;
	}

	public void accept(OWLClassExpressionVisitor visitor) {
		visitor.visit(this);
	}

	public void accept(OWLObjectVisitor visitor) {
		visitor.visit(this);
	}

	public <O> O accept(OWLClassExpressionVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	@Override
	protected int compareObjectOfSameType(OWLObject object) {
		return compareSets(getIndividuals(), ((OWLObjectOneOf) object).getIndividuals());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return valueHandles.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity()))
			throw new IllegalArgumentException("Index has to be 0 and less than " + getArity());
		return valueHandles.get(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int,
	 * org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		assert (getHyperGraph().get(handle) instanceof OWLClassExpression);

		if (!(i >= 0 && i < getArity()))
			throw new IllegalArgumentException("Index has to be 0 and less than " + getArity());
		if (handle == null)
			throw new IllegalArgumentException("handle null");
		valueHandles.set(i, handle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity()))
			throw new IllegalArgumentException("Index has to be 0 and less than " + getArity());
		valueHandles.set(i, null);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.core.HGChangeableLink#setTargetAt(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void setTargetAt(int i, HGHandle handle) {
		if (!(i >= 0 && i < getArity()))
			throw new IllegalArgumentException("Index has to be 0 and less than " + getArity());
		valueHandles.set(i, handle);
	}

}
