/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.wsdlmap;

import java.util.Map;
import org.apache.commons.collections.keyvalue.MultiKey;

/**
 *
 * @author pnw
 */
public interface WsdlMapFactory {
	boolean MYROLE = true;
	boolean PARTNERROLE = false;
	
	/**
	 * Creates map
	 *
	 * @param path   path to directory containing *.wsdl and *.bpel files
	 * @return       map, value - relative path to *.wsdl file. key - MultiKey<processName, partnerLinkName, role (bool)>
	 */
	Map<MultiKey, String> createWsdlMap(String path) throws WsdlMapException;

}
