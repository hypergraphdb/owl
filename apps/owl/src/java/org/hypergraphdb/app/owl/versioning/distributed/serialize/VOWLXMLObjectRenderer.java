package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.IMPORT;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.VERSIONED_ONTOLOGY;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.REVISION;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.CHANGE_SET;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_ADD_AXIOM_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_ADD_IMPORT_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_ADD_ONTOLOGY_ANNOTATION_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_MODIFY_ONTOLOGY_ID_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_MODIFY_ONTOLOGY_ID_NEW_ID;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_MODIFY_ONTOLOGY_ID_OLD_ID;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_REMOVE_AXIOM_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_REMOVE_IMPORT_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_REMOVE_ONTOLOGY_ANNOTATION_CHANGE;

import java.util.List;

import org.coode.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor;
import org.hypergraphdb.app.owl.versioning.change.VAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VImportChange;
import org.hypergraphdb.app.owl.versioning.change.VModifyOntologyIDChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOntologyAnnotationChange;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.vocab.Namespaces;

/**
 * VOWLXMLObjectRenderer.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public class VOWLXMLObjectRenderer implements VOWLObjectVisitor {
	    private VOWLXMLWriter writer;
	    private OWLXMLObjectRenderer owlObjectRenderer;
	    private VOWLRenderConfiguration configuration;
	    
	    public VOWLXMLObjectRenderer(VOWLXMLWriter writer, VOWLRenderConfiguration configuration) {
	        this.writer = writer;
	        this.configuration = configuration;
	        owlObjectRenderer = new OWLXMLObjectRenderer(writer);
	    }

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.VersionedOntology)
		 */
		@Override
		public void visit(VersionedOntology vo) {
			List<Revision> revisions = vo.getRevisions();
            writer.writeStartElement(VERSIONED_ONTOLOGY);
            writer.writeAttribute(VOWLVocabulary.NAMESPACE + "ontologyID", vo.getHeadRevision().getOntologyID().toString());
            writer.writeAttribute(VOWLVocabulary.NAMESPACE + "headRevisionIndex", Integer.toString((revisions.size() - 1)));
            //writer.writeTextContent(decl.getURI().toString());
			List<ChangeSet> changeSets = vo.getChangeSets();

			int firstRevision = configuration.getFirstRevisionIndex();
			int lastRevision = Math.min(revisions.size() - 1, configuration.getLastRevisionIndex());
			int headChangeSetIndex = Math.max(0, revisions.size() - 1);
			for (int i = firstRevision; i <= lastRevision; i++) {
				revisions.get(i).accept(this);
				if (i < headChangeSetIndex || configuration.isUncommittedChanges()) {
					changeSets.get(i).accept(this);
				} else {
					//do not include last uncommitted changes changeset.
				}
			}
			//Data
			if (configuration.isLastRevisionData()) {
				OWLOntology ontologyData; 
				ontologyData = vo.getRevisionData(lastRevision, configuration.isUncommittedChanges());
				//Render Ontology Data
				writer.startOntologyData(ontologyData);
				ontologyData.accept(owlObjectRenderer);
				writer.endOntologyData();
			}
			//VersionedOntology
            writer.writeEndElement();
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.Revision)
		 */
		@Override
		public void visit(Revision revision) {
			writer.writeStartElement(REVISION);
			//writer.writeStartElement("ontologyID");
			writer.writeAttribute("ontologyID", revision.getOntologyID().toString());
			writer.writeAttribute("revision", Integer.toString(revision.getRevision()));
			writer.writeAttribute("user", revision.getUser());
			writer.writeAttribute("timeStamp", revision.getTimeStamp().toString());
			writer.writeAttribute("revisionComment", revision.getRevisionComment());
			writer.writeEndElement();
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.ChangeSet)
		 */
		@Override
		public void visit(ChangeSet changeSet) {
			writer.writeStartElement(CHANGE_SET);
			writer.writeAttribute("createdDate", changeSet.getCreatedDate().toString());
			List <VOWLChange> changes = changeSet.getChanges();
			for (VOWLChange c : changes) {
				c.accept(this);
			}
			writer.writeEndElement();
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.change.VAxiomChange)
		 */
		@Override
		public void visit(VAxiomChange change) {
			if (VOWLChange.isAddChange(change)) {
				writer.writeStartElement(V_ADD_AXIOM_CHANGE);
			} else if (VOWLChange.isRemoveChange(change)){
				writer.writeStartElement(V_REMOVE_AXIOM_CHANGE);
			} else {
				throw new IllegalArgumentException("Implementation error: Change neither add nor remove" + change);
			}
			change.getAxiom().accept(owlObjectRenderer);
			
			writer.writeEndElement();
			
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.change.VImportChange)
		 */
		@Override
		public void visit(VImportChange change) {
			if (VOWLChange.isAddChange(change)) {
				writer.writeStartElement(V_ADD_IMPORT_CHANGE);
			} else if (VOWLChange.isRemoveChange(change)){
				writer.writeStartElement(V_REMOVE_IMPORT_CHANGE);
			} else {
				throw new IllegalArgumentException("Implementation error: Change neither add nor remove" + change);
			}
            writer.writeStartElement(IMPORT);
            writer.writeTextContent(change.getImportDeclaration().getURI().toString());
            writer.writeEndElement();
			writer.writeEndElement();

		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.change.VOntologyAnnotationChange)
		 */
		@Override
		public void visit(VOntologyAnnotationChange change) {
			if (VOWLChange.isAddChange(change)) {
				writer.writeStartElement(V_ADD_ONTOLOGY_ANNOTATION_CHANGE);
			} else if (VOWLChange.isRemoveChange(change)){
				writer.writeStartElement(V_REMOVE_ONTOLOGY_ANNOTATION_CHANGE);
			} else {
				throw new IllegalArgumentException("Implementation error: Change neither add nor remove" + change);
			}
			change.getOntologyAnnotation().accept(owlObjectRenderer);
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.change.VModifyOntologyIDChange)
		 */
		@Override
		public void visit(VModifyOntologyIDChange change) {
			if (VOWLChange.isModifyChange(change)) {
				writer.writeStartElement(V_MODIFY_ONTOLOGY_ID_CHANGE);
			} else {
				throw new IllegalArgumentException("Implementation error: Change must be modification but was not" + change);
			}
			OWLOntologyID newOntologyID = change.getNewOntologyID(); 
			OWLOntologyID oldOntologyID = change.getOldOntologyID(); 
			//Write NEW
			writer.writeStartElement(V_MODIFY_ONTOLOGY_ID_NEW_ID);
			writer.writeAttribute(Namespaces.OWL + "ontologyIRI", newOntologyID.getOntologyIRI().toString());
			if (newOntologyID.getVersionIRI() != null) {
				writer.writeAttribute(Namespaces.OWL + "versionIRI", newOntologyID.getVersionIRI().toString());
			}
			writer.writeEndElement();
			//Write OLD
			writer.writeStartElement(V_MODIFY_ONTOLOGY_ID_OLD_ID);
			writer.writeAttribute(Namespaces.OWL + "ontologyIRI", oldOntologyID.getOntologyIRI().toString());
			if (newOntologyID.getVersionIRI() != null) {
				writer.writeAttribute(Namespaces.OWL + "versionIRI", oldOntologyID.getVersionIRI().toString());
			}
			writer.writeEndElement();
			// End change
			writer.writeEndElement();
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLRenderConfiguration)
		 */
		@Override
		public void visit(VOWLRenderConfiguration configuration) {
			writer.writeStartElement(VOWLVocabulary.RENDER_CONFIGURATION);
			writer.writeAttribute(VOWLVocabulary.NAMESPACE + "firstRevisionIndex", "" + configuration.getFirstRevisionIndex());
			writer.writeAttribute(VOWLVocabulary.NAMESPACE + "lastRevisionIndex", "" + configuration.getLastRevisionIndex());
			writer.writeAttribute(VOWLVocabulary.NAMESPACE + "lastRevisionData", "" + configuration.isLastRevisionData());
			writer.writeAttribute(VOWLVocabulary.NAMESPACE + "unCommittedChanges", "" + configuration.isUncommittedChanges());
			writer.writeEndElement();
		}	   
}