package org.hypergraphdb.app.owl.test.hg;

import java.util.List;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.hypergraphdb.util.HGUtils;

/**
 * IssueDoubleAdd2012_01_29. DOUBLE ADD PROBLEM tested with different types.
 * 
 * 1) Adds the same java object twice (graph.add(o) and removes it (graph.remove(graph.getHandle(o).
 * 2) Tests if object deleted by hg.type(o.getClass)
 * 3) It is expected the no objects are found after removal, 
 * 3b) however, as of 2012.01.30 a double add leads to 2 different persistent handles, 
 *     therefore one persisted object remains after removal. This is a bug.  
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 30, 2012
 */
public class I2012_01_29_IssueDoubleAdd {

	public static String hypergraphDir = "C:\\temp\\hgissuedoubleadd";
	
	public static void main(String[] argv) {
		I2012_01_29_IssueDoubleAdd o = new I2012_01_29_IssueDoubleAdd();
		//
		// Start tests with test objects here:
		//
		o.reproduceError(new ChangeSet());
		o.reproduceError(new Integer(5));
		o.reproduceError("Test");
	}
	Object object;
	HyperGraph graph;
	
	public void reproduceError(Object o) {
		graph = HGEnvironment.get(hypergraphDir);
		object = o;
		System.out.println("-------------------- TEST STARTED ------------------------------");
		System.out.println("Adding Object: " + object + " twice. Type: " + object.getClass());
		HGHandle objectH1 = graph.add(object);
		HGHandle objectH2 = graph.add(object);
		System.out.println(" Handle1: " + objectH1 + " Persistent: " + objectH1.getPersistent());
		System.out.println(" Handle2: " + objectH2 + " Persistent: " + objectH2.getPersistent());
		System.out.println(" Query by type " + object.getClass() );
		findChangesets();
		removeFromGraphInTransaction();		
		System.out.println("Nothing should be found after this line.");
		int objectsAfterRemove = findChangesets();
		if (objectsAfterRemove != 0) {
			System.out.println("TEST FAILED: WE SHOULD HAVE NO OBJECT AFTER REMOVE");
		} else {
			System.out.println("TEST PASSED: As expected, no object found after remove");
		}
		graph.close();
		HGUtils.dropHyperGraphInstance(hypergraphDir);
	}
	
	public void removeFromGraphInTransaction() {
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				System.out.println(" REMOVING : using graph.getHandle(javaObject)");
				HGHandle objectH = graph.getHandle(object);
				System.out.println(" REMOVE Handle: " +  objectH + " Persistent: " + objectH.getPersistent());
				graph.remove(objectH, true);
				return null;
			}}, HGTransactionConfig.DEFAULT);
	}
	
	public int findChangesets() {
		List<Object> objectL = graph.getAll(hg.type(object.getClass()));
		for (Object o : objectL) {
			System.out.println("Object found: " + o + " handle: (graph.getHandle(foundO)) " + graph.getHandle(o));
		}
		return objectL.size();
	}
}


