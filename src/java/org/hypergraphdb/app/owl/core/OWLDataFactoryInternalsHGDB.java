package org.hypergraphdb.app.owl.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.model.OWLAnnotationPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLClassHGDB;
import org.hypergraphdb.app.owl.model.OWLDataPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLDatatypeHGDB;
import org.hypergraphdb.app.owl.model.OWLLiteralHGDB;
import org.hypergraphdb.app.owl.model.OWLNamedIndividualHGDB;
import org.hypergraphdb.app.owl.model.OWLObjectPropertyHGDB;
import org.hypergraphdb.app.owl.type.link.AxiomAnnotatedBy;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.hypergraphdb.transaction.TxMap;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

/**
 * OWLDataFactoryInternalsHGDB.
 * 
 * Look up in graph if exists. Use, otherwise create, add to graph.
 * 
 * history:
 * 2011.09.28 removed individualsByURI
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 28, 2011
 */
public class OWLDataFactoryInternalsHGDB {
	
	public static boolean DBG = false;
	
	/**
	 *  Cache counter for testing - remove before release
	 */
	public static volatile long CACHE_PUT = 0;

	/**
	 *  Cache counter for testing - remove before release
	 */
	public static volatile long CACHE_HIT = 0;
	
	/**
	 *  Cache counter for testing - remove before release
	 */
	public static volatile long CACHE_MISS = 0;
	
	
    //private WeakHashMap<IRI, WeakReference<? extends OWLEntity>> classesByURI;
    //private final WeakHashMap<IRI, WeakReference<? extends OWLEntity>> objectPropertiesByURI;
    //private final WeakHashMap<IRI, WeakReference<? extends OWLEntity>> dataPropertiesByURI;
    //private final WeakHashMap<IRI, WeakReference<? extends OWLEntity>> datatypesByURI;
    //private final WeakHashMap<IRI, WeakReference<? extends OWLEntity>> individualsByURI;
    //private final WeakHashMap<IRI, WeakReference<? extends OWLEntity>> annotationPropertiesByURI;
	
	private final TxMap<Pair<IRI, Class<? extends OWLEntity>>, OWLEntity> builtinByIRIClassPairCache;
    private final Set<IRI> XSD_VOCABULARY_IRIS = new HashSet<IRI>();
    
    private final OWLDataFactoryHGDB factory;

    HGQuery<HGHandle> lookupLiteral = null;
    HGQuery<HGHandle> lookupIRIByValue = null;
    HGQuery<OWLAnnotation> getAxiomAnnotationsQuery = null;
    
    HashMap<Class<?>, HGQuery<OWLEntity>> lookupEntityByIRI = 
    		new HashMap<Class<?>, HGQuery<OWLEntity>>();

    private void fillBuiltinTypes()
    {
    	for (XSDVocabulary xsd : XSDVocabulary.values())
    		XSD_VOCABULARY_IRIS.add(xsd.getIRI());
    }
    
    private void preCompileQueries()
    {
        lookupIRIByValue = HGQuery.make(HGHandle.class, factory.getHyperGraph()).compile(
        		hg.and(hg.typePlus(IRI.class), hg.eq(hg.var("iri"))));
        lookupLiteral = HGQuery.make(HGHandle.class, factory.getHyperGraph()).compile(
        		hg.and(hg.type(OWLLiteralHGDB.class), 
        			   hg.eq("literal", hg.var("literal")),
        			   hg.incident(hg.var("datatype")),
        			   hg.eq("lang", hg.var("lang")))); //lang needed otherwise OWLAPI tests fail.
        getAxiomAnnotationsQuery = HGQuery.make(OWLAnnotation.class, factory.getHyperGraph()).compile(
        	hg.deref(factory.getHyperGraph(),
			hg.apply(hg.targetAt(factory.getHyperGraph(), 1), //1 .. Annotation, 0 .. Axiom for AxiomAnnotatedBy
			hg.and(hg.type(AxiomAnnotatedBy.class),
			hg.incident(hg.var("axiom"))))
			));        
    }

    HGQuery<OWLEntity> entityByIRIQuery(Class<?> entityType)
    {
    	HGQuery<OWLEntity> q = lookupEntityByIRI.get(entityType);
    	if (q == null)
    	{
    		q = HGQuery.make(OWLEntity.class, factory.getHyperGraph()).compile(
    				hg.deref(factory.getHyperGraph(), 
    						hg.and(hg.type(entityType), hg.eq("IRI", hg.var("iri")))));
    		lookupEntityByIRI.put(entityType, q);
    	}
    	return q;
    }
    
    // call after HyperGraph instance is available in 'factory'.
    void initialize()
    {
    	preCompileQueries();
    	fillBuiltinTypes();
    }
    
