package com.google.appinventor.buildserver.tasks;

import com.google.appinventor.buildserver.*;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


@BuildType(aab = true)
public class RunBundletool implements Task {
  private AabPaths aab;

  @Override
  public TaskResult execute(CompilerContext context) {
    this.aab = new AabPaths();

    context.getReporter().info("Creating structure");
    aab.setROOT(ExecutorUtils.createDir(context.getProject().getBuildDirectory(), "aab"));
    aab.setProtoApk(context.getPaths().getTmpPackageName());
    if (!createStructure(context)) {
      return TaskResult.generateError("Could not create AAB structure");
    }

    context.getReporter().info("Extracting protobuf resources");
    if (!extractProtobuf(context)) {
      return TaskResult.generateError("Could not extract protobuf");
    }

    context.getReporter().info("Running bundletool");
    if (!bundletool(context)) {
      return TaskResult.generateError("Could not run bundletool");
    }

    context.getReporter().info("Signing bundle");
    if (!jarsigner(context)) {
      return TaskResult.generateError("Could not sign bundle");
    }
    return TaskResult.generateSuccess();
  }

  private boolean createStructure(CompilerContext context) {
    // Manifest is extracted from the protobuffed APK
    aab.setManifestDir(ExecutorUtils.createDir(aab.getROOT(), "manifest"));

    // Resources are extracted from the protobuffed APK
    aab.setResDir(ExecutorUtils.createDir(aab.getROOT(), "res"));

    // Assets are extracted from the protobuffed APK
    aab.setAssetsDir(ExecutorUtils.createDir(aab.getROOT(), "assets"));

    aab.setDexDir(ExecutorUtils.createDir(aab.getROOT(), "dex"));
    context.getReporter().log("Moving dex files");
    File[] dexFiles = context.getPaths().getTmpDir().listFiles();
    if (dexFiles != null) {
      for (File dex : dexFiles) {
        if (dex.isFile()) {
          try {
            Files.move(dex, new File(aab.getDexDir(), dex.getName()));
          } catch (IOException e) {
            context.getReporter().error(e.getMessage(), true);
            return false;
          }
        }
      }
    }

    aab.setLibDir(ExecutorUtils.createDir(aab.getROOT(), "lib"));
    context.getReporter().log("Moving lib files");
    File[] libFiles = context.getPaths().getLibsDir().listFiles();
    if (libFiles != null) {
      for (File lib : libFiles) {
        try {
          Files.move(lib, new File(ExecutorUtils.createDir(aab.getROOT(), "lib"), lib.getName()));
        } catch (IOException e) {
          context.getReporter().error(e.getMessage(), true);
          return false;
        }
      }
    }

    return true;
  }

