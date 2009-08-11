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
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pl.touk.proxygeneratorapi.support.ExtFileFilter;

/**
 *
 * @author pnw & azl
 */
public class GetCommunicationParser
{
	protected DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	protected DocumentBuilder builder;
	protected XPathFactory factory = XPathFactory.newInstance();
	protected ArrayList exchangeList = null;
	
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

	/*
	 *  parses single getCommunication.xml file to single TestCase. Particular TestSteps are exchanges operation, parsed from getCommunication file. 
	 */
	protected void parseSingleGetCommunication(WsdlTestCase testCase, Document getComDoc)
	{
		exchangeList = new ArrayList();
		Node inDomRoot = null;
		inDomRoot = getComDoc.getFirstChild();
		String inDomRootName = inDomRoot.getNodeName();

		XPath xpath = factory.newXPath();
		String xpathExpr = "/Envelope/Body/getCommunicationResponse/getCommunicationResponse/restoreInstance/exchange";
		NodeList exchangeOperationList = null;
		try
		{
			exchangeOperationList = (NodeList) xpath.evaluate(xpathExpr, getComDoc, XPathConstants.NODESET);
		} catch (XPathExpressionException ex)
		{
			throw new UnsupportedOperationException("Not yet implemented");
		}

		int length = exchangeOperationList.getLength();
		for( int i = 0; i < length; i++)
		{
			Node exchange = exchangeOperationList.item(i);

			XPath xpathOperation = factory.newXPath();
			String xpathOperationExpr = "/Envelope/Body/getCommunicationResponse/getCommunicationResponse/restoreInstance/exchange/operation";
			NodeList operationList = null;
			try
			{
				operationList = (NodeList) xpathOperation.evaluate(xpathOperationExpr, exchange, XPathConstants.NODESET);
			} catch (XPathExpressionException ex)
			{
				throw new UnsupportedOperationException("Not yet implemented");
			}

			int operationListLength = operationList.getLength();
			Node operation = operationList.item(i);
			String operationContent = operation.getTextContent();
//			System.out.println(operationContent);


//			System.out.println(exchangeOperationList.item(i).toString());
			exchangeList.add(exchangeOperationList.item(i));
		}


//		for (int i = 0; i < exchangeList.size(); i ++)
//		{
//			Node exchange = (Node) exchangeList.get(i);
//			XPath operationXpath = factory.newXPath();
//			String xpathOperationExpr = "/Envelope/Body/getCommunicationResponse/getCommunicationResponse/restoreInstance/exchange/operation";
//			NodeList operationList = null;
//			try
//			{
//				operationList = (NodeList) xpath.evaluate(xpathOperationExpr, getComDoc, XPathConstants.NODESET);
//				int operationListLength = operationList.getLength();
//				System.out.println(operationList.item(0).getNodeName());
//			} catch (XPathExpressionException ex)
//			{
//				throw new UnsupportedOperationException("Not yet implemented");
//			}
//		}

//		throw new UnsupportedOperationException("Not yet implemented");
	}

	protected void createTestCase(WsdlTestSuite suite, File singleFile)
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
