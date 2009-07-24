/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.proxygenerator.deployparser;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

/**
 *
 * @author azl
 */
public class DeployParser implements DeployParserInterface
{
	private final static Pattern namespacePrefixPattern = Pattern.compile("(\\w+):(\\w+)");
	private final static String deployFileName = "deploy.xml";
	private final static Logger log = Logger.getLogger(DeployParser.class.getName());
	private DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder builder;
	private XPathFactory factory = XPathFactory.newInstance();

	public DeployParser() throws ParserConfigurationException
	{
		domFactory.setNamespaceAware(true);
		domFactory.setIgnoringComments(true);
		builder = domFactory.newDocumentBuilder();
	};



}
