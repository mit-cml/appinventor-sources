// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;

import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.TranslationDesignerPallete;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.PropertiesUtil;
import com.google.appinventor.client.editor.youngandroid.YaBlocksEditor;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.explorer.SourceStructureExplorerItem;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.widgets.ClonedWidget;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.client.widgets.dnd.DragSourceSupport;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertyChangeListener;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.client.widgets.properties.TextPropertyEditor;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.HasAssetsFolder;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerCollection;
import com.google.gwt.user.client.ui.SourcesMouseEvents;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.ClippedImagePrototype;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface.ComponentDefinition;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface.PropertyDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Abstract superclass for all components in the visual designer.
 *
 * <p>Since the actual component implementation are for a target platform
 * that is different from the platform used to implement the development
 * environment, we need to mock them.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public abstract class MockComponent extends Composite implements PropertyChangeListener,
    SourcesMouseEvents, DragSource {
  // Common property names (not all components support all properties).
  protected static final String PROPERTY_NAME_NAME = "Name";
  protected static final String PROPERTY_NAME_UUID = "Uuid";
  protected static final String PROPERTY_NAME_SOURCE = "Source";
  protected static final List<String> YAIL_NAMES = Arrays.asList("CsvUtil", "Double", "Float",
    "Integer", "JavaCollection", "JavaIterator", "KawaEnvironment", "Long", "Short",
    "SimpleForm", "String", "Pattern", "YailList", "YailNumberToString", "YailRuntimeError");
  private static final int ICON_IMAGE_WIDTH = 16;
  private static final int ICON_IMAGE_HEIGHT = 16;
  public static final int BORDER_SIZE = 2 + 2; // see ode-SimpleMockComponent in Ya.css

  /**
   * This class defines the dialog box for renaming a component.
   */
  private class RenameDialog extends DialogBox {
    // UI elements
    private final LabeledTextBox newNameTextBox;

    RenameDialog(String oldName) {
      super(false, true);

      setStylePrimaryName("ode-DialogBox");
      setText(MESSAGES.renameTitle());
      VerticalPanel contentPanel = new VerticalPanel();

      LabeledTextBox oldNameTextBox = new LabeledTextBox(MESSAGES.oldNameLabel());
      oldNameTextBox.setText(getName());
      oldNameTextBox.setEnabled(false);
      contentPanel.add(oldNameTextBox);

      newNameTextBox = new LabeledTextBox(MESSAGES.newNameLabel());
      newNameTextBox.setText(oldName);
      newNameTextBox.getTextBox().addKeyUpHandler(new KeyUpHandler() {
        @Override
        public void onKeyUp(KeyUpEvent event) {
          int keyCode = event.getNativeKeyCode();
          if (keyCode == KeyCodes.KEY_ENTER) {
            handleOkClick();
          } else if (keyCode == KeyCodes.KEY_ESCAPE) {
            hide();
          }
        }
      });
      contentPanel.add(newNameTextBox);

      Button cancelButton = new Button(MESSAGES.cancelButton());
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
        }
      });
      Button okButton = new Button(MESSAGES.okButton());
      okButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          handleOkClick();
        }
      });
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.add(cancelButton);
      buttonPanel.add(okButton);
      buttonPanel.setSize("100%", "24px");
      contentPanel.add(buttonPanel);
      contentPanel.setSize("320px", "100%");

      add(contentPanel);
    }

    private void handleOkClick() {
      String newName = newNameTextBox.getText();
      // Remove leading and trailing whitespace
      // Replace nonempty sequences of internal spaces by underscores
      newName = newName.trim().replaceAll("[\\s\\xa0]+", "_");
      if (newName.equals(getName())) {
        hide();
      } else if (validate(newName)) {
        hide();
        String oldName = getName();
        changeProperty(PROPERTY_NAME_NAME, newName);
        getForm().fireComponentRenamed(MockComponent.this, oldName);
      } else {
        newNameTextBox.setFocus(true);
        newNameTextBox.selectAll();
      }
    }

    private boolean validate(String newName) {

      // Check that it meets the formatting requirements.
      if (!TextValidators.isValidComponentIdentifier(newName)) {
        Window.alert(MESSAGES.malformedComponentNameError());
        return false;
      }

      // Check that it's unique.
      final List<String> names = editor.getComponentNames();
      if (names.contains(newName)) {
        Window.alert(MESSAGES.duplicateComponentNameError());
        return false;
      }

      // Check that it is a variable name used in the Yail code
      if (YAIL_NAMES.contains(newName)) {
        Window.alert(MESSAGES.badComponentNameError());
        return false;
      }

      //Check that it is not a Component type name, as this is bad for generics
      SimpleComponentDatabase COMPONENT_DATABASE = SimpleComponentDatabase.getInstance();
      if (COMPONENT_DATABASE.isComponent(newName)) {
        Window.alert(MESSAGES.sameAsComponentTypeNameError());
        return false;
      }

      return true;
    }

    @Override
    public void show() {
      super.show();

      DeferredCommand.addCommand(new Command() {
        @Override
        public void execute() {
          newNameTextBox.setFocus(true);
          newNameTextBox.selectAll();
        }
      });
    }
  }

  // Component database: information about components (including their properties and events)
  private final SimpleComponentDatabase COMPONENT_DATABASE;

  // Image bundle
  protected static final Images images = Ode.getImageBundle();

  // Empty component children array so that we don't have to special case and test for null in
  // case of no children
  private static final List<MockComponent> NO_CHILDREN = Collections.emptyList();

  // Editor of Simple form source file the component belongs to
  protected final SimpleEditor editor;

  private final String type;
  private ComponentDefinition componentDefinition;
  private Image iconImage;

  private final SourceStructureExplorerItem sourceStructureExplorerItem;
  /**
   * The state of the branch in the components tree corresponding to this component.
   */
  protected boolean expanded;

  // Properties of the component
  // Expose these to individual component subclasses, which might need to
  // check properties fpr UI manipulation.  One example is MockHorizontalArrangement
  protected final EditableProperties properties;

  private DragSourceSupport dragSourceSupport;

  // Component container the component belongs to (this will be null for the root component aka the
  // form)
  private MockContainer container;

  private MouseListenerCollection mouseListeners = new MouseListenerCollection();

  /**
   * Creates a new instance of the component.
   *
   * @param editor  editor of source file the component belongs to
   */
  MockComponent(SimpleEditor editor, String type, Image iconImage) {
    this.editor = editor;
    this.type = type;
    this.iconImage = iconImage;
    COMPONENT_DATABASE = SimpleComponentDatabase.getInstance(editor.getProjectId());
    componentDefinition = COMPONENT_DATABASE.getComponentDefinition(type);

    sourceStructureExplorerItem = new SourceStructureExplorerItem() {
      @Override
      public void onSelected() {
        // are we showing the blocks editor? if so, toggle the component drawer
        if (Ode.getInstance().getCurrentFileEditor() instanceof YaBlocksEditor) {
          YaBlocksEditor blocksEditor =
              (YaBlocksEditor) Ode.getInstance().getCurrentFileEditor();
          OdeLog.log("Showing item " + getName());
          blocksEditor.showComponentBlocks(getName());
        } else {
          select();
        }
      }

      @Override
      public void onStateChange(boolean open) {
        // The user has expanded or collapsed the branch in the components tree corresponding to
        // this component. Remember that by setting the expanded field so that when we re-build
        // the tree, we will keep the branch in the same state.
        expanded = open;
      }

      @Override
      public boolean canRename() {
        return !isForm();
      }

      @Override
      public void rename() {
        if (!isForm()) {
          new RenameDialog(getName()).center();
        }
      }

      @Override
      public boolean canDelete() {
        return !isForm();
      }

      @Override
      public void delete() {
        if (!isForm()) {
          if (Window.confirm(MESSAGES.reallyDeleteComponent())) {
            MockComponent.this.delete();
          }
        }
      }
    };
    expanded = true;

    // Create a default property set for the component
    properties = new EditableProperties(true);

    // Add the mock component itself as a property change listener so that it can update its
    // visual aspects according to changes of its properties
    properties.addPropertyChangeListener(this);

    // Allow dragging this component in a drag-and-drop action if this is not the root form
    if (!isForm()) {
      dragSourceSupport = new DragSourceSupport(this);
      addMouseListener(dragSourceSupport);
    }
  }

  /**
   * Sets the components widget representation and initializes its properties.
   *
   * <p>To be called from implementing constructor.
   *
   * @param widget  components visual representation in designer
   */
  void initComponent(Widget widget) {
    // Widget needs to be initialized before the component itself so that the component properties
    // can be reflected by the widget
    initWidget(widget);

    // Capture mouse and click events in onBrowserEvent(Event)
    sinkEvents(Event.MOUSEEVENTS | Event.ONCLICK);

    // Add the special name property and set the tooltip
    String name = componentName();
    setTitle(name);
    addProperty(PROPERTY_NAME_NAME, name, null, new TextPropertyEditor());

    // TODO(user): Ensure this value is unique within the project using a list of
    // already used UUIDs
    // Set the component's UUID
    // The default value here can be anything except 0, because YoungAndroidProjectServce
    // creates forms with an initial Uuid of 0, and Properties.java doesn't encode
    // default values when it generates JSON for a component.
    addProperty(PROPERTY_NAME_UUID, "-1", null, new TextPropertyEditor());
    changeProperty(PROPERTY_NAME_UUID, "" + Random.nextInt());

    editor.getComponentPalettePanel().configureComponent(this);
  }

  public boolean isPropertyPersisted(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_NAME)) {
      return false;
    }
    return true;
  }

  protected boolean isPropertyVisible(String propertyName) {
    if (propertyName.equals(PROPERTY_NAME_NAME) ||
        propertyName.equals(PROPERTY_NAME_UUID)) {
      return false;
    }
    return true;
  }

  protected boolean isPropertyforYail(String propertyName) {
    // By default we use the same criterion as persistance
    // This method can then be overriden by the invididual
    // component Mocks
    return isPropertyPersisted(propertyName);
  }

  /**
   * Invoked after a component is created from the palette.
   *
   * <p>Some subclasses may wish to override this method to initialize
   * properties of the newly created component. For example, a component with a
   * caption may want to initialize the caption to match the component's name.
   */
  public void onCreateFromPalette() {
  }

  /**
   * Returns a unique default component name.
   */
  private String componentName() {
    String compType = TranslationDesignerPallete.getCorrespondingString(getType());
    compType = compType.replace(" ", "_").replace("'", "_"); // Make sure it doesn't have any spaces in it
    return compType + getNextComponentIndex();
  }

  /**
   * All components have default names for new component instantiations,
   * usually consisting of the type name and an index. This method
   * returns the next available component index for this component's type.
   *
   * We lower case the typeName and cName so we don't wind up with
   * components of the names 'fooComponent1' and 'FooComponent1' where
   * the only difference is the case of the first (or other)
   * letters. Ultimately the case does matter but when gensyming new
   * component names components whose only difference is in case will
   * still result in an incremented index. So if 'fooComponent1' exist
   * the new component will be 'FooComponent2' instead of
   * 'FooComponent1'. Hopefully this will be less confusing.
   *
   */
  private int getNextComponentIndex() {
    int highIndex = 0;
    if (editor != null) {
      final String typeName = TranslationDesignerPallete.getCorrespondingString(getType())
        .toLowerCase()
        .replace(" ", "_")
        .replace("'", "_");
      final int nameLength = typeName.length();
      for (String cName : editor.getComponentNames()) {
        cName = cName.toLowerCase();
        try {
          if (cName.startsWith(typeName)) {
            highIndex = Math.max(highIndex, Integer.parseInt(cName.substring(nameLength)));
          }
        } catch (NumberFormatException e) {
          continue;
        }
      }
    }
    return highIndex + 1;
  }

  /**
   * Adds a new property for the component.
   *
   * @param name  property name
   * @param defaultValue  default value of property
   * @param caption property's caption for use in the ui
   * @param editor  property editor
   */
  public final void addProperty(String name, String defaultValue, String caption,
      PropertyEditor editor) {

    int type = EditableProperty.TYPE_NORMAL;
    if (!isPropertyPersisted(name)) {
      type |= EditableProperty.TYPE_NONPERSISTED;
    }
    if (!isPropertyVisible(name)) {
      type |= EditableProperty.TYPE_INVISIBLE;
    }
    if (isPropertyforYail(name)) {
      type |= EditableProperty.TYPE_DOYAIL;
    }
    properties.addProperty(name, defaultValue, caption, editor, type);
  }

  /**
   * Returns the component name.
   * <p>
   * This should not be called prior to {@link #initComponent(Widget)}.
   *
   * @return  component name
   */
  public String getName() {
    return properties.getPropertyValue(PROPERTY_NAME_NAME);
  }

  /**
   * Returns true if there is a property with the given name.
   *
   * @param name  property name
   * @return  true if the property exists
   */
  public boolean hasProperty(String name) {
    return properties.getProperty(name) != null;
  }

  /**
   * Returns the property's value.
   *
   * @param name  property name
   * @return  property value
   */
  public String getPropertyValue(String name) {
    return properties.getPropertyValue(name);
  }

  /**
   * Changes the value of a component property.
   *
   * @param name  property name
   * @param value  new property value
   */
  public void changeProperty(String name, String value) {
    properties.changePropertyValue(name, value);
  }

  /**
   * Returns the properties set for the component.
   *
   * @return  properties
   */
  public EditableProperties getProperties() {
    return properties;
  }

  /**
   * Returns the children of this component. Note that the return value will
   * never be {@code null} but rather an empty array for components without
   * children.
   * <p>
   * The returned list should not be modified.
   *
   * @return  children of the component
   */
  public List<MockComponent> getChildren() {
    return NO_CHILDREN;
  }

  /**
   * Returns the visible children of this component that should be showing.
   * <p>
   * The returned list should not be modified.
   */
  public final List<MockComponent> getShowingVisibleChildren() {
    List<MockComponent> allChildren = getChildren();
    if (allChildren.size() == 0) {
      return NO_CHILDREN;
    }

    List<MockComponent> showingVisibleChildren = new ArrayList<MockComponent>();
    for (MockComponent child : allChildren) {
      if (child.isVisibleComponent() && child.showComponentInDesigner()) {
        showingVisibleChildren.add(child);
      }
    }
    return showingVisibleChildren;
  }

  /**
   * Returns the visible children of this component that should be hidden.
   * <p>
   * The returned list should not be modified.
   */
  public final List<MockComponent> getHiddenVisibleChildren() {
    List<MockComponent> allChildren = getChildren();
    if (allChildren.size() == 0) {
      return NO_CHILDREN;
    }

    List<MockComponent> hiddenVisibleChildren = new ArrayList<MockComponent>();
    for (MockComponent child : allChildren) {
      if (child.isVisibleComponent() && !child.showComponentInDesigner()) {
        hiddenVisibleChildren.add(child);
      }
    }
    return hiddenVisibleChildren;
  }

  /**
   * Returns the form containing this component.
   *
   * @return  containing form
   */
  public MockForm getForm() {
    return getContainer().getForm();
  }

  public boolean isForm() {
    return false;
  }

  /**
   * Indicates whether a component has a visible representation.
   * <p>
   * The return value of this method will not change upon successive invocations.
   *
   * @return  {@code true} if there is a visible representation for the
   *          component, otherwise {@code false}
   */
  public abstract boolean isVisibleComponent();

  /**
   * Selects this component in the visual editor.
   */
  public final void select() {
    getForm().setSelectedComponent(this);
  }

  /**
   * Invoked when the selection state of this component changes.
   * <p>
   * Implementations may override this method to perform additional
   * alterations to their appearance based on their new selection state.
   * Overriders must call {@code super.onSelectedChange(selected)}
   * before performing their own alterations.
   */
  protected void onSelectedChange(boolean selected) {
    if (selected) {
      addStyleDependentName("selected");
    } else {
      removeStyleDependentName("selected");
    }
    getForm().fireComponentSelectionChange(this, selected);
  }

  /**
   * Returns whether this component is selected.
   */
  public boolean isSelected() {
    return (getForm().getSelectedComponent() == this);
  }

  /**
   * Returns the type of the component.
   * The return value must not change between invocations.
   * <p>
   * This is used in the serialization format of the component.
   *
   * @return  component type
   */
  public final String getType() {
    return type;
  }

  /**
   * Returns the user-visible type name of the component.
   * By default this is the internal type string.
   *
   * @return  component type name
   */
  public String getVisibleTypeName() {
    return getType();
  }

  /**
   * Returns the icon's image for the component (e.g. to be used on the component palette).
   * The return value must not change between invocations.
   *
   * @return  icon for the component
   */
  public final Image getIconImage() {
    return iconImage;
  }

  /**
   * Returns the unique id for the component
   *
   * @return  uuid for the component
   */
  public final String getUuid() {
    return getPropertyValue(PROPERTY_NAME_UUID);
  }

  /**
   * Sets the component container to which the component belongs.
   *
   * @param container  owning component container for this component
   */
  protected final void setContainer(MockContainer container) {
    this.container = container;
  }

  /**
   * Returns the component container to which the component belongs.
   *
   * @return  owning component container for this component
   */
  protected final MockContainer getContainer() {
    return container;
  }

  /**
   * Constructs a tree item for the component which will be displayed in the
   * source structure explorer.
   *
   * @return  tree item for this component
   */
  protected TreeItem buildTree() {
    // Instantiate new tree item for this component
    // Note: We create a ClippedImagePrototype because we need something that can be
    // used to get HTML for the iconImage. AbstractImagePrototype requires
    // an ImageResource, which we don't necessarily have.
    String imageHTML = new ClippedImagePrototype(iconImage.getUrl(), iconImage.getOriginLeft(),
        iconImage.getOriginTop(), ICON_IMAGE_WIDTH, ICON_IMAGE_HEIGHT).getHTML();
    TreeItem itemNode = new TreeItem(
        new HTML("<span>" + imageHTML + getName() + "</span>"));
    itemNode.setUserObject(sourceStructureExplorerItem);
    return itemNode;
  }

  /**
   * If this component isn't a Form, and this component's type isn't already in typesAndIcons,
   * adds this component's type name as a key to typesAndIcons, mapped to the HTML string used
   * to display the component type's icon. Subclasses that contain components should override
   * this to add their own info as well as that for their contained components.
   * @param typesAndIcons
   */
  public void collectTypesAndIcons(Map<String, String> typesAndIcons) {
    String name = getVisibleTypeName();
    if (!isForm() && !typesAndIcons.containsKey(name)) {
      String imageHTML = new ClippedImagePrototype(iconImage.getUrl(), iconImage.getOriginLeft(),
          iconImage.getOriginTop(), ICON_IMAGE_WIDTH, ICON_IMAGE_HEIGHT).getHTML();
      typesAndIcons.put(name, imageHTML);
    }
  }

  /**
   * Returns the source structure explorer item for this component.
   */
  public final SourceStructureExplorerItem getSourceStructureExplorerItem() {
    return sourceStructureExplorerItem;
  }

  /**
   * Returns the asset node with the given name.
   *
   * @param name  asset name
   * @return  asset node found or {@code null}
   */
  protected ProjectNode getAssetNode(String name) {
    Project project = Ode.getInstance().getProjectManager().getProject(editor.getProjectId());
    if (project != null) {
      HasAssetsFolder<YoungAndroidAssetsFolder> hasAssetsFolder =
          (YoungAndroidProjectNode) project.getRootNode();
      for (ProjectNode asset : hasAssetsFolder.getAssetsFolder().getChildren()) {
        if (asset.getName().equals(name)) {
          return asset;
        }
      }
    }
    return null;
  }

  /**
   * Converts the given image property value to an image url.
   * Returns null if the image property value is blank or not recognized as an
   * asset.
   */
  protected String convertImagePropertyValueToUrl(String text) {
    if (text.length() > 0) {
      ProjectNode asset = getAssetNode(text);
      if (asset != null) {
        return StorageUtil.getFileUrl(asset.getProjectId(), asset.getFileId());
      }
    }
    return null;
  }

  // For debugging purposes only
  private String describeElement(com.google.gwt.dom.client.Element element) {
    if (element == null) {
      return "null";
    }
    if (element == getElement()) {
      return "this";
    }
    try {
      return element.getTagName();
    } catch (com.google.gwt.core.client.JavaScriptException e) {
      // Can get here if the browser throws a permission denied error
      return "????";
    }
  }

  /**
   * Invoked by GWT whenever a browser event is dispatched to this component.
   */
  @Override
  public final void onBrowserEvent(Event event) {
    switch (event.getTypeInt()) {
      case Event.ONMOUSEDOWN:
      case Event.ONMOUSEUP:
      case Event.ONMOUSEMOVE:
      case Event.ONMOUSEOVER:
      case Event.ONMOUSEOUT:
        cancelBrowserEvent(event);
        mouseListeners.fireMouseEvent(this, event);
        break;

      case Event.ONCLICK:
        cancelBrowserEvent(event);
        select();
        break;

      default:
        // Ignore unexpected events
        break;
    }
  }

  /*
   * Prevent browser from doing its own event handling and consume event
   */
  private static void cancelBrowserEvent(Event event) {
    DOM.eventPreventDefault(event);
    DOM.eventCancelBubble(event, true);
  }

  // SourcesMouseEvents implementation

  /**
   * Adds the specified mouse-listener to this component's widget.
   * The listener will be notified of mouse events.
   */
  @Override
  public final void addMouseListener(MouseListener listener) {
    mouseListeners.add(listener);
  }

  /**
   * Removes the specified mouse-listener from this component's widget.
   */
  @Override
  public final void removeMouseListener(MouseListener listener) {
    mouseListeners.remove(listener);
  }

  // DragSource implementation

  @Override
  public final void onDragStart() {
    // no action until createDragWidget() is called
  }

  @Override
  public final Widget createDragWidget(int x, int y) {
    // TODO(user): Make sure the cloned widget does NOT appear in the
    //                    selected state, even if the original widget is in
    //                    the selected state.
    Widget w = new ClonedWidget(this);
    DragSourceSupport.configureDragWidgetToAppearWithCursorAt(w, x, y);

    // Hide this element, but keep taking up space in the UI.
    // This must be done after the drag-widget is created so that
    // the drag widget itself isn't hidden.
    setVisible(false);

    return w;
  }

  @Override
  public Widget getDragWidget() {
    return dragSourceSupport.getDragWidget();
  }

  @Override
  public DropTarget[] getDropTargets() {
    final List<DropTarget> targetsWithinForm = getForm().getDropTargetsWithin();
    return targetsWithinForm.toArray(new DropTarget[targetsWithinForm.size()]);
  }

  @Override
  public final void onDragEnd() {
    // Reshow this element
    setVisible(true);
  }

  /**
   * Returns the preferred width of the component if there was no layout restriction,
   * including the CSS border.
   * <p>
   * Callers should be aware that most components cannot calculate their
   * preferred size correctly until they are attached to the UI; see {@link #isAttached()}.
   * Unattached components are liable to return {@code 0} for any query about their preferred size.
   *
   * @return  preferred width
   */
  // TODO(user): see getPreferredHeight()!
  public int getPreferredWidth() {
    return MockComponentsUtil.getPreferredWidth(this);
  }

  /**
   * Returns the preferred height of the component if there was no layout restriction,
   * including the CSS border.
   * <p>
   * Callers should be aware that most components cannot calculate their
   * preferred size correctly until they are attached to the UI; see {@link #isAttached()}.
   * Unattached components are liable to return {@code 0} for any query about their preferred size.
   *
   * @return  preferred height
   */
  // TODO(user): The concept of preferred height/width is implemented completely wrong.
  //                 Currently we are taking the default size of GWT components. This should be
  //                 implemented to match the behavior of the Android components being mocked.
  public int getPreferredHeight() {
    return MockComponentsUtil.getPreferredHeight(this);
  }

  /*
   * Returns true if this component should be shown in the designer.
   */
  private boolean showComponentInDesigner() {
    if (hasProperty(MockVisibleComponent.PROPERTY_NAME_VISIBLE)) {
      boolean visible = Boolean.parseBoolean(getPropertyValue(
          MockVisibleComponent.PROPERTY_NAME_VISIBLE));
      // If this component's visible property is false, we need to check whether to show hidden
      // components.
      if (!visible) {
        boolean showHiddenComponents = Boolean.parseBoolean(
            editor.getProjectEditor().getProjectSettingsProperty(
            SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
            SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_HIDDEN_COMPONENTS));
        return showHiddenComponents;
      }
    }
    return true;
  }

  int getWidthHint() {
    return Integer.parseInt(getPropertyValue(MockVisibleComponent.PROPERTY_NAME_WIDTH));
  }

  int getHeightHint() {
    return Integer.parseInt(getPropertyValue(MockVisibleComponent.PROPERTY_NAME_HEIGHT));
  }

  /**
   * Refreshes the form.
   *
   * <p>This method should be called whenever a property that affects the size
   * of the component is changed.
   */
  final void refreshForm() {
    if (isAttached()) {
      if (getContainer() != null || isForm()) {
        getForm().refresh();
      }
    }
  }

  // PropertyChangeListener implementation

  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    if (propertyName.equals(PROPERTY_NAME_NAME)) {
      setTitle(newValue);
    } else if (getContainer() != null || isForm()) {
      /* If we've already placed the component onto a Form (and therefore
       * into a container) then call fireComponentPropertyChanged().
       * It's not really an instantiated component until its been added to
       * a container. If we don't make this test then we end up calling
       * fireComponentPropertyChanged when we start dragging the component from
       * the palette. We need to explicitly trigger on Form here, because forms
       * are not in containers.
       */
      getForm().fireComponentPropertyChanged(this, propertyName, newValue);
    }
  }

  public void onRemoved()
  {

  }

  public void delete() {
    OdeLog.log("Got delete component for " + this.getName());
    this.editor.getProjectEditor().clearLocation(getName());
    getForm().select();
    // Pass true to indicate that the component is being permanently deleted.
    getContainer().removeComponent(this, true);
    // tell the component its been removed, so it can remove children's blocks
    onRemoved();
    properties.removePropertyChangeListener(this);
    properties.clear();
  }

  // Layout

  LayoutInfo createLayoutInfo(Map<MockComponent, LayoutInfo> layoutInfoMap) {
    return new LayoutInfo(layoutInfoMap, this) {
      @Override
      int calculateAutomaticWidth() {
        return getPreferredWidth();
      }

      @Override
      int calculateAutomaticHeight() {
        return getPreferredHeight();
      }
    };
  }

  /** Upgrading MockComponent
   *
   * When extensions are upgraded, the MockComponents might need to undergo changes.
   * These changes can be produced inside this function.
   * All subclasses overriding this method must call super.upgrade()!
   */
  public void upgrade() {
    //Upgrade Icon

    //We copy all compatible properties values
    List<PropertyDefinition> newProperties = COMPONENT_DATABASE.getPropertyDefinitions(this.type);
    List<PropertyDefinition> oldProperties = componentDefinition.getProperties();
    EditableProperties currentProperties = getProperties();
    //Operations
    List<String> toBeRemoved = new ArrayList<String>();
    List<String> toBeAdded = new ArrayList<String>();
    //Plan operations
    for (EditableProperty property : currentProperties) {
      boolean presentInNewProperties = false;
      boolean presentInOldProperties = false;
      String oldType = "";
      String newType = "";
      for (PropertyDefinition prop : newProperties) {
        if (prop.getName() == property.getName()) {
          presentInNewProperties = true;
          newType = prop.getEditorType();
        }
      }
      for (PropertyDefinition prop : oldProperties) {
        if (prop.getName() == property.getName()) {
          presentInOldProperties = true;
          oldType = prop.getEditorType();
        }
      }
      // deprecated property
      if (!presentInNewProperties && presentInOldProperties) {
        toBeRemoved.add(property.getName());
      }
      // new property, less likely to happen here
      else if (presentInNewProperties && !presentInOldProperties) {
        toBeAdded.add(property.getName());
      }
      // existing property
      else if (presentInNewProperties && presentInOldProperties) {
        if (newType != oldType) { // type change detected
          toBeRemoved.add(property.getName());
          toBeAdded.add(property.getName());
        }
      }
    }
    //New property
    for (PropertyDefinition property : newProperties) {
      if (!toBeAdded.contains(property.getName()) && !currentProperties.hasProperty(property.getName())) {
        toBeAdded.add(property.getName());
      }
    }
    //Execute operations
    for (String prop : toBeRemoved) {
      currentProperties.removeProperty(prop);
    }
    for (PropertyDefinition property : newProperties) {
      if (toBeAdded.contains(property.getName())) {
        PropertyEditor propertyEditor = PropertiesUtil.createPropertyEditor(property.getEditorType(), (YaFormEditor) editor);
        addProperty(property.getName(), property.getDefaultValue(), property.getCaption(), propertyEditor);
      }
    }

  }

  /**
   * upgradeComplete()
   * Mark a MockComponent upgrade complete.
   * This MUST be called manually after calling upgrade()!
   * All subclasses overriding this method must call super.upgradeComplete()!
   */
  public void upgradeComplete() {
    this.componentDefinition = COMPONENT_DATABASE.getComponentDefinition(this.type); //Update ComponentDefinition
  }



}
