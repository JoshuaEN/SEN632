package ojdev.client.ui.gui;

import javax.swing.JPanel;

import ojdev.client.Client;
import ojdev.common.ConnectedClientState;
import ojdev.common.SelectedAction;
import ojdev.common.WarriorCombatResult;
import ojdev.common.actions.Action;
import ojdev.common.weapons.Weapon;

import java.awt.BorderLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JLabel;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import javax.swing.JTextField;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JToggleButton;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JProgressBar;

import static ojdev.client.ui.gui.ClientFormatHelper.*;

@SuppressWarnings("serial")
public class EngagedWarrior extends JPanel {

	private final boolean isUs;
	private final Client client;
	private ConnectedClientState state;
	private WarriorCombatResult combatResult;
	private JTextField txtWeapon;
	private JTextField txtAction;
	private JTextField txtAttackSpeed;
	private JTextField txtAttackPower;
	private JTextField txtDefensePower;
	private JTextField txtAttackSpeedMod;
	private JTextField txtAttackSpeedTotal;
	private JTextField txtAttackPowerMod;
	private JTextField txtAttackPowerTotal;
	private JTextField txtDefensePowerMod;
	private JTextField txtDefensePowerTotal;
	private JLabel lblTitle;
	private JProgressBar healthBar;
	private JToggleButton btnTarget;
	private JButton btnWarriorDetails;
	private JButton btnSendMessage;
	private JLabel lblTarget;
	private JTextField txtTarget;
	private JLabel lblChosenActionFor;
	private JLabel lblWeaponBonus;
	private JTextField txtAttackPowerWepBonus;
	
