/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.data;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.testsuite.OperationTestStep;
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

	public Collection<GCXpathAssertion> getXpathAssertionCollection()
	{
		return assertions;
	}

	public void addAssertions(List<GCXpathAssertion> addAssertions)
	{
		assertions.addAll(addAssertions);
	}
}
