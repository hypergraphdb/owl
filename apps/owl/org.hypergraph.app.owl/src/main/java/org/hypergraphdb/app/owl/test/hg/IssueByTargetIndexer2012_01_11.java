package org.hypergraphdb.app.owl.test.hg;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.indexing.ByTargetIndexer;
import org.hypergraphdb.util.HGUtils;

/**
 * IssueByTargetIndexer2012_01_11. 
 * "Remove of an HGLink with ByTargetIndexer present may cause exception"
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 11, 2012
 * 
 */
public class IssueByTargetIndexer2012_01_11 {

	public static String hypergraphDir = "C:\\temp\\hgissuegraph";
	
	public static void main(String[] argv) {
		IssueByTargetIndexer2012_01_11 o = new IssueByTargetIndexer2012_01_11();
		o.reproduceError();
	}
	
	public void reproduceError() {
		HyperGraph graph = HGEnvironment.get(hypergraphDir);
		HGHandle a1 = graph.add("A1");
		HGHandle a2 = graph.add("A2");
		HGHandle l1 = graph.add(new HGPlainLink(a1, a2));
		ByTargetIndexer idx1 = new ByTargetIndexer(graph.getType(l1), 1);
		graph.getIndexManager().register(idx1);
		
		//Will Throw exception here:
		// Issue reported
		graph.remove(a2, true);
		//Should break here.
		//graph.remove(l1, true);
		
		HGUtils.dropHyperGraphInstance(hypergraphDir);
	}
	
}
