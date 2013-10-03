// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;

import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.client.GalleryClient;
import com.google.appinventor.client.OdeAsyncCallback;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import com.google.gwt.user.client.ui.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import com.google.appinventor.client.wizards.NewProjectWizard.NewProjectCommand;
import com.google.appinventor.shared.rpc.project.UserProject;

import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.user.client.Window;
/**
 * The gallery list shows apps from the gallery in a table.
 *
 * <p> The project name and date created will be shown in the table.
 *
 * @author wolberd@google.com (Dave Wolber)
 */
public class GalleryList extends Composite {
  private enum SortField {
    NAME,
    DATE,
  }
  private enum SortOrder {
    ASCENDING,
    DESCENDING,
  }
  private  List<GalleryApp> apps;
  private final List<GalleryApp> selectedApps;
  private final Map<GalleryApp, ProjectWidgets> projectWidgets;
  private SortField sortField;
  private SortOrder sortOrder;

  // UI elements
  private final Grid table;
  private final Label nameSortIndicator;
  private final Label dateSortIndicator;

  /**
   * Creates a new GalleryList
   */
  public GalleryList() {
    //apps = new ArrayList<GalleryApp>();
	GalleryClient gallery = new GalleryClient();
	//apps = gallery.generateFakeApps();
    selectedApps = new ArrayList<GalleryApp>();
    projectWidgets = new HashMap<GalleryApp, ProjectWidgets>();
    /*
    for (GalleryApp app : apps) {
    	projectWidgets.put(app, new ProjectWidgets(app));
    }
    */
    
    sortField = SortField.NAME;
    sortOrder = SortOrder.ASCENDING;

    // Initialize UI
    table = new Grid(1, 4); // The table initially contains just the header row.
    table.addStyleName("ode-ProjectTable");
    table.setWidth("100%");
    table.setCellSpacing(0);
    nameSortIndicator = new Label("");
    dateSortIndicator = new Label("");
    refreshSortIndicators();
    setHeaderRow();

    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");

    panel.add(table);
    initWidget(panel);
    
    getApps("http://gallery.appinventor.mit.edu/rpc?tag=featured");
    
    //GalleryClient client = new GalleryClient();
	//apps = client.generateFakeApps();
	

    // It is important to listen to project manager events as soon as possible.
    //Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
  }

  /**
   * Adds the header row to the table.
   *
   */
  private void setHeaderRow() {
    table.getRowFormatter().setStyleName(0, "ode-ProjectHeaderRow");

    HorizontalPanel nameHeader = new HorizontalPanel();
    final Label nameHeaderLabel = new Label(MESSAGES.projectNameHeader());
    nameHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    nameHeader.add(nameHeaderLabel);
    nameSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    nameHeader.add(nameSortIndicator);
    table.setWidget(0, 1, nameHeader);
    
    HorizontalPanel imageHeader = new HorizontalPanel();
    Label imageHeaderLabel = new Label("image");
    imageHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    imageHeader.add(imageHeaderLabel);
    //dateSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    //imageHeader.add(dateSortIndicator);
    table.setWidget(0, 2, imageHeader);

    HorizontalPanel dateHeader = new HorizontalPanel();
    Label dateHeaderLabel = new Label(MESSAGES.projectDateHeader());
    dateHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
    dateHeader.add(dateHeaderLabel);
    dateSortIndicator.addStyleName("ode-ProjectHeaderLabel");
    dateHeader.add(dateSortIndicator);
    table.setWidget(0, 3, dateHeader);
    
    
   

    MouseDownHandler mouseDownHandler = new MouseDownHandler() {
     // @Override
      public void onMouseDown(MouseDownEvent e) {
        SortField clickedSortField =
            (e.getSource() == nameHeaderLabel || e.getSource() == nameSortIndicator)
            ? SortField.NAME
            : SortField.DATE;
        changeSortOrder(clickedSortField);
      }
    };
    nameHeaderLabel.addMouseDownHandler(mouseDownHandler);
    nameSortIndicator.addMouseDownHandler(mouseDownHandler);
    dateHeaderLabel.addMouseDownHandler(mouseDownHandler);
    dateSortIndicator.addMouseDownHandler(mouseDownHandler);
  }

  private void changeSortOrder(SortField clickedSortField) {
    if (sortField != clickedSortField) {
      sortField = clickedSortField;
      sortOrder = SortOrder.ASCENDING;
    } else {
      if (sortOrder == SortOrder.ASCENDING) {
        sortOrder = SortOrder.DESCENDING;
      } else {
        sortOrder = SortOrder.ASCENDING;
      }
    }
  //  refreshTable(true);
  }

  private void refreshSortIndicators() {
    String text = (sortOrder == SortOrder.ASCENDING)
        ? "\u25B2"      // up-pointing triangle
        : "\u25BC";     // down-pointing triangle
    switch (sortField) {
      case NAME:
        nameSortIndicator.setText(text);
        dateSortIndicator.setText("");
        break;
      case DATE:
        dateSortIndicator.setText(text);
        nameSortIndicator.setText("");
        break;
    }
  }

  private class ProjectWidgets {
    final CheckBox checkBox;
    final Label nameLabel;
    final Label dateLabel;
    final Image image;

    private ProjectWidgets(final GalleryApp app) {
      checkBox = new CheckBox();
      checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
       // @Override
        public void onValueChange(ValueChangeEvent<Boolean> event) {
          boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
          int row = 1 + apps.indexOf(app);
          if (isChecked) {
            table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
            selectedApps.add(app);
          } else {
            table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
            selectedApps.remove(app);
          }
          Ode.getInstance().getProjectToolbar().updateButtons();
        }
      });

