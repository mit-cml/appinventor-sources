// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * Chat Bot Component, based on the Translator Component
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 */

package com.google.appinventor.components.runtime;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.google.appinventor.common.version.AppInventorFeatures;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.Provider;
import com.google.appinventor.components.annotations.ProviderModel;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.chatbot.ChatBotToken;
import com.google.appinventor.components.runtime.errors.StopBlocksExecution;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.Base58Util;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.MediaUtil;
import com.google.protobuf.ByteString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * The ChatBot component is a non-visible component for chatting with an AI
 * chatbot. This version uses a proxy run by MIT that in turn uses the ChatGPT
 * generative large language model.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

@DesignerComponent(version = YaVersion.CHATBOT_COMPONENT_VERSION,
    description = "A Non-visible component for communicating with an AI chat " +
  "bot. This component currently communicates with a proxy run by MIT which in turn " +
  "uses OpenAI's ChatGPT API. This component is considered experimental.",
    category = ComponentCategory.EXPERIMENTAL,
    nonVisible = true,
    iconName = "images/chatbot.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "protobuf-java-3.0.0.jar")
@SimpleObject
public final class ChatBot extends AndroidNonvisibleComponent {

  private static final String COMODO_ROOT =
    "-----BEGIN CERTIFICATE-----\n" +
    "MIIENjCCAx6gAwIBAgIBATANBgkqhkiG9w0BAQUFADBvMQswCQYDVQQGEwJTRTEU\n" +
    "MBIGA1UEChMLQWRkVHJ1c3QgQUIxJjAkBgNVBAsTHUFkZFRydXN0IEV4dGVybmFs\n" +
    "IFRUUCBOZXR3b3JrMSIwIAYDVQQDExlBZGRUcnVzdCBFeHRlcm5hbCBDQSBSb290\n" +
    "MB4XDTAwMDUzMDEwNDgzOFoXDTIwMDUzMDEwNDgzOFowbzELMAkGA1UEBhMCU0Ux\n" +
    "FDASBgNVBAoTC0FkZFRydXN0IEFCMSYwJAYDVQQLEx1BZGRUcnVzdCBFeHRlcm5h\n" +
    "bCBUVFAgTmV0d29yazEiMCAGA1UEAxMZQWRkVHJ1c3QgRXh0ZXJuYWwgQ0EgUm9v\n" +
    "dDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALf3GjPm8gAELTngTlvt\n" +
    "H7xsD821+iO2zt6bETOXpClMfZOfvUq8k+0DGuOPz+VtUFrWlymUWoCwSXrbLpX9\n" +
    "uMq/NzgtHj6RQa1wVsfwTz/oMp50ysiQVOnGXw94nZpAPA6sYapeFI+eh6FqUNzX\n" +
    "mk6vBbOmcZSccbNQYArHE504B4YCqOmoaSYYkKtMsE8jqzpPhNjfzp/haW+710LX\n" +
    "a0Tkx63ubUFfclpxCDezeWWkWaCUN/cALw3CknLa0Dhy2xSoRcRdKn23tNbE7qzN\n" +
    "E0S3ySvdQwAl+mG5aWpYIxG3pzOPVnVZ9c0p10a3CitlttNCbxWyuHv77+ldU9U0\n" +
    "WicCAwEAAaOB3DCB2TAdBgNVHQ4EFgQUrb2YejS0Jvf6xCZU7wO94CTLVBowCwYD\n" +
    "VR0PBAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wgZkGA1UdIwSBkTCBjoAUrb2YejS0\n" +
    "Jvf6xCZU7wO94CTLVBqhc6RxMG8xCzAJBgNVBAYTAlNFMRQwEgYDVQQKEwtBZGRU\n" +
    "cnVzdCBBQjEmMCQGA1UECxMdQWRkVHJ1c3QgRXh0ZXJuYWwgVFRQIE5ldHdvcmsx\n" +
    "IjAgBgNVBAMTGUFkZFRydXN0IEV4dGVybmFsIENBIFJvb3SCAQEwDQYJKoZIhvcN\n" +
    "AQEFBQADggEBALCb4IUlwtYj4g+WBpKdQZic2YR5gdkeWxQHIzZlj7DYd7usQWxH\n" +
    "YINRsPkyPef89iYTx4AWpb9a/IfPeHmJIZriTAcKhjW88t5RxNKWt9x+Tu5w/Rw5\n" +
    "6wwCURQtjr0W4MHfRnXnJK3s9EK0hZNwEGe6nQY1ShjTK3rMUUKhemPR5ruhxSvC\n" +
    "Nr4TDea9Y355e6cJDUCrat2PisP29owaQgVR1EX1n6diIWgVIEM8med8vSTYqZEX\n" +
    "c4g/VhsxOBi0cQ+azcgOno4uG+GMmIPLHzHxREzGBHNJdmAPx/i9F4BrLunMTA5a\n" +
    "mnkPIAou1Z5jJh5VkpTYghdae9C8x49OhgQ=\n" +
    "-----END CERTIFICATE-----\n";

  // We have to include this intermediate certificate because of bugs
  // in older versions of Android

  private static final String COMODO_USRTRUST =
    "-----BEGIN CERTIFICATE-----\n" +
    "MIIFdzCCBF+gAwIBAgIQE+oocFv07O0MNmMJgGFDNjANBgkqhkiG9w0BAQwFADBv\n" +
    "MQswCQYDVQQGEwJTRTEUMBIGA1UEChMLQWRkVHJ1c3QgQUIxJjAkBgNVBAsTHUFk\n" +
    "ZFRydXN0IEV4dGVybmFsIFRUUCBOZXR3b3JrMSIwIAYDVQQDExlBZGRUcnVzdCBF\n" +
    "eHRlcm5hbCBDQSBSb290MB4XDTAwMDUzMDEwNDgzOFoXDTIwMDUzMDEwNDgzOFow\n" +
    "gYgxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpOZXcgSmVyc2V5MRQwEgYDVQQHEwtK\n" +
    "ZXJzZXkgQ2l0eTEeMBwGA1UEChMVVGhlIFVTRVJUUlVTVCBOZXR3b3JrMS4wLAYD\n" +
    "VQQDEyVVU0VSVHJ1c3QgUlNBIENlcnRpZmljYXRpb24gQXV0aG9yaXR5MIICIjAN\n" +
    "BgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAgBJlFzYOw9sIs9CsVw127c0n00yt\n" +
    "UINh4qogTQktZAnczomfzD2p7PbPwdzx07HWezcoEStH2jnGvDoZtF+mvX2do2NC\n" +
    "tnbyqTsrkfjib9DsFiCQCT7i6HTJGLSR1GJk23+jBvGIGGqQIjy8/hPwhxR79uQf\n" +
    "jtTkUcYRZ0YIUcuGFFQ/vDP+fmyc/xadGL1RjjWmp2bIcmfbIWax1Jt4A8BQOujM\n" +
    "8Ny8nkz+rwWWNR9XWrf/zvk9tyy29lTdyOcSOk2uTIq3XJq0tyA9yn8iNK5+O2hm\n" +
    "AUTnAU5GU5szYPeUvlM3kHND8zLDU+/bqv50TmnHa4xgk97Exwzf4TKuzJM7UXiV\n" +
    "Z4vuPVb+DNBpDxsP8yUmazNt925H+nND5X4OpWaxKXwyhGNVicQNwZNUMBkTrNN9\n" +
    "N6frXTpsNVzbQdcS2qlJC9/YgIoJk2KOtWbPJYjNhLixP6Q5D9kCnusSTJV882sF\n" +
    "qV4Wg8y4Z+LoE53MW4LTTLPtW//e5XOsIzstAL81VXQJSdhJWBp/kjbmUZIO8yZ9\n" +
    "HE0XvMnsQybQv0FfQKlERPSZ51eHnlAfV1SoPv10Yy+xUGUJ5lhCLkMaTLTwJUdZ\n" +
    "+gQek9QmRkpQgbLevni3/GcV4clXhB4PY9bpYrrWX1Uu6lzGKAgEJTm4Diup8kyX\n" +
    "HAc/DVL17e8vgg8CAwEAAaOB9DCB8TAfBgNVHSMEGDAWgBStvZh6NLQm9/rEJlTv\n" +
    "A73gJMtUGjAdBgNVHQ4EFgQUU3m/WqorSs9UgOHYm8Cd8rIDZsswDgYDVR0PAQH/\n" +
    "BAQDAgGGMA8GA1UdEwEB/wQFMAMBAf8wEQYDVR0gBAowCDAGBgRVHSAAMEQGA1Ud\n" +
    "HwQ9MDswOaA3oDWGM2h0dHA6Ly9jcmwudXNlcnRydXN0LmNvbS9BZGRUcnVzdEV4\n" +
    "dGVybmFsQ0FSb290LmNybDA1BggrBgEFBQcBAQQpMCcwJQYIKwYBBQUHMAGGGWh0\n" +
    "dHA6Ly9vY3NwLnVzZXJ0cnVzdC5jb20wDQYJKoZIhvcNAQEMBQADggEBAJNl9jeD\n" +
    "lQ9ew4IcH9Z35zyKwKoJ8OkLJvHgwmp1ocd5yblSYMgpEg7wrQPWCcR23+WmgZWn\n" +
    "RtqCV6mVksW2jwMibDN3wXsyF24HzloUQToFJBv2FAY7qCUkDrvMKnXduXBBP3zQ\n" +
    "YzYhBx9G/2CkkeFnvN4ffhkUyWNnkepnB2u0j4vAbkN9w6GAbLIevFOFfdyQoaS8\n" +
    "Le9Gclc1Bb+7RrtubTeZtv8jkpHGbkD4jylW6l/VXxRTrPBPYer3IsynVgviuDQf\n" +
    "Jtl7GQVoP7o81DgGotPmjw7jtHFtQELFhLRAlSv0ZaBIefYdgWOWnU914Ph85I6p\n" +
    "0fKtirOMxyHNwu8=\n" +
    "-----END CERTIFICATE-----\n";

  // Replacement for the expired DST_ROOT_X3
  // used by Letsencrypt

  private static final String ISRG_ROOT_X1 =
    "-----BEGIN CERTIFICATE-----\n" +
    "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\n" +
    "-----END CERTIFICATE-----\n";

  private static final String MIT_CA =
    "-----BEGIN CERTIFICATE-----\n" +
    "MIIFXjCCBEagAwIBAgIJAMLfrRWIaHLbMA0GCSqGSIb3DQEBCwUAMIHPMQswCQYD\n" +
    "VQQGEwJVUzELMAkGA1UECBMCTUExEjAQBgNVBAcTCUNhbWJyaWRnZTEuMCwGA1UE\n" +
    "ChMlTWFzc2FjaHVzZXR0cyBJbnN0aXR1dGUgb2YgVGVjaG5vbG9neTEZMBcGA1UE\n" +
    "CxMQTUlUIEFwcCBJbnZlbnRvcjEmMCQGA1UEAxMdQ2xvdWREQiBDZXJ0aWZpY2F0\n" +
    "ZSBBdXRob3JpdHkxEDAOBgNVBCkTB0Vhc3lSU0ExGjAYBgkqhkiG9w0BCQEWC2pp\n" +
    "c0BtaXQuZWR1MB4XDTE3MTIyMjIyMzkyOVoXDTI3MTIyMDIyMzkyOVowgc8xCzAJ\n" +
    "BgNVBAYTAlVTMQswCQYDVQQIEwJNQTESMBAGA1UEBxMJQ2FtYnJpZGdlMS4wLAYD\n" +
    "VQQKEyVNYXNzYWNodXNldHRzIEluc3RpdHV0ZSBvZiBUZWNobm9sb2d5MRkwFwYD\n" +
    "VQQLExBNSVQgQXBwIEludmVudG9yMSYwJAYDVQQDEx1DbG91ZERCIENlcnRpZmlj\n" +
    "YXRlIEF1dGhvcml0eTEQMA4GA1UEKRMHRWFzeVJTQTEaMBgGCSqGSIb3DQEJARYL\n" +
    "amlzQG1pdC5lZHUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDHzI3D\n" +
    "FobNDv2HTWlDdedmbxZIJYSqWlzdRJC3oVJgCubdAs46WJRqUxDRWft9UpYGMKkw\n" +
    "mYN8mdPby2m5OJagdVIZgnguB71zIQkC8yMzd94FC3gldX5m7R014D/0fkpzvsSt\n" +
    "6fsNectJT0k7gPELOH6t4u6AUbvIsEX0nNyRWsmA/ucXCsDBwXyBJxfOKIQ9tDI4\n" +
    "/WfcKk9JDpeMF7RP0CIOtlAPotKIaPoY1W3eMIi/0riOt5vTFsB8pxhxAVy0cfGX\n" +
    "iHukdrAkAJixTgkyS7wzk22xOeXVnRIzAMGK5xHMDw/HRQGTrUGfIXHENV3u+3Ae\n" +
    "L5/ZoQwyZTixmQNzAgMBAAGjggE5MIIBNTAdBgNVHQ4EFgQUZfMKQXqtC5UJGFrZ\n" +
    "gZE1nmlx+t8wggEEBgNVHSMEgfwwgfmAFGXzCkF6rQuVCRha2YGRNZ5pcfrfoYHV\n" +
    "pIHSMIHPMQswCQYDVQQGEwJVUzELMAkGA1UECBMCTUExEjAQBgNVBAcTCUNhbWJy\n" +
    "aWRnZTEuMCwGA1UEChMlTWFzc2FjaHVzZXR0cyBJbnN0aXR1dGUgb2YgVGVjaG5v\n" +
    "bG9neTEZMBcGA1UECxMQTUlUIEFwcCBJbnZlbnRvcjEmMCQGA1UEAxMdQ2xvdWRE\n" +
    "QiBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxEDAOBgNVBCkTB0Vhc3lSU0ExGjAYBgkq\n" +
    "hkiG9w0BCQEWC2ppc0BtaXQuZWR1ggkAwt+tFYhoctswDAYDVR0TBAUwAwEB/zAN\n" +
    "BgkqhkiG9w0BAQsFAAOCAQEAIkKr3eIvwZO6a1Jsh3qXwveVnrqwxYvLw2IhTwNT\n" +
    "/P6C5jbRnzUuDuzg5sEIpbBo/Bp3qIp7G5cdVOkIrqO7uCp6Kyc7d9lPsEe/cbF4\n" +
    "aNwNmdWroRN1y0tuMU6+z7frd5pOeAZP9E/DM/0Uaz4yVzwnlvZUttaLymyMhH54\n" +
    "isGQKbAqHDFtKZvb6DxsHzrO2YgeaBAtjeVhPWiv8BhzbOo9+hhZvYHYtoM2W+Ze\n" +
    "DHuvv0v+qouphftDKVBp16N8Pk5WgabTXzV6VcNee92iwbWYDEv06+S3AF/q2TBe\n" +
    "xxXtAa5ywbp6IRF37QuQChcYnOx7zIylYI1PIENfQFC2BA==\n" +
    "-----END CERTIFICATE-----\n";

  private static final String CHATBOT_SERVICE_URL = AppInventorFeatures.chatBotHost() + "chat/v1";
  private static final String LOG_TAG = "ChatBot";
  private static final boolean DEBUG = false;

  private String apiKey;        // User supplied actual ChatGPT API key
  private String model = "";    // Model to use, provider dependent
  private String provider = "chatgpt";      // The provider to use (default chatgpt)
  private SSLSocketFactory sslSockFactory = null; // Socket Factory for using
                                                  // SSL
  private String system = "";   // The "System" string for ChatGPT
  private String token;         // MIT Access Token, generated by MockChatBot
  private String uuid = "";     // The UUID for continuing conversations
  private int size = 256;

  /**
   * Creates a new component.
   *
   * @param container  container, component will be placed in
   */
  public ChatBot(ComponentContainer container) {
    super(container.$form());
  }

  @SimpleFunction(description = "Reset the current conversation, Chat bot will forget " +
    "any previous conversation when responding in the future.")
  public void ResetConversation() {
    this.uuid = "";
  }

  @SimpleFunction(description = "Ask a question of the Chat Bot. Successive calls will " +
    "remember information from earlier in the conversation. Use the \"ResetConversation\" " +
    "function to reset for a new conversation.")
  public void Converse(final String question) {

    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        performRequest(uuid, question, null);
      }
      });
  }

  private void performRequest(String uuid, String question, Bitmap image) {
    // languageToTransateTo is provided either as a two letter code, or two
    // two letter codes separated by a dash. If only one two letter code is
    // provided, it is the target language and we set the source language to auto
    // which tells the service to detect the language

    /* Convert Bitmap to an image String suitable to send to the ChatBot proxy */
    ByteString imageString = null;
    if (image != null) {
      ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();
      image.compress(Bitmap.CompressFormat.PNG, 100, imageBuffer);
      imageString = ByteString.copyFrom(imageBuffer.toByteArray());
    }

    HttpsURLConnection connection = null;
    ensureSslSockFactory();
    String iToken;
    int responseCode = -1;   // A reasonable default
    try {
      Log.d(LOG_TAG, "performRequest: apiKey = " + apiKey);
      if (token != null && !token.equals("") && token.substring(0, 1).equals("%")) {
        iToken = token.substring(1);
      } else {
        iToken = token;
      }
      byte [] decodedToken = Base58Util.decode(iToken);
      ChatBotToken.token token = ChatBotToken.token.parseFrom(decodedToken);
      ChatBotToken.request.Builder builder = ChatBotToken.request.newBuilder()
        .setToken(token)
        .setUuid(uuid)
        .setProvider(provider)
        .setQuestion(question);
      if (!system.equals("") && uuid.equals("")) {
        builder = builder.setSystem(system);
      }
      if (apiKey != null && !apiKey.equals("")) {
        builder = builder.setApikey(apiKey);
      }
      if (!model.isEmpty()) {
        builder.setModel(model);
      }
      if (imageString != null) {
        builder.setInputimage(imageString);
      }
      ChatBotToken.request request = builder.build();

      URL url = new URL(CHATBOT_SERVICE_URL);
      connection = (HttpsURLConnection) url.openConnection();
      if (connection != null) {
        try {
          connection.setSSLSocketFactory(sslSockFactory);
          connection.setRequestMethod("POST");
          connection.setDoOutput(true);
          request.writeTo(connection.getOutputStream());
          responseCode = connection.getResponseCode();
          ChatBotToken.response response = ChatBotToken.response.parseFrom(connection.getInputStream());
          String returnText;
          if (responseCode == 200) {
            returnText = response.getAnswer();
            this.uuid = response.getUuid();
            GotResponse(returnText);
          } else {
            returnText = getResponseContent(connection, true);
            ErrorOccurred(responseCode, returnText);
          }
        } finally {
          connection.disconnect();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      if (e instanceof FileNotFoundException && connection != null) {
        String returnText;
        try {
          returnText = getResponseContent(connection, true);
        } catch  (IOException ee) {
          returnText = "Error Fetching from ChatBot";
        }
        ErrorOccurred(responseCode, returnText);
      } else {
        ErrorOccurred(responseCode, "Error talking to ChatBot proxy");
      }
    }
  }

  @SimpleFunction(description = "Ask a question of the Chat Bot using an Image. Successive calls will " +
    "remember information from earlier in the conversation. Use the \"ResetConversation\" " +
    "function to reset for a new conversation.")
  public void ConverseWithImage(final String question, final Object source) {
    try {
      final Bitmap bitmap = loadImage(source);
      if (bitmap != null) {
        AsynchUtil.runAsynchronously(new Runnable() {
          @Override
          public void run() {
            performRequest(uuid, question, bitmap);
          }
        });
      } else {
        form.androidUIHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            ErrorOccurred(-1, "Invalid input to ChatBot");
          }
        }, 0);
        throw new StopBlocksExecution();
      }
    } catch (IOException e) {
      Log.e(LOG_TAG, "Unable to read image", e);
    }
  }

  /**
   * Event indicating that a request has finished and has returned data (output from ChatBot).
   *
   * @param responseText the response content from the server
   */
  @SimpleEvent(description = "Event fired when the Chat Bot answers a question.")
  public void GotResponse(final String responseText) {
    form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          EventDispatcher.dispatchEvent(ChatBot.this, "GotResponse", responseText);
        }
      });
  }

  /**
   * The "System" value. Used by ChatGPT. Example: "You are a funny person"
   * It sets the tone for the conversation.
   */

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    description = "The \"System\" value given to ChatGPT. It is " +
    "used to set the tone of a conversation. For example: \"You are a funny person.\"",
    userVisible = true)
  public String System() {
    return system;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_TEXTAREA,
    defaultValue = "")
  @SimpleProperty(description = "The \"System\" value given to ChatGPT. It is " +
    "used to set the tone of a conversation. For example: \"You are a funny person.\"",
    userVisible = true)
  public void System(String system) {
    this.system = system;
  }

  /**
   * The MIT Access token to use. MIT App Inventor will automatically fill this
   * value in. You should not need to change it.
   *
   */

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(description = "The MIT Access token to use. MIT App Inventor will automatically fill this " +
    "value in. You should not need to change it.",
    userVisible = true, category = PropertyCategory.ADVANCED)
  public void Token(String token) {
    this.token = token;
  }

  /**
   * An ApiKey for ChatGPT. User supplied. If provided, we will use it instead of the
   * API key embedded in the chat proxy service.
   *
   * Note: We do not provide this as a DesignerProperty, it should be stored using the
   * blocks, preferably using the Obfuscated Text block to provide some protection
   * (not perfect protection) of the key embedded in a packaged app.
   *
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "A ChatGPT API Key. If provided, it will be used instead of " +
         "the embedded APIKEY in the ChatBot proxy server")
  public void ApiKey(String apikey) {
    this.apiKey = apikey;
  }

  @SimpleProperty
  public String ApiKey() {
    return this.apiKey;
  }

  /**
   * Set the name of the provider to use. See https://appinv.us/chatbot for
   * the current list of supported providers.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    description = "Set the name of the provider to use. " +
    "See https://appinv.us/chatbot for the current list of supported " +
    "providers.",
    userVisible = true)
  public String Provider() {
    return provider;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHATBOT_PROVIDER,
    defaultValue = "chatgpt")
  @SimpleProperty(description = "Set the name of the provider to use. " +
    "See https://appinv.us/chatbot for the current list of supported " +
    "providers.",
    userVisible = true)
  public void Provider(@Provider String provider) {
    this.provider = provider;
  }

  /**
   * Set the name of the model to use. See https://appinv.us/chatbot for
   * the current list of supported models.
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    description = "Set the name of the model to use. " +
    "See https://appinv.us/chatbot for the current list of supported " +
    "models. Leaving this blank will result in the default model set by " +
    "the provider being used",
    userVisible = true)
  public String Model() {
    return model;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_CHATBOT_MODEL,
    defaultValue = "")
  @SimpleProperty(description = "Set the name of the model to use. " +
    "See https://appinv.us/chatbot for the current list of supported " +
    "models. Leaving this blank will result in the default model set by " +
    "the provider being used",
    userVisible = true)
  public void Model(@ProviderModel String model) {
    this.model = model;
  }

  /**
   * This method reads from a stream based on the passed connection
   * @param connection the connection to read from
   * @return the contents of the stream
   * @throws IOException if it cannot read from the http connection
   */
  private static String getResponseContent(HttpsURLConnection connection, boolean error) throws IOException {
    // Use the content encoding to convert bytes to characters.
    String encoding = connection.getContentEncoding();
    if (encoding == null) {
      encoding = "UTF-8";
    }
    InputStreamReader reader;
    if (error) {
      reader = new InputStreamReader(connection.getErrorStream(), encoding);
    } else {
      reader = new InputStreamReader(connection.getInputStream(), encoding);
    }
    try {
      int contentLength = connection.getContentLength();
      StringBuilder sb = (contentLength != -1)
          ? new StringBuilder(contentLength)
          : new StringBuilder();
      char[] buf = new char[1024];
      int read;
      while ((read = reader.read(buf)) != -1) {
        sb.append(buf, 0, read);
      }
      return sb.toString();
    } finally {
      reader.close();
    }
  }

  /**
   * The ErrorOccurred event will be run when an error occurs during
   * processing, such as if your you are over usage quota, or some
   * other error signaled by ChatGPT or PaLM. See
   * https://appinv.us/chatbot for current information.
   *
   * @param responseCode the HTTP status code returned by the server
   * @param responseText a description of the error
   */

  @SimpleEvent
  public void ErrorOccurred(final int responseCode, final String responseText) {
    form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (!EventDispatcher.dispatchEvent(ChatBot.this, "ErrorOccurred", responseCode, responseText)) {
            form.dispatchErrorOccurredEvent(ChatBot.this, "ErrorOccurred",
              ErrorMessages.ERROR_CHATBOT_ERROR, responseCode, responseText);
          }
        }
      });
  }

  private Bitmap loadImage(Object source) throws IOException {
    Bitmap bitmap = null;
    Log.d(LOG_TAG, "loadImage source = " + source);
    if (source instanceof Canvas) {
      bitmap = ((Canvas) source).getBitmap();
    } else if (source instanceof Image) {
      bitmap = ((BitmapDrawable) ((Image) source).getView().getBackground()).getBitmap();
    } else {
      String sourceStr = source.toString();
      bitmap = MediaUtil.getBitmapDrawable(form, sourceStr).getBitmap();
    }
    if (bitmap != null) {
      if (bitmap.getWidth() == size && bitmap.getHeight() == size) {
        return bitmap;
      } else {
        bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
      }
    }
    return bitmap;
  }

  // We are synchronized because we are called simultaneously from two
  // different threads. Rather then do the work twice, the first one
  // does the work and the second one waits!
  private synchronized void ensureSslSockFactory() {
    if (sslSockFactory != null) {
      return;
    } else {
      try {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream caInput = new ByteArrayInputStream(COMODO_ROOT.getBytes("UTF-8"));
        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();
        caInput = new ByteArrayInputStream(COMODO_USRTRUST.getBytes("UTF-8"));
        Certificate inter = cf.generateCertificate(caInput);
        caInput.close();
        caInput = new ByteArrayInputStream(MIT_CA.getBytes("UTF-8"));
        Certificate mitca = cf.generateCertificate(caInput);
        caInput.close();
        caInput = new ByteArrayInputStream(ISRG_ROOT_X1.getBytes("UTF-8"));
        Certificate isrg = cf.generateCertificate(caInput);
        caInput.close();
        if (DEBUG) {
          Log.d(LOG_TAG, "comodo=" + ((X509Certificate) ca).getSubjectDN());
          Log.d(LOG_TAG, "inter=" + ((X509Certificate) inter).getSubjectDN());
          Log.d(LOG_TAG, "mitca=" + ((X509Certificate) mitca).getSubjectDN());
          Log.d(LOG_TAG, "isrg=" + ((X509Certificate) isrg).getSubjectDN());
        }
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);
        // First add the system trusted certificates
        int count = 1;
        for (X509Certificate cert : getSystemCertificates()) {
          keyStore.setCertificateEntry("root" + count, cert);
          count += 1;
        }
        if (DEBUG) {
          Log.d(LOG_TAG, "Added " + (count -1) + " system certificates!");
        }
        // Now add our additions
        keyStore.setCertificateEntry("comodo", ca);
        keyStore.setCertificateEntry("inter", inter);
        keyStore.setCertificateEntry("mitca", mitca);
        keyStore.setCertificateEntry("isrg", isrg);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        // DEBUG
        if (DEBUG) {
          Log.d(LOG_TAG, "And now for something completely different...");
          X509TrustManager tm = (X509TrustManager) tmf.getTrustManagers()[0];
          for (X509Certificate cert : tm.getAcceptedIssuers()) {
            Log.d(LOG_TAG, cert.getSubjectX500Principal().getName());
          }
        }
        // END DEBUG
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);
        sslSockFactory = ctx.getSocketFactory();
      } catch (Exception e) {
        Log.e(LOG_TAG, "Could not setup SSL Trust Store for ChatBot", e);
        throw new YailRuntimeError("Could Not setup SSL Trust Store for ChatBot: ", e.getMessage());
      }
    }
  }

  /*
   * Get the list of root CA's trusted by this device
   *
   */
  private X509Certificate[] getSystemCertificates() {
    try {
      TrustManagerFactory otmf = TrustManagerFactory.getInstance(
        TrustManagerFactory.getDefaultAlgorithm());
      otmf.init((KeyStore) null);
      X509TrustManager otm = (X509TrustManager) otmf.getTrustManagers()[0];
      return otm.getAcceptedIssuers();
    } catch (Exception e) {
      Log.e(LOG_TAG, "Getting System Certificates", e);
      return new X509Certificate[0];
    }
  }
}

