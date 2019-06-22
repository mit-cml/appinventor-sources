
package com.google.appinventor.client.editor.simple.components;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.SimpleEditor;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * Mock ListView component.
 *
 */
public final class MockRecyclerView extends MockVisibleComponent {

//public final class MockRecyclerView extends MockImageBase {

  
    /**
   * Component type name.
   */
  public static final String TYPE = "RecyclerView";
  private final VerticalPanel listViewWidget;
  private HorizontalPanel listItemWidget;

  private TextBox textBoxWidget;
  private InlineLabel labelInItemFirst,labelInItemSecond;
  private SimplePanel panelForItem;
  private String[] currentList;
  private boolean filterShowing = false;

  private static final String DEFAULT_BACKGROUND_COLOR = "&HFF000000";

  private static final String DEFAULT_TEXT_COLOR = "&HFFFFFFFF";

  //  Needed for background color of labelInItem
  private String backgroundColor;
  private String textColor;
  private String currentElements;

    /**
     * Creates a new MockImage component.
     *
     * @param editor  editor of source file the component belongs to
     */
    public MockRecyclerView(SimpleEditor editor) {
        super(editor, TYPE, images.recyclerview());

        listViewWidget = new VerticalPanel();
        
    //TODO (Jose) extract magic numbers as ComponentConstants.java
    listViewWidget.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px", "100%");
    listViewWidget.setStylePrimaryName("ode-SimpleMockComponent");
    listViewWidget.setStyleName("listViewComponentStyle", true);

    createFilterBox();

     // textColor must be set before the component is initialized, because onPropertyChange
    // might call setSlementsFromString, which tries to set the item textcolor
    textColor  = DEFAULT_TEXT_COLOR;

    initComponent(listViewWidget);
    MockComponentsUtil.setWidgetBackgroundColor(listViewWidget, DEFAULT_BACKGROUND_COLOR);


    }

     @Override
  public void onCreateFromPalette() {
    changeProperty(PROPERTY_NAME_TEXT, MESSAGES.textPropertyValue(getName()));
  }

  /*
   * Sets the listview's BackgroundColor property to a new value.
   */
  private void setBackgroundColorProperty(String text) {
    if (MockComponentsUtil.isDefaultColor(text)) {
      text = "&HFF000000";  // black
    }
    backgroundColor = text;
    MockComponentsUtil.setWidgetBackgroundColor(listViewWidget, text);
  }


   /**
   * This method is called when the show filter box is checked or unchecked.
   * Checking the showfilterbar adds a textbox in the mocklistview and
   * vice versa.
   */
  private void setFilterShowBox(String value) {
    if (Boolean.parseBoolean(value)) {
      filterShowing = true;
      textBoxWidget.setVisible(true);
    }
    else {
      filterShowing = false;
      textBoxWidget.setVisible(false);
    }
  }

    private void createFilterBox() {
    textBoxWidget = new TextBox();
    textBoxWidget.setText("Search list...");
    textBoxWidget.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_FILTER_PREFERRED_HEIGHT + "px");
    textBoxWidget.setVisible(false);
    listViewWidget.add(textBoxWidget);
  }

   private void setElementsFromStringProperty(String text){
    currentElements = text;
    currentList = text.split(",");

    listViewWidget.clear();
    createFilterBox();

    if (filterShowing) {
      textBoxWidget.setVisible(true);
    }
    else {
      textBoxWidget.setVisible(false);
    }

    for(int i=0; i<currentList.length; i+=2){
      createLabelItem(i);
      createLabelPanel();
    }

  }

  private void createLabelItem(int i) {
   
    listItemWidget = new HorizontalPanel();

    labelInItemFirst =new InlineLabel(currentList[i]);
    labelInItemSecond =new InlineLabel(currentList[i+1]);
    labelInItemFirst.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px", "100%");
    MockComponentsUtil.setWidgetBackgroundColor(labelInItemFirst, backgroundColor);
    MockComponentsUtil.setWidgetTextColor(labelInItemFirst, textColor);

    labelInItemSecond.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px", "100%");
    MockComponentsUtil.setWidgetBackgroundColor(labelInItemSecond, backgroundColor);
    MockComponentsUtil.setWidgetTextColor(labelInItemSecond, textColor);

    listItemWidget.add(labelInItemFirst);
    listItemWidget.add(labelInItemSecond);


  }

  private void createLabelPanel() {
    panelForItem =new SimplePanel();
    panelForItem.setStylePrimaryName("listViewItemStyle");
    panelForItem.setSize(ComponentConstants.LISTVIEW_PREFERRED_WIDTH + "px",
        ComponentConstants.LISTVIEW_PREFERRED_HEIGHT + "px");


    //panelForItem.add(labelInItemFirst);
    panelForItem.add(listItemWidget);
    listViewWidget.add(panelForItem);
  }



  // PropertyChangeListener implementation
  @Override
  public void onPropertyChange(String propertyName, String newValue) {
    super.onPropertyChange(propertyName, newValue);
    // Apply changed properties to the mock component
   if (propertyName.equals(PROPERTY_NAME_LISTVIEW)) {
      setElementsFromStringProperty(newValue);
      refreshForm();
    } else if (propertyName.equals(PROPERTY_NAME_SHOW_FILTER_BAR)) {
      setFilterShowBox(newValue);
      refreshForm();
    } 
    else if (propertyName.equals(PROPERTY_NAME_BACKGROUNDCOLOR)) {
      setBackgroundColorProperty(newValue);
      if (currentList != null) {
        setElementsFromStringProperty(currentElements);
      }
      refreshForm();
    }
    
    /* else if (propertyName.equals(PROPERTY_NAME_TEXTCOLOR)) {
      textColor = newValue;
      if (currentList != null) {
        setElementsFromStringProperty(currentElements);
      }
      refreshForm();
    }*/
  }


  }
