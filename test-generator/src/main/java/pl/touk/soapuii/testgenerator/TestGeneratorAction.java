package pl.touk.soapuii.testgenerator;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * 
 *
 */
public class TestGeneratorAction extends AbstractSoapUIAction<WsdlProject>
{
	public TestGeneratorAction()
	{
		super( "Demo Action", "Demonstrates an extension to soapUI" );
	}

	public void perform( WsdlProject target, Object param )
	{
		UISupport.showInfoMessage( "Welcome to my action in project [" + target.getName() + "]" );
	}
}