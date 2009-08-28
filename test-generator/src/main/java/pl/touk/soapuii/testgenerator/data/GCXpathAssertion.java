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
	private XPathContainsAssertion assertion;
	private String shortName;
	private GCTestStep parent;
	private String defaultValue;
	private GCConfig config;

	public GCXpathAssertion(XPathContainsAssertion assertion, String shortName, GCTestStep parent, String defaultValue)
	{
		this.defaultValue = defaultValue;
		this.parent = parent;
		this.assertion = assertion;
		this.shortName = shortName;
		config = new GCConfig(GCConfig.Type.DISABLED, defaultValue);
	}

	public String getDefaultValue()
	{
		return defaultValue;
	}

	public void setDisabled(boolean flag) { assertion.setDisabled(flag); }

	public String getExpcectedContent() { return assertion.getExpectedContent(); }

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

	public GCConfig getConfig() { return config; }

	public void setConfig(GCConfig config)
	{
		this.config = config;
		switch(config.getType())
		{
			case DISABLED:
				setDisabled(true);
				setExpcectedContent(config.getValue());
				break;
			case STATIC:
				setDisabled(false);
				setExpcectedContent(config.getValue());
				break;
			case CASE:
				setDisabled(false);
				TestCase testCase = getParent().getParentWsdlTestCase();
				if (!testCase.hasProperty(config.getValue()))
					testCase.setPropertyValue(config.getValue(), getDefaultValue());
				setExpcectedContent("${#TestCase#" + config.getValue() + "}");
				break;
			case SUITE:
				TestSuite testSuite = getParent().getParentWsdlTestSuite();
				setDisabled(false);
				if (!testSuite.hasProperty(config.getValue()))
					testSuite.setPropertyValue(config.getValue(), getDefaultValue());
				setExpcectedContent("${#TestSuite#" + config.getValue() + "}");
				break;
		}
	}
}
