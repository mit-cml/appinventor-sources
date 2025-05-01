// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016-2025 MIT, All rights reserved.
// This is unreleased code.

package edu.mit.appinventor.ssogen;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.ini4j.Wini;

import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;
import org.keyczar.util.Base64Coder;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import com.google.protobuf.ByteString;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Token {
  private static final String HMAC_ALGORITHM = "HmacSHA256";
  private static SecretKeySpec secretKey;

  private Token() {
  }

  public static String makeOneProjectToken(String projectString, String projectName,
    String displayAccountName, String sdueDate, boolean readOnly, String backPackId) {
    try {
      long projectId = Long.parseLong(projectString);
      long dueDate = Long.parseLong(sdueDate);
      TokenProto.token newToken = TokenProto.token.newBuilder()
        .setCommand(TokenProto.token.CommandType.SSOLOGIN)
        .setOneProjectId(projectId)
        .setReadOnly(readOnly)
        .setDisplayprojectname(projectName)
        .setDisplayaccountname(displayAccountName)
        .setDuedate(dueDate)
        .setBackpackid(backPackId)
        .setTs(System.currentTimeMillis()).build();
      Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
      hmac.init(secretKey);
      byte [] signature = hmac.doFinal(newToken.toByteArray());
      TokenProto.envelope newEnvelope = TokenProto.envelope.newBuilder()
        .setUnsigned(ByteString.copyFrom(newToken.toByteArray()))
        .setSignature(ByteString.copyFrom(signature))
        .build();
      return Base64Coder.encode(newEnvelope.toByteArray());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String makeSSOToken(String userId, boolean readOnly, String displayName, String backPackId) {
    try {
      TokenProto.token.Builder builder = TokenProto.token.newBuilder()
        .setCommand(TokenProto.token.CommandType.SSOLOGIN)
        .setUuid(userId)
        .setReadOnly(readOnly)
        .setBackpackid(backPackId)
        .setTs(System.currentTimeMillis());
      if (displayName != null && !displayName.isEmpty()) {
        builder.setDisplayaccountname(displayName);
      }
      TokenProto.token newToken = builder.build();
      Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
      hmac.init(secretKey);
      byte [] signature = hmac.doFinal(newToken.toByteArray());
      TokenProto.envelope newEnvelope = TokenProto.envelope.newBuilder()
        .setUnsigned(ByteString.copyFrom(newToken.toByteArray()))
        .setSignature(ByteString.copyFrom(signature))
        .build();
      return Base64Coder.encode(newEnvelope.toByteArray());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static synchronized String makeAccountCreateToken(String userId, String name)
    throws TokenException {
    try {
      TokenProto.token newToken = TokenProto.token.newBuilder()
        .setCommand(TokenProto.token.CommandType.CREATEACCOUNT)
        .setUuid(userId)
        .setName(name)
        .setTs(System.currentTimeMillis()).build();
      Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
      hmac.init(secretKey);
      byte [] signature = hmac.doFinal(newToken.toByteArray());
      TokenProto.envelope newEnvelope = TokenProto.envelope.newBuilder()
        .setUnsigned(ByteString.copyFrom(newToken.toByteArray()))
        .setSignature(ByteString.copyFrom(signature))
        .build();
      return Base64Coder.encode(newEnvelope.toByteArray());

    } catch (Exception e) {
      throw new TokenException(e.getMessage());
    }
  }

  public static synchronized String makeLogoutToken(String userId)
    throws TokenException {
    try {
      TokenProto.token newToken = TokenProto.token.newBuilder()
        .setCommand(TokenProto.token.CommandType.LOGOUT)
        .setUuid(userId)
        .setTs(System.currentTimeMillis()).build();
      Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
      hmac.init(secretKey);
      byte [] signature = hmac.doFinal(newToken.toByteArray());
      TokenProto.envelope newEnvelope = TokenProto.envelope.newBuilder()
        .setUnsigned(ByteString.copyFrom(newToken.toByteArray()))
        .setSignature(ByteString.copyFrom(signature))
        .build();
      return Base64Coder.encode(newEnvelope.toByteArray());
    } catch (Exception e) {
      throw new TokenException(e.getMessage());
    }
  }

  public static String makeNoopToken(String userId)
    throws TokenException {
    try {
      TokenProto.token newToken = TokenProto.token.newBuilder()
        .setCommand(TokenProto.token.CommandType.NOOP)
        .setUuid(userId)
        .setTs(System.currentTimeMillis()).build();
      Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
      hmac.init(secretKey);
      byte [] signature = hmac.doFinal(newToken.toByteArray());
      TokenProto.envelope newEnvelope = TokenProto.envelope.newBuilder()
        .setUnsigned(ByteString.copyFrom(newToken.toByteArray()))
        .setSignature(ByteString.copyFrom(signature))
        .build();
      return Base64Coder.encode(newEnvelope.toByteArray());
    } catch (Exception e) {
      throw new TokenException(e.getMessage());
    }
  }

  public static String makeCreateProjectToken(String userId, String projectName, String oldProjectId)
    throws TokenException {
    long oldpid = 0;
    if (oldProjectId != null) {
      try {
        oldpid = Long.parseLong(oldProjectId);
      } catch (NumberFormatException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
    }
    try {
      TokenProto.token newToken = TokenProto.token.newBuilder()
        .setCommand(TokenProto.token.CommandType.CREATEPROJECT)
        .setUuid(userId)
        .setName(projectName)
        .setProjectid(oldpid)
        .setTs(System.currentTimeMillis()).build();
      Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
      hmac.init(secretKey);
      byte [] signature = hmac.doFinal(newToken.toByteArray());
      TokenProto.envelope newEnvelope = TokenProto.envelope.newBuilder()
        .setUnsigned(ByteString.copyFrom(newToken.toByteArray()))
        .setSignature(ByteString.copyFrom(signature))
        .build();
      return Base64Coder.encode(newEnvelope.toByteArray());
    } catch (Exception e) {
      throw new TokenException(e.getMessage());
    }
  }

  public static void Usage() {
    System.out.println("Usage: java -jar tokendemo.jar sso <account> <displayname> [-r] [-b backPackId]");
    System.out.println("Usage: java -jar tokendemo.jar createaccount <uuid> <name>");
    System.out.println("Usage: java -jar tokendemo.jar oneproject <projectid> <projectName> <displayName> <dueDate> [-r] [-b backPackId]");
    System.out.println("Usage: java -jar tokendemo.jar createproject <userid> <projectname> [oldprojectid]");
    System.out.println("Usage: java -jar tokendemo.jar logout <userid>");
  }

  public static void main(String [] argv) {

    if (argv.length < 2) {
      Usage();
      System.exit(1);
    }

    OptionParser optionsParser = new OptionParser("rb:");
    String [] optionsToParse = null;
    String command = argv[0];
    if (command.equals("createaccount")) {
      if (argv.length < 3) {
        Usage();
        System.exit(1);
      }
      optionsToParse = Arrays.copyOfRange(argv, 3, argv.length);
    } else {
      optionsToParse = Arrays.copyOfRange(argv, 2, argv.length);
    }

    OptionSet options = optionsParser.parse(optionsToParse);

    Wini parser = null;
    try {
      parser = new Wini(new File("ssogen.ini"));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    String key = parser.get("main", "key");
    secretKey = new SecretKeySpec(key.getBytes(), HMAC_ALGORITHM);

    String uri = parser.get("main", "host");

    try {
      if (command.equals("sso")) {
        boolean readOnly = options.has("r");
        String backPackId = "";
        if (options.has("b")) {
          backPackId = (String) options.valueOf("b");
          System.out.println("backPackId = " + backPackId);
        }
        String token = makeSSOToken(argv[1], readOnly, argv[2], backPackId);
        System.out.println("http://" + uri + "/login/stoken?token=" + token);
      } else if (command.equals("oneproject")) {
        boolean readOnly = options.has("r");
        String backPackId = "";
        if (options.has("b")) {
          backPackId = (String) options.valueOf("b");
          System.out.println("backPackId = " + backPackId);
        }
        String token = makeOneProjectToken(argv[1], argv[2], argv[3], argv[4], readOnly, backPackId);
        System.out.println("http://" + uri + "/login/stoken?token=" + token);
      } else if (command.equals("createaccount")) {
        String token = makeAccountCreateToken(argv[1], argv[2]);
        System.out.println("http://" + uri + "/rest?stoken=" + token);
      } else if (command.equals("noop")) {
        String token = makeNoopToken(argv[1]);
        System.out.println("http://" + uri + "/rest?stoken=" + token);
      } else if (command.equals("logout")) {
        String token = makeLogoutToken(argv[1]);
        System.out.println("http://" + uri + "/rest?stoken=" + token);
      } else if (command.equals("createproject")) {
        String token = null;
        if (argv.length == 3) {
          token = makeCreateProjectToken(argv[1], argv[2], null);
        } else if (argv.length == 4) {
          token = makeCreateProjectToken(argv[1], argv[2], argv[3]);
        }
        System.out.println("http://" + uri + "/rest?stoken=" + token);
      }
    } catch (TokenException e) {
      throw new RuntimeException(e);
    }
  }

}
