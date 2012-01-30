package org.hypergraphdb.app.owl.test.hg;

import java.util.List;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.indexing.ByTargetIndexer;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.hypergraphdb.util.HGUtils;

/**
 * I2012_01_27_IssueNotRemovedFromGraph.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 27, 2012
 */
public class I2012_01_27_IssueNotRemovedFromGraph {
	public static String hypergraphDir = "C:\\temp\\hgissuenotremoved";
	
	public static void main(String[] argv) {
		I2012_01_27_IssueNotRemovedFromGraph o = new I2012_01_27_IssueNotRemovedFromGraph();
		o.reproduceError();
	}
	ChangeSet cs;
	HyperGraph graph;
	
	public void reproduceError() {
		HGUtils.dropHyperGraphInstance(hypergraphDir);
		graph = HGEnvironment.get(hypergraphDir);
		cs = new ChangeSet();
		HGHandle csH = graph.add(cs);
		System.out.println("Changeset created: " + cs + " Handle: " + csH);
		findChangesets();
		removeFromGraphInTransaction();		
		System.out.println("Nothing should be found after this line.");
		findChangesets();
		graph.close();
		try { Thread.sleep(1000); } catch (Exception e) {};
	}
	
	public void removeFromGraphInTransaction() {
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				HGHandle csH = graph.getHandle(cs);
				System.out.println("REMOVING " + cs + " Handle: " + csH);
				graph.remove(csH, true);
				return null;
			}}, HGTransactionConfig.READONLY);
	}
	
	public void findChangesets() {
		List<ChangeSet> csL = graph.getAll(hg.type(ChangeSet.class));
		for (ChangeSet s : csL) {
			System.out.println("Changeset found: " + s + " handle: " + graph.getHandle(s));
		}
	}

}
