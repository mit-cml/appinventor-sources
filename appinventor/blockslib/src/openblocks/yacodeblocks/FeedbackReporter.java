// Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

import openblocks.codeblockutil.HTMLPane;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


/**
 * FeedbackReporter provides a mechanism for showing the user status and error
 * messages.
 *
 * @author sharon@google.com (Sharon)
 *
 */

public class FeedbackReporter extends JOptionPane {

  private static final String ERROR_MESSAGE_TITLE = "Oops!";
  private static final String INFO_MESSAGE_TITLE = "FYI";
  private static final String SYSTEM_ERROR_MESSAGE_TITLE = "Not your fault";
  private static final String SYSTEM_ERROR_MESSAGE_PREFIX =
    "We made a mistake! Please help by reporting this bug:\n";
  private static final String WARNING_MESSAGE_TITLE = "Warning";
  private static final String CONFIRM_DIALOG_TITLE = "Please choose...";

  public static final String NO_PROJECT_MESSAGE =
      "You need to open a project in the App Inventor Designer.";


  // TODO(sharon): clickable links not yet implemented
  /*private static final String TROUBLESHOOTING_LINK =
    "<hr><p>The <a href=\"http://appinventor.googlelabs.com/learn/troubleshooting.html\">"
    + "Troubleshooting Guide</a> may provide further help.</p>";
   */
  private static final String TROUBLESHOOTING_LINK =
    "<hr><p>The Troubleshooting Guide at"
    + " http://appinventor.googlelabs.com/learn/troubleshooting.html"
    + " <br />may provide further help.</p>";

  // This is the notice icon from the documentation.   We use it here for
  // warnings
  private static ImageIcon androidWarning =
    new ImageIcon
    (FeedbackReporter.class.getResource("images/android-warning.png"));

  // This is the warning icon from the documentation.   We use it here for
  // errors
  private static ImageIcon androidError =
    new ImageIcon
    (FeedbackReporter.class.getResource("images/android-error.png"));

  private static JFrame frame;

  public static boolean testingMode = false;
  // testingConfirmationResult is the result to return from getConfirmation
  // when testingMode==true.
  public static boolean testingConfirmationResult = true;
  // result to return from getInput when testingMode=true.
  public static String testingInputResult = "";
  // Flag that is true when testingMode = true and one of the error reporting methods
  // has been called since the flag was last cleared.
  public static boolean testingShowedErrorMessage = false;

  private FeedbackReporter() {
  }

  public static void setFrame(JFrame parent) {
    frame = parent;
  }


  public static void showErrorMessage(String msgText, String title) {
    if (!testingMode) {
      if (WorkspaceControllerHolder.isHeadless()) {
        throw new RuntimeException(msgText);
      }
      String options[] = {"OK"};
      HTMLPane htmlMsg = new HTMLPane(msgText + TROUBLESHOOTING_LINK);
      showOptionDialog(frame, htmlMsg, title,
          DEFAULT_OPTION, ERROR_MESSAGE, androidError, options, null);
    } else {
      System.out.println(msgText);
      testingShowedErrorMessage = true;
    }
  }

  public static void showErrorMessage(String msgText) {
    showErrorMessage(msgText, ERROR_MESSAGE_TITLE);
  }

  public static void showErrorMessageWithExit(String msgText) {
    if (!testingMode) {
      if (WorkspaceControllerHolder.isHeadless()) {
        throw new RuntimeException(msgText);
      }
      String options[] = {"NOT NOW", "EXIT"};
      HTMLPane htmlMsg = new HTMLPane(msgText + "<p>Exit the Blocks Editor now?</p> + "
          + TROUBLESHOOTING_LINK);
      int choice = showOptionDialog(frame, htmlMsg,
          ERROR_MESSAGE_TITLE, YES_NO_OPTION, ERROR_MESSAGE, null, options, options[0]);
      if (choice == 1) {
        System.exit(0);
      }
    } else {
      System.out.println(msgText);
      testingShowedErrorMessage = true;
    }
  }

  // The most general form of warning lets you specify a message,
  // a title for the box, the text for an "OK" button, and an icon.
  public static void showWarningMessage(
      String msgText, String title, String buttonText, ImageIcon icon) {
    if (!testingMode) {
      if (WorkspaceControllerHolder.isHeadless()) {
        return;
      }
      String options[] = {buttonText};
      HTMLPane htmlMsg = new HTMLPane(msgText + TROUBLESHOOTING_LINK);
      showOptionDialog(frame, htmlMsg, title,
          DEFAULT_OPTION, WARNING_MESSAGE, icon, options, null);
    } else {
      System.out.println(msgText);
    }
  }

