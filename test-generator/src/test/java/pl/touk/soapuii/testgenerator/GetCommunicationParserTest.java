/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.touk.soapuii.testgenerator;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.SwingToolHost;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.impl.swing.SwingFormFactory;
import java.io.File;
import java.util.Map;
import javax.xml.namespace.QName;
import org.junit.*;
import org.w3c.dom.Document;
import pl.touk.proxygeneratorapi.support.SimpleXmlParser;
import pl.touk.soapuii.testgenerator.wsdlbinding.WsdlBindingMapFactory;

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
		setQuite(true);
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

		UISupport.setToolHost( new SwingToolHost() );
		XFormFactory.Factory.instance = new SwingFormFactory();

		Map<QName, WsdlInterface> bindingMap = (new WsdlBindingMapFactory()).createBindingMap(project, file);

		GetCommunicationParser gCP= new GetCommunicationParser();


		Document doc = SimpleXmlParser.parse(file, false);

//		gCP.parseSingleGetCommunication(case1, doc, bindingMap);


		// TODO review the generated test code and remove the default call to fail.
		performAsserts();
	}
}
