// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;

import android.util.Log;

import com.google.appinventor.common.version.AppInventorFeatures;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.errors.StopBlocksExecution;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.imagebot.ImageBotToken;

import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.Base58Util;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.FileUtil;
import com.google.appinventor.components.runtime.util.IOUtils;
import com.google.appinventor.components.runtime.util.MediaUtil;

import com.google.protobuf.ByteString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.URL;

import java.nio.charset.StandardCharsets;
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
 * The ImageBot is a non-visible component that uses DALL-E 2 to create and edit images. You must
 * supply your own OpenAI API key for this component by setting its ApiKey property in the blocks.
 */
@SimpleObject
@DesignerComponent(
    version = YaVersion.IMAGEBOT_COMPONENT_VERSION,
    iconName = "images/paintpalette.png",
    nonVisible = true,
    category = ComponentCategory.EXPERIMENTAL,
    androidMinSdk = 9
)
@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "protobuf-java-3.0.0.jar")
public class ImageBot extends AndroidNonvisibleComponent {
  public static final String LOG_TAG = ImageBot.class.getSimpleName();

  private static final boolean DEBUG = false;

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
          "MIIFazCCA1OgAwIBAgIRAIIQz7DSQONZRGPgu2OCiwAwDQYJKoZIhvcNAQELBQAw\n" +
          "TzELMAkGA1UEBhMCVVMxKTAnBgNVBAoTIEludGVybmV0IFNlY3VyaXR5IFJlc2Vh\n" +
          "cmNoIEdyb3VwMRUwEwYDVQQDEwxJU1JHIFJvb3QgWDEwHhcNMTUwNjA0MTEwNDM4\n" +
          "WhcNMzUwNjA0MTEwNDM4WjBPMQswCQYDVQQGEwJVUzEpMCcGA1UEChMgSW50ZXJu\n" +
          "ZXQgU2VjdXJpdHkgUmVzZWFyY2ggR3JvdXAxFTATBgNVBAMTDElTUkcgUm9vdCBY\n" +
          "MTCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAK3oJHP0FDfzm54rVygc\n" +
          "h77ct984kIxuPOZXoHj3dcKi/vVqbvYATyjb3miGbESTtrFj/RQSa78f0uoxmyF+\n" +
          "0TM8ukj13Xnfs7j/EvEhmkvBioZxaUpmZmyPfjxwv60pIgbz5MDmgK7iS4+3mX6U\n" +
          "A5/TR5d8mUgjU+g4rk8Kb4Mu0UlXjIB0ttov0DiNewNwIRt18jA8+o+u3dpjq+sW\n" +
          "T8KOEUt+zwvo/7V3LvSye0rgTBIlDHCNAymg4VMk7BPZ7hm/ELNKjD+Jo2FR3qyH\n" +
          "B5T0Y3HsLuJvW5iB4YlcNHlsdu87kGJ55tukmi8mxdAQ4Q7e2RCOFvu396j3x+UC\n" +
          "B5iPNgiV5+I3lg02dZ77DnKxHZu8A/lJBdiB3QW0KtZB6awBdpUKD9jf1b0SHzUv\n" +
          "KBds0pjBqAlkd25HN7rOrFleaJ1/ctaJxQZBKT5ZPt0m9STJEadao0xAH0ahmbWn\n" +
          "OlFuhjuefXKnEgV4We0+UXgVCwOPjdAvBbI+e0ocS3MFEvzG6uBQE3xDk3SzynTn\n" +
          "jh8BCNAw1FtxNrQHusEwMFxIt4I7mKZ9YIqioymCzLq9gwQbooMDQaHWBfEbwrbw\n" +
          "qHyGO0aoSCqI3Haadr8faqU9GY/rOPNk3sgrDQoo//fb4hVC1CLQJ13hef4Y53CI\n" +
          "rU7m2Ys6xt0nUW7/vGT1M0NPAgMBAAGjQjBAMA4GA1UdDwEB/wQEAwIBBjAPBgNV\n" +
          "HRMBAf8EBTADAQH/MB0GA1UdDgQWBBR5tFnme7bl5AFzgAiIyBpY9umbbjANBgkq\n" +
          "hkiG9w0BAQsFAAOCAgEAVR9YqbyyqFDQDLHYGmkgJykIrGF1XIpu+ILlaS/V9lZL\n" +
          "ubhzEFnTIZd+50xx+7LSYK05qAvqFyFWhfFQDlnrzuBZ6brJFe+GnY+EgPbk6ZGQ\n" +
          "3BebYhtF8GaV0nxvwuo77x/Py9auJ/GpsMiu/X1+mvoiBOv/2X/qkSsisRcOj/KK\n" +
          "NFtY2PwByVS5uCbMiogziUwthDyC3+6WVwW6LLv3xLfHTjuCvjHIInNzktHCgKQ5\n" +
          "ORAzI4JMPJ+GslWYHb4phowim57iaztXOoJwTdwJx4nLCgdNbOhdjsnvzqvHu7Ur\n" +
          "TkXWStAmzOVyyghqpZXjFaH3pO3JLF+l+/+sKAIuvtd7u+Nxe5AW0wdeRlN8NwdC\n" +
          "jNPElpzVmbUq4JUagEiuTDkHzsxHpFKVK7q4+63SM1N95R1NbdWhscdCb+ZAJzVc\n" +
          "oyi3B43njTOQ5yOf+1CceWxG1bQVs5ZufpsMljq4Ui0/1lvh+wjChP4kqKOJ2qxq\n" +
          "4RgqsahDYVvTH9w7jXbyLeiNdd8XM2w9U/t7y0Ff/9yi0GE44Za4rF2LN9d11TPA\n" +
          "mRGunUHBcnWEvgJBQl9nJEiU0Zsnvgc/ubhPgXRR4Xq37Z0j4r7g1SgEEzwxA57d\n" +
          "emyPxgcYxn/eR44/KJ4EBs+lVDR3veyJm+kXQ99b21/+jh5Xos1AnX5iItreGCc=\n" +
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