      nameLabel = new Label(app.getTitle());
      nameLabel.addClickHandler(new ClickHandler() {
      //  @Override
        public void onClick(ClickEvent event) {
          //Ode.getInstance().openYoungAndroidProjectInDesigner(app);
          loadGalleryZip(app.getTitle(),app.getZipURL());
        }
      });
      nameLabel.addStyleName("ode-ProjectNameLabel");

      //Date date = new Date(app.getDate());
      //DateTimeFormat dateTimeFormat = DateTimeFormat.getMediumDateTimeFormat();
      dateLabel = new Label(app.getCreationDate());
      image = new Image();
      image.setUrl(app.getImageURL());
      
    }
  }

  private void refreshTable(List<GalleryApp> apps, boolean needToSort) {
    /*if (needToSort) {
      // Sort the projects.
      Comparator<GalleryApp> comparator;
      switch (sortField) {
        default:
        case NAME:
          comparator = (sortOrder == SortOrder.ASCENDING)
              ? ProjectComparators.COMPARE_BY_NAME_ASCENDING
              : ProjectComparators.COMPARE_BY_NAME_DESCENDING;
          break;
        case DATE:
          comparator = (sortOrder == SortOrder.ASCENDING)
              ? ProjectComparators.COMPARE_BY_DATE_ASCENDING
              : ProjectComparators.COMPARE_BY_DATE_DESCENDING;
          break;
      }
      Collections.sort(apps, comparator);
    }*/
    projectWidgets.put(apps.get(0), new ProjectWidgets(apps.get(0)));
	projectWidgets.put(apps.get(1), new ProjectWidgets(apps.get(1)));
    refreshSortIndicators();
    
    // Refill the table.
    table.resize(1 + apps.size(), 4);
    int row = 1;
    for (GalleryApp app : apps) {
      ProjectWidgets pw = projectWidgets.get(app);
      if (selectedApps.contains(app)) {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
        pw.checkBox.setValue(true);
      } else {
        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
        pw.checkBox.setValue(false);
      }
      table.setWidget(row, 0, pw.checkBox);
      table.setWidget(row, 1, pw.nameLabel);
      table.setWidget(row,2,pw.image);
      table.setWidget(row, 3, pw.dateLabel);
      
      row++;
    }

    //Ode.getInstance().getProjectToolbar().updateButtons();
  }

  /**
   * Gets the number of projects
   *
   * @return the number of projects
   */
  public int getNumProjects() {
    return apps.size();
  }

  /**
   * Gets the number of selected projects
   *
   * @return the number of selected projects
   */
  public int getNumSelectedApps() {
    return selectedApps.size();
  }

  /**
   * Returns the list of selected projects
   *
   * @return the selected projects
   */
  public List<GalleryApp> getSelectedApps() {
    return selectedApps;
  }
  
  public void getApps(String url)
  {
	  
	// Callback for when the server returns us the apps
	    final Ode ode = Ode.getInstance();
	    // was string
	    final OdeAsyncCallback<List<GalleryApp>> callback = new OdeAsyncCallback<List<GalleryApp>>(
			      // failure message
	      MESSAGES.galleryError()) {
	        @Override
	        public void onSuccess(List<GalleryApp> list) {
	        // the server has returned us something
	        if (list== null) {

	          Window.alert("api call: no data returned from service");
	       
			  return;
		    }
		    // things are good so lets refresh the list
		   
			refreshTable(list,false);
	        //Window.alert("api call: got some data:"+list.get(0).getTitle());
	      }
	    };
	   // ok, this is below the call back, but of course it is done first 
	   ode.getProjectService().getApps(callback);
	  
	/* we were trying to directly call api
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
   
    Request response=null;
	try {
	     response = builder.sendRequest(null, new RequestCallback() {
        public void onError(Request request, Throwable exception) {
        // Code omitted for clarity
      	  
        }

        public void onResponseReceived(Request request, Response response) {
        // Code omitted for clarity
        	int status=response.getStatusCode();
        	
        	OdeLog.log("gallery: got a response " + String.valueOf(status));
        	//refreshTable(false);
        
        }
  
        });
    } catch (RequestException e) {
    	// Code omitted for clarity
    	
    }
    */
  
  }
  
  public void loadGalleryZip(final String projectName, String zipURL) {
	final NewProjectCommand onSuccessCommand = new NewProjectCommand() {
       @Override
       public void execute(Project project) {
            Ode.getInstance().openYoungAndroidProjectInDesigner(project);
       }
    };

    // Callback for updating the project explorer after the project is created on the back-end
    final Ode ode = Ode.getInstance();
    final OdeAsyncCallback<UserProject> callback = new OdeAsyncCallback<UserProject>(
		      // failure message
      MESSAGES.createProjectError()) {
      @Override
      public void onSuccess(UserProject projectInfo) {
      // Update project explorer -- i.e., display in project view
      if (projectInfo == null) {

        Window.alert("This template has no zip file. Creating a new project with name = " + projectName);
        ode.getProjectService().newProject(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE,
		            projectName,
		            new NewYoungAndroidProjectParameters(projectName),
		            this);
		return;
	  }
      Project project = ode.getProjectManager().addProject(projectInfo);
      if (onSuccessCommand != null) {
        onSuccessCommand.execute(project);
      }
    }
   };
   RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, zipURL);
   try {
      Request response = builder.sendRequest(null, new RequestCallback() {
      @Override
      public void onError(Request request, Throwable exception) {
         Window.alert("Unable to load Gallery zip file");
      }
      @Override
      public void onResponseReceived(Request request, Response response) {
          Window.alert("response:"+String.valueOf(response.getStatusCode()));
    	  ode.getProjectService().newProjectFromExternalTemplate(projectName,response.getText(),callback);
      }

      });
    } catch (RequestException e) {
        Window.alert("Error fetching project zip file template.");
    } 
  } 
}	  
  
  

 
