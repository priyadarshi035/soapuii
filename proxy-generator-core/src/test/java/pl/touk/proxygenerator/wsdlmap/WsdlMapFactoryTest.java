/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.wsdlmap;

import java.util.Map;
import junit.framework.TestCase;

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
			Map expResult = null;
			Map result = instance.createWsdlMap("/home/pnw/wsdl/przykladowy_proces/przykladowy_proces/");
			assertEquals(expResult, result);
		}
		catch (Exception ex)
		{
			System.err.println(ex);
		}

	}
}
