package ojdev.common.actions;

public enum ActionStance {
	ATTACK("Attack", "ATK"),
	DEFENSE_BLOCK("Defense Block", "DEF-B"),
	DEFENSE_COUNTER("Defense Counter", "DEF-C"),
	NONE("None", "");

	private final String name;
	private final String abbr;
	
	private ActionStance(String name, String abbr) {
		this.name = name;
		this.abbr = abbr;
	}

	public String getName() {
		return name;
	}

	public String getAbbr() {
		return abbr;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}
