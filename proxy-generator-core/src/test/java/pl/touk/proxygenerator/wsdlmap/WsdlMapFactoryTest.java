/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.wsdlmap;

import java.net.URI;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.commons.collections.keyvalue.MultiKey;

/**
 *
 * @author pnw
 */
public class WsdlMapFactoryTest extends TestCase
{
    
    public WsdlMapFactoryTest(String testName)
	{
        super(testName);
    }

    protected void setUp() throws Exception
	{
        super.setUp();
    }

    protected void tearDown() throws Exception
	{
        super.tearDown();
    }

	/**
	 * Test of createWsdlMap method, of class WsdlMapFactory.
	 */
	public void testCreateWsdlMap()
	{
		try 
		{
			System.out.println("createWsdlMap");
			WsdlMapFactory instance = new WsdlMapFactory();
			MultiKey key = new MultiKey("HelloWorld2", "helloPartnerLink", WsdlMapFactory.MYROLE);
			Map<MultiKey, String> result = instance.createWsdlMap("src/test/resources/bpel/HelloWorld2/");
			String path = result.get(key);
			assertTrue(path != null);
			URI absUri = new URI(path);
			URI expectedUri = new URI("src/test/resources/bpel/HelloWorld2/HelloWorld2.wsdl");
			URI userDir = new URI(System.getProperty("user.dir"));
			URI endResult = userDir.relativize(absUri);
			assertTrue(endResult.equals(expectedUri));
		}
		catch (Exception ex)
		{
			System.err.println(ex);
		}

	}
}