  private boolean extractProtobuf(CompilerContext context) {
    try (ZipInputStream is = new ZipInputStream(new FileInputStream(aab.getProtoApk()))) {
      ZipEntry entry;
      byte[] buffer = new byte[1024];
      while ((entry = is.getNextEntry()) != null) {
        String n = entry.getName();
        File f = null;
        if (n.equals("AndroidManifest.xml")) {
          context.getReporter().log("Found AndroidManifest.xml");
          f = new File(aab.getManifestDir(), n);
        } else if (n.equals("resources.pb")) {
          context.getReporter().log("Found resources.pb");
          f = new File(aab.getROOT(), n);
        } else if (n.startsWith("assets")) {
          f = new File(aab.getAssetsDir(), n.substring(("assets").length()));
        } else if (n.startsWith("res")) {
          f = new File(aab.getResDir(), n.substring(("res").length()));
        }

        if (f != null) {
          f.getParentFile().mkdirs();
          FileOutputStream fos = new FileOutputStream(f);
          int len;
          while ((len = is.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
          fos.close();
        }
      }

      is.close();
      return true;
    } catch (IOException e) {
      context.getReporter().error(e.getMessage(), true);
    }
    return false;
  }

  private boolean bundletool(CompilerContext context) {
    aab.setBASE(new File(context.getProject().getBuildDirectory(), "base.zip"));

    if (!AabZipper.zipBundle(aab.getROOT(), aab.getBASE(), aab.getROOT().getName() + File.separator)) {
      context.getReporter().error("Could not zip files for the bundle", true);
      return false;
    }

    String bundletool = context.getResources().bundletool();
    if (bundletool == null) {
      context.getReporter().error("Bundletool jar file was not found", true);
      return false;
    }

    List<String> bundletoolCommandLine = new ArrayList<String>();
    bundletoolCommandLine.add(System.getProperty("java.home") + "/bin/java");
    bundletoolCommandLine.add("-jar");
    bundletoolCommandLine.add("-mx" + context.getChildProcessRam() + "M");
    bundletoolCommandLine.add(bundletool);
    bundletoolCommandLine.add("build-bundle");
    bundletoolCommandLine.add("--modules=" + aab.getBASE());
    bundletoolCommandLine.add("--output=" + context.getPaths().getDeployFile().getAbsolutePath());
    String[] bundletoolBuildCommandLine = bundletoolCommandLine.toArray(new String[0]);

    return Execution.execute(null, bundletoolBuildCommandLine, context.getReporter().getSystemOut(), System.err);
  }

  private boolean jarsigner(CompilerContext context) {
    List<String> jarsignerCommandLine = new ArrayList<String>();

    String jarsigner = context.getResources().jarsigner();
    if (jarsigner == null) {
      context.getReporter().error("Jarsigner executable file was not found", true);
      return false;
    }

    jarsignerCommandLine.add(jarsigner);
    jarsignerCommandLine.add("-sigalg");
    jarsignerCommandLine.add("SHA256withRSA");
    jarsignerCommandLine.add("-digestalg");
    jarsignerCommandLine.add("SHA-256");
    jarsignerCommandLine.add("-keystore");
    jarsignerCommandLine.add(context.getKeystoreFilePath());
    jarsignerCommandLine.add("-storepass");
    jarsignerCommandLine.add("android");
    jarsignerCommandLine.add(context.getPaths().getDeployFile().getAbsolutePath());
    jarsignerCommandLine.add("AndroidKey");
    String[] jarsignerSignCommandLine = jarsignerCommandLine.toArray(new String[0]);

    return Execution.execute(null, jarsignerSignCommandLine, context.getReporter().getSystemOut(), System.err);
  }
}


class AabPaths {
  private File ROOT = null;
  private File BASE = null;
  private File protoApk = null;

  private File assetsDir = null;
  private File dexDir = null;
  private File libDir = null;
  private File manifestDir = null;
  private File resDir = null;

  public File getROOT() {
    return ROOT;
  }

  public void setROOT(File ROOT) {
    this.ROOT = ROOT;
  }

  public File getBASE() {
    return BASE;
  }

  public void setBASE(File BASE) {
    this.BASE = BASE;
  }

  public File getProtoApk() {
    return protoApk;
  }

  public void setProtoApk(File protoApk) {
    this.protoApk = protoApk;
  }

  public File getAssetsDir() {
    return assetsDir;
  }

  public void setAssetsDir(File assetsDir) {
    this.assetsDir = assetsDir;
  }

  public File getDexDir() {
    return dexDir;
  }

  public void setDexDir(File dexDir) {
    this.dexDir = dexDir;
  }

  public File getLibDir() {
    return libDir;
  }

  public void setLibDir(File libDir) {
    this.libDir = libDir;
  }

  public File getManifestDir() {
    return manifestDir;
  }

  public void setManifestDir(File manifestDir) {
    this.manifestDir = manifestDir;
  }

  public File getResDir() {
    return resDir;
  }

  public void setResDir(File resDir) {
    this.resDir = resDir;
  }
}

class AabZipper {
  public static boolean zipBundle(File src, File dest, String root) {
    try {
      FileOutputStream fos = new FileOutputStream(dest);
      ZipOutputStream zipOut = new ZipOutputStream(fos);

      zipFile(src, src.getName(), zipOut, root);
      zipOut.close();
      fos.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, String root) throws IOException {
    if (fileToZip.isHidden()) {
      return;
    }
    String zipFileName = fileName;
    if (zipFileName.startsWith(root)) {
      zipFileName = zipFileName.substring(root.length());
    }

    boolean windows = !File.separator.equals("/");
    if (windows) {
      zipFileName = zipFileName.replace(File.separator, "/");
    }

    if (fileToZip.isDirectory()) {
      if (zipFileName.endsWith("/")) {
        zipOut.putNextEntry(new ZipEntry(zipFileName));
      } else {
        zipOut.putNextEntry(new ZipEntry(zipFileName + "/"));
      }
      zipOut.closeEntry();
      File[] children = fileToZip.listFiles();
      assert children != null;
      for (File childFile : children) {
        zipFile(childFile, fileName + File.separator + childFile.getName(), zipOut, root);
      }
      return;
    }

    FileInputStream fis = new FileInputStream(fileToZip);
    ZipEntry zipEntry = new ZipEntry(zipFileName);
    zipOut.putNextEntry(zipEntry);
    byte[] bytes = new byte[1024];
    int length;
    while ((length = fis.read(bytes)) >= 0) {
      zipOut.write(bytes, 0, length);
    }
    fis.close();
  }
}
