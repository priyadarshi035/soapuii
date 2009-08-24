/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.actions.project;

import java.io.File;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.iface.GenerateMockServiceAction;
import com.eviware.soapui.impl.wsdl.actions.iface.GenerateWsdlTestSuiteAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.SoapUIException;
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
import java.io.FileFilter;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.log4j.Logger;

/**
 * Action for creating a new WSDL project
 * 
 * @author Ole.Matzura
 */

public class AddWsdlAction extends AbstractSoapUIAction<WsdlProject>
{
	private static Logger log = Logger.getLogger(AddWsdlAction.class);
	public static final String SOAPUI_ACTION_ID = "NewWsdlProjectAction";
	private XFormDialog dialog;

	public static final MessageSupport messages = MessageSupport.getMessages( AddWsdlAction.class );

	public AddWsdlAction()
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}

	public void perform( WsdlProject project, Object param )
	{
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );
			dialog.setValue( Form.CREATEREQUEST, Boolean.toString( true ) );
			dialog.getFormField( Form.INITIALWSDL ).addFormFieldListener( new XFormFieldListener()
			{
				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					String value = newValue.toLowerCase().trim();

					dialog.getFormField( Form.CREATEREQUEST ).setEnabled( value.length() > 0 );
					dialog.getFormField( Form.GENERATEMOCKSERVICE ).setEnabled( newValue.trim().length() > 0 );
					dialog.getFormField( Form.GENERATETESTSUITE ).setEnabled( newValue.trim().length() > 0 );
				}
			} );
		}
		else
		{
			dialog.setValue( Form.INITIALWSDL, "" );

			dialog.getFormField( Form.CREATEREQUEST ).setEnabled( false );
			dialog.getFormField( Form.GENERATEMOCKSERVICE ).setEnabled( false );
			dialog.getFormField( Form.GENERATETESTSUITE ).setEnabled( false );
		}

		while( dialog.show() )
		{
			try
			{
				String url = dialog.getValue( Form.INITIALWSDL ).trim();
				if( StringUtils.hasContent( url ) )
				{
					String expUrl = PathUtils.expandPath( url, project );

					File wsdl = new File( expUrl );
					if( wsdl.exists() )
						url = wsdl.toURI().toURL().toString();

					WsdlInterface[] results = new WsdlInterface[0];

					if (wsdl.isDirectory())
					{
						for( File singleFile : (Collection<File>) FileUtils.listFiles(wsdl, new String[]{"wsdl"}, true) )
						{
							if (singleFile.isDirectory())
								continue;
							try
							{
								log.info("importing: " + singleFile.getPath());
								WsdlInterface[] tmpResults = importWsdl( project, singleFile.getPath() );
								WsdlInterface[] newResult = new WsdlInterface[results.length + tmpResults.length];
								System.arraycopy(results, 0, newResult, 0, results.length);
								System.arraycopy(tmpResults, 0, newResult, results.length, tmpResults.length);
								results = newResult;
							}
							catch(Exception ex)
							{
								UISupport.showErrorMessage( ex );
							}
						}
					}
					else
					{
						log.info("importing: " + expUrl);
						results = importWsdl( project, expUrl );
					}

					if( !url.equals( expUrl ) )
						for( WsdlInterface iface : results )
							iface.setDefinition( url, false );

					break;
				}
			}
			catch( Exception ex )
			{
				UISupport.showErrorMessage( ex );
			}
		}
	}

	private WsdlInterface[] importWsdl( WsdlProject project, String url ) throws SoapUIException
	{
		WsdlInterface[] results = WsdlInterfaceFactory.importWsdl( project, url, dialog.getValue( Form.CREATEREQUEST )
				.equals( "true" ) );
		for( WsdlInterface iface : results )
		{
			UISupport.select( iface );

			if( dialog.getValue( Form.GENERATETESTSUITE ).equals( "true" ) )
			{
				GenerateWsdlTestSuiteAction generateTestSuiteAction = new GenerateWsdlTestSuiteAction();
				generateTestSuiteAction.generateTestSuite( iface, true );
			}

			if( dialog.getValue( Form.GENERATEMOCKSERVICE ).equals( "true" ) )
			{
				GenerateMockServiceAction generateMockAction = new GenerateMockServiceAction();
				generateMockAction.generateMockService( iface, false );
			}
		}

		return results;
	}

	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWPROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( description = "Form.InitialWsdl.Description", type = AFieldType.FILE_OR_FOLDER )
		public final static String INITIALWSDL = messages.get( "Form.InitialWsdl.Label" );

		@AField( description = "Form.CreateRequests.Description", type = AFieldType.BOOLEAN, enabled = false )
		public final static String CREATEREQUEST = messages.get( "Form.CreateRequests.Label" );

		@AField( description = "Form.GenerateTestSuite.Description", type = AFieldType.BOOLEAN, enabled = false )
		public final static String GENERATETESTSUITE = messages.get( "Form.GenerateTestSuite.Label" );

		@AField( description = "Form.GenerateMockService.Description", type = AFieldType.BOOLEAN, enabled = false )
		public final static String GENERATEMOCKSERVICE = messages.get( "Form.GenerateMockService.Label" );
	}
}