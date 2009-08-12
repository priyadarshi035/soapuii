package pl.touk.soapuii.testgenerator;

import pl.touk.soapuii.testgenerator.wsdlbinding.WsdlBindingMapFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
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
import javax.xml.parsers.ParserConfigurationException;
import pl.touk.proxygeneratorapi.Defaults;

/**
 * 
 *
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
			dialog.setValue( Form.SAMPLE, Boolean.toString( true ) );
			dialog.setValue( Form.OUTPUTURI, Defaults.outputUri );
			dialog.setValue( Form.LISTENURI, Defaults.listenUri );
			dialog.getFormField( Form.INITIALCOMMUNICATION ).addFormFieldListener( new XFormFieldListener()
			{
				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					String value = newValue.toLowerCase().trim();

					dialog.getFormField( Form.SAMPLE ).setEnabled( value.length() > 0 );
				}
			} );
		}
		else
		{
			dialog.setValue( Form.INITIALCOMMUNICATION, "" );
			dialog.getFormField( Form.SAMPLE ).setEnabled( false );
		}

		while( dialog.show() )
		{
			try
			{
				String url = dialog.getValue( Form.INITIALCOMMUNICATION ).trim();
				if( StringUtils.hasContent( url ) )
				{
					String expUrl = PathUtils.expandPath( url, testSuite.getProject() );
					File file = new File (expUrl);

					if( file.exists() )
					{
						try
						{
							Map bindingMap = new WsdlBindingMapFactory().createBindingMap(testSuite.getProject(), file);
							if (bindingMap.isEmpty())
								throw new TestGeneratorException("Empty binding map. Generation failed.");
							
							new GetCommunicationParser().parseGetCommunications(
									testSuite, file, dialog.getValue(Form.LISTENURI),
									dialog.getValue(Form.OUTPUTURI), bindingMap);
						}
						catch (ParserConfigurationException ex)
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
		//@AField( description = "Form.GetCommunication.Description", type = AFieldType.FILE )
		public final static String INITIALCOMMUNICATION = messages.get( "Form.GetCommunication.Label" );

		@AField( description = "Form.ListenUri.Description", type = AFieldType.STRING )
		public final static String LISTENURI = messages.get( "Form.ListenUri.Label" );

		@AField( description = "Form.OutputUri.Description", type = AFieldType.STRING )
		public final static String OUTPUTURI = messages.get( "Form.OutputUri.Label" );

		@AField( description = "Form.Sample.Description", type = AFieldType.BOOLEAN, enabled = false )
		public final static String SAMPLE = messages.get( "Form.Sample.Label" );
	}
}