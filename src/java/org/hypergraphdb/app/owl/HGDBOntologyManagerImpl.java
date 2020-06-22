package org.hypergraphdb.app.owl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.core.AddPrefixChange;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.core.PrefixChange;
import org.hypergraphdb.app.owl.core.PrefixChangeListener;
import org.hypergraphdb.app.owl.core.RemovePrefixChange;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersioningChangeListener;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeBroadcastStrategy;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyChangeProgressListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;

/**
 * <p>
 * HyperGraphDB backed implementation an <code>OWLOntologyManager</code>.
 * </p>
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County), Borislav Iordanov
 * @created Feb 3, 2012
 */
public class HGDBOntologyManagerImpl extends OWLOntologyManagerImpl implements HGDBOntologyManager, PrefixChangeListener
{
	private static final long serialVersionUID = 1L;

	public static boolean DBG = false;

	/**
	 * Set this to have all removed ontologies also deleted from the repository.
	 * This is intended for unit testing only.
	 */
	private static boolean deleteOntologiesOnRemove = true;

	OntologyDatabase ontologyRepository;
	VersionManager versionManager;
	
	/**
	 * @return the deleteOntologiesOnRemove
	 */
	public static boolean isDeleteOntologiesOnRemove()
	{
		return deleteOntologiesOnRemove;
	}

	/**
	 * @param deleteOntologiesOnRemove
	 *            the deleteOntologiesOnRemove to set
	 */
	public static void setDeleteOntologiesOnRemove(boolean deleteOntologiesOnRemove)
	{
		HGDBOntologyManagerImpl.deleteOntologiesOnRemove = deleteOntologiesOnRemove;
	}

	public HGDBOntologyManagerImpl(OWLDataFactoryHGDB dataFactory, OntologyDatabase ontologyRepository)
	{
		super(dataFactory);
		this.ontologyRepository = ontologyRepository;
		versionManager = new VersionManager(ontologyRepository.getHyperGraph(), null);
		this.addOntologyChangeListener(new VersioningChangeListener(versionManager));
		addIRIMapper(new HGDBIRIMapper(ontologyRepository));
		dataFactory.setHyperGraph(ontologyRepository.getHyperGraph());
		for (HGDBOntology onto : ontologyRepository.getOntologies())
		{
			this.ontologiesByID.put(onto.getOntologyID(), onto);
			this.documentIRIsByID.put(onto.getOntologyID(), onto.getDocumentIRI());
			onto.setOWLOntologyManager(this);
		}
	}

	@Override
	public OntologyDatabase getOntologyRepository()
	{
		return ontologyRepository;
	}
	
	public VersionManager getVersionManager()
	{
		return versionManager;
	}

	@Override
	public void removeOntology(OWLOntology ontology)
	{
		super.removeOntology(ontology);
		if (isDeleteOntologiesOnRemove() && ontology instanceof HGDBOntology)
		{
			OWLOntologyID oid = ontology.getOntologyID();
			if (ontologyRepository.existsOntology(oid))
			{
				versionManager.removeVersioning(ontologyRepository.getHyperGraph().getHandle(ontology));
				boolean deleted = ontologyRepository.deleteOntology(ontology.getOntologyID());
				if (DBG)
				{
					if (deleted)
					{
						System.out.println("Deleted on remove: " + ontology.getOntologyID());
					}
					else
					{
						System.out.println("Deleted on remove FAILED NOT FOUND: " + ontology.getOntologyID());
					}
				}
			}
			else
			{
				System.out.println("OID of to remove onto not found in repo: " + ontology.getOntologyID());
			}
		}
	}

