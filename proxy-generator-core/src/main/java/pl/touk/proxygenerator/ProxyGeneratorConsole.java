package pl.touk.proxygenerator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.touk.proxygenerator.wsdlmap.WsdlMapFactoryImpl;
import org.apache.commons.cli.*;

/**
 * Hello world!
 *
 */
public class ProxyGeneratorConsole
{
	protected static Options options = null;
	private static final String listenUriCmd = "luri";
	private static final String outputUriCmd = "ouri";
	private static final String outputCmd = "o";
	private static final String propertiesCmd = "p";
	private static final String nozipCmd = "nz";
	private static final String ziponlyCmd = "z";
	private static final String longListenUriCmd = "listenuri";
	private static final String longOutputUriCmd = "outputuri";
	private static final String longOutputCmd = "output";
	private static final String longPropertiesCmd = "properties";
	private static final String longNozipCmd = "nozip";
	private static final String longZiponlyCmd = "ziponly";

	private static final String usageMsg = "proxy-generator [sources_directory]";
	private static final String headerMsg = "sources_directory is a path to a directory with sources (deploy.xml, bpel files, wsdl files, scheme files)";


	protected static void fail(String msg)
	{
		System.err.println(msg);
		System.exit(1);
	}

	public ProxyGeneratorConsole()
	{
		options = getCmdOptions();
	}

	protected static Options getCmdOptions()
	{
/*
 ./proxy-generator /path/to/wsdls/and/bpels -Doutput=sample_sa -Dlisten-uri=http://0.0.0.0:1234/ala -Doutput-uri=http://0.0.0.1:12345/kot -Dproperties=some.file.proprties

albo

przygotowanie paczki:
/path/to/wsdls/and/bpels -o sample_sa -luri http://0.0.0.0:1234/ala -ouri http://0.0.0.1:12345/kot -p some.file.proprties -nz

teraz mozliwa reczna edytacja paczki

spakowanie przygotowanej paczki
./proxy-generator /path/to/sample/sa -Doutput=sample_sa -Dzip-only=true
 */
		if (options == null)
		{
			options = new Options();

			Option output = OptionBuilder.withArgName("NAME")
					.hasArg()
					.withLongOpt(longOutputCmd)
					.withDescription("output file (will be \"NAME.zip\") or folder name").create(outputCmd);
			Option listenUri = OptionBuilder.withArgName("URI")
					.hasArg()
					.withLongOpt(longListenUriCmd)
					.withDescription("default listening URI for proxy server").create(listenUriCmd);
			Option outputUri = OptionBuilder.withArgName("URI")
					.hasArg()
					.withLongOpt(longOutputUriCmd)
					.withDescription("default output URI for proxy server").create(outputUriCmd);
			Option properties = OptionBuilder.withArgName("NAME")
					.hasArg()
					.withLongOpt(longPropertiesCmd)
					.withDescription("name of properties file, in which services " +
					"addresses will be defined (default http.uri.properties)").create(propertiesCmd);
			Option dnozip = OptionBuilder
					.withLongOpt(longNozipCmd)
					.withDescription("if true, output will be left as uncompressed package").create(nozipCmd);
			Option dziponly = OptionBuilder
					.withLongOpt(longZiponlyCmd)
					.withDescription("if true, only pre-made package will be compressed (without creating this package)").create(ziponlyCmd);
					
			Option help = new Option( "help", "print this message" );
			//Option nozip = new Option( "nozip", "same as -Dnozip=true" );
			//Option ziponly = new Option( "ziponly", "same as -Dziponly=true" );
			options.addOption(output);
			options.addOption(listenUri);
			options.addOption(outputUri);
			options.addOption(properties);
			options.addOption(dnozip);
			options.addOption(dziponly);
			
			options.addOption(help);
			//options.addOption(nozip);
			//options.addOption(ziponly);
		}
		return options;
	}

    public static void main( String[] args )
    {
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try
		{
			cmd = parser.parse(getCmdOptions(), args);
		} catch (ParseException ex)
		{
			fail( "Parsing failed.  Reason: " + ex.getMessage() );
		}

		/*WsdlMapFactory instance = new WsdlMapFactory();
		Map result = instance.createWsdlMap("/home/pnw/wsdl/przykladowy_proces/przykladowy_proces/");
		Iterator it = result.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry pairs = (Map.Entry) it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}*/

		if (cmd.hasOption("help") || cmd.getArgList().isEmpty())
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(usageMsg, headerMsg, options, null, true);
			//formatter.printHelp("proxy-generator [options] [target [target2 [target3] ...]]", options);
		}
		else
		{
/*			Properties properties = cmd.getOptionProperties("D");
			String output = properties.getProperty(outputCmd);
			if (output == null)
				output = System.getProperty("user.dir");
			String listenuri = properties.getProperty(listenUriCmd);
			String outputuri = properties.getProperty(outputUriCmd);
			String propertiesfile = properties.getProperty(propertiesCmd);
			if (propertiesfile == null)
				propertiesfile = "http.uri.properties";*/
			String output = cmd.getOptionValue(outputCmd, System.getProperty("user.dir"));
			String listenuri = cmd.getOptionValue(listenUriCmd);
			String outputuri = cmd.getOptionValue(outputUriCmd);
			String propertiesfile = cmd.getOptionValue(propertiesCmd, "http.uri.properties");

			boolean ziponly = cmd.hasOption(ziponlyCmd);
			boolean nozip = cmd.hasOption(nozipCmd);
			if (ziponly && nozip)
				fail(ziponlyCmd + " and " + nozipCmd + " flags cannot be set simultaneously");

			System.out.println("out: " + output);
			System.out.println("listen: " + listenuri);
			System.out.println("output uri: " + outputuri);
			System.out.println("properties: " + propertiesfile);
			List sourceList = cmd.getArgList();
			if (sourceList.size() != 1)
				fail("Missing (or too much) sources_directory argument: " + sourceList);
			System.out.println(sourceList.get(0));

			//doTarget(target);
		}
    }
}
