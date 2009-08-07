/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import org.junit.Before;

/**
 *
 * @author pnw
 */
public class NewTestCaseTestCase extends ProjectTestCase
{
	public NewTestCaseTestCase(String testName) throws Exception
	{
		super(testName);
	}

	@Before
	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		quiet = false;		

		WsdlTestSuite suite1 = project.addNewTestSuite("suite1");
		WsdlTestCase case1 = suite1.addNewTestCase("case1");

		xpathExist = "/soapui-project/testSuite";
		expXpathResult = "case1";
		xpath = "/soapui-project/testSuite[@name=\"suite1\"]/testCase/@name";
		//expProjectResult = Document instance!
	}
}
