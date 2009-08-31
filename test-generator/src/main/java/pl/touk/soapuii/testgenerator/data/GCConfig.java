/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.data;

/**
 *
 * @author pnw
 */
public class GCConfig
{
	public enum Type
	{
		DISABLED, STATIC, SUITE, CASE;
	}

	protected Type type;
	protected String value;

	public GCConfig(Type type, String value)
	{
		this.type = type;
		this.value = value;
	}

	public Type getType() { return type; }
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
}
