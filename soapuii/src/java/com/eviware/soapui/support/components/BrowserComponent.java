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

package com.eviware.soapui.support.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.mozilla.interfaces.nsIHttpChannel;
import org.mozilla.interfaces.nsIRequest;
import org.mozilla.interfaces.nsISupports;
import org.mozilla.interfaces.nsIURI;
import org.mozilla.interfaces.nsIWeakReference;
import org.mozilla.interfaces.nsIWebBrowser;
import org.mozilla.interfaces.nsIWebProgress;
import org.mozilla.interfaces.nsIWebProgressListener;
import org.mozilla.xpcom.Mozilla;
import org.mozilla.xpcom.XPCOMException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;
import com.teamdev.jxbrowser.WebBrowser;
import com.teamdev.jxbrowser.WebBrowserFactory;
import com.teamdev.jxbrowser.WebBrowserWindow;
import com.teamdev.jxbrowser.WindowCreator;
import com.teamdev.jxbrowser.event.LocationEvent;
import com.teamdev.jxbrowser.event.RequestAdapter;
import com.teamdev.jxbrowser.event.StatusChangeEvent;
import com.teamdev.jxbrowser.event.StatusChangeListener;
import com.teamdev.jxbrowser.mozilla.MozillaWebBrowser;
import com.teamdev.xpcom.Xpcom;

public class BrowserComponent implements nsIWebProgressListener, nsIWeakReference, StatusChangeListener
{
	private WebBrowser browser;
	private static WebBrowserFactory webBrowserFactory;
	private JPanel panel = new JPanel( new BorderLayout() );
	private JPanel statusBar;
	private JLabel statusLabel;
	private String errorPage;
	private WebBrowserWindow browserWindowAdapter = new BrowserWindowAdapter();
	private final boolean addToolbar;
	private boolean showingErrorPage;
	public String url;
	private static Boolean initialized = false;

	public BrowserComponent( boolean addToolbar )
	{
		this.addToolbar = addToolbar;

		if( !initialized )
		{
			synchronized( initialized )
			{
				if( !initialized )
				{
					try
					{
						if( !isJXBrowserDisabled() )
						{
							if( Xpcom.isMacOSX() )
							{
								final String currentCP = System.getProperty( "java.class.path" );
								final String appleJavaExtentions = ":/System/Library/Java";
								System.setProperty( "java.class.path", currentCP + appleJavaExtentions );
							}

							Xpcom.initialize();
						}

						initialized = true;
					}
					catch( Throwable t )
					{
						t.printStackTrace();
					}
				}
			}
		}
	}

	public static boolean isJXBrowserDisabled()
	{
		String disable = System.getProperty( "soapui.jxbrowser.disable", "nope" );
		if( disable.equals( "true" ) )
			return true;

		if( !disable.equals( "false" ) && "64".equals( System.getProperty( "sun.arch.data.model" ) ) )
			return true;

		return false;
	}

	public Component getComponent()
	{
		if( isJXBrowserDisabled() )
		{
			JEditorPane jxbrowserDisabledPanel = new JEditorPane();
			jxbrowserDisabledPanel.setText( "browser component disabled" );
			panel.add( jxbrowserDisabledPanel );
		}
		else
		{
			if( browser == null )
			{
				initBrowser();
				navigate( "about:blank", null );

				statusBar = new JPanel();
				statusLabel = new JLabel();
				statusBar.add( statusLabel, BorderLayout.CENTER );

				if( addToolbar )
					panel.add( buildToolbar(), BorderLayout.NORTH );

				panel.add( browser.getComponent(), BorderLayout.CENTER );
				panel.add( statusBar, BorderLayout.SOUTH );
				
				panel.repaint();
			}
		}
		return panel;
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.addFixed( UISupport.createToolbarButton( new BackAction() ) );
		toolbar.addRelatedGap();
		toolbar.addFixed( UISupport.createToolbarButton( new ForwardAction() ) );

		toolbar.addGlue();

		return toolbar;
	}

