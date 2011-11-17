// Copyright 2011 Google Inc. All Rights Reserved.

package openblocks.codeblockutil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import openblocks.yacodeblocks.FeedbackReporter;
import openblocks.yacodeblocks.WorkspaceControllerHolder;

/**
 * Button for starting a new emulator.
 *
 * @author sharon@google.com (Sharon Perl)
 *
 */
public class CEmulatorButton extends CSaveButton {

  private static final String WAIT_MSG =
    "<p>Please wait."
       + "It can take 2 or 3 minutes for the emulator to become ready \u2014 even longer<br />\n"
       + "the first time.</p>\n"
       + "<p>The emulator will be ready when the phone screen has a picture<br />\n"
       + "background as shown in the right image here. If it has a lock screen, as shown<br />\n"
       + "in the left, slide the bar with the green lock icon to the right to unlock it.</p>\n"
       + "<p>Even after the picture appears, continue to wait for the emulator<br />\n"
       + "to finish preparing the SD card: watch the notification area at the<br />\n"
       + "top of the emulator screen.</p>"
       + "<p>When the emulator is ready, choose it from the Connect to Device menu<br />\n"
       + "to connect</p>";

  public CEmulatorButton() {
    super("New emulator", "Can't start emulator");
  }

  // icon for the "wait for emulator" alert
  private static ImageIcon emulatorReady =
    new ImageIcon(CEmulatorButton.class.getResource("images/emulator-screens.png"));

  public void init() {
    addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if (WorkspaceControllerHolder.get().getAIDir().runStartEmulator()) {
                showEmulatorNotice();
              }
            }
          });
        }});
  }

  private static void showEmulatorNotice() {
    FeedbackReporter.showWarningMessage(
        WAIT_MSG,
        "Starting the emulator.  Please be patient.",
        "OK",
        emulatorReady);
  }
}
