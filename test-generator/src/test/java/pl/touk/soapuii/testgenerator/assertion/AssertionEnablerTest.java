/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.assertion;

import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.SwingToolHost;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.impl.swing.SwingFormFactory;
import java.io.IOException;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author azl
 */
public class AssertionEnablerTest {

    public AssertionEnablerTest() {
    }

	@BeforeClass
	public static void setUpClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownClass() throws Exception
	{
	}

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

	/**
	 * Test of attachPathToProject method, of class AssertionEnabler.
	 */
	@Test
	public void testAttachPathToProject() throws AssertionEnablerException
	{
		System.out.println("attachPathToProject");
		String projectPath = "src/test/resources/testFiles/getCommunicationTest-soapui-project.xml";

		AssertionEnabler assertionInstance = new AssertionEnabler();


		try
		{
			assertionInstance.attachPathToProject(projectPath);
			UISupport.setToolHost( new SwingToolHost() );
			XFormFactory.Factory.instance = new SwingFormFactory();
			assertionInstance.buildDialog();

		}
		catch (XmlException ex)
		{
			throw new AssertionEnablerException("cannot generate UI", ex);
		}
		catch (IOException ex)
		{
			throw new AssertionEnablerException("cannot generate UI", ex);
		}
		catch (SoapUIException ex)
		{
			throw new AssertionEnablerException("cannot generate UI", ex);
		}

		// TODO review the generated test code and remove the default call to fail.

	}




}