package org.hypergraph.app.owltest.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.semanticweb.owlapi.api.test.SWRLRuleTestCase;

/**
 * OWL002Suite contains testcases with errors after 
 * 
 * Run: 2011.11.03 13h10.
 * 
 * For current status on errors see latest testlog in /testlog/ directory. 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 21, 2011
 */ 
@RunWith(Suite.class)

@Suite.SuiteClasses({
	//RenameToExistingOntologyTestCase.class,
	//DifferentPhysicalURISameOntologyIRITestCase.class,
	//SWRLRuleTestCase.class,	
	//LargeDifferentIndividualsTestCase.class //Takes > 1000 secs.
	SWRLRuleTestCase.class
})
public class OWL002Suite {

}