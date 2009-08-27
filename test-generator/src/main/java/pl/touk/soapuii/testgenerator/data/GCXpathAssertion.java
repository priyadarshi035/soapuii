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

	public GCXpathAssertion(XPathContainsAssertion assertion, String shortName, GCTestStep parent)
	{
		this.parent = parent;
		this.assertion = assertion;
		this.shortName = shortName;
	}

	public void setDisabled(boolean flag)
	{
		assertion.setDisabled(flag);
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