	public HGDBOntology createOntologyInDatabase(IRI ontologyIRI, HGHandle handle) throws OWLOntologyCreationException
	{
		try
		{
			HGDBOntologyFormat format = new HGDBOntologyFormat().atomHandle(handle);
			IRI hgdbDocumentIRI = HGDBOntologyFormat.convertToHGDBDocumentIRI(ontologyIRI);
			OWLOntology o = super.createOntology(ontologyIRI);
			setOntologyFormat(o, format);
			setOntologyDocumentIRI(o, hgdbDocumentIRI);
			saveOntology(o, format, hgdbDocumentIRI);
			HGDBOntology result = ontologyRepository.getOntologyByDocumentIRI(hgdbDocumentIRI);
			result.setOWLOntologyManager(this);
			this.ontologiesByID.put(o.getOntologyID(), result);
			return result;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	private IRI importOne(OWLOntology o, HGDBImportConfig config) throws Exception
	{
		try
		{
			HGDBOntologyFormat format = new HGDBOntologyFormat();
			OWLOntologyFormat oldFormat = o.getOWLOntologyManager().getOntologyFormat(o);
			if (oldFormat != null && oldFormat.isPrefixOWLOntologyFormat())
				format.copyPrefixesFrom(oldFormat.asPrefixOWLOntologyFormat());
			IRI hgdbDocumentIRI = HGDBOntologyFormat.convertToHGDBDocumentIRI(o.getOntologyID().getOntologyIRI());
			setOntologyFormat(o, format);
			setOntologyDocumentIRI(o, hgdbDocumentIRI);
			saveOntology(o, format, hgdbDocumentIRI);
			o = ontologyRepository.getOntologyByDocumentIRI(hgdbDocumentIRI);			
			ontologiesByID.put(o.getOntologyID(), o);
			((HGDBOntology)o).setOWLOntologyManager(this);
			return hgdbDocumentIRI;
		}
		catch (Exception ex)
		{
			if (config.silentMissingImports())
				return null;
			else
				throw ex;
		}
	}
	
	public HGDBOntology importOntology(IRI documentIRI, HGDBImportConfig config)
	{
		try
		{
			this.setSilentMissingImportsHandling(config.silentMissingImports());			
			OWLOntologyLoaderConfiguration owlapiConfig = new OWLOntologyLoaderConfiguration();
			for (IRI ignoredIri : config.ignored())
				owlapiConfig = owlapiConfig.addIgnoredImport(ignoredIri);
			OWLOntology o = loadOntologyFromOntologyDocument(new IRIDocumentSource(documentIRI), owlapiConfig);
			IRI hgdbDocumentIRI = importOne(o, config);
			for (OWLOntology imp : o.getImportsClosure())
				if (!imp.getOntologyID().equals(o.getOntologyID()) && 
					!config.ignored().contains(imp.getOntologyID().getOntologyIRI()))
				{
					importOne(imp, config);
				}
			return ontologyRepository.getOntologyByDocumentIRI(hgdbDocumentIRI);
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public void setOntologyFormat(OWLOntology ontology, OWLOntologyFormat format)
	{
		if (format instanceof HGDBOntologyFormat)
		{
			((HGDBOntologyFormat) format).addPrefixChangeListener(this);
		}
		super.setOntologyFormat(ontology, format);
	}

	/**
	 * We override the OWLAPI version which insists on having the imported ontology currently loaded in memory.
	 * In our case, we have it in the database loaded on demand, so as long as we can find the ontology 
	 * and the load the object (without necessarily all the axioms), we are good.
	 */
    @Override
    public Set<OWLOntology> getDirectImports(OWLOntology ontology) throws UnknownOWLOntologyException 
    {
        if (!contains(ontology)) 
        {
            throw new UnknownOWLOntologyException(ontology.getOntologyID());
        }
        Set<OWLOntology> imports = new HashSet<OWLOntology>();
        for (OWLImportsDeclaration axiom : ontology.getImportsDeclarations()) 
        {
            OWLOntology importedOntology = this.getOntology(axiom.getIRI());
            if (importedOntology != null) 
            {
                imports.add(importedOntology);
            }
        }
        return imports;
    }
	
	@Override
	public void prefixChanged(PrefixChange e)
	{
		// We get notfied here if anybody modifies prefixes.
		// We will have to look up the ontology and call for a change to be
		// applied
		HGDBOntology ho = getOntologyForFormat(e.getFormat());
		if (e instanceof AddPrefixChange)
		{
			applyChange(new AddPrefixChange(ho, e.getPrefixName(), e.getPrefix()));
		}
		else if (e instanceof RemovePrefixChange)
		{
			applyChange(new RemovePrefixChange(ho, e.getPrefixName(), e.getPrefix()));
		}
		else
		{
			throw new IllegalArgumentException("Unknown prefixchange: " + e + "" + e.getClass());
		}
	}

	public HGDBOntology getOntologyForFormat(HGDBOntologyFormat f)
	{
		for (OWLOntology o : getOntologies())
		{
			if (o instanceof HGDBOntology)
			{
				OWLOntologyFormat candidate = getOntologyFormat(o);
				if (f == candidate)
				{
					return (HGDBOntology) o;
				}
			}
		}
		return null;
	}
	
	public OWLDataFactory getDataFactory()
	{
		return OWLDataFactoryHGDB.get(ontologyRepository.getHyperGraph());
	}
	
	//
	// The following list of methods handling manager change events have been overwritten for the 
	// sole purpose of not swallowing exceptions. Sometimes listeners participate
	// in a HGDB transaction which could throw an exception in case of conflict.
	// 
	//
	private final Map<OWLOntologyChangeListener, OWLOntologyChangeBroadcastStrategy> listenerMap = new IdentityHashMap<OWLOntologyChangeListener, OWLOntologyChangeBroadcastStrategy>();
    @Override
    public void addOntologyChangeListener(OWLOntologyChangeListener listener) {
        listenerMap.put(listener, defaultChangeBroadcastStrategy);
    }
    @Override
    public void addOntologyChangeListener(OWLOntologyChangeListener listener,
            OWLOntologyChangeBroadcastStrategy strategy) {
        listenerMap.put(listener, strategy);
    }
    @Override
    public void removeOntologyChangeListener(OWLOntologyChangeListener listener) {
        listenerMap.remove(listener);
    }
 
    protected void broadcastChanges(List<? extends OWLOntologyChange> changes) {
        for (OWLOntologyChangeListener listener : new ArrayList<OWLOntologyChangeListener>(
                listenerMap.keySet())) {
            OWLOntologyChangeBroadcastStrategy strategy = listenerMap.get(listener);
            if (strategy == null) {
                // This listener may have been removed during the broadcast of the changes,
                // so when we attempt to retrieve it from the map it isn't there (because
                // we iterate over a copy).
                continue;
            }
            try {
                // Handle exceptions on a per listener basis.  If we have
                // badly behaving listeners, we don't want one listener
                // to prevent the other listeners from receiving events.
                strategy.broadcastChanges(listener, changes);
            }
			catch (OWLException e)
			{
				e.printStackTrace();
			} 
        }
    }
    
    protected void fireBeginChanges(int size) {
        try {
            for (OWLOntologyChangeProgressListener lsnr : progressListeners) {
                lsnr.begin(size);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void fireEndChanges() {
        try {
            for (OWLOntologyChangeProgressListener lsnr : progressListeners) {
                lsnr.end();
            }
        } catch (Throwable e) {
            // Listener threw an exception
            e.printStackTrace();
        }
    }
    
}