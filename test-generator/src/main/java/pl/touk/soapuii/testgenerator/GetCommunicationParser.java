/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import pl.touk.proxygeneratorapi.support.ExtFileFilter;

/**
 *
 * @author pnw
 */
public class GetCommunicationParser
{
	protected DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	protected DocumentBuilder builder;
	protected XPathFactory factory = XPathFactory.newInstance();
	
	public GetCommunicationParser() throws ParserConfigurationException
	{
		domFactory.setNamespaceAware(false);
		domFactory.setIgnoringComments(true);
		builder = domFactory.newDocumentBuilder();		
	}

	public void parseGetCommunications( WsdlProject project, File dir, String listenURI, String outputURI, Map bindingMap)
	{
		WsdlTestSuite suite = project.addNewTestSuite(dir.getName());

		suite.setPropertyValue("listenURI", listenURI);
		suite.setPropertyValue("outputURI", outputURI);

		for( File file : dir.listFiles(new ExtFileFilter(".xml")) )
		{
			WsdlTestCase testCase = suite.addNewTestCase(file.getName());

			Document doc;
			try
			{
				doc = builder.parse(new FileInputStream(file));
				parseSingleGetCommunication(testCase, doc);
			}
			//Exception should be fine, at least if we dont want to handle some errors other way
			catch (Exception ex)
			{
				UISupport.showErrorMessage("Parsing [" + file.getName() + "] failed: " + ex);
			}


			UISupport.select( testCase );
		}
	}

	protected void parseSingleGetCommunication(WsdlTestCase testCase, Document getComDoc)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
