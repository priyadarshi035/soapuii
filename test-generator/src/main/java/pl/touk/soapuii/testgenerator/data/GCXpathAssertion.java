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

	public GCXpathAssertion(XPathContainsAssertion assertion, String shortName)
	{
		this.assertion = assertion;
		this.shortName = shortName;
	}

	@Override
	public String toString()
	{
		return shortName;
	}
}