  private String apiKey = "";
  private boolean invert = true;
  private int size = 256;
  private String token;         // MIT Generated access token
  private SSLSocketFactory sslSockFactory;  // Socket Factory for using SSL

  private static final String IMAGEBOT_SERVICE_URL = AppInventorFeatures.chatBotHost() + "image/v1";

  public ImageBot(ComponentContainer container) {
    super(container.$form());
  }

  /**
   * The MIT Access token to use. MIT App Inventor will automatically fill this
   * value in. You should not need to change it.
   */

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty(description = "The MIT Access token to use. MIT App Inventor will automatically "
      + "fill this value in. You should not need to change it.",
    userVisible = true, category = PropertyCategory.ADVANCED)
  public void Token(String token) {
    this.token = token;
  }

  /**
   * Specifies the ApiKey used to authenticate with the ImageBot.
   *
   * @param apiKey the API key to use for requests
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void ApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  /**
   * Specifies whether the mask used for editing should have its alpha channel inverted.
   *
   * @param invert true if the alpha channel should be inverted, otherwise false
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
      defaultValue = "True")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void InvertMask(boolean invert) {
    this.invert = invert;
  }

  @SimpleProperty
  public boolean InvertMask() {
    return invert;
  }

  /**
   * Specifies the size of the generated image. Can be one of 256, 512, or 1024.
   *
   * @param size the desired image size
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER,
      defaultValue = "256")
  @SimpleProperty(category = PropertyCategory.BEHAVIOR)
  public void Size(int size) {
    this.size = size;
  }

  @SimpleProperty
  public int Size() {
    return size;
  }

  // region Methods

  /**
   * Create an image using the given description.
   *
   * @param description a description of the image to create
   */
  @SimpleFunction
  public void CreateImage(final String description) {
    AsynchUtil.runAsynchronously(new Runnable() {
      @Override
      public void run() {
        doCreateImage(description);
      }
    });
  }

