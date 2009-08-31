/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.assertion;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.SwingToolHost;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.impl.swing.SwingFormFactory;
import java.io.File;
import java.util.Map;
import javax.xml.namespace.QName;
import org.junit.*;
import pl.touk.soapuii.testgenerator.AbstractProjectTestCase;
import pl.touk.soapuii.testgenerator.GetCommunicationParser;
import pl.touk.soapuii.testgenerator.data.GCResult;
import pl.touk.soapuii.testgenerator.wsdlbinding.WsdlBindingMapFactory;

/**
 *
 * @author azl
 */
@Ignore
public class AssertionEnablerTest extends AbstractProjectTestCase
{

	public AssertionEnablerTest(String testName) throws Exception
	{
		super(testName, "src/test/resources/testFiles/getCommunicationTest-soapui-project.xml");
		setQuite(true);
		setExpXpathResult("getCommunicationTest");
	}

	/**
	 * Test of attachPathToProject method, of class AssertionEnabler.
	 */
	@Test
	public void testAttachPathToProject() throws Exception
	{
		System.out.println("attachPathToProject");

		File file = new File("src/test/resources/testFiles/");

		WsdlTestSuite suite1 = project.addNewTestSuite("suite3");

		UISupport.setToolHost( new SwingToolHost() );
		XFormFactory.Factory.instance = new SwingFormFactory();

		Map<QName, WsdlInterface> bindingMap = (new WsdlBindingMapFactory()).createBindingMap(project, file);

		GetCommunicationParser getComParser= new GetCommunicationParser();
		GCResult result = getComParser.parseGetCommunications(suite1, file, "listen_uri", "mock_uri", bindingMap);

		AssertionEnabler assertionInstance = new AssertionEnabler(result);


		UISupport.setToolHost( new SwingToolHost() );
		XFormFactory.Factory.instance = new SwingFormFactory();
		assertionInstance.buildDialog();

		// TODO review the generated test code and remove the default call to fail.

	}




}