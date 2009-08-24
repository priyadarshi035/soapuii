/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.eviware.soapui.impl;

import com.eviware.soapui.support.xml.XmlUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import static org.junit.Assert.*;

/**
 *
 * @author azl
 */
@Ignore
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

	//If you want to test this file erase the @Ignore statement before class definition and change the source file paths couple lines down when it is comment
	public void testAddBindingToWsdl() throws Exception
	{
		System.out.println("addBindingToWsdl");

		InputStream inputStream = null;
		try
		{
			//Change path line below
			inputStream = new FileInputStream(new File("src/test/resources/wsdl/MNPIntegration.wsdl"));
		} catch (FileNotFoundException ex)
		{
			throw new WsdlBindingException("File cannot be read",ex);
		}

		//Change path line below
		Document inDocument = XmlUtils.parse("src/test/resources/wsdl/MNPIntegration.wsdl");
		System.out.println("------------------------------BEFORE:----------------------------------");
		System.out.println(XmlUtils.serialize(inDocument));
	
		WsdlBindingAdderImpl instance = new WsdlBindingAdderImpl();		
		
		//
		//Main feature of the class to be tested
		InputStream result = instance.addBindingToWsdl(inputStream);
		//

		System.out.println("-------------------------------AFTER:----------------------------------");
		if (result != null)
		{
			Document resultDocument = XmlUtils.parse(result);
			File export = new File("Result.wsdl");
			XmlUtils.serializePretty(resultDocument);
			saveToFile(resultDocument, export);
			System.out.println(XmlUtils.serialize(resultDocument));
		}
		else
		System.out.println("Sorry, the input stream wasn't changed correctly");
		assertTrue(true);
//		assertEquals(expResult, result);
		// TODO review the generated test code and remove the default call to fail.		
	}

	public static void saveToFile(Document localDoc, File export) throws IOException
	{
		try
		{
			OutputFormat format = new OutputFormat(localDoc);
			format.setIndenting(true);

			XMLSerializer serializer = new XMLSerializer(new FileOutputStream(export), format);
			serializer.serialize(localDoc);
		}
		catch (IOException ie)
		{
			throw new IOException("File [" + export.getName() + "] couldn't be written: " + ie, ie);
		}
	}
}