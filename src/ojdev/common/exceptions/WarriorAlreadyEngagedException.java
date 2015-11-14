package ojdev.common.exceptions;

public class WarriorAlreadyEngagedException extends Exception {

	private static final long serialVersionUID = 7373603067997704921L;

	public WarriorAlreadyEngagedException(String message) {
		super(message);
	}
	
	public WarriorAlreadyEngagedException() {
		super("Warrior is already in an engagement");
	}
}
