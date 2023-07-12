package com.google.appinventor.client.explorer.dialogs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Image;
import com.google.appinventor.client.Ode;
import com.google.appinventor.client.DesignToolbar.Screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.google.appinventor.client.editor.ProjectEditor;
import com.google.appinventor.client.editor.simple.components.MockComponent;
import com.google.appinventor.client.editor.simple.components.MockForm;
import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.properties.Property;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertyChangeListener;
import com.google.appinventor.client.widgets.properties.PropertyEditor;

/**
 * A dailog for updating project property that can be open from any screen
 */
public class ProjectPropertiesDialogBox extends DialogBox { 

    private static ProjectPropertiesDialogBoxUiBinder uiBinder =
        GWT.create(ProjectPropertiesDialogBoxUiBinder.class);
    public static ProjectPropertiesDialogBox lastDialog = null;

    interface ProjectPropertiesDialogBoxUiBinder extends UiBinder<Widget, ProjectPropertiesDialogBox> {
    }

    @UiField
    ScrollPanel projectPropertyCategoryTitlePanel;

    @UiField
    DeckPanel propertiesDeckPanel;

    @UiField
    Image closeIcon;

    /**
     * List Of project property category, which will be used to display in the dialog 
     * property category are : General, Theming, Publishing
     */
    private static final ArrayList<String> projectPropertyCategoryTitle = new ArrayList<String>() {{
        add(Ode.MESSAGES.projectPropertyGeneralCategoryTitle());
        add(Ode.MESSAGES.projectPropertyThemingCategoryTitle());
        add(Ode.MESSAGES.projectPropertyPublishingCategoryTitle());
    }};

    /**
     * Map for the project property category title to Label
     */
    private HashMap<String, Label> categoryToLabel = new HashMap<>();

    /**
     * Maps the project property category to List of EditableProperty which
     * belongs to that particular project property category
     */
    private HashMap<String, ArrayList<EditableProperty>> categoryToProperties = new HashMap<>();

    /* Object for stroing reference of Screen1's MockForm  */
    private MockForm form;

    /* Refers to currently selected label of the project property category */
    Label selectedCategoryLabel = null;


    /* Object for stroing reference of project editor in which the dialog opned */
    private YaProjectEditor projectEditor;

    /* refers to the screen name in which dialog opend */
    private String curScreen = "";

    /* Set the value of the curScreen */
    public void setCurScreen(String screenName) {
        curScreen = screenName;
    }