    public OWLDataFactoryInternalsHGDB(OWLDataFactoryHGDB f) {
        factory = f;
        builtinByIRIClassPairCache = new TxMap<Pair<IRI, Class<? extends OWLEntity>>, OWLEntity>
        										(f.getHyperGraph().getTransactionManager(), null);
        //classesByURI = new WeakHashMap<IRI, WeakReference<? extends OWLEntity>>();
        //objectPropertiesByURI = new WeakHashMap<IRI, WeakReference<? extends OWLEntity>>();
        //dataPropertiesByURI = new WeakHashMap<IRI, WeakReference<? extends OWLEntity>>();
        //datatypesByURI = new WeakHashMap<IRI, WeakReference<? extends OWLEntity>>();
        //hilpold individualsByURI = new WeakHashMap<IRI, WeakReference<? extends OWLEntity>>();
        //annotationPropertiesByURI = new WeakHashMap<IRI, WeakReference<? extends OWLEntity>>();
    }

//    private OWLEntity unwrap(Map<IRI, WeakReference<? extends OWLEntity>> map, IRI iri, BuildableObjects type) {
//        OWLEntity toReturn = null;
//        while (toReturn == null) {
//            WeakReference<? extends OWLEntity> r = safeRead(map, iri, type);
//            if (r == null || r.get() == null) {
//                toReturn = type.build(factory, iri);
//                r = new WeakReference<OWLEntity>(toReturn);
//                safeWrite(map, iri, r, type);
//            }
//            else {
//                toReturn = r.get();
//            }
//        }
//        return toReturn;
//    }
//    @SuppressWarnings("unused")
//    private WeakReference<? extends OWLEntity> safeRead(Map<IRI, WeakReference<? extends OWLEntity>> map, IRI iri, BuildableObjects type) {
//        return map.get(iri);
//    }
//    @SuppressWarnings("unused")
//    private void safeWrite(Map<IRI, WeakReference<? extends OWLEntity>> map, IRI iri, WeakReference<? extends OWLEntity> value, BuildableObjects type) {
//        map.put(iri, value);
//    }

    private enum BuildableObjects {
        OWLCLASS {
            @Override
            OWLEntity build(OWLDataFactory f, IRI iri) {
                return new OWLClassHGDB(iri);
            }
        },
        OWLOBJECTPROPERTY {
            @Override
            OWLEntity build(OWLDataFactory f, IRI iri) {
                return new OWLObjectPropertyHGDB(iri);
            }
        },
        OWLDATAPROPERTY {
            @Override
            OWLEntity build(OWLDataFactory f, IRI iri) {
                return new OWLDataPropertyHGDB(iri);
            }
        },
        OWLNAMEDINDIVIDUAL {
            @Override
            OWLEntity build(OWLDataFactory f, IRI iri) {
            	//new, ensure in graph, set graph, set handle?
            	OWLNamedIndividualHGDB e = new OWLNamedIndividualHGDB(iri);
                return e;
            }
        },
        OWLDATATYPE {
            @Override
            OWLEntity build(OWLDataFactory f, IRI iri) {
                return new OWLDatatypeHGDB(iri);
            }
        },
        OWLANNOTATIONPROPERTY {
            @Override
            OWLEntity build(OWLDataFactory f, IRI iri) {
                return new OWLAnnotationPropertyHGDB(iri);
            }
        };

        abstract OWLEntity build(OWLDataFactory f, IRI iri);
    }

    public OWLClass getOWLClass(IRI iri) {
        return (OWLClass) ensureCreateEntityInDatabase(OWLClassHGDB.class, iri, BuildableObjects.OWLCLASS);
        //return (OWLClass) unwrap(classesByURI, iri, BuildableObjects.OWLCLASS);
    }

//    private void clear(Map<?, ?> map) {
//        map.clear();
//    }

    public void purge() {
    	//TODO well, we could clean Hypergraph of all unused here, if they occupy to much space.
    	//HG Garbage collection?
        //clear(classesByURI);
        //clear(objectPropertiesByURI);
        //clear(dataPropertiesByURI);
        //clear(datatypesByURI);
        //clear(individualsByURI);
        //clear(annotationPropertiesByURI);
    }

    public OWLObjectProperty getOWLObjectProperty(IRI iri) {
        return (OWLObjectProperty) ensureCreateEntityInDatabase(OWLObjectPropertyHGDB.class, iri, BuildableObjects.OWLOBJECTPROPERTY);
    }

    public OWLDataProperty getOWLDataProperty(IRI iri) {
        return (OWLDataProperty) ensureCreateEntityInDatabase(OWLDataPropertyHGDB.class, iri, BuildableObjects.OWLDATAPROPERTY);    	
        //return (OWLDataProperty) unwrap(dataPropertiesByURI, iri, BuildableObjects.OWLDATAPROPERTY);
    }

