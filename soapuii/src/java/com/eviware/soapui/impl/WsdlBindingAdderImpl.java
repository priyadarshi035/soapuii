/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eviware.soapui.impl;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import com.eviware.soapui.support.xml.XmlUtils;
import java.io.FileInputStream;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author azl
 */
public class WsdlBindingAdderImpl implements WsdlBindingAdder
{


	public InputStream addBindingToWsdl(InputStream inputStream) throws WsdlBindingException
	{
				
		Document wsdlInDocument = null;
//		FileInputStream fileInputStream = (FileInputStream)inputStream;
		wsdlInDocument = XmlUtils.parse(inputStream);

		if(checkBinding(wsdlInDocument))
			return returnUnchangedInputStream(wsdlInDocument);
		else
			return returnChangedInputStream(wsdlInDocument);
		
	}

	private boolean checkBinding(Document wsdlInDocument)
	{
		Element inDomRoot = XmlUtils.getFirstChildElement((Element) wsdlInDocument);
		String inDomRootName = inDomRoot.getNodeName();
		NodeList wsdlChilds = inDomRoot.getChildNodes();

//		throw new UnsupportedOperationException("Not yet implemented");
	}

	protected InputStream returnUnchangedInputStream(Document wsdlInDocument) throws WsdlBindingException
	{
		String resultStreamString = null;
		InputStream result = null;

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
//		throw new UnsupportedOperationException("Not yet implemented");
	}

	private InputStream returnChangedInputStream(Document wsdlInDocument)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}


}
