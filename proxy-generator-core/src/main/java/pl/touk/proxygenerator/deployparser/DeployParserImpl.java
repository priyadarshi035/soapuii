/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.deployparser;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
//import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
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
//import org.xml.sax.SAXException;

import pl.touk.proxygenerator.support.BasicNamespaceContext;
import pl.touk.proxygenerator.support.FilenameFilterClass;
//import pl.touk.proxygenerator.wsdlmap.WsdlMapFactory;

import pl.touk.proxygenerator.wsdlmap.WsdlMapFactoryImpl;

/**
 *
 * @author azl
 */
public class DeployParserImpl implements DeployParser
{
	private final static Pattern namespacePrefixPattern = Pattern.compile("(\\w+):(\\w+)");
	private Document dom = null;
	private WsdlMapFactoryImpl wsdlMapFactory = null;
	private boolean useNsPrefix = false;
	private final static Logger logger = Logger.getLogger(DeployParserImpl.class.getName());
	private XPathFactory factory = XPathFactory.newInstance();

	private ArrayList<String> consumerList;
	private ArrayList<String> providerList;

	public DeployParserImpl() throws ParserConfigurationException
	{
		org.apache.log4j.BasicConfigurator.configure();
		wsdlMapFactory = new WsdlMapFactoryImpl();
		consumerList = new ArrayList<String>();
		providerList = new ArrayList<String>();
	};

	/**
	 * Creates xbean file
	 *
	 * @param sourcesPath	Path to sources root folder
	 * @return Multikey		MultiKey<Document, List<String>, List<String>>, <xbean.xml, provide properties, invoke properties>
	 */


	public MultiKey parseDeployXml(String sourcesPath, String additionalPropertiesFileName) throws Exception
	{
		MultiKey key = null;
		File fileToParse = null;
		Document doc = null;

		File dir = new File(sourcesPath);
		if (!dir.isDirectory())
			throw new DeployParserException(sourcesPath + " is not a directory.");

		File[] tempFile = dir.listFiles(new FilenameFilterClass("deploy.xml"));
		fileToParse = tempFile[0];

		try {
			DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
			domBuilderFactory.setNamespaceAware(true);
			domBuilderFactory.setIgnoringComments(true);
			DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();

			dom = domBuilder.parse(fileToParse);
			dom.getDocumentElement().normalize();

		} catch (IOException ex) {
			logger.log(Level.SEVERE, null, ex);
		}

		Map<MultiKey, String> wsdlMap = wsdlMapFactory.createWsdlMap(sourcesPath);
		doc = generateDOMTree(dom, wsdlMap);

		key = new MultiKey(doc, consumerList, providerList);

		return key;
		}

