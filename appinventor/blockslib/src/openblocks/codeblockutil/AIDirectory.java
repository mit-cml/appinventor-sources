// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.codeblockutil;

import openblocks.yacodeblocks.FeedbackReporter;
import openblocks.yacodeblocks.WorkspaceControllerHolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

/**
 * This finds the fully qualified (with directory) names for invoking
 * the commands in the App Inventor Setup package, for the host OS.
 * It detects whether the host system is Mac, Windows, or *Nix
 * based on //www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyname
 *
 * For Windows, it queries the client registry to find the location of
 * Appinventor Setup. It queries the user if its heuristics for finding the
 * directory fails.It also caches the most recent successful location.
 *
 * @author halabelson@google.com (Hal Abelson)
 * @author sharon@google.com (Sharon Perl)
 *
 */

public final class AIDirectory {

  private static final boolean DEBUG = false;

  private static final String CURRENT_AI_SETUP_VERSION = "1.1";

  private static final String WINDOWS_DIRECTORY =
      "C:\\Program Files\\Appinventor\\commands-for-Appinventor";

  private static final String MAC_DIRECTORY =
      "/Applications/AppInventor/commands-for-Appinventor";

  private static final String LINUX_DIRECTORY =
      "/usr/google/appinventor/commands-for-Appinventor";

  private static final String KEY_ADB = "adb";
  private static final String KEY_ADBRESTART = "adbrestart";
  private static final String KEY_RUN_EMULATOR = "run-emulator";
  private static final String KEY_KILL_EMULATOR = "kill-emulator";
  private static final String KEY_GETVERSION = "getversion";
  private static final String KEY_UNLOCK_EMULATOR = "unlock-emulator";

  // Map from the generic name of the command to the particular
  // command name for each OS.
  private static final Map<String, String> windowsCommandNames = new HashMap<String, String>() {{
    put(KEY_ADB, "adb.exe");
    put(KEY_ADBRESTART, "adbrestart.bat");
    put(KEY_RUN_EMULATOR, "run-emulator.bat");
    put(KEY_KILL_EMULATOR, "kill-emulator.bat");
    put(KEY_GETVERSION, "getversion.bat");
    put(KEY_UNLOCK_EMULATOR, "unlock-emulator-keyboard.bat");
  }};

  private static final Map<String, String> macCommandNames = new HashMap<String, String>() {{
    put(KEY_ADB, "adb");
    put(KEY_ADBRESTART, "adbrestart");
    put(KEY_RUN_EMULATOR, "run-emulator");
    put(KEY_KILL_EMULATOR, "kill-emulator");
    put(KEY_GETVERSION, "getversion");
    put(KEY_UNLOCK_EMULATOR, "unlock-emulator-keyboard");
  }};

  private static final Map<String, String> linuxCommandNames = new HashMap<String, String>() {{
    put(KEY_ADB, "adb");
    put(KEY_ADBRESTART, "adbrestart");
    put(KEY_RUN_EMULATOR, "run-emulator");
    put(KEY_KILL_EMULATOR, "kill-emulator");
    put(KEY_GETVERSION, "getversion");
    put(KEY_UNLOCK_EMULATOR, "unlock-emulator-keyboard");
  }};

  // osCommandMap will be one of {windows,mac,linux}CommandNames
  private static Map<String, String> osCommandMap;
  private static final Map<String, String> runCommands = new HashMap<String, String>();
  private static boolean setupOkay = false;
  private static String aiDirectory;

  // the name of the cache where the directory name will be stored
  private static final String AI_DIRECTORY_CACHE_DIRECTORY = ".appinventor";
  private static final String AI_DIRECTORY_CACHE_FILE_NAME = "appinventordirectorycache";
  private File cacheDirectoryFile = null;  // save this once we compute it

  private static final String GENERIC_SETUP_ERROR_MESSAGE =
    "<p>App Inventor suspects your Setup software is out of date.</p>"
    + "<p> When you press OK, App Inventor will start the Blocks Editor "
    + "but you will not be able to connect to a device until you "
    + "fix the setup.</p>";

  private static final String NO_COMMAND_DIR_MESSAGE =
    "<p> App Inventor was unable to find the App Inventor Setup command directory, "
    + "which contains commands needed to connect to a device.</p>"
    + GENERIC_SETUP_ERROR_MESSAGE;

