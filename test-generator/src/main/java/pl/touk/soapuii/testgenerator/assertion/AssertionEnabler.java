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
import com.eviware.soapui.support.types.TupleList.Tuple;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import org.apache.xmlbeans.XmlException;
import org.oasisOpen.docs.wss.x2004.x01.oasis200401WssWssecuritySecext10.TUsage;
import pl.touk.soapuii.testgenerator.data.GCConfig;
import pl.touk.soapuii.testgenerator.data.GCResult;
import pl.touk.soapuii.testgenerator.data.GCTestCase;
import pl.touk.soapuii.testgenerator.data.GCTestStep;
import pl.touk.soapuii.testgenerator.data.GCXpathAssertion;

/**
 *
 * @author azl
 */
public class AssertionEnabler
{
	public final static String DISABLED = "Disabled";
	public final static String STATIC = "Static";
	public final static String CASE = "Case";
	public final static String SUITE = "Suite";

	private ArrayList<AssertionTuple> assertionTupleList;
	private Set<GCTestStep> changedTestSteps = new HashSet();
		
//	protected static int numberOfAssertions = 60;

	protected static WsdlProject project;
	private SwingConfigurationDialogImpl dialog;
	private JSplitPane split;

	protected StringToStringMap values = new StringToStringMap();
	private JTree tree;
	private JScrollPane treeView;
	private JScrollPane paneView;


	private GCResult result;
//	private static AssertionEnabler instance;


//	protected static HashMap<GCXpathAssertion, String> assertionToChoiceMap = new HashMap<GCXpathAssertion, String>();

	public AssertionEnabler(GCResult result)
	{
		assertionTupleList = new ArrayList<AssertionTuple>();
		this.result = result;
//		instance = this;
		createTree();
		paneView = createExchangePane(result.getTestCases().get(0).getTestSteps().get(0));
	}

	public void show()
	{
		buildDialog();
	}

	protected void attachPathToProject(String projectPath) throws XmlException, IOException, SoapUIException
	{
		project = new WsdlProject( projectPath );
	}

	protected void buildDialog()
	{

		for(int i = 0; i < 10; i++)
		{
			values.put(("string"+i), ("string"+i));
		}

		dialog = new SwingConfigurationDialogImpl( "Enable Assertions", "http://top.touk.pl/confluence/display/SUI/Test-generator",
				"Set set which assertion can be enabled with xpath", UISupport.OPTIONS_ICON );

		split = UISupport.createHorizontalSplit(UISupport.setFixedSize(treeView, 200, 500), createPanelForRightPanelSplit(paneView));
		dialog.setSize(new Dimension(800, 500));
		dialog.setContent( split );

		dialog.show(values);
	}

	private JComponent createPanelForRightPanelSplit(JComponent comp) {
		return UISupport.setFixedSize(comp, 300, 500);
	}

	private JComboBox createComboBox(GCXpathAssertion assertion)
	{
		JComboBox box = UISupport.createComboBox(100, STATIC);
		box.addItem(STATIC);
		box.addItem(DISABLED);
		box.addItem(SUITE);
		box.addItem(CASE);
		switch(assertion.getConfig().getType())
		{
			case STATIC:
				box.setSelectedIndex(0);
				break;
			case DISABLED:
				box.setSelectedIndex(1);
				break;
			case SUITE:
				box.setSelectedIndex(2);
				break;
			case CASE:
				box.setSelectedIndex(3);
				break;
		}
//		final GCXpathAssertion tempAssr = assertion;
		return box;
	}

	private JLabel createLabel(String label)
	{
		JLabel jlabel = new JLabel(label);
		jlabel.setPreferredSize(new Dimension(380, 20));
//		jlabel.setMinimumSize(new Dimension(100, 20));
		jlabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		return jlabel;
	}

	private JTextField createTextField(String field)
	{
		JTextField jtext = new JTextField(field);
		jtext.setPreferredSize(new Dimension(200, 20));
		jtext.setMinimumSize(new Dimension(150, 20));
		jtext.setMaximumSize(new Dimension(500, 20));
//		jtext.setMaximumSize(new Dimension(340, 0));
		//jtext.setSize(200, 20);

		return jtext;
	}

	private JScrollPane createExchangePane(GCTestStep testStep)
	{
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(300, testStep.getXpathAssertions().size()*20 + 40));
		panel.setLayout(new BorderLayout());
		Box box = Box.createHorizontalBox();

		Box column1 = Box.createVerticalBox();
		Box column2 = Box.createVerticalBox();
		Box column3 = Box.createVerticalBox();

		assertionTupleList.clear();

		for (GCXpathAssertion assertion : testStep.getXpathAssertions())
		{
//			sub.setPreferredSize(new Dimension(Integer.MAX_VALUE, 20));//
			AssertionTuple tuple = new AssertionTuple(
			assertion, createComboBox(assertion), createTextField(assertion.getConfig().getValue()));
			column1.add(createLabel(assertion.getShortName()));
			column2.add(tuple.getCombo());
			column3.add(tuple.getText());

			assertionTupleList.add(tuple);
//			box.add(sub);
		}
		column1.add(Box.createVerticalGlue());
		column2.add(Box.createVerticalGlue());
		column3.add(Box.createVerticalGlue());
		
		box.add(Box.createHorizontalStrut(10));
		box.add(column1);
		box.add(Box.createHorizontalStrut(10));
		box.add(column2);
		box.add(Box.createHorizontalStrut(10));
		box.add(column3);
		box.add(Box.createHorizontalStrut(10));


//		box.add(Box.createVerticalStrut(Integer.MAX_VALUE));
		if(!testStep.getXpathAssertions().isEmpty())
		{
			Box buttonBox = Box.createHorizontalBox();
			Box mainBox = Box.createVerticalBox();

			buttonBox.add(Box.createHorizontalGlue());
			buttonBox.add(createApplyButton());
			buttonBox.add(Box.createHorizontalStrut(2));
			buttonBox.add(createApplyToAllButton());
			buttonBox.add(Box.createHorizontalStrut(10));
			buttonBox.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));
