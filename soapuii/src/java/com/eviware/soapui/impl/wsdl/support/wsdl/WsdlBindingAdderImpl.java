/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eviware.soapui.impl.wsdl.support.wsdl;
import com.eviware.soapui.impl.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import com.eviware.soapui.support.xml.XmlUtils;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author azl
 */
public class WsdlBindingAdderImpl implements WsdlBindingAdder
{
	protected XPath xpath;
	protected static XPathFactory factory = XPathFactory.newInstance();

	public WsdlBindingAdderImpl()
	{
		xpath = factory.newXPath();
		BasicNamespaceContext nameSpaceContext = new BasicNamespaceContext();
		nameSpaceContext.setNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		xpath.setNamespaceContext(nameSpaceContext);
	}

	public InputStream addBindingToWsdl(InputStream inputStream) throws WsdlBindingException
	{
		Document wsdlInDocument = null;

		wsdlInDocument = XmlUtils.parse(inputStream);
		if (checkBinding(wsdlInDocument))
		{
			System.out.println("NO CHANGES");
			return returnUnchangedInputStream(wsdlInDocument);
		} 
		else
		{
			return returnChangedInputStream(wsdlInDocument);
		}
	}

	protected boolean checkBinding(Document wsdlInDocument) throws WsdlBindingException
	{
		boolean checkBindingFlag = true;
		NodeList portTypeList = getPortTypeName(wsdlInDocument);
		int portTypeLength = portTypeList.getLength();

		for(int i = 0; i < portTypeLength; i++)
		{
			Node currentPortType = portTypeList.item(i);
			String portName = currentPortType.getAttributes().getNamedItem("name").getNodeValue();
			if(!hasThisBinding(wsdlInDocument, portName))
				checkBindingFlag = false;
		}

		return checkBindingFlag;
	}

	protected boolean hasThisBinding(Document wsdlInDocument, String portName) throws WsdlBindingException
	{
		NodeList bindingList = getBindingAgainstType(wsdlInDocument, portName);
		int bindingListWithTheSameTypeLength = bindingList.getLength();
		
		if(bindingListWithTheSameTypeLength >= 1)
		{
			Node bindingNode = bindingList.item(0);	
			if(!bindingNode.getAttributes().getNamedItem("name").getNodeValue().equals(portName))
				bindingNode.getAttributes().getNamedItem("name").setNodeValue(portName);			
			return true;	
		}
		else
			return false;
		
	}

	protected InputStream returnUnchangedInputStream(Document wsdlInDocument) throws WsdlBindingException
	{
		return resultInputStream(wsdlInDocument);
	}

	protected InputStream returnChangedInputStream(Document wsdlInDocument) throws WsdlBindingException
	{

		NodeList portTypeList = getPortTypeName(wsdlInDocument);
		int portTypeLength = portTypeList.getLength();

		//adding new namespace to document - xmlns:soap = "http://schemas.xmlsoap.org/wsdl/soap/"
		Element definitions = wsdlInDocument.getDocumentElement();
		definitions.setAttribute("xmlns:soap", "http://schemas.xmlsoap.org/wsdl/soap/");

		//checking all portType to add bindings to them
		for (int i = 0; i < portTypeLength; i++)
		{
			Node currentPortType = portTypeList.item(i);
			String portName = currentPortType.getAttributes().getNamedItem("name").getNodeValue();

			if(!hasThisBinding(wsdlInDocument, portName))
			{
				Element binding = createBinding(wsdlInDocument, currentPortType, portName);
				definitions.appendChild(binding);
				Element service = createService(wsdlInDocument, currentPortType, portName);
				definitions.appendChild(service);
			}
		}	
		
		return resultInputStream(wsdlInDocument);
	}

	protected NodeList getOperationList(Node currentPortType) throws WsdlBindingException
	{
		String xpathExpr = "./wsdl:operation";
		return getDefaultNodeList(xpathExpr, currentPortType);
	}

	protected NodeList getPortTypeName(Document wsdlInDocument) throws WsdlBindingException
	{
		String xpathExpr = "/wsdl:definitions/wsdl:portType";
		return getDefaultNodeList(xpathExpr, wsdlInDocument);
	}

	protected NodeList getBindingList(Document wsdlInDocument) throws WsdlBindingException
	{
		String xpathExpr = "/wsdl:definitions/wsdl:binding";
		return getDefaultNodeList(xpathExpr, wsdlInDocument);
	}

	protected NodeList getBindingAgainstType(Document wsdlInDocument, String portName) throws WsdlBindingException
	{
		String temp = "tns:"+portName;
		String xpathExpr = "/wsdl:definitions/wsdl:binding[@type='" +temp+"']";
		return getDefaultNodeList(xpathExpr, wsdlInDocument);
	}

	protected NodeList getServiceList(Document wsdlInDocument) throws WsdlBindingException
	{
		String xpathExpr = "/wsdl:definitions/wsdl:service";
		return getDefaultNodeList(xpathExpr, wsdlInDocument);
	}

	protected NodeList getServiceListWithName(Document wsdlInDocument, String portName) throws WsdlBindingException
	{
		String xpathExpr = "/wsdl:definitions/wsdl:service[@name='" +portName+"']";
		return getDefaultNodeList(xpathExpr, wsdlInDocument);
	}

	protected NodeList getDefinitionList(Document wsdlInDocument) throws WsdlBindingException
	{
		String xpathExpr = "/wsdl:definitions";		
		return getDefaultNodeList(xpathExpr, wsdlInDocument);
	}

	protected NodeList getAllOperationChilds(Node currentWsdlOperation) throws WsdlBindingException
	{
		String xpathExpr = "./*";
		return getDefaultNodeList(xpathExpr, currentWsdlOperation);
	}

