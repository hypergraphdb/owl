package org.hypergraphdb.app.owl.model;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.OWLAnnotationValueVisitor;
import org.semanticweb.owlapi.model.OWLAnnotationValueVisitorEx;
import org.semanticweb.owlapi.model.OWLDataVisitor;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLLiteralHGDB.
 * 
 * Setters for literal and lang are provided for Hypergraph Bean persistence only.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 28, 2011
 */
public class OWLLiteralHGDB extends OWLObjectHGDB implements OWLLiteral, HGLink {

    private String literal;

    private HGHandle datatypeHandle;
    //private OWLDatatype datatype;

    private String lang;

    public OWLLiteralHGDB(HGHandle... args) {
    	datatypeHandle = args[0];
    	// Don't call constructor, because we do not know at this point what kind of literal we get. this("", args[0]);
    	// And we cannot check for rdfPlainLiteralHandle as we do not have a graph yet.
    	if (args.length != 1) throw new IllegalArgumentException("Args.length must be 1");
    	//args[0] == null allowed
    }
    
    public OWLLiteralHGDB(String literal, HGHandle datatype) {
    	//TODO check OWLDatatype datatype
    	if (literal == null) throw new IllegalArgumentException("Literal null.");
    	if (datatype == null) throw new IllegalArgumentException("Datatype null.");
        setLiteral(literal);
        this.datatypeHandle = datatype;
        setLang("");
    }

    /**
     * 
     * @param literal non-null string.
     * @param lang non-null string.
     * @param datafactoryHypergraph 
     */
    public OWLLiteralHGDB(String literal, String lang, HGHandle rdfPlainLiteralHandle) {
    	if (rdfPlainLiteralHandle == null) throw new IllegalArgumentException("rdfPlainLiteralHandle null.");
        setLiteral(literal);
        setLang(lang);
        this.datatypeHandle = rdfPlainLiteralHandle;
        //TODO check type.
        //this.datatype = dataFactory.getRDFPlainLiteral();
    }

    /**
     * For Hypergraph use only.
	 * @param literal the literal to set
	 */
	public void setLiteral(String literal) {
    	if (literal == null) throw new IllegalArgumentException("Literal null.");
		this.literal = literal;
	}
	
	/**
	 * For Hypergraph use only.
	 * @param lang the lang to set
	 */
	public void setLang(String lang) {
        if (lang == null) throw new IllegalArgumentException("Lang null.");
		this.lang = lang;
	}
	public String getLiteral() {
        return literal;
    }
    //TODO hilpold 2011.12.19
	// As this is immutable -> set this value on load or cache it.
	// this leads to expensive Databaseoperations.
    public boolean isRDFPlainLiteral() {
        return getDatatype().equals(getOWLDataFactory().getRDFPlainLiteral());
    }

    public boolean hasLang() {
        return !lang.equals("");
    }

    public boolean isInteger() {
        return getDatatype().equals(getOWLDataFactory().getIntegerOWLDatatype());
    }

    public int parseInteger() throws NumberFormatException {
        return Integer.parseInt(literal);
    }

    public boolean isBoolean() {
        return getDatatype().equals(getOWLDataFactory().getBooleanOWLDatatype());
    }

    public boolean parseBoolean() throws NumberFormatException {
        if (literal.equals("0")) {
            return false;
        }
        if (literal.equals("1")) {
            return true;
        }
        if (literal.equals("true")) {
            return true;
        }
        if (literal.equals("false")) {
            return false;
        }
        return false;
    }

    public boolean isDouble() {
        return getDatatype().equals(getOWLDataFactory().getDoubleOWLDatatype());
    }

    public double parseDouble() throws NumberFormatException {
        return Double.parseDouble(literal);
    }

    public boolean isFloat() {
        return getDatatype().equals(getOWLDataFactory().getFloatOWLDatatype());
    }

    public float parseFloat() throws NumberFormatException {
        return Float.parseFloat(literal);
    }

    public String getLang() {
        return lang;
    }

    public boolean hasLang(String lang) {
        return this.lang != null && this.lang.equalsIgnoreCase(lang.trim());
    }

    public OWLDatatype getDatatype() {
        return getHyperGraph().get(datatypeHandle);
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLLiteral)) {
                return false;
            }
            OWLLiteral other = (OWLLiteral) obj;
            return literal.equals(other.getLiteral()) && getDatatype().equals(other.getDatatype()) && lang.equals(other.getLang());
        }
        return false;
    }

    public void accept(OWLDataVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLDataVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public void accept(OWLAnnotationValueVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAnnotationValueVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
    protected int compareObjectOfSameType(OWLObject object) {
        OWLLiteral other = (OWLLiteral) object;
        int diff = literal.compareTo(other.getLiteral());
        if (diff != 0) {
            return diff;
        }
        diff = getDatatype().compareTo(other.getDatatype());
        if (diff != 0) {
            return diff;
        }
        return lang.compareTo(other.getLang());

    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return datatypeHandle ==null? 0 : 1;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (i != 0) throw new HGException("Index i must be 0");
		return datatypeHandle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i != 0) throw new HGException("Index i must be 0");
		datatypeHandle = handle;		
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i != 0) throw new HGException("Index i must be 0");		
		datatypeHandle = null;
	}
}