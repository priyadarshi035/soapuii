/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.*;
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

	public void run() throws ProxyGeneratorException
	{
		log.info("Proxy generator started. Sources directory: " + config.getSources());
		generatePackage();
		zipPackage();
		log.info("Proxy generator finished");
	}

	protected void generatePackage() throws ProxyGeneratorException
	{
		if (config.getNoPackage())
			return;
		try
		{
			log.info("Generating package");
			log.info("Creating output directory: " + config.getOutput());
			File outputSUDir = new File(config.getSUPath());
			File outputSADir = new File(config.getSAPath());
			if (outputSADir.exists())
				FileUtils.deleteDirectory(outputSADir);

			outputSUDir.mkdirs();

			File sourcesDir = new File(config.getSources());
			if (!sourcesDir.exists() || !sourcesDir.canRead())
				throw new ProxyGeneratorException("Sources directory " + config.getSources() + " doesn't exists or can't be read");

			log.info("Starting deploy parser");
			MultiKey deployResult = deployParser.parseDeployXml(config.getSources(), config.getPropertiesFile());
			log.info("test");
			if (deployResult == null || deployResult.size() != 3)
				throw new ProxyGeneratorException("Deploy parser returned invalid result");
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
			File SAMetaInfDir = new File(config.getSAMetaInfPath());
			File SUMetaInfDir = new File(config.getSUMetaInfPath());
			if (!SAMetaInfDir.mkdir())
				throw new ProxyGeneratorException("Failed to create Service Assembly META-INF directory: " + config.getSAMetaInfPath());
			if (!SUMetaInfDir.mkdir())
				throw new ProxyGeneratorException("Failed to create Servoce Unit META-INF directory: " + config.getSUMetaInfPath());

			Document SAJbi = metaInfFactory.createServiceAssemblyMetaInf(config.getOutput());
			Document SUJbi = metaInfFactory.createServiceUnitMetaInf(config.getOutput());
			printToFile(SAJbi, SAMetaInfDir, "jbi.xml");
			printToFile(SUJbi, SUMetaInfDir, "jbi.xml");
		} catch (Exception ex)
		{
			throw new ProxyGeneratorException("Failed to generate package", ex);
		}
	}

	protected void zipPackage() throws ProxyGeneratorException
	{
		if (config.getNoZip())
			return;
		log.info("Zipping package");
		
		try
		{
			File inputSA = new File(config.getSAPath());
			log.info("Creating temporary directory");
			File tmpBuild = new File("proxy-generator-build-tmp/" + config.getSAPath());
			tmpBuild.deleteOnExit();
			tmpBuild.mkdir();
			String tmpPath = "proxy-generator-build-tmp/";

			FileUtils.copyDirectory(inputSA, tmpBuild);

			log.info("Zipping HTTP Service Unit");
			String tmpSUPath = tmpPath + config.getSUPath();
			File inputTmpSU = new File(tmpSUPath);
			
			SimpleZip.makeZip(tmpSUPath, tmpSUPath + ".zip", tmpSUPath);
			
			log.info("Removing HTTP Service Unit directory");
			FileUtils.deleteDirectory(inputTmpSU);


			log.info("Zipping Service Assembly");
			String tmpSAPath = tmpPath + config.getSAPath();

			SimpleZip.makeZip(tmpSAPath, config.getSAPath() + ".zip", tmpSAPath);
			
			log.info("Removing temporary directory");
			FileUtils.deleteDirectory(tmpBuild);
		}
		catch (URISyntaxException ex)
		{
			throw new ProxyGeneratorException("URI Error: " + ex);
		}
		catch (FileNotFoundException ex)
		{
			throw new ProxyGeneratorException("File not found: " + ex);
		} catch (IOException ex)
		{
			throw new ProxyGeneratorException("IOException: " + ex);
		}
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