  private static final String FIRST_TIME_LOCATE_MSG_TAIL =
    "<p>Please locate the command directory on your computer and enter the full path below"
    + ".</p>";

  private static final String LOCATE_RETRY_MSG_PART1 = "The path you entered was: ";

  private static final String LOCATE_RETRY_MSG_PART2 =
    "<p>App Inventor can't find the Setup command directory"
    + " at the path you entered. Please try again with a new path. You can"
    + " also press Cancel to run the Blocks Editor without connecting to a device.</p>";

  private static final String LOCATE_GIVE_UP_MSG =
    "<p>You did not enter a location for the App Inventor Setup command"
    + " directory, and App Inventor was unable to find the directory on its own.</p>";


  // Check that the AI Setup directory exists and that it contains all the
  // required commands.
  public boolean checkSetup() {
    if (!setupOkay) {
      setupOSCommandMap();
      try {
        aiDirectory = getAIDirectory();
      } catch (NoAIDirectoryException e) {
        String message = NO_COMMAND_DIR_MESSAGE;
        if (e.getMessage() != null) {
          message = message.concat("<p>More detail:</p><p>" + e.getMessage() +"</p>");
        }
        FeedbackReporter.showErrorMessage(message, "App Inventor Setup Error");
        return false;
      }
      try {
        runCommands.put(KEY_ADB, getAICommand(KEY_ADB));
        runCommands.put(KEY_ADBRESTART, getAICommand(KEY_ADBRESTART));
        runCommands.put(KEY_RUN_EMULATOR, getAICommand(KEY_RUN_EMULATOR));
        runCommands.put(KEY_KILL_EMULATOR, getAICommand(KEY_KILL_EMULATOR));
        runCommands.put(KEY_GETVERSION, getAICommand(KEY_GETVERSION));
        runCommands.put(KEY_UNLOCK_EMULATOR, getAICommand(KEY_UNLOCK_EMULATOR));
        checkAISetupVersion();
      } catch (NoAICommandException e) {
        String message = e.getMessage() + GENERIC_SETUP_ERROR_MESSAGE;
        FeedbackReporter.showErrorMessage(message, "App Inventor Setup Error");
        return false;
      }

      // initialize the Android Debug Bridge from here since we know
      // that we have the adb command at this point.
      WorkspaceControllerHolder.get().getAndroidController()
        .androidInitializeCommunicationBridge(runCommands.get(KEY_ADB));

      setupOkay = true;
    }
    return true;
  }

  public String getAdbCommand() {
    if (checkSetup()) {
      return runCommands.get(KEY_ADB);
    } else {
      return null;
    }
  }

  public boolean runStartEmulator() {
    if (checkSetup()) {
      getAndRunCommand(KEY_RUN_EMULATOR);
      return true;
    } else {
      return false;
    }
  }

  public boolean restartADB() {
    if (checkSetup()) {
      getAndRunCommand(KEY_ADBRESTART);
      return true;
    } else {
      return false;
    }
  }

  private void setupOSCommandMap() {
    if (isWindows()) {
      osCommandMap = windowsCommandNames;
    } else if (isMac()) {
      osCommandMap = macCommandNames;
    } else {
      // use the *nix command names as a default. If we really can't determine
      // the OS we'll detect and complain about that later.
      osCommandMap = linuxCommandNames;
    }

  }

  // Assumes setupOkay is true!
  private void checkAISetupVersion() throws NoAICommandException {
    String installedVersion = null;
    installedVersion = runCommand(runCommands.get(KEY_GETVERSION), true).trim();
    if (DEBUG) {
      System.out.println("Client version = "
          + installedVersion
          + " ; Codeblocks version = "
          + CURRENT_AI_SETUP_VERSION);
    }
    if (installedVersion.equals(CURRENT_AI_SETUP_VERSION)) {
      // versions match.  OK.
      return;
    } else {
      throw new NoAICommandException(
          "The App Inventor Setup software"
          + "running on this computer appears to be out of date. Reported version is " + installedVersion);
    }
  }


