package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.*;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.IMPORT;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.coode.owlapi.owlxml.renderer.OWLXMLObjectRenderer;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.hypergraphdb.app.owl.versioning.Branch;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.ChangeLink;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor;
import org.hypergraphdb.app.owl.versioning.VersionedMetadata;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VAddAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VAddImportChange;
import org.hypergraphdb.app.owl.versioning.change.VAddOntologyAnnotationChange;
import org.hypergraphdb.app.owl.versioning.change.VAddPrefixChange;
import org.hypergraphdb.app.owl.versioning.change.VAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.Change;
import org.hypergraphdb.app.owl.versioning.change.VImportChange;
import org.hypergraphdb.app.owl.versioning.change.VModifyOntologyIDChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOntologyAnnotationChange;
import org.hypergraphdb.app.owl.versioning.change.VPrefixChange;
import org.hypergraphdb.app.owl.versioning.change.VRemoveAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VRemoveImportChange;
import org.hypergraphdb.app.owl.versioning.change.VRemoveOntologyAnnotationChange;
import org.hypergraphdb.app.owl.versioning.change.VRemovePrefixChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.vocab.Namespaces;

/**
 * VOWLXMLObjectRenderer - the main class responsible for serializing a 
 * {@link org.hypergraphdb.app.owl.versioning.VersionedOntology} as an XML
 * document.
 * 
 * <p>
 * Most object are serialized in a trivial way, by simply writing out
 * their properties as tag attributes. The less obvious part is how the
 * graph is serialized. There are two graphs intertwined: the revision graph
 * and the change set graph. Both are DAGs and in both cases the graphs basically
 * grow indefinitely by adding more and more descendants over time. So the connections
 * that matter from the vantage point of a node are the connections to the parents.
 * So for both revisions and change sets, we serialize the {@link ChangeLink}s. 
 * The change sets are not explicitly defined as part of the serialization. They
 * are implicitly pulled because of the revisions. So, given a revision R, we care
 * about all the changes that need to be made to its parent revisions so as to reach
 * the state "as of" that revision. So any time we are sending revisions R1 and R2 
 * with R1 being a parent of R2, we send all change sets and change records between them. 
 * All root revisions are thus presumed to be already known at destination.
 * </p>
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County), Borislav Iordanov
 * @created Feb 24, 2012
 */
public class VOWLXMLObjectRenderer implements VOWLObjectVisitor
{
	private VOWLXMLWriter writer;
	private OWLXMLObjectRenderer owlObjectRenderer;
	private VOWLXMLRenderConfiguration configuration;
	
	boolean isAddChange(VOWLChange c)
	{
		return c instanceof VAddAxiomChange || 
			   c instanceof VAddImportChange || 
			   c instanceof VAddOntologyAnnotationChange || 
			   c instanceof VAddPrefixChange;
	}

	boolean isRemoveChange(VOWLChange c)
	{
		return c instanceof VRemoveAxiomChange || 
			   c instanceof VRemoveImportChange || 
			   c instanceof VRemoveOntologyAnnotationChange || 
			   c instanceof VRemovePrefixChange;
	}
	
	public VOWLXMLObjectRenderer(VOWLXMLWriter writer, VOWLXMLRenderConfiguration configuration)
	{
		this.writer = writer;
		this.configuration = configuration;
		owlObjectRenderer = new OWLXMLObjectRenderer(writer);
	}

