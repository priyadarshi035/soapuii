/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.touk.soapuii.testgenerator;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import java.io.File;
import org.junit.*;

/**
 *
 * @author pnw
 */
@Ignore
public class GetCommunicationParserTest extends AbstractProjectTestCase
{

	public GetCommunicationParserTest(String testName) throws Exception
	{
		super(testName, "src/test/resources/testFiles/getCommunicationTest-soapui-project.xml");
		setQuite(false);
		setExpXpathResult("getCommunicationTest");
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
		
		WsdlInterface iface = (WsdlInterface) project.getInterfaceByName("CaseRunner");
		WsdlOperation operation = iface.getOperationByName("createCase");
		TestStepConfig config = WsdlTestRequestStepFactory.createConfig(operation, "teststep1");
		case1.addTestStep(config);

		// TODO review the generated test code and remove the default call to fail.
		performAsserts();
	}
}
