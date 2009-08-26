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
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapResponseAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlMockResponseStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.support.UISupport;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pl.touk.proxygeneratorapi.support.ExtFileFilter;
import pl.touk.proxygeneratorapi.support.IOSupport;
import pl.touk.proxygeneratorapi.support.SimpleXmlParser;
import pl.touk.soapuii.testgenerator.data.*;
import pl.touk.soapuii.testgenerator.support.GetCommunicationCommons;

/**
 *
 * @author pnw & azl
 */
public class GetCommunicationParser
{
//	private ArrayList<String> operationNamesList;
	private HashMap<WsdlTestCase, ArrayList<String>> testCaseOperataionNamesList;
//	private HashMap<String, Integer> sameOperationNameCounter;
	private HashMap<WsdlTestCase, HashMap<String, Integer>> testCaseSameOperationNameCounter;

	private static Logger log = Logger.getLogger(GetCommunicationParser.class);
	private WsdlTestRequestStep lastParsedRequest = null;


	public GetCommunicationParser()
	{
//		operationNamesList = new ArrayList<String>();
		testCaseOperataionNamesList = new HashMap<WsdlTestCase, ArrayList<String>>();

		testCaseSameOperationNameCounter = new HashMap<WsdlTestCase, HashMap<String, Integer>>();
	}

