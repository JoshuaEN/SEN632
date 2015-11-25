package ojdev.client.ui.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ojdev.common.warriors.WarriorBase;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class WarriorViewDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private WarriorView warriorView;
	
	/**
	 * Create the dialog.
	 */
	public WarriorViewDialog(WarriorBase warrior, int editingMode, WarriorViewDialogCallbackInterface callback) {
		setBounds(100, 100, 450, 308);
		setModalityType(ModalityType.MODELESS);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		warriorView = new WarriorView();
		warriorView.setWarrior(warrior);
		warriorView.setEditingMode(editingMode);
		
		contentPanel.add(warriorView);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			if(editingMode == WarriorView.EDIT_MODE_NEW || editingMode == WarriorView.EDIT_MODE_EDIT) {
				JButton okButton = new JButton("Save");
				okButton.setActionCommand("Save");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
				
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(callback != null) {
							callback.onWarriorViewDialogClosed(warriorView.getWarrior());
						}
						WarriorViewDialog.this.setVisible(false);
					}
				});
			}
			{
				JButton cancelButton = new JButton("Close");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(callback != null) {
							callback.onWarriorViewDialogClosed(null);
						}
						WarriorViewDialog.this.setVisible(false);
					}
				});
				cancelButton.setActionCommand("Close");
				buttonPane.add(cancelButton);
				if(editingMode == WarriorView.EDIT_MODE_NONE) {
					getRootPane().setDefaultButton(cancelButton);
				}
			}
		}
	}
	
	public WarriorBase getWarrior() {
		return warriorView.getWarrior();
	}
}
