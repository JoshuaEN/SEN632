package ojdev.common.exceptions;

@SuppressWarnings("serial")
public class CryptoKeyProblemUnlockException extends CryptoException {

	public CryptoKeyProblemUnlockException() {
		super();
	}

	public CryptoKeyProblemUnlockException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CryptoKeyProblemUnlockException(String message, Throwable cause) {
		super(message, cause);
	}

	public CryptoKeyProblemUnlockException(String message) {
		super(message);
	}

	public CryptoKeyProblemUnlockException(Throwable cause) {
		super(cause);
	}
	
}