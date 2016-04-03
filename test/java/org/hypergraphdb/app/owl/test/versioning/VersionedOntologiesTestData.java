package org.hypergraphdb.app.owl.test.versioning;

import static org.hypergraphdb.app.owl.test.TU.*;


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.app.owl.util.OntologyComparator;
import org.hypergraphdb.app.owl.versioning.ChangeLink;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.Change;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.versioning;
import org.hypergraphdb.app.owl.versioning.change.VAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VImportChange;
import org.hypergraphdb.app.owl.versioning.change.VModifyOntologyIDChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeVisitor;
import org.hypergraphdb.app.owl.versioning.change.VOntologyAnnotationChange;
import org.hypergraphdb.app.owl.versioning.change.VPrefixChange;
import org.hypergraphdb.util.HGUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import junit.framework.Assert;

/**
 * Some static methods to generate various versioned ontologies for
 * testing purposes.
 * 
 * @author Borislav Iordanov
 *
 */
public class VersionedOntologiesTestData
{
	public static boolean compareChangeLists(final HyperGraph lgraph, 
											 final HyperGraph rgraph, 
											 final List<Change<VersionedOntology>> llist, 
											 final List<Change<VersionedOntology>> rlist)
	{
		Iterator<Change<VersionedOntology>> liter = llist.iterator();
		Iterator<Change<VersionedOntology>> riter = rlist.iterator();
		while (liter.hasNext())
		{
			if (!riter.hasNext())
				return false;
			final Change<VersionedOntology> lchange = liter.next();
			final Change<VersionedOntology> rchange = riter.next();
			if (!lchange.getClass().equals(rchange.getClass()))
				return false;
			final boolean [] compare = new boolean[1];
			if (lchange instanceof VOWLChange)
				((VOWLChange)lchange).accept(new VOWLChangeVisitor() {

					@Override
					public void visit(VAxiomChange change)
					{
						compare[0] = change.getAxiom().equals(((VAxiomChange)rchange).getAxiom());
					}

					@Override
					public void visit(VImportChange change)
					{
						compare[0] = change.getImportDeclaration().equals(((VImportChange)rchange).getImportDeclaration());						
					}

					@Override
					public void visit(VOntologyAnnotationChange change)
					{
						compare[0] = change.getOntologyAnnotation().equals(((VOntologyAnnotationChange)rchange).getOntologyAnnotation());
					}

					@Override
					public void visit(VModifyOntologyIDChange change)
					{
						compare[0] = change.getNewOntologyID().equals(((VModifyOntologyIDChange)rchange).getNewOntologyID()) &&
								change.getOldOntologyID().equals(((VModifyOntologyIDChange)rchange).getOldOntologyID());
					}

					@Override
					public void visit(VPrefixChange change)
					{
						compare[0] = change.getPrefix().equals(((VPrefixChange)rchange).getPrefix());					
					}					
				});
		}
		return true;
	}
	
	public static boolean compareWorkingSets(VersionedOntology left, VersionedOntology right)
	{
		return !OntologyComparator.compare(left.ontology(), right.ontology()).hasChanges();
	}
	
	public static boolean compareOntologies(HGDBOntology left, HGDBOntology right)
	{
		return left.getAxioms().equals(right.getAxioms());
	}
	
