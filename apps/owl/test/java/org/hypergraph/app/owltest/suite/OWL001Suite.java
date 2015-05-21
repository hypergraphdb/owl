package org.hypergraph.app.owltest.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.semanticweb.owlapi.api.test.OntologyMutationTestCase;

/**
 * OWL001Suite is an initial testrun of OWL-API HG with an OWL-API JUnit testcase.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 21, 2011
 */ 
@RunWith(Suite.class)

@Suite.SuiteClasses({
	OntologyMutationTestCase.class
})
public class OWL001Suite {

}
