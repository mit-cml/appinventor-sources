package com.google.appinventor.client.explorer.youngandroid;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import com.google.gwt.dom.client.Style.Unit;

import java.lang.Override;
import java.lang.String;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;

import static com.google.appinventor.client.Ode.MESSAGES;

public class NewUserGetStarted {

  private static class Tutorial {

    enum Difficulty {
      EASY, INTERMEDIATE, HARD;

      private static final Map<String, Difficulty> mapFromString = new HashMap<String, Difficulty>();
      static {
        mapFromString.put("easy", EASY);
        mapFromString.put("intermediate", INTERMEDIATE);
        mapFromString.put("hard", HARD);
      }

      public static Difficulty fromString(String stringRepresentation) {
        return mapFromString.get(stringRepresentation);
      }
    }

    String name;
    Difficulty difficulty;
    String fileName;

    Tutorial(String name, Difficulty diff, String fileName) {
      this.name = name;
      this.difficulty = diff;
      this.fileName = fileName;
    }
  }

  public static class JavaScriptTutorialData extends JavaScriptObject {
    protected JavaScriptTutorialData() {}

    public final native String getName() /*-{
    return this.title;
  }-*/;

    public final native String getDifficulty() /*-{
    return this.difficulty;
  }-*/;

    public final native String getFileName() /*-{
    return this.fileName;
  }-*/;
  }

