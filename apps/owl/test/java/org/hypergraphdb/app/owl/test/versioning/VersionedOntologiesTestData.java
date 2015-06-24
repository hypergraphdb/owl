package org.hypergraphdb.app.owl.test.versioning;

import static org.hypergraphdb.app.owl.test.TU.aInstanceOf;
import static org.hypergraphdb.app.owl.test.TU.aProp;
import static org.hypergraphdb.app.owl.test.TU.aSubclassOf;
import static org.hypergraphdb.app.owl.test.TU.declare;
import static org.hypergraphdb.app.owl.test.TU.dprop;
import static org.hypergraphdb.app.owl.test.TU.individual;
import static org.hypergraphdb.app.owl.test.TU.literal;
import static org.hypergraphdb.app.owl.test.TU.oprop;
import static org.hypergraphdb.app.owl.test.TU.owlClass;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.test.OntologyContext;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.app.owl.versioning.ChangeRecord;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Some static methods to generate various versioned ontologies for
 * testing purposes.
 * 
 * @author Borislav Iordanov
 *
 */
public class VersionedOntologiesTestData
{
	public static void revisionGraph_1(String iri, TestContext ctx)
	{
		TestContext saveCtx = TU.ctx();
		try
		{
			if (ctx != null)
				TU.ctx.set(ctx);
			else
				ctx = TU.ctx();
			ctx.o = (HGDBOntology)ctx.m.createOntology(IRI.create(iri)); 
			ctx.vr = new VersionManager(ctx.graph, "testuser");
			ctx.vo = ctx.vr.versioned(ctx.graph.getHandle(ctx.o));
			Revision initialRevision = ctx.vo.revision();
			declare(owlClass("User"));
			declare(owlClass("Employee"));
			aSubclassOf(owlClass("User"), owlClass("Employee"));
			Revision revision1 = ctx.vo.commit("anonymous", "First version");
			
			declare(individual("Pedro"));
			aInstanceOf(owlClass("Employee"), individual("Pedro"));
			aSubclassOf(owlClass("User"), owlClass("Customer"));
			declare(owlClass("LoyalCustomer"));
			ChangeRecord mark1 = ctx.vo.flushChanges();
			
			aSubclassOf(owlClass("Customer"), owlClass("LoyalCustomer"));
			aInstanceOf(owlClass("LoyalCustomer"), individual("Mary"));
			aInstanceOf(owlClass("LoyalCustomer"), individual("Tom"));
			ChangeRecord mark2 = ctx.vo.flushChanges();
			
			aInstanceOf(owlClass("Customer"), individual("John"));
			aInstanceOf(owlClass("Employee"), individual("Fred"));
			declare(oprop("isServing"));		
			aProp(oprop("isServing"), individual("Fred"), individual("Tom"));
			ChangeRecord mark3 = ctx.vo.flushChanges();
			// no changes between last flush and the creation of a new revision
			Revision revision2 = ctx.vo.commit("administrator", "Second version by admin");		
			
			// some more in working set, shouldn't be serialized
			aProp(dprop("hasAge"), individual("Mary"), literal("54"));
		}
		catch (OWLOntologyCreationException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			TU.ctx.set(saveCtx);
		}
	}
}