  /**
   * Returns the fully qualified (with directory) name to invoke the given command.
   * Throws a NoAICommandException if it found the directory
   * but there's no command there.
   * Note: expects that aiDirectory has already been set.
   */
  private String getAICommand(String commandName)
    throws NoAICommandException {
    String expectedCommand = osCommandMap.get(commandName);
    File file = new File(aiDirectory, expectedCommand);
    if (file != null) {
      expectedCommand = file.getAbsolutePath();
      if (testCandidate(expectedCommand)) {
        return expectedCommand;
      }
    }
    // We flush the cache here even though the directory path is valid
    // because even though the path is valid, it might be pointing to the
    // wrong place, if the commands are not there.
    flushCachedAIDirectory();
    String message =
      "Appinventor was unable to find the "
      + commandName + " program.\n"
      + "It found the directory\n"
      + aiDirectory + ",\n"
      + "but there was no file\n"
      + expectedCommand + "\n"
      + "there.";
    throw new NoAICommandException(message);
  }

  /**
   * Returns the directory) path to the App Inventor Setup command.  First tries the
   * previously successful cached command.  Then tries to
   * figure out the command (a) on Windows by looking in the registry;
   * (b) on MacOS and Linux by looking in the expected place.  If that
   * fails is asks the user.  If all of this fails, it throws a
   * NoAIDirectoryException.  Successfully found locations are cached and unsuccessful
   * attempt clears the cache.
   *
   * Throws a NoAIDirectory if it can't find the directory command.
   */
  private String getAIDirectory() throws NoAIDirectoryException {
    if (DEBUG) {
      System.out.println("Trying to read AI directory from cache");
    }
    String cached = getCachedAIDirectory();
    if (DEBUG) {
      System.out.println("Got AI Directory from cache: " +  cached);
    }
    if ((!(cached == null)) && (testCandidate(cached))) {
      return cached;
    } else {
      // flush the bad previously cached command
      flushCachedAIDirectory();
      // findCandidate can throw a NoAIDirectory exception, which will be passed along
      String computed = findCandidate();
      // Warning: This code assumes that the location returned by
      // findCandidate has been tested.
      if (DEBUG) {
        System.out.println("Trying to write AI directory to cache: " +  computed);
      }
      cacheAIDirectory(computed);
      return computed;
    }
  }

  // Warning: The code that uses findCandidate assumes that any location it returns
  // has been tested.
  private  String findCandidate() throws NoAIDirectoryException {
    if (DEBUG) {
      System.out.println("OSname: " + System.getProperty("os.name"));
    }
    if (isWindows()) {
      if (testCandidate(WINDOWS_DIRECTORY)) {
        return WINDOWS_DIRECTORY;
      } else {
        // Note that finding on Windows includes asking the user
        // so we don't do it in this procedure, unlike for Mac and Linux
        return findWindowsCandidate();
      }
    } else if (isMac()) {
      if (testCandidate(MAC_DIRECTORY)) {
        return MAC_DIRECTORY;
      } else {
        return getCandidateFromUser(MAC_DIRECTORY);
      }
    } else if (isLinux()) {
      if (testCandidate(LINUX_DIRECTORY)) {
        return LINUX_DIRECTORY;
      } else {
        return getCandidateFromUser(LINUX_DIRECTORY);
      }
    }
    HTMLPane htmlMsg = new HTMLPane(
        "App Inventor is supported for Mac, GNU/Linux, and Windows only.\n"
      + " It does not know how to find the App Inventor software directory for this\n"
      + "computer, which appears to be running the \n"
      + System.getProperty("os.name") + " operating system.  This software is \n"
      + "needed for connecting to a phone or emulator. You can either try\n"
      + "to continue and enter a pathname for the directory, or\n"
      + " you can use the Blocks editor without connecting to a device.");
    Object[] options = {"Enter directory pathname", "Continue without an Android device."};
    int choice = JOptionPane.showOptionDialog(null,
        htmlMsg,
        "Unrecognized operating system",
        JOptionPane.OK_OPTION,
        JOptionPane.ERROR_MESSAGE,
        null,
        options,
        options[0]);
    if (choice == 0) {
      return getCandidateFromUser("**Could not make a guess**");
    } else {
      throw new NoAIDirectoryException(null);
    }
  }


  private  Boolean testCandidate(String candidate) {
    if (DEBUG) {
      System.out.println("Testing candidate AIDirectory: " + candidate);
    }
    Boolean result = new File(candidate).exists();
    if (DEBUG) {
      System.out.println("Test result for candidate AIDirectory: " + candidate + ": " + result);
    }
    return result;
  }

