// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

/**
 * Main entry point for the command line version of the YAIL compiler.
 *
 * @author markf@google.com (Mark Friedman)
 */
public final class Main {

  static class CommandLineOptions {
    @Option(name = "--isForCompanion", usage = "create the MIT AI2 Companion APK")
    boolean isForCompanion = false;

    @Option(name = "--inputZipFile", required = true,
            usage = "the ZIP file of the project to build")
    File inputZipFile;

    @Option(name = "--userName", required = true,
            usage = "the name of the user building the project")
    String userName;

    @Option(name = "--outputDir", required = true,
            usage = "the directory in which to put the output of the build")
    File outputDir;

    @Option(name = "--childProcessRamMb",
            usage = "Maximum ram that can be used by a child processes, in MB.")
    int childProcessRamMb = 2048;

    @Option(name = "--dexCacheDir",
            usage = "the directory to cache the pre-dexed libraries")
    String dexCacheDir = null;
  }

  private static CommandLineOptions commandLineOptions = new CommandLineOptions();

  // Logging support
  private static final Logger LOG = Logger.getLogger(Main.class.getName());

  private Main() {
  }

  /**
   * Main entry point.
   *
   * @param args  command line arguments
   */
  public static void main(String[] args) {

    CmdLineParser cmdLineParser = new CmdLineParser(commandLineOptions);
    try {
      cmdLineParser.parseArgument(args);
    } catch (CmdLineException e) {
      System.err.println(e.getMessage());
      cmdLineParser.printUsage(System.err);
      System.exit(1);
    }

    ProjectBuilder projectBuilder = new ProjectBuilder();
    ZipFile zip = null;
    try {
      zip = new ZipFile(commandLineOptions.inputZipFile);
    } catch (IOException e) {
      LOG.severe("Problem opening inout zip file: " + commandLineOptions.inputZipFile.getName());
      System.exit(1);
    }
    Result result = projectBuilder.build(commandLineOptions.userName,
                                         zip,
                                         commandLineOptions.outputDir,
                                         commandLineOptions.isForCompanion,
                                         commandLineOptions.childProcessRamMb,
                                         commandLineOptions.dexCacheDir);
    System.exit(result.getResult());
  }

  // COV_NF_END
}
