package android.app;

import android.Res;
import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gwt.user.client.Window;

public class AlertDialog extends Dialog implements DialogInterface {
  private Context ctx;
  private LinearLayout titleLabelContainer;
  private LinearLayout contentPanel;
  private TextView titleLabel;
  private TextView messageLabel;
  DialogInterface.OnClickListener itemsListener, positiveListener, negativeListener, neutralListener;
  private LinearLayout buttonsLayout;

  public static class Builder {
    Context ctx;
    View view;

    boolean cancelable = true;
    String title;
    String message;
    CharSequence items[];
    String positiveLabel, negativeLabel, neutralLabel;
    DialogInterface.OnClickListener itemsListener, positiveListener, negativeListener, neutralListener;

    public Builder(Context ctx) {
      this.ctx = ctx;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public void setTitle(int title) {
      setTitle(ctx.getString(title));
    }

    public void setMessage(int message) {
      setMessage(ctx.getString(message));
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public void setCancelable(boolean cancelable) {
      this.cancelable = cancelable;
    }

    public void setPositiveButton(int label, DialogInterface.OnClickListener listener) {
      setPositiveButton(ctx.getString(label), listener);
    }

    public void setPositiveButton(String label, DialogInterface.OnClickListener listener) {
      this.positiveLabel = label;
      this.positiveListener = listener;
    }

    public void setNegativeButton(int label, DialogInterface.OnClickListener listener) {
      setNegativeButton(ctx.getString(label), listener);
    }

    public void setNegativeButton(String label, DialogInterface.OnClickListener listener) {
      this.negativeLabel = label;
      this.negativeListener = listener;
    }

    public void setNeutralButton(int label, DialogInterface.OnClickListener listener) {
      setNeutralButton(ctx.getString(label), listener);
    }

    public void setNeutralButton(String label, DialogInterface.OnClickListener listener) {
      this.neutralLabel = label;
      this.neutralListener = listener;
    }

    public void setItems(int items, DialogInterface.OnClickListener listener) {
      this.items = ctx.getResources().getStringArray(items);
      this.itemsListener = listener;
    }

    public void setItems(CharSequence items[], DialogInterface.OnClickListener listener) {
      this.items = items;
      this.itemsListener = listener;
    }

    public void setView(View view) {
      this.view = view;
    }

    public AlertDialog create() {
      return new AlertDialog(this);
    }
  }

  public AlertDialog(Builder builder) {
    super(builder.cancelable);

    this.ctx = builder.ctx;
    this.itemsListener = builder.itemsListener;
    this.positiveListener = builder.positiveListener;
    this.negativeListener = builder.negativeListener;
    this.neutralListener = builder.neutralListener;

    LinearLayout dialogLayout = new LinearLayout(ctx);

    titleLabelContainer = new LinearLayout(ctx);
    dialogLayout.addView(titleLabelContainer);

    contentPanel = new LinearLayout(ctx);
    contentPanel.getElement().addClassName(Res.R.style().dialogContent());
    dialogLayout.addView(contentPanel);

    setTitle(builder.title);
    setMessage(builder.message);

    if (builder.items != null) {
      int count = 0;
      for (CharSequence item : builder.items) {
        final int countFinal = count;
        count++;

        Button button = new Button(ctx);
        button.setText(item.toString());
        button.getElement().addClassName(Res.R.style().dialogItem());
        button.getElement().addClassName(Res.R.style().controlHighlight());
        button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            AlertDialog.this.dismiss();
            if (itemsListener != null) {
              itemsListener.onClick(AlertDialog.this, countFinal);
            }
          }
        });
        contentPanel.addView(button);
      }
    }

    if (builder.view != null) {
      if (builder.view.getElement() != null) {
        contentPanel.addView(builder.view);
      }
    }

    Button okButton = null;
    Button cancelButton = null;
    Button neutralButton = null;