	public Document generateDOMTree(Document dom, Map<MultiKey,String> wsdlMap) throws XPathExpressionException
	{
		Node inDomRoot = null;

		inDomRoot = dom.getFirstChild();
		String inDomRootName = inDomRoot.getNodeName();
		String inDomRootNamespacePrefix = null;
		String inDomRootNamespace = null;

		Matcher inDomRootNameMatcher = namespacePrefixPattern.matcher(inDomRootName);

		if(inDomRootNameMatcher.find())
		{
			useNsPrefix = true;
			inDomRootNamespacePrefix = inDomRootNameMatcher.group(1);
			inDomRootNamespace = inDomRoot.getAttributes().getNamedItem("xmlns:" + inDomRootNameMatcher.group(1)).getNodeValue();
			inDomRootName = inDomRootNameMatcher.group(2);
		}

		XPath xpath = factory.newXPath();
		BasicNamespaceContext namespaceContext = new BasicNamespaceContext();
		
		if(useNsPrefix == true)
		{		
	//		namespaceContext.setNemaspace("dd", "http://www.apache.org/ode/schemas/dd/2007/03");
			namespaceContext.setNemaspace(inDomRootNamespacePrefix, inDomRootNamespace);
			xpath.setNamespaceContext(namespaceContext);
		}


		String xpathExpr = null;
			if (useNsPrefix == true)	
				xpathExpr =	"/"+inDomRootNamespacePrefix+":deploy/"+inDomRootNamespacePrefix+":process";
			else
				xpathExpr = "/deploy/process";

//		System.out.println("xpathExpr: " + xpathExpr);
		NodeList processList = (NodeList) xpath.evaluate(xpathExpr, inDomRoot, XPathConstants.NODESET);
//		logger.info("MARK1: number of processes " + processList.getLength() );
//		System.out.println("MARK1: Number of processes " + processList.getLength());

		NamedNodeMap domInAttrs = inDomRoot.getAttributes();


//new dom file

		Document result = null;

		try {
			DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
			result = domBuilder.newDocument();
		}catch(Exception pce) {
			  logger.info("Error while trying to instantiate DocumentBuilder " + pce);
//			  System.out.println("Error while trying to instantiate DocumentBuilder " + pce);
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

		Comment provides = result.createComment("PROVIDES");
		Comment invoke = result.createComment("INVOKES");
		
		outDomRoot.appendChild(provides);

		for(int i = 0; i < processList.getLength(); i++)
		{
			Node process = processList.item(i);

			String processName = process.getAttributes().getNamedItem("name").getNodeValue();
			String processNamePart = null;
//			String processNameNamespace = null;

			Matcher processNameMatcher = namespacePrefixPattern.matcher(processName);

			if(processNameMatcher.find())
			{
//				processNameNamespace = inDomRoot.getAttributes().getNamedItem("xmlns:" + matcher.group(1)).getNodeValue();
				processNamePart = processNameMatcher.group(2);
			}
//			System.out.println("MARK2: Process -> " + processNamePart);

			XPath xpathProvide = factory.newXPath();
			String xpathProvideExpr = null;

			if(useNsPrefix == true)
			{
				xpathProvide.setNamespaceContext(namespaceContext);
//				xpathProvideExpr = "/dd:deploy/dd:process[@name='" +processName+"']/dd:provide";
				xpathProvideExpr =  "/" + inDomRootNamespacePrefix + ":deploy/" + inDomRootNamespacePrefix + ":process[@name='" +processName+"']/" + inDomRootNamespacePrefix +":provide";
				System.out.println(xpathProvideExpr);
	//			xpathProvideExpr = "/" + inDomRootNamespacePrefix + ":deploy/" + inDomRootNamespacePrefix + ":process/" + inDomRootNamespacePrefix +":provide";
			}else
				xpathProvideExpr = "/deploy/process[@name='"+processName+"']/provide";

//			System.out.println("MARK11: xpathProvideExpr " + xpathProvideExpr);

			NodeList provideList = (NodeList) xpathProvide.evaluate(xpathProvideExpr, process, XPathConstants.NODESET);
//			System.out.println("MARK3: Number of provides " + provideList.getLength());

//			String a= "a:adf-1";
//			a = a.substring(a.indexOf(:), a.length());
//			Pattern.compile("(\\w)*").matcher(a).group(1);

			Comment processComment = result.createComment(processNamePart);
			outDomRoot.appendChild(processComment);

			for(int j = 0; j < provideList.getLength(); j++)
			{
			
				Node provide = provideList.item(j);
				String partnerLink = provide.getAttributes().getNamedItem("partnerLink").getNodeValue();
//				System.out.println("MARK5: PartnerLinkName:-> " + partnerLink);

				Boolean role = wsdlMapFactory.MYROLE;
				MultiKey key = new MultiKey(processNamePart, partnerLink, role);
				String	mapPath = wsdlMap.get(key);
//				System.out.println("MARK6: ReturnedPathFromMap:-> " + mapPath);

				XPath xpathService = factory.newXPath();
				String xpathServiceExpr = null;
				if (useNsPrefix == true)
				{
					xpathService.setNamespaceContext(namespaceContext);
//					xpathServiceExpr = "/dd:deploy/dd:process/dd:provide[@partnerLink='"+partnerLink+"']/dd:service";
					xpathServiceExpr =  "/" + inDomRootNamespacePrefix + ":deploy/" + inDomRootNamespacePrefix + ":process/" +inDomRootNamespacePrefix + ":provide[@partnerLink='" +partnerLink+"']/" + inDomRootNamespacePrefix +":service";
				}else
					xpathServiceExpr = "/deploy/process/provide[@partnerLink='"+partnerLink+"']/service";


				
				NodeList serviceList = (NodeList)xpathService.evaluate(xpathServiceExpr, provide, XPathConstants.NODESET);
//				System.out.println("MARK4: Number of services " + serviceList.getLength());

				Node service = serviceList.item(0);
				String serviceName = service.getAttributes().getNamedItem("name").getNodeValue();
//				System.out.println("MARK7: Service->" + serviceName);

				String serviceNamespacePrefix = null;
				String serviceNamePart = null;
				String locationURITemp=null;

				Matcher serviceMatcher = namespacePrefixPattern.matcher(serviceName);
				
				if(serviceMatcher.find())
				{
					serviceNamespacePrefix = serviceMatcher.group(1);
					serviceNamePart = serviceMatcher.group(2);
					locationURITemp = inDomRoot.getAttributes().getNamedItem("xmlns:"+serviceMatcher.group(1)).getNodeValue();
				}

				String locationURI = null;
				if (locationURITemp != null)
				{
					locationURITemp = locationURITemp.replace(".pl", "");
					locationURITemp = locationURITemp.replace("http://", "${pl.");
					locationURITemp = locationURITemp.replace('/', '.');
					locationURI = locationURITemp.concat("."+serviceNamePart+"}");
//					System.out.println("locationURI: " + locationURI);
				}else
					locationURI = "location uri failure";

				consumerList.add(locationURI);

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

		outDomRoot.appendChild(invoke);

		for(int i = 0; i < processList.getLength(); i++)
		{
			Node process = processList.item(i);

			String processName = process.getAttributes().getNamedItem("name").getNodeValue();
			String processNamePart = null;
//			String processNameNamespace = null;

			Matcher processNameMatcher = namespacePrefixPattern.matcher(processName);

			if(processNameMatcher.find())
			{
//				processNameNamespace = inDomRoot.getAttributes().getNamedItem("xmlns:" + matcher.group(1)).getNodeValue();
				processNamePart = processNameMatcher.group(2);
			}
//			System.out.println("MARK2: Process -> " + processNamePart);

			XPath xpathProvide = factory.newXPath();
			String xpathProvideExpr = null;

			if(useNsPrefix == true)
			{
				xpathProvide.setNamespaceContext(namespaceContext);
				xpathProvideExpr = "/dd:deploy/dd:process[@name='" +processName+"']/dd:invoke";
	//			xpathProvideExpr = "/" + inDomRootNamespacePrefix + ":deploy/" + inDomRootNamespacePrefix + ":process/" + inDomRootNamespacePrefix +":provide";
			}else
				xpathProvideExpr = "/deploy/process[@name='"+processName+"']/invoke";

//			System.out.println("MARK11: xpathProvideExpr " + xpathProvideExpr);

			NodeList invokeList = (NodeList) xpathProvide.evaluate(xpathProvideExpr, process, XPathConstants.NODESET);
//			System.out.println("MARK3: Number of invokers " + invokeList.getLength());

//			String a= "a:adf-1";
//			a = a.substring(a.indexOf(:), a.length());
//			Pattern.compile("(\\w)*").matcher(a).group(1);

			Comment processComment = result.createComment(processNamePart);
			outDomRoot.appendChild(processComment);

			for(int j = 0; j < invokeList.getLength(); j++)
			{

				Node invokeNode = invokeList.item(j);
				String partnerLink = invokeNode.getAttributes().getNamedItem("partnerLink").getNodeValue();
//				System.out.println("MARK5: PartnerLinkName:-> " + partnerLink);

				Boolean role = wsdlMapFactory.PARTNERROLE;
				MultiKey key = new MultiKey(processNamePart, partnerLink, role);
				String	mapPath = wsdlMap.get(key);
//				System.out.println("MARK6: ReturnedPathFromMap:-> " + mapPath);

				XPath xpathService = factory.newXPath();
				String xpathServiceExpr = null;
				if (useNsPrefix == true)
				{
					xpathService.setNamespaceContext(namespaceContext);
					xpathServiceExpr = "/dd:deploy/dd:process/dd:invoke[@partnerLink='"+partnerLink+"']/dd:service";
				}else
					xpathServiceExpr = "/deploy/process/invoke[@partnerLink='"+partnerLink+"']/service";



				NodeList serviceList = (NodeList)xpathService.evaluate(xpathServiceExpr, invokeNode, XPathConstants.NODESET);
//				System.out.println("MARK4: Number of services " + serviceList.getLength());

				Node service = serviceList.item(0);
				String serviceName = service.getAttributes().getNamedItem("name").getNodeValue();
//				System.out.println("MARK7: Service->" + serviceName);

				String serviceNamespacePrefix = null;
				String serviceNamePart = null;
				String locationURITemp=null;

				Matcher serviceMatcher = namespacePrefixPattern.matcher(serviceName);

				if(serviceMatcher.find())
				{
					serviceNamespacePrefix = serviceMatcher.group(1);
					serviceNamePart = serviceMatcher.group(2);
					locationURITemp = inDomRoot.getAttributes().getNamedItem("xmlns:"+serviceMatcher.group(1)).getNodeValue();
				}

				String locationURI = null;
				if (locationURITemp != null)
				{
					locationURITemp = locationURITemp.replace(".pl", "");
					locationURITemp = locationURITemp.replace("http://", "${pl.");
					locationURITemp = locationURITemp.replace('/', '.');
					locationURI = locationURITemp.concat("."+serviceNamePart+"}");
//					System.out.println("locationURI: " + locationURI);
				}else
					locationURI = "location uri failure";

				providerList.add(locationURI);

				Element endpointElement = result.createElement("http:endpoint");
				endpointElement.setAttribute("endpoint", "default");
				endpointElement.setAttribute("role", "provider");
				endpointElement.setAttribute("locationURI", locationURI);
				endpointElement.setAttribute("service", serviceName);
				endpointElement.setAttribute("soap", "true");
				endpointElement.setAttribute("soapVersion", "1.1");
				endpointElement.setAttribute("wsdlResource", mapPath);
				outDomRoot.appendChild(endpointElement);
			}
		}

		Element singleBean = result.createElement("bean");
		singleBean.setAttribute("id", "propertyConfigureer");
		singleBean.setAttribute("class", "org.springframework.beans.factory.config.PropertyPlaceholderConfigurer");

			Element singleProperty = result.createElement("property");
			singleProperty.setAttribute("name", "locations");

				Element list = result.createElement("list");

					Element value = result.createElement("value");
					value.setTextContent("classpath:default.properties");

					Element value2 = result.createElement("value");
					value2.setTextContent("classpath:http.uri.properties");

				list.appendChild(value)	;
				list.appendChild(value2);

			singleProperty.appendChild(list);
		
		singleBean.appendChild(singleProperty);
		outDomRoot.appendChild(singleBean);

		result.appendChild(outDomRoot);
		return result;			
	}

	 private File printToFile(Document localDoc)
	 {
		File xbean = new File("xbean.xml");
		try
		{
			 OutputFormat format = new OutputFormat(localDoc);
			  format.setIndenting(true);

			  XMLSerializer serializer = new XMLSerializer(new FileOutputStream(xbean), format);
			  serializer.serialize(localDoc);
		} catch(IOException ie) {
		   ie.printStackTrace();
		}
		return xbean;
	  }

//	public static void main(String [] args) throws ParserConfigurationException, Exception
//	{
//		DeployParserImpl dp = new DeployParserImpl();
//
//		String path = "src/test/resources/bpel/przykladowy_proces/";
//
//		dp.parseDeployXml(path);
//	}
}
