package ojdev.common.weapons;

public enum WeaponDamageType {
	PIERCING("Piercing", "p"),
	CUTTING("Cutting", "c"),
	BLUNT("Blunt", "b"),
	NONE("None", "");
	
	private final String printName;
	private final String printAbbr;
	
	private WeaponDamageType(String printName, String printAbbr) {
		this.printName = printName;
		this.printAbbr = printAbbr;
	}
	
	public String getName() {
		return printName;
	}
	
	public String getAbbr() {
		return printAbbr;
	}

	@Override
	public String toString() {
		return printName;
	}
}
