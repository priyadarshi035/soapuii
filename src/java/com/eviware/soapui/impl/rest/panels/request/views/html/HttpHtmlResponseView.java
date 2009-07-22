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

package com.eviware.soapui.impl.rest.panels.request.views.html;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseDocument;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseMessageEditor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.BrowserComponent;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;

@SuppressWarnings( "unchecked" )
public class HttpHtmlResponseView extends AbstractXmlEditorView<HttpResponseDocument> implements PropertyChangeListener
{
	private final HttpRequestInterface<?> restRequest;
	private JPanel contentPanel;
	private JPanel panel;
	private JLabel statusLabel;
	private BrowserComponent browser;

	public HttpHtmlResponseView( HttpResponseMessageEditor httpRequestMessageEditor, HttpRequestInterface<?> httpRequest )
	{
		super( "HTML", httpRequestMessageEditor, HttpHtmlResponseViewFactory.VIEW_ID );
		this.restRequest = httpRequest;

		httpRequest.addPropertyChangeListener( this );
	}

	public JComponent getComponent()
	{
		if( panel == null )
		{
			panel = new JPanel( new BorderLayout() );

			panel.add( buildToolbar(), BorderLayout.NORTH );
			panel.add( buildContent(), BorderLayout.CENTER );
			panel.add( buildStatus(), BorderLayout.SOUTH );
		}

		return panel;
	}

	@Override
	public void release()
	{
		super.release();

		if( browser != null )
			browser.release();
		
		restRequest.removePropertyChangeListener( this );
	}

	private Component buildStatus()
	{
		statusLabel = new JLabel();
		statusLabel.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		return statusLabel;
	}

	private Component buildContent()
	{
		contentPanel = new JPanel( new BorderLayout() );

		if( BrowserComponent.isJXBrowserDisabled() )
		{
			contentPanel.add( new JLabel( "Browser Component is disabled" ));
		}
		else
		{
			browser = new BrowserComponent( false );

			HttpResponse response = restRequest.getResponse();
			if( response != null )
				setEditorContent( response );

			Component component = browser.getComponent();
			component.setMinimumSize( new Dimension( 100, 100 ) );
			contentPanel.add( component );
		}
		return contentPanel;
	}

	protected void setEditorContent( HttpResponse httpResponse )
	{

		if( httpResponse != null && httpResponse.getContentType() != null )
		{
			String contentType = httpResponse.getContentType();
			if( contentType.contains( "html" ) || contentType.contains( "xml" ) || contentType.contains( "text" ) )
			{
				try
				{
					browser.setContent( httpResponse.getContentAsString(), contentType, httpResponse.getURL().toURI()
							.toString() );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
			else
			{
				try
				{
					String ext = ContentTypeHandler.getExtensionForContentType( contentType );
					File temp = File.createTempFile( "response", "." + ext );
					FileOutputStream fileOutputStream = new FileOutputStream( temp );
					writeHttpBody( httpResponse.getRawResponseData(), fileOutputStream );
					fileOutputStream.close();
					browser.navigate( temp.getAbsolutePath(), null );
					temp.deleteOnExit();
				}
				catch( FileNotFoundException e )
				{
					e.printStackTrace();
				}
				catch( IOException e )
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			browser.setContent( "<missing content>", "text/plain" );
		}
	}

	private void writeHttpBody( byte[] rawResponse, FileOutputStream out ) throws IOException
	{
		int index = 0;
		byte[] divider = "\r\n\r\n".getBytes();
		for( ; index < ( rawResponse.length - divider.length ); index++ )
		{
			int i;
			for( i = 0; i < divider.length; i++ )
			{
				if( rawResponse[index + i] != divider[i] )
					break;
			}

			if( i == divider.length )
			{
				out.write( rawResponse, index + divider.length, rawResponse.length - ( index + divider.length ) );
			}
		}

		out.write( rawResponse );
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		return toolbar;
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( AbstractHttpRequestInterface.RESPONSE_PROPERTY ) )
		{
			if( browser != null )
				setEditorContent( ( ( HttpResponse )evt.getNewValue() ) );
		}
	}

	@Override
	public void setXml( String xml )
	{
	}

	public boolean saveDocument( boolean validate )
	{
		return false;
	}

	public void setEditable( boolean enabled )
	{
	}

}