// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.server;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class GalleryEmail {
  private static final Logger LOG = Logger.getLogger(GalleryEmail.class.getName());

  public boolean sendEmail(String senderEmail, String receiverEmail, String title, String body){
    Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    try {
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(senderEmail, "App Inventor Gallery"));
        msg.addRecipient(Message.RecipientType.TO,
         new InternetAddress(receiverEmail, receiverEmail));
        msg.setSubject(title);
        msg.setText(body);
        Transport.send(msg);

        return true;
    } catch (AddressException e) {
        LOG.info("### AddressException");
        return false;
    } catch (MessagingException e) {
        LOG.info("### MessagingException");
        return false;
    } catch (UnsupportedEncodingException e) {
        LOG.info("### UnsupportedEncodingException");
        return false;
    }
  }
}
