package de.fuberlin.wiwiss.d2rq.map;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the map package
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 * @version $Id: AllTests.java,v 1.10 2006/09/11 23:22:27 cyganiak Exp $
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for de.fuberlin.wiwiss.d2rq.map");
		//$JUnit-BEGIN$
		suite.addTestSuite(TranslationTableTest.class);
		suite.addTestSuite(MappingTest.class);
		//$JUnit-END$
		return suite;
	}

}