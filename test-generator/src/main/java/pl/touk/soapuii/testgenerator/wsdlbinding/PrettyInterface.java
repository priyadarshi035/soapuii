/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.wsdlbinding;

import com.eviware.soapui.model.iface.Interface;

/**
 *
 * @author pnw
 */
public class PrettyInterface
{
	protected Interface binding;

	PrettyInterface(Interface binding)
	{
		this.binding = binding;
	}

	public void setInterface(Interface binding)
	{
		this.binding = binding;
	}

	public Interface getInterface()
	{
		return binding;
	}

	@Override
	public String toString()
	{
		return binding.getName();
	}
	
	@Override
	public boolean equals(Object arg0)
	{
		return binding.equals(arg0);
	}

	@Override
	public int hashCode()
	{
		return binding.hashCode();
	}
}