//			buttonBox.setMinimumSize(new Dimension(1000, 30));
			buttonBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

//			Border tmp = new LineBorder(Color.red);
//			buttonBox.setBorder(tmp);

//			mainBox.add(Box.createVerticalGlue());
			mainBox.add(box);
			mainBox.add(buttonBox);

			panel.add(mainBox);
		}
		
		JScrollPane jScrollPane = new JScrollPane(panel);
		jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		return jScrollPane;
	}


	private JButton createApplyButton()
	{
		JButton button = new JButton("Apply");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				for(AssertionTuple tuple: assertionTupleList)
				{
					String assertionContent = tuple.getText().getText();
					String choice = (String) tuple.getCombo().getModel().getSelectedItem();
					GCConfig config = null;

					if(choice.equals(DISABLED))
						config = new GCConfig(GCConfig.Type.DISABLED, assertionContent);
					if(choice.equals(STATIC))
						config = new GCConfig(GCConfig.Type.STATIC, assertionContent);
					if(choice.equals(SUITE))
						config = new GCConfig(GCConfig.Type.SUITE, assertionContent);
					if(choice.equals(CASE))
						config = new GCConfig(GCConfig.Type.CASE, assertionContent);

					tuple.getAssertion().setConfig(config);
					changedTestSteps.add(tuple.getAssertion().getParent());
					System.err.println("adding: " + tuple.getAssertion().getParent());
				}
				tree.repaint();
			}
		});
		return button;
	}

	private JButton createApplyToAllButton()
	{
		JButton button = new JButton("ApplyToAll");
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				for(AssertionTuple tuple: assertionTupleList)
				{
					String assertionContent = tuple.getText().getText();
					String choice = (String) tuple.getCombo().getModel().getSelectedItem();
					GCConfig config = null;

					if(choice.equals(DISABLED))
						config = new GCConfig(GCConfig.Type.DISABLED, assertionContent);
					if(choice.equals(STATIC))
						config = new GCConfig(GCConfig.Type.STATIC, assertionContent);
					if(choice.equals(SUITE))
						config = new GCConfig(GCConfig.Type.SUITE, assertionContent);
					if(choice.equals(CASE))
						config = new GCConfig(GCConfig.Type.CASE, assertionContent);
										
					List<GCXpathAssertion> changedAssertions = 
							result.setSimilarXpathAssertions(tuple.getAssertion(), config);
					for (GCXpathAssertion assertion : changedAssertions)
					{
						changedTestSteps.add(assertion.getParent());
						System.err.println("adding: " + assertion.getParent());
					}
				}
				tree.repaint();
			}
		});
		return button;
	}

	private void createTree()
	{
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("TestGenerator");
		createNodes(top);
		tree = new JTree(top);
		tree.setCellRenderer(new AssertionTreeCellRenderer());
		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeChangeListener());
//		tree.setSize(new Dimension(300, 400));

		int height = (result.getTestCases().size())*(result.getTestCases().get(0).getTestSteps().size()*30);

		tree.setPreferredSize(new Dimension(200, height));
		tree.setMinimumSize(new Dimension(150, 500));
		tree.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));

		treeView = new JScrollPane(tree);
		treeView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		treeView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	}	

	private void createNodes(DefaultMutableTreeNode top)
	{

		for (GCTestCase testCase : result.getTestCases())
		{
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(testCase);
			top.add(node);
			for (GCTestStep testStep : testCase.getTestSteps())
				node.add(new DefaultMutableTreeNode(testStep));
		}
	}

	private class AssertionTuple
	{
		private GCXpathAssertion assertion;
		private JComboBox combo;
		private JTextField text;

		public AssertionTuple(GCXpathAssertion assertion, JComboBox combo, JTextField text)
		{
			this.assertion = assertion;
			this.combo = combo;
			this.text = text;
		}

		public GCXpathAssertion getAssertion() { return assertion; }
		public JComboBox getCombo() { return combo; }
		public JTextField getText() { return text; }
	}

	private class TreeChangeListener implements TreeSelectionListener
	{
		public void valueChanged(TreeSelectionEvent e)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						   tree.getLastSelectedPathComponent();

			if (node == null) return;

			Object nodeInfo = node.getUserObject();
			if (node.isLeaf() && nodeInfo instanceof GCTestStep)
			{
				GCTestStep testStep = (GCTestStep) nodeInfo;
				updatePane(testStep);
			}
		}

		private void updatePane(GCTestStep testStep)
		{
			paneView = createExchangePane(testStep);
			split.setRightComponent(createPanelForRightPanelSplit(paneView));
		}
	}

	private class AssertionTreeCellRenderer extends DefaultTreeCellRenderer
	{
		public Component getTreeCellRendererComponent(JTree pTree,
				Object pValue, boolean pIsSelected, boolean pIsExpanded,
				boolean pIsLeaf, int pRow, boolean pHasFocus)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) pValue;
			super.getTreeCellRendererComponent(pTree, pValue, pIsSelected,
					pIsExpanded, pIsLeaf, pRow, pHasFocus);

			if (pIsLeaf && changedTestSteps.contains(node.getUserObject()))
			{
				setBackgroundNonSelectionColor(new Color(100, 255, 100));
			}
			else
				setBackgroundNonSelectionColor(Color.white);
			return this;
		}
	}

}
