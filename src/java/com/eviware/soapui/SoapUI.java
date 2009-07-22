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

package com.eviware.soapui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.eviware.soapui.actions.SaveAllProjectsAction;
import com.eviware.soapui.actions.SoapUIPreferencesAction;
import com.eviware.soapui.actions.SwitchDesktopPanelAction;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.actions.ImportWsdlProjectAction;
import com.eviware.soapui.impl.actions.NewWsdlProjectAction;
import com.eviware.soapui.impl.rest.actions.project.NewRestServiceAction;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.axis1.Axis1XWSDL2JavaAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.axis2.Axis2WSDL2CodeAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.cxf.CXFAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.dotnet.DotNetWsdlAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.gsoap.GSoapAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.jaxb.JaxbXjcAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.jbossws.JBossWSConsumeAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.jbossws.WSToolsWsdl2JavaAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.oracle.OracleWsaGenProxyAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.tcpmon.TcpMonAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wscompile.WSCompileAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.wsimport.WSImportAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.xfire.XFireAction;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.xmlbeans.XmlBeans2Action;
import com.eviware.soapui.impl.wsdl.actions.support.OpenUrlAction;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.PropertyHolderTable;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.PanelBuilder;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.settings.SettingsListener;
import com.eviware.soapui.model.tree.SoapUITreeNode;
import com.eviware.soapui.model.util.PanelBuilderRegistry;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceFactory;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.monitor.TestMonitor;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.ActionListBuilder;
import com.eviware.soapui.support.action.swing.ActionSupport;
import com.eviware.soapui.support.action.swing.SwingActionDelegate;
import com.eviware.soapui.support.components.BrowserComponent;
import com.eviware.soapui.support.components.JComponentInspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.dnd.DropType;
import com.eviware.soapui.support.dnd.NavigatorDragAndDropable;
import com.eviware.soapui.support.dnd.SoapUIDragAndDropHandler;
import com.eviware.soapui.support.listener.SoapUIListenerRegistry;
import com.eviware.soapui.support.log.InspectorLog4JMonitor;
import com.eviware.soapui.support.log.JLogList;
import com.eviware.soapui.support.log.Log4JMonitor;
import com.eviware.soapui.support.log.LogDisablingTestMonitorListener;
import com.eviware.soapui.support.monitor.MonitorPanel;
import com.eviware.soapui.support.monitor.RuntimeMemoryMonitorSource;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.ui.JDesktopPanelsList;
import com.eviware.soapui.ui.Navigator;
import com.eviware.soapui.ui.NavigatorListener;
import com.eviware.soapui.ui.URLDesktopPanel;
import com.eviware.soapui.ui.desktop.DesktopPanel;
import com.eviware.soapui.ui.desktop.DesktopRegistry;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;
import com.eviware.soapui.ui.desktop.standalone.StandaloneDesktop;
import com.eviware.soapui.ui.support.DesktopListenerAdapter;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;

/**
 * Main SoapUI entry point.
 */

public class SoapUI
{
	public static final String DEFAULT_DESKTOP = "Default";
	public static final String CURRENT_SOAPUI_WORKSPACE = SoapUI.class.getName() + "@workspace";
	public final static Logger log = Logger.getLogger( SoapUI.class );
	public final static String SOAPUI_VERSION = "3.0";
	public static final String DEFAULT_WORKSPACE_FILE = "default-soapui-workspace.xml";
	public static final String SOAPUI_SPLASH = "soapui-splash.jpg";
	private static final int DEFAULT_DESKTOP_ACTIONS_COUNT = 3;
	public static final String BUILDINFO_RESOURCE = "/com/eviware/soapui/resources/conf/buildinfo.txt";

	@SuppressWarnings( "deprecation" )
	public static String PUSH_PAGE_URL = "http://www.soapui.org/appindex/soapui_starterpage.php?version="
			+ URLEncoder.encode( SOAPUI_VERSION );
	public static String FRAME_ICON = "/16-perc.gif";
	public static String PUSH_PAGE_ERROR_URL = "file://" + System.getProperty("soapui.home", ".") + "/starter-page.html";

	// ------------------------------ FIELDS ------------------------------

	// private static SoapUI instance;
	private static List<Object> logCache = new ArrayList<Object>();

	private static SoapUICore soapUICore;
	private static JFrame frame;

	private static Navigator navigator;
	private static SoapUIDesktop desktop;
	private static Workspace workspace;
	private static Log4JMonitor logMonitor;
	private static Logger errorLog = Logger.getLogger( "soapui.errorlog" );
	private static boolean isStandalone;
	private static TestMonitor testMonitor;

	private JMenu desktopMenu;
	private JMenu helpMenu;
	private JMenu fileMenu;
	private static JMenuBar menuBar;
	private JDesktopPanelsList desktopPanelsList;

	public static boolean checkedGroovyLogMonitor;

	private JPanel overviewPanel;
	private JMenu toolsMenu;
	private boolean saveOnExit = true;
	private InternalDesktopListener internalDesktopListener = new InternalDesktopListener();
	private JInspectorPanel mainInspector;

	private static Timer backgroundTimer;
	private static AutoSaveTimerTask autoSaveTimerTask;
	private static String workspaceName;
	private static StringToStringMap projectOptions = new StringToStringMap();
	private static URLDesktopPanel urlDesktopPanel;
	private static JXToolBar mainToolbar;
	private static String[] mainArgs;
	private static GCTimerTask gcTimerTask;

	private final static ExecutorService threadPool = Executors.newCachedThreadPool();
	private JTextField searchField;

	// --------------------------- CONSTRUCTORS ---------------------------

	private SoapUI()
	{
	}

