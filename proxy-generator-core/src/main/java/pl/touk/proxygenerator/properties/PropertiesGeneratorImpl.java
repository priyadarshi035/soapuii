/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.properties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import pl.touk.proxygenerator.deployparser.DeployParser;
import pl.touk.proxygenerator.deployparser.DeployParserImpl;

/**
 *
 * @author pnw & azl
 *
 * Tworzenie default.properties z default'owymi ustawieniami URI
 */


public class PropertiesGeneratorImpl implements PropertiesGenerator
{
	public DeployParser dp = null;
	private Properties properties = null;

	public PropertiesGeneratorImpl() throws ParserConfigurationException
	{
		 dp = new DeployParserImpl();
		 properties = new Properties();
	}

	private void fillInvokeProperties(List<String> iP, String dLU)
	{
		for ( int i = 1; i <= iP.size(); i++)
			properties.put(iP.get(iP.size()-i ), dLU);
	}

	private void fillProvideProperties(List<String> pP, String dOU)
	{
		for ( int i = 1; i <= pP.size(); i++)
			properties.put(pP.get(pP.size() - i ), dOU);
	}

	public Properties generatePropertiesFile
			(List<String> provideProperties, List<String> invokeProperties, String defaultListenUri, String defaultOutputUri)
	{
		this.fillProvideProperties(provideProperties, defaultListenUri );
		this.fillInvokeProperties(invokeProperties, defaultOutputUri);

		/**
		 * UWAGA. Gdydyba było na odwrót to: odkomentowac 2 linie ponizej, zakomentowac 2 linie powyzej
		 *
		 */

//		this.fillProvideProperties(provideProperties, defaultOutputUri );
//		this.fillInvokeProperties(invokeProperties, defaultListenUri);

		return properties;
	}
//
////getters & setters
//	public Properties getProperties()
//	{
//		return this.properties;
//	}
//	public void setProperties(Properties properties)
//	{
//		this.properties = properties;
//	}
}
