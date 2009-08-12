/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.wsdlbinding;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
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
	protected XFormDialog dialog;

	public Map<QName, Interface> createBindingMap(WsdlProject project, File dir) throws WsdlBindingException
	{
		Map<String, QName> services = new HashMap();
		if (dir.isDirectory())
			for (File file : dir.listFiles(new ExtFileFilter(".xml")))
				services.putAll(parseGetCommunication(project, file));
		else
			services.putAll(parseGetCommunication(project, dir));

		Map<QName, Interface> result = matchBindings(project, services);

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

	protected Map<String, QName> parseGetCommunication(WsdlProject project, File file) throws WsdlBindingException
	{
		Map<String, QName> result = new HashMap();
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
					serviceName = matcher.group(2);
				}
				QName key = new QName(serviceNamespace, serviceName);
				result.put(key.toString(), key);
			}
		}
		catch(Exception ex)
		{
			throw new WsdlBindingException("Failed to parse [" + file + "]", ex);
		}
		return result;
	}

	protected Map<QName, Interface> matchBindings(WsdlProject project, Map<String, QName> services)
	{
		//WsdlInterfaceFactory.WSDL_TYPE
		List<Interface> interfaceList = project.getInterfaceList();
		Map<QName, Interface> result = new HashMap();
		//SwingConfigurationDialogImpl dialog = new SwingConfigurationDialogImpl("Match bindings", null, null, null);

		List<String> bindings = new ArrayList();
		for (Interface binding : interfaceList)
			//for (Operation operation : binding.getOperationList
				//operations.add(new PrettyInterface(operation));
			bindings.add(binding.getName());

		StringToStringMap values = buildDialog(services.keySet(), bindings);

		values = dialog.show(values);
		if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
		{
			//Map<String, String> tmpMap = new HashMap();

			Iterator it = values.keySet().iterator();
			while (it.hasNext())
			{
				String key = it.next().toString();
				String val = values.get(key);
				result.put(services.get(key), project.getInterfaceByName(val));
			}
		}
		return result;
	}

	protected StringToStringMap buildDialog(Set<String> services, List<String> bindings)
	{
		StringToStringMap values = new StringToStringMap();

		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Match bindings" );
		XForm form = builder.createForm( "Basic" );

		dialog = builder.buildDialog( builder.buildOkCancelActions(),
				"Select matching bindings to services", UISupport.OPTIONS_ICON );

		for(String service : services)
		{
			form.addComboBox(service, new String[0], "Select binding");
			values.put( service, bindings.get(0) );

			dialog.setOptions(service, bindings.toArray());
//			dialog.addComboBox(service, bindings.toArray(), "Select binding");
//			values.put( service, bindings.get(1).toString() );
		}
		return values;
	}
}
