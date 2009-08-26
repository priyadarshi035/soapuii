/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.w3c.dom.Node;
import pl.touk.proxygeneratorapi.URIGetter;

/**
 *
 * @author pnw
 */
public class GetCommunicationCommons
{
	protected final static Pattern namespacePrefixPattern = Pattern.compile("(\\w+):(.+)");

	public static QName parseServiceNode(Node service)
	{
		String serviceNamespace = null;
//		String serviceName = URIGetter.getUriEndingKey(service.getTextContent());
		String serviceName = service.getTextContent();
		
		Matcher matcher = namespacePrefixPattern.matcher(serviceName);

		if (matcher.find())
		{
			serviceNamespace =
					service.getAttributes().getNamedItem("xmlns:" + matcher.group(1)).getNodeValue();
			serviceName = matcher.group(2);
		}
		return new QName(serviceNamespace, serviceName);		
	}

}