  // Ask the Windows client to look in its registry to find the directory
  // where the Appinventor client software sits. The Windows installer
  // places this under the key HKCU\Software\AppInventor Setup
  // TODO(halabelson): Rather than querying the registry, try looking in Program Files,
  // Program Files (x86), and ProgramW6432.  Use use the System.getenv() method
  // to access the envars in Java.
  // See http://en.wikipedia.org/wiki/Environment_variable#System_path_variables
  private  final String findWindowsCandidate() throws NoAIDirectoryException {
    String candidate = null;
    // "Appinventor Setup" is specified in the installed build file
    // Need to look for all the 4 places since the differences in 32/64 bits
    final String[] registryQuerys = new String[]{
            "reg.exe QUERY \"HKLM\\Software\\Appinventor Setup\" /v PATH",
            "reg.exe QUERY \"HKCU\\Software\\Appinventor Setup\" /v PATH",
            "reg.exe QUERY \"HKLM\\Software\\Wow6432Node\\Appinventor Setup\" /v PATH",
            "reg.exe QUERY \"HKCU\\Software\\Wow6432Node\\Appinventor Setup\" /v PATH",
            };

    for(int i =0; i<registryQuerys.length; i++){
        candidate = lookForWindowsCandiate(registryQuerys[i]);
        // At this point, candidate is the result of reading the registry.  Now we need to test it.
        // Just because we found the directory in the registry, it doesn't mean
        // that the directory was there.
        if (candidate != null && testCandidate(candidate)) {
          return candidate;
        }
    }   
    // getCandidateFromUser will throw a NoAIDirectoryException if it loses
    return getCandidateFromUser(candidate);
  }
  
  private final String lookForWindowsCandiate(String registryQuery) throws NoAIDirectoryException{    
      String candidate;     
      try {
        // The HKCU key might not exist and reg.exe will produce
        // an error.  Possible reasons are that the Appinventor Setup software
        // was never installed, that there's there's a 32 vs. 64 bit incompatibility
        // between the installer and the user machine, or because Windows
        // is just generally painful. So we catch the
        // error and try asking the user where the directory is.
        // true as last argument means that we wait for the process output
        String result = CodeBlocksProcessHelper.execOnWindows(registryQuery, true);
        if (DEBUG) {
          System.out.println("got QUERY result from Windows: " + result);
        }
        // Look for the part of the result after REG_SZ
        // Warning: Windows 7 return two copies of the QUERY result, while Windows XP
        // returns one copy, which is why we pick the last element in the match result rather
        // than, e.g., the 2nd element
        String [] matchResult = result.split("REG_SZ\\S?");
        String directory = matchResult[matchResult.length - 1].trim();
        if (DEBUG) {
          System.out.println("got AI directory from Windows: " + directory);
        }
        candidate = directory;
      } catch (IOException e) {
        System.out.println("Got error trying to read registry: " + e.getMessage());
        candidate = null;
      }
      return candidate;
  }

  // Prints a message and requests the user to enter a location.  The input
  // priorAttempt is the file name the system tried previously on its own, or null
  // if there was no previous attempt.
  private  final String getCandidateFromUser(String priorAttempt)
      throws NoAIDirectoryException {
    String message;
    if (priorAttempt == null) {
      message = FIRST_TIME_LOCATE_MSG_TAIL;
    } else {
      message = "App Inventor did not find the Appinventor Setup commands at path "
        + priorAttempt + ". " + FIRST_TIME_LOCATE_MSG_TAIL;
    }
    String location = null;
    while (true) {
      location = FeedbackReporter.getInput(message);
      // location null means that the user pressed CANCEL
      if (location == null) {
        throw new NoAIDirectoryException(LOCATE_GIVE_UP_MSG);
      }
      if (testCandidate(location)) {
        break;
      }
      message = LOCATE_RETRY_MSG_PART1 + location + LOCATE_RETRY_MSG_PART2;
    }
    if (DEBUG) {
      System.out.println("Read input directory pathname command from user: " + location);
    }
    return location;
  }

