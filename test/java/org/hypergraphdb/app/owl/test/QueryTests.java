package org.hypergraphdb.app.owl.test;

import java.util.List;

import org.junit.Assert;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.OrderedLinkCondition;
import org.hypergraphdb.query.impl.KeyBasedQuery;
import org.hypergraphdb.query.impl.PipeQuery;
import org.junit.Before;
import org.junit.Test;

import static org.hypergraphdb.app.owl.test.TU.*;

public class QueryTests extends BaseTestOwl
{
	@Before
	public void createOntologyContext()
	{
		TU.ctx.set(TU.newCtx(dblocation));
		ctx = TU.ctx();
		String ontologyIri = iri_prefix + "_querytests";
		TU.ctx().prefix = ontologyIri;
		TU.ctx().o = super.make(ontologyIri);		
		declare(oprop("hasFriend"));
		declare(dprop("isNamed"));
		declare(individual("Bob"));
		aProp(dprop("isNamed"), individual("Bob"), "Bob");
		declare(individual("Tom"));
		aProp(dprop("isNamed"), individual("Tom"), "Tom");
		declare(individual("Elle"));
		aProp(dprop("isNamed"), individual("Elle"), "Elle");
		declare(individual("Anne"));
		aProp(dprop("isNamed"), individual("Anne"), "Anne");
		aProp(oprop("hasFriend"), individual("Elle"), individual("Bob"));
		aProp(oprop("hasFriend"), individual("Tom"), individual("Bob"));
	}
	
	KeyBasedQuery<HGHandle, HGHandle> sibling(final HyperGraph graph, final int unknown, final int known, final HGHandle...tuple)
	{
		final OrderedLinkCondition link = hg.orderedLink(tuple); 
		KeyBasedQuery<HGHandle, HGHandle> result = new KeyBasedQuery<HGHandle, HGHandle>() {			
			public HGSearchResult<HGHandle> execute() 
			{
				return graph.find(hg.apply(hg.targetAt(graph, unknown), link)); 						
			}

			public void setKey(HGHandle key) { link.setTarget(known, key); }
			public HGHandle getKey() { return link.targets()[known].get(); }
		};
		result.setHyperGraph(graph);
		return result;
	}
	
	@Test
	public void testTwoHopQuery()
	{
		// OWL properties are expressed as tuples [HGHandle subject, HGHandle property, HGHandle object]
		HGHandle nameBob = literal("Bob").getAtomHandle();
		HGHandle isNamed = dprop("isNamed").getAtomHandle();
		HGHandle hasFriend = oprop("hasFriend").getAtomHandle();
		HGHandle bob = ctx.graph.findOne(hg.apply(hg.targetAt(ctx.graph, 0), 
												  hg.orderedLink(hg.anyHandle(), isNamed, nameBob)));
//		System.out.println("bob=" + bob);
		List<HGHandle> bobsFriends = ctx.graph.findAll(
				hg.apply(hg.targetAt(ctx.graph, 0), 
						hg.orderedLink(hg.anyHandle(), hasFriend, bob)));	
//		System.out.println(bobsFriends);
		
		HGQuery<HGHandle> lookupName = HGQuery.make(ctx.graph, hg.apply(hg.targetAt(ctx.graph, 0), 
				  											hg.orderedLink(hg.anyHandle(), isNamed, nameBob)));
		HGQuery<HGHandle> query = new PipeQuery<HGHandle, HGHandle>(lookupName,
																    sibling(ctx.graph, 0, 2, hg.anyHandle(), hasFriend, hg.anyHandle()));
		query.setHyperGraph(ctx.graph);
		Assert.assertEquals(bobsFriends, query.findAll());
	}
}