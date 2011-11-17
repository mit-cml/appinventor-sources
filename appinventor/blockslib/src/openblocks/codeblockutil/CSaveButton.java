// Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.codeblockutil;

import java.awt.Color;

import javax.swing.SwingUtilities;

/**
 * A CSaveButton is a special CButton which provides methods to
 * disable and re-enable its functionality and update its appearance
 * accordingly.
 *
 *
 */
public class CSaveButton extends CGradientButton {

  private static final boolean DEBUG = false;

  private String enabledString;
  private String disabledString;

  public CSaveButton(String enabledString, String disabledString) {
    super(BUTTON_COLOR_TOP, BUTTON_COLOR_BOTTOM, enabledString);

    setTextLighting(TEXT_COLOR, Color.BLACK);
    this.enabledString = enabledString;
    this.disabledString = disabledString;
  }

  public void grayOut() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (DEBUG) {
          System.out.println("Disabling " + enabledString + " button.");
        }
        setText(disabledString);
        setEnabled(false);
        setLighting(BUTTON_GRAYED_COLOR, BUTTON_GRAYED_COLOR);
        setTextLighting(TEXT_GRAYED_COLOR, TEXT_GRAYED_COLOR);
        revalidate();
      }
    });
  }

  public void reColor() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (DEBUG) {
          System.out.println("Re-enabling " + enabledString + " button.");
        }
        setText(enabledString);
        setEnabled(true);
        setLighting(BUTTON_COLOR_TOP, BUTTON_COLOR_BOTTOM);
        setTextLighting(TEXT_COLOR, Color.BLACK);
        revalidate();
      }
    });
  }

}
