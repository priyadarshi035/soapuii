/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.data;

import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;

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
}