  // Default icon
  public static void showWarningMessage(
      String msgText, String title, String buttonText) {
    if (!testingMode) {
      if (WorkspaceControllerHolder.isHeadless()) {
        return;
      }
      String options[] = {buttonText};
      HTMLPane htmlMsg = new HTMLPane(msgText + TROUBLESHOOTING_LINK);
      showOptionDialog(frame, htmlMsg, title,
          DEFAULT_OPTION, WARNING_MESSAGE, androidWarning, options, null);
    } else {
      System.out.println(msgText);
    }
  }

  // Default icon, "OK" as button text
  public static void showWarningMessage(String msgText, String title) {
    if (!testingMode) {
      if (WorkspaceControllerHolder.isHeadless()) {
        return;
      }
      String options[] = {"OK"};
      HTMLPane htmlMsg = new HTMLPane(msgText + TROUBLESHOOTING_LINK);
      showOptionDialog(frame, htmlMsg, title,
          DEFAULT_OPTION, WARNING_MESSAGE, androidWarning, options, null);
    } else {
      System.out.println(msgText);
    }
  }

  //Default icon, "OK" as button text, and default title
  public static void showWarningMessage(String msgText) {
    if (!testingMode) {
      if (WorkspaceControllerHolder.isHeadless()) {
        return;
      }
      String options[] = {"OK"};
      HTMLPane htmlMsg = new HTMLPane(msgText + TROUBLESHOOTING_LINK);
      showOptionDialog(frame, htmlMsg, WARNING_MESSAGE_TITLE,
          DEFAULT_OPTION, WARNING_MESSAGE, androidWarning, options, null);
    } else {
      System.out.println(msgText);
    }
  }

  public static void showInfoMessage(String msgText) {
    if (!testingMode) {
      if (WorkspaceControllerHolder.isHeadless()) {
        return;
      }
      HTMLPane htmlMsg = new HTMLPane(msgText);
      showMessageDialog(frame, htmlMsg, INFO_MESSAGE_TITLE, JOptionPane.INFORMATION_MESSAGE);
    } else {
      System.out.println(msgText);
    }
  }

  public static void showSystemErrorMessage(String msgText) {
    // For debugging, always also print the message to the console
    System.out.println(msgText);
    // Need a different icon for a system error. Also, maybe see if user wants
    // to send an error report.
    if (!testingMode) {
      if (WorkspaceControllerHolder.isHeadless()) {
        throw new RuntimeException(msgText);
      }
      HTMLPane htmlMsg = new HTMLPane(SYSTEM_ERROR_MESSAGE_PREFIX + msgText);
      showMessageDialog(frame,
          htmlMsg, SYSTEM_ERROR_MESSAGE_TITLE,
          JOptionPane.ERROR_MESSAGE);
    } else {
      testingShowedErrorMessage = true;
    }
  }

  /**
   * Show msgText in a pop-up dialog box and ask the user to choose "yes" or
   * "no". Return true if the user selects "yes" or false if the user selects
   * "no".
   * If testingMode is true, returns testingConfirmationResult instead of
   * popping up the dialog.
   * @param title title for the dialog
   * @param msgText  Message to show
   * @return true if "yes" selected, false otherwise.
   */
  public static boolean getConfirmation(String msgText, String title) {
    if (!testingMode) {
      if (WorkspaceControllerHolder.isHeadless()) {
        throw new RuntimeException(msgText);
      }
      HTMLPane htmlMsg = new HTMLPane(msgText);
      int result = showConfirmDialog(frame, htmlMsg, title,
          JOptionPane.YES_NO_OPTION);
      return (result == JOptionPane.YES_OPTION);
    } else {
      return testingConfirmationResult;
    }
  }

  public static boolean getConfirmation(String msgText) {
    return getConfirmation(msgText, CONFIRM_DIALOG_TITLE);
  }

  public static String getInput(String msgText) {
    if (!testingMode) {
      if (WorkspaceControllerHolder.isHeadless()) {
        throw new RuntimeException(msgText);
      }
      HTMLPane htmlMsg = new HTMLPane(msgText);
      return showInputDialog(null, htmlMsg);
    } else {
      return testingInputResult;
    }
  }


  // For errors that we want logged, but not shown to the user
  public static void logError(String msgText) {
    System.out.println(msgText);
  }
}
