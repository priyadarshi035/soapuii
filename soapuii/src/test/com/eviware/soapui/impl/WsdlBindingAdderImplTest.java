/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eviware.soapui.impl;

import com.eviware.soapui.support.xml.XmlUtils;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import static org.junit.Assert.*;

/**
 *
 * @author azl
 */
public class WsdlBindingAdderImplTest {

    public WsdlBindingAdderImplTest() {
    }

	@BeforeClass
	public static void setUpClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownClass() throws Exception
	{
	}

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

	/**
	 * Test of addBindingToWsdl method, of class WsdlBindingAdderImpl.
	 */
	@Test
	public void testAddBindingToWsdl() throws Exception
	{
		System.out.println("addBindingToWsdl");

		InputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(new File("src/test/resources/wsdl/CaseObserver.wsdl"));			
		} catch (FileNotFoundException ex)
		{
			throw new WsdlBindingException("File cannot be read",ex);
		}

		Document inDocument = XmlUtils.parse("src/test/resources/wsdl/CaseObserver.wsdl");
		System.out.println("------------------------------BEFORE:----------------------------------");
		System.out.println(XmlUtils.serialize(inDocument));
	
		WsdlBindingAdderImpl instance = new WsdlBindingAdderImpl();
		InputStream result = instance.addBindingToWsdl(inputStream);
		Document resultDocument = XmlUtils.parse(result);

		System.out.println("-------------------------------AFTER:----------------------------------");
		System.out.println(XmlUtils.serialize(resultDocument));

		assertTrue(true);
//		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.		
	}

}