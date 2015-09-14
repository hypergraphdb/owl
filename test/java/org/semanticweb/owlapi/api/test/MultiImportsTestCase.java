/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.semanticweb.owlapi.api.test;

import java.net.URISyntaxException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 01-Jul-2010
 */
public class MultiImportsTestCase extends AbstractOWLAPITestCase
{
	OWLOntologyIRIMapper mapper(final String relativePath)
	{
		return new OWLOntologyIRIMapper()
		{
			public IRI getDocumentIRI(IRI ontologyIRI)
			{
				try
				{
					String[] parts = ontologyIRI.toURI().getRawPath().split("/");
					return IRI.create(this.getClass().getResource(relativePath + "/" + parts[parts.length - 1]));
				}
				catch (URISyntaxException e)
				{
					throw new RuntimeException(e);
				}
			}
		};
	}

	public void testImports() throws Exception
	{
		try
		{
			OWLOntologyManager manager = getManager();// OWLManager.createOWLOntologyManager();
			final Class<?> clazz = this.getClass();
			manager.addIRIMapper(mapper("/imports"));
			manager.loadOntologyFromOntologyDocument(clazz.getResourceAsStream("/imports/D.owl"));
		}
		catch (OWLOntologyCreationException e)
		{
			// Thread.dumpStack();
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testCyclicImports() throws Exception
	{
		try
		{
			OWLOntologyManager manager = getManager();// OWLManager.createOWLOntologyManager();
			manager.addIRIMapper(mapper("/importscyclic"));
			// OWLOntology o =
			manager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream("/importscyclic/D.owl"));
		}
		catch (OWLOntologyCreationException e)
		{
			// Thread.dumpStack();
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testCyclicImports2() throws Exception
	{
		try
		{
			OWLOntologyManager manager = getManager();
			manager.addIRIMapper(mapper("/importscyclic"));
			manager.loadOntologyFromOntologyDocument(
					IRI.create(this.getClass().getResource("/importscyclic/D.owl")));
		}
		catch (OWLOntologyCreationException e)
		{
			// Thread.dumpStack();
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
