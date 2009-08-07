/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import org.custommonkey.xmlunit.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
/**
 *
 * @author pnw
 */
public class ProjectTestCase extends XMLTestCase
{
	protected boolean quiet = true;
	protected WsdlProject project;

	protected String xpathExist = "/soapui-project/settings";
	protected String expXpathResult = "minimal";
	protected String xpath = "/soapui-project/@name";
	protected Document expProjectResult = null, result = null;

	public ProjectTestCase(String testName) throws Exception
	{
		super(testName);

		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreAttributeOrder(true);
		
		String str = SoapUI.class.getResource( "/minimal-soapui-project.xml" ).toURI().toString();

		project = new WsdlProject( str );
	}

	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		if (!quiet)
			System.out.println("setUp");
	}

	@After
	public void tearDown() throws Exception
	{
		if (!quiet)
			System.out.println("teardDown");
		super.tearDown();
	}

	public void testProject() throws Exception
	{
		if (!quiet)
			System.out.println("testProject");

		File projectFile = File.createTempFile("saopui-project", "xml");
		projectFile.deleteOnExit();

		assertTrue(project.saveIn(projectFile));
		if (!quiet)
		{
			System.out.println("Saved project: ");
			IOUtils.copy(new FileInputStream(projectFile), System.out);
			System.out.println();
		}
		
		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
		domBuilderFactory.setNamespaceAware(false);
		
		DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
		result = domBuilder.parse(projectFile);


		if (xpathExist != null)
		{
			if (!quiet)
				System.out.println("Testing xpath exist");
			assertXpathExists(xpathExist, result);
		}
		if (expXpathResult != null)
		{
			if (!quiet)
				System.out.println("Testing expected xpath result");
			assertXpathEvaluatesTo(expXpathResult, xpath, result);
		}
		if (expProjectResult != null)
		{
			if (!quiet)
				System.out.println("Testing xml equal");
			assertXMLEqual(expProjectResult, result);
		}
	}
}
