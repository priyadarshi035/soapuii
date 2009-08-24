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
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapResponseAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlMockResponseStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.UISupport;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.touk.proxygeneratorapi.support.ExtFileFilter;
import pl.touk.proxygeneratorapi.support.IOSupport;
import pl.touk.proxygeneratorapi.support.SimpleXmlParser;
import pl.touk.soapuii.testgenerator.support.GetCommunicationCommons;

/**
 *
 * @author pnw & azl
 */
public class GetCommunicationParser
{

	private ArrayList<String> operationNamesList;
	private HashMap<String, Integer> sameOperationNameCounter;
	private static int projectTestCaseCounter = 0;

	private WsdlTestRequestStep lastParsedRequest = null;


	public GetCommunicationParser()
	{
		operationNamesList = new ArrayList<String>();
		sameOperationNameCounter = new HashMap<String, Integer>();
	}

	public void parseGetCommunications( WsdlTestSuite testSuite, File dir, String odeListenURI, String mockURI, Map<QName, WsdlInterface> bindingMap) throws TestGeneratorException
	{
		try
		{
			//WsdlTestSuite testSuite = project.addNewTestSuite(file.getName());
			//we are parsing string to URI and back to string, to check for any parsing errors, that might occured in future
			testSuite.setPropertyValue("odeListenURI", (new URI(odeListenURI)).toString() );
			testSuite.setPropertyValue("odeListenAuthority", (new URI(odeListenURI).getAuthority()));
			
			testSuite.setPropertyValue("mockURI", (new URI(mockURI)).toString() );
			//testSuite.setPropertyValue("mockPort", String.valueOf((new URI(mockURI)).getPort()));
			if (dir.isDirectory())
			{
				for( File singleFile : dir.listFiles(new ExtFileFilter(".xml")) )
					if (validGetCommunication(singleFile))
						createTestCase(testSuite, singleFile, bindingMap);
			}
			else
				createTestCase(testSuite, dir, bindingMap);
		}
		catch (URISyntaxException ex)
		{
			throw new TestGeneratorException(ex.toString(), ex);
		}
	}

	/*
	 *  parses single getCommunication.xml file to single TestCase. Particular TestSteps are exchanges operation, parsed from getCommunication file.
	 */
	protected void parseSingleGetCommunication(WsdlTestCase testCase, Document getComDoc, Map<QName, WsdlInterface> bindingMap) throws Exception
	{
		NodeList exchangeList = getExchangeList(getComDoc);
		CreateTestSteps(testCase, exchangeList, getComDoc, bindingMap);
	}

