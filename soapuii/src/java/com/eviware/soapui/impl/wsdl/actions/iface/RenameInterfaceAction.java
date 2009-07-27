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

package com.eviware.soapui.impl.wsdl.actions.iface;


import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action for renaming an existing WSDL project
 * 
 * @author Piotr Nowojski
 */

public class RenameInterfaceAction extends AbstractSoapUIAction<WsdlInterface>
{
	public static final String SOAPUI_ACTION_ID = "RenameWsdlProjectAction";

	public static final MessageSupport messages = MessageSupport.getMessages( RenameInterfaceAction.class );

	public RenameInterfaceAction()
	{
		super( "Rename" ,  "Renames this WSDL" );
	}

	public void perform( WsdlInterface iface, Object param )
	{
		String name = UISupport.prompt( "Specify name of WSDL", "Rename WSDL", iface.getName());
		if( name == null || name.equals( iface.getName() ) )
			return;

		iface.setName( name );
	}
}