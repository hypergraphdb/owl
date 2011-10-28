package org.hypergraphdb.app.owl.test;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.CachingBidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.OWLEntitySetProvider;
import org.semanticweb.owlapi.util.ReferencedEntitySetProvider;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * TestBidirectionalShortFormProviderAdapter is needed to parse our test Classexpression strings.
 * The entities include all builtin entities/vocabluary, that's also accessible in the Protege GUI ClassExpression Editor.
 * Such as: TopObjectProperty, BottomObjectProperty, xsd datatypes et.c.
 * 
 * Builtin datatypes will have the shortform of toString: rdf:PlainLiteral, xsd:int, et.c.
 * For names enclosed in <NAME>, the <> will be removed.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 25, 2011
 */
public class TestBidirectionalShortFormProviderAdapter extends CachingBidirectionalShortFormProvider {

		public static int MAX_SHORTFORM_STRING_LENGTH = 25;

		public static boolean PRINT_SHORTFORM_MAP = true;
		
		private static final DecimalFormat DEC_FORMAT = new DecimalFormat("000");

		private ShortFormProvider shortFormProvider;

	    private Set<OWLOntology> ontologies;

	    OWLDataFactory factory;

	    /**
	     * Creates a BidirectionalShortFormProvider that maps between the entities that are referenced in
	     * the specified ontologies and the shortforms of these entities.
	     * @param ontologies The ontologies that contain references to the entities to be mapped.
	     * @param shortFormProvider The short form provider that should be used to generate the
	     * short forms of the referenced entities.
	     */
	    public TestBidirectionalShortFormProviderAdapter(OWLOntology ontology, OWLDataFactory datafactory, ShortFormProvider shortFormProvider) {
	        this.shortFormProvider = shortFormProvider;
	        this.ontologies = new HashSet<OWLOntology>();
	        factory = datafactory;
	        ontologies.add(ontology);
	        rebuild(new ReferencedEntitySetProvider(ontologies));
	    }

	    @Override
		protected String generateShortForm(OWLEntity entity) {
	    	if (entity instanceof OWLDatatype && entity.isBuiltIn()) {
	    		return entity.toString();
	    	} else {
	    		String sf = shortFormProvider.getShortForm(entity);
	    		if (sf.length() > 3 &&  sf.startsWith("<") && sf.endsWith(">")) {
	    			sf = sf.substring(1, sf.length() - 1);
	    		}
	    		return sf; 
	    	}
	    }
	    
	    /**
	     * rebuilds cache using the entitysetprovider and adds builting properties afterwards.
	     */
	    public void rebuild(OWLEntitySetProvider<OWLEntity> entitySetProvider) {
	    	super.rebuild(entitySetProvider);	    	
	    	//add our toplevel entities. @see OWLEntityRenderingCache
	    	add(factory.getOWLThing());
	    	add(factory.getOWLNothing());
	    	add(factory.getOWLTopObjectProperty());
	    	add(factory.getOWLBottomObjectProperty());
	    	add(factory.getOWLTopDataProperty());
	    	add(factory.getOWLBottomDataProperty());
	        // standard annotation properties        
	        for (IRI uri : OWLRDFVocabulary.BUILT_IN_ANNOTATION_PROPERTY_IRIS){
	            add(factory.getOWLAnnotationProperty(uri));
	        }
	        // datatypes
	        add(factory.getTopDatatype());
	        for (OWL2Datatype dt : OWL2Datatype.values()) {
	            add(factory.getOWLDatatype(dt.getIRI()));
	        }      
	        if (PRINT_SHORTFORM_MAP) {
	        	printShortFormMap();
	        }
	    }
	    
	    public void printShortFormMap() {	    		    	
	    	TreeMap<String, String> sortedMap = new TreeMap<String, String>();
	    	int i =0;
	    	for (String s : getShortForms()) {
	    		sortedMap.put(getEntity(s).toString(), s);
	    	}
	    	for (String s : sortedMap.keySet()) {
	    		String shortForm = sortedMap.get(s);
	    		System.out.print(DEC_FORMAT.format(i) + " " + shortForm);
	    		for (int j = MAX_SHORTFORM_STRING_LENGTH; j > shortForm.length(); j--) System.out.print(" ");
	    		System.out.println("-> " + s);
	    		i++;
	    	}
	    }
}
