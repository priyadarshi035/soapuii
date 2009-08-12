/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator;

import com.eviware.soapui.impl.wsdl.*;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;

/**
 *
 * @author pnw
 */
public class NewTestCaseTestCase extends AbstractProjectTestCase
{
	public NewTestCaseTestCase(String testName) throws Exception
	{
		super(testName);
		setQuite(false);
	}

	public void testNetTestCase() throws Exception
	{
		WsdlTestSuite suite1 = project.addNewTestSuite("suite1");
		WsdlTestCase case1 = suite1.addNewTestCase("case1");

		setXpathExist("/soapui-project/testSuite");
		setExpXpathResult("case1");
		setXpathResult("/soapui-project/testSuite[@name=\"suite1\"]/testCase/@name");

		performAsserts();
	}
}
