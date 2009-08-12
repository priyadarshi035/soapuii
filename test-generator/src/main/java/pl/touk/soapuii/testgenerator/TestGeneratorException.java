/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator;

import pl.touk.soapuii.testgenerator.wsdlbinding.*;

/**
 *
 * @author pnw
 */
public class TestGeneratorException extends Exception
{
	public TestGeneratorException(String msg, Throwable t)
	{
		super(msg, t);
	}
	public TestGeneratorException(String message)
	{
		super(message);
	}
}
