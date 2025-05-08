package com.google.appinventor.components.runtime.ar;

import android.util.Log;
import com.google.ar.core.Pose;
import com.google.gson.Gson;

import java.util.LinkedHashMap;

public final class ARUtils {
  public static Pose parseObject(String s) {

    Gson gson = new Gson();
    //TBD s.pose
    //ArrayList sRep = s.replace("{", "[").replace("}", "]");
    // Deserializing the JSON string back to a Java object
    Log.i("creating Capsule node, updated sJson", s);
    LinkedHashMap op = gson.fromJson(s, LinkedHashMap.class);
    Pose pose = null;
    try {
      LinkedHashMap translation = (LinkedHashMap) op.get("t");
      LinkedHashMap rotation = (LinkedHashMap) op.get("t");
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
      Log.i("creating Capsule node", "parsed object " + pose);
    } catch (Exception e) {
      Log.i("creating Capsule node error", "err" + e);

    }
    return pose;

  }
}