	private void buildUI()
	{
		frame.addWindowListener( new MainFrameWindowListener() );
		UISupport.setMainFrame( frame );

		navigator = new Navigator( workspace );
		navigator.addNavigatorListener( new InternalNavigatorListener() );

		desktopPanelsList = new JDesktopPanelsList( desktop );

		mainInspector = JInspectorPanelFactory.build( buildContentPanel(), SwingConstants.LEFT );
		mainInspector.addInspector( new JComponentInspector<JComponent>( buildMainPanel(), "Navigator",
				"The soapUI Navigator", true ) );
		mainInspector.setCurrentInspector( "Navigator" );

		frame.setJMenuBar( buildMainMenu() );
		frame.getContentPane().add( buildToolbar(), BorderLayout.NORTH );
		frame.getContentPane().add( mainInspector.getComponent(), BorderLayout.CENTER );
		frame.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
		frame.setSize( 1000, 750 );

		mainInspector.setDividerLocation( 250 );
		mainInspector.setResizeWeight( 0.1 );
		navigator.selectModelItem( workspace );

		desktop.addDesktopListener( internalDesktopListener );

		ToolTipManager.sharedInstance().setInitialDelay( 200 );

		JTree mainTree = navigator.getMainTree();
		DragSource dragSource = DragSource.getDefaultDragSource();
		SoapUIDragAndDropHandler navigatorDragAndDropHandler = new SoapUIDragAndDropHandler(
				new NavigatorDragAndDropable( mainTree ), DropType.ON + DropType.AFTER );

		dragSource.createDefaultDragGestureRecognizer( mainTree, DnDConstants.ACTION_COPY_OR_MOVE,
				navigatorDragAndDropHandler );

		desktop.init();
	}

	private JComponent buildToolbar()
	{
		mainToolbar = new JXToolBar();
		mainToolbar.setRollover( true );
		mainToolbar.putClientProperty( Options.HEADER_STYLE_KEY, HeaderStyle.BOTH );
		mainToolbar.add( new NewWsdlProjectActionDelegate() );
		mainToolbar.add( new ImportWsdlProjectActionDelegate() );
		mainToolbar.add( new SaveAllActionDelegate() );
		mainToolbar.addSeparator();
		mainToolbar.add( new ShowOnlineHelpAction( "User Guide", HelpUrls.USERGUIDE_HELP_URL,
				"Opens the soapUI User-Guide in a browser" ) );
		mainToolbar.add( new ShowOnlineHelpAction( "Forum", HelpUrls.FORUMS_HELP_URL,
				"Opens the soapUI Forum in a browser", "/group_go.png" ) );
		mainToolbar.addSeparator();
		mainToolbar.add( new ShowOnlineHelpAction( "Trial", HelpUrls.TRIAL_URL, "Apply for soapUI Pro Trial License",
				"/favicon.png" ) );
		mainToolbar.addSeparator();
		mainToolbar.add( new PreferencesActionDelegate() );
		mainToolbar.add( new ExitButtonAction() );
		mainToolbar.addGlue();

		searchField = new JTextField( 20 );
		searchField.addKeyListener( new KeyAdapter()
		{
			@Override
			public void keyTyped( KeyEvent e )
			{
				if( e.getKeyChar() == '\n' )
				{
					doForumSearch( searchField.getText() );
				}
			}
		} );

		mainToolbar.addLabeledFixed( "Search Forum", searchField );
		mainToolbar.add( new ToolbarForumSearchAction() );
		mainToolbar.add( new ShowOnlineHelpAction( HelpUrls.USERGUIDE_HELP_URL ) );

		mainToolbar.setBorder( BorderFactory.createEtchedBorder() );

		return mainToolbar;
	}

	@SuppressWarnings( "deprecation" )
	public void doForumSearch( String text )
	{
		if( !searchField.getText().equals( text ) )
			searchField.setText( text );

		if( StringUtils.hasContent( text ) )
		{
			Tools.openURL( "http://www.google.com/search?q=site:eviware.com+" + URLEncoder.encode( text.trim() )
					+ "&hl=en&tbs=clue:1&tbo=1" );
			
//			Tools.openURL( "http://www.eviware.com/forums/index.php?action=search2&search="
//					+ URLEncoder.encode( text.trim() ) );
		}
		else
		{
			Tools.openURL( "http://www.eviware.com/forums" );
		}
	}

	private JMenuBar buildMainMenu()
	{
		menuBar = new JMenuBar();
		menuBar.putClientProperty( Options.HEADER_STYLE_KEY, HeaderStyle.BOTH );

		menuBar.add( buildFileMenu() );
		menuBar.add( buildToolsMenu() );
		menuBar.add( buildDesktopMenu() );
		menuBar.add( buildHelpMenu() );

		return menuBar;
	}

	public static ExecutorService getThreadPool()
	{
		return threadPool;
	}

	public static Workspace getWorkspace()
	{
		return workspace;
	}

	private JMenu buildDesktopMenu()
	{
		desktopMenu = new JMenu( "Desktop" );
		desktopMenu.setMnemonic( KeyEvent.VK_D );
		desktopMenu.add( new SwitchDesktopPanelAction( desktopPanelsList ) );
		desktopMenu.add( new MaximizeDesktopAction( ( InspectorLog4JMonitor )logMonitor ) );
		desktopMenu.addSeparator();

		ActionSupport.addActions( desktop.getActions(), desktopMenu );

		return desktopMenu;
	}

