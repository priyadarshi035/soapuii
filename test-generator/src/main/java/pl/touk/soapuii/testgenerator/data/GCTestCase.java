/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.data;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author pnw
 */
public class GCTestCase
{
	protected WsdlTestCase testCase;
	protected List<GCTestStep> testSteps = new ArrayList<GCTestStep>();
	
	public GCTestCase(WsdlTestCase testCase)
	{
		this.testCase = testCase;
	}
	public Collection<GCXpathAssertion> getXpathAssertionsCollection(WsdlOperation operationFilter, String shortXpathFilter)
	{
		List<GCXpathAssertion> result = new ArrayList<GCXpathAssertion>();
		for (GCTestStep testStep : getTestStepCollection())
			if (testStep.getOperation().equals(operationFilter))
				result.addAll( testStep.getXpathAssertionsCollection(shortXpathFilter) );
		
		return result;
	}

	public Collection<GCTestStep> getTestStepCollection()
	{
		return testSteps;
	}

	public void addTestSteps(List<GCTestStep> parseSingleGetCommunication)
	{
		testSteps.addAll(testSteps);
	}

	@Override
	public String toString()
	{
		return testCase.toString();
	}
}