	/**
	 * Full revision graph comparison. Not that this won't compare the working copies, only the
	 * revision graphs!
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public static boolean compareOntologyRevisions(VersionedOntology left, HyperGraph leftRepo, 
												   VersionedOntology right, HyperGraph rightRepo)
	{
		Set<HGHandle> leftRevisions = new HashSet<HGHandle>();
		Set<HGHandle> rightRevisions = new HashSet<HGHandle>();
		leftRevisions.addAll(leftRepo.findAll(hg.dfs(left.getRootRevision(), hg.type(ChangeLink.class), hg.type(Revision.class))));
		rightRevisions.addAll(rightRepo.findAll(hg.dfs(right.getRootRevision(), hg.type(ChangeLink.class), hg.type(Revision.class))));
		for (HGHandle revisionHandle : leftRevisions)
		{
			if (!rightRevisions.contains(revisionHandle))
				return false;
			Revision revLeft = leftRepo.get(revisionHandle);
			Revision revRight = rightRepo.get(revisionHandle);
			if (!revLeft.parents().equals(revRight.parents()) ||
				!revLeft.children().equals(revRight.children())  ||
				!HGUtils.eq(revLeft.branchHandle(), revRight.branchHandle()))
				return false;
			for (HGHandle parent : revLeft.parents())
			{
				List<Change<VersionedOntology>> leftChanges = versioning.changes(leftRepo, revisionHandle, parent);
				List<Change<VersionedOntology>> rightChanges = versioning.changes(rightRepo, revisionHandle, parent);
				if (leftChanges.equals(rightChanges))
				{
					List<Change<VersionedOntology>> llist = leftChanges;
					List<Change<VersionedOntology>> rlist = rightChanges;
					if (!compareChangeLists(leftRepo, rightRepo, llist, rlist))
						return false;
				}
				else
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
			a(declare(owlClass("User")));
			a(declare(owlClass("Employee")));
			aSubclassOf(owlClass("User"), owlClass("Employee"));
			Revision revision1 = ctx.vo.commit("anonymous", "First version");
			Assert.assertNotNull(ctx.vo.revision());
			a(declare(individual("Pedro")));
			aInstanceOf(owlClass("Employee"), individual("Pedro"));
			aSubclassOf(owlClass("User"), owlClass("Customer"));
			a(declare(owlClass("LoyalCustomer")));
			
			aSubclassOf(owlClass("Customer"), owlClass("LoyalCustomer"));
			aInstanceOf(owlClass("LoyalCustomer"), individual("Mary"));
			aInstanceOf(owlClass("LoyalCustomer"), individual("Tom"));
			
			aInstanceOf(owlClass("Customer"), individual("John"));
			aInstanceOf(owlClass("Employee"), individual("Fred"));
			a(declare(oprop("isServing")));		
			aProp(oprop("isServing"), individual("Fred"), individual("Tom"));
			// no changes between last flush and the creation of a new revision
			Revision revision2 = ctx.vo.commit("administrator", "Second version by admin");		
			Assert.assertNotNull(ctx.vo.revision());
			// some more in working set, shouldn't be serialized
			aProp(dprop("hasAge"), individual("Mary"), literal("54"));
			Assert.assertNotNull(ctx.vo.revision());
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
	

	public static String randomAlphaString(int len) 
	{
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder(len);
		for(int i = 0; i < len; i++) 
			sb.append(AB.charAt(rnd.nextInt(AB.length())));
		return sb.toString();
	}	
	
	/**
	 * Create a few random changes to add to the ontology in the passed in context.
	 * The changes themselves are returned as a set of axioms so one can make checks
	 * about them.
	 * @param ctx
	 */
	public static Set<OWLAxiom> makeSomeOntologyChanges(TestContext ctx)
	{
		TestContext saveCtx = TU.ctx();
		try
		{
			if (ctx != null)
				TU.ctx.set(ctx);
			else
				ctx = TU.ctx();
			Set<OWLAxiom> result = new HashSet<OWLAxiom>();
			Random random = new Random() ;
	        int count = random.nextInt(9) + 1;
	        for (int i = 0; i < count; i++)
	        {
	        	int r = random.nextInt() % 5;
	        	switch (r)
	        	{
	        		case 0:
	        			result.add(declare(individual(randomAlphaString(random.nextInt(10) + 3))));
	        			break;
	        		case 1:
	        			result.add(aInstanceOf(owlClass(randomAlphaString(random.nextInt(10) + 3)), 
	        						individual(randomAlphaString(random.nextInt(10) + 3))));
	        			break;
	        		case 2:
	        			result.add(aProp(oprop(randomAlphaString(random.nextInt(10) + 3)), 
	        					individual(randomAlphaString(random.nextInt(10) + 3)), 
	        					individual(randomAlphaString(random.nextInt(10) + 3))));
	        			break;
	        		case 3:
	        			result.add(aProp(dprop(randomAlphaString(random.nextInt(10) + 3)), 
	        					individual(randomAlphaString(random.nextInt(10) + 3)), 
	        					literal(randomAlphaString(random.nextInt(10) + 3))));
	        			break;
	        		case 4:
	        			result.add(aSubclassOf(owlClass(randomAlphaString(random.nextInt(10) + 3)), 
	        						owlClass(randomAlphaString(random.nextInt(10) + 3))));
	        			break;
	        		default:
	        			result.add(declare(owlClass(randomAlphaString(random.nextInt(10) + 3))));
	        			break;
	        	}
	        }
	        return result;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			TU.ctx.set(saveCtx);
		}		
	}
}
