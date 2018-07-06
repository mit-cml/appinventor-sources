// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2017 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime;

import android.app.Activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Handler;

import android.util.Base64;
import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;

import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;

import com.google.appinventor.components.runtime.errors.YailRuntimeError;

import com.google.appinventor.components.runtime.util.CloudDBJedisListener;
import com.google.appinventor.components.runtime.util.JsonUtil;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.exceptions.JedisNoScriptException;

/**
 * The CloudDB component stores and retrieves information in the Cloud using Redis, an
 * open source library. The component has methods to store a value under a tag and to
 * retrieve the value associated with the tag. It also possesses a listener to fire events
 * when stored values are changed. It also posseses a sync capability which helps CloudDB
 * to sync with data collected offline.
 *
 * @author manting@mit.edu (Natalie Lao)
 * @author joymitro1989@gmail.com (Joydeep Mitra)
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

@DesignerComponent(version = YaVersion.CLOUDDB_COMPONENT_VERSION,
    description = "Non-visible component allowing you to store data on a Internet " +
        "connected database server (using Redis software). This allows the users of " +
        "your App to share data with each other. " +
        "By default data will be stored in a server maintained by MIT, however you " +
        "can setup and run your own server. Set the \"RedisServer\" property and " +
        "\"RedisPort\" Property to access your own server.",
    designerHelpDescription = "Non-visible component that communicates with CloudDB " +
        "server to store and retrieve information.",
    category = ComponentCategory.EXPERIMENTAL,
    nonVisible = true,
    iconName = "images/cloudDB.png")
@UsesPermissions(permissionNames = "android.permission.INTERNET," +
  "android.permission.ACCESS_NETWORK_STATE")

@UsesLibraries(libraries = "jedis.jar")
public final class CloudDB extends AndroidNonvisibleComponent implements Component,
  OnClearListener, OnDestroyListener {
  private static final boolean DEBUG = false;
  private static final String LOG_TAG = "CloudDB";
  private boolean importProject = false;
  private String projectID = "";
  private String token = "";
  private boolean isPublic = false;

  private volatile boolean dead = false; // On certain fatal errors we declare ourselves
                                         // "dead" which means an application restart
                                         // is required to get things going again.
                                         // For now, only an authentication error
                                         // sets this

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

  // Digital Signature Trust Root X3 -- For Letsencrypt

  private static final String DST_ROOT_X3 =
    "-----BEGIN CERTIFICATE-----\n" +
    "MIIDSjCCAjKgAwIBAgIQRK+wgNajJ7qJMDmGLvhAazANBgkqhkiG9w0BAQUFADA/\n" +
    "MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMT\n" +
    "DkRTVCBSb290IENBIFgzMB4XDTAwMDkzMDIxMTIxOVoXDTIxMDkzMDE0MDExNVow\n" +
    "PzEkMCIGA1UEChMbRGlnaXRhbCBTaWduYXR1cmUgVHJ1c3QgQ28uMRcwFQYDVQQD\n" +
    "Ew5EU1QgUm9vdCBDQSBYMzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB\n" +
    "AN+v6ZdQCINXtMxiZfaQguzH0yxrMMpb7NnDfcdAwRgUi+DoM3ZJKuM/IUmTrE4O\n" +
    "rz5Iy2Xu/NMhD2XSKtkyj4zl93ewEnu1lcCJo6m67XMuegwGMoOifooUMM0RoOEq\n" +
    "OLl5CjH9UL2AZd+3UWODyOKIYepLYYHsUmu5ouJLGiifSKOeDNoJjj4XLh7dIN9b\n" +
    "xiqKqy69cK3FCxolkHRyxXtqqzTWMIn/5WgTe1QLyNau7Fqckh49ZLOMxt+/yUFw\n" +
    "7BZy1SbsOFU5Q9D8/RhcQPGX69Wam40dutolucbY38EVAjqr2m7xPi71XAicPNaD\n" +
    "aeQQmxkqtilX4+U9m5/wAl0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNV\n" +
    "HQ8BAf8EBAMCAQYwHQYDVR0OBBYEFMSnsaR7LHH62+FLkHX/xBVghYkQMA0GCSqG\n" +
    "SIb3DQEBBQUAA4IBAQCjGiybFwBcqR7uKGY3Or+Dxz9LwwmglSBd49lZRNI+DT69\n" +
    "ikugdB/OEIKcdBodfpga3csTS7MgROSR6cz8faXbauX+5v3gTt23ADq1cEmv8uXr\n" +
    "AvHRAosZy5Q6XkjEGB5YGV8eAlrwDPGxrancWYaLbumR9YbK+rlmM6pZW87ipxZz\n" +
    "R8srzJmwN0jP41ZL9c8PDHIyh8bwRLtTcm1D9SZImlJnt1ir/md2cXjbDaJWFBM5\n" +
    "JDGFoqgCWjBH4d1QB7wCCZAA62RjYJsWvIjJEubSfZGL+T0yjWW06XyxV3bqxbYo\n" +
    "Ob8VZRzI9neWagqNdwvYkQsEjgfbKbYK7p2CNTUQ\n" +
    "-----END CERTIFICATE-----\n";

  private String defaultRedisServer = null;
  private boolean useDefault = true;

  private Handler androidUIHandler;
  private final Activity activity;

  private Jedis INSTANCE = null;
  private volatile String redisServer = "DEFAULT";
  private volatile int redisPort;
  private volatile boolean useSSL = true;
  private volatile boolean shutdown = false; // Should this instance of CloudDB
                                             // stop?

  private SSLSocketFactory SslSockFactory = null; // Socket Factory for using
                                                  // SSL

  private volatile CloudDBJedisListener currentListener;
  private volatile boolean listenerRunning = false;

  // To avoid blocking the UI thread, we do most Jedis operations in the background.
  // Rather then spawning a new thread for each request, we use an ExcutorService with
  // a single background thread to perform all the Jedis work. Using a single thread
  // also means that we can share a single Jedis connection and not worry about thread
  // synchronization.

  private volatile ExecutorService background = Executors.newSingleThreadExecutor();

  // Store can be called frequenly and quickly in some situations. For example
  // using store inside of a Canvas Drag event (for realtime updating of a remote
  // canvas). Or in a handler for the Accelerometer (gasp!). To make storing as
  // effecient as possible, we have a queue of pending store requests and we
  // have a background task that drains this queue as fast as possible and
  // iterates over the queue until it is drained.
  private final List<storedValue> storeQueue = Collections.synchronizedList(new ArrayList());

  private ConnectivityManager cm;

  private static class storedValue {
    private String tag;
    private JSONArray  valueList;
    storedValue(String tag, JSONArray valueList) {
      this.tag = tag;
      this.valueList = valueList;
    }

    public String getTag() {
      return tag;
    }

    public JSONArray getValueList() {
      return valueList;
    }
  }

  /**
   * Creates a new CloudDB component.
   * @param container the Form that this component is contained in.
   */
  public CloudDB(ComponentContainer container) {
    super(container.$form());
    // We use androidUIHandler when we set up operations that run asynchronously
    // in a separate thread, but which themselves want to cause actions
    // back in the UI thread.  They do this by posting those actions
    // to androidUIHandler.
    androidUIHandler = new Handler();
    this.activity = container.$context();
    //Defaults set in MockCloudDB.java in appengine/src/com/google/appinventor/client/editor/simple/components
    projectID = ""; // set in Designer
    token = ""; //set in Designer

    redisPort = 6381;
    cm = (ConnectivityManager) form.$context().getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
  }

  /**
   * Initialize: Do runtime initialization of CloudDB
   */
  public void Initialize() {
    if (DEBUG) {
      Log.d(LOG_TAG, "Initalize called!");
    }
    if (currentListener == null) { // currentListener may still be set
      startListener();             // in the Companion
    }
    form.registerForOnClear(this); // So we are notified when (clear-current-form)
                                   // is called.
    form.registerForOnDestroy(this); // close our Redis connections when we are leaving
  }

  private void stopListener() {
    // We do this on the UI thread to make sure it is complete
    // before we repoint the redis server (or port)
    if (DEBUG) {
      Log.d(LOG_TAG, "Listener stopping!");
    }
    if (currentListener != null) {
      currentListener.terminate();
      currentListener = null;
      listenerRunning = false;
    }
  }

  /*
   * onClear() -- Called when (clear-current-form) is invoked
   *
   */
  @Override
  public void onClear() {
    shutdown = true;            // Tell the listener to stop trying
    flushJedis(false);          // to restart
    if (DEBUG) {
      Log.d(LOG_TAG, "onClear() called");
    }
  }

  @Override
  public void onDestroy() {
    if (DEBUG) {
      Log.d(LOG_TAG, "onDestroy() called");
    }
    onClear();
  }

  private synchronized void startListener() {
    // Retrieve new posts as they are added to the CloudDB.
    // Note: We use a real thread here rather then the background executor
    // because this thread will run effectively forever
    if (listenerRunning) {
      if (DEBUG) {
        Log.d(LOG_TAG, "StartListener while already running, no action taken");
      }
      return;
    }
    listenerRunning = true;
    if (DEBUG) {
      Log.d(LOG_TAG, "Listener starting!");
    }
    Thread t = new Thread() {
        public void run() {
          Jedis jedis = getJedis(true);
          if (jedis != null) {
            try {
              currentListener = new CloudDBJedisListener(CloudDB.this);
              jedis.subscribe(currentListener, projectID);
            } catch (Exception e) {
              Log.e(LOG_TAG, "Error in listener thread", e);
              try {
                jedis.close();
              } catch (Exception ee) {
                // XXX
              }
              if (DEBUG) {
                Log.d(LOG_TAG, "Listener: connection to Redis failed, sleeping 3 seconds.");
              }
              try {
                Thread.sleep(3*1000);
              } catch (InterruptedException ee) {
              }
              if (DEBUG) {
                Log.d(LOG_TAG, "Woke up!");
              }
            }
          } else {
            if (DEBUG) {
              Log.d(LOG_TAG, "Listener: getJedis(true) returned null, retry in 3...");
            }
            try {
              Thread.sleep(3*1000);
            } catch (InterruptedException e) {
            }
            if (DEBUG) {
              Log.d(LOG_TAG, "Woke up! (2)");
            }
          }
          listenerRunning = false;
          if (!dead && !shutdown) {
            startListener();
          } else {
            if (DEBUG) {
              Log.d(LOG_TAG, "We are dead, listener not retrying.");
            }
          }
        }
      };
    t.start();
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
    defaultValue = "DEFAULT")
  public void RedisServer(String servername) {
    if (servername.equals("DEFAULT")) {
      if (!useDefault) {
        useDefault = true;
        if (defaultRedisServer == null) { // Not setup yet
          if (DEBUG) {
            Log.d(LOG_TAG, "RedisServer called before defaultServer (should not happen!)");
          }
        } else {
          redisServer = defaultRedisServer;
        }
        flushJedis(true);           // Re-initialize any existing connections
      }
    } else {
      useDefault = false;
      if (!servername.equals(redisServer)) {
        redisServer = servername;
        flushJedis(true);           // Re-initialize any existing connections
      }
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The Redis Server to use to store data. A setting of \"DEFAULT\" " +
          "means that the MIT server will be used.")
  public String RedisServer() {
    if (redisServer.equals(defaultRedisServer)) {
      return "DEFAULT";
    } else {
      return redisServer;
    }
  }

  // This is a non-documented property because it is hidden in the
  // UI. Its purpose in life is to transmit the default redis server
  // from the system into the Companion or packaged app. The Default
  // server is set in appengine-web.xml (the clouddb.server property). It
  // is sent to the client from the server via the system config call.

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING)
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
    description = "The Default Redis Server to use.",
    userVisible = false)
  public void DefaultRedisServer(String server) {
    defaultRedisServer = server;
    if (useDefault) {
      redisServer = server;
    }
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER,
    defaultValue = "6381")
  public void RedisPort(int port) {
    if (port != redisPort) {
      redisPort = port;
      flushJedis(true);
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "The Redis Server port to use. Defaults to 6381")
  public int RedisPort() {
    return redisPort;
  }

  /**
   * Getter for the ProjectID.
   *
   * @return the ProjectID for this CloudDB project
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,
      description = "Gets the ProjectID for this CloudDB project.")
  public String ProjectID() {
    checkProjectIDNotBlank();
    return projectID;
  }

  /**
   * Specifies the ID of this CloudDB project.
   *
   * @param id the project ID
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  public void ProjectID(String id) {
    if (!projectID.equals(id)) {
      projectID = id;
    }
    if (projectID.equals("")){
      throw new RuntimeException("CloudDB ProjectID property cannot be blank.");
    }
  }

  /**
   * Specifies the Token Signature of this CloudDB project.
   *
   * @param authToken for CloudDB server
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
          defaultValue = "")
  public void Token(String authToken) {
    if (!token.equals(authToken)) {
      token = authToken;
    }
    if (token.equals("")){
      throw new RuntimeException("CloudDB Token property cannot be blank.");
    }
  }

  /**
   * Getter for the authTokenSignature.
   *
   * @return the authTokenSignature for this CloudDB project
   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false,
          description = "This field contains the authentication token used to login to " +
              "the backed Redis server. For the \"DEFAULT\" server, do not edit this " +
              "value, the system will fill it in for you. A system administrator " +
              "may also provide a special value to you which can be used to share " +
              "data between multiple projects from multiple people. If using your own " +
              "Redis server, set a password in the server's config and enter it here.")
  public String Token() {
    checkProjectIDNotBlank();
    return token;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN,
           defaultValue = "True")
  public void UseSSL(boolean useSSL) {
    if (this.useSSL != useSSL) {
      this.useSSL = useSSL;
      flushJedis(true);             // Re-initialize any connections
    }
  }

  @SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false,
          description = "Set to true to use SSL to talk to CloudDB/Redis server. " +
              "This should be set to True for the \"DEFAULT\" server.")
  public boolean UseSSL() {
    return useSSL;
  }

  private static final String SET_SUB_SCRIPT =
    "local key = KEYS[1];" +
    "local value = ARGV[1];" +
    "local topublish = cjson.decode(ARGV[2]);" +
    "local project = ARGV[3];" +
    "local newtable = {};" +
    "table.insert(newtable, key);" +
    "table.insert(newtable, topublish);" +
    "redis.call(\"publish\", project, cjson.encode(newtable));" +
    "return redis.call('set', project .. \":\" .. key, value);";

  private static final String SET_SUB_SCRIPT_SHA1 = "765978e4c340012f50733280368a0ccc4a14dfb7";

  /**
   * Asks CloudDB to store the given value under the given tag.
   *
   * @param tag The tag to use
   * @param valueToStore The value to store. Can be any type of value (e.g.
   * number, text, boolean or list).
   */
  @SimpleFunction(description = "Store a value at a tag.")
  public void StoreValue(final String tag, final Object valueToStore) {
    checkProjectIDNotBlank();

    final String value;
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    boolean isConnected = networkInfo != null && networkInfo.isConnected();

    try {
      if (valueToStore != null) {
        String strval = valueToStore.toString();
        if (strval.startsWith("file:///") || strval.startsWith("/storage")) {
          value = JsonUtil.getJsonRepresentation(readFile(strval));
        } else {
          value = JsonUtil.getJsonRepresentation(valueToStore);
        }
      } else {
        value = "";
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
    }

    if (isConnected) {
      if (DEBUG) {
        Log.d(LOG_TAG,"Device is online...");
      }
      synchronized(storeQueue) {
        boolean kickit = false;
        if (storeQueue.size() == 0) { // Need to kick off the background task
          if (DEBUG) {
            Log.d(LOG_TAG, "storeQueue is zero length, kicking background");
          }
          kickit = true;
        } else {
          if (DEBUG) {
            Log.d(LOG_TAG, "storeQueue has " + storeQueue.size() + " entries");
          }
        }
        JSONArray valueList = new JSONArray();
        try {
          valueList.put(0, value);
        } catch (JSONException e) {
          throw new YailRuntimeError("JSON Error putting value.", "value is not convertable");
        }
        storedValue work  = new storedValue(tag, valueList);
        storeQueue.add(work);
        if (kickit) {
          background.submit(new Runnable() {
              public void run() {
                JSONArray pendingValueList = null;
                String pendingTag = null;
                String pendingValue = null;
                try {
                  storedValue work;
                  if (DEBUG) {
                    Log.d(LOG_TAG, "store background task running.");
                  }
                  while (true) {
                    synchronized(storeQueue) {
                      if (DEBUG) {
                        Log.d(LOG_TAG, "store: In background synchronized block");
                      }
                      int size = storeQueue.size();
                      if (size == 0) {
                        if (DEBUG) {
                          Log.d(LOG_TAG, "store background task exiting.");
                        }
                        work = null;
                      } else {
                        if (DEBUG) {
                          Log.d(LOG_TAG, "store: storeQueue.size() == " + size);
                        }
                        work = storeQueue.remove(0);
                        if (DEBUG) {
                          Log.d(LOG_TAG, "store: got work.");
                        }
                      }
                    }
                    if (DEBUG) {
                      Log.d(LOG_TAG, "store: left synchronized block");
                    }
                    if (work == null) {
                      try {
                        if (pendingTag != null) {
                          String jsonValueList = pendingValueList.toString();
                          if (DEBUG) {
                            Log.d(LOG_TAG, "Workqueue empty, sending pendingTag, valueListLength = " + pendingValueList.length());
                          }
                          jEval(SET_SUB_SCRIPT, SET_SUB_SCRIPT_SHA1, 1, pendingTag, pendingValue, jsonValueList, projectID);
                        }
                      } catch (JedisException e) {
                        CloudDBError(e.getMessage());
                        flushJedis(true);
                      }
                      return;
                    }

                    String tag = work.getTag();
                    JSONArray valueList = work.getValueList();
                    if (tag == null || valueList == null) {
                      if (DEBUG) {
                        Log.d(LOG_TAG, "Either tag or value is null!");
                      }
                    } else {
                      if (DEBUG) {
                        Log.d(LOG_TAG, "Got Work: tag = " + tag + " value = " + valueList.get(0));
                      }
                    }
                    if (pendingTag == null) { // First time through this invocation
                      pendingTag = tag;
                      pendingValueList = valueList;
                      pendingValue = valueList.getString(0);
                    } else if (pendingTag.equals(tag)) { // work is for the same tag
                      pendingValue = valueList.getString(0);
                      pendingValueList.put(pendingValue);
                    } else {    // Work is for a different tag, send what we have
                      try {     // and add the new tag,incoming valuelist for the next round
                        String jsonValueList = pendingValueList.toString();
                        if (DEBUG) {
                          Log.d(LOG_TAG, "pendingTag changed sending pendingTag, valueListLength = " + pendingValueList.length());
                        }
                        jEval(SET_SUB_SCRIPT, SET_SUB_SCRIPT_SHA1, 1, pendingTag, pendingValue, jsonValueList, projectID);
                      } catch (JedisException e) {
                        CloudDBError(e.getMessage());
                        flushJedis(true);
                        storeQueue.clear(); // Flush pending changes, we are in
                        return;             // an error state
                      }
                      pendingTag = tag;
                      pendingValueList = valueList;
                      pendingValue = valueList.getString(0);
                    }
                  }
                } catch (Exception e) {
                Log.e(LOG_TAG, "Exception in store worker!", e);
                }
              }
            });
        }
      }
    } else {
      CloudDBError("Cannot store values off-line.");
    }
  }

  /**
   * GetValue asks CloudDB to get the value stored under the given tag.
   * It will pass valueIfTagNotThere to GotValue if there is no value stored
   * under the tag.
   *
   * @param tag The tag whose value is to be retrieved.
   * @param valueIfTagNotThere The value to pass to the event if the tag does
   *                           not exist.
   */
  @SimpleFunction(description = "Get the Value for a tag, doesn't return the " +
    "value but will cause a GotValue event to fire when the " +
    "value is looked up.")
  public void GetValue(final String tag, final Object valueIfTagNotThere) {
    checkProjectIDNotBlank();
    if (DEBUG) {
      Log.d(LOG_TAG, "getting value ... for tag: " + tag);
    }
    final AtomicReference<Object> value = new AtomicReference<Object>();
    Cursor cursor = null;
    SQLiteDatabase db = null;
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    boolean isConnected = networkInfo != null && networkInfo.isConnected();

    if (isConnected) {
      // Set value to either the JSON from the CloudDB
      // or the JSON representation of valueIfTagNotThere
      background.submit(new Runnable() {
          public void run() {
            Jedis jedis = getJedis();
            try {
              if (DEBUG) {
                Log.d(LOG_TAG,"about to call jedis.get()");
              }
              String returnValue = jedis.get(projectID + ":" + tag);
              if (DEBUG) {
                Log.d(LOG_TAG, "finished call jedis.get()");
              }
              if (returnValue != null) {
                String val = JsonUtil.getJsonRepresentationIfValueFileName(returnValue);
                if(val != null) value.set(val);
                else value.set(returnValue);
              }
              else {
                if (DEBUG) {
                  Log.d(CloudDB.LOG_TAG,"Value retrieved is null");
                }
                value.set(JsonUtil.getJsonRepresentation(valueIfTagNotThere));
              }
            } catch (JSONException e) {
              CloudDBError("JSON conversion error for " + tag);
              return;
            } catch (NullPointerException e) {
              CloudDBError("System Error getting tag " + tag);
              flushJedis(true);
              return;
            } catch (JedisException e) {
              Log.e(LOG_TAG, "Exception in GetValue", e);
              CloudDBError(e.getMessage());
              flushJedis(true);
              return;
            }

            androidUIHandler.post(new Runnable() {
                public void run() {
                  // Signal an event to indicate that the value was
                  // received.  We post this to run in the Application's main
                  // UI thread.
                  GotValue(tag, value.get());
                }
              });
          }
        });
    } else {
      if (DEBUG) {
        Log.d(LOG_TAG, "GetValue(): We're offline");
      }
      CloudDBError("Cannot fetch variables while off-line.");
    }
  }

  @SimpleFunction(description = "returns True if we are on the network and will likely " +
    "be able to connect to the CloudDB server.")
  public boolean CloudConnected() {
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    boolean isConnected = networkInfo != null && networkInfo.isConnected();
    return isConnected;
  }

  @SimpleEvent(description = "Event triggered by the \"RemoveFirstFromList\" function. The " +
    "argument \"value\" is the object that was the first in the list, and which is now " +
    "removed.")
  public void FirstRemoved(Object value) {
    if (DEBUG) {
      Log.d(CloudDB.LOG_TAG, "FirstRemoved: Value = " + value);
    }
    checkProjectIDNotBlank();
    try {
      if(value != null && value instanceof String) {
        value = JsonUtil.getObjectFromJson((String) value);
      }
    } catch (JSONException e) {
      Log.e(CloudDB.LOG_TAG,"error while converting to JSON...",e);
      return;
    }
    final Object sValue = value;
    androidUIHandler.post(new Runnable() {
        @Override
        public void run() {
          EventDispatcher.dispatchEvent(CloudDB.this, "FirstRemoved", sValue);
        }
      });
  }

  private static final String POP_FIRST_SCRIPT =
      "local key = KEYS[1];" +
      "local project = ARGV[1];" +
      "local currentValue = redis.call('get', project .. \":\" .. key);" +
      "local decodedValue = cjson.decode(currentValue);" +
      "local subTable = {};" +
      "local subTable1 = {};" +
      "if (type(decodedValue) == 'table') then " +
      "  local removedValue = table.remove(decodedValue, 1);" +
      "  local newValue = cjson.encode(decodedValue);" +
      "  redis.call('set', project .. \":\" .. key, newValue);" +
      "  table.insert(subTable, key);" +
      "  table.insert(subTable1, newValue);" +
      "  table.insert(subTable, subTable1);" +
      "  redis.call(\"publish\", project, cjson.encode(subTable));" +
      "  return cjson.encode(removedValue);" +
      "else " +
      "  return error('You can only remove elements from a list');" +
      "end";

  private static final String POP_FIRST_SCRIPT_SHA1 = "ed4cb4717d157f447848fe03524da24e461028e1";

  @SimpleFunction(description = "Return the first element of a list and atomically remove it. " +
    "If two devices use this function simultaneously, one will get the first element and the " +
    "the other will get the second element, or an error if there is no available element. " +
    "When the element is available, the \"FirstRemoved\" event will be triggered.")
  public void RemoveFirstFromList(final String tag) {
    checkProjectIDNotBlank();

    final String key = tag;

    background.submit(new Runnable() {
        public void run() {
          Jedis jedis = getJedis();
          try {
            FirstRemoved(jEval(POP_FIRST_SCRIPT, POP_FIRST_SCRIPT_SHA1, 1, key, projectID));
          } catch (JedisException e) {
            CloudDBError(e.getMessage());
            flushJedis(true);
          }
        }
      });
  }

  private static final String APPEND_SCRIPT =
      "local key = KEYS[1];" +
      "local toAppend = cjson.decode(ARGV[1]);" +
      "local project = ARGV[2];" +
      "local currentValue = redis.call('get', project .. \":\" .. key);" +
      "local newTable;" +
      "local subTable = {};" +
      "local subTable1 = {};" +
      "if (currentValue == false) then " +
      "  newTable = {};" +
      "else " +
      "  newTable = cjson.decode(currentValue);" +
      "  if not (type(newTable) == 'table') then " +
      "    return error('You can only append to a list');" +
      "  end " +
      "end " +
      "table.insert(newTable, toAppend);" +
      "local newValue = cjson.encode(newTable);" +
      "redis.call('set', project .. \":\" .. key, newValue);" +
      "table.insert(subTable1, newValue);" +
      "table.insert(subTable, key);" +
      "table.insert(subTable, subTable1);" +
      "redis.call(\"publish\", project, cjson.encode(subTable));" +
      "return newValue;";

  private static final String APPEND_SCRIPT_SHA1 = "d6cc0f65b29878589f00564d52c8654967e9bcf8";

  @SimpleFunction(description = "Append a value to the end of a list atomically. " +
    "If two devices use this function simultaneously, both will be appended and no " +
    "data lost.")
  public void AppendValueToList(final String tag, final Object itemToAdd) {
    checkProjectIDNotBlank();

    Object itemObject = new Object();
    try {
      if(itemToAdd != null) {
        itemObject = JsonUtil.getJsonRepresentation(itemToAdd);
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert to JSON.", "JSON Creation Error.");
    }

    final String item = (String) itemObject;
    final String key = tag;

    background.submit(new Runnable() {
        public void run() {
          Jedis jedis = getJedis();
          try {
            jEval(APPEND_SCRIPT, APPEND_SCRIPT_SHA1, 1, key, item, projectID);
          } catch(JedisException e) {
            CloudDBError(e.getMessage());
            flushJedis(true);
          }
        }
      });
  }

  /**
   * Indicates that a GetValue request has succeeded.
   *
   * @param value the value that was returned. Can be any type of value
   *              (e.g. number, text, boolean or list).
   */
  @SimpleEvent
  public void GotValue(String tag, Object value) {
    if (DEBUG) {
      Log.d(CloudDB.LOG_TAG, "GotValue: tag = " + tag + " value = " + (String) value);
    }
    checkProjectIDNotBlank();

    // We can get a null value is the Jedis connection failed in some way.
    // not sure what to do here, so we'll signal an error for now.
    if (value == null) {
      CloudDBError("Trouble getting " + tag + " from the server.");
      return;
    }

    try {
      if (DEBUG) {
        Log.d(LOG_TAG, "GotValue: Class of value = " + value.getClass().getName());
      }
      if(value != null && value instanceof String) {
        value = JsonUtil.getObjectFromJson((String) value);
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert from JSON.", "JSON Retrieval Error.");
    }

    // Invoke the application's "GotValue" event handler
    EventDispatcher.dispatchEvent(this, "GotValue", tag, value);
  }

  /**
   * Asks CloudDB to forget (delete or set to "null") a given tag.
   *
   * @param tag The tag to remove
   */
  @SimpleFunction(description = "Remove the tag from CloudDB")
  public void ClearTag(final String tag) {
    checkProjectIDNotBlank();
    background.submit(new Runnable() {
        public void run() {
          try {
            Jedis jedis = getJedis();
            jedis.del(projectID + ":" + tag);
          } catch (Exception e) {
            CloudDBError(e.getMessage());
            flushJedis(true);
          }
        }
      });
  }

  /**
   * GetTagList asks CloudDB to retrieve all the tags belonging to this project.
   *
   * The resulting list is returned in GotTagList
   */
  @SimpleFunction(description = "Get the list of tags for this application. " +
      "When complete a \"TagList\" event will be triggered with the list of " +
      "known tags.")
  public void GetTagList() {
    checkProjectIDNotBlank();
    NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    boolean isConnected = networkInfo != null && networkInfo.isConnected();
    if (isConnected) {
      background.submit(new Runnable() {
          public void run() {

            Jedis jedis = getJedis();
            Set<String> value = null;
            try {
              value = jedis.keys(projectID + ":*");
            } catch (JedisException e) {
              CloudDBError(e.getMessage());
              flushJedis(true);
              return;
            }
            final List<String> listValue = new ArrayList<String>(value);

            for(int i = 0; i < listValue.size(); i++){
              listValue.set(i, listValue.get(i).substring((projectID + ":").length()));
            }

            androidUIHandler.post(new Runnable() {
                @Override
                public void run() {
                  TagList(listValue);
                }
              });
          }
        });
    } else {
      CloudDBError("Not connected to the Internet, cannot list tags");
    }
  }

  /**
   * Indicates that a GetTagList request has succeeded.
   *
   * @param value the list of tags that was returned.
   */
  @SimpleEvent(description = "Event triggered when we have received the list of known tags. " +
      "Used with the \"GetTagList\" Function.")
  public void TagList(List<String> value) {
    checkProjectIDNotBlank();
    EventDispatcher.dispatchEvent(this, "TagList", value);
  }

  /**
   * Indicates that the data in the CloudDB project has changed.
   * Launches an event with the tag and value that have been updated.
   *
   * @param tag the tag that has changed.
   * @param value the new value of the tag.
   */
  @SimpleEvent
  public void DataChanged(final String tag, final Object value) {
    Object tagValue = "";
    try {
      if(value != null && value instanceof String) {
        tagValue = JsonUtil.getObjectFromJson((String) value);
      }
    } catch(JSONException e) {
      throw new YailRuntimeError("Value failed to convert from JSON.", "JSON Retrieval Error.");
    }
    final Object finalTagValue = tagValue;

    androidUIHandler.post(new Runnable() {
      public void run() {
        // Invoke the application's "DataChanged" event handler
        EventDispatcher.dispatchEvent(CloudDB.this, "DataChanged", tag, finalTagValue);
      }
    });
  }

  /**
   * Indicates that the communication with the CloudDB signaled an error.
   *
   * @param message the error message
   */
  @SimpleEvent(description = "Indicates that an error occurred while communicating " +
                   "with the CloudDB Redis server.")
  public void CloudDBError(final String message) {
    // Log the error message for advanced developers
    Log.e(LOG_TAG, message);
    androidUIHandler.post(new Runnable() {
        @Override
        public void run() {

          // Invoke the application's "CloudDBError" event handler
          boolean dispatched = EventDispatcher.dispatchEvent(CloudDB.this, "CloudDBError", message);
          if (!dispatched) {
            // If the handler doesn't exist, then put up our own alert
            new Notifier(form).ShowAlert("CloudDBError: " + message);
          }
        }
      });
  }

  private void checkProjectIDNotBlank(){
    if (projectID.equals("")){
      throw new RuntimeException("CloudDB ProjectID property cannot be blank.");
    }
    if(token.equals("")){
      throw new RuntimeException("CloudDB Token property cannot be blank");
    }
  }

  public Jedis getJedis(boolean createNew) {
    Jedis jedis;
    if (dead) {                 // If we are dead, we are dead!
      return null;
    }
    try {
      if (DEBUG) {
        Log.d(LOG_TAG, "getJedis(true): Attempting a new connection (createNew = " +
          createNew + " redisServer = " + redisServer + " redisPort = " +
          redisPort + " useSSL = " +
          useSSL);
      }
      if (useSSL) {             // Need to create a TrustManager and trust the Comodo
                                // Root certificate because it isn't present in older
                                // Android versions
        ensureSslSockFactory();
        jedis = new Jedis(redisServer, redisPort, true, SslSockFactory, null, null);
      } else {
        jedis = new Jedis(redisServer, redisPort, false);
      }
      if (DEBUG) {
        Log.d(LOG_TAG, "getJedis(true): Have new connection.");
      }
      // If the first character of the token is %, we toss it away
      // it is used by MockCloudDB.java to determine if the token should
      // be kept or fetched from the server when needed
      if (token.substring(0, 1).equals("%")) {
        jedis.auth(token.substring(1));
      } else {
        jedis.auth(token);
      }
      if (DEBUG) {
        Log.d(LOG_TAG, "getJedis(true): Authentication complete.");
      }
    } catch (JedisConnectionException e) {
      Log.e(LOG_TAG, "in getJedis()", e);
      CloudDBError(e.getMessage());
      return null;
    } catch (JedisDataException e) {
      // This is always an authentication error
      Log.e(LOG_TAG, "in getJedis()", e);
      CloudDBError(e.getMessage() + " CloudDB disabled, restart to re-enable.");
      dead = true;
      return null;
    }
    return jedis;
  }

  public synchronized Jedis getJedis() {
    if (INSTANCE == null) {
      INSTANCE = getJedis(true);
    }
    return INSTANCE;
  }

  /*
   * flushJedis -- Flush the singleton jedis connection. This is
   * used when we detect an error from jedis. It is possible that after
   * an error the jedis connection is in an invalid state (or closed) so
   * we want to make sure we get a new one the next time around!
   */

  private void flushJedis(boolean restartListener) {
    if (INSTANCE == null) {
      return;                   // Nothing to do
    }
    try {
      INSTANCE.close();         // Just in case we still have
                                // a connection
    } catch (Exception e) {
      // XXX
    }
    INSTANCE = null;
    // We are now going to kill the executor, as it may
    // have hung tasks. We do this on the UI thread as a
    // way to synchronize things.
    androidUIHandler.post(new Runnable() {
        public void run() {
          List <Runnable> tasks = background.shutdownNow();
          if (DEBUG) {
            Log.d(LOG_TAG, "Killing background executor, returned tasks = " + tasks);
          }
          background = Executors.newSingleThreadExecutor();
        }
      });

    stopListener();             // This is probably hosed to, so restart
    if (restartListener) {
      startListener();
    }
  }

 /**
   * Accepts a file name and returns a Yail List with two
   * elements. the first element is the file's extension (example:
   * jpg, gif, etc.). The second element is the base64 encoded
   * contents of the file. This function is suitable for reading
   * binary files such as sounds and images. The base64 contents can
   * then be stored with mechanisms oriented around text, such as
   * tinyDB, Fusion tables and Firebase.
   *
   * Written by Jeff Schiller (jis) for the BinFile Extension
   *
   * @param fileName
   * @returns YailList the list of the file extension and contents
   */
  private YailList readFile(String fileName) {
    try {
      String originalFileName = fileName;
      // Trim off file:// part if present
      if (fileName.startsWith("file://")) {
        fileName = fileName.substring(7);
      }
      if (!fileName.startsWith("/")) {
        throw new YailRuntimeError("Invalid fileName, was " + originalFileName, "ReadFrom");
      }
      File inputFile = new File(fileName);
      if (!inputFile.isFile()) {
        throw new YailRuntimeError("Cannot find file", "ReadFrom");
      }
      String extension = getFileExtension(fileName);
      FileInputStream inputStream = new FileInputStream(inputFile);
      byte [] content = new byte[(int)inputFile.length()];
      int bytesRead = inputStream.read(content);
      if (bytesRead != inputFile.length()) {
        throw new YailRuntimeError("Did not read complete file!", "Read");
      }
      inputStream.close();
      String encodedContent = Base64.encodeToString(content, Base64.DEFAULT);
      Object [] results = new Object[2];
      results[0] = "." + extension;
      results[1] = encodedContent;
      return YailList.makeList(results);
    } catch (FileNotFoundException e) {
      throw new YailRuntimeError(e.getMessage(), "Read");
    } catch (IOException e) {
      throw new YailRuntimeError(e.getMessage(), "Read");
    }
  }

  // Utility to get the file extension from a filename
  // Written by Jeff Schiller (jis) for the BinFile Extension
  private String getFileExtension(String fullName) {
    String fileName = new File(fullName).getName();
    int dotIndex = fileName.lastIndexOf(".");
    return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
  }

  public ExecutorService getBackground() {
    return background;
  }

  public Object jEval(String script, String scriptsha1, int argcount, String... args) throws JedisException {
    Jedis jedis = getJedis();
    try {
      return jedis.evalsha(scriptsha1, argcount, args);
    } catch (JedisNoScriptException e) {
      if (DEBUG) {
        Log.d(LOG_TAG, "Got a JedisNoScriptException for " + scriptsha1);
      }
      // This happens if the server doesn't have the script loaded
      // So we use regular eval, which should then cache the script
      return jedis.eval(script, argcount, args);
    }
  }

  // We are synchronized because we are called simultaneously from two
  // different threads. Rather then do the work twice, the first one
  // does the work and the second one waits!
  private synchronized void ensureSslSockFactory() {
    if (SslSockFactory != null) {
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
        caInput = new ByteArrayInputStream(DST_ROOT_X3.getBytes("UTF-8"));
        Certificate dstx3 = cf.generateCertificate(caInput);
        caInput.close();
        if (DEBUG) {
          Log.d(LOG_TAG, "comodo=" + ((X509Certificate) ca).getSubjectDN());
          Log.d(LOG_TAG, "inter=" + ((X509Certificate) inter).getSubjectDN());
          Log.d(LOG_TAG, "dstx3=" + ((X509Certificate) dstx3).getSubjectDN());
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
        keyStore.setCertificateEntry("dstx3", dstx3);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);
        // // DEBUG
        // Log.d(LOG_TAG, "And now for something completely different...");
        // X509TrustManager tm = (X509TrustManager) tmf.getTrustManagers()[0];
        // for (X509Certificate cert : tm.getAcceptedIssuers()) {
        //   Log.d(LOG_TAG, cert.getSubjectX500Principal().getName());
        // }
        // // END DEBUG
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);
        SslSockFactory = ctx.getSocketFactory();
      } catch (Exception e) {
        Log.e(LOG_TAG, "Could not setup SSL Trust Store for CloudDB", e);
        throw new YailRuntimeError("Could Not setup SSL Trust Store for CloudDB: ", e.getMessage());
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
