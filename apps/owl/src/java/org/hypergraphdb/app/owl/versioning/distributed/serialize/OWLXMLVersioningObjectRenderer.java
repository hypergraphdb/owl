package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.IMPORT;

import java.util.List;

import org.coode.owlapi.owlxml.renderer.OWLXMLWriter;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.VersioningObject;
import org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor;
import org.hypergraphdb.app.owl.versioning.change.VAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VImportChange;
import org.hypergraphdb.app.owl.versioning.change.VModifyOntologyIDChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOntologyAnnotationChange;

/**
 * OWLXMLVersioningObjectRenderer.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public class OWLXMLVersioningObjectRenderer implements VersioningObjectVisitor {
	    private OWLXMLWriter writer;

	    
	    
	    public OWLXMLVersioningObjectRenderer(OWLXMLWriter writer) {
	        this.writer = writer;
	    }

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.VersionedOntology)
		 */
		@Override
		public void visit(VersionedOntology vo) {
			List<Revision> revisions = vo.getRevisions();
            writer.writeStartElement(IMPORT);
            //writer.writeTextContent(decl.getURI().toString());
            writer.writeEndElement();

			for (Revision r : revisions) {
				r.accept(this);
			}
			List<ChangeSet> changeSets = vo.getChangeSets();
			for (ChangeSet cs : changeSets) {
				cs.accept(this);
			}
			
			
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.Revision)
		 */
		@Override
		public void visit(Revision revision) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.ChangeSet)
		 */
		@Override
		public void visit(ChangeSet changeSet) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.change.VOWLChange)
		 */
		@Override
		public void visit(VOWLChange change) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.change.VAxiomChange)
		 */
		@Override
		public void visit(VAxiomChange change) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.change.VImportChange)
		 */
		@Override
		public void visit(VImportChange change) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.change.VOntologyAnnotationChange)
		 */
		@Override
		public void visit(VOntologyAnnotationChange change) {
			// TODO Auto-generated method stub
			
		}

		/* (non-Javadoc)
		 * @see org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor#visit(org.hypergraphdb.app.owl.versioning.change.VModifyOntologyIDChange)
		 */
		@Override
		public void visit(VModifyOntologyIDChange change) {
			// TODO Auto-generated method stub
			
		}
	    
	    

}