	private class BackAction extends AbstractAction
	{
		public BackAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/arrow_left.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Navigate to previous selection" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( browser.getHistory().getCurrentPosition() == 0 )
				Toolkit.getDefaultToolkit().beep();
			else
				browser.goBack();
		}
	}

	private class ForwardAction extends AbstractAction
	{
		public ForwardAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/arrow_right.png" ) );
			putValue( Action.SHORT_DESCRIPTION, "Navigate to next selection" );
		}

		public void actionPerformed( ActionEvent e )
		{
			// WebBrowserHistory history = browser.getHistory();
			// List entries = history.getEntries();
			// if( history.getCurrentPosition() == entries.size()-1 )
			// Toolkit.getDefaultToolkit().beep();
			// else
			browser.goForward();
		}
	}

	public void initBrowser()
	{
		if( webBrowserFactory == null )
			webBrowserFactory = WebBrowserFactory.getInstance();

		browser = webBrowserFactory.createBrowser();
		nsIWebBrowser nsWebBrowser = ( ( MozillaWebBrowser )browser ).getWebBrowser();
		nsWebBrowser.addWebBrowserListener( this, nsIWebProgressListener.NS_IWEBPROGRESSLISTENER_IID );
		browser.addStatusChangeListener( this );

		browser.setWindowCreator( new WindowCreator()
		{
			public WebBrowserWindow createChildWindow( Component parentComponent, long flags )
			{
				return browserWindowAdapter;
			}
		} );

		browser.deactivate();
	}

	public void release()
	{
		if( browser != null )
		{
			browser.stop();
			browser.deactivate();
			
			nsIWebBrowser nsWebBrowser = ( ( MozillaWebBrowser )browser ).getWebBrowser();
			nsWebBrowser.removeWebBrowserListener( this, nsIWebProgressListener.NS_IWEBPROGRESSLISTENER_IID );
			browser.removeStatusChangeListener( this );
			browser.dispose();
		}
		
		browser = null;
	}

	public void setContent( String contentAsString, String contentType, String contextUri )
	{
		if( browser == null )
		{
			initBrowser();
		}
		browser.setContentWithContext( contentAsString, contentType, contextUri );
	}

	public void setContent( String content, String contentType )
	{
		// setContent( content, contentType, null );

		if( browser == null )
		{
			initBrowser();
		}

		try
		{
			browser.setContent( content, contentType );
		}
		catch( Throwable t )
		{
			SoapUI.logError( t );
		}
	}

	public boolean navigate( String url, String errorPage )
	{
		if( browser == null )
		{
			initBrowser();
		}

		if( errorPage != null )
			setErrorPage( errorPage );

		this.url = url;
		SwingUtilities.invokeLater( new Navigator() );

		return true;
	}

	public String getContent()
	{
		return browser == null ? null : XmlUtils.serialize( browser.getDocument() );
	}

	private final class Navigator implements Runnable
	{
		public void run()
		{
			try
			{
				browser.navigate( getUrl() );
			}
			catch( Throwable e )
			{
				SoapUI.log( e.toString() );
			}
		}
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl( String url )
	{
		navigate( url, null );
	}

	public nsISupports queryInterface( String uuid )
	{
		return Mozilla.queryInterface( this, uuid );
	}

	public nsISupports queryReferent( String uuid )
	{
		return Mozilla.queryInterface( this, uuid );
	}

	public void onLocationChange( nsIWebProgress arg0, nsIRequest arg1, nsIURI arg2 )
	{
	}

	public void onProgressChange( nsIWebProgress arg0, nsIRequest arg1, int arg2, int arg3, int arg4, int arg5 )
	{
	}

	public void onSecurityChange( nsIWebProgress arg0, nsIRequest arg1, long arg2 )
	{
	}

	public void onStateChange( nsIWebProgress arg0, nsIRequest request, long arg2, long arg3 )
	{
		try
		{
			if( !getUrl().equals( "about:blank" ) )
			{
				nsIHttpChannel ch = null;
				try {
					ch = ( nsIHttpChannel )request.queryInterface( nsIHttpChannel.NS_IHTTPCHANNEL_IID );
				}
					catch( XPCOMException e )
					{
						if( (!browser.getLocationURL().equals( errorPage )) && ( e.errorcode != 2147500034l ) )
						{
							SoapUI.log.warn( "Error [" + e.getMessage() + "] for " + e.errorcode );
							browser.navigate( errorPage );
						}
					}
				if( ch != null && ch.getResponseStatus() >= 400 )
				{
					//SoapUI.log.warn( "Error [" + ch.getResponseStatus() + "] for " + ch.getOriginalURI().toString() );
					if( !showingErrorPage )
						showErrorPage();
				}
			}
		}
		catch( XPCOMException e )
		{
			//if( (!browser.getLocationURL().equals( errorPage )) && ( e.errorcode != 2147500034l ) )
			//{
				//SoapUI.log.warn( "Error [" + e.getMessage() + "] for " + e.errorcode );
				//browser.navigate( errorPage );
			//}
		}
	}

	private void showErrorPage()
	{
		showingErrorPage = true;
		browser.navigate( errorPage );
		showingErrorPage = false;
	}

	public String getErrorPage()
	{
		return errorPage;
	}

	public void setErrorPage( String errorPage )
	{
		this.errorPage = errorPage;
	}

	public void onStatusChange( nsIWebProgress arg0, nsIRequest arg1, long arg2, String arg3 )
	{
	}

	private class BrowserWindowAdapter implements WebBrowserWindow
	{
		private boolean resizable;

		public void close()
		{
		}

		public boolean isClosed()
		{
			return true;
		}

		public void setModal( boolean arg0 )
		{
		}

		public void setSize( int arg0, int arg1 )
		{
		}

		public void setVisible( boolean arg0 )
		{
		}

		public void setWebBrowser( WebBrowser arg0 )
		{
			if( arg0 != null )
			{
				arg0.addRequestListener( new RequestAdapter()
				{

					@Override
					public void locationChanged( final LocationEvent arg0 )
					{
						if( !arg0.getLocation().equals( "about:blank" ) )
						{
							if( UISupport.confirm( "Open url [" + arg0.getLocation() + "] in external browser?", "Open URL" ) )
								Tools.openURL( arg0.getLocation() );

							arg0.getWebBrowser().stop();
							arg0.getWebBrowser().deactivate();
							arg0.getWebBrowser().dispose();
							arg0.getWebBrowser().removeRequestListener( this );
						}
						// Toolkit.getDefaultToolkit().beep();
					}
				} );

				// arg0.stop();
				// arg0.deactivate();
				// arg0.dispose();
				//
				// Toolkit.getDefaultToolkit().beep();
			}

		}

		public boolean isResizable()
		{
			return resizable;
		}

		public void setResizable( boolean resizable )
		{
			this.resizable = resizable;
		}
	}

	public void statusChanged( final StatusChangeEvent event )
	{
		if( statusLabel != null )
		{
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					statusLabel.setText( event.getStatus() );
				}
			} );
		}
	}
	
	public boolean isBrowserInitialised() {
		return browser != null;
	}
}
