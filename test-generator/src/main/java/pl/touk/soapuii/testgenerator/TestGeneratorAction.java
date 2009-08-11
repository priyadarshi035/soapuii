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
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
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
					{
						try
						{
							Map bindingMap = createBindingMap(file);
							new GetCommunicationParser().parseGetCommunications(
									project, file, dialog.getValue(Form.LISTENURI),
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

	protected XFormDialog buildDialog( WsdlProject modelItem )
	{
		return null;
		/*
		if( modelItem == null )
			return null;

		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Launch TestRunner" );

		mainForm = builder.createForm( "Basic" );
		mainForm.addComboBox( TESTSUITE, new String[] {}, "The TestSuite to run" ).addFormFieldListener(
				new XFormFieldListener()
				{

					public void valueChanged( XFormField sourceField, String newValue, String oldValue )
					{
						List<String> testCases = new ArrayList<String>();
						String tc = mainForm.getComponentValue( TESTCASE );

						if( newValue.equals( ALL_VALUE ) )
						{
							for( TestSuite testSuite : testSuites )
							{
								for( TestCase testCase : testSuite.getTestCaseList() )
								{
									if( !testCases.contains( testCase.getName() ) )
										testCases.add( testCase.getName() );
								}
							}
						}
						else
						{
							TestSuite testSuite = getModelItem().getTestSuiteByName( newValue );
							if( testSuite != null )
								testCases.addAll( Arrays.asList( ModelSupport.getNames( testSuite.getTestCaseList() ) ) );
						}

						testCases.add( 0, ALL_VALUE );
						mainForm.setOptions( TESTCASE, testCases.toArray() );

						if( testCases.contains( tc ) )
						{
							mainForm.getFormField( TESTCASE ).setValue( tc );
						}
					}
				} );

		mainForm.addComboBox( TESTCASE, new String[] {}, "The TestCase to run" );
		mainForm.addSeparator();
		mainForm.addCheckBox( PRINTREPORT, "Prints a summary report to the console" );
		mainForm.addCheckBox( EXPORTJUNITRESULTS, "Exports results to a JUnit-Style report" );
		mainForm.addCheckBox( EXPORTALL, "Exports all results (not only errors)" );
		mainForm.addTextField( ROOTFOLDER, "Folder to export to", XForm.FieldType.FOLDER );
		mainForm.addCheckBox( COVERAGE, "Generate WSDL Coverage report (soapUI Pro only)" );
		mainForm.addCheckBox( OPEN_REPORT, "Open generated HTML report in browser (soapUI Pro only)" );
		mainForm.addSeparator();
		mainForm.addCheckBox( ENABLEUI, "Enables UI components in scripts" );
		mainForm.addTextField( TESTRUNNERPATH, "Folder containing TestRunner.bat to use", XForm.FieldType.FOLDER );
		mainForm.addCheckBox( SAVEPROJECT, "Saves project before running" ).setEnabled( !modelItem.isRemote() );
		mainForm.addCheckBox( ADDSETTINGS, "Adds global settings to command-line" );

		advForm = builder.createForm( "Overrides" );
		advForm.addComboBox( ENDPOINT, new String[] { "" }, "endpoint to forward to" );
		advForm.addTextField( HOSTPORT, "Host:Port to use for requests", XForm.FieldType.TEXT );
		advForm.addSeparator();
		advForm.addTextField( USERNAME, "The username to set for all requests", XForm.FieldType.TEXT );
		advForm.addTextField( PASSWORD, "The password to set for all requests", XForm.FieldType.PASSWORD );
		advForm.addTextField( DOMAIN, "The domain to set for all requests", XForm.FieldType.TEXT );
		advForm.addComboBox( WSSTYPE, new String[] { "", "Text", "Digest" }, "The username to set for all requests" );

		setToolsSettingsAction( null );
		buildArgsForm( builder, false, "TestRunner" );

		return builder.buildDialog( buildDefaultActions( HelpUrls.TESTRUNNER_HELP_URL, modelItem ),
				"Specify arguments for launching soapUI TestRunner", UISupport.TOOL_ICON );*/
	}

	private Map createBindingMap(File dir)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWPROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		public final static String FILENOTFOUND = messages.get( "Form.FileNotFound" );

		@AField( description = "test1", type = AFieldType.MULTILIST )
		public final static String TEST1 = messages.get( "Form.ListenUri.Label" );

		@AField(description = "test2", type = AFieldType.ACTION)
		public final static String TEST2 = messages.get("Form.ListenUri.Label");

		@AField( description = "test3", type = AFieldType.FILELIST )
		public final static String TEST3 = "test3";

		@AField( description = "test4", type = AFieldType.STRINGLIST )
		public final static String TEST4 = messages.get( "Form.ListenUri.Label" );

		@AField( description = "Form.GetCommunication.Description", type = AFieldType.FILE )
		public final static String INITIALCOMMUNICATION = messages.get( "Form.GetCommunication.Label" );

		@AField( description = "Form.ListenUri.Description", type = AFieldType.STRING )
		public final static String LISTENURI = messages.get( "Form.ListenUri.Label" );

		@AField( description = "Form.OutputUri.Description", type = AFieldType.STRING )
		public final static String OUTPUTURI = messages.get( "Form.OutputUri.Label" );

		@AField( description = "Form.Sample.Description", type = AFieldType.BOOLEAN, enabled = false )
		public final static String SAMPLE = messages.get( "Form.Sample.Label" );
	}
}