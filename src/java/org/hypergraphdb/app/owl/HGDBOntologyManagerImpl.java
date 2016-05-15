package org.hypergraphdb.app.owl;

import java.io.File;


import java.util.concurrent.Callable;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.AddPrefixChange;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.core.PrefixChange;
import org.hypergraphdb.app.owl.core.PrefixChangeListener;
import org.hypergraphdb.app.owl.core.RemovePrefixChange;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.VersioningChangeListener;
import org.hypergraphdb.app.owl.versioning.distributed.activity.ActivityUtils;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;

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

	public static boolean DBG = true;

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
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.app.owl.HGDBOntologyManager#getOntologyRepository()
	 */
	@Override
	public OntologyDatabase getOntologyRepository()
	{
		return ontologyRepository;
	}
	
	public VersionManager getVersionManager()
	{
		return versionManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl#removeOntology(
	 * org.semanticweb.owlapi.model.OWLOntology)
	 */
	@Override
	public void removeOntology(OWLOntology ontology)
	{
		super.removeOntology(ontology);
		if (isDeleteOntologiesOnRemove())
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

	/**
	 * Imports a full versionedOntology from a VOWLXMLFormat file. Throws one
	 * of: OWLOntologyChangeException, UnloadableImportException,
	 * HGDBOntologyAlreadyExistsByDocumentIRIException,
	 * HGDBOntologyAlreadyExistsByOntologyIDException,
	 * HGDBOntologyAlreadyExistsByOntologyUUIDException, OWLParserException,
	 * IOException wrapped as cause of a RuntimeException.
	 */
	public VersionedOntology importVersionedOntology(File vowlxmlFile) throws RuntimeException
	{
		if (!vowlxmlFile.exists())
			throw new IllegalArgumentException("File does not exist: " + vowlxmlFile);
		final FileDocumentSource fds = new FileDocumentSource(vowlxmlFile);
		HyperGraph graph = ontologyRepository.getHyperGraph();
		return graph.getTransactionManager().ensureTransaction(new Callable<VersionedOntology>()
		{
			public VersionedOntology call()
			{
				try
				{
					VOWLXMLDocument doc = ActivityUtils.parseVersionedDoc(HGDBOntologyManagerImpl.this, fds);
					return ActivityUtils.storeClonedOntology(HGDBOntologyManagerImpl.this, doc);
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			}
		});
	}

	public HGDBOntology createOntologyInDatabase(IRI ontologyIRI) throws OWLOntologyCreationException
	{
		try
		{
			HGDBOntologyFormat format = new HGDBOntologyFormat();
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

	public HGDBOntology importOntology(IRI documentIRI)
	{
		try
		{
			this.setSilentMissingImportsHandling(true);
//			OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration().addIgnoredImport(IRI.create("http://www.essepuntato.it/2008/12/pattern"));
			OWLOntology o = loadOntologyFromOntologyDocument(new IRIDocumentSource(documentIRI)/*, config*/);
			HGDBOntologyFormat format = new HGDBOntologyFormat();
			IRI hgdbDocumentIRI = HGDBOntologyFormat.convertToHGDBDocumentIRI(o.getOntologyID().getOntologyIRI());
			setOntologyFormat(o, format);
			setOntologyDocumentIRI(o, hgdbDocumentIRI);
			saveOntology(o, format, hgdbDocumentIRI);
			HGDBOntology result = ontologyRepository.getOntologyByDocumentIRI(hgdbDocumentIRI);
			result.setOWLOntologyManager(this);
			return result;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.core.PrefixChangeListener#prefixChanged(org.
	 * hypergraphdb.app.owl.core.PrefixChange)
	 */
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
}