	private JMenu buildHelpMenu()
	{
		helpMenu = new JMenu( "Help" );
		helpMenu.setMnemonic( KeyEvent.VK_H );

		helpMenu.add( new ShowPushPageAction() );
		helpMenu.addSeparator();
		helpMenu.add( new ShowOnlineHelpAction( "User Guide", HelpUrls.USERGUIDE_HELP_URL ) );
		helpMenu.add( new ShowOnlineHelpAction( "Getting Started", HelpUrls.GETTINGSTARTED_HELP_URL ) );
		helpMenu.add( new SearchForumAction() );
		helpMenu.addSeparator();
		helpMenu.add( new ShowSystemPropertiesAction() );
		helpMenu.addSeparator();
		helpMenu.add( new OpenUrlAction( "soapui.org", "http://www.soapui.org" ) );
		helpMenu.add( new ShowOnlineHelpAction( "soapUI Pro Trial", HelpUrls.TRIAL_URL,
				"Apply for soapUI Pro Trial License", "/favicon.png" ) );
		helpMenu.addSeparator();
		helpMenu.add( new AboutAction() );
		return helpMenu;
	}

	private JMenu buildToolsMenu()
	{
		toolsMenu = new JMenu( "Tools" );
		toolsMenu.setMnemonic( KeyEvent.VK_T );

		toolsMenu.add( SwingActionDelegate.createDelegate( WSToolsWsdl2JavaAction.SOAPUI_ACTION_ID ) );
		toolsMenu.add( SwingActionDelegate.createDelegate( JBossWSConsumeAction.SOAPUI_ACTION_ID ) );
		toolsMenu.addSeparator();
		toolsMenu.add( SwingActionDelegate.createDelegate( WSCompileAction.SOAPUI_ACTION_ID ) );
		toolsMenu.add( SwingActionDelegate.createDelegate( WSImportAction.SOAPUI_ACTION_ID ) );
		toolsMenu.addSeparator();
		toolsMenu.add( SwingActionDelegate.createDelegate( Axis1XWSDL2JavaAction.SOAPUI_ACTION_ID ) );
		toolsMenu.add( SwingActionDelegate.createDelegate( Axis2WSDL2CodeAction.SOAPUI_ACTION_ID ) );
		toolsMenu.add( SwingActionDelegate.createDelegate( CXFAction.SOAPUI_ACTION_ID ) );
		toolsMenu.add( SwingActionDelegate.createDelegate( XFireAction.SOAPUI_ACTION_ID ) );
		toolsMenu.add( SwingActionDelegate.createDelegate( OracleWsaGenProxyAction.SOAPUI_ACTION_ID ) );
		toolsMenu.addSeparator();
		toolsMenu.add( SwingActionDelegate.createDelegate( XmlBeans2Action.SOAPUI_ACTION_ID ) );
		toolsMenu.add( SwingActionDelegate.createDelegate( JaxbXjcAction.SOAPUI_ACTION_ID ) );
		toolsMenu.addSeparator();
		toolsMenu.add( SwingActionDelegate.createDelegate( DotNetWsdlAction.SOAPUI_ACTION_ID ) );
		toolsMenu.add( SwingActionDelegate.createDelegate( GSoapAction.SOAPUI_ACTION_ID ) );
		toolsMenu.addSeparator();
		toolsMenu.add( SwingActionDelegate.createDelegate( TcpMonAction.SOAPUI_ACTION_ID ) );
		// toolsMenu.addSeparator();
		// toolsMenu.add( new XQueryXPathTesterAction());

		return toolsMenu;
	}

	private JMenu buildFileMenu()
	{
		fileMenu = new JMenu( "File" );
		fileMenu.setMnemonic( KeyEvent.VK_F );

		ActionList actions = ActionListBuilder.buildActions( workspace );
		actions.removeAction( actions.getActionCount() - 1 );

		ActionSupport.addActions( actions, fileMenu );

		fileMenu.add( SoapUIPreferencesAction.getInstance() );
		fileMenu.add( new SavePreferencesAction() );
		fileMenu.add( new ImportPreferencesAction() );

		fileMenu.addSeparator();
		fileMenu.add( buildRecentMenu() );
		fileMenu.addSeparator();
		fileMenu.add( new ExitAction() );
		fileMenu.add( new ExitWithoutSavingAction() );
		fileMenu.addSeparator();
		fileMenu.add( new ShowOnlineHelpAction( HelpUrls.OVERVIEW_HELP_URL ) );

		return fileMenu;
	}

	private JMenuItem buildRecentMenu()
	{
		JMenu recentMenu = new JMenu( "Recent" );

		JMenu recentProjectsMenu = new JMenu( "Projects" );
		JMenu recentWorkspacesMenu = new JMenu( "Workspaces" );
		JMenu recentEditorsMenu = new JMenu( "Editors" );

		recentMenu.add( recentEditorsMenu );
		recentMenu.add( recentProjectsMenu );
		recentMenu.add( recentWorkspacesMenu );

		RecentItemsListener recentItemsListener = new RecentItemsListener( recentWorkspacesMenu, recentProjectsMenu,
				recentEditorsMenu );
		workspace.addWorkspaceListener( recentItemsListener );
		desktop.addDesktopListener( recentItemsListener );

		return recentMenu;
	}

	public JFrame getFrame()
	{
		return frame;
	}

	private JComponent buildMainPanel()
	{
		JInspectorPanel inspectorPanel = JInspectorPanelFactory.build( navigator );
		inspectorPanel.addInspector( new JComponentInspector<JComponent>( buildOverviewPanel(), "Properties",
				"Properties for the currently selected item", true ) );
		inspectorPanel.setDividerLocation( 500 );
		inspectorPanel.setResizeWeight( 0.6 );
		inspectorPanel.setCurrentInspector( "Properties" );

		return inspectorPanel.getComponent();
	}

