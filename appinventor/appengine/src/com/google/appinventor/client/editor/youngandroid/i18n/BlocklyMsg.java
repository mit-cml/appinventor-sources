package com.google.appinventor.client.editor.youngandroid.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ExternalTextResource;
import com.google.gwt.resources.client.ResourceCallback;
import com.google.gwt.resources.client.ResourceException;
import com.google.gwt.resources.client.TextResource;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import jsinterop.annotations.JsFunction;

/**
 * BlocklyMsg leverages GWT's internationalization in ClientBundle's to load
 * locale specific Blockly translations without having to ship the entire set
 * of languages to every user. One can call
 * {@link Loader#ensureTranslationsLoaded(LoadCallback)}
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public interface BlocklyMsg extends ClientBundle {
  @Source("messages.json")
  ExternalTextResource messages();

  @Source("messages_default.json")
  TextResource default_messages();

  // Translation loading management

  /**
   * Callers that need to do additional work once the translations are loaded
   * can pass an implementation of the {@code LoadCallback} interface to
   * {@link Loader#ensureTranslationsLoaded(LoadCallback)} to be called after
   * the translations are finished loading.
   */
  @JsFunction
  interface LoadCallback {
    void call();
  }

  final class Loader {
    private static boolean translationsLoaded = false;
    private static Set<LoadCallback> pendingCallbacks = new HashSet<LoadCallback>();
    private static boolean loadInitiated = false;

    private Loader() {
      // Not instantiable
    }

    static void loadTranslation() {
      loadInitiated = true;
      try {
        final String default_str = ((BlocklyMsg) GWT.create(BlocklyMsg.class)).default_messages().getText();
        ((BlocklyMsg) GWT.create(BlocklyMsg.class)).messages()
            .getText(new ResourceCallback<TextResource>() {
              @Override
              public void onError(ResourceException e) {
                // TODO(ewpatton): How do we handle failures to load translations?
              }

              @Override
              public void onSuccess(TextResource textResource) {
                installTranslations(textResource.getText(), default_str);
                translationsLoaded = true;
                for (LoadCallback callback : pendingCallbacks) {
                  try {
                    callback.call();
                  } catch (Exception e) {
                    // foo
                  }
                }
                pendingCallbacks.clear();
              }
            });
      } catch (ResourceException e) {
        // TODO(ewpatton): How do we handle failures to load translations?
      }
    }

    /**
     * Ensure that the Blockly translations are loaded. This will start the
     * loading process if it has not previously been called.
     */
    public static void ensureTranslationsLoaded() {
      ensureTranslationsLoaded(null);
    }

    /**
     * Ensure that the Blockly translations are loaded. This will start the
     * loading process if it has not previously been called.
     *
     * @param callback An optional callback to call once the translations have
     *                 been loaded.
     */
    public static void ensureTranslationsLoaded(@Nullable LoadCallback callback) {
      if (!loadInitiated) {
        if (callback != null) {
          pendingCallbacks.add(callback);
        }
        loadTranslation();
      } else if (translationsLoaded) {
        if (callback != null) {
          callback.call();
        }
      } else {
        if (callback != null) {
          pendingCallbacks.add(callback);
        }
      }
    }

    private static native void installTranslations(String translations, String default_strings)/*-{
        var messages = JSON.parse(translations);
        var default_json = JSON.parse(default_strings);
        Object.keys(default_json).forEach(function (key) {
            if (key.indexOf("Blockly.Msg.") === 0) {
              if (key in messages) {
                Blockly.Msg[key.replace("Blockly.Msg.", "")] = messages[key];
              } else {
                Blockly.Msg[key.replace("Blockly.Msg.", "")] = default_json[key];
              }
            }
        });
    }-*/;
  }
}
