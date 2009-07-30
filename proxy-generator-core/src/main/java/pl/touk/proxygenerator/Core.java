/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
			log.info("Creating output directory: " + config.getOutput());
			File outputDir = new File(config.getOutput());
			if (outputDir.exists())
				if (!outputDir.delete())
					throw new ProxyGeneratorException("Output directory " + config.getOutput() + " already exists and couldn't be deleted");
					
			outputDir.mkdirs();

			log.info("Starting deploy parser");
			MultiKey deployResult = deployParser.parseDeployXml(config.getSources(), config.getPropertiesFile());
			Document xmlbean = (Document) deployResult.getKey(0);
			List<String> provideProperties = (List<String>) deployResult.getKey(1);
			List<String> invokeProperties = (List<String>) deployResult.getKey(2);

			printToFile(xmlbean, outputDir, "xbean.xml");

			log.info("Generating default properties file");
			Properties properties = propertiesGenerator.generatePropertiesFile(
					provideProperties, invokeProperties, config.getListenUri(), config.getOutputUri());

			//printToFile(properties, outputDir, "default.properties");

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
	
	private void printToFile(Document localDoc, File outputDir, String fileName) throws ProxyGeneratorException
	{
		log.info("Saving file: " + fileName + " in: " + outputDir.getPath());
		File xbean = new File(outputDir, fileName);
		if (xbean.exists())
			if (!xbean.delete())
				throw new ProxyGeneratorException("File " + fileName + " already exists and couldn't be deleted");
		
		try
		{
			xbean.createNewFile();
			
			OutputFormat format = new OutputFormat(localDoc);
			format.setIndenting(true);

			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(xbean), format);
			serializer.serialize(localDoc);
		} catch (IOException ie)
		{
			throw new ProxyGeneratorException("File " + fileName + " couldn't be written: " + ie);
		}
	}

	public Config getConfig() { return config; }
	public void setConfig(Config config) { this.config = config; }
}
