package ojdev.client.ui.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ojdev.common.actions.Action;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class ActionSelectorDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private final ActionSelector selector;
	private String result;
	
	/**
	 * Create the dialog.
	 */
	public ActionSelectorDialog(List<Action> actions) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent arg0) {
				handleControlButtonAction(new ActionEvent(arg0.getSource(), ActionEvent.ACTION_PERFORMED, "Cancel"));
			}
		});
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(null);
		contentPanel.setLayout(new BorderLayout(0, 0));
		selector = new ActionSelector(actions);
		contentPanel.add(selector);
		setTitle("Action Selector");
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				buttonPane.setLayout(new BorderLayout(0, 0));
			}
			{
				JPanel panel = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel.getLayout();
				flowLayout.setAlignment(FlowLayout.TRAILING);
				buttonPane.add(panel, BorderLayout.CENTER);
				JButton okButton = new JButton("OK");
				panel.add(okButton);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						handleControlButtonAction(arg0);
					}
				});
				okButton.setActionCommand("OK");
				getRootPane().setDefaultButton(okButton);
				{
					JButton cancelButton = new JButton("Cancel");
					panel.add(cancelButton);
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							handleControlButtonAction(e);
						}
					});
					cancelButton.setActionCommand("Cancel");
				}
			}
			{
				JLabel lblPleaseSelectAn = new JLabel("Please Select an Action then click OK");
				lblPleaseSelectAn.setBorder(new EmptyBorder(0, 5, 0, 0));
				buttonPane.add(lblPleaseSelectAn, BorderLayout.WEST);
				lblPleaseSelectAn.setFont(new Font("Tahoma", Font.BOLD, 12));
			}
		}
	}

	public Action getSelectedAction() {
		return selector.getSelectedAction();
	}
	
	public String showDialog() {
		setVisible(true);
		return result;
	}
	
	private void handleControlButtonAction(ActionEvent e) {
		result = e.getActionCommand();
		setVisible(false);
	}
}