    public OWLNamedIndividual getOWLNamedIndividual(IRI iri) {
        return (OWLNamedIndividual) ensureCreateEntityInDatabase(OWLNamedIndividualHGDB.class, iri, BuildableObjects.OWLNAMEDINDIVIDUAL);
    }


	public OWLDatatype getOWLDatatype(IRI iri) {
        return (OWLDatatype) ensureCreateEntityInDatabase(OWLDatatypeHGDB.class, iri, BuildableObjects.OWLDATATYPE);
	}

    public OWLAnnotationProperty getOWLAnnotationProperty(IRI iri) {
        return (OWLAnnotationProperty) ensureCreateEntityInDatabase(OWLAnnotationPropertyHGDB.class, iri, BuildableObjects.OWLANNOTATIONPROPERTY);
    }

    /**
     * ensureCreateEntityInDatabase loads an entity by IRI and type from the database or creates and adds it.
     * The returned entity is guaranteed to have a graph and handle set and to have a persisted representation in the database. 
     * 
     * The entity type MUST have an "IRI" property defined in the HG type system.
     * @param entityType the Hypergraph type of the Entity object we are trying to load. 
     * @param iri 
     * @param buildable
     * @return a loaded or created OwlEntityObject of type type.
     */
    @SuppressWarnings("unchecked")
	private <V extends OWLEntity> OWLEntity ensureCreateEntityInDatabase(final Class<V> entityType, final IRI iri, final BuildableObjects buildable) 
    {
    	final HyperGraph graph = factory.getHyperGraph();
    	boolean isBuiltin = OWLRDFVocabulary.BUILT_IN_VOCABULARY_IRIS.contains(iri) || XSD_VOCABULARY_IRIS.contains(iri);
    	
    	//check builtin cache
    	V e;
    	if (isBuiltin) {
    		e = (V)builtinByIRIClassPairCache.get(new Pair<IRI, Class<V>>(iri, entityType));
    		if (e != null) {
    			CACHE_HIT ++;
    			assert (e.getClass().equals(entityType));
    			if (graph.getHandle(e) != null) {
    				return e;
    			} else {
    				//make sure it gets added below.
    				System.out.println(" Unusual but recoverable: no handle for cached: " + e + " Class: " + e.getClass());
    			}
    		}
    	}
    	CACHE_MISS ++;
    	final HGQuery<OWLEntity> query = entityByIRIQuery(entityType);
    	e = (V)query.var("iri", iri).findOne();
    	if (e == null) {
        	e = graph.getTransactionManager().ensureTransaction(new Callable<V>() {
    			public V call() {
		    		//DOUBLE CHECK
		        	V eInt = (V)query.findOne();
		        	if (eInt == null) {
		        		eInt = (V)buildable.build(factory, iri);
			    		if (!entityType.isAssignableFrom(eInt.getClass())) throw new HGException("Built object type must be same or subclass of type " + entityType);
			    		if (!(eInt instanceof HGGraphHolder && eInt instanceof HGHandleHolder)) throw new HGException("Built entity must be Graphholder and Handleholder");
			    		if (DBG) System.out.println("FACTINTERN CREATED/ADDED ENTITY: " + eInt + " type: " + eInt.getClass().getSimpleName() );
			    		graph.add(eInt);
		        	}
		        	return eInt;
    			}}, HGTransactionConfig.DEFAULT);
    	}
		//Cache put if BUILTIN and cache miss.
		if (isBuiltin) {
			//assert (!builtinByIRIClassPairCache.containsKey(iri);
			CACHE_PUT ++;
			builtinByIRIClassPairCache.put(new Pair<IRI, Class<? extends OWLEntity>>(iri, entityType), e);    			
		}    	
		//Handle and graph guaranteed to be set on add or get. 
		return e;
	}
    
	HGHandle findOrAddIRIHandle(final IRI iri) {
		//TODO replace by assertAtom?
    	final HyperGraph graph = factory.getHyperGraph();
    	HGHandle iriHandle  = lookupIRIByValue.var("iri", iri).findOne();
//    			
//    			graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>() {
//			public HGHandle call() {
//				return hg.findOne(graph, hg.and(hg.type(IRI.class), hg.eq(iri)));
//			}}, HGTransactionConfig.READONLY);
		if (DBG) {
			System.out.println("findOrAddIRIHandle IRI: " + iri + " found?: " + iriHandle);
		}
		if (iriHandle == null) {
			//TRANSACTION START WRITE
			//DOUBLE CHECK 
	    	iriHandle  =  graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>() {
				public HGHandle call() {
					HGHandle iriHandleInt = lookupIRIByValue.var("iri", iri).findOne();
					if (iriHandleInt == null) {
						iriHandleInt = graph.add(iri);
					}
					return iriHandleInt;
				}}, HGTransactionConfig.DEFAULT);
			//TRANSACTION END
		}
		return iriHandle;
	}
}