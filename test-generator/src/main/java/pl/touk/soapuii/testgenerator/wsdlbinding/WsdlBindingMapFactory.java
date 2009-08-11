/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.wsdlbinding;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.components.SwingConfigurationDialogImpl;
import com.eviware.soapui.support.types.StringToStringMap;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pl.touk.proxygeneratorapi.URIGetter;
import pl.touk.proxygeneratorapi.support.ExtFileFilter;
import pl.touk.proxygeneratorapi.support.SimpleXmlParser;


/**
 *
 * @author pnw
 */
public class WsdlBindingMapFactory
{
	protected final static Pattern namespacePrefixPattern = Pattern.compile("(\\w+):(\\w+)");

	public Map createBindingMap(WsdlProject project, File dir) throws WsdlBindingException
	{
		List<String> services = new ArrayList();
		if (dir.isDirectory())
			for (File file : dir.listFiles(new ExtFileFilter(".xml")))
				services.addAll(parseGetCommunication(project, file));
		else
			services.addAll(parseGetCommunication(project, dir));

		Map result = matchBindings(project.getInterfaceList(), services);

		return result;
	}

/*
<soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
   <soap:Body>
      <getCommunicationResponse xmlns:axis2ns3="http://www.apache.org/ode/pmapi" xmlns="http://www.apache.org/ode/pmapi">
         <getCommunicationResponse xmlns="">
            <restoreInstance xmlns:ns="http://www.apache.org/ode/pmapi/types/2006/08/02/" xmlns="http://www.apache.org/ode/pmapi/types/2006/08/02/">
               <processType xmlns:bpel="http://playmobile.pl/process/mnpm/portIn/bpel">bpel:CaseProcess</processType>
               <exchange>
                  <type>M</type>
                  <createTime>2009-07-21T17:01:47.670+02:00</createTime>
                  <service xmlns:por="http://playmobile.pl/process/mnpm/portIn">por:CaseRunner-1</service>
						  */

	protected List<String> parseGetCommunication(WsdlProject project, File file) throws WsdlBindingException
	{
		List<String> result = new ArrayList();
		try
		{
			String serviceXpath = "/Envelope/Body/getCommunicationResponse/getCommunicationResponse/restoreInstance/exchange/service";
			NodeList services = SimpleXmlParser.evaluate(serviceXpath, file, null);
			for (int i = 0; i < services.getLength(); i++)
			{
				Node service = services.item(i);

				String serviceNamespace = null;
				String serviceName = URIGetter.getUriEndingKey(service.getTextContent());

				Matcher matcher = namespacePrefixPattern.matcher(serviceName);

				if (matcher.find())
				{
					serviceNamespace =
						service.getAttributes().getNamedItem("xmlns:" + matcher.group(1)).getNodeValue();
					serviceName = "(" + serviceNamespace + ") " + matcher.group(2);
				}
				result.add(serviceName);
			}
		}
		catch(Exception ex)
		{
			throw new WsdlBindingException("Failed to parse [" + file + "]", ex);
		}
		return result;
	}

	protected Map matchBindings(List<Interface> interfaceList, List<String> services)
	{
		Map result = new HashMap();
		SwingConfigurationDialogImpl dialog = new SwingConfigurationDialogImpl("Match bindings", null, null, null);

		List<PrettyInterface> bindings = new ArrayList();
		for (Interface binding : interfaceList)
			//for (Operation operation : binding.getOperationList
				//operations.add(new PrettyInterface(operation));
			bindings.add(new PrettyInterface(binding));
		for(String service : services)
			dialog.addComboBox(service, bindings.toArray(), "Select binding");

		while (dialog.show(new StringToStringMap()))
		{
		}
		return result;
	}
}
