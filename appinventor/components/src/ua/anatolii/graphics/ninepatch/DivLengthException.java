package ua.anatolii.graphics.ninepatch;

/**
 * Created by Anatolii on 8/28/13.
 */
public class DivLengthException extends RuntimeException {
	public DivLengthException() {
	}

	public DivLengthException(String detailMessage) {
		super(detailMessage);
	}

	public DivLengthException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public DivLengthException(Throwable throwable) {
		super(throwable);
	}
}
