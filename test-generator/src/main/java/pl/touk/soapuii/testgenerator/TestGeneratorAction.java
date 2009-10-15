package pl.touk.soapuii.testgenerator;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import pl.touk.soapuii.testgenerator.wsdlbinding.WsdlBindingMapFactory;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
//import com.sun.org.apache.xml.internal.serialize.OutputFormat.Defaults;
import java.io.File;
import java.util.Map;
import javax.xml.namespace.QName;
import pl.touk.proxygeneratorapi.Defaults;
import pl.touk.soapuii.testgenerator.assertion.AssertionEnabler;
import pl.touk.soapuii.testgenerator.data.GCResult;

/**
 * 
 * @author pnw
 */
public class TestGeneratorAction extends AbstractSoapUIAction<WsdlTestSuite>
{
	public static final String SOAPUI_ACTION_ID = "GenerateTestsAction";
	private XFormDialog dialog;
	private XForm mainForm;

	public static final MessageSupport messages = MessageSupport.getMessages( TestGeneratorAction.class );

	public TestGeneratorAction()
	{
		super( "Generate Tests", "Generates tests form getCommunication dumps" );
	}

	public void perform( WsdlTestSuite testSuite, Object param )
	{
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );
			dialog.setValue( Form.ASSERTION, Boolean.toString( true ) );
			dialog.setValue( Form.MOCKURI, Defaults.outputUri );
			dialog.setValue( Form.ODELISTENURI, Defaults.listenUri.replace("http://0.0.0.0", "http://localhost") );
//			dialog.getFormField( Form.INITIALCOMMUNICATION ).addFormFieldListener( new XFormFieldListener()
//			{
//				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
//				{
//					String value = newValue.toLowerCase().trim();
//
//					dialog.getFormField( Form.ASSERTION ).setEnabled( value.length() > 0 );
//				}
//			} );
		}
		else
		{
			dialog.setValue( Form.INITIALCOMMUNICATION, "" );
			dialog.getFormField( Form.ASSERTION ).setEnabled( true );
		}

		while( dialog.show() )
		{
			try
			{
				String url = dialog.getValue( Form.INITIALCOMMUNICATION );
				if( StringUtils.hasContent( url ) )
				{
				    url = url.trim();
					String expUrl = PathUtils.expandPath( url, testSuite.getProject() );
					File file = new File (expUrl);

					if( file.exists() )
					{
						try
						{
							Map<QName, WsdlInterface> bindingMap = new WsdlBindingMapFactory().createBindingMap(testSuite.getProject(), file);
							if (bindingMap.isEmpty())
								//throw new TestGeneratorException("Empty binding map. Generation failed.");
								throw new TestGeneratorException("Generation canceled.");
							
							GCResult result = new GetCommunicationParser().parseGetCommunications(
									testSuite, file, dialog.getValue(Form.ODELISTENURI),
									dialog.getValue(Form.MOCKURI), bindingMap);
							if (dialog.getBooleanValue( Form.ASSERTION))
								(new AssertionEnabler(result)).show();
						}
						catch (TestGeneratorException ex)
						{
							UISupport.showErrorMessage(ex);
						}
					}
					else
						UISupport.showErrorMessage( dialog.getValue( Form.FILENOTFOUND ) );

					break;
				}
			}
			catch( Exception ex )
			{
				UISupport.showErrorMessage( ex );
			}
		}
	}


	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWPROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		public final static String FILENOTFOUND = messages.get( "Form.FileNotFound" );

		@AField( description = "Form.GetCommunication.Description", type = AFieldType.FILE_OR_FOLDER )
		public final static String INITIALCOMMUNICATION = messages.get( "Form.GetCommunication.Label" );

		@AField( description = "Form.OdeListenUri.Description", type = AFieldType.STRING )
		public final static String ODELISTENURI = messages.get( "Form.OdeListenUri.Label" );

		@AField( description = "Form.MockUri.Description", type = AFieldType.STRING )
		public final static String MOCKURI = messages.get( "Form.MockUri.Label" );

		@AField( description = "Form.Assertion.Description", type = AFieldType.BOOLEAN, enabled = true )
		public final static String ASSERTION = messages.get( "Form.Assertion.Label" );
	}
}