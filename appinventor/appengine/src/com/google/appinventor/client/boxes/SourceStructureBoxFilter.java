// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2023 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.boxes;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.editor.designer.DesignerRootComponent;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.explorer.SourceStructureExplorer;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.DropDownItem;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DockPanel;

import java.util.ArrayList;
import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * Box implementation for source structure explorer (new style, with filters).
 */
public final class SourceStructureBoxFilter implements ISourceStructureBox {
  private Integer view = 1;

  private final DropDownButton dropDownButton;
  private final SourceStructureExplorer sourceStructureExplorer = new SourceStructureExplorer();

  /**
   * Creates new source structure explorer box.
   */
  public SourceStructureBoxFilter(SourceStructureBox container) {
    super();

    List<DropDownItem> items = new ArrayList<>();
    items.add(new DropDownItem("AllComponents", MESSAGES.sourceStructureBoxCaptionAll(), new SelectSourceView(1)));
    items.add(new DropDownItem("VisibleComponents", MESSAGES.sourceStructureBoxCaptionVisible(), new SelectSourceView(2)));
    items.add(new DropDownItem("NonVisibleComponents", MESSAGES.sourceStructureBoxCaptionNonVisible(), new SelectSourceView(3)));

    dropDownButton = new DropDownButton("ComponentsTreeFilter", "", items, false);
    dropDownButton.addStyleName("components-tree-filter");
    dropDownButton.setCaption(MESSAGES.sourceStructureBoxCaptionAll());

    container.getHeaderContainer().clear();
    container.getHeaderContainer().add(dropDownButton, DockPanel.LINE_START);
  }

  public void show(DesignerRootComponent root) {
    sourceStructureExplorer.updateTree(root.buildComponentsTree(view),
        root.getLastSelectedComponent().getSourceStructureExplorerItem());
    updateSourceDropdownButtonCaption();

    sourceStructureExplorer.setVisible(true);
  }

  public SourceStructureExplorer getSourceStructureExplorer() {
    return sourceStructureExplorer;
  }

  public void setView(Integer view) {
    this.view = view;
  }

  public Integer getView() {
    return view;
  }

  private void updateSourceDropdownButtonCaption() {
    String c;
    switch (view) {
      case 1:
        c = MESSAGES.sourceStructureBoxCaptionAll();
        break;
      case 2:
        c = MESSAGES.sourceStructureBoxCaptionVisible();
        break;
      case 3:
        c = MESSAGES.sourceStructureBoxCaptionNonVisible();
        break;
      default:
        c = MESSAGES.sourceStructureBoxCaption();
        break;
    }

    dropDownButton.setCaption(c);
  }

  private final class SelectSourceView implements Command {
    /* 1 - > All components
     * 2 - > Visible components
     * 3 - > Non-visible components
     */
    private final Integer view;

    SelectSourceView(Integer view) {
      super();
      this.view = view;
    }

    @Override
    public void execute() {
      MockForm form = ((YaFormEditor) Ode.getInstance().getCurrentFileEditor()).getForm();
      sourceStructureExplorer.updateTree(form.buildComponentsTree(view),
          form.getForm().getLastSelectedComponent().getSourceStructureExplorerItem());
      SourceStructureBoxFilter.this.setView(view);

      updateSourceDropdownButtonCaption();
    }
  }

}
