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
import org.apache.log4j.Logger;

/**
 * Class containing some data collected during parsing getCommunication file
 * @author pnw
 */
public class GCResult
{
	private Logger log = Logger.getLogger(GCResult.class);
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

	public Collection<GCXpathAssertion> getXpathAssertionsCollection(WsdlOperation operationFilter, String shortXpathFilter)
	{
		System.out.println("getXpathAssertionsCollection [" + operationFilter.getName() + "] [" + shortXpathFilter + "]");
		//log.debug("getXpathAssertionsCollection [" + operationFilter.getName() + "] [" + shortXpathFilter + "]");
		List<GCXpathAssertion> result = new ArrayList<GCXpathAssertion>();
		for (GCTestCase testCase : getTestCaseCollection())
		{
			log.debug("visiting testCase: [" + testCase + "]");
			//result.addAll( testCase.getXpathAssertionsCollection(operationFilter, shortXpathFilter) );
		}
						
		return result;
	}
}
