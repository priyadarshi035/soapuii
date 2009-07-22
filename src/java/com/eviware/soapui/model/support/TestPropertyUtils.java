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

package com.eviware.soapui.model.support;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.testsuite.TestProperty;

public class TestPropertyUtils
{
	public static int saveTo( TestPropertyHolder propertyHolder, String fileName ) throws IOException
	{
		PrintWriter writer = new PrintWriter( new FileWriter( fileName ) );

		for( TestProperty prop : propertyHolder.getPropertyList() )
		{
			writer.print( prop.getName() );
			writer.print( '=' );
			String value = prop.getValue();
			if( value == null )
				value = "";

			String[] lines = value.split( "\n" );
			for( int c = 0; c < lines.length; c++ )
			{
				if( c > 0 )
					writer.println( "\\" );
				writer.print( lines[c] );
			}

			writer.println();
		}

		writer.close();
		return propertyHolder.getPropertyCount();
	}

	public synchronized static void sortProperties( MutableTestPropertyHolder holder )
	{
	}
}