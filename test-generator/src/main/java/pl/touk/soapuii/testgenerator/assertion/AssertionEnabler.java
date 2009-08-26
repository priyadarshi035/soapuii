/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.assertion;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.iface.tools.support.SwingToolHost;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SwingConfigurationDialogImpl;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.impl.swing.JTextFieldFormField;
import com.eviware.x.impl.swing.SwingFormFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.apache.xmlbeans.XmlException;
import pl.touk.soapuii.testgenerator.data.GCXpathAssertion;

/**
 *
 * @author azl
 */
public class AssertionEnabler
{
	protected static WsdlProject project;
	private SwingConfigurationDialogImpl dialog;

	protected StringToStringMap values = new StringToStringMap();
	private JTree tree;

	private static AssertionEnabler instance;

	public AssertionEnabler()
	{
		instance = this;
	}

	public static AssertionEnabler getInstance()
	{
		if( instance == null )
			instance = new AssertionEnabler();

		return instance;
	}
	protected void attachPathToProject(String projectPath) throws XmlException, IOException, SoapUIException
	{
		project = new WsdlProject( projectPath );
	}

	protected void buildDialog()
	{
//		StringToStringMap values = new StringToStringMap();

		for(int i = 0; i < 10; i++)
		{
			values.put(("string"+i), ("string"+i));
		}


		dialog = new SwingConfigurationDialogImpl( "Enable Assertions", HelpUrls.PREFERENCES_HELP_URL,
				"Set set which assertion can be enabled with xpath", UISupport.OPTIONS_ICON );

		dialog.setContent( UISupport.createHorizontalSplit(UISupport.setFixedSize(createTree(), 200, 800), UISupport.setFixedSize(createExchangePane(), 1000, 800)));


		dialog.show(values);
	}


	private JComboBox createComboBox()
	{
		JComboBox box = UISupport.createComboBox(200, "disable");
		box.addItem("disable");
		box.addItem("enable");
		box.addItem("suite");
		box.addItem("case");
		return box;
	}

	private JLabel createLabel(String label)
	{
		JLabel jlabel = new JLabel(label,JLabel.LEFT);
		jlabel.setSize(300, 20);
		return jlabel;
	}

	private JTextField createTextField()
	{
		JTextField jtext = new JTextField(null, 300);

		return jtext;
	}

	private JPanel createExchangePane()
	{
				
		JPanel exchangePane = new JPanel();


		exchangePane.add(createLabel("hej"));
		exchangePane.add(createComboBox());
		exchangePane.add(createTextField());


//		exchangePane = (JPanel) builder.buildDialog( builder.buildOkCancelActions(),
//				"Select matching bindings to services", UISupport.OPTIONS_ICON );
//
//		for(int i = 0; i < 10; i++)
//		{
//			String temp = values.get("string"+i);
//			Object[] ob = {"enable", "disable", "suite", "case"};
//			form.setOptions(temp, ob);
//		}
//		
		return exchangePane;
	}

	private JButton createNextButton()
	{
		JButton nextButton = new JButton("Next");
		return nextButton;
	}

	private JScrollPane createTree()
	{
		TreeSelectionListener treeSelectionListener = new MyTreeSelectionListener();
		DefaultMutableTreeNode top =  new DefaultMutableTreeNode("TestGenerator");
		createNodes(top);
		tree = new JTree(top);

		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tree.addTreeSelectionListener(treeSelectionListener);

		tree.setSize(new Dimension(300, 400));

		JScrollPane treeView = new JScrollPane(tree);


		return treeView;
	}

	private class MyTreeSelectionListener implements TreeSelectionListener
	{
		public MyTreeSelectionListener()
		{

		}

		public void valueChanged(TreeSelectionEvent e)
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

	}

	private void createNodes(DefaultMutableTreeNode top)
	{
		DefaultMutableTreeNode testCase = null;
		DefaultMutableTreeNode testStep = null;

		testCase = new DefaultMutableTreeNode("TestCase1");
		top.add(testCase);

		//original Tutorial
		testStep = new DefaultMutableTreeNode("Exchange1");
		testCase.add(testStep);

		//Tutorial Continued
//		testStep = new DefaultMutableTreeNode("Exchange2");
		testStep = new DefaultMutableTreeNode(new GCXpathAssertion(null, "test"));
		testCase.add(testStep);

		testCase = new DefaultMutableTreeNode("TestCase2");
		top.add(testCase);

		//VM
		testStep = new DefaultMutableTreeNode("TestStep21");
		testCase.add(testStep);

		//Language Spec
		testStep = new DefaultMutableTreeNode("TestStep22");
		testCase.add(testStep);

		testCase = new DefaultMutableTreeNode("TestCase3");
		top.add(testCase);

		//VM
		testStep = new DefaultMutableTreeNode("TestStep31");
		testCase.add(testStep);

		//Language Spec
		testStep = new DefaultMutableTreeNode("TestStep32");
		testCase.add(testStep);

		testCase = new DefaultMutableTreeNode("TestCase4");
		top.add(testCase);

		//VM
		testStep = new DefaultMutableTreeNode("TestStep41");
		testCase.add(testStep);

		//Language Spec
		testStep = new DefaultMutableTreeNode("TestStep42");
		testCase.add(testStep);

		testCase = new DefaultMutableTreeNode("TestCase5");
		top.add(testCase);

		//VM
		testStep = new DefaultMutableTreeNode("TestStep51");
		testCase.add(testStep);

		//Language Spec
		testStep = new DefaultMutableTreeNode("TestStep52");
		testCase.add(testStep);
	}


}
