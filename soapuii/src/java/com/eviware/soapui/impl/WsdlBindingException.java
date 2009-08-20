/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eviware.soapui.impl;

/**
 *
 * @author azl
 */
public class WsdlBindingException extends Exception
{
	public WsdlBindingException(String message)
	{
		super(message);
	}

	public WsdlBindingException(String message, Throwable t)
	{
		super(message, t);
	}
}