	public void visit(VersionedOntology vo, Set<HGHandle> revisions)
	{
		HyperGraph graph = vo.ontology().getHyperGraph();
		
		writer.writeStartElement(VERSIONED_ONTOLOGY);
		writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "ontologyID", vo.getOntology().toString());
		writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "versionedID", vo.getAtomHandle().toString());
		
		HashSet<ChangeLink> changeLinks = new HashSet<ChangeLink>();
		HashSet<HGHandle> visited = new HashSet<HGHandle>();		
		for (HGHandle revisionHandle : revisions)
		{
			Revision revision = graph.get(revisionHandle);
			visit(revision);
			List<ChangeLink> links = hg.getAll(graph, hg.and(hg.type(ChangeLink.class), 
														     hg.incident(revisionHandle)));
			for (ChangeLink changeLink : links)
			{
				if (changeLinks.contains(changeLink))
					continue;
				visit(changeLink);
				changeLinks.add(changeLink);
			}
		}
		
		
		// Now that we have the revision graph itself serialized, collect
		// the change sets between revisions. 
		for (ChangeLink changeLink : changeLinks)
		{
			// If this is a link between a root revision (i.e. either the very first revision of the
			// ontology or already known at destination) and its parent, or between a head revision
			// and its child, no change sets are needed. A head with a child seems like a contradiction
			// but it is possible to manually designate some revisions as the last one to serialize, or
			// if there was a new head created after the current revisions set was established
			if (!revisions.contains(changeLink.child()))
				continue;
			if (!revisions.contains(changeLink.parent()) && configuration.roots().contains(changeLink.child()))
				continue;			
			ChangeSet<VersionedOntology> changeSet = graph.get(changeLink.change());
			visit(changeSet);
			visited.add(changeSet.getAtomHandle());
		}
		
		if (configuration.revisionSnapshot() != null)
		{
			OWLOntologyEx ontologyData = vo.getCurrentRevision().equals(configuration.revisionSnapshot()) ?
					vo.ontology() : vo.getRevisionData(configuration.revisionSnapshot());
			// Render Ontology Data
			writer.startOntologyData(ontologyData);
			ontologyData.accept(owlObjectRenderer);
			writer.endOntologyData();
			// Need to render Ontology contained Format Prefixes here.
			if (!ontologyData.getPrefixes().isEmpty())
			{
				writer.writeStartElement(VOWLXMLVocabulary.V_PREFIX_MAP);
				Map<String, String> prefixMap = ontologyData.getPrefixes();
				for (Map.Entry<String, String> prefix : prefixMap.entrySet())
				{
					writer.writeStartElement(VOWLXMLVocabulary.V_PREFIX_MAP_ENTRY);
					writer.writeAttribute("prefixName", prefix.getKey());
					writer.writeAttribute("namespace", prefix.getValue());
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
		}
		// VersionedOntology
		writer.writeEndElement();
	}

	public void visit(ChangeLink parentLink)
	{
		writer.writeStartElement(MARK_PARENT);
		writer.writeAttribute("parent", parentLink.parent());
		writer.writeAttribute("child", parentLink.child());
		writer.writeAttribute("change", parentLink.change());
		writer.writeAttribute("handle", parentLink.getAtomHandle());
		writer.writeEndElement();
	}
	
	public void visit(Branch branch)
	{			
		writer.writeStartElement(BRANCH)
			  .writeAttribute("name", branch.getName())
			  .writeAttribute("createdOn", Long.toString(branch.getCreatedOn()))
			  .writeAttribute("createdBy", branch.getCreatedBy())
			  .writeAttribute("versioned", branch.versioned())
			  .writeAttribute("handle", branch.getAtomHandle())
			  .writeEndElement();				
	}
	
	public void visit(Revision revision)
	{
		writer.writeStartElement(REVISION)
			  .writeAttribute("ontology", revision.versioned())
			  .writeAttribute("handle", revision.getAtomHandle())
			  .writeAttribute("user", revision.user())
			  .writeAttribute("branch", revision.branchHandle())
			  .writeAttribute("timestamp", "" + revision.timestamp())
			  .writeAttribute("comment", revision.comment());
		writer.writeEndElement();
	}

	public void visit(ChangeSet<VersionedOntology> changeSet)
	{
		writer.writeStartElement(CHANGE_SET);
		writer.writeAttribute("timestamp", Long.toString(changeSet.timestamp()));
		writer.writeAttribute("handle", changeSet.getAtomHandle());
		for (Change<VersionedOntology> c : changeSet.changes())
			((VOWLChange)c).accept(this);
		writer.writeEndElement();
	}

	public void visit(VAxiomChange change)
	{
		if (isAddChange(change))
		{
			writer.writeStartElement(V_ADD_AXIOM_CHANGE);
		}
		else if (isRemoveChange(change))
		{
			writer.writeStartElement(V_REMOVE_AXIOM_CHANGE);
		}
		else
		{
			throw new IllegalArgumentException("Implementation error: Change neither add nor remove" + change);
		}
		change.getAxiom().accept(owlObjectRenderer);

		writer.writeEndElement();

	}
	
	public void visit(VImportChange change)
	{
		if (isAddChange(change))
		{
			writer.writeStartElement(V_ADD_IMPORT_CHANGE);
		}
		else if (isRemoveChange(change))
		{
			writer.writeStartElement(V_REMOVE_IMPORT_CHANGE);
		}
		else
		{
			throw new IllegalArgumentException("Implementation error: Change neither add nor remove" + change);
		}
		writer.writeStartElement(IMPORT);
		writer.writeTextContent(change.getImportDeclaration().getURI().toString());
		writer.writeEndElement();
		writer.writeEndElement();

	}

	public void visit(VOntologyAnnotationChange change)
	{
		if (isAddChange(change))
		{
			writer.writeStartElement(V_ADD_ONTOLOGY_ANNOTATION_CHANGE);
		}
		else if (isRemoveChange(change))
		{
			writer.writeStartElement(V_REMOVE_ONTOLOGY_ANNOTATION_CHANGE);
		}
		else
		{
			throw new IllegalArgumentException("Implementation error: Change neither add nor remove" + change);
		}
		change.getOntologyAnnotation().accept(owlObjectRenderer);
		writer.writeEndElement();
	}
	
	public void visit(VPrefixChange change)
	{
		if (isAddChange(change))
		{
			writer.writeStartElement(V_ADD_PREFIX_CHANGE);
		}
		else if (isRemoveChange(change))
		{
			writer.writeStartElement(V_REMOVE_PREFIX_CHANGE);
		}
		else
		{
			throw new IllegalArgumentException("Prefixchange neither add nor remove: " + change);
		}
		// Write NEW
		writer.writeAttribute(Namespaces.OWL + "prefixName", change.getPrefixName());
		writer.writeAttribute(Namespaces.OWL + "prefix", change.getPrefix());
		writer.writeEndElement();
	}

	public void visit(VModifyOntologyIDChange change)
	{
		if (VOWLChange.isModifyChange(change))
		{
			writer.writeStartElement(V_MODIFY_ONTOLOGY_ID_CHANGE);
		}
		else
		{
			throw new IllegalArgumentException("Implementation error: Change must be modification but was not" + change);
		}
		OWLOntologyID newOntologyID = change.getNewOntologyID();
		OWLOntologyID oldOntologyID = change.getOldOntologyID();
		// Write NEW
		writer.writeStartElement(V_MODIFY_ONTOLOGY_ID_NEW_ID);
		writer.writeAttribute(Namespaces.OWL + "ontologyIRI", newOntologyID.getOntologyIRI().toString());
		if (newOntologyID.getVersionIRI() != null)
		{
			writer.writeAttribute(Namespaces.OWL + "versionIRI", newOntologyID.getVersionIRI().toString());
		}
		writer.writeEndElement();
		// Write OLD
		writer.writeStartElement(V_MODIFY_ONTOLOGY_ID_OLD_ID);
		writer.writeAttribute(Namespaces.OWL + "ontologyIRI", oldOntologyID.getOntologyIRI().toString());
		if (newOntologyID.getVersionIRI() != null)
		{
			writer.writeAttribute(Namespaces.OWL + "versionIRI", oldOntologyID.getVersionIRI().toString());
		}
		writer.writeEndElement();
		// End change
		writer.writeEndElement();
	}
	
	public void visit(VOWLXMLRenderConfiguration configuration)
	{
		writer.writeStartElement(VOWLXMLVocabulary.RENDER_CONFIGURATION);
		writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "maxDepth", "" + configuration.maxDepth());
		if (configuration.firstRevision() != null)
			writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "firstRevision", "" + configuration.firstRevision());
		if (configuration.bottomRevision() != null)
			writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "bottomRevision", "" + configuration.bottomRevision());			
		if (configuration.revisionSnapshot() != null)
			writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "revisionSnapshot", "" + configuration.revisionSnapshot());
		writer.writeStartElement(VOWLXMLVocabulary.ROOTS);
		for (HGHandle root : configuration.roots())
		{
			writer.writeStartElement(VOWLXMLVocabulary.HGHANDLE);
			writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "value", root.getPersistent().toString());
			writer.writeEndElement();
		}
		writer.writeEndElement(); // Roots
		writer.writeStartElement(VOWLXMLVocabulary.HEADS);
		for (HGHandle head : configuration.heads())
		{
			writer.writeStartElement(VOWLXMLVocabulary.HGHANDLE);
			writer.writeAttribute(VOWLXMLVocabulary.NAMESPACE + "value", head.getPersistent().toString());
			writer.writeEndElement();
		}
		writer.writeEndElement(); // Heads
		writer.writeEndElement();
	}

	public void visit(VersionedMetadata<VersionedOntology> metadata)
	{
		writer.writeStartElement(VOWLXMLVocabulary.METADATA);
		for (Branch branch : metadata.allBranches())
		{
			visit(branch);
		}
		writer.writeEndElement();
	}
}