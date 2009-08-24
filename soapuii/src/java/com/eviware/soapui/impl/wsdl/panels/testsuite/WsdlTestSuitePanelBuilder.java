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

package com.eviware.soapui.impl.wsdl.panels.testsuite;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for WsdlTestSuite
 * 
 * @author Ole.Matzura
 */

public class WsdlTestSuitePanelBuilder<T extends WsdlTestSuite> extends EmptyPanelBuilder<T>
{
	public WsdlTestSuitePanelBuilder()
	{
	}

	public DesktopPanel buildDesktopPanel( T testSuite )
	{
		return new WsdlTestSuiteDesktopPanel( testSuite );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( T modelItem )
	{
		JPropertiesTable<WsdlTestSuite> table = new JPropertiesTable<WsdlTestSuite>( "TestSuite Properties", modelItem );

		table.addProperty( "Name", "name", true );
		table.addProperty( "Run TestSuite Startup Script On Startup Of TestCase", "runSuiteStartupInTestCase", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Run TestSuite Startup Script On Startup Of TestStep", "runSuiteStartupInTestStep", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Run TestCase Startup Script On Startup Of TestStep", "runCaseStartupInTestStep", JPropertiesTable.BOOLEAN_OPTIONS );

		table.setPropertyObject( modelItem );

		return table;
	}
}
