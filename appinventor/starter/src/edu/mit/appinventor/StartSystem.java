package edu.mit.appinventor;

import org.ini4j.Wini;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.keyczar.KeyczarTool;

public class StartSystem {

    private static String storage = null;

    public static void main(String [] argv) {
      File execDir = new File(new File(StartSystem.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent());
      Process s = null;
      Process build = null;
      String mailhost = null;
      String mailuser = null;
      String mailpassword = null;
      String mailfrom = null;
      boolean useStartTls = false;

      // See if we have the "makeauthkey" argument
      if (argv.length > 0 && argv[0].equals("makeauthkey")) {
          System.out.println("Building the AuthKey");
          makeAuthKey();
          System.exit(0);
      } else {
          System.out.println("Normal Startup");
          if (!verifyAuthKey()) {
              System.err.println("authkey is missing, use \"makeauthkey\" argument to create it.");
              System.exit(1);
          }
      }

      List<String> pArgs = new ArrayList<String>();
      pArgs.add("java");

      ConfigBuilder config = new ConfigBuilder();
      Map<String,String> other = null;

      try {
          Wini parser = new Wini(new File("appinventor.ini"));
          storage = parser.get("main", "storage");
          mailhost = parser.get("mail", "host");
          mailuser = parser.get("mail", "user");
          mailfrom = parser.get("mail", "mailfrom");
          mailpassword = parser.get("mail", "password");
          String stls = parser.get("mail", "starttls");
          String firebaseURL = parser.get("main", "firebaseurl");
          String firebaseSecret = parser.get("main", "firebasesecret");
          if (firebaseURL != null && firebaseSecret != null) {
              config.add("firebase.url", firebaseURL);
              config.add("firebase.secret", firebaseSecret);
          }
          if ((stls != null) && (stls.equals("true"))) {
              config.add("mail.smtp.starttls.enable", "true");
          }
          String smtpport = parser.get("mail", "port");
          if (smtpport != null) {
              config.add("mail.smtp.port", smtpport);
          }
          String keystore = parser.get("mail", "keystore");
          if (keystore != null) {
              config.add("javax.net.ssl.trustStore", keystore);
          }
          other = parser.get("other"); // Fetch the "other" section
      } catch (IOException e) {
          System.err.println("Missing appinventor.ini file. Please create from sample");
          System.exit(1);
      }

      // Parse the "other" section. We iterate through all of the keys

      Set<String> keys = other.keySet();
      for (String key : keys) {
          String value = other.get(key);
          config.add(key, value);
      }

      if (argv.length < 1) {
          if (storage == null) {
              System.err.println("Usage: java -jar starter.jar <path-to-root-storage>");
              System.exit(1);
          }
      } else {
          storage = argv[0];    // Command line overrides
      }

      config.add("storage.root", storage);

      if (mailhost != null) {
          config.add("mail.smtp.host", mailhost);
      }
      if (mailuser != null) {
          config.add("mail.smtp.user", mailuser);
      }
      if (mailfrom != null && !mailfrom.equals("")) {
          config.add("mail.smtp.mailfrom", mailfrom);
      }
      if (mailpassword != null) {
          config.add("mail.smtp.password", mailpassword);
      }
      config.add("jetty.base", "..");
      config.save();
      pArgs.add("-XX:+UseG1GC");
      pArgs.add("-Djetty.base=..");
      pArgs.add("-jar");
      pArgs.add("../jetty-home/start.jar");

      ProcessBuilder server = new ProcessBuilder(pArgs);
      server.inheritIO();
      server.directory(execDir);

      File buildserverLibs = new File(execDir.getPath() + "/../buildserver");
      File [] fileList = buildserverLibs.listFiles();
      if (fileList == null) {
          System.err.println("Could not find buildserver libraries.");
          System.exit(1);
      }
      String cp = "";
      boolean first = true;
      for (File file: fileList) {
          if (file.isFile()) {
              if (first) {
                  cp += file.getPath();
                  first = false;
              } else {
                  cp += File.pathSeparator + file.getPath();
              }
          }
      }

      String maxMem = "-Xmx1828m";
      ProcessBuilder buildserver = null;
      if (System.getProperty("os.name").startsWith("Windows")) {
          maxMem = "-Xmx1024m";
          buildserver = new ProcessBuilder("java", maxMem, "-cp", cp,
            "com.google.appinventor.buildserver.BuildServer","--dexCacheDir",
            "/tmp/dxcache", "--childProcessRamMb", "1024");
      } else {
          buildserver = new ProcessBuilder("java", maxMem, "-cp", cp,
            "com.google.appinventor.buildserver.BuildServer","--dexCacheDir",
            "/tmp/dxcache");
      }
      buildserver.inheritIO();
      buildserver.directory(execDir);

      try {
          s = server.start();
          build = buildserver.start();
      } catch (IOException e) {
          System.exit(1);
      }
      try {
          s.waitFor();
          build.waitFor();
      } catch (InterruptedException e) {
      }
    }

    private static boolean verifyAuthKey() {
      File meta = new File("authkey/meta");
      if (meta.isFile()) {
          return true;
      } else {
          return false;
      }
    }

    private static void makeAuthKey() {
      if (verifyAuthKey()) {
          System.err.println("authkey is present, will not over-write.");
          System.exit(1);
      }
      File authkeydir = new File("authkey");
      authkeydir.mkdirs();
      runKeyCzar("create", "--location=authkey", "--purpose=crypt");
      runKeyCzar("addkey", "--location=authkey", "--size=128");
      runKeyCzar("promote", "--location=authkey", "--version=1");
    }

    private static void runKeyCzar(String... args) {
      KeyczarTool.main(args);
    }

    private static class ConfigBuilder {

        private static String footer = "</Configure>\n";

        // t1, t2 and t3 are template strings for writing out an XML file with the right goop
        // to set system properties

        private static String t1 = "   <Call class=\"java.lang.System\" name=\"setProperty\">\n" +
          "      <Arg>";
        private static String t2 = "</Arg>\n      <Arg>";
        private static String t3 = "</Arg>\n   </Call>\n";
        private StringBuffer config;

        ConfigBuilder() {
          config = new StringBuffer();
        }

        ConfigBuilder add(String prop, String value) {
          config.append(t1);
          config.append(prop);
          config.append(t2);
          config.append(value);
          config.append(t3);
          return this;
        }

        void save() {
          try {
              FileOutputStream out = new FileOutputStream("appinventor.xml");
              out.write(readHeader().getBytes());
              out.write(config.toString().getBytes());
              out.write(footer.getBytes());
              out.close();
          } catch (IOException e) {
              throw new RuntimeException(e); // XXX
          }
        }

        String readHeader() {
          String line = null;
          StringBuffer header = new StringBuffer();
          BufferedReader inr = null;
          try {
              inr = new BufferedReader(new FileReader("appinventor.tpl"));
              while ((line = inr.readLine()) != null) {
                  header.append(line + "\n");
              }
              return header.toString();
          } catch (IOException e) {
              throw new RuntimeException(e);
          } finally {
              if (inr != null)
                  try {
                      inr.close();
                  } catch (IOException e) {
                  }
          }
        }
    }
}
