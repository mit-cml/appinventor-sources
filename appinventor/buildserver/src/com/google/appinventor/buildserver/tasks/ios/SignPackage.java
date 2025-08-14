// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.tasks.ios;

import com.google.appinventor.buildserver.TaskResult;
import com.google.appinventor.buildserver.context.IosCompilerContext;
import com.google.appinventor.buildserver.interfaces.BuildType;
import com.google.appinventor.buildserver.interfaces.IosTask;
import com.google.appinventor.buildserver.util.Execution;
import com.google.common.annotations.VisibleForTesting;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@BuildType(ipa = true, asc = true)
public class SignPackage implements IosTask {
  private static final Object LOCK = new Object();
  private static final String SECURITY = "/usr/bin/security";
  private static final String CODESIGN = "/usr/bin/codesign";
  private static final String FORCE = "--force";
  private static final String SIGN = "--sign";
  private static final String VERBOSE = "--verbose";
  private static final Map<String, String> ENV = new HashMap<>();

  static {
    ENV.put("CODESIGN_ALLOCATE", "/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin/codesign_allocate");
    ENV.put("DEVELOPER_DIR", "/Applications/Xcode.app/Contents/Developer");
    ENV.put("SDKROOT", "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk");
  }

  private String certificateSha1;
  private String keychainName;

