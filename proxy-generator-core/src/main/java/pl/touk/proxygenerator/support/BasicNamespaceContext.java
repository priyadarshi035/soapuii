/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.support;

import java.util.HashMap;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;

/**
 *
 * @author pnw
 */
public class BasicNamespaceContext implements NamespaceContext
{
	private HashMap<String, String> namespacesMap = new HashMap();

	public void setNemaspace(String prefix, String namespace)
	{
		namespacesMap.put(prefix, namespace);
	}
	public String getNamespaceURI(String prefix)
	{
		return namespacesMap.get(prefix);
	}

	public String getPrefix(String uri)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Iterator getPrefixes(String uri)
	{
		throw new UnsupportedOperationException("Not supported yet.");
	}
};
