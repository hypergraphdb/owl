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

import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.app.owl.versioning.ChangeRecord;
import org.hypergraphdb.app.owl.versioning.ParentLink;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.RevisionMark;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
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
	/**
	 * Full revision graph comparison.
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public static boolean compareOntologies(VersionedOntology left, HyperGraph leftRepo, 
											VersionedOntology right, HyperGraph rightRepo)
	{
		Set<HGHandle> leftRevisions = new HashSet<HGHandle>();
		Set<HGHandle> rightRevisions = new HashSet<HGHandle>();
		leftRevisions.addAll(leftRepo.findAll(hg.dfs(left.getRootRevision(), hg.type(ParentLink.class), hg.type(Revision.class))));
		rightRevisions.addAll(rightRepo.findAll(hg.dfs(right.getRootRevision(), hg.type(ParentLink.class), hg.type(Revision.class))));
		for (HGHandle revisionHandle : leftRevisions)
		{
			if (!rightRevisions.contains(revisionHandle))
				return false;
			Revision revLeft = leftRepo.get(revisionHandle);
			Revision revRight = rightRepo.get(revisionHandle);
			if (!revLeft.parents().equals(revRight.parents()) ||
				!revLeft.children().equals(revRight.children())  ||
				!revLeft.changeRecords().equals(revRight.changeRecords()))
				return false;
			for (HGHandle markHandle : revRight.revisionMarks())
			{
				RevisionMark markRight = rightRepo.get(markHandle);				
				RevisionMark markLeft = leftRepo.get(markHandle);
				if (!markLeft.revision().equals(markRight.revision()))
					return false;
				if (!markLeft.changeRecord().equals(markRight.changeRecord()))
					return false;
				ChangeRecord recordLeft = leftRepo.get(markLeft.changeRecord());
				ChangeRecord recordRight = rightRepo.get(markRight.changeRecord());
				if (!recordLeft.changeset().equals(recordRight.changeset()) ||
					!recordLeft.parents().equals(recordRight.parents()) ||
					!recordLeft.children().equals(recordRight.children()))
					return false;
				if (!leftRepo.get(recordLeft.changeset()).equals(rightRepo.get(recordRight.changeset())))
					return false;
			}
		}
		return true;
	}
	
	public static void emptyRevisionGraph(String iri, TestContext ctx)
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
