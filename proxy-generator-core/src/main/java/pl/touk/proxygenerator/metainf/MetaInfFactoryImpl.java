/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.metainf;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author pnw
 */
public class MetaInfFactoryImpl implements MetaInfFactory
{

	public Document createServiceAssemblyMetaInf(String projectName) throws ParserConfigurationException
	{
		/*
		<?xml version="1.0" encoding="UTF-8"?>
		<jbi xmlns="http://java.sun.com/xml/ns/jbi" version="1.0">
			<service-assembly>
				<identification>
					<name>przykladowe_proxy-sa</name>
					<description>aaa</description>
				</identification>
				<service-unit>
					<identification>
						<name>przykladowe_proxy-http-su</name>
						<description>aaa</description>
					</identification>
					<target>
						<artifacts-zip>przykladowe_proxy-http-su.zip</artifacts-zip>
						<component-name>servicemix-http</component-name>
					</target>
				</service-unit>
			</service-assembly>
		</jbi>
		 */
		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
		Document result = domBuilder.newDocument();
		Element jbi = result.createElement("jbi");
		jbi.setAttribute("xmlns", "http://java.sun.com/xml/ns/jbi");
		jbi.setAttribute("version", "1.0");

		Element service_assembly = result.createElement("service-assembly");
		
		Element identification = result.createElement("identification");
		Element name = result.createElement("name");
		name.appendChild(result.createTextNode(projectName + "-sa"));
		identification.appendChild(name);
		Element description = result.createElement("description");
		description.appendChild(result.createTextNode(projectName + " Service Assembly"));
		identification.appendChild(description);

		service_assembly.appendChild(identification);

		Element service_unit = result.createElement("service-unit");

		Element suIdentification = result.createElement("identification");
		Element suName = result.createElement("name");
		suName.appendChild(result.createTextNode(projectName + "-http-su"));
		suIdentification.appendChild(suName);
		Element suDescription = result.createElement("description");
		suDescription.appendChild(result.createTextNode(projectName + " HTTP Service Unit"));
		suIdentification.appendChild(suDescription);

		service_unit.appendChild(suIdentification);

		Element target = result.createElement("target");
		Element artifacts_zip = result.createElement("artifacts-zip");
		artifacts_zip.appendChild(result.createTextNode(projectName + "-http-su.zip"));
		target.appendChild(artifacts_zip);
		Element component_name = result.createElement("component-name");
		component_name.appendChild(result.createTextNode("servicemix-http"));
		target.appendChild(component_name);

		service_unit.appendChild(target);
		service_assembly.appendChild(service_unit);
		jbi.appendChild(service_assembly);
		result.appendChild(jbi);

		return result;
	}

	public Document createServiceUnitMetaInf(String projectName) throws ParserConfigurationException
	{
		/*
		 <?xml version="1.0" encoding="UTF-8"?>
		<jbi xmlns="http://java.sun.com/xml/ns/jbi" version="1.0">
		  <services binding-component="false"/>
		</jbi>
		 */
		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
		Document result = domBuilder.newDocument();
		Element jbi = result.createElement("jbi");
		jbi.setAttribute("xmlns", "http://java.sun.com/xml/ns/jbi");
		jbi.setAttribute("version", "1.0");

		Element services = result.createElement("services");
		services.setAttribute("binding-component", "false");

		jbi.appendChild(services);
		result.appendChild(jbi);

		return result;
	}

}
