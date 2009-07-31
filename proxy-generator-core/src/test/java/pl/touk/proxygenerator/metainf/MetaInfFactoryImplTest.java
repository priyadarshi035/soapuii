/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.metainf;

import org.custommonkey.xmlunit.*;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.File;
import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 *
 * @author pnw
 */
public class MetaInfFactoryImplTest extends XMLTestCase {
    public MetaInfFactoryImplTest(String testName) {
        super(testName);
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreAttributeOrder(true);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

	/**
	 * Test of createServiceAssemblyMetaInf method, of class MetaInfFactoryImpl.
	 */
	public void testCreateServiceAssemblyMetaInf() throws Exception {
		System.out.println("createServiceAssemblyMetaInf");
		String projectName = "przykladowy_proces";
		MetaInfFactoryImpl instance = new MetaInfFactoryImpl();

		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
		Document expResult = domBuilder.parse(new File("src/test/resources/metainf/sa_jbi.xml"));
		Document result = instance.createServiceAssemblyMetaInf(projectName);
		assertXMLEqual(expResult, result);
		
	}

	/**
	 * Test of createServiceUnitMetaInf method, of class MetaInfFactoryImpl.
	 */
	public void testCreateServiceUnitMetaInf() throws Exception {
		System.out.println("createServiceUnitMetaInf");
		String projectName = "przykladowy_proces";
		MetaInfFactoryImpl instance = new MetaInfFactoryImpl();

		DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
		Document expResult = domBuilder.parse(new File("src/test/resources/metainf/su_jbi.xml"));
		Document result = instance.createServiceUnitMetaInf(projectName);
		assertXMLEqual(expResult, result);
	}

}
