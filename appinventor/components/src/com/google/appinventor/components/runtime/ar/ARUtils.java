package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.util.AR3DFactory.ARNode;
import android.util.Log;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.gson.Gson;

import java.util.LinkedHashMap;

public final class ARUtils {

  public static Pose parsePoseObject(String s) {
    Gson gson = new Gson();
    //TBD s.pose
    //ArrayList sRep = s.replace("{", "[").replace("}", "]");
    // Deserializing the JSON string back to a Java object
    Log.i("creating Capsule node, updated sJson", s);
    LinkedHashMap op = gson.fromJson(s, LinkedHashMap.class);
    return parsePoseLinkedHashMap(op);

  }


  public static Pose parsePoseLinkedHashMap(LinkedHashMap op) {
    //TBD s.pose
    //ArrayList sRep = s.replace("{", "[").replace("}", "]");
    // Deserializing the JSON string back to a Java object
    Log.i("parsing pose hash Json...", "");

    Pose pose = null;
    try {
      LinkedHashMap translation = (LinkedHashMap) op.get("t");
      LinkedHashMap rotation = (LinkedHashMap) op.get("q");
      double x = (Double) translation.get("x");
      float xf = (float) x;
      double y = (Double) translation.get("y");
      float yf = (float) y;
      double z = (Double) translation.get("z");
      float zf = (float) z;
      double qx = (Double) rotation.get("x");
      float qxf = (float) qx;
      double qy = (Double) rotation.get("y");
      float qyf = (float) qy;
      double qz = (Double) rotation.get("z");
      float qzf = (float) qy;
      double qw = (Double) rotation.get("z");
      float qwf = (float) qw;
      pose = new Pose(new float[]{xf, yf, zf}, new float[]{qxf, qyf, qzf, qwf});
      Log.i("creating Pose in parsePoseLinkedHM ", "parsed object " + pose);
    } catch (Exception e) {
      Log.i("creating Pose error", "err" + e);

    }
    return pose;
  }


  public static ARNode parseNodeObject(ARNode node, String s, Session trackingObj) {

    Gson gson = new Gson();
    Log.i("creating node from Json", s);
    LinkedHashMap op = gson.fromJson(s, LinkedHashMap.class);
    Log.i("creating node from Json", "op is " + op);
    try {
      LinkedHashMap nodeJson = (LinkedHashMap) op.get("node");
      String model = (String) nodeJson.get("model");
      String texture = (String) nodeJson.get("texture");
      String type = (String) nodeJson.get("type");

      Log.i("creating node from json", "parsed model " + model);
      Log.i("creating node from json", "parsed texture " + texture);
      Log.i("creating node from json", "parsed type " + type);
      node.Model(model);
      node.Texture(texture);
      //node.Type(type);

      LinkedHashMap poseHM = (LinkedHashMap) nodeJson.get("pose");
      node.Anchor(trackingObj.createAnchor(parsePoseLinkedHashMap(poseHM)));
      Log.i("creating node from json", "node now has anchor  " + node.Anchor());
    } catch (Exception e) {
      Log.i("creating node from jsonerror", "err" + e);

    }
    return node;

  }

}