	protected void CreateTestSteps(WsdlTestCase testCase, NodeList exchangeList, Document getComDoc, Map<QName, WsdlInterface> bindingMap) throws Exception
	{
		int exchangeListLength = exchangeList.getLength();
		for (int i = 0; i < exchangeListLength; i++)
		{
			Node exchange = exchangeList.item(i);

//			NodeList operationList = getOperation(exchange);
//			NodeList serviceList = getService(exchange);
//			NodeList typeList = getType(exchange);

			Node operationNode = getOperation(exchange).item(0);
			Node serviceNode = getService(exchange).item(0);
			Node typeNode = getType(exchange).item(0);

			String operationName = operationNode.getTextContent();
			String roleType = typeNode.getTextContent();

//			NodeList bodyList = getBody(exchange, roleType, operationName);
			Node bodyContent = getBody(exchange, roleType, operationName).item(0);
//			IOSupport.printNode(bodyContent, System.err);

//			String body = bodyNode.toString();

//			int operationListLength = operationList.getLength();
//			int serviceListLength = serviceList.getLength();
//			int typeListLength = typeList.getLength();

			QName qName = GetCommunicationCommons.parseServiceNode(serviceNode);

			WsdlInterface iface = bindingMap.get(qName);

			WsdlOperation operation = iface.getOperationByName(operationName);
//			WsdlOperation operation = iface.getOperationByName(qName.getLocalPart());

			WsdlTestRequestStep request = null;
			WsdlMockResponseTestStep mock = null;

			String strContent = "";
			if (roleType.equals("M"))
			{
				String localOperationName = projectTestCaseCounter+"_"+operationName;
				request = createWsdlTestRequestStep(testCase, operation, localOperationName);
				strContent = request.getHttpRequest().getRequestContent();
//				wsdlTestRequestStep.getTestRequest().setRequestContent(body);
			}
			else
			{
				String localOperationName = projectTestCaseCounter+"_"+operationName;
				WsdlTestRequestStep tempRequest = createWsdlTestRequestStep(testCase, operation, localOperationName);
				String tempStrContent = tempRequest.getHttpRequest().getRequestContent();
				testCase.removeTestStep(tempRequest);
				operationNamesList.remove(operationNamesList.size()-1);
				
				mock = createWsdlMockResponse(testCase, operation, localOperationName);
				mock.getMockResponse().setResponseContent(tempStrContent);

				strContent = mock.getMockResponse().getResponseContent();
			}

//			System.err.println("STRING CONTENT");

			Document xmlContent = SimpleXmlParser.parse(new ByteArrayInputStream(strContent.getBytes()), false);

			//adding soap:header to requests
			if(roleType.equals("M"))
			{
//				<soapenv:Header xmlns:wsa="http://schemas.xmlsoap.org/ws/2004/08/addressing">
//					<wsa:To>http://playmobile.pl/process/mnpm/portIn/ExxHandlerUNF/default</wsa:To>
//					<wsa:Action>null/null/handleE13</wsa:Action>
//				</soapenv:Header>
				Element header = (Element) SimpleXmlParser.evaluate("/Envelope/Header", xmlContent, null).item(0);
				header.setAttribute("xmlns:wsa", "http://schemas.xmlsoap.org/ws/2004/08/addressing");

				Element wsaTo = xmlContent.createElement("wsa:To");
				wsaTo.appendChild( xmlContent.createTextNode(
						qName.getNamespaceURI() + "/" + qName.getLocalPart() + "/default"));

				Element wsaAction = xmlContent.createElement("wsa:Action");
				wsaAction.appendChild(xmlContent.createTextNode("null/null/" + operationName));

				header.appendChild(wsaTo);
				header.appendChild(wsaAction);
			}

//			IOSupport.printDoc(xmlContent, System.err);
//			if(roleType.equals("M"))
			String xPathDefaultBodyContent = "/Envelope/Body/*[1]";
//			else
//				xPathDefaultBodyContent = "/Envelope/Body/"+operationName+"Response";

			Node defaultBodyContent = SimpleXmlParser.evaluate(xPathDefaultBodyContent, xmlContent, null).item(0);
			Node bodyNode = SimpleXmlParser.evaluate("/Envelope/Body", xmlContent, null).item(0);
			if (defaultBodyContent == null)
			{
//				System.err.println("ops... ");
				continue;
			}

			String docString = null;
//			IOSupport.printNode(bodyNode, System.err);
			if(bodyContent != null)
			{
				Node temp = xmlContent.importNode(bodyContent, true);
				bodyNode.replaceChild(temp, defaultBodyContent);
				docString = IOSupport.printDoc(xmlContent);
			}
			else
				docString = " ";

			if(roleType.equals("M"))
			{
				request.getHttpRequest().setRequestContent(docString);
				//String endpoint = "${#TestSuite#odeListenUri}/";
				URI odeListnUri = new URI(testCase.getTestSuite().getPropertyValue("odeListenURI"));
				String endpoint = "http://${#TestSuite#odeListenAuthority}" + odeListnUri.getPath();
				request.getHttpRequest().setEndpoint(endpoint + qName.getLocalPart() + "/");
				lastParsedRequest = request;
			}
			else
			{
				mock.getMockResponse().setResponseContent(docString);				
				String tempMockURI = testCase.getTestSuite().getPropertyValue("mockURI");
				String mockURI = null;
				try
				{
					mockURI = new URI(tempMockURI).getPath();
				} catch (URISyntaxException ex)
				{
					throw new TestGeneratorException(ex.toString(), ex);
				}
				String path = mockURI;
				mock.setPath(path+qName.getLocalPart());
				//testSuite.setPropertyValue("mockPort", ((new URI(mockURI)).getPort()));
				int port = (new URI(testCase.getTestSuite().getPropertyValue("mockURI"))).getPort();
				mock.setPort(port);
				if (lastParsedRequest != null)
					mock.setStartStep(lastParsedRequest.getName());
			}

			//defaultBodyContent.setNodeValue("hmmmmm");
//			System.err.println("===============================<<");
//			IOSupport.printDoc(xmlContent, System.err);

		}
	}

	protected WsdlMockResponseTestStep createWsdlMockResponse(WsdlTestCase testCase, WsdlOperation operation, String testStepName) throws TestGeneratorException
	{		
		TestStepConfig config = WsdlMockResponseStepFactory.createConfig(operation, false);
		return (WsdlMockResponseTestStep) createWsdlTestStep(testCase, operation, testStepName, config);
	}

	protected WsdlTestRequestStep createWsdlTestRequestStep(WsdlTestCase testCase, WsdlOperation operation, String testStepName) throws TestGeneratorException
	{
		TestStepConfig config = WsdlTestRequestStepFactory.createConfig(operation, testStepName);
		return (WsdlTestRequestStep) createWsdlTestStep(testCase, operation, testStepName, config);
	}

