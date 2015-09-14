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

import java.util.Collections;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyRenameException;
import org.semanticweb.owlapi.model.SetOntologyID;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 22-Dec-2009
 */
public class RenameToExistingOntologyTestCase extends AbstractOWLAPITestCase {

    public void testRenameToExistingOntology() throws Exception {
        try {
            OWLOntologyManager manager = getManager();
            IRI ontologyAIRI = IRI.create("http://www.semanticweb.org/ontologies/ontologyA");
            OWLOntology ontologyA = manager.createOntology(ontologyAIRI);
            getManager().addAxiom(ontologyA, manager.getOWLDataFactory().getOWLDeclarationAxiom(getOWLClass("A")));
            IRI ontologyBIRI = IRI.create("http://www.semanticweb.org/ontologies/ontologyB");
            OWLOntology ontologyB = manager.createOntology(ontologyBIRI);
            manager.applyChange(new SetOntologyID(ontologyB, new OWLOntologyID(ontologyAIRI)));
            fail();
        }
        //2011.11.03 catch (OWLOntologyRenameException e) {
        catch (RuntimeException e) {
            System.out.println("Got expected rename exception?: " + e.getMessage());
            System.out.println("CAUSE " + e.getCause());
        }
    }

}
