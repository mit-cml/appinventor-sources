package com.google.appinventor.client.editor.simple.components;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.palette.SimplePaletteItem;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.ui.AbsolutePanel;

/**
 * Mock Popup Menu component.
 *
 * @author singhalsara48@gmail.com (Sara Singhal)
 */
public class MockPopupMenu extends MockContainer {
    public static final String TYPE = "PopupMenu";
    private AbsolutePanel menuWidget;

    /**
     * Creates a new MockPopupMenu component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockPopupMenu(SimpleEditor editor) {
        super(editor, TYPE, images.menu(), new MockHVLayout(ComponentConstants.LAYOUT_ORIENTATION_VERTICAL));

        rootPanel.setHeight("100%");
        menuWidget = new AbsolutePanel();
        menuWidget.setStylePrimaryName("ode-SimpleMockContainer");
        menuWidget.add(rootPanel);

        initComponent(menuWidget);
    }

    @Override
    protected boolean acceptableSource(DragSource source) {
        MockComponent component = null;
        if (source instanceof MockComponent) {
            component = (MockComponent) source;
        } else if (source instanceof SimplePaletteItem) {
            component = (MockComponent) source.getDragWidget();
        }
        return component instanceof MockContextMenuItem;
    }
}
