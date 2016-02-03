package com.google.appinventor.client.explorer.youngandroid;

/*import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
*/

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

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;

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
    String content = "Pick a walkthrough:";
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setText(content);
    dialogBox.setStylePrimaryName("ode-DialogBox-Tutorial");
    dialogBox.getElement().setId("walkthroughPicker");

    //setting position of dialog box
    dialogBox.center();
    dialogBox.setAnimationEnabled(true);

    //elements
    Button closeButton = new Button("close");
    closeButton.getElement().setId("closeButton");
    final Button startButton = new Button("start");
    startButton.getElement().setId("startButton");
    startButton.setEnabled(false);

    final List<Tutorial> TUTORIALS = new ArrayList<Tutorial>();

    // for each tutorial, get the file name, the tutorial name and the difficulty level
    JsArray<JavaScriptTutorialData> tutorialMetaData = BlocklyPanel.callGetTutorialMetaData();


    for (int i = 0; i < tutorialMetaData.length(); i++) {
      JavaScriptTutorialData tutorial = tutorialMetaData.get(i);
      // add the new Tutorial object (name, difficulty, filename)
      TUTORIALS.add(new Tutorial(tutorial.getName(), Tutorial.Difficulty.fromString(tutorial.getDifficulty()), tutorial.getFileName()));
    }

    final CellTable<Tutorial> table = new CellTable<Tutorial>();

    // Create name column.
    TextColumn<Tutorial> nameColumn = new TextColumn<Tutorial>() {
      @Override
      public String getValue(Tutorial tutorial) {
        return tutorial.name;
      }
    };

    // Create difficulty column.
    TextColumn<Tutorial> difficultyColumn = new TextColumn<Tutorial>() {
      @Override
      public String getValue(Tutorial tutorial) {
        return tutorial.difficulty.toString();
      }
    };

    table.addColumn(nameColumn, "Name");
    nameColumn.setSortable(true);
    table.addColumn(difficultyColumn, "Difficulty");
    difficultyColumn.setSortable(true);

    // Create a data provider.
    ListDataProvider<Tutorial> dataProvider = new ListDataProvider<Tutorial>();

    // Connect the table to the data provider.
    dataProvider.addDataDisplay(table);

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
    table.addColumnSortHandler(columnSortHandler);

    table.getColumnSortList().push(nameColumn);

    // Set the width of the table and put the table in fixed width mode.
    table.setWidth("100%", true);

    // Set the width of each column.
    table.setColumnWidth(nameColumn, 65.0, Unit.PCT);
    table.setColumnWidth(difficultyColumn, 35.0, Unit.PCT);


    // a long-standing bug in GWT's celltable selection is that the first row will not be actively selected
    // when clicked on after the picker box first appears.
    final SingleSelectionModel<Tutorial> selectionModel = new SingleSelectionModel<Tutorial>();
    table.setSelectionModel(selectionModel);
    selectionModel.addSelectionChangeHandler(new Handler() {
       @Override
       public void onSelectionChange(SelectionChangeEvent event) {
         startButton.setEnabled(true);
       }
    });

    VerticalPanel dialogVPanel = new VerticalPanel();
    HorizontalPanel hPanel = new HorizontalPanel();
    hPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
    dialogVPanel.add(table);
    dialogVPanel.add(hPanel);

    dialogVPanel.setWidth("300px");
    dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
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

    hPanel.add(closeButton);
    hPanel.add(startButton);

    dialogBox.setGlassEnabled(false);
    dialogBox.setModal(true);


    // Set the contents of the Widget
    dialogBox.setWidget(dialogVPanel);
    dialogBox.show();
  }

  public static void displayDialog(){
    final DialogBox dialogBox = new DialogBox();
    dialogBox.setStylePrimaryName("ode-DialogBox-Tutorial");
    dialogBox.getElement().setId("tutorialDialog");

    dialogBox.center();
    dialogBox.setAnimationEnabled(true);

    //elements
    Button closeButton = new Button("close");
    closeButton.getElement().setId("closeButton");
    Frame frame = new Frame("");
    frame.getElement().setId("Tutorial_frame");
    Button nextButton = new Button("next");
    nextButton.getElement().setId("nextButton");
    Button backButton = new Button("back");
    backButton.getElement().setId("backButton");
    HTML nextStepErrorMsg = new HTML("It looks like you haven't finished this step yet. Try reading through all the instructions again to make sure you've finished the whole step.");
    nextStepErrorMsg.getElement().setId("nextStepErrorMsg");


    VerticalPanel dialogVPanel = new VerticalPanel();
    HorizontalPanel hPanel = new HorizontalPanel();
    hPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
    // dialogVPanel.add(frame);
    dialogVPanel.add(nextStepErrorMsg);
    dialogVPanel.add(hPanel);

    dialogVPanel.setWidth("300px");
    dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialogBox.hide();
      }
    });
    //next button

    nextButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        BlocklyPanel.callNextStep();
      }
    });

    backButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        BlocklyPanel.callBackStep();
      }
    });

    hPanel.add(closeButton);
    hPanel.add(backButton);
    hPanel.add(nextButton);

    dialogBox.setGlassEnabled(false);
    dialogBox.setModal(false);
    dialogBox.setAutoHideEnabled(false);

    // Set the contents of the Widget
    dialogBox.setWidget(dialogVPanel);
    dialogBox.show();
  }
}