/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.wsdlbinding;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pl.touk.proxygeneratorapi.support.ExtFileFilter;
import pl.touk.proxygeneratorapi.support.SimpleXmlParser;
import pl.touk.soapuii.testgenerator.support.GetCommunicationCommons;


/**
 *
 * @author pnw
 */
public class WsdlBindingMapFactory
{
	protected final static Pattern namespacePrefixPattern = Pattern.compile("(\\w+):(\\w+)");
	protected XFormDialog dialog;

	public Map<QName, WsdlInterface> createBindingMap(WsdlProject project, File dir) throws WsdlBindingException
	{
		Map<String, QName> services = new HashMap();
		if (dir.isDirectory())
			for (File file : dir.listFiles(new ExtFileFilter(".xml")))
				services.putAll(parseGetCommunication(project, file));
		else
			services.putAll(parseGetCommunication(project, dir));

		Map<QName, WsdlInterface> result = matchBindings(project, services);

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
				QName key = GetCommunicationCommons.parseServiceNode(service);
				
				result.put(key.toString(), key);
			}
		}
		catch(Exception ex)
		{
			throw new WsdlBindingException("Failed to parse [" + file + "]", ex);
		}
		return result;
	}

	protected Map<QName, WsdlInterface> matchBindings(WsdlProject project, Map<String, QName> services) throws WsdlBindingException
	{
		//WsdlInterfaceFactory.WSDL_TYPE
		List<AbstractInterface<?>> interfaceList = project.getInterfaces(WsdlInterfaceFactory.WSDL_TYPE);
		if (interfaceList.size() < 1)
			throw new WsdlBindingException("There are no WSDL interfaces in current project");

		List<WsdlInterface> wsdlInterfacesList = new ArrayList();
		for(AbstractInterface iface : interfaceList)
			wsdlInterfacesList.add((WsdlInterface) iface);

		Map<QName, WsdlInterface> result = new HashMap();

		List<String> bindings = new ArrayList();
		for (WsdlInterface binding : wsdlInterfacesList)
			bindings.add(binding.getName());

		StringToStringMap values = buildDialog(services.values(), bindings);

		values = dialog.show(values);
		if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
		{
			Iterator it = values.keySet().iterator();
			while (it.hasNext())
			{
				String key = it.next().toString();
				String val = values.get(key);
				result.put(services.get(key),(WsdlInterface) project.getInterfaceByName(val));
			}
		}
		return result;
	}

	protected class BindingPredicate implements Predicate
	{

		private String value;

		public BindingPredicate(String value)
		{
			this.value = value.toLowerCase();
		}

		public boolean evaluate(Object obj)
		{
			boolean accept = false;
			if (obj instanceof String)
				accept = ((String) obj).toLowerCase().startsWith(value) ||
						 value.startsWith(((String) obj).toLowerCase());
			return accept;
		}
	}

	protected StringToStringMap buildDialog(Collection<QName> services, List<String> bindings)
	{
		StringToStringMap values = new StringToStringMap();

		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Match bindings" );
		XForm form = builder.createForm( "Basic" );

		dialog = builder.buildDialog( builder.buildOkCancelActions(),
				"Select matching bindings to services", UISupport.OPTIONS_ICON );

		for(QName service : services)
		{
			form.addComboBox(service.toString(), new String[0], "Select binding");

			String val = (String) CollectionUtils.find(bindings, new BindingPredicate(service.getLocalPart()));
			val = val == null ? bindings.get(0) : val;
			values.put( service.toString(),  val);

			dialog.setOptions(service.toString(), bindings.toArray());
//			dialog.addComboBox(service, bindings.toArray(), "Select binding");
//			values.put( service, bindings.get(1).toString() );
		}
		return values;
	}
}
