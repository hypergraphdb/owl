package org.hypergraphdb.app.owl.test.versioning;

import static org.hypergraphdb.app.owl.test.TU.aInstanceOf;
import static org.hypergraphdb.app.owl.test.TU.individual;
import static org.hypergraphdb.app.owl.test.TU.owlClass;

import java.io.File;

import junit.framework.Assert;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.test.TestData;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.util.HGUtils;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class VersioningTests // implements OntologyContext
{
	static final String dblocation = 
			System.getProperty("java.io.tmpdir") + 
			File.separator + 
			"hgdbtest";
	protected HGDBOntology o;
	protected VersionedOntology vo;
	protected HGDBOntologyManager m;
	protected OWLDataFactory df;
	protected VHGDBOntologyRepository r;
	
	public OWLOntology ontology() { return o; }
	public OWLOntologyManager manager() { return o.getOWLOntologyManager(); }
	public OWLDataFactory df() { return df; }
	public HGDBOntologyRepository repo() { return r; }
	public VHGDBOntologyRepository vrepo() { return r; }
	
	@Before
	public void setUp() throws Exception
	{
		HGUtils.dropHyperGraphInstance(dblocation);
		r =  new VHGDBOntologyRepository(dblocation);		
		m = HGOntologyManagerFactory.getOntologyManager(dblocation);
		o = (HGDBOntology)m.createOntology(IRI.create("hgdb://UNITTESTONT_VERSIONED"));
		vo = r.addVersionControl(o, "testuser");
		df = m.getOWLDataFactory();				
		//TU.ctx = this;
		TestData.fillOntology(df, o);
	}	
	
	@Test
	public void testCommit()
	{
		vo.commit();		
		aInstanceOf(owlClass("ClassCommit"), individual("IndividualCommit"));	
		ChangeSet changeSet = vo.getWorkingSetChanges();
		System.out.println(changeSet.getChanges());
		vo.commit();
		System.out.println(vo.getChangeSet(vo.getHeadRevision()).getChanges());
		Revision lastCommitted = vo.getRevisions().get(vo.getNrOfRevisions() - 2);
		Assert.assertEquals(changeSet, vo.getChangeSet(lastCommitted));
		Assert.assertEquals(0, vo.getWorkingSetChanges().size());
	}
}
