package com.google.appinventor.buildserver.tasks;

import com.google.appinventor.buildserver.BuildType;
import com.google.appinventor.buildserver.CompilerContext;
import com.google.appinventor.buildserver.Execution;
import com.google.appinventor.buildserver.ExecutorUtils;
import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.interfaces.Task;

import com.google.appinventor.buildserver.util.AabPaths;
import com.google.appinventor.buildserver.util.AabZipper;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@BuildType(aab = true)
public class RunBundletool implements Task {
  private AabPaths aab;

  // These regexes were taken from a project compiled via Android Studio
  private static final String[] NONCOMPRESSIBLE_EXTS = new String[] {
      "**.3[gG]2",
      "**.3[gG][pP]",
      "**.3[gG][pP][pP]",
      "**.3[gG][pP][pP]2",
      "**.[aA][aA][cC]",
      "**.[aA][mM][rR]",
      "**.[aA][wW][bB]",
      "**.[gG][iI][fF]",
      "**.[iI][mM][yY]",
      "**.[jJ][eE][tT]",
      "**.[jJ][pP][eE][gG]",
      "**.[jJ][pP][gG]",
      "**.[mM]4[aA]",
      "**.[mM]4[vV]",
      "**.[mM][iI][dD]",
      "**.[mM][iI][dD][iI]",
      "**.[mM][kK][vV]",
      "**.[mM][pP]2",
      "**.[mM][pP]3",
      "**.[mM][pP]4",
      "**.[mM][pP][eE][gG]",
      "**.[mM][pP][gG]",
      "**.[oO][gG][gG]",
      "**.[oO][pP][uU][sS]",
      "**.[pP][nN][gG]",
      "**.[rR][tT][tT][tT][lL]",
      "**.[sS][mM][fF]",
      "**.[tT][fF][lL][iI][tT][eE]",
      "**.[wW][aA][vV]",
      "**.[wW][eE][bB][mM]",
      "**.[wW][eE][bB][pP]",
      "**.[wW][mM][aA]",
      "**.[wW][mM][vV]",
      "**.[xX][mM][fF]"
  };

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
    // Create the bundle configuration
    File configFile;
    try {
      configFile = File.createTempFile("BundleConfig", ".pb.json");
    } catch (IOException e) {
      throw new RuntimeException("Unable to generate bundle config", e);
    }
    try (FileOutputStream out = new FileOutputStream(configFile)) {
      JSONObject config = new JSONObject();
      JSONObject compression = new JSONObject();
      JSONArray uncompressedGlob = new JSONArray();
      config.put("compression", compression);
      compression.put("uncompressedGlob", uncompressedGlob);
      for (String ext : NONCOMPRESSIBLE_EXTS) {
        uncompressedGlob.put(ext);
      }
      out.write(config.toString().getBytes(StandardCharsets.UTF_8));
    } catch (JSONException | IOException e) {
      throw new RuntimeException("Unable to generate AAB", e);
    }

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
    bundletoolCommandLine.add("--config=" + configFile.getAbsolutePath());
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
