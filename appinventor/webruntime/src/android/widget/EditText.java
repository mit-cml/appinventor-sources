package android.widget;

import android.content.Context;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.user.client.DOM;

public class EditText extends TextView {

  boolean isTextArea = false;

  public EditText(Context context) {
    super(DOM.createInputText());
  }

  public EditText(Element element, boolean isTextArea) {
    super(element);
    this.isTextArea = isTextArea;
  }

  public void setText(int stringId) {
    setText(Context.resources.getString(stringId));
  }

  public void setText(String text) {
    if (isTextArea) {
      TextAreaElement.as(element).setValue(text);
    } else {
      InputElement.as(element).setValue(text);
    }
  }

  public String getText() {
    if (isTextArea) {
      return TextAreaElement.as(element).getValue();
    } else {
      return InputElement.as(element).getValue();
    }
  }

  public void setInputType(int inputType) {
    // TODO: Implement input type handling
  }

  public void setTransformationMethod(Object method) {
    if (!isTextArea) {
      InputElement.as(element).setAttribute("type", "password");
    }
    // TODO(lroman10): Real implementation
  }
}
