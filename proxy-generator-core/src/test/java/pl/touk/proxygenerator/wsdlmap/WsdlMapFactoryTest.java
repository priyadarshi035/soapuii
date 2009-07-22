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
public class WsdlMapFactoryTest extends TestCase {
    
    public WsdlMapFactoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

	/**
	 * Test of createWsdlMap method, of class WsdlMapFactory.
	 */
	public void testCreateWsdlMap() {
		System.out.println("createWsdlMap");
		WsdlMapFactory instance = new WsdlMapFactory();
		Map expResult = null;
		Map result = instance.createWsdlMap();
		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

}
