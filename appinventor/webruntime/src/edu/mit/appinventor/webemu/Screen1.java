package edu.mit.appinventor.webemu;

import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.ReplForm;
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

  @JsProperty
  private JavaScriptObject environment = JavaScriptObject.createObject();

  public Screen1() {
    super();
  }

  public void Initialize() {
    evalScheme("(init-runtime)");
    super.Initialize();
  }

  @JsMethod
  public void clear() {
    super.clear();
  }

  @Override
  @SuppressWarnings("unusable-by-js")
  public boolean dispatchEvent(Component component, String componentName, String eventName,
      Object[] args) {
    return false;
  }

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
}
