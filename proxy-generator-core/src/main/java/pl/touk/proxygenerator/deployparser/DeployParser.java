/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.deployparser;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author azl
 */
public class DeployParser implements DeployParserInterface
{
	private final static Pattern namespacePrefixPattern = Pattern.compile("(\\w+):(\\w+)");
	private final static String deployFileName = "deploy.xml";
	private final static Logger log = Logger.getLogger(DeployParser.class.getName());
	private DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder domBuilder;
	private Document dom;

	private XPathFactory factory = XPathFactory.newInstance();

	public DeployParser() throws ParserConfigurationException
	{
		domBuilderFactory.setNamespaceAware(true);
		domBuilderFactory.setIgnoringComments(true);
		domBuilder = domBuilderFactory.newDocumentBuilder();
	};

	public void ParseXML(File newFile) throws SAXException
	{
		try {
			dom = domBuilder.parse(newFile);
//		Document doc = docBuilder.parse (new File("test.xml"));
		} catch (IOException ex) {
			Logger.getLogger(DeployParser.class.getName()).log(Level.SEVERE, null, ex);
		}
//		Document doc = docBuilder.parse (new File("test.xml"));
	}


}
