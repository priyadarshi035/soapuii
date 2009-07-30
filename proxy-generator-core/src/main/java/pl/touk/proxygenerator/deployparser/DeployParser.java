/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.deployparser;
import org.apache.commons.collections.keyvalue.MultiKey;

/**
 *
 * @author azl
 */
public interface DeployParser
{

/**
	 * Creates xbean file
	 *
	 * @param sourcesPath					Path to sources root folder
 	 * @param additionalPropertiesFileName	Additional properties file name to be writen in xbean.xml
	 * @return Multikey						MultiKey<Document, List<String>, List<String>>, <xbean.xml, provide properties, invoke properties>
	 */
	MultiKey parseDeployXml(String sourcesPath, String additionalPropertiesFileName) throws Exception;
}
