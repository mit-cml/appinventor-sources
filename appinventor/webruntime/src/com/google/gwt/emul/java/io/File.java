package java.io;

public class File {
  private String path = "";

  public File(File parent, String name) {

  }

  public File(String pathname) {
    path = pathname;
  }

  public String getAbsolutePath() {
    return path;
  }
}
