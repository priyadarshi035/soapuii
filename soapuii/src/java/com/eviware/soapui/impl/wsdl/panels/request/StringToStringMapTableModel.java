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

package com.eviware.soapui.impl.wsdl.panels.request;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.eviware.soapui.support.types.StringToStringMap;

/**
 * TableModel for StringToString Maps
 * 
 * @author ole.matzura
 */

public class StringToStringMapTableModel extends AbstractTableModel implements TableModel
{
	private StringToStringMap data;
	private final String keyCaption;
	private final String valueCaption;
	private List<String> keyList;
	private final boolean editable;

	public StringToStringMapTableModel( StringToStringMap data, String keyCaption, String valueCaption, boolean editable )
	{
		this.data = data;
		this.keyCaption = keyCaption;
		this.valueCaption = valueCaption;
		this.editable = editable;

		keyList = data == null ? new ArrayList<String>() : new ArrayList<String>( data.keySet() );
	}

	public int getColumnCount()
	{
		return 2;
	}

	public String getColumnName( int arg0 )
	{
		return arg0 == 0 ? keyCaption : valueCaption;
	}

	public boolean isCellEditable( int arg0, int arg1 )
	{
		return editable;
	}

	public Class<?> getColumnClass( int arg0 )
	{
		return String.class;
	}

	public void setValueAt( Object arg0, int arg1, int arg2 )
	{
		String oldKey = keyList.get( arg1 );

		if( arg2 == 0 )
		{
			String value = data.get( oldKey );

			data.remove( oldKey );
			data.put( arg0.toString(), value );

			keyList.set( arg1, arg0.toString() );
		}
		else
		{
			data.put( oldKey, arg0.toString() );
		}

		fireTableCellUpdated( arg1, arg2 );
	}

	public int getRowCount()
	{
		return data == null ? 0 : data.size();
	}

	public Object getValueAt( int arg0, int arg1 )
	{
		String str = keyList.get( arg0 );
		return arg1 == 0 ? str : data.get( str );
	}

	public void add( String key, String value )
	{
		if( keyList.contains( key ) )
		{
			data.put( key, value );
			fireTableCellUpdated( keyList.indexOf( key ), 1 );
		}
		else
		{
			data.put( key, value );
			keyList.add( key );
			fireTableRowsInserted( keyList.size() - 1, keyList.size() - 1 );
		}
	}

	public void remove( int row )
	{
		String key = keyList.get( row );
		keyList.remove( row );
		data.remove( key );

		fireTableRowsDeleted( row, row );
	}

	public StringToStringMap getData()
	{
		return new StringToStringMap( data );
	}

	public void setData( StringToStringMap data )
	{
		this.data = data == null ? new StringToStringMap() : data;

		keyList = new ArrayList<String>( this.data.keySet() );
		fireTableDataChanged();
	}
}
