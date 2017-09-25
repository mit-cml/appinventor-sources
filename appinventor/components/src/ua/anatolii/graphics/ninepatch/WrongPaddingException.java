package ua.anatolii.graphics.ninepatch;

/**
 * Created by Anatolii on 8/28/13.
 */
public class WrongPaddingException extends RuntimeException {
	public WrongPaddingException() {
	}

	public WrongPaddingException(String detailMessage) {
		super(detailMessage);
	}

	public WrongPaddingException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public WrongPaddingException(Throwable throwable) {
		super(throwable);
	}
}
