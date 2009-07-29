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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pl.touk.proxygenerator.support.BasicNamespaceContext;
import pl.touk.proxygenerator.wsdlmap.WsdlMapFactory;

import pl.touk.proxygenerator.wsdlmap.WsdlMapFactoryImpl;


/**
 *
 * @author azl
 */
public class DeployParserImpl implements DeployParser
{
	private final static Pattern namespacePrefixPattern = Pattern.compile("(\\w+):(\\w+)");
	private final static String deployFileName = "deploy.xml";
	private final static String xbeanFileName = "xbean.xml";
	private String luri;
//	private File deployFile;
//	private File xbeanFile;
//	private Beans beans;
	private Document dom = null;
	private WsdlMapFactoryImpl wsdlMapFactory;
//	private Map<MultiKey, String> wsdlMap;

	private final static Logger log = Logger.getLogger(DeployParserImpl.class.getName());

	
//	private List endpointData;

	private XPathFactory factory = XPathFactory.newInstance();

	public DeployParserImpl() throws ParserConfigurationException
	{
		org.apache.log4j.BasicConfigurator.configure();
		wsdlMapFactory = new WsdlMapFactoryImpl();
//		endpointData = new ArrayList();
	};

	public File parseDeployXml(File fileToParse, String path) throws Exception
	{
		File result = null;
		
		Document doc = null;

		try {
			DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
			domBuilderFactory.setNamespaceAware(true);
			domBuilderFactory.setIgnoringComments(true);


			DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();

			dom = domBuilder.parse(fileToParse);
			dom.getDocumentElement().normalize();

		} catch (IOException ex) {
			Logger.getLogger(DeployParserImpl.class.getName()).log(Level.SEVERE, null, ex);
		}

		Map<MultiKey, String> wsdlMap = wsdlMapFactory.createWsdlMap(path);
		doc = generateDOMTree(dom, wsdlMap);


		printToFile(doc);
		return result;
	}


	public Document generateDOMTree(Document dom, Map<MultiKey,String> wsdlMap) throws XPathExpressionException
	{
		Node inDomRoot = null;

		inDomRoot = dom.getFirstChild();
		System.out.println ("Root element of the doc is " +
						inDomRoot.getNodeName());

		BasicNamespaceContext namespaceContext = new BasicNamespaceContext();
		namespaceContext.setNemaspace("dd", "http://www.apache.org/ode/schemas/dd/2007/03");

		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext(namespaceContext);

		String xpathExpr = "/deploy/process";
		NodeList processList = (NodeList) xpath.evaluate(xpathExpr, inDomRoot, XPathConstants.NODESET);
		System.out.println("MARK1: Number of processes " + processList.getLength());

		NamedNodeMap domInAttrs = inDomRoot.getAttributes();


//new dom file
		Document result = null;

		try {
			DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
			result = domBuilder.newDocument();
		}catch(Exception pce) {
			  System.out.println("Error while trying to instantiate DocumentBuilder " + pce);
			  System.exit(1);
		}

		Element outDomRoot = result.createElement("beans");

		//attach attributes to documentRoot
		for (int i = 0; i < domInAttrs.getLength(); i++)
		{
			Attr attr = (Attr)domInAttrs.item(i);
			String attrName = attr.getNodeName();
			String attrValue = attr.getNodeValue();

			outDomRoot.setAttribute(attrName, attrValue);
		}

		for(int i = 0; i < processList.getLength(); i++)
		{
			Node process = processList.item(i);

			String processName = process.getAttributes().getNamedItem("name").getNodeValue();
//			String processNameNamespace = null;

			Matcher processNameMatcher = namespacePrefixPattern.matcher(processName);

			if(processNameMatcher.find())
			{
//				processNameNamespace = inDomRoot.getAttributes().getNamedItem("xmlns:" + matcher.group(1)).getNodeValue();
				processName = processNameMatcher.group(2);
			}
			System.out.println("MARK2: Process -> " + processName);

			XPath xpathProvide = factory.newXPath();

			String xpathProvideExpr = "/deploy/process/provide";
			NodeList provideList = (NodeList) xpathProvide.evaluate(xpathProvideExpr, inDomRoot, XPathConstants.NODESET);
			System.out.println("MARK3: Number of provides " + provideList.getLength());

			

			for(int j = 0; j < provideList.getLength(); j++)
			{
			
				Node provide = provideList.item(j);
				String partnerLink = provide.getAttributes().getNamedItem("partnerLink").getNodeValue();
				System.out.println("MARK5: PartnerLinkName:-> " + partnerLink);

				Boolean role = wsdlMapFactory.MYROLE;
				MultiKey key = new MultiKey(processName, partnerLink, role);
				String	mapPath = wsdlMap.get(key);
				System.out.println("MARK6: ReturnedPathFromMap:-> " + mapPath);

				XPath xpathService = factory.newXPath();
				String xpathServiceExpr = "/deploy/process/provide/service";
				NodeList serviceList = (NodeList)xpathService.evaluate(xpathServiceExpr, inDomRoot, XPathConstants.NODESET);
				System.out.println("MARK4: Number of services " + serviceList.getLength());

				Node service = serviceList.item(0);
				String serviceName = service.getAttributes().getNamedItem("name").getNodeValue();
				String serviceNamePart = null;
				Matcher serviceMatcher = namespacePrefixPattern.matcher(serviceName);
				
				if(serviceMatcher.find())
				{
					serviceNamePart = serviceMatcher.group(2);
				}
				
				String locationURI = luri.concat("/process/mnpm/portIn/"+serviceNamePart+"/");

				Element endpointElement = result.createElement("http:endpoint");
				endpointElement.setAttribute("endpoint", "default");
				endpointElement.setAttribute("role", "consumer");
				endpointElement.setAttribute("locationURI", locationURI);
				endpointElement.setAttribute("service", serviceName);
				endpointElement.setAttribute("soap", "true");
				endpointElement.setAttribute("soapVersion", "1.1");
				endpointElement.setAttribute("wsdlResource", mapPath);
				outDomRoot.appendChild(endpointElement);
			}
			

		}

		Comment provides = result.createComment("provides");
		Comment caseProcess = result.createComment("CaseProcess");
		Comment utilitiesProvider = result.createComment("UtilitiesProvider");
		Comment customerNotifier = result.createComment("CustomerNotifier");
		Comment standardContractProcess = result.createComment("StandardContractProcess");
		Comment oneVisitContractProcess	= result.createComment("OneVisitContractProcess");
		Comment invoke = result.createComment("Invoke");


		result.appendChild(outDomRoot);
		return result;
			
	}

	 private void printToFile(Document localDoc)
	 {
		try
		{
			 OutputFormat format = new OutputFormat(localDoc);
			  format.setIndenting(true);
			  XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new File("xbean.xml")), format);
			  serializer.serialize(localDoc);
		} catch(IOException ie) {
		   ie.printStackTrace();
		}
	  }

	public void setLuri(String luri)
	{
		this.luri = luri;
	}

	public static void main(String [] args) throws ParserConfigurationException, Exception
	{
		DeployParserImpl dp = new DeployParserImpl();
		dp.setLuri("http://0.0.0.0:8667");

		File deploy = new File("deploy.xml");
		String path = "src/test/resources/bpel/HelloWorld2/";

		File xbean = dp.parseDeployXml(deploy, path);

	}
}
