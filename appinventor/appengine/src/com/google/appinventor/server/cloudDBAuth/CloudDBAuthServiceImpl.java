// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.cloudDBAuth;

import com.google.appinventor.server.OdeRemoteServiceServlet;
import com.google.appinventor.server.flags.Flag;

import com.google.appinventor.shared.rpc.cloudDB.CloudDBAuthService;

import com.google.appinventor.shared.util.Base58Util;

import com.google.protobuf.ByteString;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * CloudDB Authentication Service implementation
 * @author joymitro1989@gmail.com(Joydeep Mitra).
 */
public class CloudDBAuthServiceImpl extends OdeRemoteServiceServlet
  implements CloudDBAuthService {

  private String SECRET_KEY_UUID = Flag.createFlag("clouddb.uuid.secret", "").get();
  private String SECRET_KEY_CLOUD_DB = Flag.createFlag("clouddb.secret", "").get();
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  /*
   * returns the auth token for CloudDB encoded in base58.
   */
  @Override
  public String getToken() {
    byte [] hunsigned = createUnsigned(getHuuid()).toByteArray();
    if (hunsigned != null) {
      try {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY_CLOUD_DB.getBytes(), HMAC_ALGORITHM);
        Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
        hmac.init(secretKeySpec);
        TokenAuth.token token = createToken(hunsigned,hmac.doFinal(hunsigned));
        return Base58Util.encode(token.toByteArray());
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
        return null;
      } catch (InvalidKeyException e) {
        e.printStackTrace();
        return null;
      } catch(Exception e){
        e.printStackTrace();
        return null;
      }
    }
    return null;
  }

  /*
    returns hashed userId
  */
  private String getHuuid(){
    try {
      SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY_UUID.getBytes(), HMAC_ALGORITHM);
      Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
      hmac.init(secretKeySpec);
      return Base58Util.encode(hmac.doFinal(userInfoProvider.getUserId().getBytes()));
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    } catch(InvalidKeyException e) {
      e.printStackTrace();
      return null;
    }
  }

  /*
    returns a Token as a Google Protocol Buffer object
  */
  private TokenAuth.token createToken(byte[] unsigned, byte[] signature){
    TokenAuth.token token = TokenAuth.token.newBuilder().setVersion(1)
      .setKeyid(1)
      .setUnsigned(ByteString.copyFrom(unsigned))
      .setSignature(ByteString.copyFrom(signature)).build();
    return token;
  }

  private TokenAuth.unsigned createUnsigned(String huuid) {
    TokenAuth.unsigned retval = TokenAuth.unsigned.newBuilder().setHuuid(huuid).build();
    return retval;
  }
}
