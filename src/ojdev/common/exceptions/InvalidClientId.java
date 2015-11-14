package ojdev.common.exceptions;

public class InvalidClientId extends Exception {

	private static final long serialVersionUID = -1193678608706610287L;
	
	private final int clientId;
	
	public InvalidClientId(int clientId) {
		super(String.format("Invalid Client ID of %d", clientId));
		this.clientId = clientId;
	}
	
	public InvalidClientId(int clientId, String message) {
		super(message);
		this.clientId = clientId;
	}
	
	public int getClientId() {
		return clientId;
	}
}
