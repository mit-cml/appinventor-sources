// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.tokenauth;

import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.OdeRemoteServiceServlet;
import com.google.appinventor.server.flags.Flag;

import com.google.appinventor.shared.rpc.tokenauth.TokenAuthService;

import com.google.appinventor.shared.util.Base58Util;

import com.google.protobuf.ByteString;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * CloudDB, TranslateComponent and ChatBot Authentication Service implementation
 * @author joymitro1989@gmail.com(Joydeep Mitra).
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 * This module generates access tokens for the CloudDB database and
 * the translation proxy service.  Neither of these tokens are
 * encrypted, so one needs to be careful placing sensitive data within
 * them. However when these tokens are sent to their respective
 * services they are sent over an encrypted connection.
 *
 * The tokens themselves are base58 encoded Protocol Buffers. The
 * ".proto" files in this directory define the buffer formats.
 *
 * All tokens consist of a protocol buffer which contains an id along
 * with optional flags. An HmacSha256 signature is then created over
 * the result. The "unsigned" data and the signature are then placed
 * in a structure which is encoded and returned
 *
 * Note: There are subtle differences between the tokens. The CloudDB
 * token contains the userId hashed and base58 encoded while the
 * translation token just includes the userId in plain text (as a String).
 * In the ClouDB the token is used to separate out individual MIT App
 * Inventor programmers data. We hash the userId to provide some privacy.
 * In the case of the translation token, we use it to keep track of usage
 * of the service in which case we wish to be able to know who is using
 * (or abusing) the service.
 *
 * The translation token contains a generation field so we can
 * invalidate a translation token which is being used in an abusive
 * mannor while still being able to issue a new token to the
 * programmer (assuming that the abusive behavior was an
 * accident). Note: We have not yet coded the setting of this field,
 * so for now the default value is alway used.
 *
 */
public class TokenAuthServiceImpl extends OdeRemoteServiceServlet
  implements TokenAuthService {
  private static final Logger LOG = Logger.getLogger(TokenAuthServiceImpl.class.getName());

  private String SECRET_KEY_UUID = Flag.createFlag("clouddb.uuid.secret", "").get();
  private String SECRET_KEY_CLOUD_DB = Flag.createFlag("clouddb.secret", "").get();
  private String SECRET_KEY_TRANSLATE = Flag.createFlag("translator.secret", "").get();
  private int SECRET_KEY_TRANSLATE_KEYID = Flag.createFlag("translator.keyid", 1).get().intValue();
  private String SECRET_KEY_CHATBOT = Flag.createFlag("chatbot.secret", "").get();
  private int SECRET_KEY_CHATBOT_KEYID = Flag.createFlag("chatbot.keyid", 1).get().intValue();
  private static final String HMAC_ALGORITHM = "HmacSHA256";

  /*
   * Fetch the CloudDB token
   */
  @Override
  public String getCloudDBToken() {
    byte [] hunsigned = createCloudDBUnsigned(getHuuid()).toByteArray();
    if (hunsigned != null) {
      try {
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY_CLOUD_DB.getBytes(), HMAC_ALGORITHM);
        Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
        hmac.init(secretKeySpec);
        CloudDBTokenAuth.token token = createCloudDBToken(hunsigned,hmac.doFinal(hunsigned));
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
   * returns hashed userId
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

  /**
   * Get the token for the translation service
   *
   * @return base58 encoded token
   */
  @Override
  public String getTranslateToken() {
    try {
      byte [] utoken =
        TranslatorToken.unsigned.newBuilder().setHuuid(userInfoProvider.getUserId()).build().toByteArray();
      SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY_TRANSLATE.getBytes(), HMAC_ALGORITHM);
      Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
      hmac.init(secretKeySpec);
      byte [] signature = hmac.doFinal(utoken);
      byte [] token =
        TranslatorToken.token.newBuilder().setUnsigned(ByteString.copyFrom(utoken))
        .setKeyid(SECRET_KEY_TRANSLATE_KEYID)
        .setSignature(ByteString.copyFrom(signature)).build().toByteArray();
      return (Base58Util.encode(token));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Get the token for the chatbot service also used by the ImageBot
   * service ImageBot may get its own tokens in the future, but for
   * now we use the same tokens. Changing in the future will be easy,
   * because it isn't a component update.
   *
   * @return base58 encoded token
   */
  @Override
  public String getChatBotToken() {
    try {
      byte [] utoken =
        ChatBotToken.unsigned.newBuilder().setHuuid(userInfoProvider.getUserId()).build().toByteArray();
      SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY_CHATBOT.getBytes(), HMAC_ALGORITHM);
      Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
      hmac.init(secretKeySpec);
      byte [] signature = hmac.doFinal(utoken);
      byte [] token =
        ChatBotToken.token.newBuilder().setUnsigned(ByteString.copyFrom(utoken))
        .setKeyid(SECRET_KEY_CHATBOT_KEYID)
        .setSignature(ByteString.copyFrom(signature)).build().toByteArray();
      return (Base58Util.encode(token));
    } catch (Exception e) {
      throw CrashReport.createAndLogError(LOG, null, null, e);
    }
  }

  /*
    returns a Token as a Google Protocol Buffer object
  */
  private CloudDBTokenAuth.token createCloudDBToken(byte[] unsigned, byte[] signature){
    CloudDBTokenAuth.token token = CloudDBTokenAuth.token.newBuilder().setVersion(1)
      .setKeyid(1)
      .setUnsigned(ByteString.copyFrom(unsigned))
      .setSignature(ByteString.copyFrom(signature)).build();
    return token;
  }

  private CloudDBTokenAuth.unsigned createCloudDBUnsigned(String huuid) {
    CloudDBTokenAuth.unsigned retval = CloudDBTokenAuth.unsigned.newBuilder().setHuuid(huuid).build();
    return retval;
  }
}
