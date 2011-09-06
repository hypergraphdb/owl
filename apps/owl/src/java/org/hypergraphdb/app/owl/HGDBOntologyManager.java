package org.hypergraphdb.app.owl;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HyperGraph;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.ImpendingOWLOntologyChangeListener;
import org.semanticweb.owlapi.model.MissingImportListener;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeBroadcastStrategy;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyChangeProgressListener;
import org.semanticweb.owlapi.model.OWLOntologyChangeVetoException;
import org.semanticweb.owlapi.model.OWLOntologyChangesVetoedListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFactory;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyRenameException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLOntologyStorer;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class HGDBOntologyManager implements OWLOntologyManager, HGGraphHolder
{
	private HyperGraph graph;
	
	public HGDBOntologyManager()
	{		
	}
	
	public HGDBOntologyManager(HyperGraph graph)
	{
		this.graph = graph;
	}
		
	public HyperGraph getHyperGraph()
	{
		return graph;
	}

	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
	}

	public List<OWLOntologyChange> addAxiom(OWLOntology ont, OWLAxiom axiom)
	{
		return addAxioms(ont, Collections.singleton(axiom));
	}

	public List<OWLOntologyChange> addAxioms(OWLOntology ont, Set<? extends OWLAxiom> axioms)
	{
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>(axioms.size() + 2);
        for (OWLAxiom ax : axioms) {
            changes.add(new AddAxiom(ont, ax));
        }
        return applyChanges(changes);
	}

	public void addIRIMapper(OWLOntologyIRIMapper arg0)
	{
		// TODO Auto-generated method stub

	}

	public void addMissingImportListener(MissingImportListener arg0)
	{
		// TODO Auto-generated method stub

	}

	public void addOntologyChangeListener(OWLOntologyChangeListener arg0)
	{
		// TODO Auto-generated method stub

	}

	public void addOntologyChangeListener(OWLOntologyChangeListener arg0,
			OWLOntologyChangeBroadcastStrategy arg1)
	{
		// TODO Auto-generated method stub

	}

	public void addOntologyChangeProgessListener(
			OWLOntologyChangeProgressListener arg0)
	{
		// TODO Auto-generated method stub

	}

	public void addOntologyFactory(OWLOntologyFactory arg0)
	{
		// TODO Auto-generated method stub

	}

	public void addOntologyLoaderListener(OWLOntologyLoaderListener arg0)
	{
		// TODO Auto-generated method stub

	}

	public void addOntologyStorer(OWLOntologyStorer arg0)
	{
		// TODO Auto-generated method stub

	}

	public List<OWLOntologyChange> applyChange(OWLOntologyChange change)
			throws OWLOntologyRenameException
	{
        return applyChanges(Arrays.asList(change));
    }

	public List<OWLOntologyChange> applyChanges(List<? extends OWLOntologyChange> changes)
			throws OWLOntologyRenameException
	{
        try 
        {
            //broadcastImpendingChanges(changes);
        }
        catch (OWLOntologyChangeVetoException e) 
        {
            // Some listener blocked the changes.
            //broadcastOntologyChangesVetoed(changes, e);
            return Collections.emptyList();
        }
        List<OWLOntologyChange> appliedChanges = new ArrayList<OWLOntologyChange>(changes.size() + 2);
//        fireBeginChanges(changes.size());
//        for (OWLOntologyChange change : changes) 
//        {
//            appliedChanges.addAll(enactChangeApplication(change));
//            fireChangeApplied(change);
//        }
//        fireEndChanges();
//        broadcastChanges(changes);
        return appliedChanges;
	}

	public void clearIRIMappers()
	{
		// TODO Auto-generated method stub

	}

	public boolean contains(IRI arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean contains(OWLOntologyID arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public OWLOntology createOntology() throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology createOntology(Set<OWLAxiom> arg0)
			throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology createOntology(IRI arg0)
			throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology createOntology(OWLOntologyID arg0)
			throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology createOntology(Set<OWLAxiom> arg0, IRI arg1)
			throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology createOntology(IRI arg0, Set<OWLOntology> arg1)
			throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology createOntology(IRI arg0, Set<OWLOntology> arg1,
			boolean arg2) throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLOntology> getDirectImports(OWLOntology arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology getImportedOntology(OWLImportsDeclaration arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLOntology> getImports(OWLOntology arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLOntology> getImportsClosure(OWLOntology arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLDataFactory getOWLDataFactory()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLOntology> getOntologies()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLOntology> getOntologies(OWLAxiom arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology getOntology(IRI arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology getOntology(OWLOntologyID arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public IRI getOntologyDocumentIRI(OWLOntology arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntologyFormat getOntologyFormat(OWLOntology arg0)
			throws UnknownOWLOntologyException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<OWLOntology> getSortedImportsClosure(OWLOntology arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLOntology> getVersions(IRI arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSilentMissingImportsHandling()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public OWLOntology loadOntology(IRI arg0)
			throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology loadOntologyFromOntologyDocument(IRI arg0)
			throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology loadOntologyFromOntologyDocument(File arg0)
			throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology loadOntologyFromOntologyDocument(InputStream arg0)
			throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntology loadOntologyFromOntologyDocument(
			OWLOntologyDocumentSource arg0) throws OWLOntologyCreationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void makeLoadImportRequest(OWLImportsDeclaration arg0)
			throws UnloadableImportException
	{
		// TODO Auto-generated method stub

	}

	public List<OWLOntologyChange> removeAxiom(OWLOntology arg0, OWLAxiom arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<OWLOntologyChange> removeAxioms(OWLOntology arg0,
			Set<? extends OWLAxiom> arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void removeIRIMapper(OWLOntologyIRIMapper arg0)
	{
		// TODO Auto-generated method stub

	}

	public void removeMissingImportListener(MissingImportListener arg0)
	{
		// TODO Auto-generated method stub

	}

	public void removeOntology(OWLOntology arg0)
	{
		// TODO Auto-generated method stub

	}

	public void removeOntologyChangeListener(OWLOntologyChangeListener arg0)
	{
		// TODO Auto-generated method stub

	}

	public void removeOntologyChangeProgessListener(
			OWLOntologyChangeProgressListener arg0)
	{
		// TODO Auto-generated method stub

	}

	public void removeOntologyFactory(OWLOntologyFactory arg0)
	{
		// TODO Auto-generated method stub

	}

	public void removeOntologyLoaderListener(OWLOntologyLoaderListener arg0)
	{
		// TODO Auto-generated method stub

	}

	public void removeOntologyStorer(OWLOntologyStorer arg0)
	{
		// TODO Auto-generated method stub

	}

	public void saveOntology(OWLOntology arg0)
			throws OWLOntologyStorageException
	{
		// TODO Auto-generated method stub

	}

	public void saveOntology(OWLOntology arg0, IRI arg1)
			throws OWLOntologyStorageException
	{
		// TODO Auto-generated method stub

	}

	public void saveOntology(OWLOntology arg0, OutputStream arg1)
			throws OWLOntologyStorageException
	{
		// TODO Auto-generated method stub

	}

	public void saveOntology(OWLOntology arg0, OWLOntologyFormat arg1)
			throws OWLOntologyStorageException
	{
		// TODO Auto-generated method stub

	}

	public void saveOntology(OWLOntology arg0, OWLOntologyDocumentTarget arg1)
			throws OWLOntologyStorageException
	{
		// TODO Auto-generated method stub

	}

	public void saveOntology(OWLOntology arg0, OWLOntologyFormat arg1, IRI arg2)
			throws OWLOntologyStorageException
	{
		// TODO Auto-generated method stub

	}

	public void saveOntology(OWLOntology arg0, OWLOntologyFormat arg1,
			OutputStream arg2) throws OWLOntologyStorageException
	{
		// TODO Auto-generated method stub

	}

	public void saveOntology(OWLOntology arg0, OWLOntologyFormat arg1,
			OWLOntologyDocumentTarget arg2) throws OWLOntologyStorageException
	{
		// TODO Auto-generated method stub

	}

	public void setDefaultChangeBroadcastStrategy(
			OWLOntologyChangeBroadcastStrategy arg0)
	{
		// TODO Auto-generated method stub

	}

	public void setOntologyDocumentIRI(OWLOntology arg0, IRI arg1)
			throws UnknownOWLOntologyException
	{
		// TODO Auto-generated method stub

	}

	public void setOntologyFormat(OWLOntology arg0, OWLOntologyFormat arg1)
	{
		// TODO Auto-generated method stub

	}

	public void setSilentMissingImportsHandling(boolean arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void addImpendingOntologyChangeListener(
			ImpendingOWLOntologyChangeListener arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOntologyChangesVetoedListener(
			OWLOntologyChangesVetoedListener arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<OWLOntologyFactory> getOntologyFactories()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeImpendingOntologyChangeListener(
			ImpendingOWLOntologyChangeListener arg0)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeOntologyChangesVetoedListener(
			OWLOntologyChangesVetoedListener arg0)
	{
		// TODO Auto-generated method stub
		
	}
}
