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
	boolean MYROLE = true;
	boolean PARTNERROLE = false;

/**
	 * Creates xbean file
	 *
	 * @param sourcesPath	Path to sources root folder
	 * @return Multikey		MultiKey<Document, List<String>, List<String>>, <xbean.xml, provide properties, invoke properties>
	 */
	MultiKey parseDeployXml(String sourcesPath) throws Exception;
}
