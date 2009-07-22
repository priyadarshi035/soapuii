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

package com.eviware.soapui.impl.wsdl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.xml.namespace.QName;

import org.apache.commons.ssl.OpenSSL;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.InterfaceConfig;
import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.config.MockServiceDocumentConfig;
import com.eviware.soapui.config.ProjectConfig;
import com.eviware.soapui.config.SoapuiProjectDocumentConfig;
import com.eviware.soapui.config.TestSuiteConfig;
import com.eviware.soapui.config.TestSuiteDocumentConfig;
import com.eviware.soapui.config.TestSuiteRunTypesConfig;
import com.eviware.soapui.config.TestSuiteRunTypesConfig.Enum;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.rest.support.RestRequestConverter.RestConversionException;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.endpoint.DefaultEndpointStrategy;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.wsdl.UrlWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlLoader;
import com.eviware.soapui.impl.wsdl.support.wss.DefaultWssContainer;
import com.eviware.soapui.impl.wsdl.testcase.WsdlProjectRunner;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.EndpointStrategy;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.project.ProjectListener;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.ProjectRunContext;
import com.eviware.soapui.model.testsuite.ProjectRunListener;
import com.eviware.soapui.model.testsuite.ProjectRunner;
import com.eviware.soapui.model.testsuite.TestRunnable;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestSuite.TestSuiteRunType;
import com.eviware.soapui.settings.ProjectSettings;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;
import com.eviware.soapui.support.resolver.ResolveDialog;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * WSDL project implementation
 * 
 * @author Ole.Matzura
 */

