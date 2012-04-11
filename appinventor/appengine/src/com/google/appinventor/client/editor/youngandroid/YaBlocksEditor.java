package com.google.appinventor.client.editor.youngandroid;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.editor.FileEditor;
import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.youngandroid.CodeblocksManager;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Editor for Young Android Blocks (.blk) files.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class YaBlocksEditor extends FileEditor {
  private final YoungAndroidBlocksNode blocksNode;

  private final Widget palettePanel;

  YaBlocksEditor(ProjectEditor projectEditor, YoungAndroidBlocksNode blocksNode) {
    super(projectEditor, blocksNode);

    this.blocksNode = blocksNode;

    // TODO(BLOCKS-IN-BROWSER) - Create a real palette for block drawers.
    palettePanel = createFakePalette();

    AbsolutePanel blocksArea = new AbsolutePanel();
    blocksArea.add(new Label("Future home of the blocks editor"), 100, 250);
    blocksArea.setSize("500px", "500px");
    initWidget(blocksArea);
  }

  private static Widget createFakePalette() {
    VerticalPanel panel = new VerticalPanel();
    panel.add(new Label("Built-In | My Blocks | Advanced"));
    panel.add(new Label("Definition"));
    panel.add(new Label("Text"));
    panel.add(new Label("Lists"));
    panel.add(new Label("Math"));
    panel.add(new Label("Logic"));
    panel.add(new Label("Control"));
    panel.add(new Label("Colors"));
    return panel;
  }

  // FileEditor methods

  @Override
  public void loadFile(final Command afterFileLoaded) {
    OdeAsyncCallback<String> callback = new OdeAsyncCallback<String>(MESSAGES.loadError()) {
      @Override
      public void onSuccess(String blkFileContent) {
        // TODO(BLOCKS-IN-BROWSER) - create the visual blocks.
        if (afterFileLoaded != null) {
          afterFileLoaded.execute();
        }
      }
    };
    Ode.getInstance().getProjectService().load(getProjectId(), getFileId(), callback);
  }

  @Override
  public String getTabText() {
    return MESSAGES.blocksEditorTabName(blocksNode.getFormName());
  }

  @Override
  public void onShow() {
    // When this editor is shown, update the "current" editor.
    Ode.getInstance().setCurrentFileEditor(this);

    // Set the palette box's content.
    PaletteBox paletteBox = PaletteBox.getPaletteBox();
    paletteBox.setContent(palettePanel);

    // TODO(BLOCKS-IN-BROWSER) - For now, we tell codeblocks to load the form and blocks when this
    // blocks editor is shown. We can remove this code when we really support editing blocks in the browser.
    CodeblocksManager.getCodeblocksManager().loadPropertiesAndBlocks(blocksNode, null);

    super.onShow();
  }

  @Override
  public void onHide() {
    // TODO(BLOCKS-IN-BROWSER) - remove the following call to
    // CodeblocksManager.saveCodeblocksSource when we really support editing blocks in the browser.
    CodeblocksManager.getCodeblocksManager().saveCodeblocksSource(null);

    // Clear the palette box.
    PaletteBox paletteBox = PaletteBox.getPaletteBox();
    paletteBox.clear();

    // When an editor is detached, clear the "current" editor.
    Ode.getInstance().setCurrentFileEditor(null);

    super.onHide();
  }

  @Override
  public String getRawFileContent() {
    // TODO(BLOCKS-IN-BROWSER) - return the content that should be saved in the .blk file
    throw new RuntimeException("YaBlocksEditor.getRawFileContent is not implemented yet!");
  }

  @Override
  public void onSave() {
    // Nothing to do after blocks are saved.
  }
}
