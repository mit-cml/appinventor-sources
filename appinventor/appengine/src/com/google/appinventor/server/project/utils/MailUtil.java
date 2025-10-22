// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2018 Massachusetts Institute of Technology. All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.project.utils;

import com.google.appinventor.server.CrashReport;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailUtil {

  public static final String MESSAGE_TEXT = "%s has shared an App Inventor project with you. " +
      "Please go to %s to accept this invitation to edit.";
  public static final String MESSAGE_HTML;

  static {
    StringBuilder sb = new StringBuilder();
    InputStream is = null;
    try {
      is = MailUtil.class.getResourceAsStream("message.tpl.html");
      InputStreamReader isr = new InputStreamReader(is);
      char[] buffer = new char[4096];
      int read;
      while ((read = isr.read(buffer, 0, 4096)) > 0) {
        sb.append(buffer, 0, read);
      }
    } catch (IOException e) {
      // should never happen since we are reading from the JAR
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch(IOException e) {
          // ... no words
        }
      }
    }
    MESSAGE_HTML = sb.toString();
  }

  public MailUtil() {
  }

  public static void sendMail(String email, String subject, String message, String html) throws IOException {
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    try {
      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress("noreply@appinventor.mit.edu", "MIT App Inventor"));
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
      msg.setSubject(subject);
      if (html != null) {
        Multipart mp = new MimeMultipart("related");
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setContent(message, "text/plain");
        mp.addBodyPart(mbp);
        mbp = new MimeBodyPart();
        mbp.setContent(html, "text/html");
        mp.addBodyPart(mbp);
        mbp = new MimeBodyPart();
        mbp.setFileName("codi.png");
        mbp.setContentID("<codi>");
        mbp.setContent(MailUtil.class.getResourceAsStream("codi.png"), "image/png");
        mp.addBodyPart(mbp);
        msg.setContent(mp);
      } else {
        msg.setText(message);
      }
      Transport.send(msg);
    } catch(MessagingException|UnsupportedEncodingException e) {
      throw new IOException("Unable to send email to " + email, e);
    }
  }
}
