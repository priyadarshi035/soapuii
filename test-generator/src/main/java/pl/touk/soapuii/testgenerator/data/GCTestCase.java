/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.data;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.Operation;
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
	public List<GCXpathAssertion> getXpathAssertions(Operation operationFilter, String shortXpathFilter)
	{
		List<GCXpathAssertion> result = new ArrayList<GCXpathAssertion>();
		for (GCTestStep testStep : getTestSteps())
		{
			Operation stepsOperation = testStep.getOperationStep().getOperation();;
//			System.err.println("filter = " + operationFilter);
//			System.err.println("comparing to = " + stepsOperation);

			if (stepsOperation.getId().equals(operationFilter.getId()))
				result.addAll( testStep.getXpathAssertions(shortXpathFilter) );
		}
		
		return result;
	}

	public List<GCTestStep> getTestSteps()
	{
		return testSteps;
	}

	public void addTestStep(GCTestStep step)
	{
		testSteps.add(step);
	}
	
	public void addTestSteps(List<GCTestStep> addTestSteps)
	{
		testSteps.addAll(addTestSteps);
	}

	@Override
	public String toString()
	{
		return testCase.getName();
	}
}
