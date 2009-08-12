/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.touk.soapuii.testgenerator;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import java.io.File;
import java.util.Map;
import org.junit.*;
import static org.junit.Assert.*;
import org.w3c.dom.Document;
import pl.touk.soapuii.testgenerator.wsdlbinding.BindingMapKey;

/**
 *
 * @author pnw
 */
public class GetCommunicationParserTest extends AbstractProjectTestCase
{

	public GetCommunicationParserTest(String testName) throws Exception
	{
		super(testName, "src/test/resources/testFiles/feature 6-soapui.xml");
		setQuite(false);
		setExpXpathResult("feature 6");
	}

	/**
	 * Test of createWsdlTestRequestStep method, of class GetCommunicationParser.
	 */
	@Test
	public void testCreateWsdlTestRequestStep() throws Exception
	{
		System.out.println("createTestCase");
		File file = new File("src/test/resources/testFiles/getCommunication.xml");

		WsdlTestCase case1 = project.addNewTestSuite("suite1").addNewTestCase("case1");
		
		WsdlInterface iface = (WsdlInterface) project.getInterfaceAt(0);
		WsdlOperation operation = iface.getOperationAt(0);
		TestStepConfig config = WsdlTestRequestStepFactory.createConfig(operation, "teststep1");
		case1.addTestStep(config);

		// TODO review the generated test code and remove the default call to fail.
		performAsserts();
	}
}
