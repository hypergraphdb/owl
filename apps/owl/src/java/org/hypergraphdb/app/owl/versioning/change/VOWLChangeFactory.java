package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.ImportChange;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

/**
 * VOWLChangeFactory creates storable change objects from OWL-API Change objects and vice versa.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class VOWLChangeFactory {

	/**
	 * Creates a storable VOWLChange that represents the given OWLOntologyChange.
	 * @param ooc
	 * @param graph
	 * @return
	 */
	public static VOWLChange create(OWLOntologyChange ooc, HyperGraph graph) {
		if (ooc instanceof AddImport) {
			ImportChange ic = (ImportChange)ooc;
			HGHandle handle = graph.getHandle(ic.getImportDeclaration());
			if (handle == null) throw new IllegalStateException("Could not get handle for ImportDeclaration of change : " + ooc + ". Cannot create VOWLChange." );
			return new VAddImportChange(handle);
		} else if (ooc instanceof RemoveImport) {
			ImportChange ic = (ImportChange)ooc;
			HGHandle handle = graph.getHandle(ic.getImportDeclaration());
			if (handle == null) throw new IllegalStateException("Could not get handle for ImportDeclaration of change : " + ooc + ". Cannot create VOWLChange." );
			return new VRemoveImportChange(handle);
		} else if (ooc instanceof AddAxiom) {
			AddAxiom aac = (AddAxiom)ooc;
			HGHandle handle = graph.getHandle(aac.getAxiom());
			if (handle == null) throw new IllegalStateException("Could not get handle for Axiom of change : " + ooc + ". Cannot create VOWLChange." );
			return new VAddAxiomChange(handle);
		} else if (ooc instanceof RemoveAxiom) {
			RemoveAxiom rac = (RemoveAxiom)ooc;
			HGHandle handle = graph.getHandle(rac.getAxiom());
			if (handle == null) throw new IllegalStateException("Could not get handle for Axiom of change : " + ooc + ". Cannot create VOWLChange." );
			return new VRemoveAxiomChange(handle);
		} else if (ooc instanceof AddOntologyAnnotation) {
			AddOntologyAnnotation aoac = (AddOntologyAnnotation)ooc;
			HGHandle handle = graph.getHandle(aoac.getAnnotation());			
			if (handle == null) throw new IllegalStateException("Could not get handle for Annotation of change : " + ooc + ". Cannot create VOWLChange." );
			return new VAddOntologyAnnotationChange(handle);
		} else if (ooc instanceof RemoveOntologyAnnotation) {
			RemoveOntologyAnnotation roac = (RemoveOntologyAnnotation)ooc;
			HGHandle handle = graph.getHandle(roac.getAnnotation());
			if (handle == null) throw new IllegalStateException("Could not get handle for Annotation of change : " + ooc + ". Cannot create VOWLChange." );
			return new VRemoveOntologyAnnotationChange(handle);
		} else if (ooc instanceof SetOntologyID) {
			SetOntologyID soic = (SetOntologyID)ooc;
			HGHandle oldId = graph.getHandle(soic.getOriginalOntologyID());
			HGHandle newId = graph.getHandle(soic.getNewOntologyID());
			if (oldId == null) throw new IllegalStateException("Could not get handle for oldID of change : " + ooc + ". Cannot create VOWLChange." );
			if (newId == null) throw new IllegalStateException("Could not get handle for newID of change : " + ooc + ". Cannot create VOWLChange." );
			// old is first param
			return new VModifyOntologyIDChange(oldId, newId);
		} else {
			throw new IllegalArgumentException("OWLOntologyChangeType unknown: " + ooc.getClass());
		}
	}
	
	/**
	 * Creates an OWL-API change that represents the given VOWLChange.
	 * @param voc
	 * @param onto
	 * @param graph
	 * @return
	 */
	public static OWLOntologyChange create(VOWLChange voc, OWLOntology onto, HyperGraph graph) {
		if (voc instanceof VAddImportChange) {
			VImportChange ic = (VImportChange)voc;
			return new AddImport(onto, (OWLImportsDeclaration)graph.get(ic.getImportDeclarationHandle()));
		} else if (voc instanceof VRemoveImportChange) {
			VImportChange ic = (VImportChange)voc;
			return new RemoveImport(onto, (OWLImportsDeclaration)graph.get(ic.getImportDeclarationHandle()));
		} else if (voc instanceof VAddAxiomChange) {
			VAxiomChange ac = (VAxiomChange)voc;
			return new AddAxiom(onto, (OWLAxiom)graph.get(ac.getAxiomHandle()));
		} else if (voc instanceof VRemoveAxiomChange) {
			VAxiomChange ac = (VAxiomChange)voc;
			return new RemoveAxiom(onto, (OWLAxiom)graph.get(ac.getAxiomHandle()));
		} else if (voc instanceof VAddOntologyAnnotationChange) {
			VOntologyAnnotationChange aoac = (VOntologyAnnotationChange)voc;
			return new AddOntologyAnnotation(onto, (OWLAnnotation)graph.get(aoac.getOntologyAnnotationHandle()));
		} else if (voc instanceof VRemoveOntologyAnnotationChange) {
			VOntologyAnnotationChange aoac = (VOntologyAnnotationChange)voc;
			return new RemoveOntologyAnnotation(onto, (OWLAnnotation)graph.get(aoac.getOntologyAnnotationHandle()));
		} else if (voc instanceof VModifyOntologyIDChange) {
			VModifyOntologyIDChange soic = (VModifyOntologyIDChange)voc;
			//HGHandle oldId = graph.get(soic.getOldOntologyID());
			OWLOntologyID newId = graph.get(soic.getNewOntologyIDHandle());
			// old is first param
			return new SetOntologyID(onto, newId);
		} else {
			throw new IllegalArgumentException("VOWLOntologyChangeType unknown: " + voc.getClass());
		}
	}
	
	/**
	 * Creates an OWL-API change that represents the reversal of the given VOWLChange.
	 * @param voc
	 * @param onto
	 * @param graph
	 * @return
	 */
	public static OWLOntologyChange createInverse(VOWLChange voc, OWLOntology onto, HyperGraph graph) {
		if (voc instanceof VAddImportChange) {
			VImportChange ic = (VImportChange)voc;
			return new RemoveImport(onto, (OWLImportsDeclaration)graph.get(ic.getImportDeclarationHandle()));
		} else if (voc instanceof VRemoveImportChange) {
			VImportChange ic = (VImportChange)voc;
			return new AddImport(onto, (OWLImportsDeclaration)graph.get(ic.getImportDeclarationHandle()));
		} else if (voc instanceof VAddAxiomChange) {
			VAxiomChange ac = (VAxiomChange)voc;
			return new RemoveAxiom(onto, (OWLAxiom)graph.get(ac.getAxiomHandle()));
		} else if (voc instanceof VRemoveAxiomChange) {
			VAxiomChange ac = (VAxiomChange)voc;
			return new AddAxiom(onto, (OWLAxiom)graph.get(ac.getAxiomHandle()));
		} else if (voc instanceof VAddOntologyAnnotationChange) {
			VOntologyAnnotationChange aoac = (VOntologyAnnotationChange)voc;
			return new RemoveOntologyAnnotation(onto, (OWLAnnotation)graph.get(aoac.getOntologyAnnotationHandle()));
		} else if (voc instanceof VRemoveOntologyAnnotationChange) {
			VOntologyAnnotationChange aoac = (VOntologyAnnotationChange)voc;
			return new AddOntologyAnnotation(onto, (OWLAnnotation)graph.get(aoac.getOntologyAnnotationHandle()));
		} else if (voc instanceof VModifyOntologyIDChange) {
			VModifyOntologyIDChange soic = (VModifyOntologyIDChange)voc;
			OWLOntologyID oldId = graph.get(soic.getOldOntologyIDHandle());
			//OWLOntologyID newId = graph.get(soic.getNewOntologyID());
			// old is first param
			return new SetOntologyID(onto, oldId);
		} else {
			throw new IllegalArgumentException("VOWLOntologyChangeType unknown: " + voc.getClass());
		}
	}
}
