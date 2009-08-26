/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.data;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class containing some data collected during parsing getCommunication file
 * @author pnw
 */
public class GCResult
{
	protected WsdlTestSuite testSuite;
	protected List<GCTestCase> testCases = new ArrayList();

	public GCResult(WsdlTestSuite testSuite)
	{
		this.testSuite = testSuite;
	}
	
	/**
	 *
	 * @return Created TestCases
	 */
	public Collection<GCTestCase> getTestCaseCollection()
	{
		return testCases;
	}

	public void addTestCase(GCTestCase createTestCase)
	{
		testCases.add(createTestCase);
	}

	public Collection<GCXpathAssertion> getXpathAssertionsCollection(WsdlOperation operationFilter, String shortXpath)
	{
		throw new UnsupportedOperationException();
	}
}
