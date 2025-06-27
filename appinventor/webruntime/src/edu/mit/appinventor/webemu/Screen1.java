package edu.mit.appinventor.webemu;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.PermissionResultHandler;
import com.google.appinventor.components.runtime.ReplForm;
import com.google.appinventor.components.runtime.util.BulkPermissionRequest;
import com.google.gwt.core.client.JavaScriptObject;
import java.util.logging.Logger;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Primary screen for the App Inventor REPL.
 */
@JsType(namespace = "appinventor")
public class Screen1 extends ReplForm {
  private static final Logger LOG = Logger.getLogger(Screen1.class.getName());

  static {
    initRuntime();
  }

  @SuppressWarnings("FieldMayBeFinal")
  @JsProperty
  private JavaScriptObject environment = JavaScriptObject.createObject();

  public Screen1() {
    super();
  }

  @JsMethod
  public void Initialize() {
    evalScheme("(init-runtime)");
    super.Initialize();
  }

  @JsMethod
  public void clear() {
    super.clear();
  }

  @Override
  public boolean isDeniedPermission(String permission) {
    return false;  // The browser will handle it
  }

  @Override
  @SuppressWarnings("unusable-by-js")
  public native boolean dispatchEvent(Component component, String componentName, String eventName,
      Object[] args)/*-{
    // TODO(ewpatton): Move dispatchEvent to runtime.scm
    var registeredObject = $wnd._scm2host(this.@edu.mit.appinventor.webemu.Screen1::environment[componentName]);
    if (registeredObject !== component) {
      // Not the right target component.
      return false;
    }
    var cb = this.@edu.mit.appinventor.webemu.Screen1::environment[componentName + '$' + eventName];
    if (typeof cb !== 'function') {
      // No event handler for this event.
      return false;
    }
    // Call the event handler with the component as 'this'.
    if (args === null || args === undefined) {
      args = [];
    }
    // Convert args to an array if it is not already.
    if (!Array.isArray(args)) {
      args = Array.prototype.slice.call(args);
    }
    cb.apply($wnd, args);
    return true;
  }-*/;

  @Override
  public void dispatchGenericEvent(Component component, String eventName, boolean notAlreadyHandled,
      Object[] args) {
    // TODO(ewpatton): Real implementation
  }

  @JsMethod
  @SuppressWarnings("unusable-by-js")
  public static Form getActiveForm() {
    return activeForm;
  }

  public void setFormName(String formName) {
    this.formName = formName;
  }

  @Override
  protected void $define() {
    // TODO(ewpatton): Real implementation
    LOG.info("$define");
    setTitle("Screen1");
  }

  public native void evalScheme(String code) /*-{
    $wnd.evalScheme(code);
  }-*/;

  private static native void initRuntime() /*-{
    $wnd.initRuntime();
  }-*/;
}
