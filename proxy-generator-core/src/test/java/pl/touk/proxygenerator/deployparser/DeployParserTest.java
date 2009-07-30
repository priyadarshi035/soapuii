/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.deployparser;

import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.apache.commons.collections.keyvalue.MultiKey;

/**
 *
 * @author azl
 */
public class DeployParserTest extends TestCase {
    
    public DeployParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

	/**
	 * Test of parseDeployXml method, of class DeployParser.
	 */
	public void testParseDeployXml() throws DeployParserException, ParserConfigurationException, Exception {
		System.out.println("parseDeployXml");
		
		//path insert by the user of the test
		String sourcesPath = "src/test/resources/bpel/przykladowy_proces/";

		DeployParserImpl instance = new DeployParserImpl();
		MultiKey expResult = null;
		MultiKey result = instance.parseDeployXml(sourcesPath);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}
}

