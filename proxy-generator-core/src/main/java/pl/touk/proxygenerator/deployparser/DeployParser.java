/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.deployparser;
import java.io.File;
import java.util.Map;
import org.apache.commons.collections.keyvalue.MultiKey;

/**
 *
 * @author azl
 */
public interface DeployParser
{
	boolean MYROLE = true;
	boolean PARTNERROLE = false;

/**
	 * Creates xbean file
	 *
	 * @param fileToParse	file to be parsed into xbean file. By default it is deploy.xml
	 * @param wsdlMap		map of WsdlMap. key  - MultiKey<processName, partnerLinkName, role (bool)>
	 * @return file				 parsed xml file with xbean
	 */
	File parseDeployXml(File fileToParse, String path) throws Exception;
}
