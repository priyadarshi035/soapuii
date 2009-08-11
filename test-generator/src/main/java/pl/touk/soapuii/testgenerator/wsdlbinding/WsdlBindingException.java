/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.wsdlbinding;

/**
 *
 * @author pnw
 */
public class WsdlBindingException extends Exception
{
	public WsdlBindingException(String msg, Throwable t)
	{
		super(msg, t);
	}
	public WsdlBindingException(String message)
	{
		super(message);
	}
}