	protected Assertable createWsdlTestStep(WsdlTestCase testCase, WsdlOperation operation, String testStepName, TestStepConfig config) throws TestGeneratorException
	{
		if (operationNamesList.contains(testStepName))
		{
			int numberOfSameOperation = sameOperationNameCounter.get(testStepName);
			testStepName += ("_"+numberOfSameOperation);
			sameOperationNameCounter.put(testStepName, Integer.valueOf(numberOfSameOperation++));
		}
		else
		{
			operationNamesList.add(testStepName);
			sameOperationNameCounter.put(testStepName, Integer.valueOf(1));
		}
		config.setName(testStepName);
	
		Assertable step = (Assertable) testCase.addTestStep(config);
		if (step == null)
			throw new TestGeneratorException("Failed to add TestStep: " + testStepName);
		
		step.addAssertion(SoapResponseAssertion.LABEL);//.setDisabled(true);
		step.addAssertion(SchemaComplianceAssertion.LABEL);//.setDisabled(true);
		//step.addAssertion("XPath Match");
//		step.addAssertion(XPathContainsAssertion.LABEL);
//		XPathContainsAssertion xText = (XPathContainsAssertion) step.getAssertionAt(2);
//		UISupport.showErrorMessage(xText.getName());
//		xText.setPath("/test/text()");
//		xText.setExpectedContent("hhmmm?");
		//xText.

		return step;
	}

	protected NodeList getExchangeList(Document comDoc) throws ParserConfigurationException, IOException, IOException, SAXException, XPathExpressionException
	{
		String xpathExpr = "/Envelope/Body/getCommunicationResponse/getCommunicationResponse/restoreInstance/exchange";
		NodeList exchangeList = null;
		exchangeList = SimpleXmlParser.evaluate(xpathExpr, comDoc, null);

		return exchangeList;
	}

	protected NodeList getBody(Node exchange, String roleType, String operationName) throws ParserConfigurationException, IOException, IOException, SAXException, XPathExpressionException
	{
		NodeList bodyList = null;
		String xpathBodyExpr = null;
		
		if (roleType.equals("M"))
//			xpathBodyExpr = "./in/message/parameters/"+operationName;
			xpathBodyExpr = "./in/message/parameters/*[1]";
		else
//			xpathBodyExpr = "./out/message/parameters/"+operationName+"Response";
			xpathBodyExpr = "./out/message/parameters/*[1]";

		bodyList = SimpleXmlParser.evaluate(xpathBodyExpr, exchange, null);
		return bodyList;
	}

	protected NodeList getType(Node exchange) throws ParserConfigurationException, IOException, IOException, SAXException, XPathExpressionException
	{
		String xpathTypeExpr = "./type";
		NodeList typeList = null;
		typeList = SimpleXmlParser.evaluate(xpathTypeExpr, exchange, null);
		return typeList;
	}

	protected NodeList getOperation(Node exchange) throws ParserConfigurationException, IOException, IOException, SAXException, XPathExpressionException
	{
		String xpathOperationExpr = "./operation";
		NodeList operationList = null;
		operationList = SimpleXmlParser.evaluate(xpathOperationExpr, exchange, null);

		return operationList;
	}

	protected NodeList getService(Node exchange) throws ParserConfigurationException, IOException, IOException, SAXException, XPathExpressionException
	{
		String xpathServiceExpr = "./service";
		NodeList serviceList = null;
		serviceList = SimpleXmlParser.evaluate(xpathServiceExpr, exchange, null);

		return serviceList;
	}

	protected void createTestCase(WsdlTestSuite suite, File singleFile, Map<QName, WsdlInterface> bindingMap)
	{
		String testCaseName = singleFile.getName().replaceAll("\\.xml$", "");
		WsdlTestCase testCase = suite.addNewTestCase(testCaseName);

		try
		{
			Document doc = SimpleXmlParser.parse(singleFile, false);
			projectTestCaseCounter++;
			parseSingleGetCommunication(testCase, doc, bindingMap);			
		} //Exception should be fine, at least if we dont want to handle some errors other way
		catch (Exception ex)
		{
			UISupport.showErrorMessage("Parsing [" + singleFile.getName() + "] failed: " + ex);
		}
		UISupport.select(testCase);
	}

	protected boolean validGetCommunication(File singleFile)
	{
		try
		{
			String testXpath = "/Envelope/Body/getCommunicationResponse/getCommunicationResponse";
			NodeList nodes = SimpleXmlParser.evaluate(testXpath, singleFile, null);
			return nodes.getLength() == 1;
		}
		catch(Exception ex)
		{
			return false;
		}
	}

}
