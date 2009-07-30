/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator;

import java.util.List;
import java.util.Properties;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import pl.touk.proxygenerator.deployparser.DeployParser;
import pl.touk.proxygenerator.deployparser.DeployParserImpl;
import pl.touk.proxygenerator.properties.PropertiesGenerator;
import pl.touk.proxygenerator.properties.PropertiesGeneratorImpl;

/**
 *
 * @author pnw
 */
public class Core
{
	private final static Logger log = Logger.getLogger(Core.class);
	protected Config config;
	protected DeployParser deployParser;
	protected PropertiesGenerator propertiesGenerator;

	public Core(Config config) throws ParserConfigurationException
	{
		deployParser = new DeployParserImpl();
		propertiesGenerator = new PropertiesGeneratorImpl();
		this.config = config;
	}

	public void generateZip()
	{
		log.info("Generating full zipped package");
		generatePackage();
		zipPackage();
	}

	public void generatePackage()
	{
		try
		{
			log.info("Generating package");

			MultiKey deployResult = deployParser.parseDeployXml(config.getSources());
			Document xmlbean = (Document) deployResult.getKey(0);
			List<String> provideProperties = (List<String>) deployResult.getKey(1);
			List<String> invokeProperties = (List<String>) deployResult.getKey(2);
			
			Properties properties = propertiesGenerator.generatePropertiesFile(
					provideProperties, invokeProperties, config.getListenUri(), config.getOutputUri());

			//save xmlbean, properties, move wsdls

		} catch (Exception ex)
		{
			log.error(ex);
		}
	}

	public void zipPackage()
	{
		log.info("Zipping package");
	}

	public Config getConfig() { return config; }
	public void setConfig(Config config) { this.config = config; }
}
