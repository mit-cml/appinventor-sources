// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
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
