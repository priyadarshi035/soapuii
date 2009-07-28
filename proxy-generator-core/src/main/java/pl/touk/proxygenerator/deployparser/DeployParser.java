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
import pl.touk.proxygenerator.wsdlmap.WsdlMapFactory;

/**
 *
 * @author azl
 */
public class DeployParser implements DeployParserInterface
{
	private final static Pattern namespacePrefixPattern = Pattern.compile("(\\w+):(\\w+)");
	private final static String deployFileName = "deploy.xml";
	private final static String xbeanFileName = "xbean.xml";
	private File deployFile;
	private File xbeanFile;
	private Beans beans;
	private Document dom = null;
	private WsdlMapFactory wsdlMapFactory;
	private Map<MultiKey, String> wsdlMap;

	private final static Logger log = Logger.getLogger(DeployParser.class.getName());

	
	private List endpointData;

	private XPathFactory factory = XPathFactory.newInstance();

	public DeployParser() throws ParserConfigurationException
	{
		org.apache.log4j.BasicConfigurator.configure();
		wsdlMapFactory = new WsdlMapFactory();
		endpointData = new ArrayList();
	};

	public File parseDeployXml(File fileToParse, Map<MultiKey, String> wsdlMap) throws Exception
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
			Logger.getLogger(DeployParser.class.getName()).log(Level.SEVERE, null, ex);
		}

		doc = generateDOMTree(dom, wsdlMap);

		printToFile(doc);
		return result;
	}


	public Document generateDOMTree(Document dom, Map<MultiKey,String> wsdlMap)
	{
		Element domRoot = null;
//		NodeList domList = dom.getChildNodes();

//		for	(int i = 0; i < domList.getLength(); i++)
//		{
//			if (domList.item(i) instanceof Element)
//			{
//				domRoot = (Element) domList.item(i);
//				System.out.println ("Root element of the doc is " +
//						domRoot.getNodeName());
//				break;
//			}
//		}


		domRoot = dom.getDocumentElement();
		System.out.println ("Root element of the doc is " +
						domRoot.getNodeName());

		NamedNodeMap domInAttrs = domRoot.getAttributes();


//		System.out.println ("Root element of the doc is " +
//                 dom.getDocumentElement().getNodeName());
		


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


		Element rootElement = result.createElement("beans");

		for (int i = 0; i < domInAttrs.getLength(); i++)
		{
			Attr attr = (Attr)domInAttrs.item(i);
			String attrName = attr.getNodeName();
			String attrValue = attr.getNodeValue();

			result.createAttributeNS(attrName, attrValue);
		}


		Comment provides = result.createComment("provides");
		Comment caseProcess = result.createComment("CaseProcess");
		Comment utilitiesProvider = result.createComment("UtilitiesProvider");
		Comment customerNotifier = result.createComment("CustomerNotifier");
		Comment standardContractProcess = result.createComment("StandardContractProcess");
		Comment oneVisitContractProcess	= result.createComment("OneVisitContractProcess");
		Comment invoke = result.createComment("Invoke");


//		rootElement.setAttribute("xmlns:http", beans.getHttp());
//		rootElement.setAttribute("xmlns:atm", beans.getAtm());
//		rootElement.setAttribute("xmlns:dh", beans.getDh());
//		rootElement.setAttribute("xmlns:qmm", beans.getQmm());
//		rootElement.setAttribute("xmlns:mwk", beans.getMwk());
//		rootElement.setAttribute("xmlns:ws", beans.getWs());

		result.appendChild(rootElement);


//		endpointData.add(new Endpoint("default", "http://0.0.0.0:8667/process/mnpm/portln/Caserunner-1/ ", "consumer", "ws:CaseRunner-1", "true", "1,1", "classpath:Caserunner.wsdl") );
//
//		Iterator it = endpointData.iterator();
//		while(it.hasNext())
//		{
//			Endpoint endpoint = (Endpoint)it.next();
//			Element endpointElement = createEndpointElement(endpoint);
//			rootElement.appendChild(endpointElement);
//		}

		return result;
				
	}

	private Element createEndpointElement( Endpoint endpoint)
	{
       Element endpointElement = dom.createElement("http:endpoint");
//       endpointElement.setAttribute("endpoint", endpoint.getEndpoint());
//	   endpointElement.setAttribute("locationURI", endpoint.getLocationURI());
//	   endpointElement.setAttribute("role", endpoint.getRole());
//	   endpointElement.setAttribute("service", endpoint.getService());
//	   endpointElement.setAttribute("soap", endpoint.getSoap());
//	   endpointElement.setAttribute("soapVerstion", endpoint.getSoapVersion());
//	   endpointElement.setAttribute("wsdlResource", endpoint.getWsdlResource());

	   return endpointElement;
	}

	public File createXBeanFile(Document doc) throws DeployParserException
	{
		File temp = null;
		Document localDoc = doc;

		printToFile(localDoc);
		return temp;
	}

	 private void printToFile(Document localDoc)
	 {
		try
		{
			 OutputFormat format = new OutputFormat(localDoc);
			  format.setIndenting(true);
		   //to generate output to console use this serializer
		  //XMLSerializer serializer = new XMLSerializer(System.out, format);
		 //to generate a file output use fileoutputstream instead of system.out
			  XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new File("xbean.xml")), format);
			  serializer.serialize(localDoc);
		} catch(IOException ie) {
		   ie.printStackTrace();
		}
	  }

	 public static void main(String [] args) throws ParserConfigurationException, Exception
	{
		DeployParser dp = new DeployParser();
		File deploy = new File("deploy.xml");

		Map<MultiKey, String> wsdlMap = dp.wsdlMapFactory.createWsdlMap("src/test/resources/bpel/HelloWorld2/");

		File xbean = dp.parseDeployXml(deploy, wsdlMap);

		//File xbean = dp.parseDeployXml(deploy, wsdlMap);

//		Beans beans = new Beans("http://servicemix.apache.org/http/1.0",
//								"http://playmobile.pl/adapter/atmosfera",
//								"http://playmobile.pl/adapter/dh",
//								"http://playmobile.pl/service/mnpm",
//								"http://playmobile.pl/adapter/mwk",
//								"http://playmobile.pl/process/mnpm/portIn");
//		dp.createDOMTree(beans);
//		xg.printToFile();
	}
}
