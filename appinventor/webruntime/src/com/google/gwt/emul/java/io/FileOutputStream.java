package java.io;

import javax.annotation.Nonnull;
import weblib.FileSystemSimulator;

public class FileOutputStream extends OutputStream {
  private final ByteArrayOutputStream buffer;
  private final String path;

  public FileOutputStream(File file) {
    this.path = file.getAbsolutePath();
    this.buffer = new ByteArrayOutputStream();
  }

  @Override
  public void write(@Nonnull byte[] b, int off, int len) {
    buffer.write(b, off, len);
  }

  @Override
  public void write(int b) throws IOException {
    buffer.write(b);
  }

  @Override
  public void write(@Nonnull byte[] b) {
    write(b, 0, b.length);
  }

  public void flush() {
  }

  public void close() {
    FileSystemSimulator.storeFile(path, buffer.toByteArray());
  }
}
