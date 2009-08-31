/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.assertion;

/**
 *
 * @author azl
 */
public class AssertionEnablerException extends Exception
{

	public AssertionEnablerException(String msg, Throwable t)
	{
		super(msg, t);
	}

	public AssertionEnablerException(String message)
	{
		super(message);
	}
}
