package ojdev.client.ui.gui;

import javax.swing.JPanel;

import ojdev.common.ConnectedClientState;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;
import java.awt.Font;
import java.awt.GridLayout;

@SuppressWarnings("serial")
public class ConnectedClientStateView extends JPanel {

	private ConnectedClientState state;
	
	public ConnectedClientStateView(ConnectedClientState state) {
		this.setState(state);
	}
	
	public int getClientId() {
		return state.getClientId();
	}

	public ConnectedClientState getState() {
		return state;
	}

	public void setState(ConnectedClientState state) {
		this.state = state;
		GridLayout gridLayout = new GridLayout(0, 3, 0, 0);

		setLayout(gridLayout);
		
		JLabel lblClientId = new JLabel("0");
		lblClientId.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblClientId.setHorizontalAlignment(SwingConstants.TRAILING);
		add(lblClientId);
		
		JCheckBox chkInEncounter = new JCheckBox("");
		chkInEncounter.setEnabled(false);
		add(chkInEncounter);
		
		JCheckBox chkCanBeEngaged = new JCheckBox("");
		chkCanBeEngaged.setEnabled(false);
		add(chkCanBeEngaged);
	}
	


}
