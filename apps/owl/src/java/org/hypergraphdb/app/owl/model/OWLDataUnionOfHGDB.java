package org.hypergraphdb.app.owl.model;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.DataRangeType;
import org.semanticweb.owlapi.model.OWLDataRangeVisitor;
import org.semanticweb.owlapi.model.OWLDataRangeVisitorEx;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDataVisitor;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLDataUnionOfHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 31, 2011
 */
public class OWLDataUnionOfHGDB extends OWLNaryDataRangeHGDB implements OWLDataUnionOf {
	
    public OWLDataUnionOfHGDB(HGHandle...args) {
    	super(args);
    	// no duplicates allowed
    	assert(new TreeSet<HGHandle>(Arrays.asList(args)).size() == args.length);
    }

	public OWLDataUnionOfHGDB(Set<? extends HGHandle> operands) {
		//Set<? extends OWLDataRange>
        super(operands);
    }

    public DataRangeType getDataRangeType() {
        return DataRangeType.DATA_UNION_OF;
    }

    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        OWLDataUnionOf other = (OWLDataUnionOf) object;
        return compareSets(getOperands(), other.getOperands());
    }

    @Override
	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OWLDataUnionOf)) {
            return false;
        }
        OWLDataUnionOf other = (OWLDataUnionOf) obj;
        return this.getOperands().equals(other.getOperands());
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public void accept(OWLDataVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLDataVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public void accept(OWLDataRangeVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLDataRangeVisitorEx<O> visitor) {
        return visitor.visit(this);
    }
}