	public GCResult parseGetCommunications(
			WsdlTestSuite testSuite, File dir, String odeListenURI, String mockURI, Map<QName, WsdlInterface> bindingMap)
			throws TestGeneratorException
	{
		GCResult result = new GCResult(testSuite);
		try
		{
//			int numberOfTestCases = testSuite.getTestCaseCount();
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
						result.addTestCase( createTestCase(testSuite, singleFile, bindingMap) );
			}
			else
				result.addTestCase( createTestCase(testSuite, dir, bindingMap) );
		}
		catch (URISyntaxException ex)
		{
			throw new TestGeneratorException(ex.toString(), ex);
		}
		return result;
	}

	protected GCTestCase createTestCase(WsdlTestSuite suite, File singleFile, Map<QName, WsdlInterface> bindingMap)
	{
		String testCaseName = singleFile.getName().replaceAll("\\.xml$", "");
		WsdlTestCase testCase = suite.addNewTestCase(testCaseName);
		GCTestCase gcTestCase = new GCTestCase(testCase);

		try
		{
			Document doc = SimpleXmlParser.parse(singleFile, false);
			gcTestCase.addTestSteps( parseSingleGetCommunication(testCase, doc, bindingMap) );
		} //Exception should be fine, at least if we dont want to handle some errors other way
		catch (Exception ex)
		{
			UISupport.showErrorMessage("Parsing [" + singleFile.getName() + "] failed: " + ex);
		}
		UISupport.select(testCase);
		return gcTestCase;
	}

	/*
	 *  parses single getCommunication.xml file to single TestCase. Particular TestSteps are exchanges operation, parsed from getCommunication file.
	 */
	protected List<GCTestStep> parseSingleGetCommunication(
			WsdlTestCase testCase, Document getComDoc, Map<QName, WsdlInterface> bindingMap) throws Exception
	{
		NodeList exchangeList = getExchangeList(getComDoc);
		return CreateTestSteps(testCase, exchangeList, getComDoc, bindingMap);
	}

	protected List<GCTestStep> CreateTestSteps(
			WsdlTestCase testCase, NodeList exchangeList, Document getComDoc, Map<QName, WsdlInterface> bindingMap) throws Exception
	{
		List<GCTestStep> gcTestSteps = new ArrayList<GCTestStep>();
		int exchangeListLength = exchangeList.getLength();
		for (int i = 0; i < exchangeListLength; i++)
		{
			Node exchange = exchangeList.item(i);

			Node operationNode = getOperation(exchange).item(0);
			Node serviceNode = getService(exchange).item(0);
			Node typeNode = getType(exchange).item(0);

			String operationName = operationNode.getTextContent();
			String roleType = typeNode.getTextContent();

			Node bodyContent = getBody(exchange, roleType, operationName).item(0);


			//if soapui sends request, suspectedBodyContent is suspected response for this request
			//if soapui creates mock response, suspectedBodyContent is suspected incomming request
			Node suspectedBodyContent = roleType.equals("M") ?
					getBody(exchange, "P", operationName).item(0) :
					getBody(exchange, "M", operationName).item(0);

//			IOSupport.printNode(bodyContent, System.err);

//			String body = bodyNode.toString();

//			int operationListLength = operationList.getLength();
//			int serviceListLength = serviceList.getLength();
//			int typeListLength = typeList.getLength();

			QName qName = GetCommunicationCommons.parseServiceNode(serviceNode);

			WsdlInterface iface = bindingMap.get(qName);

			WsdlOperation operation = iface.getOperationByName(operationName);

			WsdlTestRequestStep request = null;
			WsdlMockResponseTestStep mock = null;

			String strContent = "";
			if (roleType.equals("M"))
			{
				String localOperationName = operationName;
				request = createWsdlTestRequestStep(testCase, operation, localOperationName);
				strContent = request.getHttpRequest().getRequestContent();

				GCTestStep gcTestStep = new GCTestStep(request);
				gcTestStep.addAssertions( addAssertions(request, suspectedBodyContent) );
//				wsdlTestRequestStep.getTestRequest().setRequestContent(body);
			}
			else
			{
				String localOperationName = operationName;
				WsdlTestRequestStep tempRequest = createWsdlTestRequestStep(testCase, operation, localOperationName);
				String tempStrContent = tempRequest.getHttpRequest().getRequestContent();
				testCase.removeTestStep(tempRequest);
				ArrayList<String> thisCaseOperationNamesList = testCaseOperataionNamesList.get(testCase);
				thisCaseOperationNamesList.remove(thisCaseOperationNamesList.size()-1);
				
				mock = createWsdlMockResponse(testCase, operation, localOperationName);
				mock.getMockResponse().setResponseContent(tempStrContent);

				strContent = mock.getMockResponse().getResponseContent();

				GCTestStep gcTestStep = new GCTestStep(mock);
				gcTestStep.addAssertions( addAssertions(mock, suspectedBodyContent) );
			}

			Document xmlContent = SimpleXmlParser.parse(new ByteArrayInputStream(strContent.getBytes()), false);

			//adding soap:header to requests
			if(roleType.equals("M"))
			{
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

			String xPathDefaultBodyContent = "/Envelope/Body/*[1]";

			Node defaultBodyContent = SimpleXmlParser.evaluate(xPathDefaultBodyContent, xmlContent, null).item(0);
			Node bodyNode = SimpleXmlParser.evaluate("/Envelope/Body", xmlContent, null).item(0);

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

		}
		return gcTestSteps;
	}

	protected WsdlMockResponseTestStep createWsdlMockResponse(
			WsdlTestCase testCase, WsdlOperation operation, String testStepName) throws TestGeneratorException
	{		
		TestStepConfig config = WsdlMockResponseStepFactory.createConfig(operation, false);
		return (WsdlMockResponseTestStep) createWsdlTestStep(testCase, operation, testStepName, config);
	}

	protected WsdlTestRequestStep createWsdlTestRequestStep(
			WsdlTestCase testCase, WsdlOperation operation, String testStepName) throws TestGeneratorException
	{
		TestStepConfig config = WsdlTestRequestStepFactory.createConfig(operation, testStepName);
		return (WsdlTestRequestStep) createWsdlTestStep(testCase, operation, testStepName, config);
	}

	protected Assertable createWsdlTestStep(
			WsdlTestCase testCase, WsdlOperation operation, String testStepName, TestStepConfig config) throws TestGeneratorException
	{
//		int id_case = testCase.getTestSuite().getTestCaseIndex(testCase);

		String tempTestStepName = testStepName;
		ArrayList<String> tempOperationNamesList = new ArrayList<String>();

		if(!testCaseOperataionNamesList.isEmpty() && (testCaseOperataionNamesList.get(testCase)!= null))
		{
			tempOperationNamesList = testCaseOperataionNamesList.get(testCase);

			if (tempOperationNamesList.contains(testStepName))
			{
				HashMap<String, Integer> sameOperationNameCounter = testCaseSameOperationNameCounter.get(testCase);
				int numberOfSameOperation = sameOperationNameCounter.get(testStepName);

				tempTestStepName += ("_"+(numberOfSameOperation-1));
				numberOfSameOperation++;
				sameOperationNameCounter.put(testStepName, Integer.valueOf(numberOfSameOperation));
				testCaseSameOperationNameCounter.put(testCase, sameOperationNameCounter);
			}
			else
			{
				tempOperationNamesList.add(testStepName);
				testCaseOperataionNamesList.put(testCase, tempOperationNamesList);
//				HashMap<String, Integer> sameOperationNameCounter = new HashMap<String, Integer>();
				testCaseSameOperationNameCounter.get(testCase).put(testStepName, Integer.valueOf(1));
			}
		}
		else
		{
				tempOperationNamesList.add(testStepName);
				testCaseOperataionNamesList.put(testCase, tempOperationNamesList);
				HashMap<String, Integer> sameOperationNameCounter = new HashMap<String, Integer>();
				sameOperationNameCounter.put(testStepName, Integer.valueOf(1));
				testCaseSameOperationNameCounter.put(testCase, sameOperationNameCounter);
		}
		config.setName(tempTestStepName);
	
		Assertable step = (Assertable) testCase.addTestStep(config);
		if (step == null)
			throw new TestGeneratorException("Failed to add TestStep: " + testStepName);
		
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
			xpathBodyExpr = "./in/message/parameters/*[1]";
		else
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

	protected List<GCXpathAssertion> addAssertions(Assertable step, Node suspectedBodyContent)
	{
		List<GCXpathAssertion> assertions = new ArrayList<GCXpathAssertion>();
		try
		{
			//shit... suspectedBodyContent is with turned off namespace awareness... :/
			File tmpFile = File.createTempFile("xml", null);
			FileOutputStream fos = new FileOutputStream(tmpFile);
			IOSupport.printNode(suspectedBodyContent, fos);
			fos.close();
			suspectedBodyContent = SimpleXmlParser.parse(tmpFile).getFirstChild();
			tmpFile.delete();

			step.addAssertion(SoapResponseAssertion.LABEL);//.setDisabled(true);
			step.addAssertion(SchemaComplianceAssertion.LABEL);//.setDisabled(true);

			xPathAssertionsExtractor extractor = new xPathAssertionsExtractor();

			for (xPathAssertionsExtractor.xPathAssertion assertion : extractor.extract(suspectedBodyContent))
			{	
				XPathContainsAssertion xText = (XPathContainsAssertion) step.addAssertion(XPathContainsAssertion.LABEL);
				xText.setName(assertion.getName());
				xText.setPath(assertion.getPath());
				xText.setExpectedContent(assertion.getExpectedContent());
				xText.setDisabled(true);
				assertions.add(new GCXpathAssertion(xText, assertion.getShortName()));
			}
		}
		catch (Exception ex)
		{
			log.warn("Failed to add Xpath assertions for: " + step.getModelItem().getName());
		}
		return assertions;
	}

}
