package org.hypergraphdb.app.owl.repair;

import java.util.Iterator;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLClassAtomHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLDataRangeAtomHGDB;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.type.DefaultJavaTypeMapper;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.type.HGCompositeType;
import org.semanticweb.owlapi.model.IRI;

/**
 * R20120807RepairSWRLNull.
 * 
 * Repairs a repository that keeps causing the following problem:
 * 
 * java.lang.NullPointerException
        at org.semanticweb.owlapi.util.HashCode.visit(HashCode.java:605)
        at org.hypergraphdb.app.owl.model.swrl.SWRLClassAtomHGDB.accept(SWRLClassAtomHGDB.java:40)
        at org.semanticweb.owlapi.util.HashCode.hashCode(HashCode.java:146)
        at org.hypergraphdb.app.owl.core.OWLObjectHGDB.hashCode(OWLObjectHGDB.java:153)
        at java.util.HashMap.put(HashMap.java:372)
        at java.util.HashSet.add(HashSet.java:200)
        at org.hypergraphdb.app.owl.model.swrl.SWRLRuleHGDB.getBody(SWRLRuleHGDB.java:190)
        at org.semanticweb.owlapi.util.SimpleRenderer.visit(SimpleRenderer.java:946)
        at org.hypergraphdb.app.owl.model.swrl.SWRLRuleHGDB.accept(SWRLRuleHGDB.java:164)
        at org.semanticweb.owlapi.util.SimpleRenderer.render(SimpleRenderer.java:247)
        at org.semanticweb.owlapi.io.ToStringRenderer.getRendering(ToStringRenderer.java:89)
        at org.hypergraphdb.app.owl.core.OWLObjectHGDB.toString(OWLObjectHGDB.java:193)
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Aug 7, 2012
 */
public class R20120807RepairSWRLNull {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HGDBOntologyRepository.setHypergraphDBLocation(args[0]);
		System.out.println("Opening Repository at " + args[0]);
		VHGDBOntologyRepository vrep = VHGDBOntologyRepository.getInstance();
		HyperGraph graph = vrep.getHyperGraph();
		System.out.println("Number of SWRLClassAtomHGDB: " + vrep.getNrOfAtomsByType(SWRLClassAtomHGDB.class));
		System.out.println("Number of SWRLDataRangeAtomHGDB: " + vrep.getNrOfAtomsByType(SWRLDataRangeAtomHGDB.class));
		System.out.println("Replacing old bean based type with new record type.");
		replaceTheTypeWithTypeMapper(graph);
		List<SWRLClassAtomHGDB> l = graph.getAll(hg.type(SWRLClassAtomHGDB.class));
		int i = 0;		
		for (SWRLClassAtomHGDB atom : l) {
			System.out.println("Atom Number: " + i);
			System.out.println("  Type: " + atom.getAtomType());
			System.out.println("  Argument: " + atom.getArgument());
			System.out.println("  Predicate: " + atom.getPredicate());
			System.out.println("  Datafactory: " + atom.getOWLDataFactory());
			OWLDataFactoryHGDB df = (OWLDataFactoryHGDB)atom.getOWLDataFactory();
			df.setHyperGraph(graph);
			if (atom.getPredicate() == null) {
				System.err.println("  THE PREDICATE WAS NULL, REPAIRING CURRENT ATOM: " + i);
				atom.setPredicateDirect(df.getOWLClass(IRI.create("DUMMY_TEST_CLASS")));
				System.out.println("After repair: " + atom);
				graph.update(atom);
			}
			HGCompositeType hgtype = (HGCompositeType)graph.getTypeSystem().getAtomType(atom);
			Iterator<String> dimensionsIt = hgtype.getDimensionNames();
			while (dimensionsIt.hasNext()) {
				String dimName = dimensionsIt.next();
				System.out.println("   Dimension: " + dimName);
				System.out.println("   Projection: " + hgtype.getProjection(dimName));
			}
			i++;
		}
		System.out.println("Closing Repository.");
		vrep.dispose();
	}
	
//	public static void replaceTheType(HyperGraph graph) {
//		RecordType newType = new RecordType();
//		HGHandle argumentType = graph.getTypeSystem().getTypeHandle(SWRLIArgument.class);
//		HGHandle predicateDirectType = graph.getTypeSystem().getTypeHandle(SWRLPredicate.class);
//		//HGHandle argumentType = graph.getTypeSystem().getTypeHandle(Object.class);
//		//HGHandle predicateDirectType = graph.getTypeSystem().getTypeHandle(Object.class);
//		if (argumentType == null) throw new IllegalArgumentException("argumentType null");
//		if (predicateDirectType == null) throw new IllegalArgumentException("predicateDirectType null");
//		SlotType st = new SlotType();
//		st.setHyperGraph(graph);
//		Slot slot1 = new Slot("argument", argumentType); //SWRLIArgument
//		Slot slot2 = new Slot("predicateDirect", predicateDirectType); //SWRLPredicate
//		newType.setHyperGraph(graph);
//		HGHandle slot1H; 
//		HGHandle slot2H; 
//		if (st.find(slot1).hasNext()) {
//			slot1H = st.find(slot1).next();
//		} else {
//			slot1H = graph.add(slot1);
//		}
//		if (st.find(slot2).hasNext()) {
//			slot2H = st.find(slot2).next();
//		} else {
//			slot2H = graph.add(slot2);
//		}
//		newType.addSlot(slot1H);
//		newType.addSlot(slot2H);
//		//graph.add(newType);
//		HGHandle newTypeHandle = graph.add(newType);
//		// Fetch old type
//		HGHandle typeHandle = graph.getTypeSystem().getTypeHandle(SWRLClassAtomHGDB.class);
//		HGHandle typeTypeHandle = graph.getTypeSystem().getTypeHandle(Top.class);
//		JavaBeanBinding jbb = new JavaBeanBinding(typeHandle, newType, SWRLClassAtomHGDB.class);
//		jbb.setHyperGraph(graph);
//		//HGHandle jbbHandle = graph.add(jbb, typeHandle);
//		graph.replace(typeHandle, jbb, typeTypeHandle);
//	}

	public static void replaceTheTypeWithTypeMapper(HyperGraph graph) {
		HGHandle originalTypeHandle = graph.getTypeSystem().getTypeHandle(SWRLClassAtomHGDB.class);
		HGAtomType originalType = graph.get(originalTypeHandle);
		HGAtomType originalTypeType = graph.getTypeSystem().getAtomType(originalType);
		HGHandle originalTypeTypeHandle = graph.getHandle(originalTypeType);
		//This is a RecordTypeConstructor
		System.out.println("Original type's type: " + originalTypeType);
		DefaultJavaTypeMapper tm = new DefaultJavaTypeMapper();
		tm.setHyperGraph(graph);
		HGAtomType newType = tm.defineHGType(SWRLClassAtomHGDB.class, originalTypeHandle);
		HGHandle newTypeHandle = graph.add(newType);
		HGAtomType javaBinding = tm.getJavaBinding(newTypeHandle, newType, SWRLClassAtomHGDB.class);
		javaBinding.setHyperGraph(graph);
		graph.replace(originalTypeHandle, javaBinding, originalTypeTypeHandle);
	}
}
