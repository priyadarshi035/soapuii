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

	public void parseGetCommunications( WsdlProject project, File file, String listenURI, String outputURI, Map bindingMap)
	{
		WsdlTestSuite suite = project.addNewTestSuite(file.getName());

		suite.setPropertyValue("listenURI", listenURI);
		suite.setPropertyValue("outputURI", outputURI);

		if (file.isDirectory())
			for( File singleFile : file.listFiles(new ExtFileFilter(".xml")) )
				createTestCase(suite, singleFile);
		else
			createTestCase(suite, file);
	}

	protected void parseSingleGetCommunication(WsdlTestCase testCase, Document getComDoc)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void createTestCase(WsdlTestSuite suite, File singleFile)
	{
		WsdlTestCase testCase = suite.addNewTestCase(singleFile.getName());

		Document doc;
		try
		{
			doc = builder.parse(new FileInputStream(singleFile));
			parseSingleGetCommunication(testCase, doc);
		} //Exception should be fine, at least if we dont want to handle some errors other way
		catch (Exception ex)
		{
			UISupport.showErrorMessage("Parsing [" + singleFile.getName() + "] failed: " + ex);
		}

		UISupport.select(testCase);
	}
}
