package ojdev.client.ui.gui;

import javax.swing.JPanel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;

import ojdev.common.Armory;
import ojdev.common.actions.Action;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.FlowLayout;
import javax.swing.ListSelectionModel;

@SuppressWarnings("serial")
public class ActionSelector extends JPanel {
	private DefaultListModel<String> model; 
	private JList<String> list;
	
	/**
	 * Create the panel.
	 */
	public ActionSelector(List<Action> actions) {
		setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane);
		
		list = new JList<String>();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setFont(new Font("Monospaced", Font.PLAIN, 11));
		model = new DefaultListModel<String>();
		list.setModel(model);
		scrollPane.setViewportView(list);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		add(panel, BorderLayout.SOUTH);
		
		JLabel lblLegendIndicates = new JLabel("Legend: ! indicates Actions which also End the Engagement");
		panel.add(lblLegendIndicates);

		setActions(actions);
	}

	public void setActions(List<Action> actions) {
		model.clear();
		
		for(Action action : actions) {
			model.addElement((action.isEngagementEnder() ? "!" : " ") + " " + action.getName());
		}
	}
	
	public Action getSelectedAction() {
		if(list.isSelectionEmpty()) {
			return null;
		} else {
			return Armory.getActionFromName(list.getSelectedValue().substring(2));
		}
	}
}
