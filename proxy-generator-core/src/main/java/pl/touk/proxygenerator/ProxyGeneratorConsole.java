package pl.touk.proxygenerator;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.touk.proxygenerator.wsdlmap.WsdlMapFactory;
import org.apache.commons.cli.*;

/**
 * Hello world!
 *
 */
public class ProxyGeneratorConsole
{
	protected static Options options = null;

	protected static void fail(String msg)
	{
		System.out.println(msg);
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
./proxy-generator /path/to/wsdls/and/bpels -Doutput=sample_sa -Dlisten-uri=http://0.0.0.0:1234/ala -Doutput-uri=http://0.0.0.1:12345/kot -Dproperties=some.file.proprties -Dno-zip=true

teraz mozliwa reczna edytacja paczki

spakowanie przygotowanej paczki
./proxy-generator /path/to/sample/sa -Doutput=sample_sa -Dzip-only=true
 */
		if (options == null)
		{
			options = new Options();

			Option property = OptionBuilder.withArgName("property=value")
					.hasArgs(2).withValueSeparator()
					.withDescription("use value for given property")
					.create("D");
			Option help = new Option( "help", "print this message" );
			Option nozip = new Option( "nozip", "same as -Dnozip=true" );
			Option ziponly = new Option( "ziponly", "same as -Dziponly=true" );
			
			options.addOption("testwsdlmap", false, "test argument");
			options.addOption(property);
			options.addOption(help);
			options.addOption(nozip);
			options.addOption(ziponly);
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
			System.err.println( "Parsing failed.  Reason: " + ex.getMessage() );
		}

		/*WsdlMapFactory instance = new WsdlMapFactory();
		Map result = instance.createWsdlMap("/home/pnw/wsdl/przykladowy_proces/przykladowy_proces/");
		Iterator it = result.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry pairs = (Map.Entry) it.next();
			System.out.println(pairs.getKey() + " = " + pairs.getValue());
		}*/

		if (cmd.hasOption("help"))
		{
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("proxy-generator", options);
		}
		else
		{
			Properties properties = cmd.getOptionProperties("D");
			String output = properties.getProperty("output");
			if (output == null)
				output = System.getProperty("user.dir");
			String listenuri = properties.getProperty("listen-uri");
			String outputuri = properties.getProperty("output-uri");
			String propertiesfile = properties.getProperty("properties");
			if (propertiesfile == null)
				propertiesfile = "http.uri.properties";

			System.out.println("out: " + output);
			System.out.println("listen: " + listenuri);
			System.out.println("output uri: " + outputuri);
			System.out.println("properties: " + propertiesfile);
			List sourceList = cmd.getArgList();
			for (Iterator i = sourceList.iterator(); i.hasNext();)
			{
				String source = (String) i.next();
				System.out.println(source);
				//doTarget(target);
			}
		}
    }
}
