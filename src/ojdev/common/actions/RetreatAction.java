package ojdev.common.actions;

/**
 * A special action that ends the engagement.
 */
public class RetreatAction extends Action implements Special {

	private static final long serialVersionUID = -2731330942643794405L;
	
	public RetreatAction(String name, ActionDirection direction, int defensePowerModifier) {
		super(name, direction, defensePowerModifier);
	}
	
	public RetreatAction(String name, ActionDirection direction) {
		this(name, direction, 0);
	}
	
	public RetreatAction(int defensePowerModifier) {
		this("Retreat", ActionDirection.None, defensePowerModifier);
	}
	
	public RetreatAction() {
		this(0);
	}

	@Override
	public String toString() {
		return String.format("RetreatAction[getName()=%s, getDirection()=%s]", getName(), getDirection());
	}

}