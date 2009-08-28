/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.data;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;

/**
 *
 * @author pnw
 */
public class GCXpathAssertion
{
	protected XPathContainsAssertion assertion;
	protected String shortName;
	protected GCTestStep parent;
	protected String defaultValue;

	public GCXpathAssertion(XPathContainsAssertion assertion, String shortName, GCTestStep parent, String defaultValue)
	{
		this.defaultValue = defaultValue;
		this.parent = parent;
		this.assertion = assertion;
		this.shortName = shortName;
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDisabled(boolean flag)
	{
		assertion.setDisabled(flag);
	}
	
	public void setExpcectedContent(String string)
	{
		assertion.setExpectedContent(string);
	}

	@Override
	public String toString()
	{
		return getShortName();
	}

	public String getShortName()
	{
		return shortName;
	}

	public GCTestStep getParent()
	{
		return parent;
	}

	public void setConfig(GCConfig flag)
	{
		switch(flag.getType())
		{
			case DISABLED:
				setDisabled(true);
				setExpcectedContent(flag.getValue());
				break;
			case STATIC:
				setDisabled(false);
				setExpcectedContent(flag.getValue());
				break;
			case CASE:
				setDisabled(false);
				TestCase testCase = getParent().getParentWsdlTestCase();
				if (!testCase.hasProperty(flag.getValue()))
					testCase.setPropertyValue(flag.getValue(), getDefaultValue());
				setExpcectedContent("${#TestCase#" + flag.getValue() + "}");
				break;
			case SUITE:
				TestSuite testSuite = getParent().getParentWsdlTestSuite();
				setDisabled(false);
				if (!testSuite.hasProperty(flag.getValue()))
					testSuite.setPropertyValue(flag.getValue(), getDefaultValue());
				setExpcectedContent("${#TestSuite#" + flag.getValue() + "}");
				break;
		}
	}
}
