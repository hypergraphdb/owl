package org.hypergraphdb.app.owl.model.swrl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.model.axioms.OWLLogicalAxiomHGDB;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SWRLVariableExtractor;

/**
 * SWRLRuleHGDB. Internal Cache.
 * 
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLRuleHGDB extends OWLLogicalAxiomHGDB implements SWRLRule, HGLink {
	private HGHandle bodyHandle;
	private HGHandle headHandle;

	private Set<SWRLVariable> variables;
	private Boolean containsAnonymousClassExpressions = null;
	private Set<OWLClassExpression> classAtomsPredicates;

	public SWRLRuleHGDB(HGHandle... args) {
		super(Collections.<OWLAnnotation> emptySet());
		assert args.length == 2 : new IllegalArgumentException("Expecting 2 targets to SWRLRuleHGDB");
		bodyHandle = args[0];
		headHandle = args[1];
	}

	public SWRLRuleHGDB(HGHandle body, HGHandle head, Set<? extends OWLAnnotation> annotations) {
		//SWRLBody body, SWRLHead head.
		super(annotations);
		if (body == null) throw new IllegalArgumentException();
		if (head == null) throw new IllegalArgumentException();
		this.bodyHandle = body;
		this.headHandle = head;				
	}
	
	public SWRLHead getHeadAtom() {
		return getHyperGraph().get(headHandle);
	}

	public SWRLBody getBodyAtom() {
		return getHyperGraph().get(bodyHandle);
	}

	public void addConclusion(HGHandle conclusion) {
		SWRLHead H = getHeadAtom();
		ArrayList<HGHandle> L = new ArrayList<HGHandle>(H.asCollection());
		L.add(conclusion);
		getHyperGraph().replace(headHandle, new SWRLHead(L));
	}

	public void addPremise(HGHandle premise) {
		SWRLBody H = getBodyAtom();
		ArrayList<HGHandle> L = new ArrayList<HGHandle>(H.asCollection());
		L.add(premise);
		getHyperGraph().replace(bodyHandle, new SWRLBody(L));
	}

	public int getArity() {
		return 2;
	}

	public HGHandle getTargetAt(int i) {
		if (i == 0)
			return bodyHandle;
		else if (i == 1)
			return headHandle;
		else
			throw new IllegalArgumentException("i != 0 or 1");
	}

	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i == 0)
			bodyHandle = handle;
		else if (i == 1)
			headHandle = handle;
		else
			throw new IllegalArgumentException("i != 0 or 1");
	}

	public void notifyTargetRemoved(int i) {
		if (i == 0)
			bodyHandle = getHyperGraph().getHandleFactory().nullHandle();
		else if (i == 1)
			headHandle = getHyperGraph().getHandleFactory().nullHandle();
		// throw new UnsupportedOperationException();
	}

	public SWRLRule getAxiomWithoutAnnotations() {
		if (!isAnnotated()) {
			return this;
		}
		return getOWLDataFactory().getSWRLRule(getBody(), getHead());
	}

	public OWLAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
		return getOWLDataFactory().getSWRLRule(getBody(), getHead());
	}

	public Set<SWRLVariable> getVariables() {
		if (variables == null) {
			Set<SWRLVariable> vars = new HashSet<SWRLVariable>();
			SWRLVariableExtractor extractor = new SWRLVariableExtractor();
			accept(extractor);
			vars.addAll(extractor.getVariables());
			variables = new HashSet<SWRLVariable>(vars);
		}
		return variables;
	}

	public boolean containsAnonymousClassExpressions() {
		if (containsAnonymousClassExpressions == null) {
			containsAnonymousClassExpressions = false;
			for (SWRLAtom atom : getHead()) {
				if (atom instanceof SWRLClassAtom) {
					if (((SWRLClassAtom) atom).getPredicate().isAnonymous()) {
						containsAnonymousClassExpressions = true;
						break;
					}
				}
			}
			if (containsAnonymousClassExpressions == null) {
				for (SWRLAtom atom : getBody()) {
					if (atom instanceof SWRLClassAtom) {
						if (((SWRLClassAtom) atom).getPredicate().isAnonymous()) {
							containsAnonymousClassExpressions = true;
							break;
						}
					}
				}
			}
		}
		return containsAnonymousClassExpressions;
	}

	public Set<OWLClassExpression> getClassAtomPredicates() {
		if (classAtomsPredicates == null) {
			Set<OWLClassExpression> predicates = new HashSet<OWLClassExpression>();
			for (SWRLAtom atom : getHead()) {
				if (atom instanceof SWRLClassAtom) {
					predicates.add(((SWRLClassAtom) atom).getPredicate());
				}
			}
			for (SWRLAtom atom : getBody()) {
				if (atom instanceof SWRLClassAtom) {
					predicates.add(((SWRLClassAtom) atom).getPredicate());
				}
			}
			classAtomsPredicates = new HashSet<OWLClassExpression>(predicates);
		}
		return classAtomsPredicates;
	}

	public void accept(OWLObjectVisitor visitor) {
		visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	public void accept(SWRLObjectVisitor visitor) {
		visitor.visit(this);
	}

	public <O> O accept(SWRLObjectVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	/**
	 * Gets the atoms in the antecedent
	 * 
	 * @return A set of <code>SWRLAtom</code>s, which represent the atoms in the
	 *         antecedent of the rule.
	 */
	public Set<SWRLAtom> getBody() {
		SWRLConjuction C = getHyperGraph().get(bodyHandle);
		HashSet<SWRLAtom> S = new HashSet<SWRLAtom>();
		for (int i = 0; i < C.getArity(); i++) {
			SWRLAtom a = getHyperGraph().get(C.getTargetAt(i));
			S.add(a);
		}
		return S;
	}

	/**
	 * Gets the atoms in the consequent.
	 * 
	 * @return A set of <code>SWRLAtom</code>s, which represent the atoms in the
	 *         consequent of the rule
	 */
	public Set<SWRLAtom> getHead() {
		SWRLConjuction C = getHyperGraph().get(headHandle);
		HashSet<SWRLAtom> S = new HashSet<SWRLAtom>();
		for (int i = 0; i < C.getArity(); i++) {
			SWRLAtom a = getHyperGraph().get(C.getTargetAt(i));
			S.add(a);
		}
		return S;
	}

	public void accept(OWLAxiomVisitor visitor) {
		visitor.visit(this);
	}

	public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
		return visitor.visit(this);
	}

	/**
	 * If this rule contains atoms that have predicates that are inverse object
	 * properties, then this method creates and returns a rule where the
	 * arguments of these atoms are fliped over and the predicate is the inverse
	 * (simplified) property
	 * 
	 * @return The rule such that any atoms of the form inverseOf(p)(x, y) are
	 *         transformed to p(x, y).
	 */
	public SWRLRule getSimplified() {
		return (SWRLRule) this.accept(ATOM_SIMPLIFIER);
	}

	/**
	 * Determines if this axiom is a logical axiom. Logical axioms are defined
	 * to be axioms other than declaration axioms (including imports
	 * declarations) and annotation axioms.
	 * 
	 * @return <code>true</code> if the axiom is a logical axiom,
	 *         <code>false</code> if the axiom is not a logical axiom.
	 */
	public boolean isLogicalAxiom() {
		return true;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof SWRLRule)) {
			return false;
		}
		SWRLRule other = (SWRLRule) obj;
		return other.getBody().equals(getBody()) && other.getHead().equals(getHead());
	}

	public AxiomType<SWRLRule> getAxiomType() {
		return AxiomType.SWRL_RULE;
	}

	protected int compareObjectOfSameType(OWLObject object) {
		SWRLRule other = (SWRLRule) object;

		int diff = ImplUtils.compareSets(getBody(), other.getBody());
		if (diff == 0) {
			diff = ImplUtils.compareSets(getHead(), other.getHead());
		}
		return diff;

	}

	protected AtomSimplifier ATOM_SIMPLIFIER = new AtomSimplifier();

	protected class AtomSimplifier implements SWRLObjectVisitorEx<SWRLObject> {

		public SWRLRule visit(SWRLRule node) {
			Set<SWRLAtom> body = new HashSet<SWRLAtom>();
			for (SWRLAtom atom : node.getBody()) {
				body.add((SWRLAtom) atom.accept(this));
			}
			Set<SWRLAtom> head = new HashSet<SWRLAtom>();
			for (SWRLAtom atom : node.getHead()) {
				head.add((SWRLAtom) atom.accept(this));
			}
			return getOWLDataFactory().getSWRLRule(body, head);
		}

		public SWRLClassAtom visit(SWRLClassAtom node) {
			return node;
		}

		public SWRLDataRangeAtom visit(SWRLDataRangeAtom node) {
			return node;
		}

		public SWRLObjectPropertyAtom visit(SWRLObjectPropertyAtom node) {
			return node.getSimplified();
		}

		public SWRLDataPropertyAtom visit(SWRLDataPropertyAtom node) {
			return node;
		}

		public SWRLBuiltInAtom visit(SWRLBuiltInAtom node) {
			return node;
		}

		public SWRLVariable visit(SWRLVariable node) {
			return node;
		}

		public SWRLIndividualArgument visit(SWRLIndividualArgument node) {
			return node;
		}

		public SWRLLiteralArgument visit(SWRLLiteralArgument node) {
			return node;
		}

		public SWRLSameIndividualAtom visit(SWRLSameIndividualAtom node) {
			return node;
		}

		public SWRLDifferentIndividualsAtom visit(SWRLDifferentIndividualsAtom node) {
			return node;
		}
	}
}