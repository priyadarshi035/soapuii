/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.deployparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pl.touk.proxygenerator.wsdlmap.WsdlMapFactory;
import pl.touk.proxygenerator.wsdlmap.WsdlMapFactoryImpl;

/**
 *
 * @author azl
 */
public class DeployParserTest extends TestCase {

	private Logger logger;
    
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
		

		Document expDoc = null;
		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
		expDoc = domBuilder.newDocument();
//
//		Element outDomRoot = expDoc.createElement("beans");
		
//		dom.getDocumentElement().normalize();
//
//		DeployParserImpl temp = new DeployParserImpl();
//
//		WsdlMapFactoryImpl wsdlMapFactoryImpl = new WsdlMapFactoryImpl();
//		Map<MultiKey, String> wsdlMap = wsdlMapFactoryImpl.createWsdlMap("src/test/resources/bpel/HelloWorld2/");
//		Document expDoc = temp.generateDOMTree(dom, wsdlMap);


		ArrayList<String> expProvidesList = new ArrayList<String>();
		ArrayList<String> expConsumerList = new ArrayList<String>();
		expProvidesList.add("${pl.playmobile.process.mnpm.portIn.HelloService}");

		MultiKey expResult = new MultiKey(expDoc,expProvidesList, expConsumerList);

		DeployParserImpl instance = new DeployParserImpl();
		String sourcesPath = "src/test/resources/bpel/HelloWorld2/";
		String additionalProperitesFileName = "src/test/resources/bpel/HelloWorld2/";

		MultiKey result = instance.parseDeployXml(sourcesPath, additionalProperitesFileName);
		System.out.println(expResult.toString());
		System.out.println(result.toString());
		assertEquals(expResult.getKey(2) , result.getKey(2));
		assertEquals(expResult.getKey(1) , result.getKey(1));
		assertEquals(expResult.getKey(0) , result.getKey(0));
//		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		//fail("The test case is a prototype.");
	}
}

