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

	public void setSimilarXpathAssertions(GCXpathAssertion similarAssertion, GCConfig flag)
	{
		Operation operation = similarAssertion.getParent().getOperation();
		String shortName = similarAssertion.getShortName();
		for(GCXpathAssertion assertion : getXpathAssertions( operation, shortName) )
		{
			switch(flag.getType())
			{
				case DISABLED:
					assertion.setDisabled(true);
					break;
				case STATIC:
					assertion.setDisabled(false);
					assertion.setExpcectedContent(flag.getValue());
					break;
				case CASE:
					assertion.setDisabled(false);
					TestCase testCase = assertion.getParent().getParentWsdlTestCase();
					if (!testCase.hasProperty(flag.getValue()))
						testCase.setPropertyValue(flag.getValue(), assertion.getDefaultValue());
					assertion.setExpcectedContent("${#TestCase#" + flag.getValue() + "}");
					break;
				case SUITE:
					assertion.setDisabled(false);
					if (!testSuite.hasProperty(flag.getValue()))
						testSuite.setPropertyValue(flag.getValue(), assertion.getDefaultValue());
					assertion.setExpcectedContent("${#TestSuite#" + flag.getValue() + "}");
					break;
			}
			
		}
	}
}
