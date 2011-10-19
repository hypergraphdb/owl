package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;

import uk.ac.manchester.cs.owl.owlapi.OWLQuantifiedRestrictionImpl;

/**
 * OWLQuantifiedObjectRestrictionHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public abstract class OWLQuantifiedObjectRestrictionHGDB extends
		OWLQuantifiedRestrictionHGDB<OWLClassExpression, OWLObjectPropertyExpression, OWLClassExpression> {

	/**
	 * @param args [0]...property, [1]...filler
	 */
    public OWLQuantifiedObjectRestrictionHGDB(HGHandle... args) {
    	super(args[0], args[1]);
    	if (args.length != 2) throw new IllegalArgumentException("Must be exactly 2 handles.");
    }
	
    public OWLQuantifiedObjectRestrictionHGDB(HGHandle property, int cardinality, HGHandle filler) {
    	//TODO check types: OWLObjectPropertyExpression property, OWLClassExpression filler
        super(property, filler);
    }
	
	@Override
	protected int compareObjectOfSameType(OWLObject object) {
        @SuppressWarnings("unchecked")
		OWLQuantifiedRestriction<OWLClassExpression, OWLObjectPropertyExpression, OWLClassExpression> other = (OWLQuantifiedRestriction<OWLClassExpression, OWLObjectPropertyExpression, OWLClassExpression>) object;
        int diff = getProperty().compareTo(other.getProperty());
        if(diff != 0) {
            return diff;
        }
        return getFiller().compareTo(other.getFiller());
    }
}
