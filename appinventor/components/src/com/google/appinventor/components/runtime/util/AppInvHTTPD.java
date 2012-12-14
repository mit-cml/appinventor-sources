// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
// This work is licensed under a Creative Commons Attribution 3.0 Unported License.

package com.google.appinventor.components.runtime.util;
import com.google.appinventor.components.runtime.ReplForm;
import java.util.Enumeration;
import java.util.Properties;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import com.google.appinventor.components.common.YaVersion;
import kawa.standard.Scheme;
import gnu.expr.Language;

import android.content.Intent;
import android.net.Uri;

public class AppInvHTTPD extends NanoHTTPD {

  private File rootDir;
  private Language scheme;
  private ReplForm form;
  private static final int YAV_SKEW = 1;

  public AppInvHTTPD( int port, File wwwroot, ReplForm form) throws IOException
  {
    super(port, wwwroot);
    this.rootDir = wwwroot;
    this.scheme = Scheme.getInstance("scheme");
    this.form = form;
    gnu.expr.ModuleExp.mustNeverCompile();
    try {
      scheme.eval("(begin (require com.google.youngandroid.runtime)  (setup-repl-environment \"<<\" \":\" \"@@\" \"Success\" \"Failure\" \"==\" \">>\" '((\">>\" \"&2\")(\"<<\" \"&1\")(\"&\" \"&0\"))))");
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  /**
   *
   * @param uri Percent-decoded URI without parameters, for example "/index.cgi"
   * @param method      "GET", "POST" etc.
   * @param parms       Parsed, percent decoded parameters from URI and, in case of POST, data.
   * @param header      Header entries, percent decoded
   * @return HTTP response, see class Response for details
   */
  public Response serve( String uri, String method, Properties header, Properties parms, Properties files )
  {
    myOut.println( method + " '" + uri + "' " );

    // Special case for _version: This uri has a parameter of
    // "version" which is the blocks editor idea of what
    // YaVersion.YOUNG_ANDROID_VERSION should be. If this is not in
    // the range of our YOUNG_ANDROID_VERSION - YAV_SKEW to
    // YOUNG_ANDROID_VERSION, we call "badversion" which is defined in
    // the Yail code for the Wireless Debug Repl. It arranges to do
    // the right thing vis. a vis. the REPL UI
    //
    // We support a range on the theory that people cannot upgrade the
    // REPL exactly when we upgrade the App Engine server. So we
    // permit some version skew. Exactly how much is defined by
    // YAV_SKEW (defined above)

    if (uri.equals("/_version")) { // handle special uri's here
      Response res;
      try {
        String strversion = parms.getProperty("version", "0");
        int version = (new Integer(strversion)).intValue();
        if ((version > (YaVersion.YOUNG_ANDROID_VERSION + YAV_SKEW)) ||
            (version < (YaVersion.YOUNG_ANDROID_VERSION - YAV_SKEW))) {
          scheme.eval("(begin (require com.google.youngandroid.runtime) (process-repl-input ((get-var badversion)) \"foo\"))");
        } else {
          // If we have a good version, start the repl
          // We use Scheme here so we can use process-repl-input which will arrange for
          // the correct thread to be used to start the repl (by going through the android os handler
          scheme.eval("(begin (require com.google.youngandroid.runtime) (process-repl-input ((get-var *start-repl*)) \"foo\"))");
        }
        res = new Response(HTTP_OK, MIME_PLAINTEXT, "OK");
      } catch (Throwable e) {
        res = new Response(HTTP_OK, MIME_PLAINTEXT, e.toString());
        e.printStackTrace();
      }
      return (res);
    } else if (uri.equals("/_package")) { // Handle installing a package
      Response res;
      String packageapk = parms.getProperty("package", null);
      if (packageapk == null) {
        res = new Response(HTTP_OK, MIME_PLAINTEXT, "NOT OK"); // Should really return an error code, but we don't look at it yet
        return (res);
      }
      myOut.println(rootDir + "/" + packageapk);
      Intent intent = new Intent(Intent.ACTION_VIEW);
      Uri packageuri = Uri.fromFile(new File(rootDir + "/" + packageapk));
      intent.setDataAndType(packageuri, "application/vnd.android.package-archive");
      form.startActivity(intent);
      res = new Response(HTTP_OK, MIME_PLAINTEXT, "OK");
      return (res);
    }

    Enumeration e = header.propertyNames();
    while ( e.hasMoreElements())
      {
        String value = (String)e.nextElement();
        myOut.println( "  HDR: '" + value + "' = '" +
                       header.getProperty( value ) + "'" );
      }
    e = parms.propertyNames();
    while ( e.hasMoreElements())
      {
        String value = (String)e.nextElement();
        myOut.println( "  PRM: '" + value + "' = '" +
                       parms.getProperty( value ) + "'" );
      }
    e = files.propertyNames();
    while ( e.hasMoreElements())
      {
        String fieldname = (String)e.nextElement();
        String tempLocation = (String) files.getProperty(fieldname);
        String filename = (String) parms.getProperty(fieldname);
        if (filename.startsWith("..") || filename.endsWith("..")
            || filename.indexOf("../") >= 0) {
          myOut.println(" Ignoring invalid filename: " + filename);
          filename = null;
        }
        File fileFrom = new File(tempLocation);
        if (filename == null) {
          fileFrom.delete(); // Cleanup our mess (remove temp file).
        } else {
          File fileTo = new File(rootDir + "/" + filename);
          if (!fileFrom.renameTo(fileTo)) { // First try rename, otherwise we have to copy
            copyFile(fileFrom, fileTo);
            fileFrom.delete();  // Cleanup temp file
          }
        }
        myOut.println( " UPLOADED: '" + filename + "' was at '" + tempLocation + "'");
      }

    return serveFile( uri, header, rootDir, true );
  }

  private void copyFile(File infile, File outfile) {
    try {
      FileInputStream in = new FileInputStream(infile);
      FileOutputStream out = new FileOutputStream(outfile);
      byte[] buffer = new byte[32768]; // 32K, probably too small
      int len;

      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }

      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
