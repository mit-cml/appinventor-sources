package ua.anatolii.graphics.ninepatch;

/**
 * Created by Anatolii on 8/28/13.
 */
public class ChunkNotSerializedException extends RuntimeException{
	public ChunkNotSerializedException() {
	}

	public ChunkNotSerializedException(String detailMessage) {
		super(detailMessage);
	}

	public ChunkNotSerializedException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ChunkNotSerializedException(Throwable throwable) {
		super(throwable);
	}
}
