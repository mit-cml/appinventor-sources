package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.editor.simple.components.MockComponent;
import jsinterop.annotations.JsFunction;

@JsFunction
public interface MockComponentFactory {
    MockComponent create(SimpleEditor editor);
}