  @Override
  public TaskResult execute(IosCompilerContext context) {
    try {
      certificateSha1 = createAndRegisterKeychain(context);
    } catch (Exception e) {
      certificateSha1 = null;
      e.printStackTrace();
      return TaskResult.generateError(e);
    }
    TaskResult result = TaskResult.generateSuccess();
    try {
      File[] frameworks = context.getPaths().getFrameworkDir()
          .listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
              return pathname.isDirectory()
                  && pathname.getName().endsWith(".framework");
            }
          });
      if (frameworks != null) {
        for (File framework : frameworks) {
          if (!(result = signFramework(context, framework)).isSuccess()) {
            return result;
          }
        }
      }
      File[] dylibs = context.getPaths().getFrameworkDir()
          .listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
              return pathname.getName().endsWith(".dylib");
            }
          });
      if (dylibs != null) {
        for (File dylib : dylibs) {
          if (!(result = signSwiftLib(context, dylib)).isSuccess()) {
            return result;
          }
        }
      }
      return signApp(context, context.getPaths().getAppDir());
    } finally {
      certificateSha1 = null;
      unregisterAndDeleteKeychain(context);
    }
  }

  @VisibleForTesting
  String createAndRegisterKeychain(IosCompilerContext context) throws Exception {
    String sha1 = context.getCertificateSignature();
    if (sha1 == null) {
      throw new FileNotFoundException("No valid mobileprovision profile included in project");
    }
    synchronized (LOCK) {
      createKeychain(context, sha1);
      populateKeychain(context, context.getPaths().getAndroidKeystore(), context.getCertificate());
      registerKeychain(context);
    }
    return sha1;
  }

  private void createKeychain(IosCompilerContext context, String sha1) throws IOException {
    keychainName = new File(context.getPaths().getTmpDir(), sha1 + ".keychain")
        .getAbsolutePath();
    String[] args = {
        SECURITY,
        "create-keychain",
        "-p", "appinventor",
        keychainName
    };
    if (!Execution.execute(context, args)) {
      throw new IOException("Unable to create iOS signing keychain");
    }
  }

  private void populateKeychain(IosCompilerContext context, File keystoreFile,
      String certificatePem) throws Exception {
    // Create .p12
    File pkcs12 = File.createTempFile("pkcs", ".p12", context.getPaths().getTmpDir());
    try (FileInputStream in = new FileInputStream(keystoreFile)) {
      KeyStore keystore = KeyStore.getInstance("JKS");
      keystore.load(in, "android".toCharArray());
      Key key = keystore.getKey("androidkey", "android".toCharArray());

      CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
      Certificate cert = certFactory.generateCertificate(new ByteArrayInputStream(certificatePem.getBytes()));

      KeyStore outKeyStore = KeyStore.getInstance("PKCS12");
      outKeyStore.load(null, "appinventor".toCharArray());
      outKeyStore.setKeyEntry("appinventor", key, "appinventor".toCharArray(), new Certificate[] { cert });

      try (FileOutputStream out = new FileOutputStream(pkcs12)) {
        outKeyStore.store(out, "appinventor".toCharArray());
      }
    }

    // Import .p12 into Keychain
    String[] args = {
        SECURITY,
        "import",
        pkcs12.getAbsolutePath(),
        "-k",
        keychainName,
        "-t",
        "agg",
        "-f",
        "pkcs12",
        "-P",
        "appinventor",
        "-T",
        CODESIGN
    };
    Execution.execute(context, args);
    if (!pkcs12.delete()) {
      throw new IOException("Unable to delete pkcs12 file");
    }
  }

  private void registerKeychain(IosCompilerContext context) throws IOException {
    // Get list of currently registered keychains
    String[] args = {
        SECURITY,
        "list-keychains",
        "-d",
        "user"
    };
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    Execution.execute(context.getPaths().getBuildDir(), args, new PrintStream(out),
        new PrintStream(err));
    String[] existingKeychains = out.toString().split("\n");
    for (int i = 0; i < existingKeychains.length; i++) {
      int left = existingKeychains[i].indexOf("\"");
      int right = existingKeychains[i].lastIndexOf("\"");
      existingKeychains[i] = existingKeychains[i].substring(left + 1, right);
    }

    // Register the new keychain
    List<String> setargs = new ArrayList<>();
    setargs.add(SECURITY);
    setargs.add("list-keychains");
    setargs.add("-d");
    setargs.add("user");
    setargs.add("-s");
    setargs.add(keychainName);
    Collections.addAll(setargs, existingKeychains);
    Execution.execute(context, setargs.toArray(new String[0]));
  }

  @VisibleForTesting
  void unregisterAndDeleteKeychain(IosCompilerContext context) {
    String[] args = {
        SECURITY,
        "delete-keychain",
        keychainName
    };
    synchronized (LOCK) {
      Execution.execute(context, args);
    }
  }

  private TaskResult signFramework(IosCompilerContext context, File framework) {
    final String name = framework.getName();
    String[] args = new String[] {
        CODESIGN,
        FORCE,
        SIGN,
        certificateSha1,
        "--timestamp=none",
        (name.contains("AIComponentKit") || name.contains("SchemeKit")) ?
        "--preserve-metadata=identifier,entitlements,flags" :
        "--preserve-metadata=identifier,entitlements",
        framework.getAbsolutePath()
    };
    if (Execution.execute(context, args, ENV)) {
      return TaskResult.generateSuccess();
    } else {
      return TaskResult.generateError("Failed to sign framework " + framework);
    }
  }

  private TaskResult signSwiftLib(IosCompilerContext context, File lib) {
    /*
    /usr/bin/codesign --force --sign 4DAFB33258A6729C9E40BC86CAC9855303594A96 --verbose /Users/ewpatton/Programming/mit/app-inventor-private-ios/appinventor/components-ios/build/App Inventor/Build/Intermediates.noindex/ArchiveIntermediates/AICompanionApp/InstallationBuildProductsLocation/Applications/AICompanionApp.app/Frameworks/libswiftUIKit.dylib
     */
    String[] args = new String[] {
        CODESIGN,
        FORCE,
        SIGN,
        certificateSha1,
        "--generate-entitlement-der",
        "--preserve-metadata=identifier,flags,runtime",
        VERBOSE,
        lib.getAbsolutePath()
    };
    if (Execution.execute(context, args, ENV)) {
      return TaskResult.generateSuccess();
    } else {
      return TaskResult.generateError("Failed to sign library " + lib);
    }
  }

  private TaskResult signApp(IosCompilerContext context, File appDir) {
    /*
    /usr/bin/codesign --force --sign 4DAFB33258A6729C9E40BC86CAC9855303594A96
        --entitlements /Users/ewpatton/Programming/mit/app-inventor-private-ios/appinventor/components-ios/build/App\ Inventor/Build/Intermediates.noindex/ArchiveIntermediates/AICompanionApp/IntermediateBuildFilesPath/AICompanionApp.build/Release-iphoneos/AICompanionApp.build/AICompanionApp.app.xcent
        /Users/ewpatton/Programming/mit/app-inventor-private-ios/appinventor/components-ios/build/App\ Inventor/Build/Intermediates.noindex/ArchiveIntermediates/AICompanionApp/InstallationBuildProductsLocation/Applications/AICompanionApp.app
     */
    String[] args = new String[] {
        CODESIGN,
        FORCE,
        SIGN,
        certificateSha1,
        "--timestamp=none",
        "--entitlements",
        context.getEntitlements().getAbsolutePath(),
        appDir.getAbsolutePath()
    };
    if (Execution.execute(context, args, ENV)) {
      return TaskResult.generateSuccess();
    } else {
      return TaskResult.generateError("Failed to sign app " + appDir);
    }
  }
}
