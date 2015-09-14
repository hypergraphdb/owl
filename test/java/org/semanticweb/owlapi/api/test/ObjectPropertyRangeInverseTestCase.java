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

import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.gc.GarbageCollectorStatistics;
import org.hypergraphdb.app.owl.test.versioning.distributed.DistributedTests;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Information Management Group<br>
 * Date: 02-Jul-2009
 */
public class ObjectPropertyRangeInverseTestCase extends AbstractAxiomsRoundTrippingTestCase {

    @Override
	protected Set<? extends OWLAxiom> createAxioms() {
        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
        axioms.add(getFactory().getOWLObjectPropertyRangeAxiom(
        		getOWLObjectProperty("p").getInverseProperty(), getOWLClass("A")));
        return axioms;
    }

    @Override
    public void testManchesterOWLSyntax() throws Exception {
        // Can't represent inverse object property frames in Manchester OWL Syntax
//        super.testManchesterOWLSyntax();
    }
    
    public static void main(String [] argv)
    {
    	ObjectPropertyRangeInverseTestCase test = new ObjectPropertyRangeInverseTestCase();
    	try
    	{
    		test.setUp();
    		test.testRDFXML();
			GarbageCollectorStatistics stats = new GarbageCollector(((HGDBOntologyManager)test.getManager())
						.getOntologyRepository()).runGarbageCollection();
    		test.tearDown();
    	}
    	catch (Throwable t)
    	{
    		t.printStackTrace();
    		System.exit(0);
    	}
//		JUnitCore junit = new JUnitCore();
//		Result result = junit.run(Request.method(ObjectPropertyRangeInverseTestCase.class, 
//					"testRDFXML"));
//		System.out.println("Failures " + result.getFailureCount());
//		if (result.getFailureCount() > 0)
//		{
//			for (Failure failure : result.getFailures())
//			{
//				failure.getException().printStackTrace();
//			}
//		}
    	
    }
}