    if (builder.positiveLabel != null) {
      okButton = new Button(ctx);
      okButton.setText(builder.positiveLabel);
      okButton.getElement().addClassName(Res.R.style().dialogButton());
      okButton.getElement().addClassName(Res.R.style().controlHighlight());
      okButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          AlertDialog.this.dismiss();
          if (positiveListener != null) {
            positiveListener.onClick(AlertDialog.this, BUTTON_POSITIVE);
          }
        }
      });
    }

    if (builder.negativeLabel != null) {
      cancelButton = new Button(ctx);
      cancelButton.setText(builder.negativeLabel);
      cancelButton.getElement().addClassName(Res.R.style().dialogButton());
      cancelButton.getElement().addClassName(Res.R.style().controlHighlight());
      cancelButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          AlertDialog.this.dismiss();
          if (negativeListener != null) {
            negativeListener.onClick(AlertDialog.this, BUTTON_NEGATIVE);
          }
        }
      });
    }

    if (builder.neutralLabel != null) {
      neutralButton = new Button(ctx);
      neutralButton.setText(builder.neutralLabel);
      neutralButton.getElement().addClassName(Res.R.style().dialogButton());
      neutralButton.getElement().addClassName(Res.R.style().controlHighlight());
      neutralButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          AlertDialog.this.dismiss();
          if (neutralListener != null) {
            neutralListener.onClick(AlertDialog.this, BUTTON_NEUTRAL);
          }
        }
      });
    }

    if (okButton != null || cancelButton != null || neutralButton != null) {
      buttonsLayout = new LinearLayout(ctx);
      buttonsLayout.getElement().addClassName(Res.R.style().dialogButtons());
      if (cancelButton != null) {
        buttonsLayout.addView(cancelButton);
      }
      if (neutralButton != null) {
        buttonsLayout.addView(neutralButton);
      }
      if (okButton != null) {
        buttonsLayout.addView(okButton);
      }
      dialogLayout.addView(buttonsLayout);
    }

    popupPanel.getElement().appendChild(dialogLayout.getElement());
  }

  public void setTitle(int title) {
    setTitle(Context.resources.getString(title));
  }

  public void setTitle(String title) {
    if (titleLabel == null && title != null && !title.isEmpty()) {
      titleLabel = new TextView(ctx);
      titleLabel.getElement().addClassName(Res.R.style().dialogTitle());
      titleLabelContainer.addView(titleLabel);
    }
    if (titleLabel != null) {
      titleLabel.setText(title);
    }
    if (popupPanel.isShowing()) {
      popupPanel.center();
    }
  }

  public void setMessage(int message) {
    setMessage(Context.resources.getString(message));
  }

  public void setMessage(CharSequence message) {
    if (messageLabel == null && message != null && !"".equals(message)) {
      messageLabel = new TextView(ctx);
      messageLabel.getElement().addClassName(Res.R.style().dialogMessage());
      contentPanel.addView(messageLabel);
    }
    if (messageLabel != null) {
      messageLabel.setText(message.toString().replace("\n", "<br/>"));
    }
    if (popupPanel.isShowing()) {
      popupPanel.center();
    }
  }

  public void setView(View view) {
    if (view != null && view.getElement() != null) {
      contentPanel.removeAllViews();
      contentPanel.addView(view);
    }
  }

  public void setButton(int whichButton, CharSequence text, OnClickListener listener) {
    if (buttonsLayout == null) {
      return;
    }
    Button b = (Button) buttonsLayout.getChildAt(whichButton);
    if (b == null) {
      return;
    }
    b.setText(text);
    if (listener != null) {
      b.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          AlertDialog.this.dismiss();
          listener.onClick(AlertDialog.this, whichButton);
        }
      });
    } else {
      b.setOnClickListener(null);
    }
  }

  public void show() {
    // TODO Couldn't achieve the vertical scroll in the AlertDialog with CSS
    contentPanel.getElement().getStyle().setProperty("max-height", ((int) (Window.getClientHeight() * 0.75)) + "px");
    super.show();
  }
}