public class WsdlProject extends AbstractTestPropertyHolderWsdlModelItem<ProjectConfig> implements Project,
		PropertyExpansionContainer, PropertyChangeListener, TestRunnable
{
	public final static String AFTER_LOAD_SCRIPT_PROPERTY = WsdlProject.class.getName() + "@setupScript";
	public final static String BEFORE_SAVE_SCRIPT_PROPERTY = WsdlProject.class.getName() + "@tearDownScript";
	public final static String RESOURCE_ROOT_PROPERTY = WsdlProject.class.getName() + "@resourceRoot";

	private WorkspaceImpl workspace;
	protected String path;
	protected List<AbstractInterface<?>> interfaces = new ArrayList<AbstractInterface<?>>();
	protected List<WsdlTestSuite> testSuites = new ArrayList<WsdlTestSuite>();
	protected List<WsdlMockService> mockServices = new ArrayList<WsdlMockService>();
	private Set<ProjectListener> projectListeners = new HashSet<ProjectListener>();
	protected SoapuiProjectDocumentConfig projectDocument;
	private ImageIcon disabledIcon;
	private ImageIcon closedIcon;
	private ImageIcon remoteIcon;
	private ImageIcon openEncyptedIcon;
	protected EndpointStrategy endpointStrategy = new DefaultEndpointStrategy();
	protected long lastModified;
	private boolean remote;
	private boolean open = true;
	private boolean disabled;

	private SoapUIScriptEngine afterLoadScriptEngine;
	private SoapUIScriptEngine beforeSaveScriptEngine;
	private PropertyExpansionContext context = new DefaultPropertyExpansionContext( this );
	protected DefaultWssContainer wssContainer;
	private String projectPassword = null;

	/*
	 * 3 state flag: 1. 0 - project not encrypted 2. 1 - encrypted , good
	 * password, means that it could be successfully decrypted 3. -1 - encrypted,
	 * but with bad password or no password.
	 */

	protected int encrypted;
	private ImageIcon closedEncyptedIcon;
	protected final PropertyChangeSupport pcs;
	private SoapUIScriptEngine afterRunScriptEngine;
	private SoapUIScriptEngine beforeRunScriptEngine;
	private Set<ProjectRunListener> runListeners = new HashSet<ProjectRunListener>();

	protected final static Logger log = Logger.getLogger( WsdlProject.class );

	public WsdlProject() throws XmlException, IOException, SoapUIException
	{
		this( ( WorkspaceImpl )null );
	}

	public WsdlProject( String path ) throws XmlException, IOException, SoapUIException
	{
		this( path, ( WorkspaceImpl )null );
	}

	public WsdlProject( String projectFile, String projectPassword )
	{
		this( projectFile, null, true, true, null, projectPassword );
	}

	public WsdlProject( WorkspaceImpl workspace )
	{
		this( null, workspace, true );
	}

	public WsdlProject( String path, WorkspaceImpl workspace )
	{
		this( path, workspace, true );
	}

	public WsdlProject( String path, WorkspaceImpl workspace, boolean create )
	{
		this( path, workspace, create, true, null, null );
	}

	public WsdlProject( String path, WorkspaceImpl workspace, boolean create, boolean open, String tempName,
			String projectPassword )
	{
		super( null, workspace, "/project.gif" );

		pcs = new PropertyChangeSupport( this );

		this.workspace = workspace;
		this.path = path;
		this.projectPassword = projectPassword;

		for( ProjectListener listener : SoapUI.getListenerRegistry().getListeners( ProjectListener.class ) )
		{
			addProjectListener( listener );
		}

		for( ProjectRunListener listener : SoapUI.getListenerRegistry().getListeners( ProjectRunListener.class ) )
		{
			addProjectRunListener( listener );
		}

		try
		{
			if( path != null && open )
			{
				File file = new File( path.trim() );
				if( file.exists() )
				{
					try
					{
						loadProject( new URL( "file:" + file.getAbsolutePath() ) );
						lastModified = file.lastModified();
					}
					catch( MalformedURLException e )
					{
						SoapUI.logError( e );
						disabled = true;
					}
				}
				else
				{
					try
					{
						if( !PathUtils.isHttpPath( path ) )
							SoapUI.log.info( "File [" + file.getAbsolutePath() + "] does not exist, trying URL instead" );

						remote = true;
						loadProject( new URL( path ) );
					}
					catch( MalformedURLException e )
					{
						SoapUI.logError( e );
						disabled = true;
					}
				}
			}
		}
		catch( SoapUIException e )
		{
			SoapUI.logError( e );
			disabled = true;
		}
		finally
		{
			closedIcon = UISupport.createImageIcon( "/closedProject.gif" );
			remoteIcon = UISupport.createImageIcon( "/remoteProject.gif" );
			disabledIcon = UISupport.createImageIcon( "/disabledProject.gif" );
			openEncyptedIcon = UISupport.createImageIcon( "/openEncryptedProject.gif" );
			closedEncyptedIcon = UISupport.createImageIcon( "/closedEncryptedProject.gif" );

			this.open = open && !disabled && ( this.encrypted != -1 );

			if( projectDocument == null )
			{
				projectDocument = SoapuiProjectDocumentConfig.Factory.newInstance();
				setConfig( projectDocument.addNewSoapuiProject() );
				if( tempName != null || path != null )
					getConfig().setName( StringUtils.isNullOrEmpty( tempName ) ? getNameFromPath() : tempName );

				setPropertiesConfig( getConfig().addNewProperties() );
				wssContainer = new DefaultWssContainer( this, getConfig().addNewWssContainer() );
				// setResourceRoot("${projectDir}");
			}

			if( getConfig() != null )
			{
				endpointStrategy.init( this );
			}
			if( getSettings() != null )
			{
				setProjectRoot( path );
			}

			addPropertyChangeListener( this );
		}
	}

	protected PropertyChangeSupport getPropertyChangeSupport()
	{
		return pcs;
	}

	public boolean isRemote()
	{
		return remote;
	}

	public void loadProject( URL file ) throws SoapUIException
	{
		try
		{
			UISupport.setHourglassCursor();

			UrlWsdlLoader loader = new UrlWsdlLoader( file.toString(), this );
			loader.setUseWorker( false );
			projectDocument = SoapuiProjectDocumentConfig.Factory.parse( loader.load() );

			// see if there is encoded data
			this.encrypted = checkForEncodedData( projectDocument.getSoapuiProject() );

			setConfig( projectDocument.getSoapuiProject() );

			// removed cached definitions if caching is disabled
			if( !getSettings().getBoolean( WsdlSettings.CACHE_WSDLS ) )
			{
				removeDefinitionCaches( projectDocument );
			}

			log.info( "Loaded project from [" + file.toString() + "]" );

			try
			{
				int majorVersion = Integer
						.parseInt( projectDocument.getSoapuiProject().getSoapuiVersion().split( "\\." )[0] );
				if( majorVersion > Integer.parseInt( SoapUI.SOAPUI_VERSION.split( "\\." )[0] ) )
					log.warn( "Project '" + projectDocument.getSoapuiProject().getName() + "' is from a newer version ("
							+ projectDocument.getSoapuiProject().getSoapuiVersion() + ") of soapUI than this ("
							+ SoapUI.SOAPUI_VERSION + ") and parts of it may be incompatible or incorrect. "
							+ "Saving this project with this version of soapUI may cause it to function differently." );
			}
			catch( Exception e )
			{
			}

			List<InterfaceConfig> interfaceConfigs = getConfig().getInterfaceList();
			for( InterfaceConfig config : interfaceConfigs )
			{
				AbstractInterface<?> iface = InterfaceFactoryRegistry.build( this, config );
				interfaces.add( iface );
			}

			List<TestSuiteConfig> testSuiteConfigs = getConfig().getTestSuiteList();
			for( TestSuiteConfig config : testSuiteConfigs )
			{
				testSuites.add( buildTestSuite( config ) );
			}

			List<MockServiceConfig> mockServiceConfigs = getConfig().getMockServiceList();
			for( MockServiceConfig config : mockServiceConfigs )
			{
				mockServices.add( new WsdlMockService( this, config ) );
			}

			if( !getConfig().isSetWssContainer() )
				getConfig().addNewWssContainer();

			wssContainer = new DefaultWssContainer( this, getConfig().getWssContainer() );

			endpointStrategy.init( this );

			if( !getConfig().isSetProperties() )
				getConfig().addNewProperties();

			if( !getConfig().isSetAbortOnError() )
				getConfig().setAbortOnError( false );

			// if( !getConfig().isSetFailOnErrors() )
			// getConfig().setFailOnErrors( true );

			if( !getConfig().isSetRunType() )
				getConfig().setRunType( TestSuiteRunTypesConfig.SEQUENTIAL );

			setPropertiesConfig( getConfig().getProperties() );
			afterLoad();
		}
		catch( Exception e )
		{
			if( e instanceof XmlException )
			{
				XmlException xe = ( XmlException )e;
				XmlError error = xe.getError();
				if( error != null )
					System.err.println( "Error at line " + error.getLine() + ", column " + error.getColumn() );
			}

			if( e instanceof RestConversionException )
			{
				log.error( "Project file needs to be updated manually, please reload the project." );
				throw new SoapUIException( "Failed to load project from file [" + file.toString() + "]", e );
			}

			e.printStackTrace();
			throw new SoapUIException( "Failed to load project from file [" + file.toString() + "]", e );
		}
		finally
		{
			UISupport.resetCursor();
		}
	}

	protected WsdlTestSuite buildTestSuite( TestSuiteConfig config )
	{
		return new WsdlTestSuite( this, config );
	}

	/**
	 * Decode encrypted data and restore user/pass
	 * 
	 * @param soapuiProject
	 * @return 0 - not encrypted, 1 - successfull decryption , -1 error while
	 *         decrypting, bad password, no password.
	 * @throws IOException
	 * @throws GeneralSecurityException
	 * @author robert nemet
	 */
	protected int checkForEncodedData( ProjectConfig soapuiProject ) throws IOException, GeneralSecurityException
	{

		byte[] encryptedContent = soapuiProject.getEncryptedContent();
		char[] password = null;

		// no encrypted data then go back
		if( encryptedContent == null || encryptedContent.length == 0 )
			return 0;

		String projectPassword = null;
		if( workspace != null )
		{
			projectPassword = workspace.getProjectPassword( soapuiProject.getName() );
		}
		else
		{
			projectPassword = this.projectPassword;
		}
		if( projectPassword == null )
		{
			password = UISupport.promptPassword( "Enter Password:", soapuiProject.getName() );
		}
		else
		{
			password = projectPassword.toCharArray();
		}
		byte[] data = null;
		// no pass go back.
		if( password == null )
		{
			return -1;
		}

		try
		{
			data = OpenSSL.decrypt( "des3", password, encryptedContent );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return -1;
		}

		String decryptedData = new String( data, "UTF-8" );

		if( decryptedData != null )
		{
			if( decryptedData.length() > 0 )
			{
				try
				{
					projectDocument.getSoapuiProject().set( XmlObject.Factory.parse( decryptedData ) );
				}
				catch( XmlException e )
				{
					UISupport.showErrorMessage( "Wrong password. Project need to be reloaded." );
					getWorkspace().clearProjectPassword( soapuiProject.getName() );
					return -1;
				}
			}
		}
		else
		{
			UISupport.showErrorMessage( "Wrong project password" );
			getWorkspace().clearProjectPassword( soapuiProject.getName() );
			return -1;
		}
		projectDocument.getSoapuiProject().setEncryptedContent( null );
		return 1;
	}

	@Override
	public void afterLoad()
	{
		super.afterLoad();

		try
		{
			ProjectListener[] a = projectListeners.toArray( new ProjectListener[projectListeners.size()] );

			for( int c = 0; c < a.length; c++ )
			{
				a[c].afterLoad( this );
			}

			runAfterLoadScript();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	protected void setProjectRoot( String path )
	{
		if( path != null && projectDocument != null )
		{
			int ix = path.lastIndexOf( File.separatorChar );
			if( ix > 0 )
				getSettings().setString( ProjectSettings.PROJECT_ROOT, path.substring( 0, ix ) );
		}
	}

	public void setResourceRoot( String resourceRoot )
	{
		String old = getResourceRoot();

		getConfig().setResourceRoot( resourceRoot );
		notifyPropertyChanged( RESOURCE_ROOT_PROPERTY, old, resourceRoot );
	}

	public String getResourceRoot()
	{
		if( !getConfig().isSetResourceRoot() )
			getConfig().setResourceRoot( "" );

		return getConfig().getResourceRoot();
	}

	@Override
	public ImageIcon getIcon()
	{
		if( isDisabled() )
			return disabledIcon;
		else if( getEncrypted() != 0 )
		{
			if( isOpen() )
			{
				return openEncyptedIcon;
			}
			else
			{
				return closedEncyptedIcon;
			}
		}
		else if( !isOpen() )
			return closedIcon;
		else if( isRemote() )
			return remoteIcon;
		else
			return super.getIcon();
	}

	private String getNameFromPath()
	{
		int ix = path.lastIndexOf( isRemote() ? '/' : File.separatorChar );
		String name = ix == -1 ? path : path.substring( ix + 1 );
		return name;
	}

	@Override
	public String getDescription()
	{
		if( isOpen() )
			return super.getDescription();

		String name = getName();

		if( isDisabled() )
			name += " - disabled [" + getPath() + "]";
		else
			name += " - closed [" + getPath() + "]";

		return name;
	}

	public WorkspaceImpl getWorkspace()
	{
		return workspace;
	}

	public AbstractInterface<?> getInterfaceAt( int index )
	{
		return interfaces.get( index );
	}

	public AbstractInterface<?> getInterfaceByName( String interfaceName )
	{
		return ( AbstractInterface<?> )getWsdlModelItemByName( interfaces, interfaceName );
	}

	public AbstractInterface<?> getInterfaceByTechnicalId( String technicalId )
	{
		for( int c = 0; c < getInterfaceCount(); c++ )
		{
			if( getInterfaceAt( c ).getTechnicalId().equals( technicalId ) )
				return getInterfaceAt( c );
		}

		return null;
	}

	public int getInterfaceCount()
	{
		return interfaces.size();
	}

	public String getPath()
	{
		return path;
	}

	public boolean save() throws IOException
	{
		return save( null );
	}

	public boolean save( String folder ) throws IOException
	{
		if( !isOpen() || isDisabled() || isRemote() )
			return true;

		if( path == null || isRemote() )
		{
			path = getName() + "-soapui-project.xml";
			if( folder != null )
			{
				path = folder + File.separatorChar + path;
			}

			File file = null;

			while( file == null
					|| ( file.exists() && !UISupport.confirm( "File [" + file.getName() + "] exists, overwrite?",
							"Overwrite File?" ) ) )
			{
				file = UISupport.getFileDialogs().saveAs( this, "Save project " + getName(), ".xml", "XML Files (*.xml)",
						new File( path ) );
				if( file == null )
					return false;
			}

			path = file.getAbsolutePath();
		}

		File projectFile = new File( path );

		while( projectFile.exists() && !projectFile.canWrite() )
		{
			if( UISupport.confirm( "Project file [" + path + "] can not be written to, save to new file?", "Save Project" ) )
			{
				projectFile = UISupport.getFileDialogs().saveAs( this, "Save project " + getName(), ".xml",
						"XML Files (*.xml)", projectFile );

				if( projectFile == null )
					return false;

				path = projectFile.getAbsolutePath();
			}
			else
				return false;
		}

		// check modified
		if( projectFile.exists() && lastModified != 0 && lastModified < projectFile.lastModified() )
		{
			if( !UISupport.confirm( "Project file for [" + getName() + "] has been modified externally, overwrite?",
					"Save Project" ) )
				return false;
		}

		if( projectFile.exists() && getSettings().getBoolean( UISettings.CREATE_BACKUP ) )
		{
			createBackup( projectFile );
		}

		return saveIn( projectFile );
	}

	public boolean saveBackup() throws IOException
	{
		File projectFile;
		if( path == null || isRemote() )
		{
			projectFile = new File( getName() + "-soapui-project.xml" );
		}
		else
		{
			projectFile = new File( path );
		}
		File backupFile = getBackupFile( projectFile );
		return saveIn( backupFile );
	}

	public boolean saveIn( File projectFile ) throws IOException
	{
		long size = 0;

		beforeSave();
		// work with copy beacuse we do not want to change working project while
		// working with it
		// if user choose save project, save all etc.
		SoapuiProjectDocumentConfig projectDocument = ( SoapuiProjectDocumentConfig )this.projectDocument.copy();

		// check for caching
		if( !getSettings().getBoolean( WsdlSettings.CACHE_WSDLS ) )
		{
			// no caching -> create copy and remove definition cachings
			removeDefinitionCaches( projectDocument );
		}

		// remove project root
		XmlBeansSettingsImpl tempSettings = new XmlBeansSettingsImpl( this, null, projectDocument.getSoapuiProject()
				.getSettings() );
		tempSettings.clearSetting( ProjectSettings.PROJECT_ROOT );

		// check for encryption
		String passwordForEncryption = getSettings().getString( ProjectSettings.SHADOW_PASSWORD, null );

		// if it has encryptedContend that means it is not decrypted corectly( bad
		// password, etc ), so do not encrypt it again.
		if( projectDocument.getSoapuiProject().getEncryptedContent() == null )
		{
			if( passwordForEncryption != null )
			{
				if( passwordForEncryption.length() > 1 )
				{
					// we have password so do encryption
					try
					{
						String data = getConfig().xmlText();
						byte[] encrypted = OpenSSL.encrypt( "des3", passwordForEncryption.toCharArray(), data.getBytes() );
						projectDocument.getSoapuiProject().setEncryptedContent( encrypted );
						projectDocument.getSoapuiProject().setInterfaceArray( null );
						projectDocument.getSoapuiProject().setTestSuiteArray( null );
						projectDocument.getSoapuiProject().setMockServiceArray( null );
						projectDocument.getSoapuiProject().unsetSettings();
						projectDocument.getSoapuiProject().unsetProperties();

					}
					catch( GeneralSecurityException e )
					{
						UISupport.showErrorMessage( "Encryption Error" );
					}
				}
				else
				{
					// no password no encryption.
					projectDocument.getSoapuiProject().setEncryptedContent( null );
				}
			}
		}
		// end of encryption.

		XmlOptions options = new XmlOptions();
		if( SoapUI.getSettings().getBoolean( WsdlSettings.PRETTY_PRINT_PROJECT_FILES ) )
			options.setSavePrettyPrint();

		projectDocument.getSoapuiProject().setSoapuiVersion( SoapUI.SOAPUI_VERSION );

		try
		{
			File tempFile = File.createTempFile( "project-temp-", ".xml", projectFile.getParentFile() );

			// save once to make sure it can be saved
			FileOutputStream tempOut = new FileOutputStream( tempFile );
			projectDocument.save( tempOut, options );
			tempOut.close();

			if( getSettings().getBoolean( UISettings.LINEBREAK ) )
				normalizeLineBreak( projectFile, tempFile );
			else
			{
				// now save it for real
				FileOutputStream projectOut = new FileOutputStream( projectFile );
				projectDocument.save( projectOut, options );
				projectOut.close();
				// delete tempFile here so we have it as backup in case second save
				// fails
				if( !tempFile.delete() )
				{
					SoapUI.getErrorLog().warn( "Failed to delete temporary project file; " + tempFile.getAbsolutePath() );
					tempFile.deleteOnExit();
				}
			}
			size = projectFile.length();
		}
		catch( Throwable t )
		{
			SoapUI.logError( t );
			UISupport.showErrorMessage( "Failed to save project [" + getName() + "]: " + t.toString() );
			return false;
		}

		lastModified = projectFile.lastModified();
		log.info( "Saved project [" + getName() + "] to [" + projectFile.getAbsolutePath() + " - " + size + " bytes" );
		setProjectRoot( getPath() );
		return true;
	}

	private static void normalizeLineBreak( File target, File tmpFile )
	{
		try
		{
			FileReader fr = new FileReader( tmpFile );
			BufferedReader in = new BufferedReader( fr );
			FileWriter fw = new FileWriter( target );
			BufferedWriter out = new BufferedWriter( fw );
			String line = "";
			while( ( line = in.readLine() ) != null )
			{
				out.write( line );
				out.newLine();
				out.flush();
			}
			out.close();
			fw.close();
			in.close();
			fr.close();
		}
		catch( FileNotFoundException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if( !tmpFile.delete() )
		{
			SoapUI.getErrorLog().warn( "Failed to delete temporary file: " + tmpFile.getAbsolutePath() );
			tmpFile.deleteOnExit();
		}
	}

	public void beforeSave()
	{
		try
		{
			ProjectListener[] a = projectListeners.toArray( new ProjectListener[projectListeners.size()] );

			for( int c = 0; c < a.length; c++ )
			{
				a[c].beforeSave( this );
			}

			runBeforeSaveScript();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		// notify
		for( AbstractInterface<?> iface : interfaces )
			iface.beforeSave();

		for( WsdlTestSuite testSuite : testSuites )
			testSuite.beforeSave();

		for( WsdlMockService mockService : mockServices )
			mockService.beforeSave();

		endpointStrategy.onSave();
	}

	protected void createBackup( File projectFile ) throws IOException
	{
		File backupFile = getBackupFile( projectFile );
		log.info( "Backing up [" + projectFile + "] to [" + backupFile + "]" );
		Tools.copyFile( projectFile, backupFile, true );
	}

	protected File getBackupFile( File projectFile )
	{
		String backupFolderName = getSettings().getString( UISettings.BACKUP_FOLDER, "" );

		File backupFolder = new File( backupFolderName );
		if( !backupFolder.isAbsolute() )
		{
			backupFolder = new File( projectFile.getParentFile(), backupFolderName );
		}

		if( !backupFolder.exists() )
			backupFolder.mkdirs();

		File backupFile = new File( backupFolder, projectFile.getName() + ".backup" );
		return backupFile;
	}

	protected void removeDefinitionCaches( SoapuiProjectDocumentConfig config )
	{
		for( InterfaceConfig ifaceConfig : config.getSoapuiProject().getInterfaceList() )
		{
			if( ifaceConfig.isSetDefinitionCache() )
			{
				log.info( "Removing definition cache from interface [" + ifaceConfig.getName() + "]" );
				ifaceConfig.unsetDefinitionCache();
			}
		}
	}

	public AbstractInterface<?> addNewInterface( String name, String type )
	{
		AbstractInterface<?> iface = ( AbstractInterface<?> )InterfaceFactoryRegistry.createNew( this, type, name );
		if( iface != null )
		{
			iface.getConfig().setType( type );

			interfaces.add( iface );
			fireInterfaceAdded( iface );
		}

		return iface;
	}

	public void addProjectListener( ProjectListener listener )
	{
		projectListeners.add( listener );
	}

	public void removeProjectListener( ProjectListener listener )
	{
		projectListeners.remove( listener );
	}

	public void fireInterfaceAdded( AbstractInterface<?> iface )
	{
		ProjectListener[] a = projectListeners.toArray( new ProjectListener[projectListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].interfaceAdded( iface );
		}
	}

	public void fireInterfaceRemoved( AbstractInterface<?> iface )
	{
		ProjectListener[] a = projectListeners.toArray( new ProjectListener[projectListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].interfaceRemoved( iface );
		}
	}

	public void fireInterfaceUpdated( AbstractInterface<?> iface )
	{
		ProjectListener[] a = projectListeners.toArray( new ProjectListener[projectListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].interfaceUpdated( iface );
		}
	}

	public void fireTestSuiteAdded( WsdlTestSuite testSuite )
	{
		ProjectListener[] a = projectListeners.toArray( new ProjectListener[projectListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].testSuiteAdded( testSuite );
		}
	}

	private void fireTestSuiteMoved( WsdlTestSuite testCase, int ix, int offset )
	{
		ProjectListener[] a = projectListeners.toArray( new ProjectListener[projectListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].testSuiteMoved( testCase, ix, offset );
		}
	}

	public void fireTestSuiteRemoved( WsdlTestSuite testSuite )
	{
		ProjectListener[] a = projectListeners.toArray( new ProjectListener[projectListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].testSuiteRemoved( testSuite );
		}
	}

	public void fireMockServiceAdded( WsdlMockService mockService )
	{
		ProjectListener[] a = projectListeners.toArray( new ProjectListener[projectListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].mockServiceAdded( mockService );
		}
	}

	public void fireMockServiceRemoved( WsdlMockService mockService )
	{
		ProjectListener[] a = projectListeners.toArray( new ProjectListener[projectListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].mockServiceRemoved( mockService );
		}
	}

	public void removeInterface( AbstractInterface<?> iface )
	{
		int ix = interfaces.indexOf( iface );
		interfaces.remove( ix );
		try
		{
			fireInterfaceRemoved( iface );
		}
		finally
		{
			iface.release();
			getConfig().removeInterface( ix );
		}
	}

	public void removeTestSuite( WsdlTestSuite testSuite )
	{
		int ix = testSuites.indexOf( testSuite );
		testSuites.remove( ix );

		try
		{
			fireTestSuiteRemoved( testSuite );
		}
		finally
		{
			testSuite.release();
			getConfig().removeTestSuite( ix );
		}
	}

	public boolean isDisabled()
	{
		return disabled;
	}

	public int getTestSuiteCount()
	{
		return testSuites.size();
	}

	public WsdlTestSuite getTestSuiteAt( int index )
	{
		return testSuites.get( index );
	}

	public WsdlTestSuite getTestSuiteByName( String testSuiteName )
	{
		return ( WsdlTestSuite )getWsdlModelItemByName( testSuites, testSuiteName );
	}

	public WsdlTestSuite addNewTestSuite( String name )
	{
		WsdlTestSuite testSuite = buildTestSuite( getConfig().addNewTestSuite() );
		testSuite.setName( name );
		testSuites.add( testSuite );
		fireTestSuiteAdded( testSuite );

		return testSuite;
	}

	public boolean isCacheDefinitions()
	{
		return getSettings().getBoolean( WsdlSettings.CACHE_WSDLS );
	}

	public void setCacheDefinitions( boolean cacheDefinitions )
	{
		getSettings().setBoolean( WsdlSettings.CACHE_WSDLS, cacheDefinitions );
	}

	public boolean saveAs( String fileName ) throws IOException
	{
		if( !isOpen() || isDisabled() )
			return false;

		String oldPath = path;
		path = fileName;
		boolean result = save();
		if( !result )
			path = oldPath;
		else
			remote = false;

		setProjectRoot( path );

		return result;
	}

	@Override
	public void release()
	{
		super.release();

		if( isOpen() )
		{
			endpointStrategy.release();

			for( WsdlTestSuite testSuite : testSuites )
				testSuite.release();

			for( WsdlMockService mockService : mockServices )
				mockService.release();

			for( AbstractInterface<?> iface : interfaces )
				iface.release();

			if( wssContainer != null )
			{
				wssContainer.release();
				wssContainer = null;
			}
		}

		projectListeners.clear();

		if( afterLoadScriptEngine != null )
			afterLoadScriptEngine.release();

		if( beforeSaveScriptEngine != null )
			beforeSaveScriptEngine.release();
	}

	public WsdlMockService addNewMockService( String name )
	{
		WsdlMockService mockService = new WsdlMockService( this, getConfig().addNewMockService() );
		mockService.setName( name );
		mockServices.add( mockService );
		fireMockServiceAdded( mockService );

		return mockService;
	}

	public WsdlMockService getMockServiceAt( int index )
	{
		return mockServices.get( index );
	}

	public WsdlMockService getMockServiceByName( String mockServiceName )
	{
		return ( WsdlMockService )getWsdlModelItemByName( mockServices, mockServiceName );
	}

	public int getMockServiceCount()
	{
		return mockServices.size();
	}

	public void removeMockService( WsdlMockService mockService )
	{
		int ix = mockServices.indexOf( mockService );
		mockServices.remove( ix );

		try
		{
			fireMockServiceRemoved( mockService );
		}
		finally
		{
			mockService.release();
			getConfig().removeMockService( ix );
		}
	}

	public List<TestSuite> getTestSuiteList()
	{
		return new ArrayList<TestSuite>( testSuites );
	}

	public List<MockService> getMockServiceList()
	{
		return new ArrayList<MockService>( mockServices );
	}

	public List<Interface> getInterfaceList()
	{
		return new ArrayList<Interface>( interfaces );
	}

	public Map<String, Interface> getInterfaces()
	{
		Map<String, Interface> result = new HashMap<String, Interface>();
		for( Interface iface : interfaces )
			result.put( iface.getName(), iface );

		return result;
	}

	public Map<String, TestSuite> getTestSuites()
	{
		Map<String, TestSuite> result = new HashMap<String, TestSuite>();
		for( TestSuite iface : testSuites )
			result.put( iface.getName(), iface );

		return result;
	}

	public Map<String, MockService> getMockServices()
	{
		Map<String, MockService> result = new HashMap<String, MockService>();
		for( MockService mockService : mockServices )
			result.put( mockService.getName(), mockService );

		return result;
	}

	public void reload() throws SoapUIException
	{
		reload( path );
	}

	public void reload( String path ) throws SoapUIException
	{
		this.path = path;
		getWorkspace().reloadProject( this );
	}

	public boolean hasNature( String natureId )
	{
		Settings projectSettings = getSettings();
		String projectNature = projectSettings.getString( ProjectSettings.PROJECT_NATURE, null );
		return natureId.equals( projectNature );
	}

	public AbstractInterface<?> importInterface( AbstractInterface<?> iface, boolean importEndpoints, boolean createCopy )
	{
		iface.beforeSave();

		InterfaceConfig ifaceConfig = ( InterfaceConfig )iface.getConfig().copy();
		ifaceConfig = ( InterfaceConfig )getConfig().addNewInterface().set( ifaceConfig );

		AbstractInterface<?> imported = InterfaceFactoryRegistry.build( this, ifaceConfig );
		interfaces.add( imported );

		if( iface.getProject() != this && importEndpoints )
		{
			endpointStrategy.importEndpoints( iface );
		}

		if( createCopy )
			ModelSupport.unsetIds( imported );

		imported.afterLoad();
		fireInterfaceAdded( imported );

		return imported;
	}

	public WsdlTestSuite importTestSuite( WsdlTestSuite testSuite, String name, int index, boolean createCopy,
			String description )
	{
		testSuite.beforeSave();
		TestSuiteConfig testSuiteConfig = index == -1 ? ( TestSuiteConfig )getConfig().addNewTestSuite().set(
				testSuite.getConfig().copy() ) : ( TestSuiteConfig )getConfig().insertNewTestSuite( index ).set(
				testSuite.getConfig().copy() );

		testSuiteConfig.setName( name );

		testSuite = buildTestSuite( testSuiteConfig );
		if( description != null )
			testSuite.setDescription( description );

		if( index == -1 )
			testSuites.add( testSuite );
		else
			testSuites.add( index, testSuite );

		if( createCopy )
			ModelSupport.unsetIds( testSuite );

		testSuite.afterLoad();
		fireTestSuiteAdded( testSuite );

		resolveImportedTestSuite( testSuite );

		return testSuite;
	}

	public WsdlMockService importMockService( WsdlMockService mockService, String name, boolean createCopy,
			String description )
	{
		mockService.beforeSave();
		MockServiceConfig mockServiceConfig = ( MockServiceConfig )getConfig().addNewMockService().set(
				mockService.getConfig().copy() );
		mockServiceConfig.setName( name );
		if( mockServiceConfig.isSetId() && createCopy )
			mockServiceConfig.unsetId();
		mockService = new WsdlMockService( this, mockServiceConfig );
		mockService.setDescription( description );
		mockServices.add( mockService );
		if( createCopy )
			ModelSupport.unsetIds( mockService );

		mockService.afterLoad();

		fireMockServiceAdded( mockService );

		return mockService;
	}

	public EndpointStrategy getEndpointStrategy()
	{
		return endpointStrategy;
	}

	public boolean isOpen()
	{
		return open;
	}

	public List<? extends ModelItem> getChildren()
	{
		ArrayList<ModelItem> list = new ArrayList<ModelItem>();
		list.addAll( getInterfaceList() );
		list.addAll( getTestSuiteList() );
		list.addAll( getMockServiceList() );
		return list;
	}

	public void setAfterLoadScript( String script )
	{
		String oldScript = getAfterLoadScript();

		if( !getConfig().isSetAfterLoadScript() )
			getConfig().addNewAfterLoadScript();

		getConfig().getAfterLoadScript().setStringValue( script );
		if( afterLoadScriptEngine != null )
			afterLoadScriptEngine.setScript( script );

		notifyPropertyChanged( AFTER_LOAD_SCRIPT_PROPERTY, oldScript, script );
	}

	public String getAfterLoadScript()
	{
		return getConfig().isSetAfterLoadScript() ? getConfig().getAfterLoadScript().getStringValue() : null;
	}

	public void setBeforeSaveScript( String script )
	{
		String oldScript = getBeforeSaveScript();

		if( !getConfig().isSetBeforeSaveScript() )
			getConfig().addNewBeforeSaveScript();

		getConfig().getBeforeSaveScript().setStringValue( script );
		if( beforeSaveScriptEngine != null )
			beforeSaveScriptEngine.setScript( script );

		notifyPropertyChanged( BEFORE_SAVE_SCRIPT_PROPERTY, oldScript, script );
	}

	public String getBeforeSaveScript()
	{
		return getConfig().isSetBeforeSaveScript() ? getConfig().getBeforeSaveScript().getStringValue() : null;
	}

	public Object runAfterLoadScript() throws Exception
	{
		String script = getAfterLoadScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( afterLoadScriptEngine == null )
		{
			afterLoadScriptEngine = SoapUIScriptEngineRegistry.create( this );
			afterLoadScriptEngine.setScript( script );
		}

		afterLoadScriptEngine.setVariable( "context", context );
		afterLoadScriptEngine.setVariable( "project", this );
		afterLoadScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return afterLoadScriptEngine.run();
	}

	public Object runBeforeSaveScript() throws Exception
	{
		String script = getBeforeSaveScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( beforeSaveScriptEngine == null )
		{
			beforeSaveScriptEngine = SoapUIScriptEngineRegistry.create( this );
			beforeSaveScriptEngine.setScript( script );
		}

		beforeSaveScriptEngine.setVariable( "context", context );
		beforeSaveScriptEngine.setVariable( "project", this );
		beforeSaveScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return beforeSaveScriptEngine.run();
	}

	public PropertyExpansionContext getContext()
	{
		return context;
	}

	public DefaultWssContainer getWssContainer()
	{
		return wssContainer;
	}

	@Override
	public void resolve( ResolveContext<?> context )
	{
		super.resolve( context );

		wssContainer.resolve( context );
	}

	public PropertyExpansion[] getPropertyExpansions()
	{
		List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

		result.addAll( Arrays.asList( wssContainer.getPropertyExpansions() ) );
		// result.addAll(Arrays.asList(databaseConnectionContainer.
		// getPropertyExpansions()));

		return result.toArray( new PropertyExpansion[result.size()] );

	}

	public String getPropertiesLabel()
	{
		return "Custom Properties";
	}

	public String getShadowPassword()
	{
		projectPassword = getSettings() == null ? projectPassword : getSettings().getString(
				ProjectSettings.SHADOW_PASSWORD, null );
		return projectPassword;
	}

	public void setShadowPassword( String password )
	{
		String oldPassword = getSettings().getString( ProjectSettings.SHADOW_PASSWORD, null );
		getSettings().setString( ProjectSettings.SHADOW_PASSWORD, password );
		this.pcs.firePropertyChange( "projectPassword", oldPassword, password );
	}

	public void inspect()
	{

		if( !isOpen() )
			return;

		byte data[] = projectDocument.getSoapuiProject().getEncryptedContent();
		if( data != null && data.length > 0 )
		{
			try
			{
				reload();
			}
			catch( SoapUIException e )
			{
				e.printStackTrace();
			}
		}
	}

	public int getEncrypted()
	{
		return this.encrypted;
	}

	public int setEncrypted( int code )
	{
		return this.encrypted = code;
	}

	public void addPropertyChangeListener( PropertyChangeListener listener )
	{
		this.pcs.addPropertyChangeListener( listener );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( "projectPassword".equals( evt.getPropertyName() ) )
		{
			if( encrypted == 0 & ( evt.getOldValue() == null || ( ( String )evt.getOldValue() ).length() == 0 ) )
			{
				encrypted = 1;
			}
			if( encrypted == 1 & ( evt.getNewValue() == null || ( ( String )evt.getNewValue() ).length() == 0 ) )
			{
				encrypted = 0;
			}
			SoapUI.getNavigator().repaint();
		}

	}

	public SoapuiProjectDocumentConfig getProjectDocument()
	{
		return projectDocument;
	}

	public int getInterfaceCount( String type )
	{
		int result = 0;

		for( AbstractInterface<?> iface : interfaces )
		{
			if( iface.getType().equals( type ) )
				result++ ;
		}

		return result;
	}

	public List<AbstractInterface<?>> getInterfaces( String type )
	{
		ArrayList<AbstractInterface<?>> result = new ArrayList<AbstractInterface<?>>();

		for( AbstractInterface<?> iface : interfaces )
		{
			if( iface.getType().equals( type ) )
				result.add( iface );
		}

		return result;
	}

	public void importTestSuite( File file )
	{
		if( !file.exists() )
		{
			UISupport.showErrorMessage( "Error loading test case " );
			return;
		}

		TestSuiteDocumentConfig newTestSuiteConfig = null;

		try
		{
			newTestSuiteConfig = TestSuiteDocumentConfig.Factory.parse( file );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		if( newTestSuiteConfig == null )
		{
			UISupport.showErrorMessage( "Not valild test case xml" );
		}
		else
		{
			TestSuiteConfig config = ( TestSuiteConfig )projectDocument.getSoapuiProject().addNewTestSuite().set(
					newTestSuiteConfig.getTestSuite() );
			WsdlTestSuite testSuite = buildTestSuite( config );

			ModelSupport.unsetIds( testSuite );
			testSuite.afterLoad();

			testSuites.add( testSuite );
			fireTestSuiteAdded( testSuite );

			resolveImportedTestSuite( testSuite );
		}
	}

	private void resolveImportedTestSuite( WsdlTestSuite testSuite )
	{
		ResolveDialog resolver = new ResolveDialog( "Validate TestSuite", "Checks TestSuite for inconsistencies", null );
		resolver.setShowOkMessage( false );
		resolver.resolve( testSuite );
	}

	/**
	 * @see com.eviware.soapui.impl.WsdlInterfaceFactory.importWsdl
	 * @deprecated
	 */

	public WsdlInterface[] importWsdl( String url, boolean createRequests ) throws SoapUIException
	{
		return WsdlInterfaceFactory.importWsdl( this, url, createRequests );
	}

	/**
	 * @see com.eviware.soapui.impl.WsdlInterfaceFactory.importWsdl
	 * @deprecated see WsdlInterfaceFactory
	 */

	public WsdlInterface[] importWsdl( String url, boolean createRequests, WsdlLoader wsdlLoader )
			throws SoapUIException
	{
		return WsdlInterfaceFactory.importWsdl( this, url, createRequests, null, wsdlLoader );
	}

	/**
	 * @see com.eviware.soapui.impl.WsdlInterfaceFactory.importWsdl
	 * @deprecated see WsdlInterfaceFactory
	 */

	public WsdlInterface[] importWsdl( String url, boolean createRequests, QName bindingName, WsdlLoader wsdlLoader )
			throws SoapUIException
	{
		return WsdlInterfaceFactory.importWsdl( this, url, createRequests, bindingName, wsdlLoader );
	}

	public void setDefaultScriptLanguage( String id )
	{
		getConfig().setDefaultScriptLanguage( id );
	}

	public String getDefaultScriptLanguage()
	{
		if( getConfig().isSetDefaultScriptLanguage() )
			return getConfig().getDefaultScriptLanguage();
		else
			return SoapUIScriptEngineRegistry.DEFAULT_SCRIPT_ENGINE_ID;
	}

	public int getIndexOfTestSuite( TestSuite testSuite )
	{
		return testSuites.indexOf( testSuite );
	}

	public String getBeforeRunScript()
	{
		return getConfig().isSetBeforeRunScript() ? getConfig().getBeforeRunScript().getStringValue() : null;
	}

	public void setBeforeRunScript( String script )
	{
		String oldScript = getBeforeRunScript();

		if( !getConfig().isSetBeforeRunScript() )
			getConfig().addNewBeforeRunScript();

		getConfig().getBeforeRunScript().setStringValue( script );
		if( beforeRunScriptEngine != null )
			beforeRunScriptEngine.setScript( script );

		notifyPropertyChanged( "beforeRunScript", oldScript, script );
	}

	public Object runBeforeRunScript( ProjectRunContext context, ProjectRunner runner ) throws Exception
	{
		String script = getBeforeRunScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( beforeRunScriptEngine == null )
		{
			beforeRunScriptEngine = SoapUIScriptEngineRegistry.create( this );
			beforeRunScriptEngine.setScript( script );
		}

		beforeRunScriptEngine.setVariable( "runner", runner );
		beforeRunScriptEngine.setVariable( "context", context );
		beforeRunScriptEngine.setVariable( "project", this );
		beforeRunScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return beforeRunScriptEngine.run();
	}

	public String getAfterRunScript()
	{
		return getConfig().isSetAfterRunScript() ? getConfig().getAfterRunScript().getStringValue() : null;
	}

	public void setAfterRunScript( String script )
	{
		String oldScript = getAfterRunScript();

		if( !getConfig().isSetAfterRunScript() )
			getConfig().addNewAfterRunScript();

		getConfig().getAfterRunScript().setStringValue( script );
		if( afterRunScriptEngine != null )
			afterRunScriptEngine.setScript( script );

		notifyPropertyChanged( "afterRunScript", oldScript, script );
	}

	public Object runAfterRunScript( ProjectRunContext context, ProjectRunner runner ) throws Exception
	{
		String script = getAfterRunScript();
		if( StringUtils.isNullOrEmpty( script ) )
			return null;

		if( afterRunScriptEngine == null )
		{
			afterRunScriptEngine = SoapUIScriptEngineRegistry.create( this );
			afterRunScriptEngine.setScript( script );
		}

		afterRunScriptEngine.setVariable( "runner", runner );
		afterRunScriptEngine.setVariable( "context", context );
		afterRunScriptEngine.setVariable( "project", this );
		afterRunScriptEngine.setVariable( "log", SoapUI.ensureGroovyLog() );
		return afterRunScriptEngine.run();
	}

	public void addProjectRunListener( ProjectRunListener projectRunListener )
	{
		runListeners.add( projectRunListener );
	}

	public void removeProjectRunListener( ProjectRunListener projectRunListener )
	{
		runListeners.remove( projectRunListener );
	}

	public WsdlProjectRunner run( StringToObjectMap context, boolean async )
	{
		WsdlProjectRunner runner = new WsdlProjectRunner( this, context );
		runner.start( async );
		return runner;
	}

	public boolean isAbortOnError()
	{
		return getConfig().getAbortOnError();
	}

	// public boolean isFailOnErrors()
	// {
	// return getConfig().getFailOnErrors();
	// }
	//	
	// public void setFailOnErrors( boolean arg0 )
	// {
	// getConfig().setFailOnErrors( arg0 );
	// }

	public void setAbortOnError( boolean arg0 )
	{
		getConfig().setAbortOnError( arg0 );
	}

	public long getTimeout()
	{
		return getConfig().getTimeout();
	}

	public void setTimeout( long timeout )
	{
		getConfig().setTimeout( timeout );
	}

	public ProjectRunListener[] getProjectRunListeners()
	{
		return runListeners.toArray( new ProjectRunListener[runListeners.size()] );
	}

	public TestSuiteRunType getRunType()
	{
		Enum runType = getConfig().getRunType();

		if( TestSuiteRunTypesConfig.PARALLELL.equals( runType ) )
			return TestSuiteRunType.PARALLEL;
		else
			return TestSuiteRunType.SEQUENTIAL;
	}

	public void setRunType( TestSuiteRunType runType )
	{
		TestSuiteRunType oldRunType = getRunType();

		if( runType == TestSuiteRunType.PARALLEL && oldRunType != TestSuiteRunType.PARALLEL )
		{
			getConfig().setRunType( TestSuiteRunTypesConfig.PARALLELL );
			notifyPropertyChanged( "runType", oldRunType, runType );
		}
		else if( runType == TestSuiteRunType.SEQUENTIAL && oldRunType != TestSuiteRunType.SEQUENTIAL )
		{
			getConfig().setRunType( TestSuiteRunTypesConfig.SEQUENTIAL );
			notifyPropertyChanged( "runType", oldRunType, runType );
		}
	}

	public WsdlTestSuite moveTestSuite( int ix, int offset )
	{
		WsdlTestSuite testSuite = testSuites.get( ix );

		if( offset == 0 )
			return testSuite;

		testSuites.remove( ix );
		testSuites.add( ix + offset, testSuite );

		TestSuiteConfig[] configs = new TestSuiteConfig[testSuites.size()];

		for( int c = 0; c < testSuites.size(); c++ )
		{
			if( offset > 0 )
			{
				if( c < ix )
					configs[c] = ( TestSuiteConfig )getConfig().getTestSuiteArray( c ).copy();
				else if( c < ( ix + offset ) )
					configs[c] = ( TestSuiteConfig )getConfig().getTestSuiteArray( c + 1 ).copy();
				else if( c == ix + offset )
					configs[c] = ( TestSuiteConfig )getConfig().getTestSuiteArray( ix ).copy();
				else
					configs[c] = ( TestSuiteConfig )getConfig().getTestSuiteArray( c ).copy();
			}
			else
			{
				if( c < ix + offset )
					configs[c] = ( TestSuiteConfig )getConfig().getTestSuiteArray( c ).copy();
				else if( c == ix + offset )
					configs[c] = ( TestSuiteConfig )getConfig().getTestSuiteArray( ix ).copy();
				else if( c <= ix )
					configs[c] = ( TestSuiteConfig )getConfig().getTestSuiteArray( c - 1 ).copy();
				else
					configs[c] = ( TestSuiteConfig )getConfig().getTestSuiteArray( c ).copy();
			}
		}

		getConfig().setTestSuiteArray( configs );
		for( int c = 0; c < configs.length; c++ )
		{
			testSuites.get( c ).resetConfigOnMove( getConfig().getTestSuiteArray( c ) );
		}

		fireTestSuiteMoved( testSuite, ix, offset );
		return testSuite;

	}

	public void importMockService( File file )
	{
		if( !file.exists() )
		{
			UISupport.showErrorMessage( "Error loading test case " );
			return;
		}

		MockServiceDocumentConfig newMockServiceConfig = null;

		try
		{
			newMockServiceConfig = MockServiceDocumentConfig.Factory.parse( file );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		if( newMockServiceConfig == null )
		{
			UISupport.showErrorMessage( "Not valid mock service xml" );
		}
		else
		{
			MockServiceConfig config = ( MockServiceConfig )projectDocument.getSoapuiProject().addNewMockService().set(
					newMockServiceConfig.getMockService() );
			WsdlMockService mockService = new WsdlMockService( this, config );

			ModelSupport.unsetIds( mockService );
			mockService.afterLoad();

			mockServices.add( mockService );
			fireMockServiceAdded( mockService );

			resolveImportedMockService( mockService );
		}
	}

	private void resolveImportedMockService( WsdlMockService mockService )
	{
		ResolveDialog resolver = new ResolveDialog( "Validate MockService", "Checks MockService for inconsistencies",
				null );
		resolver.setShowOkMessage( false );
		resolver.resolve( mockService );
	}
}
