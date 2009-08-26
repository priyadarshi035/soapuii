/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author pnw
 */
public class xPathAssertionsExtractor
{
	protected static final String defaultNode = "defaultNode";
	protected Set<String> usedPrefixes = new HashSet<String>();
	protected Map<String, QName> declarations = new HashMap<String, QName>();
	protected final String defaultNs = "defns";
	protected int uesdDefaultNs = 0;

	public class xPathAssertion
	{
		private String xpath;
		private String content;
		private String name;
		private String shortXPath;

		private xPathAssertion(String string, Collection<QName> declarations, String nodeValue, String shortXPath)
		{
			this.shortXPath = shortXPath;
			this.name = "XPath [" + shortXPath + "] Match [" + nodeValue + "]";
			xpath = "";
			for (QName declaration : declarations)
				xpath += "declare namespace " + declaration.getPrefix() + "='" + declaration.getNamespaceURI() +"';\n";
			xpath += "\n" + string;
			content = nodeValue;
		}

		public String getPath()
		{
			return xpath;
		}
		public String getExpectedContent()
		{
			return content;
		}
		public String getName()
		{
			return name;
		}
		public String getShortName()
		{
			return shortXPath;
		}
	}

	public List<xPathAssertion> extract(Node xml)
	{
		List<xPathAssertion> assertions = new ArrayList<xPathAssertion>();
		if (xml instanceof Element)
		{
			//Map<String, QName> declarations = new HashMap<String, QName>();
			//declarations.add("declare namespace soap='http://schemas.xmlsoap.org/soap/envelope/';");

			declarations.put("http://schemas.xmlsoap.org/soap/envelope/",
					generateQName("http://schemas.xmlsoap.org/soap/envelope/", "soap"));
			QName qnamedXml = generateQName(xml);
			declarations.put(qnamedXml.getNamespaceURI(), qnamedXml);
			final String xPath = "/soap:Envelope/soap:Body/" + qnamedXml.getPrefix() + ":" + xml.getLocalName();
			
			assertions = traverse((Element) xml, xPath, "/" + xml.getLocalName());
		}
		return assertions;
	}

	protected List<xPathAssertion> traverse(Element xml, final String xPath, String shortXPath)
	{
		List<xPathAssertion> assertions = new ArrayList<xPathAssertion>();
		NodeList childs = xml.getChildNodes();
		for(int i = 0; i < childs.getLength(); i++)
		{
			Node child = childs.item(i);
			if(child instanceof Element)
			{
//				String traversexPath = xPath + "/"+ xml.getPrefix() + ":" + xml.getLocalName();
				//Map<String, QName> traverseDeclarations = new HashMap(declarations);

				QName qnamedChild = generateQName(child);
				if (declarations.get(qnamedChild.getNamespaceURI()) == null)
					declarations.put(qnamedChild.getNamespaceURI(), qnamedChild);
				else
					qnamedChild = declarations.get(qnamedChild.getNamespaceURI());
				
				String traversexPath = xPath + "/" + qnamedChild.getPrefix() + ":" + child.getLocalName();
				
				assertions.addAll( traverse( (Element) child, traversexPath, shortXPath + "/" + child.getLocalName()));
			}
			else if (child instanceof Text)
			{
				if (!child.getNodeValue().matches(".*\\S.*"))
					continue;
				assertions.add( new xPathAssertion(
						xPath + "/text()", declarations.values(), child.getNodeValue(), shortXPath) );
			}
		}
		return assertions;
	}

	protected QName generateQName(String namespace, String prefix)
	{
		if (prefix == null)
		{
			prefix = defaultNs + uesdDefaultNs;
			uesdDefaultNs += 1;
		}
		while (usedPrefixes.contains(prefix))
		{
			prefix = defaultNs + uesdDefaultNs;
			uesdDefaultNs += 1;
		}

		usedPrefixes.add(prefix);
		return new QName(namespace, defaultNode, prefix);
	}

	protected QName generateQName(Node xml)
	{
		return generateQName(xml.getNamespaceURI(), xml.getPrefix()) ;
	}
}
