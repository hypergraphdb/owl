package org.hypergraphdb.app.owl.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * A000AllOntologyManagerTests.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 21, 2011
 */ 
@RunWith(Suite.class)
@Suite.SuiteClasses({
	  T001Classes.class,
	  T002ClassExpressionTest.class,
	  T003PropertiesTest.class
})

public class A000AllOntologyManagerTests {
	//intentionally empty.
}