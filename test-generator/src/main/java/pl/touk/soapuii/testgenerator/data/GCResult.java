/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.data;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.testsuite.TestCase;
import java.util.ArrayList;
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
	public List<GCTestCase> getTestCases()
	{
		return testCases;
	}

	public void addTestCase(GCTestCase createTestCase)
	{
		testCases.add(createTestCase);
	}

	public List<GCXpathAssertion> getXpathAssertions(Operation operationFilter, String shortXpathFilter)
	{
//		System.err.println("getXpathAssertionsCollection [" + operationFilter.getName() + "] [" + shortXpathFilter + "]");
		//log.debug("getXpathAssertionsCollection [" + operationFilter.getName() + "] [" + shortXpathFilter + "]");
		List<GCXpathAssertion> result = new ArrayList<GCXpathAssertion>();
		for (GCTestCase testCase : getTestCases())
			result.addAll( testCase.getXpathAssertions(operationFilter, shortXpathFilter) );
						
		return result;
	}

	public List<GCXpathAssertion> getXpathAssertions(GCXpathAssertion similarAssertion)
	{
		return getXpathAssertions(similarAssertion.getParent().getOperation(), similarAssertion.getShortName());
	}

	public void setSimilarXpathAssertions(GCXpathAssertion similarAssertion, GCConfig config)
	{
		Operation operation = similarAssertion.getParent().getOperation();
		String shortName = similarAssertion.getShortName();
		for(GCXpathAssertion assertion : getXpathAssertions( operation, shortName) )
			assertion.setConfig(config);
	}
}
