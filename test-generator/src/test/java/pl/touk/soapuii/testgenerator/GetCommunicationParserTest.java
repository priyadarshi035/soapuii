/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.touk.soapuii.testgenerator;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.SwingToolHost;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.impl.swing.SwingFormFactory;
import java.io.File;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.junit.*;
import pl.touk.soapuii.testgenerator.data.GCConfig;
import pl.touk.soapuii.testgenerator.data.GCResult;
import pl.touk.soapuii.testgenerator.data.GCTestCase;
import pl.touk.soapuii.testgenerator.data.GCTestStep;
import pl.touk.soapuii.testgenerator.data.GCXpathAssertion;
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
		System.out.println("testCreateWsdlTestRequestStep");
		File file = new File("src/test/resources/testFiles/");

		WsdlTestSuite suite1 = project.addNewTestSuite("suite3");
		WsdlInterface iface = (WsdlInterface) project.getInterfaceByName("CaseRunner");
		WsdlOperation operation = iface.getOperationByName("createCase");
		
		UISupport.setToolHost( new SwingToolHost() );
		XFormFactory.Factory.instance = new SwingFormFactory();

		Map<QName, WsdlInterface> bindingMap = (new WsdlBindingMapFactory()).createBindingMap(project, file);

		GetCommunicationParser getComParser= new GetCommunicationParser();
		GCResult result = getComParser.parseGetCommunications(suite1, file, "listen_uri", "mock_uri", bindingMap);
		GCTestCase testCase = result.getTestCases().get(0);
		GCTestStep step = testCase.getTestSteps().get(0);
		GCXpathAssertion assertion = step.getXpathAssertions().get(0);

		result.setSimilarXpathAssertions(assertion, new GCConfig(GCConfig.Type.SUITE, "caseId"));
		result.setSimilarXpathAssertions(step.getXpathAssertions().get(1), new GCConfig(GCConfig.Type.CASE, "msisdn"));

//		List<GCXpathAssertion> xpaths = result.getXpathAssertions(operation, "/createCaseResponse/caseId");
		List<GCXpathAssertion> xpaths = result.getXpathAssertions(assertion);

		System.out.println("found xpaths: " + xpaths.size());
		project.saveIn(new File("test_project3.xml"));
	}
}
