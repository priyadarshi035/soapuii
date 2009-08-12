/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.wsdlbinding;

/**
 *
 * @author pnw
 */
public class BindingMapKey
{
	protected String namespace, name;

	public String getNamespace() { return namespace; }
	public String getName() { return name; }

	public BindingMapKey(String namespace, String name)
	{
		this.name = name;
		this.namespace = namespace;
	}
	
	public boolean equals(BindingMapKey arg0)
	{
		return namespace.equals(arg0.getNamespace()) && name.equals(arg0.getName());
	}

	@Override
	public int hashCode()
	{
		return namespace.hashCode() + name.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final BindingMapKey other = (BindingMapKey) obj;
		if ((this.namespace == null) ? (other.namespace != null) : !this.namespace.equals(other.namespace))
		{
			return false;
		}
		if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
		{
			return false;
		}
		return true;
	}

	@Override
	public String toString()
	{
		return "(" + namespace + ") " + name;
	}


}
