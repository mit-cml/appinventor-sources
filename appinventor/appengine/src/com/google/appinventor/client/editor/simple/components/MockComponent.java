// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2022 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.blocks.BlocksEditor;
import com.google.appinventor.client.editor.designer.DesignerEditor;
import com.google.appinventor.client.editor.designer.DesignerRootComponent;
import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.simple.components.i18n.ComponentTranslationTable;
import com.google.appinventor.client.Images;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.SourceStructureBox;
import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.editor.simple.components.utils.PropertiesUtil;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.explorer.SourceStructureExplorerItem;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.widgets.ClonedWidget;
import com.google.appinventor.client.widgets.LabeledTextBox;
import com.google.appinventor.client.widgets.dnd.DragSource;
import com.google.appinventor.client.widgets.dnd.DragSourceSupport;
import com.google.appinventor.client.widgets.dnd.DropTarget;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertyChangeListener;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.client.widgets.properties.StringPropertyEditor;
import com.google.appinventor.client.widgets.properties.TextPropertyEditor;
import com.google.appinventor.client.youngandroid.TextValidators;
import com.google.appinventor.shared.rpc.project.HasAssetsFolder;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface.ComponentDefinition;
import com.google.appinventor.shared.simple.ComponentDatabaseInterface.PropertyDefinition;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllTouchHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.TouchCancelHandler;
import com.google.gwt.event.dom.client.TouchEndHandler;
import com.google.gwt.event.dom.client.TouchMoveHandler;
import com.google.gwt.event.dom.client.TouchStartHandler;
import com.google.gwt.event.dom.client.TouchCancelEvent;
import com.google.gwt.event.dom.client.TouchEndEvent;
import com.google.gwt.event.dom.client.TouchMoveEvent;
import com.google.gwt.event.dom.client.TouchStartEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerCollection;
import com.google.gwt.user.client.ui.SourcesMouseEvents;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    SourcesMouseEvents, DragSource, HasAllTouchHandlers, DesignPreviewChangeListener {
  private static final Logger LOG = Logger.getLogger(MockComponent.class.getName());
  // Common property names (not all components support all properties).
  public static final String PROPERTY_NAME_NAME = "Name";
  public static final String PROPERTY_NAME_UUID = "Uuid";
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
      super(false, false);
      setGlassEnabled(true);
      setStylePrimaryName("ode-DialogBox");
      setText(MESSAGES.renameTitle());
      VerticalPanel contentPanel = new VerticalPanel();

      Button topInvisible = new Button();
      contentPanel.add(topInvisible);

      LabeledTextBox oldNameTextBox = new LabeledTextBox(MESSAGES.oldNameLabel());
      oldNameTextBox.setText(getName());
      oldNameTextBox.setEnabled(false);
      contentPanel.add(oldNameTextBox);

      newNameTextBox = new LabeledTextBox(MESSAGES.newNameLabel());
      newNameTextBox.setText(oldName);
      newNameTextBox.getTextBox().addKeyDownHandler(new KeyDownHandler() {
        @Override
        public void onKeyDown(KeyDownEvent event) {
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

      Button bottomInvisible = new Button();
      bottomInvisible.setStyleName("FocusTrap");
      topInvisible.setStyleName("FocusTrap");
      topInvisible.addFocusHandler(new FocusHandler() {
        @Override
        public void onFocus(FocusEvent event) {
          okButton.setFocus(true); 
        }
      });
      bottomInvisible.addFocusHandler(new FocusHandler() {
        public void onFocus(FocusEvent event) {
          newNameTextBox.setFocus(true); 
        }
      });

      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.add(cancelButton);
      buttonPanel.add(okButton);
      buttonPanel.add(bottomInvisible);
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
        getRoot().fireComponentRenamed(MockComponent.this, oldName);
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
      if (TextValidators.isReservedName(newName)) {
        Window.alert(MESSAGES.reservedNameError());
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

  /**
   * This class defines the dialog box for deleting a component.
   */
  private class DeleteDialog extends DialogBox {
    private final Button deleteButton;
    DeleteDialog() {
      super(false, false);

      setStylePrimaryName("ode-DialogBox");
      setText(MESSAGES.deleteComponentButton());
      VerticalPanel contentPanel = new VerticalPanel();

      contentPanel.add(new HTML(MESSAGES.reallyDeleteComponent()));
      Button cancelButton = new Button(MESSAGES.cancelButton());
      cancelButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
        }
      });
      deleteButton = new Button(MESSAGES.deleteButton());
      deleteButton.addStyleName("destructive-action");
      deleteButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          hide();
          MockComponent.this.delete();
          SourceStructureBox.getSourceStructureBox().getSourceStructureExplorer().getTree().setFocus(true);
        }
      });
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.add(cancelButton);
      buttonPanel.add(deleteButton);
      buttonPanel.setSize("100%", "24px");
      contentPanel.add(buttonPanel);
      contentPanel.setSize("320px", "100%");

      add(contentPanel);
    }
    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
      super.onPreviewNativeEvent(event);
      if (event.getTypeInt() == Event.ONKEYDOWN && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
        hide();
      }
    }

    public void center() {
      super.center();
      deleteButton.setFocus(true);
    }
  }

  // Component database: information about components (including their properties and events)
  private final ComponentDatabaseInterface COMPONENT_DATABASE;

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
  private HandlerManager handlers;

  /**
   * Creates a new instance of the component.
   *
   * @param editor  editor of source file the component belongs to
   */
  MockComponent(SimpleEditor editor, String type, Image iconImage) {
    this.editor = editor;
    this.type = type;
    this.iconImage = iconImage;
    this.handlers = new HandlerManager(this);
    COMPONENT_DATABASE = editor.getComponentDatabase();
    componentDefinition = COMPONENT_DATABASE.getComponentDefinition(type);

    sourceStructureExplorerItem = new SourceStructureExplorerItem() {
      @Override
      public void onSelected(NativeEvent source) {
        // are we showing the blocks editor? if so, toggle the component drawer
        if (Ode.getInstance().getCurrentFileEditor() instanceof BlocksEditor) {
          BlocksEditor<?, ?> blocksEditor =
              (BlocksEditor<?, ?>) Ode.getInstance().getCurrentFileEditor();
          LOG.info("Showing item " + getName());
          blocksEditor.showComponentBlocks(getName());
        } else {
          select(source);
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
        return !isRoot();
      }

      @Override
      public void rename() {
        if (!isRoot()) {
          new RenameDialog(getName()).center();
        }
      }

      @Override
      public boolean canDelete() {
        return !isRoot();
      }

      @Override
      public void delete() {
        if (!isRoot()) {
          new DeleteDialog().center();
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
    if (!isRoot()) {
      dragSourceSupport = new DragSourceSupport(this);
      addMouseListener(dragSourceSupport);
      addTouchStartHandler(dragSourceSupport);
      addTouchMoveHandler(dragSourceSupport);
      addTouchEndHandler(dragSourceSupport);
      addTouchCancelHandler(dragSourceSupport);
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
    sinkEvents(Event.MOUSEEVENTS | Event.ONCLICK | Event.TOUCHEVENTS);

    // Add the special name property and set the tooltip
    String name = componentName();
    setTitle(name);
    addProperty(PROPERTY_NAME_NAME, name, null, null, null, new TextPropertyEditor());

    // TODO(user): Ensure this value is unique within the project using a list of
    // already used UUIDs
    // Set the component's UUID
    // The default value here can be anything except 0, because YoungAndroidProjectService
    // creates forms with an initial Uuid of 0, and Properties.java doesn't encode
    // default values when it generates JSON for a component.
    addProperty(PROPERTY_NAME_UUID, "-1", null, null, null, new TextPropertyEditor());
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
    // This method can then be overridden by the individual
    // component Mocks
    if (PROPERTY_NAME_UUID.equals(propertyName)) {
      return false;
    }
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
    String compType = ComponentTranslationTable.getComponentName(getType());
    compType = compType.replace(" ", "_").replace("'", "_"); // Make sure it doesn't have any spaces in it
    if (compType.equals("Slot") || compType.equals("Intent")) {
      return compType + getNextComponentLetter();
    }
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
      final String typeName = ComponentTranslationTable.getComponentName(getType())
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
   * Follows all the same logic as getNextComponentIndex, except it uses upper case
   * letters instead of numbers. This is only used for Alexa Skills, as Alexa
   * doesn't allow numbers in Intent or Slot names.
   *
   * If a user has more than 26 of the same component, then next increment will be AA, AB,
   * and so on.
   */
  private String getNextComponentLetter() {
    String highLetter = "a";
    final String type = getType().toLowerCase();
    if (editor != null) {
      Set<String> names = editor.getComponentNames().stream().map(String::toLowerCase).collect(Collectors.toSet());
      while (names.contains(type + highLetter)) {
        if (highLetter.endsWith("z")) {
          highLetter = highLetter.substring(0, highLetter.length() - 1) + "aa";
        } else {
          highLetter = highLetter.substring(0, highLetter.length() - 1) +
              (char) (highLetter.charAt(highLetter.length() - 1) + 1);
        }
      }
    }
    return highLetter.toUpperCase();
  }

  protected final void addProperty(String name) {
    addProperty(name, "", null, null, null, new StringPropertyEditor());
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
      String category, String description, PropertyEditor editor) {

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
    properties.addProperty(name, defaultValue, caption, category, description, editor, type, "", null);
  }

  /**
   * Adds a new property for the component.
   *
   * @param name  property name
   * @param defaultValue  default value of property
   * @param caption  property's caption for use in the ui
   * @param editorType  editor type for the property
   * @param editorArgs  additional editor arguments
   * @param editor  property editor
   */
  public final void addProperty(String name, String defaultValue, String caption, String category,
                                String editorType, String[] editorArgs, PropertyEditor editor) {

    String propertyDesc = ComponentTranslationTable.getPropertyDescription(name
      + "PropertyDescriptions");
    if (propertyDesc.equals(name + "PropertyDescriptions")) {
      propertyDesc = ComponentTranslationTable.getPropertyDescription((type.equals("Form")
          ? "Screen" : type) + "." + propertyDesc);
    }

    int propertyType = EditableProperty.TYPE_NORMAL;
    if (!isPropertyPersisted(name)) {
      propertyType |= EditableProperty.TYPE_NONPERSISTED;
    }
    if (!isPropertyVisible(name)) {
      propertyType |= EditableProperty.TYPE_INVISIBLE;
    }
    if (isPropertyforYail(name)) {
      propertyType |= EditableProperty.TYPE_DOYAIL;
    }
    properties.addProperty(name, defaultValue, ComponentTranslationTable.getPropertyName(caption),
        ComponentTranslationTable.getCategoryName(category),  propertyDesc, editor, propertyType, editorType, editorArgs);
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
   * Renames the component to {@code newName}.
   * @param newName The new name for the component.
   */
  public void rename(String newName) {
    String oldName = getPropertyValue(PROPERTY_NAME_NAME);
    properties.changePropertyValue(PROPERTY_NAME_NAME, newName);
    getForm().fireComponentRenamed(this, oldName);
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

  public DesignerRootComponent getRoot() {
    return getContainer().getRoot();
  }

  public boolean isForm() {
    return false;
  }

  public boolean isRoot() {
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
  public final void select(NativeEvent event) {
    getRoot().setSelectedComponent(this, event);
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
    getRoot().fireComponentSelectionChange(this, selected);
  }

  /**
   * Returns whether this component is selected.
   */
  public boolean isSelected() {
    return (getRoot().getSelectedComponents() == this);
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
  protected void setContainer(MockContainer container) {
    this.container = container;
  }

  /**
   * Returns the component container to which the component belongs.
   *
   * @return  owning component container for this component
   */
  public final MockContainer getContainer() {
    return container;
  }

  private final Focusable nullFocusable = new Focusable() {
    @Override
    public int getTabIndex() {
      return 0;
    }

    @Override
    public void setAccessKey(char key) {

    }

    @Override
    public void setFocus(boolean focused) {

    }

    @Override
    public void setTabIndex(int index) {

    }
  };

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
    TreeItem itemNode = new TreeItem(
        new HTML("<span>" + iconImage.getElement().getString() + SafeHtmlUtils.htmlEscapeAllowEntities(getName()) + "</span>")) {
      @Override
      protected Focusable getFocusable() {
        return nullFocusable;
      }
    };
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
      typesAndIcons.put(name, iconImage.getElement().getString());
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
  public void onBrowserEvent(Event event) {
    if (!shouldCancel(event)) return;
    switch (event.getTypeInt()) {
      case Event.ONTOUCHSTART:
      case Event.ONTOUCHEND:
        if (isForm()) {
          select(event);
        }
      case Event.ONTOUCHMOVE:
      case Event.ONTOUCHCANCEL:
        cancelBrowserEvent(event);
        DomEvent.fireNativeEvent(event, handlers);
        break;

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
        select(event);
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

  @Override
  public final HandlerRegistration addTouchStartHandler(TouchStartHandler handler) {
    return handlers.addHandler(TouchStartEvent.getType(), handler);
  }

  @Override
  public final HandlerRegistration addTouchMoveHandler(TouchMoveHandler handler) {
    return handlers.addHandler(TouchMoveEvent.getType(), handler);
  }

  @Override
  public final HandlerRegistration addTouchEndHandler(TouchEndHandler handler) {
    return handlers.addHandler(TouchEndEvent.getType(), handler);
  }

  @Override
  public final HandlerRegistration addTouchCancelHandler(TouchCancelHandler handler) {
    return handlers.addHandler(TouchCancelEvent.getType(), handler);
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
    final List<DropTarget> targetsWithinForm = getRoot().getDropTargetsWithin();
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
        YaFormEditor formEditor = (YaFormEditor) editor;
        return formEditor.shouldDisplayHiddenComponents();
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
   * of the component is changed. It calls refreshForm(false) which permits
   * throttling.
   */
  final void refreshForm() {
    refreshForm(false);
  }

  /*
   * Refresh the current form. If force is true, we bypass the
   * throttling code. This is needed by MockImageBase because it
   * *must* refresh the form before resizing loaded images.
   *
   */
  final void refreshForm(boolean force) {
    if (isAttached()) {
      if (getContainer() != null || isForm()) {
        if (force) {
          getForm().doRefresh();
        } else {
          getForm().refresh();
        }
      }
    }
  }

  // Null onDesignPreviewChange implementation

  @Override
  public void onDesignPreviewChanged() {
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
      getRoot().fireComponentPropertyChanged(this, propertyName, newValue);
    }
  }

  public void onRemoved()
  {

  }

  public void delete() {
    this.editor.getProjectEditor().clearLocation(getName());
    getRoot().select(null);
    // Pass true to indicate that the component is being permanently deleted.
    getContainer().removeComponent(this, true);
    // tell the component its been removed, so it can remove children's blocks
    onRemoved();
    getForm().select(null);
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
        if (prop.getName().equals(property.getName())) {
          presentInNewProperties = true;
          newType = prop.getEditorType();
        }
      }
      for (PropertyDefinition prop : oldProperties) {
        if (prop.getName().equals(property.getName())) {
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
        PropertyEditor propertyEditor = PropertiesUtil.createPropertyEditor(
            property.getEditorType(), property.getDefaultValue(),
            (DesignerEditor<?, ?, ?, ?, ?>) editor, property.getEditorArgs());
        addProperty(property.getName(), property.getDefaultValue(), property.getCaption(),
            property.getCategory(), property.getEditorType(),
            property.getEditorArgs(), propertyEditor);
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

  /**
   * Hides or shows the specified property of the Component.
   *
   * @param property  Property key
   * @param show  will show the property if set to true, will hide it otherwise
   */
  protected void showProperty(String property, boolean show) {
    // Get the current type flags of the Property
    int type = properties.getProperty(property).getType();

    if (show) {
      type &= ~EditableProperty.TYPE_INVISIBLE; // AND with all bits except INVISIBLE flag
    } else {
      type |= EditableProperty.TYPE_INVISIBLE; // OR with INVISIBLE flag to add invisibility
    }

    // Set the new type
    properties.getProperty(property).setType(type);
  }

  public native void setShouldCancel(Event event, boolean cancelable)/*-{
    event.shouldNotCancel = !cancelable;
  }-*/;

  public native boolean shouldCancel(Event event)/*-{
    return !event.shouldNotCancel;
  }-*/;

}
