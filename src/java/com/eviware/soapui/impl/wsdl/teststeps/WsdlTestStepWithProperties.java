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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.support.TestStepBeanProperty;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

import java.util.*;

/**
 * Base class for WSDL TestCase test steps.
 * 
 * @author Ole.Matzura
 */

abstract public class WsdlTestStepWithProperties extends WsdlTestStep
{
	private Map<String, TestProperty> properties;
	private List<TestProperty> propertyList = new ArrayList<TestProperty>();
	private Map<String, Set<String>> normalizedPropertyNames = new HashMap<String, Set<String>>();
	private Set<TestPropertyListener> listeners = new HashSet<TestPropertyListener>();

	protected WsdlTestStepWithProperties( WsdlTestCase testCase, TestStepConfig config, boolean hasEditor,
			boolean forLoadTest )
	{
		super( testCase, config, hasEditor, forLoadTest );
	}

	public String[] getPropertyNames()
	{
		if( properties == null )
			return new String[0];

		String[] result = new String[properties.size()];
		int ix = 0;
		for( TestProperty property : properties.values() )
			result[ix++ ] = property.getName();

		return result;
	}

	public TestProperty getProperty( String name )
	{
		return properties == null || name == null ? null : properties.get( getPropertyKeyName( name ) );
	}

	public String getPropertyValue( String name )
	{
		if( properties == null )
			return null;

		TestProperty testStepProperty = properties.get( getPropertyKeyName( name ) );
		return testStepProperty == null ? null : testStepProperty.getValue();
	}

	public void setPropertyValue( String name, String value )
	{
		if( properties == null )
			return;

		TestProperty testStepProperty = properties.get( getPropertyKeyName( name ) );
		if( testStepProperty != null )
		{
			testStepProperty.setValue( value );
		}
	}

	protected void addProperty( TestProperty property )
	{
		addProperty( property, false );
	}

	protected void addProperty( TestProperty property, boolean notify )
	{
		if( properties == null )
			properties = new HashMap<String, TestProperty>();
		
		String name = property.getName();
		String upper = name.toUpperCase();
		
		if( !normalizedPropertyNames.containsKey( upper ) )
			normalizedPropertyNames.put( upper, new HashSet<String>() );
		
		normalizedPropertyNames.get( upper ).add( name );

		properties.put( name, property );
		propertyList.add( property );

		if( notify )
		{
			firePropertyAdded( name );
		}
	}
	
	private String getPropertyKeyName(String name)
	{
		if(properties.containsKey( name ))
			return name;
		
		Set<String> props = normalizedPropertyNames.get( name.toUpperCase() );
		if(props != null && !props.isEmpty())
		{
			return props.iterator().next();
		}
		return name;
	}

	protected TestProperty deleteProperty( String name, boolean notify )
	{
		if( properties != null )
		{
			name = getPropertyKeyName(name);
			TestProperty result = properties.remove( name );
			
			if( result != null )
			{
				normalizedPropertyNames.get( name.toUpperCase() ).remove( name );
				propertyList.remove( result );

				if( notify )
					firePropertyRemoved( name );

				return result;
			}
		}

		return null;
	}

	public void propertyRenamed( String oldName )
	{
		if( properties == null )
			return;
		
		oldName = getPropertyKeyName( oldName );
		String upper = oldName.toUpperCase();

		TestProperty testStepProperty = properties.get( oldName );
		if( testStepProperty == null )
			return;

		Set<String> props = normalizedPropertyNames.get( upper );
		properties.remove( oldName );		
		props.remove( oldName );
		String newName = testStepProperty.getName();
		properties.put( newName, testStepProperty );
		
		upper = newName.toUpperCase();
		if( !normalizedPropertyNames.containsKey( upper ) )
			normalizedPropertyNames.put( upper, new HashSet<String>() );
		normalizedPropertyNames.get( upper ).add( newName );

		firePropertyRenamed( oldName, newName );
	}

	public void addTestPropertyListener( TestPropertyListener listener )
	{
		listeners.add( listener );
	}

	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		listeners.remove( listener );
	}

	protected void firePropertyAdded( String name )
	{
		TestPropertyListener[] array = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : array )
		{
			listener.propertyAdded( name );
		}
	}

	protected void firePropertyRemoved( String name )
	{
		TestPropertyListener[] array = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : array )
		{
			listener.propertyRemoved( name );
		}
	}

	protected void firePropertyRenamed( String oldName, String newName )
	{
		TestPropertyListener[] array = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : array )
		{
			listener.propertyRenamed( oldName, newName );
		}
	}

	public void firePropertyValueChanged( String name, String oldValue, String newValue )
	{
		if( oldValue == null && newValue == null )
			return;

		if( oldValue != null && oldValue.equals( newValue ) )
			return;

		if( newValue != null && newValue.equals( oldValue ) )
			return;

		TestPropertyListener[] array = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : array )
		{
			listener.propertyValueChanged( name, oldValue, newValue );
		}
	}

	public Map<String, TestProperty> getProperties()
	{
		Map<String, TestProperty> result = new HashMap<String, TestProperty>();

		if( properties != null )
		{
			for( String name : properties.keySet() )
				result.put( properties.get( name ).getName(), properties.get( name ) );
		}

		return result;
	}

	public boolean hasProperty( String name )
	{
		return properties != null && properties.containsKey( getPropertyKeyName( name ) );
	}

	public boolean hasProperties()
	{
		return true;
	}

	public TestProperty getPropertyAt( int index )
	{
		return propertyList.get( index );
	}

	public int getPropertyCount()
	{
		return propertyList.size();
	}

	public List<TestProperty> getPropertyList()
	{
		return Collections.unmodifiableList( propertyList );
	}

	protected void firePropertyMoved( String name, int oldIndex, int newIndex )
	{
		TestPropertyListener[] listenersArray = listeners.toArray( new TestPropertyListener[listeners.size()] );
		for( TestPropertyListener listener : listenersArray )
		{
			listener.propertyMoved( name, oldIndex, newIndex );
		}
	}

	public void moveProperty( String propertyName, int targetIndex )
	{
		TestProperty property = getProperty( propertyName );
		int ix = propertyList.indexOf( property );

		if( ix == targetIndex )
			return;

		if( targetIndex < 0 )
			targetIndex = 0;

		if( targetIndex < properties.size() )
			propertyList.add( targetIndex, propertyList.remove( ix ) );
		else
			propertyList.add( propertyList.remove( ix ) );

		if( targetIndex > properties.size() )
			targetIndex = properties.size();

		firePropertyMoved( propertyName, ix, targetIndex );

	}

}