  public static void displayWalkthroughPicker() {
    String content = MESSAGES.walkthroughSelect();
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText(content);
    dialogBox.setStylePrimaryName("ode-DialogBox-Tutorial");
    dialogBox.getElement().setId("walkthroughPicker");

    //setting position of dialog box
    dialogBox.center();
    dialogBox.setAnimationEnabled(true);

    //button elements
    final Button closeButton = new Button(MESSAGES.walkthroughCloseButton());
    closeButton.getElement().setId("closeButton");

    final Button startButton = new Button(MESSAGES.walkthroughStartButton());
    startButton.getElement().setId("startButton");
    startButton.setEnabled(false);


    final List<Tutorial> TUTORIALS = new ArrayList<Tutorial>();

    // for each tutorial, get the file name, the tutorial name and the difficulty level
    JsArray<JavaScriptTutorialData> tutorialMetaData = BlocklyPanel.callGetTutorialMetaData();


    for (int i = 0; i < tutorialMetaData.length(); i++) {
      JavaScriptTutorialData tutorial = tutorialMetaData.get(i);
      // add the new Tutorial object (name, difficulty, filename)
      TUTORIALS.add(new Tutorial(tutorial.getName(), Tutorial.Difficulty.fromString(tutorial.getDifficulty()),
          tutorial.getFileName()));
    }

    final CellTable<Tutorial> allTutorialsTable = new CellTable<Tutorial>();

    // Create name column
    TextColumn<Tutorial> nameColumn = new TextColumn<Tutorial>() {
      @Override
      public String getValue(Tutorial tutorial) {
        return tutorial.name;
      }
    };

    // Create difficulty column
    TextColumn<Tutorial> difficultyColumn = new TextColumn<Tutorial>() {
      @Override
      public String getValue(Tutorial tutorial) {
        return tutorial.difficulty.toString();
      }
    };

    allTutorialsTable.addColumn(nameColumn, MESSAGES.walkthroughNameColumn());
    nameColumn.setSortable(true);
    allTutorialsTable.addColumn(difficultyColumn, MESSAGES.walkthroughDifficultyColumn());
    difficultyColumn.setSortable(true);

    // Create a data provider
    ListDataProvider<Tutorial> dataProvider = new ListDataProvider<Tutorial>();

    // Connect allTutorialsTable to the data provider
    dataProvider.addDataDisplay(allTutorialsTable);

    // Add the data to the data provider, which automatically pushes it to the
    // widget.
    List<Tutorial> list = dataProvider.getList();
    for (Tutorial tutorial : TUTORIALS) {
      list.add(tutorial);
    }

    ColumnSortEvent.ListHandler<Tutorial> columnSortHandler = new ColumnSortEvent.ListHandler(dataProvider.getList());
    columnSortHandler.setComparator(nameColumn, new Comparator<Tutorial>() {
      public int compare(Tutorial o1, Tutorial o2) {
        if (o1 == o2) {
          return 0;
        }

        // Compare the name columns.
        if (o1 != null) {
          return (o2 != null) ? o1.name.compareTo(o2.name) : 1;
        }
        return -1;
      }
    });
    columnSortHandler.setComparator(difficultyColumn, new Comparator<Tutorial>() {
      public int compare(Tutorial o1, Tutorial o2) {
        if (o1 == o2) {
          return 0;
        }

        // Compare the difficulty columns.
        if (o1 != null) {
          return (o2 != null) ? o1.difficulty.compareTo(o2.difficulty) : 1;
        }
        return -1;
      }
    });

    allTutorialsTable.addColumnSortHandler(columnSortHandler);
    allTutorialsTable.getColumnSortList().push(nameColumn);

    // Set the width of allTutorialsTable and put allTutorialsTable in fixed width mode
    allTutorialsTable.setWidth("100%", true);

    // Set the width of each column
    allTutorialsTable.setColumnWidth(nameColumn, 65.0, Unit.PCT);
    allTutorialsTable.setColumnWidth(difficultyColumn, 35.0, Unit.PCT);


    // a long-standing bug in GWT's celltable selection is that the first row will not be actively selected
    // when clicked on after the picker box first appears
    final SingleSelectionModel<Tutorial> selectionModel = new SingleSelectionModel<Tutorial>();
    allTutorialsTable.setSelectionModel(selectionModel);
    selectionModel.addSelectionChangeHandler(new Handler() {
       @Override
       public void onSelectionChange(SelectionChangeEvent event) {
         startButton.setEnabled(true);
       }
    });

    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }
    });
    startButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
        NewUserGetStarted.displayDialog();
        BlocklyPanel.callSetTutorial(selectionModel.getSelectedObject().fileName);
      }
    });


    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
    buttonPanel.add(closeButton);
    buttonPanel.add(startButton);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.add(allTutorialsTable);
    dialogPanel.add(buttonPanel);

    dialogPanel.setWidth("300px");
    dialogPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);

    dialogBox.setGlassEnabled(false);
    dialogBox.setModal(false);

    // Set the contents of the Widget
    dialogBox.setWidget(dialogPanel);
    dialogBox.show();
  }

  public static void displayDialog(){
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setStylePrimaryName("ode-DialogBox-Tutorial");
    dialogBox.getElement().setId("tutorialDialog");

    dialogBox.center();
    dialogBox.setAnimationEnabled(true);

    //button elements
    Button closeButton = new Button(MESSAGES.walkthroughCloseButton());
    closeButton.getElement().setId("closeButton");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }
    });

    Button nextButton = new Button(MESSAGES.walkthroughNextButton());
    nextButton.getElement().setId("nextButton");
    nextButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        BlocklyPanel.callNextStep();
      }
    });

    Button backButton = new Button(MESSAGES.walkthroughBackButton());
    backButton.getElement().setId("backButton");
    backButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        BlocklyPanel.callBackStep();
      }
    });

    HTML nextStepErrorMsg = new HTML(MESSAGES.walkthroughNextStepErrorText());
    nextStepErrorMsg.getElement().setId("nextStepErrorMsg");

    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
    buttonPanel.add(closeButton);
    buttonPanel.add(backButton);
    buttonPanel.add(nextButton);

    VerticalPanel dialogPanel = new VerticalPanel();
    dialogPanel.add(nextStepErrorMsg);
    dialogPanel.add(buttonPanel);

    dialogPanel.setWidth("300px");
    dialogPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);

    dialogBox.setGlassEnabled(false);
    dialogBox.setModal(false);
    dialogBox.setAutoHideEnabled(false);

    // Set the contents of the Widget
    dialogBox.setWidget(dialogPanel);
    dialogBox.show();
  }
}