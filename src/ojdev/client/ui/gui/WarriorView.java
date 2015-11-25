package ojdev.client.ui.gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import ojdev.common.Armory;
import ojdev.common.warriors.CryptoWarrior;
import ojdev.common.warriors.UndeadWarrior;
import ojdev.common.warriors.Warrior;
import ojdev.common.warriors.WarriorBase;
import ojdev.common.warriors.WarriorBase.UnusableWeaponException;
import ojdev.common.weapons.Weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;

@SuppressWarnings("serial")
public class WarriorView extends JPanel {

	public static final int EDIT_MODE_NEW = 0;
	public static final int EDIT_MODE_EDIT = 1;
	public static final int EDIT_MODE_NONE = 2;

	private int editingMode = EDIT_MODE_NONE;
	private JComboBox<String> txtType;
	private JFormattedTextField txtName;
	private JSpinner txtHealth;
	private JComboBox<String> txtWeapon;
	private JTextField txtOriginLocation;
	private JTextArea txtDescription;
	
	private static final List<String> KNOWN_WARRIOR_TYPES;
	private JPasswordField txtCryptoKey;
	private JLabel lblCryptoKey;
	static {
		List<String> tmpList = new ArrayList<String>();
		
		tmpList.add(Warrior.TYPE_NAME);
		tmpList.add(UndeadWarrior.TYPE_NAME);
		tmpList.add(CryptoWarrior.TYPE_NAME);
		
		KNOWN_WARRIOR_TYPES = Collections.unmodifiableList(tmpList);
	}
	
	private WarriorBase setWarrior;
	
