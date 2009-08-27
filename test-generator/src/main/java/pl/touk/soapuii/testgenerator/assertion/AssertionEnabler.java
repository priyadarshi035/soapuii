/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.touk.soapuii.testgenerator.assertion;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SwingConfigurationDialogImpl;
import com.eviware.soapui.support.types.StringToStringMap;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

	protected static int numberOfAssertions = 10;

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


		dialog = new SwingConfigurationDialogImpl( "Enable Assertions", "http://top.touk.pl/confluence/display/SUI/Test-generator",
				"Set set which assertion can be enabled with xpath", UISupport.OPTIONS_ICON );

//		JPanel panelTemp = createExchangePane(numberOfAssertions);
		dialog.setContent( UISupport.createHorizontalSplit(UISupport.setFixedSize(createTree(), 200, 200), UISupport.setFixedSize(createExchangePane(numberOfAssertions), 800, 200)));

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
		JLabel jlabel = new JLabel(label);
		jlabel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 0));
		return jlabel;
	}

	private JTextField createTextField()
	{
		JTextField jtext = new JTextField();
		jtext.setMinimumSize(new Dimension(300, 0));
		//jtext.setSize(200, 20);

		return jtext;
	}

	private JScrollPane createExchangePane(int numberOfAssertions)
	{
//Pa
		
		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		Box box = Box.createVerticalBox();
		for (int i = 0; i < 10; i++) {
			Box sub = Box.createHorizontalBox();
			sub.setPreferredSize(new Dimension(Integer.MAX_VALUE, 20));
			//JPanel subPanel = new JPanel();
			//subPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 20));
			//subPanel.setLayout(new BorderLayout());
			//subPanel.add(sub);
			sub.add(Box.createHorizontalStrut(20));
			sub.add(createLabel("hej"+i));
			sub.add(Box.createHorizontalStrut(20));
			sub.add(createComboBox());
			sub.add(Box.createHorizontalStrut(20));
			sub.add(createTextField());
			sub.add(Box.createHorizontalStrut(20));
			box.add(sub);
		}
		//box.add(subPanel);
//		box.add(Box.createVerticalStrut(Integer.MAX_VALUE));
		panel.add(box);
		panel.setSize(800, 500);

		JScrollPane jScrollPane = new JScrollPane(panel);

		return jScrollPane;
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
		testStep = new DefaultMutableTreeNode("Exchange2");
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
