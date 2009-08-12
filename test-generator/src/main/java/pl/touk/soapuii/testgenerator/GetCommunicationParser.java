/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.support.UISupport;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.touk.proxygeneratorapi.support.ExtFileFilter;
import pl.touk.proxygeneratorapi.support.SimpleXmlParser;

/**
 *
 * @author pnw & azl
 */
public class GetCommunicationParser
{
	public GetCommunicationParser() throws ParserConfigurationException
	{
	}

	public void parseGetCommunications( WsdlTestSuite testSuite, File file, String listenURI, String outputURI, Map<QName, WsdlInterface> bindingMap)
	{
		//WsdlTestSuite testSuite = project.addNewTestSuite(file.getName());
		testSuite.setPropertyValue("listenURI", listenURI);
		testSuite.setPropertyValue("outputURI", outputURI);

		if (file.isDirectory())
			for( File singleFile : file.listFiles(new ExtFileFilter(".xml")) )
				createTestCase(testSuite, singleFile, bindingMap);
		else
			createTestCase(testSuite, file, bindingMap);
	}

	/*
	 *  parses single getCommunication.xml file to single TestCase. Particular TestSteps are exchanges operation, parsed from getCommunication file. 
	 */
	protected void parseSingleGetCommunication(WsdlTestCase testCase, Document getComDoc, Map<QName, WsdlInterface> bindingMap) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException
	{
		NodeList exchangeList = getExchangeList(getComDoc);
		CreateTestSteps(testCase, exchangeList, getComDoc, bindingMap);
	}

	protected void CreateTestSteps(WsdlTestCase testCase, NodeList exchangeList, Document getComDoc, Map<QName, WsdlInterface> bindingMap) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException
	{
		int exchangeListLength = exchangeList.getLength();
		for (int i = 0; i < exchangeListLength; i++)
		{
			Node exchange = exchangeList.item(i);
			NodeList operationList = getOperationList(getComDoc);
			NodeList  serviceList = getService(getComDoc);

			int operationListLength = operationList.getLength();
			int serviceListLength =serviceList.getLength();

//			for(int j = 0; j < operationListLength; j++)
//			{
//				Node operation = operationList.item(j);
//				Node service = operationList.item(j);
//
//				String operationName = operation.getNodeValue();
//				String serviceName = service.getNodeValue();
//
//				QName qN = GetCommunicationCommons.parseServiceNode(service);
//
//				WsdlInterface iface = bindingMap.get(qN);
//				WsdlOperation operation = iface.getOperationByName("CaseRunner");
//				createWsdlTestRequestStep(testCase, operation, "testStepName");
//			}

		}
	}

	protected void createWsdlTestRequestStep(WsdlTestCase testCase, WsdlOperation operation, String testStepName)
	{
		//WsdlInterface iface = (WsdlInterface) testSuite.getProject().getInterfaceByName("CaseRunner");
		//WsdlOperation operation = iface.getOperationByName("createCase");
		TestStepConfig config = WsdlTestRequestStepFactory.createConfig(operation, testStepName);
		testCase.addTestStep(config);
	}

	protected NodeList getExchangeList(Document comDoc) throws ParserConfigurationException, IOException, IOException, SAXException, XPathExpressionException
	{
		String xpathExpr = "/Envelope/Body/getCommunicationResponse/getCommunicationResponse/restoreInstance/exchange";
		NodeList exchangeList = null;
		exchangeList = SimpleXmlParser.evaluate(xpathExpr, comDoc, null);

		return exchangeList;
	}

	protected NodeList getOperationList(Document exchange) throws ParserConfigurationException, IOException, IOException, SAXException, XPathExpressionException
	{
		String xpathOperationExpr = "//exchange/operation/text()";
		NodeList operationList = null;		
		operationList = SimpleXmlParser.evaluate(xpathOperationExpr, exchange, null);

		return operationList;
	}

	protected NodeList getService(Document comDoc) throws ParserConfigurationException, IOException, IOException, SAXException, XPathExpressionException
	{
		String xpathServiceExpr = "//exchange/service/text()";
		NodeList serviceList = null;
		serviceList = SimpleXmlParser.evaluate(xpathServiceExpr, comDoc, null);

		return serviceList;
	}

	protected void createTestCase(WsdlTestSuite suite, File singleFile, Map<QName, WsdlInterface> bindingMap)
	{
		WsdlTestCase testCase = suite.addNewTestCase(singleFile.getName());

		try
		{
			Document doc = SimpleXmlParser.parse(singleFile, false);
			parseSingleGetCommunication(testCase, doc, bindingMap);
		} //Exception should be fine, at least if we dont want to handle some errors other way
		catch (Exception ex)
		{
			UISupport.showErrorMessage("Parsing [" + singleFile.getName() + "] failed: " + ex);
		}
		UISupport.select(testCase);
	}

}