  /**
   * Edit the source image using the given description. Editable areas of the image should have
   * a transparent alpha. The source can be a Canvas component, an Image component, or a string
   * representing the path to a file.
   *
   * @param source      the source image
   * @param description the description of how to edit the image
   */
  @SimpleFunction
  @Deprecated
  public void EditImage(Object source, final String description) {
    try {
      // Load the image on the main thread. This isn't ideal but prevents the image from being
      // edited after this method is called but before the image is copied.
      final Bitmap bitmap = loadImage(source);
      if (bitmap != null) {
        AsynchUtil.runAsynchronously(new Runnable() {
          @Override
          public void run() {
            doEditImage(bitmap, null, description);
          }
        });
      } else {
        form.androidUIHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            ErrorOccurred(-1, "Invalid input to EditImage");
          }
        }, 0);
        throw new StopBlocksExecution();
      }
    } catch (IOException e) {
      Log.e(LOG_TAG, "Unable to read source image", e);
    }
  }

  /**
   * Edit the imageSource using the given description. The editable area of the image should be
   * indicated by the maskSource. The sources can be a Canvas, an Image, or a string
   * representing the path to a file.
   *
   * @param imageSource the source image
   * @param maskSource  the edit mask for the image
   * @param prompt      the description of how to edit the image
   */
  @SimpleFunction
  public void EditImageWithMask(Object imageSource, Object maskSource, final String prompt) {
    try {
      final Bitmap bitmap = loadImage(imageSource);
      final Bitmap mask = loadMask(maskSource);
      if (bitmap == null) {
        return;
      }
      if (mask == null) {
        return;
      }
      AsynchUtil.runAsynchronously(new Runnable() {
        @Override
        public void run() {
          doEditImage(bitmap, mask, prompt);
        }
      });
    } catch (IOException e) {
      Log.e(LOG_TAG, "Unable to read source image", e);
    }
  }

  // endregion

  // region Events

  /**
   * The ImageCreated event will be run when the ImageBot successfully creates an image.
   *
   * @param fileName the location of the created PNG file
   */
  @SimpleEvent
  public void ImageCreated(String fileName) {
    EventDispatcher.dispatchEvent(this, "ImageCreated", fileName);
  }

  /**
   * The ImageCreated event will be run when the ImageBot successfully edits an image.
   *
   * @param fileName the location of the edited PNG file.
   */
  @SimpleEvent
  public void ImageEdited(String fileName) {
    EventDispatcher.dispatchEvent(this, "ImageEdited", fileName);
  }

  /**
   * The ErrorOccurred event will be run when an error occurs during processing, such as if you
   * forget to provide an API key or the server is overloaded.
   *
   * @param responseCode the HTTP status code returned by the server
   * @param responseText a description of the error
   */
  @SimpleEvent
  public void ErrorOccurred(int responseCode, String responseText) {
    if (!EventDispatcher.dispatchEvent(this, "ErrorOccurred", responseCode, responseText)) {
      form.dispatchErrorOccurredEvent(ImageBot.this, "ErrorOccurred",
          ErrorMessages.ERROR_IMAGEBOT_ERROR, responseCode, responseText);
    }
  }

  private static class ImageException extends Exception {
    private final int code;
    private final String description;

    private ImageException(int code, String description, Throwable cause) {
      super(cause);
      this.code = code;
      this.description = description;
    }

    private String getResponseMessage() {
      return description;
    }

    private int getResponseCode() {
      return code;
    }
  }

  // endregion

  private void doCreateImage(String prompt) {
    try {
      String iToken;
      if (token != null && !token.equals("") && token.charAt(0) == '%') {
        iToken = token.substring(1);
      } else {
        iToken = token;
      }
      byte[] decodedToken = Base58Util.decode(iToken);
      ImageBotToken.token token = ImageBotToken.token.parseFrom(decodedToken);
      ImageBotToken.request.Builder builder = ImageBotToken.request.newBuilder()
          .setToken(token)
          .setSize("" + size)
          .setOperation(ImageBotToken.request.OperationType.CREATE)
          .setPrompt(prompt);
      if (apiKey != null && !apiKey.isEmpty()) {
        builder = builder.setApikey(apiKey);
      }
      ImageBotToken.request request = builder.build();
      try {
        final String response = sendRequest(request);
        form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            ImageCreated(response);
          }
        });
      } catch (final ImageException e) {
        Log.e(LOG_TAG, "Unable to create image", e);
        form.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            ErrorOccurred(e.getResponseCode(), e.getResponseMessage());
          }
        });
      }
    } catch (final Exception e) {
      Log.e(LOG_TAG, "Unable to create image", e);
      form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ErrorOccurred(404, e.toString());
        }
      });
    }
  }

  private void doEditImage(Bitmap source, Bitmap mask, String description) {
    ByteArrayOutputStream sourceBuffer = new ByteArrayOutputStream();
    source.compress(Bitmap.CompressFormat.PNG, 100, sourceBuffer);
    ByteString sourceString = ByteString.copyFrom(sourceBuffer.toByteArray());

    ByteString maskString = null;
    if (mask != null) {
      ByteArrayOutputStream maskBuffer = new ByteArrayOutputStream();
      mask.compress(Bitmap.CompressFormat.PNG, 100, maskBuffer);
      maskString = ByteString.copyFrom(maskBuffer.toByteArray());
    }

    String iToken;
    if (token != null && !token.equals("") && token.charAt(0) == '%') {
      iToken = token.substring(1);
    } else {
      iToken = token;
    }
    ImageBotToken.token token;
    try {
      byte[] decodedToken = Base58Util.decode(iToken);
      token = ImageBotToken.token.parseFrom(decodedToken);
    } catch (IOException e) {
      form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ErrorOccurred(403, "Invalid Token");
        }
      });
      return;
    }
    ImageBotToken.request.Builder builder = ImageBotToken.request.newBuilder()
        .setToken(token)
        .setSource(sourceString)
        .setOperation(ImageBotToken.request.OperationType.EDIT)
        .setSize("" + size)
        .setPrompt(description);
    if (apiKey != null && !apiKey.isEmpty()) {
      builder = builder.setApikey(apiKey);
    }
    if (maskString != null) {
      builder.setMask(maskString);
    }
    ImageBotToken.request request = builder.build();
    try {
      final String response = sendRequest(request);
      form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ImageEdited(response);
        }
      });
    } catch (final ImageException e) {
      Log.e(LOG_TAG, "Unable to edit image", e);
      form.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          ErrorOccurred(e.getResponseCode(), e.getResponseMessage());
        }
      });
    }
  }

  private String sendRequest(ImageBotToken.request request) throws ImageException {
    HttpsURLConnection connection = null;
    ensureSslSockFactory();
    int responseCode = -1;     // This means the connection never succeeded
    try {
      URL url = new URL(IMAGEBOT_SERVICE_URL);
      connection = (HttpsURLConnection) url.openConnection();
      if (connection != null) {
        connection.setSSLSocketFactory(sslSockFactory);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        request.writeTo(connection.getOutputStream());
        responseCode = connection.getResponseCode();
        if (responseCode == 200) {
          ImageBotToken.response response = ImageBotToken.response.parseFrom(
            connection.getInputStream());
          byte[] imageData = response.getImage().toByteArray();
          File outFile = getOutputFile();
          FileOutputStream out = new FileOutputStream(outFile);
          try {
            out.write(imageData);
            out.flush();
          } finally {
            out.close();
          }
          return Uri.fromFile(outFile).toString();
        }
        String errorMessage = IOUtils.readStreamAsString(connection.getErrorStream());
        throw new ImageException(responseCode, errorMessage, null);
      }
    } catch (IOException e) {
      Log.e(LOG_TAG, "Got an IOException", e);
      throw new ImageException(responseCode, e.toString(), e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
    throw new ImageException(404, "Could not connect to proxy server", null);
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

  private Bitmap loadMask(Object mask) throws IOException {
    Bitmap bitmap = loadImage(mask);
    if (invert) {
      // Invert the alpha channel
      ColorMatrix transform = new ColorMatrix(new float[] {
          0, 0, 0, 0, 0,
          0, 0, 0, 0, 0,
          0, 0, 0, 0, 0,
          0, 0, 0, -1, 255
      });
      ColorMatrixColorFilter filter = new ColorMatrixColorFilter(transform);
      Paint paint = new Paint();
      paint.setColorFilter(filter);
      Bitmap newBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
      android.graphics.Canvas canvas = new android.graphics.Canvas(newBitmap);
      canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
      bitmap = newBitmap;
    }
    return bitmap;
  }

  private File getOutputFile() throws IOException {
    String tempdir = FileUtil.resolveFileName(form, "", form.DefaultFileScope());
    if (tempdir.startsWith("file://")) {
      tempdir = tempdir.substring(7);
    } else if (tempdir.startsWith("file:")) {
      tempdir = tempdir.substring(5);
    }
    Log.d(LOG_TAG, "tempdir = " + tempdir);
    File outFile = File.createTempFile("ImageBot", ".png", new File(tempdir));
    Log.d(LOG_TAG, "outfile = " + outFile);
    return outFile;
  }

  // We are synchronized because we are called simultaneously from two
  // different threads. Rather than do the work twice, the first one
  // does the work and the second one waits!
  private synchronized void ensureSslSockFactory() {
    if (sslSockFactory == null) {
      try {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream caInput = new ByteArrayInputStream(
            COMODO_ROOT.getBytes(StandardCharsets.UTF_8));
        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();
        caInput = new ByteArrayInputStream(COMODO_USRTRUST.getBytes(StandardCharsets.UTF_8));
        Certificate inter = cf.generateCertificate(caInput);
        caInput.close();
        caInput = new ByteArrayInputStream(MIT_CA.getBytes(StandardCharsets.UTF_8));
        Certificate mitca = cf.generateCertificate(caInput);
        caInput.close();
        caInput = new ByteArrayInputStream(ISRG_ROOT_X1.getBytes(StandardCharsets.UTF_8));
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
          Log.d(LOG_TAG, "Added " + (count - 1) + " system certificates!");
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
        Log.e(LOG_TAG, "Could not setup SSL Trust Store for ImageBot", e);
        throw new YailRuntimeError("Could Not setup SSL Trust Store for ImageBot: ", e.getMessage());
      }
    }
  }

  /**
   * Get the list of root CA's trusted by this device.
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
