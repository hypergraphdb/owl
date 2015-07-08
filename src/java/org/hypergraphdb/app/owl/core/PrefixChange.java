package org.hypergraphdb.app.owl.core;

import java.util.HashSet;

import java.util.Set;

import org.hypergraphdb.app.owl.HGDBOntologyFormat;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.change.OWLOntologyChangeData;
import org.semanticweb.owlapi.change.OWLOntologyChangeDataVisitor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * PrefixChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 1, 2012
 */
public abstract class PrefixChange extends OWLOntologyChange {

	private HGDBOntologyFormat format;
	private Pair<String, String> prefixNameToPrefix;

	public PrefixChange(HGDBOntologyFormat format, String prefixName, String prefix) {
		super(null);
		this.format = format;
		setPrefixNameToPrefix(new Pair<String, String>(prefixName, prefix));
	}

	/**
	 * @param ont
	 */
	public PrefixChange(OWLOntologyEx ont, String prefixName, String prefix) {
		super(ont);
		setPrefixNameToPrefix(new Pair<String, String>(prefixName, prefix));
	}
	
	/**
	 * @return the prefixNameToPrefix
	 */
	public Pair<String, String> getPrefixNameToPrefix() {
		return prefixNameToPrefix;
	}

	/**
	 * @param prefixNameToPrefix the prefixNameToPrefix to set
	 */
	public void setPrefixNameToPrefix(Pair<String, String> prefixNameToPrefix) {
		this.prefixNameToPrefix = prefixNameToPrefix;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChange#isAxiomChange()
	 */
	@Override
	public boolean isAxiomChange() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChange#getAxiom()
	 */
	@Override
	public OWLAxiom getAxiom() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChange#isImportChange()
	 */
	@Override
	public boolean isImportChange() {
		return false;
	}
	
	public HGDBOntologyFormat getFormat() {
		return format;
	}

	public String getPrefixName() {
		return prefixNameToPrefix.getFirst();
	}

	public String getPrefix() {
		return prefixNameToPrefix.getSecond();
	}

    @Override
    public boolean isAddAxiom() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public OWLOntologyChangeData getChangeData() {
        // TODO - this is bogus, this whole change data API seems to be for serialization
        // which we don't need.
        return new OWLOntologyChangeData() {
            private static final long serialVersionUID = 1L;
            public <O, E extends Exception> O accept(OWLOntologyChangeDataVisitor<O, E> visitor) throws E {
                return null; // visitor.visit((E)null);
            }
            public PrefixChange createOntologyChange(OWLOntology ontology) {
                if(ontology == null) {
                    throw new NullPointerException("ontology must not be null");
                }
                return null; //new AddImport(ontology, getDeclaration());
            }            
        };
    }

    @Override
    public Set<OWLEntity> getSignature()
    {
        return new HashSet<OWLEntity>();
    }
	
	public String toString() {
		return getPrefixName() + " -> " + getPrefix();
	}
}