package pl.touk.soapuii.testgenerator;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
import java.io.File;
import pl.touk.proxygeneratorapi.Defaults;
import pl.touk.proxygeneratorapi.support.ExtFileFilter;

/**
 * 
 *
 */
public class TestGeneratorAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "GenerateTestsAction";
	private XFormDialog dialog;

	public static final MessageSupport messages = MessageSupport.getMessages( TestGeneratorAction.class );

	public TestGeneratorAction()
	{
		super( "Generate Tests", "Generates tests form getCommunication dumps" );
	}

	public void perform( WsdlProject project, Object param )
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
					String expUrl = PathUtils.expandPath( url, project );
					File file = new File (expUrl);
					if( file.exists() )
						parseGetCommunications( project, file );
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

	private void parseGetCommunications( WsdlProject project, File dir )
	{
		WsdlTestSuite suite = project.addNewTestSuite(dir.getName());

		suite.setPropertyValue("listenURI", dialog.getValue( Form.LISTENURI ));
		suite.setPropertyValue("outputURI", dialog.getValue( Form.OUTPUTURI ));

		WsdlTestCase[] results = null;
		for( File file : dir.listFiles(new ExtFileFilter(".xml")) )
		{
			WsdlTestCase testCase = suite.addNewTestCase(file.getName());

			parseSingleGetCommunication(testCase, file);
			
			UISupport.select( testCase );

			if( dialog.getValue( Form.SAMPLE ).equals( "true" ) )
			{
			}
		}
	}

	private void parseSingleGetCommunication(WsdlTestCase testCase, File file)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWPROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		public final static String FILENOTFOUND = messages.get( "Form.FileNotFound" );

		@AField( description = "Form.ListenUri.Description", type = AFieldType.STRING )
		public final static String LISTENURI = messages.get( "Form.ListenUri.Label" );

		@AField( description = "Form.OutputUri.Description", type = AFieldType.STRING )
		public final static String OUTPUTURI = messages.get( "Form.OutputUri.Label" );

		@AField( description = "Form.GetCommunication.Description", type = AFieldType.FOLDER )
		public final static String INITIALCOMMUNICATION = messages.get( "Form.GetCommunication.Label" );

		@AField( description = "Form.Sample.Description", type = AFieldType.BOOLEAN, enabled = false )
		public final static String SAMPLE = messages.get( "Form.Sample.Label" );
	}
}