	protected NodeList getDefaultNodeList(String xpathExpr, Document wsdlInDocument) throws WsdlBindingException
	{
		NodeList defaultList = null;
		try
		{
			defaultList = (NodeList) xpath.evaluate(xpathExpr, wsdlInDocument, XPathConstants.NODESET);
		}
		catch (XPathExpressionException ex)
		{
			throw new WsdlBindingException("Cannot execute this xPath", ex);
		}

		return defaultList;
	}

	protected NodeList getDefaultNodeList(String xpathExpr, Node wsdlOperation) throws WsdlBindingException
	{
		NodeList defaultList = null;
		try
		{
			defaultList = (NodeList) xpath.evaluate(xpathExpr, wsdlOperation, XPathConstants.NODESET);
		}
		catch (XPathExpressionException ex)
		{
			throw new WsdlBindingException("Cannot execute this xPath", ex);
		}

		return defaultList;
	}

	protected Element createBinding(Document wsdlInDocument, Node currentPortType, String portName) throws WsdlBindingException
	{
		Element wsdlBinding = wsdlInDocument.createElementNS("http://schemas.xmlsoap.org/wsdl/", "binding");

		wsdlBinding.setAttribute("name", portName);
		wsdlBinding.setAttribute("type", ("tns:"+portName));

		Element soapBinding = createSoapBinding(wsdlInDocument);
		wsdlBinding.appendChild(soapBinding);

		//adding operations to binding
		NodeList wsdlOperationList = getOperationList(currentPortType);
		int wsdlOperationListLength = wsdlOperationList.getLength();

		for (int i = 0; i < wsdlOperationListLength; i++)
		{
			Node currentWsdlOperation = wsdlOperationList.item(i);
			String wsdlOperationName = currentWsdlOperation.getAttributes().getNamedItem("name").getNodeValue();
			Element wsdlBindingOperation = createWsdlBindingOperation(wsdlInDocument, currentWsdlOperation, wsdlOperationName);
			wsdlBinding.appendChild(wsdlBindingOperation);
		}

		return wsdlBinding;
	}

	protected Element createSoapBinding(Document wsdlInDocument)
	{
		//<soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
		Element soapBinding = wsdlInDocument.createElementNS("http://schemas.xmlsoap.org/wsdl/soap/", "binding");
		soapBinding.setAttribute("style", "document");
		soapBinding.setAttribute("transport", "http://schemas.xmlsoap.org/soap/http");
		return soapBinding;
	}

	protected Element createWsdlBindingOperation(Document wsdlInDocument, Node currentWsdlOperation, String wsdlOperationName) throws WsdlBindingException
	{

		Element wsdlBindingOperation = wsdlInDocument.createElementNS("http://schemas.xmlsoap.org/wsdl/", "operation");
		wsdlBindingOperation.setAttribute("name", wsdlOperationName);

		Element soapBindingOperation = wsdlInDocument.createElementNS("http://schemas.xmlsoap.org/wsdl/soap/", "operation");
		wsdlBindingOperation.appendChild(soapBindingOperation);


		NodeList operationChilds = getAllOperationChilds(currentWsdlOperation);
		int operationChildsLength = operationChilds.getLength();

		for(int i = 0; i < operationChildsLength; i++)
		{
			Node currentOperationChild = operationChilds.item(i);
			String childName = currentOperationChild.getNodeName();
			String nameAttrr = null;
			
			Node operationChildNameAttribute = currentOperationChild.getAttributes().getNamedItem("name");
			if (operationChildNameAttribute != null)
				nameAttrr = operationChildNameAttribute.getNodeValue();

			Element operationChild = wsdlInDocument.createElementNS("http://schemas.xmlsoap.org/wsdl/", childName);
			if (nameAttrr != null)
				operationChild.setAttribute("name", nameAttrr);

				Element soapBody = wsdlInDocument.createElementNS("http://schemas.xmlsoap.org/wsdl/soap/", "body");
				soapBody.setAttribute("use", "literal");
			operationChild.appendChild(soapBody);

			wsdlBindingOperation.appendChild(operationChild);
		}

		return wsdlBindingOperation;
	}

	protected Element createService(Document wsdlInDocument, Node currentPortType, String portName)
	{
		Element wsdlService = wsdlInDocument.createElementNS("http://schemas.xmlsoap.org/wsdl/", "service");

		wsdlService.setAttribute("name", portName);

		Element wsdlPort = wsdlInDocument.createElementNS("http://schemas.xmlsoap.org/wsdl/", "port");
			wsdlPort.setAttribute("name", portName);
			wsdlPort.setAttribute("binding", ("tns:"+portName));
				Element soapAddress = wsdlInDocument.createElementNS("http://schemas.xmlsoap.org/wsdl/soap/", "address");
				soapAddress.setAttribute("location", ("http://localhost:${HttpDefaultPort}/service/"+portName));
			wsdlPort.appendChild(soapAddress);

		wsdlService.appendChild(wsdlPort);

		return wsdlService;
	}

	protected InputStream resultInputStream(Document wsdlInDocument) throws WsdlBindingException
	{
		InputStream result = null;
		String resultStreamString = null;
		resultStreamString = XmlUtils.serialize(wsdlInDocument);

		try
		{
			result = new ByteArrayInputStream(resultStreamString.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			throw new WsdlBindingException("Cannot support this encoding", e);
		}

		return result;
	}

	private class BasicNamespaceContext implements NamespaceContext
	{
		private HashMap<String, String> namespacesMap = new HashMap();

		public void setNamespace(String prefix, String namespace)
		{
			namespacesMap.put(prefix, namespace);
		}
		public String getNamespaceURI(String prefix)
		{
			return namespacesMap.get(prefix);
		}

		public String getPrefix(String uri)
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Iterator getPrefixes(String uri)
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

}
