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
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.apache.commons.collections.keyvalue.MultiKey;
import pl.touk.proxygenerator.deployparser.DeployParser;
import pl.touk.proxygenerator.deployparser.DeployParserImpl;

/**
 *
 * @author azl
 */
public class PropertiesGeneratorImplTest extends TestCase {
    
    public PropertiesGeneratorImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

	private void printToFile(Properties tempP)
	{
		File tempF = new File("default.properties");
		OutputStream tempO;

		try {
			tempO = new FileOutputStream(tempF);
			tempP.store(tempO, null);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(PropertiesGeneratorImpl.class.getName()).log(Level.SEVERE, null, ex);
			throw new UnsupportedOperationException("Not supported yet.");
		} catch (IOException ex) {
			Logger.getLogger(PropertiesGeneratorImpl.class.getName()).log(Level.SEVERE, null, ex);
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

	private Properties generateExpProperty()
	{
		Properties result = new Properties();
		result.put("${pl.ode.bpel.unit-test.wsdl.HelloService}", "http://0.0.0.0:1234/ala");
		return result;
	}

	/**
	 * Test of generatePropertiesFile method, of class PropertiesGeneratorImpl.
	 */
	public void testGeneratePropertiesFile() throws ParserConfigurationException, Exception {
		System.out.println("generatePropertiesFile");

			DeployParser dp = new DeployParserImpl();
//			String sourcesPath = "src/test/resources/bpel/przykladowy_proces/";
			String sourcesPath = "src/test/resources/bpel/HelloWorld2/";
			String additionalProperitesFileName = "src/test/resources/bpel/HelloWorld2/";

			MultiKey result = dp.parseDeployXml(sourcesPath, additionalProperitesFileName);

		PropertiesGenerator instance = new PropertiesGeneratorImpl();

		ArrayList<String> provideProperties = new ArrayList<String>();
		ArrayList<String> invokeProperties = new ArrayList<String>();
		provideProperties = (ArrayList<String>)result.getKey(1);
		invokeProperties = (ArrayList<String>)result.getKey(2);

		String dLU = "http://0.0.0.0:1234/ala";
		String dOU = "http://0.0.0.0:wymyslmyjakisportinnynizwyzej/nazwaplikuzpunktu2";

		Properties tempP = instance.generatePropertiesFile(provideProperties, invokeProperties, dLU, dOU);
//		printToFile(tempP);
		Properties expResult = generateExpProperty();

		assertEquals(expResult, tempP);
	}
}
