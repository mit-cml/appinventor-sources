package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.appinventor.client.utils.Promise;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ExternalTextResource;
import com.google.gwt.resources.client.ResourceCallback;
import com.google.gwt.resources.client.ResourceException;
import com.google.gwt.resources.client.TextResource;

/**
 * BlocklyMsg leverages GWT's internationalization in ClientBundle's to load
 * locale specific Blockly translations without having to ship the entire set
 * of languages to every user.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public interface BlocklyMsg extends ClientBundle {
  @Source("messages.json")
  ExternalTextResource messages();

  // Translation loading management

  final class Loader {
    private static Promise<Boolean> translationPromise = null;
    @SuppressWarnings("FieldMayBeFinal")  // used from JSNI
    private static JavaScriptObject translations = null;

    private Loader() {
      // Not instantiable
    }

    public static Promise<Boolean> loadTranslations() {
      if (translationPromise != null) {
        return translationPromise;
      }
      translationPromise = new Promise<>((resolve, reject) -> {
        try {
          ((BlocklyMsg) GWT.create(BlocklyMsg.class)).messages()
              .getText(new ResourceCallback<TextResource>() {
                @Override
                public void onError(ResourceException e) {
                  reject.apply(new Promise.WrappedException(e));
                }

                @Override
                public void onSuccess(TextResource textResource) {
                  installTranslations(textResource.getText());
                  resolve.apply(true);
                }
              });
        } catch (ResourceException e) {
          reject.apply(new Promise.WrappedException(e));
        }
      });
      return translationPromise;
    }

    public static JavaScriptObject getTranslations() {
      return translations;
    }

    private static native void installTranslations(String translations)/*-{
      @com.google.appinventor.client.editor.youngandroid.i18n.BlocklyMsg.Loader::translations =
        JSON.parse(translations);
    }-*/;
  }
}
