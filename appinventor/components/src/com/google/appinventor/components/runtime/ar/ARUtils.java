package com.google.appinventor.components.runtime.ar;

import com.google.appinventor.components.runtime.util.AR3DFactory.ARNode;
import android.util.Log;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.gson.Gson;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import static java.lang.Float.parseFloat;

public final class ARUtils {
  private static final java.util.Map<String, Object> nodes = new HashMap<String, Object>();
  private static final String ERROR_UNKNOWN_TYPE = "Unrecognized/invalid type in JSON object";
  private static final String ARJSON_COORDINATES = "coordinates";
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
    // Deserializing the JSON string back to a Java object
    Log.i("parsing pose hash Json...", "");

    Pose pose = null;
    LinkedHashMap translation = (LinkedHashMap) op.get("t");
    LinkedHashMap rotation = (LinkedHashMap) op.get("q");

    try {
      float x = parseFloat(String.format("%.2f",((Double)translation.get("x")).floatValue()));
      float y = parseFloat(String.format("%.2f",((Double)translation.get("y")).floatValue()));
      float z = parseFloat(String.format("%.2f",((Double)translation.get("z")).floatValue()));
      float qx = parseFloat(String.format("%.2f",((Double)rotation.get("x")).floatValue()));
      float qy = parseFloat(String.format("%.2f",((Double)rotation.get("y")).floatValue()));
      float qz = parseFloat(String.format("%.2f",((Double)rotation.get("z")).floatValue()));
      float qw = parseFloat(String.format("%.2f",((Double)rotation.get("w")).floatValue()));


      pose = new Pose(new float[]{x, y, z}, new float[]{qx, qy, qz, qw});
      Log.i("creating Pose in parsePoseLinkedHM ", "parsed object " + pose);
    } catch (Exception e) {
      Log.i("creating Pose error", "err" + e);

      float x = parseFloat((String)translation.get("x"));
      float y = parseFloat((String)translation.get("y"));
      float z = parseFloat((String)translation.get("z"));
      float qx = parseFloat((String)rotation.get("x"));
      float qy= parseFloat((String)rotation.get("y"));
      float qz =  parseFloat((String)rotation.get("z"));
      float qw= parseFloat((String)rotation.get("w"));

      pose = new Pose(new float[]{x, y, z}, new float[]{qx, qy, qz, qw});
      Log.i("creating Pose error", "but corrected with string");
    }
    return pose;
  }


  public static ARNode parseNodeObject(ARNode node, String s) {

    Gson gson = new Gson();
    Log.i("creating node from Json", s);

    LinkedHashMap op = gson.fromJson(s, LinkedHashMap.class);
    Log.i("parseNodeObject", "op is " + op);
    try {
      LinkedHashMap nodeJson = (LinkedHashMap) op.get("node");
      String model = (String) nodeJson.get("model");
      String texture = (String) nodeJson.get("texture");
      String type = (String) nodeJson.get("type");

      Log.i("parseNodeObject", "parsed model " + model);
      Log.i("parseNodeObject", "parsed texture " + texture);
      Log.i("parseNodeObject", "parsed type " + type);
      node.Model(model);
      node.Texture(texture);
      //node.Type(type);

      LinkedHashMap poseHM = (LinkedHashMap) nodeJson.get("pose");
      node.Pose(parsePoseLinkedHashMap(poseHM));
      Log.i("parseNodeObject", "node now has anchor  " + node.Anchor());
    } catch (Exception e) {
      Log.i("parseNodeObject error", "err" + e);
      throw e;

    }
    return node;

  }

  public static ARNode parseYailToNode(ARNode node, Object yailObj, Session trackingObj) {

    try{
      YailDictionary keyvalue = (YailDictionary)yailObj;
      Object o1 = keyvalue.getObject(0);

      String model = (String) keyvalue.get("model");
      String texture = (String) keyvalue.get("texture");
      String type = (String) keyvalue.get("type");

      Log.i("parseYailToNode", "parsed model " + model);
      Log.i("parseYailToNode", "parsed texture " + texture);
      Log.i("parseYailToNode", "parsed type " + type);
      node.Model(model);
      node.Texture(texture);

      LinkedHashMap poseHM = (LinkedHashMap) keyvalue.get("pose");
      Log.i("parseYailToNode", "parsed pose before conversion " + poseHM);
      node.Anchor(trackingObj.createAnchor(parsePoseLinkedHashMap(poseHM)));

      Log.i("parseYailToNode", type + " new node from yail now has anchor  " + node.Anchor());
    } catch (Exception e) {
      Log.i("parseYailToNode error", "err" + e);
      throw e;

    }
    return node;

  }

  public static YailList jsonObjectToYail(final String logTag, final JSONObject object) throws JSONException {
    List<YailList> pairs = new ArrayList<YailList>();
    @SuppressWarnings("unchecked")  // json only allows String keys
    Iterator<String> j = object.keys();
    while (j.hasNext()) {
      String key = j.next();
      Object value = object.get(key);
      Log.wtf(logTag,"value is " + value);
      if (value instanceof Boolean ||
          value instanceof Integer ||
          value instanceof Long ||
          value instanceof Double ||
          value instanceof String) {
        pairs.add(YailList.makeList(new Object[] { key, value }));
      } else if (value instanceof JSONArray) {
        pairs.add(YailList.makeList(new Object[] { key, jsonArrayToYail(logTag, (JSONArray) value)}));
      } else if (value instanceof JSONObject) {
        pairs.add(YailList.makeList(new Object[] { key, jsonObjectToYail(logTag, (JSONObject) value)}));
      } else if (!JSONObject.NULL.equals(value)) {
        Log.wtf(logTag, ERROR_UNKNOWN_TYPE + ": " + value.getClass());
        throw new IllegalArgumentException(ERROR_UNKNOWN_TYPE);
      }
    }
    return YailList.makeList(pairs);
  }

  public static YailList jsonArrayToYail(final String logTag, final JSONArray array) throws JSONException {
    List<Object> items = new ArrayList<Object>();
    for (int i = 0; i < array.length(); i++) {
      Object value = array.get(i);
      if (value instanceof Boolean ||
          value instanceof Integer ||
          value instanceof Long ||
          value instanceof Double ||
          value instanceof String) {
        items.add(value);
      } else if (value instanceof JSONArray) {
        items.add(jsonArrayToYail(logTag, (JSONArray) value));
      } else if (value instanceof JSONObject) {
        items.add(jsonObjectToYail(logTag, (JSONObject) value));
      } else if (!JSONObject.NULL.equals(value)) {
        Log.wtf(logTag, ERROR_UNKNOWN_TYPE + ": " + value.getClass());
        throw new IllegalArgumentException(ERROR_UNKNOWN_TYPE);
      }
    }
    return YailList.makeList(items);
  }

}