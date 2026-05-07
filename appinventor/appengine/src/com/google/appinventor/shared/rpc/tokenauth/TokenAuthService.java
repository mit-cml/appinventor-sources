// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.tokenauth;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Service interface for the CloudDB authentication RPC.
 * Added TranslateToken and ChatBotToken
 * @author joymitro1989@gmail.com (Joydeep Mitra).
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

@RemoteServiceRelativePath(ServerLayout.TOKEN_AUTH_SERVICE)
public interface TokenAuthService extends RemoteService {

  String getCloudDBToken();
  String getTranslateToken();
  String getChatBotToken();
}
