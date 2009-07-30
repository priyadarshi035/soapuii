/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.properties;

import java.util.List;
import java.util.Properties;

/**
 *
 * @author pnw
 */
public interface PropertiesGenerator
{
	Properties generatePropertiesFile
			(List<String>  provideProperties, List<String> invokeProperties, String defaultListenUri, String defaultOutputUri);
}