	private JComponent buildOverviewPanel()
	{
		overviewPanel = new JPanel( new BorderLayout() );
		overviewPanel.setBorder( BorderFactory.createEmptyBorder( 3, 0, 0, 2 ) );

		return overviewPanel;
	}

	private void setOverviewPanel( Component panel )
	{
		if( overviewPanel.getComponentCount() == 0 && panel == null )
			return;

		overviewPanel.removeAll();
		if( panel != null )
			overviewPanel.add( panel, BorderLayout.CENTER );
		overviewPanel.revalidate();
		overviewPanel.repaint();
	}

	private JComponent buildContentPanel()
	{
		return buildLogPanel( true, "soapUI log" );
	}

	private JComponent buildLogPanel( boolean hasDefault, String defaultName )
	{
		InspectorLog4JMonitor inspectorLog4JMonitor = new InspectorLog4JMonitor( desktop.getDesktopComponent() );

		JComponent monitor = initLogMonitor( hasDefault, defaultName, inspectorLog4JMonitor );

		if( !SoapUI.getSettings().getBoolean( UISettings.SHOW_LOGS_AT_STARTUP ) )
			inspectorLog4JMonitor.activate( null );

		MonitorPanel monitorPanel = new MonitorPanel( new RuntimeMemoryMonitorSource() );
		monitorPanel.start();
		inspectorLog4JMonitor.addInspector( new JComponentInspector<JComponent>( monitorPanel, "memory log",
				"Shows runtime memory consumption", true ) );

		return monitor;
	}

	public static JComponent initLogMonitor( boolean hasDefault, String defaultName, Log4JMonitor logMonitor )
	{
		SoapUI.logMonitor = logMonitor;
		logMonitor.addLogArea( defaultName, "com.eviware.soapui", hasDefault ).setLevel( Level.DEBUG );
		logMonitor.addLogArea( "http log", "httpclient.wire", false ).setLevel( Level.DEBUG );
		logMonitor.addLogArea( "jetty log", "jetty", false ).setLevel( Level.INFO );
		logMonitor.addLogArea( "error log", "soapui.errorlog", false ).setLevel( Level.DEBUG );
		logMonitor.addLogArea( "wsrm log", "wsrm", false ).setLevel( Level.INFO );

		for( Object message : logCache )
		{
			logMonitor.logEvent( message );
		}

		return logMonitor.getComponent();
	}

	// -------------------------- OTHER METHODS --------------------------

