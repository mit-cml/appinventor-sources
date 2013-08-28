// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
// This work is licensed under a Creative Commons Attribution 3.0 Unported License.

package com.google.appinventor.components.runtime.util;
import com.google.appinventor.components.runtime.ReplForm;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Properties;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.os.Build;
import android.util.Log;

import com.google.appinventor.components.common.YaVersion;

import kawa.standard.Scheme;
import gnu.expr.Language;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

public class AppInvHTTPD extends NanoHTTPD {

  private File rootDir;
  private Language scheme;
  private ReplForm form;
  private static final int YAV_SKEW_FORWARD = 1;
  private static final int YAV_SKEW_BACKWARD = 4;
  private static final String LOG_TAG = "AppInvHTTPD";
  private static byte[] hmacKey;
  private static int seq;
  private static final String MIME_JSON = "application/json"; // Other mime types defined in NanoHTTPD

  public AppInvHTTPD( int port, File wwwroot, ReplForm form) throws IOException
  {
    super(port, wwwroot);
    this.rootDir = wwwroot;
    this.scheme = Scheme.getInstance("scheme");
    this.form = form;
    gnu.expr.ModuleExp.mustNeverCompile();
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
    Log.d(LOG_TAG,  method + " '" + uri + "' " );

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

    if (method.equals("OPTIONS")) { // This is a complete hack. OPTIONS requests are used
                                    // by Cross Origin Resource Sharing. We give a response
                                    // that permits connections to us from Javascript
                                    // loaded from other pages (like the App Inventor Blocks Editor)
      Enumeration e = header.propertyNames();
      while ( e.hasMoreElements())
        {
          String value = (String)e.nextElement();
          Log.d(LOG_TAG,  "  HDR: '" + value + "' = '" +
            header.getProperty( value ) + "'" );
        }
      Response res = new Response(HTTP_OK, MIME_PLAINTEXT, "OK");
      res.addHeader("Access-Control-Allow-Origin", "*");
      res.addHeader("Access-Control-Allow-Headers", "origin, content-type");
      res.addHeader("Access-Control-Allow-Methods", "POST,OPTIONS,GET,HEAD,PUT");
      res.addHeader("Allow", "POST,OPTIONS,GET,HEAD,PUT");
      return (res);
    }


    if (uri.equals("/_newblocks")) { // Handle AJAX calls from the newblocks code
      String inSeq = parms.getProperty("seq", "0");
      int iseq = Integer.parseInt(inSeq);
      String blockid = parms.getProperty("blockid");
      String code = parms.getProperty("code");
      String inMac = parms.getProperty("mac", "no key provided");
      String compMac = "";
      String input_code = code;
      if (hmacKey != null) {
        try {
          Mac hmacSha1 = Mac.getInstance("HmacSHA1");
          SecretKeySpec key = new SecretKeySpec(hmacKey, "RAW");
          hmacSha1.init(key);
          byte [] tmpMac = hmacSha1.doFinal((code + inSeq + blockid).getBytes());
          StringBuffer sb = new StringBuffer(tmpMac.length * 2);
          Formatter formatter = new Formatter(sb);
          for (byte b : tmpMac)
            formatter.format("%02x", b);
          compMac = sb.toString();
        } catch (Exception e) {
          Log.e(LOG_TAG, "Error working with hmac", e);
          form.dispatchErrorOccurredEvent(form, "AppInvHTTPD",
            ErrorMessages.ERROR_REPL_SECURITY_ERROR, "Exception working on HMAC");
          Response res = new Response(HTTP_OK, MIME_PLAINTEXT, "NOT");
          return(res);
        }
        Log.d(LOG_TAG, "Incoming Mac = " + inMac);
        Log.d(LOG_TAG, "Computed Mac = " + compMac);
        Log.d(LOG_TAG, "Incoming seq = " + inSeq);
        Log.d(LOG_TAG, "Computed seq = " + seq);
        Log.d(LOG_TAG, "blockid = " + blockid);
        if (!inMac.equals(compMac)) {
          Log.e(LOG_TAG, "Hmac does not match");
          form.dispatchErrorOccurredEvent(form, "AppInvHTTPD",
            ErrorMessages.ERROR_REPL_SECURITY_ERROR, "Invalid HMAC");
          Response res = new Response(HTTP_OK, MIME_JSON, "{\"status\" : \"BAD\", \"message\" : \"Security Error: Invalid MAC\"}");
          return(res);
        }
        if ((seq != iseq) && (seq != (iseq+1))) {
          Log.e(LOG_TAG, "Seq does not match");
          form.dispatchErrorOccurredEvent(form, "AppInvHTTPD",
            ErrorMessages.ERROR_REPL_SECURITY_ERROR, "Invalid Seq");
          Response res = new Response(HTTP_OK, MIME_JSON, "{\"status\" : \"BAD\", \"message\" : \"Security Error: Invalid Seq\"}");
          return(res);
        }
        // Seq Fixup: Sometimes the Companion doesn't increment it's seq if it is in the middle of a project switch
        // so we tolerate an off-by-one here.
        if (seq == (iseq+1))
          Log.e(LOG_TAG, "Seq Fixup Invoked");
        seq = iseq + 1;
      } else {                  // No hmacKey
        Log.e(LOG_TAG, "No HMAC Key");
        form.dispatchErrorOccurredEvent(form, "AppInvHTTPD",
          ErrorMessages.ERROR_REPL_SECURITY_ERROR, "No HMAC Key");
        Response res = new Response(HTTP_OK, MIME_JSON, "{\"status\" : \"BAD\", \"message\" : \"Security Error: No HMAC Key\"}");
        return(res);
      }

      code = "(begin (require <com.google.youngandroid.runtime>) (process-repl-input " + blockid + " (begin " +
        code + " )))";

      Log.d(LOG_TAG, "To Eval: " + code);

      Response res;

      try {
        // Don't evaluate a simple "#f" which is used by the poller
        if (input_code.equals("#f")) {
          Log.e(LOG_TAG, "Skipping evaluation of #f");
        } else {
          scheme.eval(code);
        }
        res = new Response(HTTP_OK, MIME_JSON, RetValManager.fetch(false));
      } catch (Throwable ex) {
        Log.e(LOG_TAG, "newblocks: Scheme Failure", ex);
        RetValManager.appendReturnValue(blockid, "BAD", "");
        res = new Response(HTTP_OK, MIME_JSON, RetValManager.fetch(false));
      }
      res.addHeader("Access-Control-Allow-Origin", "*");
      res.addHeader("Access-Control-Allow-Headers", "origin, content-type");
      res.addHeader("Access-Control-Allow-Methods", "POST,OPTIONS,GET,HEAD,PUT");
      res.addHeader("Allow", "POST,OPTIONS,GET,HEAD,PUT");
      return(res);
    } else if (uri.equals("/_values")) {
      Response res = new Response(HTTP_OK, MIME_JSON, RetValManager.fetch(true)); // Blocking Fetch
      res.addHeader("Access-Control-Allow-Origin", "*");
      res.addHeader("Access-Control-Allow-Headers", "origin, content-type");
      res.addHeader("Access-Control-Allow-Methods", "POST,OPTIONS,GET,HEAD,PUT");
      res.addHeader("Allow", "POST,OPTIONS,GET,HEAD,PUT");
      return(res);
    } else if (uri.equals("/_getversion")) {
      Response res;
      try {
        PackageInfo pInfo = form.getPackageManager().getPackageInfo(form.getPackageName(), 0);
        String versionName = pInfo.versionName;
        res = new Response(HTTP_OK, MIME_JSON, "{\"version\" : \"" + versionName + "\", \"fingerprint\" : \"" + Build.FINGERPRINT + "\"}");
      } catch (NameNotFoundException n) {
        n.printStackTrace();
        res = new Response(HTTP_OK, MIME_JSON, "{\"verison\" : \"Unknown\"");
      }
      res.addHeader("Access-Control-Allow-Origin", "*");
      res.addHeader("Access-Control-Allow-Headers", "origin, content-type");
      res.addHeader("Access-Control-Allow-Methods", "POST,OPTIONS,GET,HEAD,PUT");
      res.addHeader("Allow", "POST,OPTIONS,GET,HEAD,PUT");
      return (res);
    } else if (uri.equals("/_package")) { // Handle installing a package
      Response res;
      String packageapk = parms.getProperty("package", null);
      if (packageapk == null) {
        res = new Response(HTTP_OK, MIME_PLAINTEXT, "NOT OK"); // Should really return an error code, but we don't look at it yet
        return (res);
      }
      Log.d(LOG_TAG, rootDir + "/" + packageapk);
      Intent intent = new Intent(Intent.ACTION_VIEW);
      Uri packageuri = Uri.fromFile(new File(rootDir + "/" + packageapk));
      intent.setDataAndType(packageuri, "application/vnd.android.package-archive");
      form.startActivity(intent);
      res = new Response(HTTP_OK, MIME_PLAINTEXT, "OK");
      return (res);
    }

    if (method.equals("PUT")) { // Asset File Upload for newblocks
      Boolean error = false;
      String tmpFileName = (String) files.getProperty("content", null);
      if (tmpFileName != null) { // We have content
        File fileFrom = new File(tmpFileName);
        String filename = parms.getProperty("filename", null);
        if (filename != null) {
          if (filename.startsWith("..") || filename.endsWith("..")
            || filename.indexOf("../") >= 0) {
            Log.d(LOG_TAG, " Ignoring invalid filename: " + filename);
            filename = null;
          }
        }
        if (filename != null) { // We have a filename and it has not been declared
                                // invalid by the code above
          File fileTo = new File(rootDir + "/" + filename);
          if (!fileFrom.renameTo(fileTo)) { // First try rename
            copyFile(fileFrom, fileTo);
            fileFrom.delete();  // Remove temp file
          }
        } else {
          fileFrom.delete();    // We have content but no file name
          Log.e(LOG_TAG, "Received content without a file name!");
          error = true;
        }
      } else {
        Log.e(LOG_TAG, "Received PUT without content.");
        error = true;
      }
      if (error) {
        Response res = new Response(HTTP_OK, MIME_PLAINTEXT, "NOTOK");
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Headers", "origin, content-type");
        res.addHeader("Access-Control-Allow-Methods", "POST,OPTIONS,GET,HEAD,PUT");
        res.addHeader("Allow", "POST,OPTIONS,GET,HEAD,PUT");
        return (res);
      } else {
        Response res = new Response(HTTP_OK, MIME_PLAINTEXT, "OK");
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Headers", "origin, content-type");
        res.addHeader("Access-Control-Allow-Methods", "POST,OPTIONS,GET,HEAD,PUT");
        res.addHeader("Allow", "POST,OPTIONS,GET,HEAD,PUT");
        return (res);
      }
    }

    Enumeration e = header.propertyNames();
    while ( e.hasMoreElements())
      {
        String value = (String)e.nextElement();
        Log.d(LOG_TAG,  "  HDR: '" + value + "' = '" +
                       header.getProperty( value ) + "'" );
      }
    e = parms.propertyNames();
    while ( e.hasMoreElements())
      {
        String value = (String)e.nextElement();
        Log.d(LOG_TAG,  "  PRM: '" + value + "' = '" +
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
          Log.d(LOG_TAG, " Ignoring invalid filename: " + filename);
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
        Log.d(LOG_TAG,  " UPLOADED: '" + filename + "' was at '" + tempLocation + "'");
        Response res = new Response(HTTP_OK, MIME_PLAINTEXT, "OK");
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Headers", "origin, content-type");
        res.addHeader("Access-Control-Allow-Methods", "POST,OPTIONS,GET,HEAD,PUT");
        res.addHeader("Allow", "POST,OPTIONS,GET,HEAD,PUT");
        return(res);
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

  /**
   * @param inputKey String key to use the HTOP algorithm seed
   *
   */
  public static void setHmacKey(String inputKey) {
    hmacKey = inputKey.getBytes();
    seq = 1;              // Initialize this now
  }

}
