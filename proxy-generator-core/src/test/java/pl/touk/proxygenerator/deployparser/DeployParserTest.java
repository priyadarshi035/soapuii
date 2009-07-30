/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.deployparser;

import java.io.File;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.w3c.dom.Document;

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
	public void testParseDeployXml() throws Exception {
		/*System.out.println("parseDeployXml");
		File fileToParse = null;
		String path = "";
		DeployParserImpl instance = new DeployParserImpl();
		File expResult = null;
		File result = instance.parseDeployXml(fileToParse, path);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");*/
	}

	/**
	 * Test of generateDOMTree method, of class DeployParser.
	 */
	public void testGenerateDOMTree() throws Exception {
		/*System.out.println("generateDOMTree");
		Document dom = null;
		Map<MultiKey, String> wsdlMap = null;
		DeployParserImpl instance = new DeployParserImpl();
		Document expResult = null;
		Document result = instance.generateDOMTree(dom, wsdlMap);
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");*/
	}

	/**
	 * Test of setLuri method, of class DeployParser.
	 */
	public void testSetLuri() throws ParserConfigurationException {
		/*System.out.println("setLuri");
		String luri = "";
		DeployParserImpl instance = new DeployParserImpl();
		instance.setLuri(luri);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");*/
	}

}
