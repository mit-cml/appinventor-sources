// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.components;

import com.firebase.security.token.TokenOptions;
import com.google.appinventor.server.OdeRemoteServiceServlet;
import com.firebase.security.token.TokenGenerator;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.shared.rpc.components.FirebaseAuthService;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servlet for the Firebase Authentication RPC.
 *
 * @author will2596@gmail.com (William Byrne)
 */
public class FirebaseAuthServiceImpl extends OdeRemoteServiceServlet
    implements FirebaseAuthService {

  /**
   * Creates a JSON Web Token (JWT) to authenticate an App Inventor App
   * using the default App Inventor Firebase Account.
   *
   * @param  developer the App Inventor user who created the Firebase App
   * @param  project   the App Inventor project using the Firebase component
   * @return a JWT containing developer and project
   *         information for the Firebase App
   */
  public String getToken(String project) {
    Map<String, Object> payload = new HashMap<String, Object>();
    String authenticatedDeveloper = userInfoProvider.getUserEmail().replace(".", ":") + "";
    payload.put("developer", authenticatedDeveloper);
    payload.put("project", project);
    payload.put("uid", "" + UUID.randomUUID());

    // Create a TokenGenerator with the App Inventor Firebase Secret
    String secret = Flag.createFlag("firebase.secret", "").get();
    if(!secret.equals("")) {
      TokenGenerator tokenGen = new TokenGenerator(secret);

      // We need the token to last for the foreseeable future. It would not be
      // feasible to require the end users of App Inventor apps to make
      // requests to the Auth servlet on a regular basis.
      TokenOptions expiration = new TokenOptions();
      Calendar future = Calendar.getInstance();
      future.set(Calendar.YEAR, 2500);
      expiration.setExpires(future.getTime());

      return tokenGen.createToken(payload, expiration); // return a JWT containing the payload
    }
    return ""; // return the empty string if no Firebase Secret was specified
  }
}
