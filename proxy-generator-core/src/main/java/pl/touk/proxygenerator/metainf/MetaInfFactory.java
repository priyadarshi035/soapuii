/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.metainf;

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

/**
 *
 * @author pnw
 */
public interface MetaInfFactory
{
	public Document createServiceAssemblyMetaInf(String projectName) throws ParserConfigurationException;
	public Document createServiceUnitMetaInf(String projectName) throws ParserConfigurationException;
}
