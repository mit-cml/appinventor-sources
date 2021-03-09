package com.google.appinventor.buildserver.tasks;

import com.google.appinventor.buildserver.*;
import com.google.appinventor.buildserver.Compiler;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import java.io.File;
import java.io.IOException;


/**
 * libSetup()
 */
@BuildType(apk = true, aab = true)
public class SetupLibs implements Task {
  @Override
  public TaskResult execute(CompilerContext context) {
    String osName = System.getProperty("os.name");
    if (osName.equals("Linux")) {
      ensureLib("/tmp/lib64", "libc++.so", "/tools/linux/lib64/libc++.so");
    } else if (osName.startsWith("Windows")) {
      ensureLib(System.getProperty("java.io.tmpdir"), "libwinpthread-1.dll", "/tools/windows/libwinpthread-1.dll");
    }
    return TaskResult.generateSuccess();
  }

  private void ensureLib(String tempdir, String name, String resource) {
    try {
      File outFile = new File(tempdir, name);
      if (outFile.exists()) {
        return;
      }
      File tmpLibDir = new File(tempdir);
      tmpLibDir.mkdirs();
      Files.copy(Resources.newInputStreamSupplier(Compiler.class.getResource(resource)), outFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
