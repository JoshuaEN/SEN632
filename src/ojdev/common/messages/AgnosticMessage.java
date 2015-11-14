package ojdev.common.messages;

public abstract class AgnosticMessage extends MessageBase {

	private static final long serialVersionUID = 10642621333595810L;

	public AgnosticMessage() {
		super();
	}

	public final AllowedMessageContext getAllowedContext() {
		return AllowedMessageContext.Either;
	}

}