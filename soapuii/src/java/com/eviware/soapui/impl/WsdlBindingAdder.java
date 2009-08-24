/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eviware.soapui.impl;
import java.io.InputStream;

/**
 *
 * @author azl
 */
public interface WsdlBindingAdder
{
/**
	 * Checks if incoming wsdl file has binding and if not adds a default one
	 *
	 * @param wsdlFile					Input stream with wsdl file to check
 	 * @return InputStream			  Input stream with modified wsdl file
	 */
	InputStream addBindingToWsdl(InputStream wsdlFile) throws WsdlBindingException;
}
