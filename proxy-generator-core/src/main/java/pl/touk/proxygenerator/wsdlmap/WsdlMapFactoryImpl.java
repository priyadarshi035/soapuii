/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.wsdlmap;

import java.net.URISyntaxException;
import pl.touk.proxygenerator.wsdlmap.*;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.xml.sax.SAXException;
import pl.touk.proxygenerator.support.BasicNamespaceContext;
import pl.touk.proxygenerator.support.ExtFileFilter;
import pl.touk.proxygenerator.support.FileTraversal;

/**
 *
 * @author pnw
 */
public class WsdlMapFactoryImpl implements WsdlMapFactory
{
	private final static Pattern namespacePrefixPattern = Pattern.compile("(\\w+):(\\w+)");
	private final static Logger log = Logger.getLogger(WsdlMapFactoryImpl.class);
	private DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder builder;
	private XPathFactory factory = XPathFactory.newInstance();
	
	public WsdlMapFactoryImpl() throws ParserConfigurationException
	{
		domFactory.setNamespaceAware(true);
		domFactory.setIgnoringComments(true);
		builder = domFactory.newDocumentBuilder();
		org.apache.log4j.BasicConfigurator.configure();
	};

	public Map<MultiKey, String> createWsdlMap(String path) throws WsdlMapException
	{
		Map<MultiKey, String> result = null;

		File dir = new File(path);
		if (!dir.isDirectory())
			throw new WsdlMapException(path + " is not a directory.");

		//File[] files = dir.listFiles(new ExtFileFilter(".bpel"));
		try
		{
			List<File> files = new FileTraversal().traverse(dir, new ExtFileFilter(".bpel"));
			log.info("found *.bpel: " + files.size());
			ArrayList<InputStream> streams = new ArrayList();

			for(File file : files)
				try
				{
					streams.add(new FileInputStream(file));
				} catch (FileNotFoundException ex) //should never happen
				{
					throw new WsdlMapException("File: [" + file + "] not found. " + ex);
				}


			Map<MultiKey, MultiKey> bpelsPartnerLinks = cacheBpels(streams);

/*			Iterator it = bpelsPartnerLinks.entrySet().iterator();
			while (it.hasNext())
			{
				Map.Entry pairs = (Map.Entry) it.next();
				System.out.println(pairs.getKey() + " = " + pairs.getValue());
			}*/

			//files = dir.listFiles(new ExtFileFilter(".wsdl"));
			files = new FileTraversal().traverse(dir, new ExtFileFilter(".wsdl"));
			ArrayList<File> filesArrayList = new ArrayList(files);

			result = parseWsdl(filesArrayList, bpelsPartnerLinks);
		}
		catch (XPathExpressionException ex) //should never happen
		{
			throw new WsdlMapException("Hardcoded xpath error. " + ex);
		} catch (SAXException ex)
		{
			throw new WsdlMapException("Building xml parser error. " + ex);
		} catch (IOException ex) //shouldn't happen
		{
			throw new WsdlMapException("Error while processing stream: " + ex);
		}
		catch (URISyntaxException ex)  //should never happen
		{
			throw new WsdlMapException("Fatal impossible error: " + ex);
		}
		return result;
	};