	/**
	 * Create the panel.
	 */
	public EngagedWarrior(Client client, ConnectedClientState state, EngagedWarriorCallbackInterface callback) {
		this.isUs = client.getClientId() == state.getClientId();
		this.client = client;
		setLayout(new BorderLayout(0, 0));
		
		
		JPanel panel_1 = new JPanel();
		add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel buttonGrid = new JPanel();
		panel_1.add(buttonGrid, BorderLayout.SOUTH);
		GridBagLayout gbl_buttonGrid = new GridBagLayout();
		gbl_buttonGrid.columnWidths = new int[]{129, 0};
		gbl_buttonGrid.rowHeights = new int[]{23, 23, 23, 0};
		gbl_buttonGrid.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_buttonGrid.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		buttonGrid.setLayout(gbl_buttonGrid);
		
		btnTarget = new JToggleButton("Set as Target");
		btnTarget.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				callback.notifyTargetStatusChanged(EngagedWarrior.this, btnTarget.isSelected());
				updateTargetState();
			}
		});
		GridBagConstraints gbc_btnTarget = new GridBagConstraints();
		gbc_btnTarget.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnTarget.insets = new Insets(0, 0, 5, 0);
		gbc_btnTarget.gridx = 0;
		gbc_btnTarget.weightx = 1;
		gbc_btnTarget.gridy = 0;
		buttonGrid.add(btnTarget, gbc_btnTarget);
		
		btnWarriorDetails = new JButton("View Warrior Details");
		GridBagConstraints gbc_btnWarriorDetails = new GridBagConstraints();
		gbc_btnWarriorDetails.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnWarriorDetails.insets = new Insets(0, 0, 5, 0);
		gbc_btnWarriorDetails.gridx = 0;
		gbc_btnWarriorDetails.weightx = 1;
		gbc_btnWarriorDetails.gridy = 1;
		buttonGrid.add(btnWarriorDetails, gbc_btnWarriorDetails);
		btnWarriorDetails.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				callback.notifyOpenWarriorDetails(state.getWarrior());
			}
		});
		
		btnSendMessage = new JButton("Send Message");
		btnSendMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				callback.notifyOpenPrivateChat(state.getClientId());
			}
		});
		
		if(isUs) {
			btnSendMessage.setEnabled(false);
			btnSendMessage.setToolTipText("Can't send a message to yourself");
		}
		
		GridBagConstraints gbc_btnSendMessage = new GridBagConstraints();
		gbc_btnSendMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSendMessage.insets = new Insets(0, 0, 5, 0);
		gbc_btnSendMessage.gridx = 0;
		gbc_btnSendMessage.weightx = 1;
		gbc_btnSendMessage.gridy = 2;
		buttonGrid.add(btnSendMessage, gbc_btnSendMessage);
		
		JPanel panel = new JPanel();
		panel_1.add(panel, BorderLayout.NORTH);
		
		lblTitle = DefaultComponentFactory.getInstance().createLabel("Title");
		panel.add(lblTitle);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
		
		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_3 = new JPanel();
		panel_2.add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel lblWeapon = new JLabel("Weapon");
		panel_3.add(lblWeapon, "2, 2, right, default");
		
		txtWeapon = new JTextField();
		txtWeapon.setEnabled(false);
		panel_3.add(txtWeapon, "4, 2, 5, 1, fill, default");
		txtWeapon.setColumns(10);
		
		lblChosenActionFor = new JLabel("Currently Selected Action");
		lblChosenActionFor.setFont(new Font("Tahoma", Font.BOLD, 11));
		panel_3.add(lblChosenActionFor, "4, 6, 5, 1");
		
		JLabel lblAction = new JLabel("Action");
		panel_3.add(lblAction, "2, 8, right, default");
		
		txtAction = new JTextField();
		txtAction.setEnabled(false);
		panel_3.add(txtAction, "4, 8, 5, 1, fill, default");
		txtAction.setColumns(10);
		
		lblTarget = new JLabel("Target");
		lblTarget.setHorizontalAlignment(SwingConstants.TRAILING);
		panel_3.add(lblTarget, "2, 10, right, default");
		
		txtTarget = new JTextField();
		txtTarget.setEnabled(false);
		panel_3.add(txtTarget, "4, 10, 5, 1, fill, default");
		txtTarget.setColumns(10);
		
		JLabel lblAttackSpeed = new JLabel("ATK SPD");
		lblAttackSpeed.setToolTipText("Attack Speed");
		panel_3.add(lblAttackSpeed, "4, 12, center, default");
		
		JLabel lblAttackPower = new JLabel("ATK PWR");
		lblAttackPower.setToolTipText("Attack Power");
		panel_3.add(lblAttackPower, "6, 12, center, default");
		
		JLabel lblDefensePower = new JLabel("DEF PWR");
		lblDefensePower.setToolTipText("Defense Power");
		panel_3.add(lblDefensePower, "8, 12, center, default");
		
		JLabel lblBase = new JLabel("Weapon Base");
		lblBase.setToolTipText("Base Stats of the Weapon");
		lblBase.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(lblBase, "2, 14, right, default");
		
		txtAttackSpeed = new JTextField();
		txtAttackSpeed.setHorizontalAlignment(SwingConstants.CENTER);
		txtAttackSpeed.setEnabled(false);
		panel_3.add(txtAttackSpeed, "4, 14, fill, default");
		txtAttackSpeed.setColumns(10);
		
		txtAttackPower = new JTextField();
		txtAttackPower.setHorizontalAlignment(SwingConstants.CENTER);
		txtAttackPower.setEnabled(false);
		panel_3.add(txtAttackPower, "6, 14, fill, default");
		txtAttackPower.setColumns(10);
		
		txtDefensePower = new JTextField();
		txtDefensePower.setHorizontalAlignment(SwingConstants.CENTER);
		txtDefensePower.setEnabled(false);
		panel_3.add(txtDefensePower, "8, 14, fill, default");
		txtDefensePower.setColumns(10);
		
		lblWeaponBonus = new JLabel("Weapon Bonus");
		lblWeaponBonus.setToolTipText("Bonus from Action Damage Type");
		panel_3.add(lblWeaponBonus, "2, 16, right, default");
		
		txtAttackPowerWepBonus = new JTextField();
		txtAttackPowerWepBonus.setHorizontalAlignment(SwingConstants.CENTER);
		txtAttackPowerWepBonus.setEnabled(false);
		panel_3.add(txtAttackPowerWepBonus, "6, 16, fill, default");
		txtAttackPowerWepBonus.setColumns(10);
		
		JLabel lblAct = new JLabel("Action Bonus");
		lblAct.setToolTipText("Bonus (or penalty) from Action");
		lblAct.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(lblAct, "2, 18, right, default");
		
		txtAttackSpeedMod = new JTextField();
		txtAttackSpeedMod.setHorizontalAlignment(SwingConstants.CENTER);
		txtAttackSpeedMod.setEnabled(false);
		panel_3.add(txtAttackSpeedMod, "4, 18, fill, default");
		txtAttackSpeedMod.setColumns(10);
		
		txtAttackPowerMod = new JTextField();
		txtAttackPowerMod.setHorizontalAlignment(SwingConstants.CENTER);
		txtAttackPowerMod.setEnabled(false);
		panel_3.add(txtAttackPowerMod, "6, 18, fill, default");
		txtAttackPowerMod.setColumns(10);
		
		txtDefensePowerMod = new JTextField();
		txtDefensePowerMod.setHorizontalAlignment(SwingConstants.CENTER);
		txtDefensePowerMod.setEnabled(false);
		panel_3.add(txtDefensePowerMod, "8, 18, fill, default");
		txtDefensePowerMod.setColumns(10);
		
		JLabel lblResult = new JLabel("Effective Value");
		lblResult.setToolTipText("The effective stats of Weapon/Action combo\r\nThis may not match Base + Act+, as the Weapon/Action combo may interact beyond their basic stats.");
		lblResult.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(lblResult, "2, 20, right, default");
		
		txtAttackSpeedTotal = new JTextField();
		txtAttackSpeedTotal.setHorizontalAlignment(SwingConstants.CENTER);
		txtAttackSpeedTotal.setEnabled(false);
		panel_3.add(txtAttackSpeedTotal, "4, 20, fill, default");
		txtAttackSpeedTotal.setColumns(10);
		
		txtAttackPowerTotal = new JTextField();
		txtAttackPowerTotal.setHorizontalAlignment(SwingConstants.CENTER);
		txtAttackPowerTotal.setEnabled(false);
		panel_3.add(txtAttackPowerTotal, "6, 20, fill, default");
		txtAttackPowerTotal.setColumns(10);
		
		txtDefensePowerTotal = new JTextField();
		txtDefensePowerTotal.setHorizontalAlignment(SwingConstants.CENTER);
		txtDefensePowerTotal.setEnabled(false);
		panel_3.add(txtDefensePowerTotal, "8, 20, fill, default");
		txtDefensePowerTotal.setColumns(10);
		
		healthBar = new JProgressBar();
		healthBar.setStringPainted(true);
		panel_2.add(healthBar, BorderLayout.NORTH);
		setState(state);
	}
	
	public void setState(ConnectedClientState state) {
		this.state = state;
		
		lblTitle.setText(getMasterNameFromState(state, isUs));
		
		txtWeapon.setText(state.getWarrior().getEquippedWeapon().getName());
		updateHealthBar();
		
		String attackPower, attackSpeed, defensePower;
			attackPower = attackSpeed = defensePower = "";
			
		Weapon weapon = state.getWarrior().getEquippedWeapon();
		attackPower = "" + weapon.getAttackPower();
		attackSpeed = "" + weapon.getAttackSpeed();
		defensePower = "" + weapon.getDefensePower();
		txtAttackPower.setText(attackPower);
		txtAttackSpeed.setText(attackSpeed);
		txtDefensePower.setText(defensePower);
	}
	
	public void setSelectedAction(SelectedAction selectedAction) {	
		String actionName, targetName,
				attackPowerMod, attackPowerWepBonus, attackPowerTotal, 
				attackSpeedMod, attackSpeedTotal,
				defensePowerMod, defensePowerTotal;
			actionName = targetName =
				attackPowerMod = attackPowerWepBonus = attackPowerTotal = 
				attackSpeedMod = attackSpeedTotal =
				defensePowerMod = defensePowerTotal = "";
		
		if(selectedAction != null) {
			Action action = selectedAction.getAction();
			Weapon weapon = state.getWarrior().getEquippedWeapon();
			ConnectedClientState targetState = client.getConnectedClientById(selectedAction.getTargetClientId());
			
			actionName = String.format(
					"%s; %s %s",
					action.getName(),
					weapon.getDamageTypeForAction(action),
					action.getStance()
					);
			
			if(targetState != null) {
				targetName = getMasterNameFromState(targetState, client.getClientId() == targetState.getClientId());
			} else {
				targetName = ((Integer)selectedAction.getTargetClientId()).toString();
			}
			
			attackPowerMod = "" + action.getAttackPowerModifier();
			attackSpeedMod = "" + action.getAttackSpeedModifier();
			defensePowerMod = "" + action.getDefensePowerModifier();
			
			attackPowerTotal = "" + weapon.getEffectiveAttackPower(action);
			attackSpeedTotal = "" + weapon.getEffectiveAttackSpeed(action);
			defensePowerTotal = "" + weapon.getEffectiveDefensePower(action);
			
			attackPowerWepBonus = "" + weapon.getDamageTypeModifier(action.getDamageType());
		}
		
		txtAction.setText(actionName);
		txtTarget.setText(targetName);
		
		txtAttackPowerMod.setText(attackPowerMod);
		txtAttackSpeedMod.setText(attackSpeedMod);
		txtDefensePowerMod.setText(defensePowerMod);
		
		txtAttackPowerTotal.setText(attackPowerTotal);
		txtAttackSpeedTotal.setText(attackSpeedTotal);
		txtDefensePowerTotal.setText(defensePowerTotal);
		
		txtAttackPowerWepBonus.setText(attackPowerWepBonus);
		
		selectedActionCurrent();
	}
	
	public void selectedActionCurrent() {
		lblChosenActionFor.setText("Currently Selected Action");
	}
	
	public void selectedActionStale() {
		lblChosenActionFor.setText("Previous Action");
	}
	
	public boolean isTarget() {
		return btnTarget.isSelected();
	}
	
	public void setTarget(boolean isTarget) {
		btnTarget.setSelected(isTarget);
		updateTargetState();		
	}
	
	private void updateTargetState() {
		if(btnTarget.isSelected()) {
			btnTarget.setText("Current Target");
		} else {
			btnTarget.setText("Set as Target");
		}
	}
	
	public int getClientId() {
		return state.getClientId();
	}
	
	public boolean getIsUs() {
		return isUs;
	}
	
	public void setCombatResult(WarriorCombatResult result) {
		this.combatResult = result;
		updateHealthBar();
	}

	private void updateHealthBar() {
		healthBar.setValue(state.getWarrior().getHealth());
		
		String healthTip = "Warrior's Remaining Health";
		String healthMsg;
		
		if(combatResult == null) {
			healthMsg = String.format("%d%%", state.getWarrior().getHealth());
		} else {
			healthTip += " (Health Change from Last Round)";
			healthMsg = String.format("%d%% (%+d)", state.getWarrior().getHealth(), combatResult.getHealthLost()*-1);
		}
		
		healthBar.setToolTipText(healthTip);
		healthBar.setString(healthMsg);
	}
}
