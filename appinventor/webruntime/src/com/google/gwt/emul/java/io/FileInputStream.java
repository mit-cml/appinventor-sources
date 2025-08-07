package java.io;

import weblib.FileSystemSimulator;

public class FileInputStream extends InputStream {
  private ByteArrayInputStream buffer;
  private String path;

  public FileInputStream(File file) throws FileNotFoundException {
    this.path = file.getAbsolutePath();
    byte[] contents = FileSystemSimulator.getFile(path);
    if (contents == null) {
      throw new FileNotFoundException("File not found: " + path);
    }
    this.buffer = new ByteArrayInputStream(contents);
  }

  @Override
  public int read() throws IOException {
    return this.buffer.read();
  }
}
