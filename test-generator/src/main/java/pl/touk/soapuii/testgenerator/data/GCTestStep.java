/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.data;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.testsuite.OperationTestStep;
import com.eviware.soapui.model.testsuite.TestCase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author pnw
 */
public class GCTestStep
{
	protected List<GCXpathAssertion> assertions = new ArrayList<GCXpathAssertion>();
	protected OperationTestStep step;
	
	public GCTestStep(OperationTestStep step)
	{
		this.step = step;
	}

	public List<GCXpathAssertion> getXpathAssertions()
	{
		return assertions;
	}
	public List<GCXpathAssertion> getXpathAssertions(String shortXpathFilter)
	{
		List<GCXpathAssertion> result = new ArrayList<GCXpathAssertion>();
		for (GCXpathAssertion assertion : getXpathAssertions() )
			if (assertion.getShortName().equals(shortXpathFilter))
				result.add(assertion);
		return result;
	}

	public TestCase getParentWsdlTestCase()
	{
		return step.getTestCase();
	}

	public void addAssertions(List<GCXpathAssertion> addAssertions)
	{
		assertions.addAll(addAssertions);
	}

	public Operation getOperation()
	{
		return getOperationStep().getOperation();
	}

	public OperationTestStep getOperationStep()
	{
		return step;
	}

	@Override
	public String toString()
	{
		return step.getName();
	}
}
