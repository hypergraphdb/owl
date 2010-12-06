package org.hypergraphdb.app.owl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

public class ImplUtils
{
	public static Set<OWLAnnotationAssertionAxiom> getAnnotationAxioms(
			OWLEntity entity, Set<OWLOntology> ontologies)
	{
		Set<OWLAnnotationAssertionAxiom> result = new HashSet<OWLAnnotationAssertionAxiom>();
		for (OWLOntology ont : ontologies)
		{
			result.addAll(ont.getAnnotationAssertionAxioms(entity.getIRI()));
		}
		return result;
	}

	public static Set<OWLAnnotation> getAnnotations(OWLEntity entity,
			Set<OWLOntology> ontologies)
	{
		Set<OWLAnnotation> result = new HashSet<OWLAnnotation>();
		for (OWLAnnotationAssertionAxiom ax : getAnnotationAxioms(entity,
				ontologies))
		{
			result.add(ax.getAnnotation());
		}
		return result;
	}

	public static Set<OWLAnnotation> getAnnotations(OWLEntity entity,
			OWLAnnotationProperty annotationProperty,
			Set<OWLOntology> ontologies)
	{
		Set<OWLAnnotation> result = new HashSet<OWLAnnotation>();
		for (OWLAnnotationAssertionAxiom ax : getAnnotationAxioms(entity,
				ontologies))
		{
			if (ax.getAnnotation().getProperty().equals(annotationProperty))
			{
				result.add(ax.getAnnotation());
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static int compareSets(Set<? extends OWLObject> set1,
			Set<? extends OWLObject> set2)
	{
		SortedSet<? extends OWLObject> ss1;
		if (set1 instanceof SortedSet)
		{
			ss1 = (SortedSet<? extends OWLObject>) set1;
		} else
		{
			ss1 = new TreeSet<OWLObject>(set1);
		}
		SortedSet<? extends OWLObject> ss2;
		if (set2 instanceof SortedSet)
		{
			ss2 = (SortedSet<? extends OWLObject>) set2;
		} else
		{
			ss2 = new TreeSet<OWLObject>(set2);
		}
		int i = 0;
		Iterator<? extends OWLObject> thisIt = ss1.iterator();
		Iterator<? extends OWLObject> otherIt = ss2.iterator();
		while (i < ss1.size() && i < ss2.size())
		{
			OWLObject o1 = thisIt.next();
			OWLObject o2 = otherIt.next();
			int diff = o1.compareTo(o2);
			if (diff != 0)
			{
				return diff;
			}
			i++;
		}
		return ss1.size() - ss2.size();
	}

}