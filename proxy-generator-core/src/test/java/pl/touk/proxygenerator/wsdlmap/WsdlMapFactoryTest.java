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
			WsdlMapFactoryImpl instance = new WsdlMapFactoryImpl();
			MultiKey key = new MultiKey("HelloWorld2", "helloPartnerLink", WsdlMapFactoryImpl.MYROLE);
			Map<MultiKey, String> result = instance.createWsdlMap("src/test/resources/bpel/HelloWorld2/");
			System.out.println(result);
			String path = result.get(key);
			assertTrue(path != null);
			URI expectedUri = new URI("src/test/resources/bpel/HelloWorld2/HelloWorld2.wsdl");
			System.out.println(path);
			assertTrue(path.equals(expectedUri.toString()));
		}
		catch (Exception ex)
		{
			System.err.println(ex);
			fail("exception occured");
		}

	}
}
