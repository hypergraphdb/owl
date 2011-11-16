package org.hypergraphdb.app.owl.core;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.model.OWLAnnotationPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLClassHGDB;
import org.hypergraphdb.app.owl.model.OWLDataPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLDatatypeHGDB;
import org.hypergraphdb.app.owl.model.OWLNamedIndividualHGDB;
import org.hypergraphdb.app.owl.model.OWLObjectPropertyHGDB;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

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
	public static boolean DBG = true;
	
    //private WeakHashMap<IRI, WeakReference<? extends OWLEntity>> classesByURI;
    //private final WeakHashMap<IRI, WeakReference<? extends OWLEntity>> objectPropertiesByURI;
    //private final WeakHashMap<IRI, WeakReference<? extends OWLEntity>> dataPropertiesByURI;
    //private final WeakHashMap<IRI, WeakReference<? extends OWLEntity>> datatypesByURI;
    //private final WeakHashMap<IRI, WeakReference<? extends OWLEntity>> individualsByURI;
    //private final WeakHashMap<IRI, WeakReference<? extends OWLEntity>> annotationPropertiesByURI;
    private final OWLDataFactoryHGDB factory;

    public OWLDataFactoryInternalsHGDB(OWLDataFactoryHGDB f) {
        factory = f;
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
	private <V extends OWLEntity> OWLEntity ensureCreateEntityInDatabase(Class<V> entityType, IRI iri, BuildableObjects buildable) {
    	HyperGraph graph = factory.getHyperGraph();
    	V e = hg.getOne(graph, hg.and(hg.type(entityType), hg.eq("IRI", iri)));
    	if (e == null) {
    		e = (V)buildable.build(factory, iri);
    		if (!entityType.isAssignableFrom(e.getClass())) throw new HGException("Built object type must be same or subclass of type " + entityType);
    		if (!(e instanceof HGGraphHolder && e instanceof HGHandleHolder)) throw new HGException("Built entity must be Graphholder and Handleholder");
    		if (DBG) System.out.println("FACTINTERN CREATED/ADDED ENTITY: " + e + " type: " + e.getClass().getSimpleName() );
    		graph.add(e);
    	}
		//Handle and graph guaranteed to be set on add or get. 
		return e;
	}
    
	HGHandle findOrAddIRIHandle(IRI iri) {
    	HyperGraph graph = factory.getHyperGraph();
		HGHandle iriHandle = hg.findOne(graph, hg.and(hg.type(IRI.class), hg.eq(iri)));
		if (DBG) {
			System.out.println("findOrAddIRIHandle IRI: " + iri + " found?: " + iriHandle);
		}
		if (iriHandle == null) {
			iriHandle = graph.add(iri);
		}
		return iriHandle;
	}
}