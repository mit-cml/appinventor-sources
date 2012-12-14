// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package openblocks.yacodeblocks;

import javax.swing.JApplet;

/**
 * Code to make codeblocks start up as an applet. We abandoned this approach for now,
 * but I'm leaving the code here in case we decide to revive it.
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */
@Deprecated
public class WorkspaceApplet extends JApplet {
  WorkspaceController wc;

  @Override
  public void init() {
/*  
    System.out.println("Applet init called");
    wc = WorkspaceController.getInstance();
    InputStream langStream = 
      this.getClass().getResourceAsStream("/yacodeblocks/support/ya_lang_def.xml");
    if (langStream != null) {
      wc.setLangDefInputStream(langStream);
      wc.loadFreshWorkspace();
      add(wc.getWorkspacePanel());
    } else {
      System.out.println("Can't find language definition. Oops!");
    }
*/
  }
  
  @Override
  public void start() {
    System.out.println("Applet start called");
  }

  @Override
  public void destroy() {
    System.out.println("Applet destroy called");
//    wc.resetWorkspace();
//    wc.resetLanguage();
  }

  @Override
  public void stop() {
    System.out.println("Applet stop called");
  }
}
