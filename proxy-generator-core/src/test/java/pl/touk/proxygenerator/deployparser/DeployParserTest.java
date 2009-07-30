/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.deployparser;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
import java.io.FileOutputStream;
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
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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

	public Document createDoc() throws ParserConfigurationException
	{
		Document result = null;
		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
		result = domBuilder.newDocument();

		Element outDomRoot = result.createElement("beans");
		outDomRoot.setAttribute("xmlns:http", "http://www.apache.org/ode/schemas/dd/2007/03");
		outDomRoot.setAttribute("xmlns:pns", "http://ode/bpel/unit-test");
		outDomRoot.setAttribute("xmlns:wns", "http://ode/bpel/unit-test.wsdl");

		Comment provides = result.createComment("PROVIDES");
		Comment invoke = result.createComment("INVOKES");
		Comment hello = result.createComment("HelloWorld2");
		outDomRoot.appendChild(provides);
		outDomRoot.appendChild(result.createComment("HelloWorld2"));
				
		Element endpointElement = result.createElement("http:endpoint");
		endpointElement.setAttribute("endpoint", "default");
		endpointElement.setAttribute("role", "consumer");
		endpointElement.setAttribute("locationURI", "${pl.ode.bpel.unit-test.wsdl.HelloService}");
		endpointElement.setAttribute("service", "wns:HelloService");
		endpointElement.setAttribute("soap", "true");
		endpointElement.setAttribute("soapVersion", "1.1");
		endpointElement.setAttribute("wsdlResource","src/test/resources/bpel/HelloWorld2/HelloWorld2.wsdl");
		outDomRoot.appendChild(endpointElement);
		outDomRoot.appendChild(invoke);
		outDomRoot.appendChild(hello);
		result.appendChild(outDomRoot);
		
//		result.normalize();
		return result;
	}
	
	public void printDocToFile(Document docToPrint, String fileName)
	{
		File xbean_exp = new File(fileName);
		try
		{
			 OutputFormat format = new OutputFormat(docToPrint);
			  format.setIndenting(true);

			  XMLSerializer serializer = new XMLSerializer(new FileOutputStream(xbean_exp), format);
			  serializer.serialize(docToPrint);
		} catch(IOException ie) {
		   ie.printStackTrace();
		}
	}

	/**
	 * Test of parseDeployXml method, of class DeployParser.
	 */


	public void testParseDeployXml() throws DeployParserException, ParserConfigurationException, Exception {

		System.out.println("parseDeployXml");

		Document expDoc = createDoc();
		printDocToFile(expDoc, "xbean_utest_exp.xml");
		

		ArrayList<String> expProvidesList = new ArrayList<String>();
		ArrayList<String> expConsumerList = new ArrayList<String>();
		expProvidesList.add("${pl.ode.bpel.unit-test.wsdl.HelloService}");

		MultiKey expResult = new MultiKey(expDoc,expProvidesList, expConsumerList);

		DeployParserImpl instance = new DeployParserImpl();
		String sourcesPath = "src/test/resources/bpel/HelloWorld2/";
		String additionalProperitesFileName = "src/test/resources/bpel/HelloWorld2/";

		MultiKey result = instance.parseDeployXml(sourcesPath, additionalProperitesFileName);
		Document resDoc = (Document)result.getKey(0);
//		resDoc.normalize();
		
		printDocToFile(resDoc,"xbean_utest_got.xml");
		
		assertEquals(expResult.getKey(2) , result.getKey(2));
		assertEquals(expResult.getKey(1) , result.getKey(1));
		assertTrue(expDoc.isEqualNode((Document)result.getKey(0)));
//		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		//fail("The test case is a prototype.");
	}
}

