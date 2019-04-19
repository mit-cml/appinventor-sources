// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2018 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.content.Context;

import android.util.Log;

import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.ReplForm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.entity.StringEntity;
import org.apache.http.entity.StringEntity;

import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.webrtc.DataChannel.Buffer;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.Observer;
import org.webrtc.PeerConnection.SignalingState;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;


public class WebRTCNativeMgr {

  private static final boolean DEBUG = true;

  private static final String LOG_TAG = "AppInvWebRTC";
  private static final CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder();

  private ReplForm form;

  private PeerConnection peerConnection;
  /* We need to keep track of whether or not we have processed an element */
  /* Received from the rendezvous server. */
  private TreeSet<String> seenNonces = new TreeSet();
  private boolean haveOffer = false;
  private String rCode;
  private volatile boolean keepPolling = true;
  private boolean first = true; // This is used for logging in the Rendezvous server
  private Random random = new Random();
  private DataChannel dataChannel = null;
  private String rendezvousServer = null; // Primary (first level) Rendezvous server
  private String rendezvousServer2 = null; // Second level (webrtc rendezvous) Rendezvous server
  private List<PeerConnection.IceServer> iceServers = new ArrayList();

  Timer timer = new Timer();

  /* Callback that handles sdp offer/answers */
  SdpObserver sdpObserver = new SdpObserver() {
      public void onCreateFailure(String str) {
      }

      public void onCreateSuccess(SessionDescription sessionDescription) {
        try {
          if (DEBUG) {
            Log.d(LOG_TAG, "sdp.type = " + sessionDescription.type.canonicalForm());
            Log.d(LOG_TAG, "sdp.description = " + sessionDescription.description);
          }
          DataChannel.Init init = new DataChannel.Init();
          if (sessionDescription.type == SessionDescription.Type.OFFER) {
            peerConnection.setRemoteDescription(sdpObserver, sessionDescription);
          } else if (sessionDescription.type == SessionDescription.Type.ANSWER) {
            peerConnection.setLocalDescription(sdpObserver, sessionDescription);
            /* Send to peer */
            JSONObject offer = new JSONObject();
            offer.put("type", "answer");
            offer.put("sdp", sessionDescription.description);
            JSONObject response = new JSONObject();
            response.put("offer", offer);
            sendRendezvous(response);
          }
        } catch (Exception e) {
          Log.e(LOG_TAG, "Exception during onCreateSuccess", e);
        }
      }

      public void onSetFailure(String str) {
      }

      public void onSetSuccess() {
      }
    };

  /* callback that handles iceCandidate negotiation */
  Observer observer = new Observer() {
      public void onAddStream(MediaStream mediaStream) {
      }

      public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreamArr) {
      }

      public void onDataChannel(DataChannel dataChannel) {
        if (DEBUG) {
          Log.d(LOG_TAG, "Have Data Channel!");
          Log.d(LOG_TAG, "v5");
        }
        WebRTCNativeMgr.this.dataChannel = dataChannel;
        dataChannel.registerObserver(dataObserver);
        keepPolling = false;    // Turn off talking to the rendezvous server
        timer.cancel();
        if (DEBUG) {
          Log.d(LOG_TAG, "Poller() Canceled");
        }
        seenNonces.clear();
      }

      public void onIceCandidate(IceCandidate iceCandidate) {
        try {
          if (DEBUG) {
            Log.d(LOG_TAG, "IceCandidate = " + iceCandidate.toString());
          }
          /* Send to Peer */
          JSONObject response = new JSONObject();
          response.put("nonce", random.nextInt(100000));
          JSONObject jsonCandidate = new JSONObject();
          jsonCandidate.put("candidate", iceCandidate.sdp);
          jsonCandidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
          jsonCandidate.put("sdpMid", iceCandidate.sdpMid);
          response.put("candidate", jsonCandidate);
          sendRendezvous(response);
        } catch (Exception e) {
          Log.e(LOG_TAG, "Exception during onIceCandidate", e);
        }
      }

      public void onIceCandidatesRemoved(IceCandidate[] iceCandidateArr) {
      }

      public void onIceConnectionChange(IceConnectionState iceConnectionState) {
      }

      public void onIceConnectionReceivingChange(boolean z) {
      }

      public void onIceGatheringChange(IceGatheringState iceGatheringState) {
      }

      public void onRemoveStream(MediaStream mediaStream) {
      }