    public ProjectPropertiesDialogBox() {
        this.setStylePrimaryName("ode-projectPropertyDialogDiv");
        add(uiBinder.createAndBindUi(this));
        this.setAnimationEnabled(true);
        this.setAutoHideEnabled(false);
        lastDialog = this;

        // get current instance of YaProjectEditor
    	projectEditor = (YaProjectEditor)Ode.getInstance().getEditorManager().getOpenProjectEditor(
            Ode.getInstance().getCurrentYoungAndroidProjectId());

        // screen1 mock form
        form = projectEditor.getFormFileEditor("Screen1").getForm();

        // get the editable properties of the screen1 MockForm
	    EditableProperties editableProperties = form.getProperties();
        Iterator<EditableProperty> properties = editableProperties.iterator();

        // iterate and put the editable property to the corresponding category on the map
        while (properties.hasNext()) {
            EditableProperty property = properties.next();

            if (!categoryToProperties.containsKey(property.getCategory())) {
                categoryToProperties.put(property.getCategory(), new ArrayList<EditableProperty>());
            } 

            categoryToProperties.get(property.getCategory()).add(property);
        }

        // vertical panel for 
        VerticalPanel  categoryLablesVerticalPanel = new VerticalPanel();
        categoryLablesVerticalPanel.setStyleName("ode-propertyDialogVerticalPanel");

        for (String categoryTitle : projectPropertyCategoryTitle) {
            // create the label from the category name
            Label categoryNameLabel = new Label(categoryTitle);
            categoryNameLabel.setStyleName("ode-propertyDialogCategoryTitle");
            categoryToLabel.put(categoryTitle, categoryNameLabel);

            /* when project property category clicked by user */
            categoryNameLabel.addClickHandler(event -> {
                onProjectPropertyCategoryChange(categoryNameLabel);
            });

            propertiesDeckPanel.add(getPanel(categoryTitle));

            // make the first one selected by default
            if (selectedCategoryLabel == null) {
                selectedCategoryLabel = categoryNameLabel;
                selectedCategoryLabel.setStyleName("ode-propertyDialogCategoryTitleSelected");
            }

            // add the label to vertical panel
            categoryLablesVerticalPanel.add(categoryNameLabel);
        }

        // add vertical panel to scroll panel
        projectPropertyCategoryTitlePanel.add(categoryLablesVerticalPanel);
        propertiesDeckPanel.showWidget(0);

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                ProjectPropertiesDialogBox.this.center();
            }
        });
    }

    /**
     * Get the Editable Property of the selected category and add to the vertical panel
     * 
     * @param category indicates the currently selected category text
     * @return vertical panel which contains the all the Editable Property belongs to the selected category
     */
    private VerticalPanel getPanel(String category) {
        // main container for the child vertical panels
        VerticalPanel mainContainer = new VerticalPanel();
        mainContainer.setStyleName("ode-propertyDialogVerticalPanel");

        ArrayList<EditableProperty> properties = categoryToProperties.get(category);

        for (EditableProperty property : properties) {
            // container for displaing one editable property
            VerticalPanel container = new VerticalPanel();
            container.setStyleName("ode-propertyDialogPropertyContainer");

            // name of the EditableProperty
            Label name = new Label(property.getName());
            name.setStyleName("ode-propertyDialogPropertyTitle");

            // Description of the property
            Label description = new Label(property.getDescription());
            description.setStyleName("ode-propertyDialogPropertyDescription");

            // editor of the editor
            PropertyEditor editor = property.getEditor();
            editor.setStyleName("ode-propertyDialogPropertyEditor");

            // add to the container
            container.add(name);
            container.add(description);
            container.add(editor);

            // add to the main container
            mainContainer.add(container);
        }  
        
        return mainContainer;
    }

    /**
     * Called when project property category changes 
     */
    private void onProjectPropertyCategoryChange(Label categoryNameLabel) {
        // change the styles when label clicked
        selectedCategoryLabel.setStyleName("ode-propertyDialogCategoryTitle");
        categoryNameLabel.setStyleName("ode-propertyDialogCategoryTitleSelected");

        // assign clicked label to selectedCategoryLabel
        selectedCategoryLabel = categoryNameLabel;

        // display corresponding editable properties on the right panel
        propertiesDeckPanel.showWidget(projectPropertyCategoryTitle.indexOf(selectedCategoryLabel.getText()));
    }

    /**
     *  Users press down arrow button, project property category will be changed
     */
    @Override
    protected void onPreviewNativeEvent(NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONKEYDOWN:
                Ode.CLog("Key Pressed");
                int currentIndex = projectPropertyCategoryTitle.indexOf(selectedCategoryLabel.getText());
                int code = event.getNativeEvent().getKeyCode();
                if (code == KeyCodes.KEY_DOWN) {
                    int newIndex = (currentIndex+1)%projectPropertyCategoryTitle.size();
                    onProjectPropertyCategoryChange(categoryToLabel.get(projectPropertyCategoryTitle.get(newIndex)));
                } else if (code == KeyCodes.KEY_UP) {
                    int newIndex = currentIndex == 0 ? projectPropertyCategoryTitle.size()-1 : currentIndex - 1;
                    onProjectPropertyCategoryChange(categoryToLabel.get(projectPropertyCategoryTitle.get(newIndex)));
                } else if (code == KeyCodes.KEY_ESCAPE) {
                    this.hide();
                }
                break;
        }
    }

    @UiHandler("closeIcon")
    void handleClose(ClickEvent e) {
        this.hide();
        if (curScreen != "Screen1") {
            MockForm curform = projectEditor.getFormFileEditor(curScreen).getForm();
            if (curform != null) {
                curform.curFormProjectPropertyChange();
            }
        }
    }

}