  private boolean isWindows() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("win");
  }

  private boolean isMac() {
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("mac");
  }

  private boolean isLinux() {
    // Linux or Unix
    String os = System.getProperty("os.name").toLowerCase();
    return os.contains("nix") || os.contains("nux");
  }

  // TODO(halabelson): Put the cache in a place won't get deleted
  // as easily as /tmp

  private void flushCachedAIDirectory(){
    File cache = aiDirectoryCacheFile();
    if (cache.exists()) {
      cache.delete();
    }
  }

  private void cacheAIDirectory(String aiDirectory){
    if (DEBUG) {
      System.out.println("Trying to cache AI directory command: " +  aiDirectory);
    }
    File cache = aiDirectoryCacheFile();
    try {
      writeStringToFile(cache, aiDirectory);
      if (DEBUG) {
        System.out.println("AI Directory written to cache: " +  aiDirectory);
      }
    } catch (IOException e) {
      System.out.println("Could not write AI directory cache file.  Proceed without caching." +
          "Error was: " + e.getMessage());
    }
     }

  private String getCachedAIDirectory(){
    File cache = aiDirectoryCacheFile();
    if (!(cache.exists())) {
      return null;
    } else {
      try {
        String command = readLineFromFile(cache);
        if (DEBUG) {
          System.out.println("AI Directory read from cache: " +  command);
        }
        return command;
      } catch (IOException e) {
        System.out.println("Could not read from AI directory cache file.  "
            + "Proceed without caching."
            + "Error was: " + e.getMessage());
        return null;
      }
    }
  }

  private  void writeStringToFile(File f, String s) throws IOException {
    OutputStream out = new FileOutputStream(f);
    PrintWriter p = new PrintWriter(out);
    if (DEBUG) {
     System.out.println("WriteStringToFile: writing " + s);
    }
    p.print(s);
    p.flush();
    p.close();

    if (DEBUG) {
      System.out.println("WriteStringToFile: wrote " + s);
     }
  }

  private  String readLineFromFile(File f) throws IOException {
  BufferedReader in = new BufferedReader(new FileReader(f));
  return in.readLine();
}

  private  File aiDirectoryCacheFile()  {
    if (cacheDirectoryFile == null) {
      File dir = new File(System.getProperty("user.home"), AI_DIRECTORY_CACHE_DIRECTORY);
      if (!dir.exists()) {
        dir.mkdir();
      }
      cacheDirectoryFile = new File(dir, AI_DIRECTORY_CACHE_FILE_NAME);
    }
    return cacheDirectoryFile;
  }

  // Run the command and retry if it fails. If the retry fails, report the
  // error to the user.
  // Assumes setupOkay = true!
  private void getAndRunCommand(String command) {
    // osCommand is command specialized to the current OS
      String osCommand = null;
      if (runCommands.containsKey(command)) {
        osCommand = runCommands.get(command);
      } else {
        FeedbackReporter.showSystemErrorMessage("Trying to run unknown command: " + command);
      }
      try {
        runCommand(osCommand, false);
      } catch (NoAICommandException e) {
        FeedbackReporter.showErrorMessage(e.getMessage(), "Error running command");
      }
    }

  private String runCommand(String fullCommand, boolean waitForOutput) throws NoAICommandException {
    try {
      if (DEBUG) {
        System.out.println("Trying to run command: " + fullCommand);
      }
      return runCommandOnce(fullCommand, waitForOutput);
    } catch (IOException e0) {
      System.out.println("Running the command " + fullCommand + " failed.\n"
          + "The error message was: \n"
          + e0.getMessage()
          + "\n ... Retrying ...");
      try {
        return runCommandOnce(fullCommand, waitForOutput);
      } catch (IOException e) {
        String message =
          "<p>App Inventor got an error trying to run the command\n"
          + fullCommand + "\n"
          + "on your computer.  You can press OK to try to continue, but this is\n"
          + "unlikely to succeed.</p>"
          + "<p>The error message was: \n"
          + e.getMessage() + "</p>";
        throw new NoAICommandException(message);
      }
    }
  }

  private String runCommandOnce(String command, boolean waitForOutput) throws IOException {
    if (isWindows()) {
      return CodeBlocksProcessHelper.execOnWindows(command, waitForOutput);
    } else {
      return CodeBlocksProcessHelper.exec(new String[] {command}, waitForOutput);
    }
  }
}