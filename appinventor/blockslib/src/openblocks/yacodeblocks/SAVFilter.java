package openblocks.yacodeblocks;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class SAVFilter extends FileFilter {
  @Override
  public boolean accept(File file) {
    if(file != null){
      String filename = file.getName();
      if (filename != null){
        return filename.endsWith(".sav");
      }
    }
    return false;
  }
  @Override
  public String getDescription() {
    return "*.sav";
  }
}
