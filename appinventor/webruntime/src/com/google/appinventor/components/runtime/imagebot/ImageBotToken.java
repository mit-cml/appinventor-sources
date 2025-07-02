package com.google.appinventor.components.runtime.imagebot;

import com.google.appinventor.components.runtime.protobuf.Reader;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.Uint8Array;
import com.google.protobuf.ByteString;
import java.io.InputStream;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

public class ImageBotToken {
  @JsType(isNative = true, namespace = "$root", name = "imagetoken")
  public static class token {
    @JsOverlay
    public static token parseFrom(byte[] data) {
      // This is a placeholder for actual parsing logic.
      // In a real implementation, this would parse the byte array into a token object.
      return decode(data);
    }

    public static native token decode(byte[] data);
  }

  @JsType(isNative = true, namespace = "$root", name = "imagerequest")
  public static class request {
    public static class OperationType {
      public static final int CREATE = 0;
      public static final int EDIT = 1;
    }

    public static class Builder {
      private JavaScriptObject buffer;

      public Builder() {
        this.buffer = JavaScriptObject.createObject();
      }

      private native void setValue(String key, Object value) /*-{
        this.@com.google.appinventor.components.runtime.imagebot.ImageBotToken.request.Builder::buffer[key] = value;
      }-*/;

      public Builder setToken(ImageBotToken.token token) {
        setValue("token", token);
        return this;
      }

      public Builder setSource(ByteString source) {
        setValue("source", source);
        return this;
      }

      public Builder setOperation(int operation) {
        setValue("operation", operation);
        return this;
      }

      public Builder setSize(String size) {
        setValue("size", size);
        return this;
      }

      public Builder setPrompt(String prompt) {
        setValue("prompt", prompt);
        return this;
      }

      public Builder setApikey(String apikey) {
        setValue("apikey", apikey);
        return this;
      }

      public Builder setMask(ByteString mask) {
        setValue("mask", mask);
        return this;
      }

      public native request build() /*-{
        return top.$root.imagerequest.encode(this.@com.google.appinventor.components.runtime.imagebot.ImageBotToken.request.Builder::buffer);
      }-*/;
    }

    @JsOverlay
    public static Builder newBuilder() {
      return new Builder();
    }

    @JsMethod
    public native byte[] finish();

    @JsOverlay
    public final byte[] toByteArray() {
      return finish();
    }
  }

  @JsType(isNative = true, namespace = "$root", name = "imageresponse")
  public static class response {
    @JsOverlay
    public static response parseFrom(byte[] data) {
      // This is a placeholder for actual parsing logic.
      // In a real implementation, this would parse the byte array into a response object.
      return decode(new Reader(data));
    }

    public static native response decode(Reader reader);

    @JsOverlay
    public static response parseFrom(InputStream is) {
      return new response();
    }

    @JsOverlay
    public final ByteString getImage() {
      return new ByteString(getImageAsUint8Array());
    }

    @JsProperty(name = "image")
    public native Uint8Array getImageAsUint8Array();
  }
}