	protected Map parseWsdl(ArrayList<File> files, Map<MultiKey, MultiKey> bpelsPartnerLinks) throws SAXException, IOException, XPathExpressionException, URISyntaxException
	{
		Map<MultiKey, String> result = new HashMap<MultiKey, String>();
		for (File file : files)
		{
//			log.info("processing file: " + file.getName());
			Document doc = builder.parse(new FileInputStream(file));
			Node root = doc.getFirstChild();

			String targetNamespace = root.getAttributes().getNamedItem("targetNamespace").getNodeValue();
			BasicNamespaceContext namespaceContext = new BasicNamespaceContext();
			//String pre = root.getPrefix();
			//namespaceContext.setNemaspace(pre, root.getNamespaceURI());
			namespaceContext.setNemaspace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
			namespaceContext.setNemaspace("plnk", "http://docs.oasis-open.org/wsbpel/2.0/plnktype");

			XPath xpath = factory.newXPath();
			xpath.setNamespaceContext(namespaceContext);

			//String xpathExpr = String.format( "/%s:definitions/plnk:partnerLinkType", pre);
			String xpathExpr = "/wsdl:definitions/plnk:partnerLinkType";
			NodeList partnerLinkTypes = (NodeList) xpath.evaluate(xpathExpr, root, XPathConstants.NODESET);

			for (int i = 0; i < partnerLinkTypes.getLength(); i++)
			{
				Node partnerLinkType = partnerLinkTypes.item(i);
				String partnerLinkTypeName =
						partnerLinkType.getAttributes().getNamedItem("name").getNodeValue();

				//String roleXpathExpr = String.format("/%s:definitions/plnk:partnerLinkType[@name='%s']/plnk:role", pre, partnerLinkTypeName);
				String roleXpathExpr = String.format(
						"/wsdl:definitions/plnk:partnerLinkType[@name='%s']/plnk:role",
						partnerLinkTypeName);
				
				NodeList roles = (NodeList) xpath.evaluate(roleXpathExpr, root, XPathConstants.NODESET);

				for (int j = 0; j < roles.getLength(); j++)
				{
					Node role = roles.item(j);
					String roleName = role.getAttributes().getNamedItem("name").getNodeValue();
//					log.info("Processing: " + targetNamespace + ", " + partnerLinkTypeName + ", " + roleName);
					MultiKey key =  bpelsPartnerLinks.get(new MultiKey(targetNamespace, partnerLinkTypeName, roleName));
					if (key != null)
					{
						URI absPath = new URI(file.getAbsolutePath());
						URI userDir = new URI(System.getProperty("user.dir"));
						URI relPath = userDir.relativize(absPath);

						result.put(key, relPath.toString());
					}
				}
			}
		}
		return result;
	}

	protected Map<MultiKey, MultiKey> cacheBpels(ArrayList<InputStream> streams) throws SAXException, IOException, XPathExpressionException
	{
		Map<MultiKey, MultiKey> result = new HashMap<MultiKey, MultiKey>();
		for (InputStream stream : streams)
		{
			Document doc = builder.parse(stream);
			Node root = doc.getFirstChild();
			String processName = root.getAttributes().getNamedItem("name").getNodeValue();
			BasicNamespaceContext namespaceContext = new BasicNamespaceContext();
			//String pre = root.getPrefix();
			//namespaceContext.setNemaspace(pre, root.getNamespaceURI());
			namespaceContext.setNemaspace("bpws", "http://docs.oasis-open.org/wsbpel/2.0/process/executable");

			XPath xpath = factory.newXPath();
			xpath.setNamespaceContext(namespaceContext);

			//String xpathExpr = String.format("/%s:process/%s:partnerLinks/%s:partnerLink", pre, pre, pre);
			String xpathExpr = "/bpws:process/bpws:partnerLinks/bpws:partnerLink";
			NodeList partnerLinks = (NodeList) xpath.evaluate(xpathExpr, root, XPathConstants.NODESET);
			for (int i = 0; i < partnerLinks.getLength(); i++)
			{
				Node partnerLink = partnerLinks.item(i);

				String partnerLinkName = partnerLink.getAttributes().getNamedItem("name").getNodeValue();
				String partnerLinkType = partnerLink.getAttributes().getNamedItem("partnerLinkType").getNodeValue();
				String partnerLinkTypeNamespace = null;

				MultiKey key, value = null;
				String roleName = "";

				Matcher matcher = namespacePrefixPattern.matcher(partnerLinkType);

				//namespace used
				if (matcher.find())
				{
					partnerLinkTypeNamespace =
							root.getAttributes().getNamedItem("xmlns:" + matcher.group(1)).getNodeValue();
					partnerLinkType = matcher.group(2);
					//System.out.println(root.getAttributes().getNamedItem("xmlns:" + matcher.group(1)).getNodeValue());
				}
				
				if (partnerLink.getAttributes().getNamedItem("myRole") != null)
				{
					roleName = partnerLink.getAttributes().getNamedItem("myRole").getNodeValue();
					value = new MultiKey(processName, partnerLinkName, MYROLE);
					key = new MultiKey(partnerLinkTypeNamespace, partnerLinkType, roleName);

					result.put(key, value);
				}
				if (partnerLink.getAttributes().getNamedItem("partnerRole") != null)
				{
					roleName = partnerLink.getAttributes().getNamedItem("partnerRole").getNodeValue();
					value = new MultiKey(processName, partnerLinkName, PARTNERROLE);
					key = new MultiKey(partnerLinkTypeNamespace, partnerLinkType, roleName);
					
					result.put(key, value);
				}
			}
		}
		return result;
	}
}