      public void onRenegotiationNeeded() {
      }

      public void onSignalingChange(SignalingState signalingState) {
      }
    };

  /* Callback to process incoming data from the browser */
  DataChannel.Observer dataObserver = new DataChannel.Observer() {
      public void onBufferedAmountChange(long j) {
      }

      public void onMessage(Buffer buffer) {
        String input;
        try {
          input = utf8Decoder.decode(buffer.data).toString();
        } catch (CharacterCodingException e) {
          Log.e(LOG_TAG, "onMessage decoder error", e);
          return;
        }
        if (DEBUG) {
          Log.d(LOG_TAG, "onMessage: received: " + input);
        }
        form.evalScheme(input);
      }

      public void onStateChange() {
      }
    };

  public WebRTCNativeMgr(String rendezvousServer, String rendezvousResult) {
    this.rendezvousServer = rendezvousServer;
    if (rendezvousResult.isEmpty() || rendezvousResult.startsWith("OK")) {
      /* Provide a default when the rendezvous server doesn't provide one */
      rendezvousResult = "{\"rendezvous2\" : \"" + YaVersion.RENDEZVOUS_SERVER + "\"," +
        "\"iceservers\" : " +
        "[{ \"server\" : \"turn:turn.appinventor.mit.edu:3478\"," +
        "\"username\" : \"oh\"," +
        "\"password\" : \"boy\"}]}";
    }
    try {
      JSONObject resultJson = new JSONObject(rendezvousResult);
      this.rendezvousServer2 = resultJson.getString("rendezvous2");
      JSONArray iceServerArray = resultJson.getJSONArray("iceservers");
      this.iceServers = new ArrayList(iceServerArray.length());
      for (int i = 0; i < iceServerArray.length(); i++) {
        JSONObject jsonServer = iceServerArray.getJSONObject(i);
        PeerConnection.IceServer.Builder builder = PeerConnection.IceServer.builder(jsonServer.getString("server"));
        if (DEBUG) {
          Log.d(LOG_TAG, "Adding iceServer = " + jsonServer.getString("server"));
        }
        if (jsonServer.has("username")) {
          builder.setUsername(jsonServer.getString("username"));
        }
        if (jsonServer.has("password")) {
          builder.setPassword(jsonServer.getString("password"));
        }
        this.iceServers.add(builder.createIceServer());
      }
    } catch (JSONException e) {
      Log.e(LOG_TAG, "parsing iceServers:", e);
    }
  }

  public void initiate(ReplForm form, Context context, String code) {

    this.form = form;
    rCode = code;
    /* Initialize WebRTC globally */
    PeerConnectionFactory.initializeAndroidGlobals(context, false);
    /* Setup factory options */
    PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
    /* Create the factory */
    PeerConnectionFactory factory = new PeerConnectionFactory(options);
    /* Create the peer connection using the iceServers we received in the constructor */
    peerConnection = factory.createPeerConnection(iceServers, new MediaConstraints(),
      observer);
    timer.scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          Poller();
        }
      }, 0, 1000);              // Start the Poller now and then every second
  }

  /*
   * startPolling: poll the Rendezvous server looking for the information via the
   * the provided code with "-s" appended (because we are the receiver, replmgr.js
   * is in the sender roll.
   */
  private void Poller() {
    try {
      if (!keepPolling) {
        return;
      }

      if (DEBUG) {
        Log.d(LOG_TAG, "Poller() Called");
        Log.d(LOG_TAG, "Poller: rendezvousServer2 = " + rendezvousServer2);
      }
      HttpClient client = new DefaultHttpClient();
      HttpGet request = new HttpGet("http://" + rendezvousServer2 + "/rendezvous2/" + rCode + "-s");
      HttpResponse response = client.execute(request);
      StringBuilder sb = new StringBuilder();

      BufferedReader rd = null;
      try {
        rd = new BufferedReader
          (new InputStreamReader(
            response.getEntity().getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
          sb.append(line);
        }
      } finally {
        if (rd != null) {
          rd.close();
        }
      }

      if (!keepPolling) {
        if (DEBUG) {
          Log.d(LOG_TAG, "keepPolling is false, we're done!");
        }
        return;
      }

      String responseText = sb.toString();

      if (DEBUG) {
        Log.d(LOG_TAG, "response = " + responseText);
      }

      if (responseText.equals("")) {
        if (DEBUG) {
          Log.d(LOG_TAG, "Received an empty response");
        }
        // Empty Response
        return;
      }

      JSONArray jsonArray = new JSONArray(responseText);
      if (DEBUG) {
        Log.d(LOG_TAG, "jsonArray.length() = " + jsonArray.length());
      }
      int i = 0;
      while (i < jsonArray.length()) {
        if (DEBUG) {
          Log.d(LOG_TAG, "i = " + i);
          Log.d(LOG_TAG, "element = " + jsonArray.optString(i));
        }
        JSONObject element = (JSONObject) jsonArray.get(i);
        if (!haveOffer) {
          if (!element.has("offer")) {
            i++;
            continue;
          }
          JSONObject offer = (JSONObject) element.get("offer");
          String sdp = offer.optString("sdp");
          String type = offer.optString("type");
          haveOffer = true;
          if (DEBUG) {
            Log.d(LOG_TAG, "sdb = " + sdp);
            Log.d(LOG_TAG, "type = " + type);
            Log.d(LOG_TAG, "About to set remote offer");
          }
          peerConnection.setRemoteDescription(sdpObserver,
            new SessionDescription(SessionDescription.Type.OFFER, sdp));
          peerConnection.createAnswer(sdpObserver, new MediaConstraints());
          if (DEBUG) {
            Log.d(LOG_TAG, "createAnswer returned");
          }
          i = -1;
        } else if (element.has("nonce")) {
          if (element.isNull("candidate")) {
            if (DEBUG) {
              Log.d(LOG_TAG, "Received a null candidate, skipping...");
            }
            i++;
            continue;
          }
          String nonce = element.optString("nonce");
          JSONObject candidate = (JSONObject) element.get("candidate");
          String sdpcandidate = candidate.optString("candidate");
          String sdpMid = candidate.optString("sdpMid");
          int sdpMLineIndex = candidate.optInt("sdpMLineIndex");
          if (DEBUG) {
            Log.d(LOG_TAG, "candidate = " + sdpcandidate);
          }
          if (!seenNonces.contains(nonce)) {
            seenNonces.add(nonce);
            if (DEBUG) {
              Log.d(LOG_TAG, "new nonce, about to add candidate!");
            }
            IceCandidate iceCandidate = new IceCandidate(sdpMid, sdpMLineIndex, sdpcandidate);
            peerConnection.addIceCandidate(iceCandidate);
            if (DEBUG) {
              Log.d(LOG_TAG, "addIceCandidate returned");
            }
          }
        }
        i++;
      }
      if (DEBUG) {
        Log.d(LOG_TAG, "exited loop");
      }
    } catch (IOException e) {
      Log.e(LOG_TAG, "Caught IOException: " + e.toString(), e);
    } catch (JSONException e) {
      Log.e(LOG_TAG, "Caught JSONException: " + e.toString(), e);
    } catch (Exception e) {
      Log.e(LOG_TAG, "Caught Exception: " + e.toString(), e);
    }
  }

  private void sendRendezvous(JSONObject data) {
    try {
      data.put("first", first);
      data.put("webrtc", true);
      data.put("key", rCode + "-r");
      if (first) {
        first = false;
        data.put("apiversion", SdkLevel.getLevel());
      }
      HttpClient client = new DefaultHttpClient();
      HttpPost post = new HttpPost("http://" + rendezvousServer2 + "/rendezvous2/");
      try {
        if (DEBUG) {
          Log.d(LOG_TAG, "About to send = " + data.toString());
        }
        post.setEntity(new StringEntity(data.toString()));
        client.execute(post);
      } catch (IOException e) {
        Log.e(LOG_TAG, "sendRedezvous IOException", e);
      }
    } catch (Exception e) {
      Log.e(LOG_TAG, "Exception in sendRendezvous", e);
    }
  }

  public void send(String output) {
    try {
      if (dataChannel == null) {
        Log.w(LOG_TAG, "No Data Channel in Send");
        return;
      }
      ByteBuffer bbuffer = ByteBuffer.wrap(output.getBytes("UTF-8"));
      Buffer buffer = new Buffer(bbuffer, false); // false = not binary
      dataChannel.send(buffer);
    } catch (UnsupportedEncodingException e) {
      Log.e(LOG_TAG, "While encoding data to send to companion", e);
    }
  }

}
