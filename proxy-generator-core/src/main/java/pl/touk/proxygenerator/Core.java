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
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import pl.touk.proxygenerator.deployparser.DeployParser;
import pl.touk.proxygenerator.deployparser.DeployParserImpl;
import pl.touk.proxygenerator.metainf.MetaInfFactory;
import pl.touk.proxygenerator.metainf.MetaInfFactoryImpl;
import pl.touk.proxygenerator.properties.PropertiesGenerator;
import pl.touk.proxygenerator.properties.PropertiesGeneratorImpl;
import pl.touk.proxygenerator.support.CopyFileTraversal;
import pl.touk.proxygenerator.support.ExtFileFilter;

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
	protected MetaInfFactory metaInfFactory;

	public Core(Config config) throws ProxyGeneratorException
	{
		try
		{
			deployParser = new DeployParserImpl();
			propertiesGenerator = new PropertiesGeneratorImpl();
			metaInfFactory = new MetaInfFactoryImpl();
			this.config = config;
		} catch (ParserConfigurationException ex)
		{
			throw new ProxyGeneratorException("Couldn't create deployParser: " + ex);
		}
	}

	public void run()
	{
		if (config.getNoZip())
			generatePackage();
		else if (config.getZipOnly())
			zipPackage();
		else
			generateZip();
	}

	protected void generateZip()
	{
		log.info("Generating full zipped package");
		generatePackage();
		zipPackage();
	}

	protected void generatePackage()
	{
		try
		{
			log.info("Generating package");
			log.info("Creating output directory: " + config.getOutput());
			File outputSUDir = new File(config.getSUDir());
			if (outputSUDir.exists())
				if (!outputSUDir.delete())
					throw new ProxyGeneratorException("Output directory " + config.getSUDir() + " already exists and couldn't be deleted");

			outputSUDir.mkdirs();

			File sourcesDir = new File(config.getSources());
			if (!sourcesDir.exists() || !sourcesDir.canRead())
				throw new ProxyGeneratorException("Sources directory " + config.getSources() + " doesn't exists or can't be read");

			log.info("Starting deploy parser");
			MultiKey deployResult = deployParser.parseDeployXml(config.getSources(), config.getPropertiesFile());
			Document xmlbean = (Document) deployResult.getKey(0);
			List<String> provideProperties = (List<String>) deployResult.getKey(1);
			List<String> invokeProperties = (List<String>) deployResult.getKey(2);

			printToFile(xmlbean, outputSUDir, "xbean.xml");

			log.info("Generating default properties file (not supported yet! :( )");
			//Properties properties = propertiesGenerator.generatePropertiesFile(
					//provideProperties, invokeProperties, config.getListenUri(), config.getOutputUri());

			//printToFile(properties, outputSUDir, "default.properties");

			log.info("Coping wsdl and xsd files");
			FileUtils.copyDirectory(sourcesDir, outputSUDir, new ExtFileFilter(".wsdl"));
			FileUtils.copyDirectory(sourcesDir, outputSUDir, new ExtFileFilter(".xsd"));
			//new CopyFileTraversal(sourcesDir, outputSUDir).traverse(sourcesDir, new ExtFileFilter(".wsdl"));
			//new CopyFileTraversal(sourcesDir, outputSUDir).traverse(sourcesDir, new ExtFileFilter(".xsd"));

			log.info("Creating META-INF");
			File SAMetaInfDir = new File(config.getSAMetaInf());
			File SUMetaInfDir = new File(config.getSUMetaInf());
			if (!SAMetaInfDir.mkdir())
				throw new ProxyGeneratorException("Failed to create Service Assembly META-INF directory: " + config.getSAMetaInf());
			if (!SUMetaInfDir.mkdir())
				throw new ProxyGeneratorException("Failed to create Servoce Unit META-INF directory: " + config.getSUMetaInf());

			Document SAJbi = metaInfFactory.createServiceAssemblyMetaInf(config.getOutput());
			Document SUJbi = metaInfFactory.createServiceUnitMetaInf(config.getOutput());
			printToFile(SAJbi, SAMetaInfDir, "jbi.xml");
			printToFile(SUJbi, SUMetaInfDir, "jbi.xml");
		} catch (Exception ex)
		{
			log.error(ex);
		}
	}

	protected void zipPackage()
	{
		log.info("Zipping package (not supported yet)");
	}

	protected void printToFile(Properties properties, File outputDir, String fileName) throws ProxyGeneratorException
	{
		log.info("Saving file: " + fileName + " in: " + outputDir.getPath());
		File propertiesFile = new File(outputDir, fileName);
		if (propertiesFile.exists())
			if (!propertiesFile.delete())
				throw new ProxyGeneratorException("File " + fileName + " already exists and couldn't be deleted");

		try
		{
			propertiesFile.createNewFile();
			properties.store(new FileOutputStream(propertiesFile), "default properties");
		} catch (IOException ie)
		{
			throw new ProxyGeneratorException("File " + fileName + " couldn't be written: " + ie);
		}
	}
	protected void printToFile(Document localDoc, File outputDir, String fileName) throws ProxyGeneratorException
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
