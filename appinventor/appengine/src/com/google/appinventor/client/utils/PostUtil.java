package com.google.appinventor.client.utils;

import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class PostUtil {

    static {
        installCode();
    }

    public void doPost(String stuff) {
        XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
            @Override
            public void onReadyStateChange(XMLHttpRequest xhr) {
                if (xhr.getReadyState() == XMLHttpRequest.DONE) {
                    Window.alert("DONE!");
                }
            }
        });
        xhr.open("POST", "http://localhost:8090/api/user/create");
        xhr.setRequestHeader("Content-Type", "application/json");
//        xhr.setRequestHeader("Content-Type", "application/zip+appinventor;base64");
        JSONObject json = new JSONObject();
        try {
            json.put("authorId", new JSONString("4"));
            json.put("name", new JSONString("Olaf"));
            json.put("username", new JSONString("snow_master"));
            json.put("appInventorInstance", new JSONString("ai2"));
            xhr.send(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void test() {
        new PostUtil().doPost("hello world");
    }

    public static native void installCode()/*-{
    top.testPost = $entry(@com.google.appinventor.client.utils.PostUtil::test());
  }-*/;

    public static native void foobar()/*-{}-*/;
}