	/**
	 * Create the panel.
	 */
	public WarriorView() {
		setLayout(new BorderLayout(0, 0));
		
		JPanel panelWarriorInfo = new JPanel();
		add(panelWarriorInfo);
		panelWarriorInfo.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
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
				FormSpecs.PREF_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		JLabel lblType = DefaultComponentFactory.getInstance().createLabel("Type");
		panelWarriorInfo.add(lblType, "4, 2, right, default");
		
		txtType = new JComboBox<String>();
		for(String type : KNOWN_WARRIOR_TYPES) {
			txtType.addItem(type);
		}
				
		txtType.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateWeaponList();
				updateFields();
			}
		});
		panelWarriorInfo.add(txtType, "6, 2, fill, default");
		
		JLabel lblName = DefaultComponentFactory.getInstance().createLabel("Name");
		panelWarriorInfo.add(lblName, "4, 4, right, default");
		
		txtName = new JFormattedTextField(new JFormattedTextField.AbstractFormatter() {
			
			@Override
			public String valueToString(Object value) throws ParseException {
				if(value == null)
					return "";
				else
					return value.toString();
			}
			
			@Override
			public Object stringToValue(String text) throws ParseException {
				if(text.matches(WarriorBase.NAME_REGEX) == false) {
					throw new ParseException("Name can only consist of A to Z, 0 to 9, and _; it also cannot be blank", 0);
				}
				return text;
			}
		});
		panelWarriorInfo.add(txtName, "6, 4, fill, default");
		txtName.setColumns(10);
		
		JLabel lblHealth = DefaultComponentFactory.getInstance().createLabel("Health");
		panelWarriorInfo.add(lblHealth, "4, 6, right, default");
		
		txtHealth = new JSpinner();
		txtHealth.setModel(new SpinnerNumberModel(100, 0, 100, 1));
		panelWarriorInfo.add(txtHealth, "6, 6, fill, default");
		
		JLabel lblWeapon = DefaultComponentFactory.getInstance().createLabel("Weapon");
		panelWarriorInfo.add(lblWeapon, "4, 8, right, default");
		
		txtWeapon = new JComboBox<String>();
		panelWarriorInfo.add(txtWeapon, "6, 8, fill, default");
		
		JLabel lblOriginLocation = DefaultComponentFactory.getInstance().createLabel("Origin Location");
		panelWarriorInfo.add(lblOriginLocation, "4, 10, right, default");
		
		txtOriginLocation = new JTextField();
		panelWarriorInfo.add(txtOriginLocation, "6, 10, fill, center");
		txtOriginLocation.setColumns(10);
		
		JLabel lblDescription = DefaultComponentFactory.getInstance().createLabel("Description");
		panelWarriorInfo.add(lblDescription, "4, 12, right, top");
		
		JScrollPane scrollPane = new JScrollPane();
		panelWarriorInfo.add(scrollPane, "6, 12, fill, fill");
		
		txtDescription = new JTextArea();
		scrollPane.setViewportView(txtDescription);
		txtDescription.setRows(3);
		
		lblCryptoKey = new JLabel("Crypto Key");
		panelWarriorInfo.add(lblCryptoKey, "4, 14, right, default");
		
		txtCryptoKey = new JPasswordField();
		panelWarriorInfo.add(txtCryptoKey, "6, 14, fill, default");
	}

	public int getEditingMode() {
		return editingMode;
	}
	
	public void setEditingMode(int editingMode) {
		this.editingMode = editingMode;
		
		boolean toggle = false;
		
		if(editingMode == EDIT_MODE_NEW)
			toggle = true;
		
		txtType.setEnabled(toggle);
		txtName.setEnabled(toggle);
		
		if(editingMode == EDIT_MODE_NEW || editingMode == EDIT_MODE_EDIT)
			toggle = true;
		
		txtHealth.setEnabled(toggle);
		txtWeapon.setEnabled(toggle);
		txtOriginLocation.setEnabled(toggle);
		txtDescription.setEnabled(toggle);
		
		if(toggle) {
			txtDescription.setBackground(Color.white);
		} else {
			txtDescription.setBackground(UIManager.getColor("ComboBox.disabledBackground"));
		}
		
	}
	
	public void setWarrior(WarriorBase warrior) {
		setWarrior = warrior;
		if(warrior != null) {
			txtType.setSelectedItem(warrior.getTypeName());
			txtName.setText(warrior.getName());
			txtHealth.setValue(warrior.getHealth());
			updateWeaponList();
			txtWeapon.setSelectedItem(warrior.getEquippedWeapon().getName());
			txtOriginLocation.setText(warrior.getOriginLocation());
			txtDescription.setText(warrior.getDescription());
			
		} else {
			txtType.setSelectedIndex(0);
			txtName.setText("");
			txtHealth.setValue(100);
			updateWeaponList();
			txtOriginLocation.setText("");
			txtDescription.setText("");
		}
		txtCryptoKey.setText("");
	}
	
	/**
	 * Gets a new Warrior, as represented the by the Displayed Information
	 * This WILL NOT return the same Warrior as given via the Constructor or setWarrior
	 * @return a new WarriorBase object representing the displayed information
	 */
	public WarriorBase getWarrior() {
		try {
			switch ((String)txtType.getSelectedItem()) {
			case Warrior.TYPE_NAME:
				return new Warrior(
						txtName.getText(), 
						txtOriginLocation.getText(), 
						txtDescription.getText(), 
						(int)txtHealth.getValue(), 
						Armory.getWeaponFromName((String)txtWeapon.getSelectedItem())
				);
			case UndeadWarrior.TYPE_NAME:
				return new UndeadWarrior(
						txtName.getText(), 
						txtOriginLocation.getText(), 
						txtDescription.getText(), 
						(int)txtHealth.getValue(), 
						Armory.getWeaponFromName((String)txtWeapon.getSelectedItem())
				);
			
			case CryptoWarrior.TYPE_NAME:
				CryptoWarrior cWarrior = new CryptoWarrior(
						txtName.getText(), 
						txtOriginLocation.getText(), 
						txtDescription.getText(), 
						(int)txtHealth.getValue(), 
						Armory.getWeaponFromName((String)txtWeapon.getSelectedItem()),
						new String(txtCryptoKey.getPassword())
				);
				
				if(setWarrior != null && editingMode == EDIT_MODE_EDIT && setWarrior instanceof CryptoWarrior) {
					CryptoWarrior oldCWarrior = (CryptoWarrior)setWarrior;
					
					cWarrior.setKey(oldCWarrior.getKey());
					cWarrior.setSalt(oldCWarrior.getSalt());
					cWarrior.setIterations(oldCWarrior.getIterations());
				}
				
				return cWarrior;
			
			default:
				throw new IllegalArgumentException("Class named type of " + txtType.getSelectedItem() + " cannot be found");
			}
		} catch(UnusableWeaponException e) {
			// Should never happen
			assert false : "Weapon combo box should correctly restrict weapons to approved ones";
			throw new IllegalArgumentException("Invalid Weapon", e);
		}
	}

	private List<Weapon> getWarriorUsableWeapons() {
		if(txtType.getSelectedIndex() == -1) {
			return new ArrayList<Weapon>();
		}
		
		switch ((String)txtType.getSelectedItem()) {
		case Warrior.TYPE_NAME:
			return Warrior.USEABLE_WEAPONS;
		case UndeadWarrior.TYPE_NAME:
			return UndeadWarrior.USEABLE_WEAPONS;
		case CryptoWarrior.TYPE_NAME:
			return CryptoWarrior.USEABLE_WEAPONS;
			
		default:
			throw new IllegalArgumentException("Class named type of " + txtType.getSelectedItem() + " cannot be found");
		}
	}
	
	private void updateWeaponList() {
		txtWeapon.removeAllItems();
		for(Weapon weapon : getWarriorUsableWeapons()) {
			txtWeapon.addItem(weapon.getName());
		}
	}
	
	private void updateFields() {
		boolean cryptoToggle = false;
		
		if(txtType.getSelectedIndex() != -1 && (String)txtType.getSelectedItem() == CryptoWarrior.TYPE_NAME && editingMode == EDIT_MODE_NEW) {
			cryptoToggle = true;
		}
		
		txtCryptoKey.setVisible(cryptoToggle);
		lblCryptoKey.setVisible(cryptoToggle);
	}
}