	public static synchronized void log( final Object msg )
	{
		if( logMonitor == null )
		{
			logCache.add( msg );
			return;
		}

		if( SwingUtilities.isEventDispatchThread() )
		{
			logMonitor.logEvent( msg );
		}
		else
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					logMonitor.logEvent( msg );
				}
			} );
		}
	}

	// -------------------------- INNER CLASSES --------------------------

	private static final class WsdlProjectCreator implements Runnable
	{
		private final String arg;

		public WsdlProjectCreator( String arg )
		{
			this.arg = arg;
		}

		public void run()
		{
			SoapUIAction<ModelItem> action = getActionRegistry().getAction( NewWsdlProjectAction.SOAPUI_ACTION_ID );
			if( action != null )
				action.perform( getWorkspace(), arg );
		}
	}

	private static final class RestProjectCreator implements Runnable
	{
		private final URL arg;

		public RestProjectCreator( URL arg )
		{
			this.arg = arg;
		}

		public void run()
		{
			try
			{
				WsdlProject project = ( WsdlProject )getWorkspace().createProject( arg.getHost(), null );
				SoapUIAction<ModelItem> action = getActionRegistry().getAction( NewRestServiceAction.SOAPUI_ACTION_ID );
				if( action != null )
					action.perform( project, arg );
			}
			catch( SoapUIException e )
			{
				e.printStackTrace();
			}
		}
	}

	private final class InternalDesktopListener extends DesktopListenerAdapter
	{
		public void desktopPanelSelected( DesktopPanel desktopPanel )
		{
			ModelItem modelItem = desktopPanel.getModelItem();
			if( modelItem != null )
				navigator.selectModelItem( modelItem );
		}
	}

	private final class MainFrameWindowListener extends WindowAdapter
	{
		public void windowClosing( WindowEvent e )
		{
			if( onExit() )
				frame.dispose();
		}

		public void windowClosed( WindowEvent e )
		{
			System.out.println( "exiting.." );
			System.exit( 0 );
		}
	}

	public static void main( String[] args ) throws Exception
	{
		mainArgs = args;
		startSoapUI( mainArgs, "soapUI " + SOAPUI_VERSION, SOAPUI_SPLASH, new StandaloneSoapUICore( true ) );
	}

	public static String[] getMainArgs()
	{
		return mainArgs;
	}

	public static SoapUI startSoapUI( String[] args, String title, String splashImage, SwingSoapUICore core )
			throws Exception
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		System.setProperty( "com.apple.mrj.application.apple.menu.about.name", "SoapUI" );

		frame = new JFrame( title );

		SoapUISplash splash = new SoapUISplash( splashImage, frame );

		frame.setIconImage( UISupport.createImageIcon( FRAME_ICON ).getImage() );

		JPopupMenu.setDefaultLightWeightPopupEnabled( false );
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled( false );

		isStandalone = true;
		soapUICore = core;

		SoapUI soapUI = new SoapUI();
		Workspace workspace = null;

		org.apache.commons.cli.Options options = initSoapUIOptions();
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args );

		if( !validateCommandLineArgs( cmd, options ) )
		{
			System.exit( 1 );
		}

		if( workspaceName != null )
		{
			workspace = WorkspaceFactory.getInstance().openWorkspace( workspaceName, projectOptions );
			soapUICore.getSettings().setString( CURRENT_SOAPUI_WORKSPACE, workspaceName );
		}
		else
		{
			String wsfile = soapUICore.getSettings().getString( CURRENT_SOAPUI_WORKSPACE,
					System.getProperty( "user.home" ) + File.separatorChar + DEFAULT_WORKSPACE_FILE );
			try
			{
				workspace = WorkspaceFactory.getInstance().openWorkspace( wsfile, projectOptions );
			}
			catch( Exception e )
			{
				if( UISupport
						.confirm( "Failed to open workspace: [" + e.toString() + "], create new one instead?", "Error" ) )
				{
					new File( wsfile ).delete();
					workspace = WorkspaceFactory.getInstance().openWorkspace( wsfile, projectOptions );
				}
				else
				{
					System.exit( 1 );
				}
			}
		}

		core.prepareUI();
		soapUI.show( workspace );
		core.afterStartup( workspace );
		Thread.sleep( 500 );
		splash.setVisible( false );

		if( getSettings().getBoolean( UISettings.SHOW_STARTUP_PAGE ) && !BrowserComponent.isJXBrowserDisabled() )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					showPushPage();
				}
			} );
		}

		frame.setSize( 1000, 750 );

		String[] args2 = cmd.getArgs();
		if( args2 != null && args2.length > 0 )
		{
			String arg = args2[0];
			if( arg.toUpperCase().endsWith( ".WSDL" ) || arg.toUpperCase().endsWith( ".WADL" ) )
			{
				SwingUtilities.invokeLater( new WsdlProjectCreator( arg ) );
			}
			else
			{
				try
				{
					URL url = new URL( arg );
					SwingUtilities.invokeLater( new RestProjectCreator( url ) );
				}
				catch( Exception e )
				{
				}
			}
		}

		return soapUI;
	}

	private static boolean validateCommandLineArgs( CommandLine cmd, org.apache.commons.cli.Options options )
	{
		if( cmd.hasOption( 'w' ) )
		{
			workspaceName = cmd.getOptionValue( 'w' );
		}

		if( cmd.hasOption( 'p' ) )
		{
			for( String projectNamePassword : cmd.getOptionValues( 'p' ) )
			{
				String[] nameAndPassword = projectNamePassword.split( ":" );
				projectOptions.put( nameAndPassword[0], nameAndPassword[1] );
			}
		}

		return true;
	}

	private static org.apache.commons.cli.Options initSoapUIOptions()
	{

		org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
		options.addOption( "w", true, "Specified the name of the workspace xml file" );
		options.addOption( "p", true, "Sets project name and its password in format <project name>:<password>" );

		return options;
	}

	public static SoapUICore getSoapUICore()
	{
		return soapUICore;
	}

	public static TestPropertyHolder getGlobalProperties()
	{
		return PropertyExpansionUtils.getGlobalProperties();
	}

	public static void setSoapUICore( SoapUICore soapUICore )
	{
		SoapUI.soapUICore = soapUICore;
	}

	public static boolean isStandalone()
	{
		return isStandalone;
	}

	public static JMenuBar getMenuBar()
	{
		return menuBar;
	}

	private void show( Workspace workspace )
	{
		SoapUI.workspace = workspace;

		String desktopType = soapUICore.getSettings().getString( UISettings.DESKTOP_TYPE, SoapUI.DEFAULT_DESKTOP );
		desktop = DesktopRegistry.getInstance().createDesktop( desktopType, workspace );

		if( desktop == null )
			desktop = new StandaloneDesktop( workspace );

		if( testMonitor == null )
			testMonitor = new TestMonitor();

		soapUICore.getSettings().addSettingsListener( new SettingsListener()
		{
			public void settingChanged( String name, String newValue, String oldValue )
			{
				if( name.equals( UISettings.DESKTOP_TYPE ) )
				{
					changeDesktop( DesktopRegistry.getInstance().createDesktop( newValue, SoapUI.workspace ) );
				}
			}
		} );

		buildUI();

		testMonitor.addTestMonitorListener( new LogDisablingTestMonitorListener() );
		testMonitor.init( workspace );
		frame.setVisible( true );

		initAutoSaveTimer();
		initGCTimer();
	}

	private void changeDesktop( SoapUIDesktop newDesktop )
	{
		desktopPanelsList.setDesktop( newDesktop );
		desktop.removeDesktopListener( internalDesktopListener );

		desktop.transferTo( newDesktop );
		desktop.release();

		desktop = newDesktop;

		if( logMonitor instanceof InspectorLog4JMonitor )
			( ( InspectorLog4JMonitor )logMonitor ).setContentComponent( desktop.getDesktopComponent() );

		desktop.addDesktopListener( internalDesktopListener );

		while( desktopMenu.getItemCount() > DEFAULT_DESKTOP_ACTIONS_COUNT )
			desktopMenu.remove( DEFAULT_DESKTOP_ACTIONS_COUNT );

		ActionSupport.addActions( desktop.getActions(), desktopMenu );

		desktop.init();
	}

	protected boolean onExit()
	{
		if( saveOnExit )
		{
			String question = "Exit SoapUI?";

			if( getTestMonitor().hasRunningTests() )
				question += "\n(Projects with running tests will not be saved)";

			if( !UISupport.confirm( question, "Question" ) )
				return false;

			try
			{
				PropertyExpansionUtils.saveGlobalProperties();
				soapUICore.saveSettings();
				workspace.onClose();
			}
			catch( Exception e1 )
			{
				SoapUI.logError( e1 );
			}
		}
		else
		{
			if( !UISupport.confirm( "Exit SoapUI without saving?", "Question" ) )
			{
				saveOnExit = true;
				return false;
			}
		}

		return true;
	}

	public static void logError( Throwable e )
	{
		logError( e, null );
	}

	public static void logError( Throwable e, String message )
	{
		String msg = e.getMessage();
		if( msg == null )
			msg = e.toString();

		log.error( "An error occured [" + msg + "], see error log for details" );

		if( message != null )
			errorLog.error( message );

		errorLog.error( e.toString(), e );
		if( !isStandalone() || "true".equals( System.getProperty( "soapui.stacktrace" ) ) )
			e.printStackTrace();
	}

	public static Logger getErrorLog()
	{
		return errorLog;
	}

	public static synchronized Logger ensureGroovyLog()
	{
		if( !checkedGroovyLogMonitor )
		{
			Log4JMonitor logMonitor = getLogMonitor();
			if( logMonitor != null && !logMonitor.hasLogArea( "groovy.log" ) )
			{
				logMonitor.addLogArea( "script log", "groovy.log", false );
				checkedGroovyLogMonitor = true;
			}
		}

		return Logger.getLogger( "groovy.log" );
	}

	public class InternalNavigatorListener implements NavigatorListener
	{
		public void nodeSelected( SoapUITreeNode treeNode )
		{
			if( treeNode == null )
			{
				setOverviewPanel( null );
			}
			else
			{
				ModelItem modelItem = treeNode.getModelItem();
				PropertyHolderTable propertyHolderTable = null;

				if( modelItem instanceof TestPropertyHolder )
				{
					// check for closed project -> this should be solved with a
					// separate ClosedWsdlProject modelItem
					if( !( modelItem instanceof WsdlProject ) || ( ( WsdlProject )modelItem ).isOpen() )
					{
						propertyHolderTable = new PropertyHolderTable( ( TestPropertyHolder )modelItem );
					}
				}

				PanelBuilder<ModelItem> panelBuilder = PanelBuilderRegistry.getPanelBuilder( modelItem );
				if( panelBuilder != null && panelBuilder.hasOverviewPanel() )
				{
					Component overviewPanel = panelBuilder.buildOverviewPanel( modelItem );
					if( propertyHolderTable != null )
					{
						JTabbedPane tabs = new JTabbedPane();
						if( overviewPanel instanceof JPropertiesTable )
						{
							JPropertiesTable<?> t = ( JPropertiesTable<?> )overviewPanel;
							tabs.addTab( t.getTitle(), overviewPanel );
							t.setTitle( null );
						}
						else
						{
							tabs.addTab( "Overview", overviewPanel );
						}

						tabs.addTab( ( ( TestPropertyHolder )modelItem ).getPropertiesLabel(), propertyHolderTable );
						overviewPanel = UISupport.createTabPanel( tabs, false );
					}

					setOverviewPanel( overviewPanel );
				}
				else
				{
					setOverviewPanel( null );
				}
			}
		}
	}

	private class ExitAction extends AbstractAction
	{
		public ExitAction()
		{
			super( "Exit" );
			putValue( Action.SHORT_DESCRIPTION, "Saves all projects and exits SoapUI" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu Q" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			saveOnExit = true;
			WindowEvent windowEvent = new WindowEvent( frame, WindowEvent.WINDOW_CLOSING );
			frame.dispatchEvent( windowEvent );
		}
	}

	private class ExitButtonAction extends AbstractAction
	{
		public ExitButtonAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/system-log-out.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Saves all projects and exits SoapUI" );
		}

		public void actionPerformed( ActionEvent e )
		{
			saveOnExit = true;
			WindowEvent windowEvent = new WindowEvent( frame, WindowEvent.WINDOW_CLOSING );
			frame.dispatchEvent( windowEvent );
		}
	}

	private class ShowPushPageAction extends AbstractAction
	{
		public ShowPushPageAction()
		{
			super( "Starter Page" );
			putValue( Action.SHORT_DESCRIPTION, "Shows the starter page" );
		}

		public void actionPerformed( ActionEvent e )
		{
			showPushPage();
		}
	}

	private class ToolbarForumSearchAction extends AbstractAction
	{
		public ToolbarForumSearchAction()
		{
			putValue( Action.SHORT_DESCRIPTION, "Searches the soapUI Support Forum" );
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/find.png" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			doForumSearch( searchField.getText() );
		}
	}

	private class SearchForumAction extends AbstractAction
	{
		public SearchForumAction()
		{
			super( "Search Forum" );
			putValue( Action.SHORT_DESCRIPTION, "Searches the soapUI Support Forum" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String text = UISupport.prompt( "Search Forum", "Searches the online Forum, leave empty to open", "" );
			if( text == null )
				return;

			doForumSearch( text );
		}
	}

	public static void showPushPage()
	{
		if( urlDesktopPanel == null )
		{
			try
			{
				urlDesktopPanel = new URLDesktopPanel( "soapUI Starter Page", "Info on soapUI", null );
			}
			catch( Throwable t )
			{
				t.printStackTrace();
				return;
			}
		}

		DesktopPanel dp = UISupport.showDesktopPanel( urlDesktopPanel );
		desktop.maximize( dp );

		urlDesktopPanel.navigate( PUSH_PAGE_URL, PUSH_PAGE_ERROR_URL, true );
	}

	private static class ShowSystemPropertiesAction extends AbstractAction
	{
		public ShowSystemPropertiesAction()
		{
			super( "System Properties" );
			putValue( Action.SHORT_DESCRIPTION, "Shows the current systems properties" );
		}

		public void actionPerformed( ActionEvent e )
		{
			StringBuffer buffer = new StringBuffer();
			Properties properties = System.getProperties();

			List<String> keys = new ArrayList<String>();
			for( Object key : properties.keySet() )
				keys.add( key.toString() );

			Collections.sort( keys );

			String lastKey = null;

			for( String key : keys )
			{
				if( lastKey != null )
				{
					if( !key.startsWith( lastKey ) )
						buffer.append( "\r\n" );
				}

				int ix = key.indexOf( '.' );
				lastKey = ix == -1 ? key : key.substring( 0, ix );

				buffer.append( key ).append( '=' ).append( properties.get( key ) ).append( "\r\n" );
			}

			UISupport.showExtendedInfo( "System Properties", "Current system properties",
					"<html><body><pre><font size=-1>" + buffer.toString() + "</font></pre></body></html>", new Dimension(
							600, 400 ) );
		}
	}

	private static class AboutAction extends AbstractAction
	{
		public AboutAction()
		{
			super( "About soapUI" );
			putValue( Action.SHORT_DESCRIPTION, "Shows information on soapUI" );
		}

		public void actionPerformed( ActionEvent e )
		{
			URI splashURI = null;
			try
			{
				splashURI = UISupport.findSplash( SoapUI.SOAPUI_SPLASH ).toURI();
			}
			catch( URISyntaxException e1 )
			{
				SoapUI.logError( e1 );
			}

			Properties props = new Properties();
			try
			{
				props.load( SoapUI.class.getResourceAsStream( BUILDINFO_RESOURCE ) );
			}
			catch( Exception e1 )
			{
				SoapUI.logError( e1 );
			}

			UISupport.showExtendedInfo( "About soapUI", null,
					"<html><body><p align=center> <font face=\"Verdana,Arial,Helvetica\"><strong><img src=\"" + splashURI
							+ "\"><br>soapUI " + SOAPUI_VERSION + ", copyright (C) 2004-2009 eviware.com<br>"
							+ "<a href=\"http://www.soapui.org\">http://www.soapui.org</a> | "
							+ "<a href=\"http://www.eviware.com\">http://www.eviware.com</a><br>" + "Build "
							+ props.getProperty( "build.number" ) + ", Build Date " + props.getProperty( "build.date" )
							+ "</strong></font></p></body></html>",

					new Dimension( 470, 350 ) );
		}
	}

	private class ExitWithoutSavingAction extends AbstractAction
	{
		public ExitWithoutSavingAction()
		{
			super( "Exit without saving" );
			putValue( Action.SHORT_DESCRIPTION, "Saves all projects and exits SoapUI" );
			putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "ctrl alt Q" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			saveOnExit = false;
			WindowEvent windowEvent = new WindowEvent( frame, WindowEvent.WINDOW_CLOSING );
			frame.dispatchEvent( windowEvent );
		}
	}

	private class SavePreferencesAction extends AbstractAction
	{
		public SavePreferencesAction()
		{
			super( "Save Preferences" );
			putValue( Action.SHORT_DESCRIPTION, "Saves all global preferences" );
		}

		public void actionPerformed( ActionEvent e )
		{
			try
			{
				soapUICore.saveSettings();
			}
			catch( Exception e1 )
			{
				UISupport.showErrorMessage( e1 );
			}
		}
	}

	public static TestMonitor getTestMonitor()
	{
		if( testMonitor == null )
			testMonitor = new TestMonitor();

		return testMonitor;
	}

	public static void setTestMonitor( TestMonitor monitor )
	{
		testMonitor = monitor;
	}

	public static Log4JMonitor getLogMonitor()
	{
		return logMonitor;
	}

	public static void setLogMonitor( Log4JMonitor monitor )
	{
		logMonitor = monitor;
	}

	// instance is null in Eclipse. /Lars
	// eclipse-version(s) should provide SoapUIDesktop implementation
	public static SoapUIDesktop getDesktop()
	{
		return desktop;
	}

	public static void setDesktop( SoapUIDesktop desktop )
	{
		SoapUI.desktop = desktop;
	}

	public static Navigator getNavigator()
	{
		return navigator;
	}

	public static SoapUIActionRegistry getActionRegistry()
	{
		if( soapUICore == null )
			soapUICore = new DefaultSoapUICore();

		return soapUICore.getActionRegistry();
	}

	public static void setNavigator( Navigator navigator )
	{
		SoapUI.navigator = navigator;
	}

	public static void setStandalone( boolean standalone )
	{
		SoapUI.isStandalone = standalone;
	}

	private static class NewWsdlProjectActionDelegate extends AbstractAction
	{
		public NewWsdlProjectActionDelegate()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/project.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Creates a new WSDL Project" );
		}

		public void actionPerformed( ActionEvent e )
		{
			SoapUI.getActionRegistry().getAction( NewWsdlProjectAction.SOAPUI_ACTION_ID ).perform( workspace, null );
		}
	}

	private static class ImportWsdlProjectActionDelegate extends AbstractAction
	{
		public ImportWsdlProjectActionDelegate()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/import_project.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Imports an existing WSDL Project into the current workspace" );
		}

		public void actionPerformed( ActionEvent e )
		{
			SoapUI.getActionRegistry().getAction( ImportWsdlProjectAction.SOAPUI_ACTION_ID ).perform( workspace, null );
		}
	}

	private static class SaveAllActionDelegate extends AbstractAction
	{
		public SaveAllActionDelegate()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/disk_multiple.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Saves all projects in the current workspace" );
		}

		public void actionPerformed( ActionEvent e )
		{
			SoapUI.getActionRegistry().getAction( SaveAllProjectsAction.SOAPUI_ACTION_ID ).perform( workspace, null );
		}
	}

	private class PreferencesActionDelegate extends AbstractAction
	{
		public PreferencesActionDelegate()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/options.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Sets Global soapUI Preferences" );
		}

		public void actionPerformed( ActionEvent e )
		{
			SoapUIPreferencesAction.getInstance().actionPerformed( null );
		}
	}

	public static class ImportPreferencesAction extends AbstractAction
	{
		public static final String IMPORT_PREFERENCES_ACTION_NAME = "Import Preferences";

		public ImportPreferencesAction()
		{
			super( ImportPreferencesAction.IMPORT_PREFERENCES_ACTION_NAME );
			putValue( Action.SHORT_DESCRIPTION, "Imports soapUI Settings from another settings-file" );
		}

		public void actionPerformed( ActionEvent e )
		{
			try
			{
				// prompt for import
				File file = UISupport.getFileDialogs().open( null, ImportPreferencesAction.IMPORT_PREFERENCES_ACTION_NAME,
						".xml", "soapUI Settings XML (*.xml)", null );
				if( file != null )
					soapUICore.importSettings( file );
			}
			catch( Exception e1 )
			{
				UISupport.showErrorMessage( e1 );
			}
		}
	}

	public static SoapUIListenerRegistry getListenerRegistry()
	{
		if( soapUICore == null )
			soapUICore = DefaultSoapUICore.createDefault();

		return soapUICore.getListenerRegistry();
	}

	public static Settings getSettings()
	{
		if( soapUICore == null )
			soapUICore = DefaultSoapUICore.createDefault();

		return soapUICore.getSettings();
	}

	public static void importPreferences( File file ) throws Exception
	{
		if( soapUICore != null )
			soapUICore.importSettings( file );
	}

	public static MockEngine getMockEngine()
	{
		if( soapUICore == null )
			soapUICore = DefaultSoapUICore.createDefault();

		return soapUICore.getMockEngine();
	}

	public static String saveSettings() throws Exception
	{
		return soapUICore == null ? null : soapUICore.saveSettings();
	}

	public static void initDefaultCore()
	{
		if( soapUICore == null )
			soapUICore = DefaultSoapUICore.createDefault();
	}

	public class MaximizeDesktopAction extends AbstractAction
	{
		private JLogList lastLog;
		private int lastMainDividerLocation;
		private final InspectorLog4JMonitor log4JMonitor;
		private int lastLogDividerLocation;

		public MaximizeDesktopAction( InspectorLog4JMonitor log4JMonitor )
		{
			super( "Maximize Desktop" );
			this.log4JMonitor = log4JMonitor;

			putValue( SHORT_DESCRIPTION, "Hides/Shows the Navigator and Log tabs" );
			putValue( ACCELERATOR_KEY, UISupport.getKeyStroke( "menu M" ) );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( mainInspector.getCurrentInspector() != null || logMonitor.getCurrentLog() != null )
			{
				lastMainDividerLocation = mainInspector.getDividerLocation();
				mainInspector.deactivate();

				lastLog = logMonitor.getCurrentLog();
				lastLogDividerLocation = log4JMonitor.getDividerLocation();

				log4JMonitor.deactivate();
			}
			else
			{
				mainInspector.setCurrentInspector( "Navigator" );
				mainInspector.setDividerLocation( lastMainDividerLocation == 0 ? 250 : lastMainDividerLocation );

				log4JMonitor.setCurrentLog( lastLog );
				log4JMonitor.setDividerLocation( lastLogDividerLocation == 0 ? 500 : lastLogDividerLocation );
			}
		}
	}

	public static void initAutoSaveTimer()
	{
		Settings settings = SoapUI.getSettings();
		long interval = settings.getLong( UISettings.AUTO_SAVE_INTERVAL, 0 );

		if( autoSaveTimerTask != null )
		{
			if( interval == 0 )
				SoapUI.log( "Cancelling AutoSave Timer" );

			autoSaveTimerTask.cancel();
			autoSaveTimerTask = null;
		}

		if( interval > 0 )
		{
			autoSaveTimerTask = new AutoSaveTimerTask();

			SoapUI.log( "Scheduling autosave every " + interval + " minutes" );

			if( backgroundTimer == null )
				backgroundTimer = new Timer( "AutoSave Timer" );

			backgroundTimer.schedule( autoSaveTimerTask, interval * 1000 * 60, interval * 1000 * 60 );
		}
	}

	public static Timer getBackgroundTimer()
	{
		return backgroundTimer;
	}

	private static class AutoSaveTimerTask extends TimerTask
	{
		@Override
		public void run()
		{
			SoapUI.log( "Autosaving Workspace" );
			( ( WorkspaceImpl )SoapUI.getWorkspace() ).save( false, true );
		}
	}

	public static void initGCTimer()
	{
		Settings settings = SoapUI.getSettings();
		long interval = settings.getLong( UISettings.GC_INTERVAL, 0 );

		if( gcTimerTask != null )
		{
			if( interval == 0 )
				SoapUI.log( "Cancelling GC Timer" );

			gcTimerTask.cancel();
			gcTimerTask = null;
		}

		if( interval > 0 )
		{
			gcTimerTask = new GCTimerTask();

			SoapUI.log( "Scheduling garbage collection every " + interval + " seconds" );

			if( backgroundTimer == null )
				backgroundTimer = new Timer( "AutoSave Timer" );

			backgroundTimer.schedule( gcTimerTask, interval * 1000, interval * 1000 );
		}
	}

	private static class GCTimerTask extends TimerTask
	{
		@Override
		public void run()
		{
			System.gc();
		}
	}

	public static JXToolBar getToolBar()
	{
		return mainToolbar;
	}
}
