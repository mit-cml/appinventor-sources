package com.google.appinventor.client.editor.youngandroid.properties;

import com.google.appinventor.client.editor.youngandroid.YaFormEditor;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.List;

public class YoungAndroidImageClassifierPropertyEditor extends PropertyEditor {
    // Constants for JSON keys
    private static final String CLASSES_KEY = "classes";
    private static final String CLASS_NAME_KEY = "name";
    private static final String CLASS_IMAGES_KEY = "images";
    private static final String TRAIN_PARAMS_KEY = "trainParams";
    private static final String EPOCHS_KEY = "epochs";
    private static final String LEARNING_RATE_KEY = "learningRate";
    private static final String OPTIMIZER_KEY = "optimizer";
    private static final String TRAIN_FRACTION_KEY = "trainFraction";
    private YaFormEditor editor;

    private JSONObject json;

    public YoungAndroidImageClassifierPropertyEditor(YaFormEditor editor) {
        this.editor = editor;
        Button trainModel = new Button("Create/Train Model");
        trainModel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                TrainModelDialog dialog = new TrainModelDialog();
                dialog.center();
                dialog.show();
            }
        });
        initWidget(trainModel);
    }

    @Override
    protected void updateValue() {
        String propValue = property != null ? property.getValue() : null;
        if (propValue != null && !propValue.isEmpty()) {
            this.json = JSONParser.parseStrict(propValue).isObject();
        } else {
            this.json = new JSONObject();
            initDefaultJson();
        }
    }

    /**
     * Initialize JSON with default values
     */
    private void initDefaultJson() {
        json.put(CLASSES_KEY, new JSONArray());

        // Initialize training parameters
        JSONObject trainParams = new JSONObject();
        trainParams.put(EPOCHS_KEY, new JSONNumber(50));
        trainParams.put(LEARNING_RATE_KEY, new JSONNumber(0.001));
        trainParams.put(OPTIMIZER_KEY, new JSONString("adam"));
        trainParams.put(TRAIN_FRACTION_KEY, new JSONNumber(80));
        json.put(TRAIN_PARAMS_KEY, trainParams);
    }

    /**
     * Dialog box for training the model
     */
    class TrainModelDialog extends DialogBox {
        private final ListBox tabPanel;
        private final DeckPanel tabContentContainer;
        private FlowPanel classificationsPanel;
        private FlowPanel trainingPanel;
        private FlowPanel previewPanel;
        private FlowPanel labelsContainer;
        private final List<Classification> classifications = new ArrayList<>();
        //        private ProgressBar trainingProgress;
        private Label progressStatus;

        private TextBox epochTextBox;
        private TextBox learningRateTextBox;
        private ListBox optimizerList;
        private TextBox trainFractionTextBox;

        public TrainModelDialog() {
            addStyleName("ode-ImageClassifierDialog");
            setText("Image Teaching Model Properties");

            // Load classes from JSON
            loadClassificationsFromJson();

            // Main panel
            VerticalPanel mainPanel = new VerticalPanel();

            HorizontalPanel middleContainer = new HorizontalPanel();

            ScrollPanel contentScrollPanel = new ScrollPanel();
            tabContentContainer = new DeckPanel();
            contentScrollPanel.setStylePrimaryName("ode-ImageClassifierDialogScrollPanel");
            contentScrollPanel.add(tabContentContainer);


            // Create tab panel
            tabPanel = new ListBox();
            tabPanel.setStyleName("ode-ImageClassifierDialogTabsListBox");
            tabPanel.addItem("Classifications");
            tabPanel.addItem("Train");
            tabPanel.addItem("Testing");

            tabPanel.getElement().getStyle().setProperty("height", "400px");
            middleContainer.add(tabPanel);

            // creating tab content

            createLabelsTab();
            createTrainingTab();
            createPreviewTab();

            tabContentContainer.add(this.classificationsPanel);
            tabContentContainer.add(this.trainingPanel);
            tabContentContainer.add(this.previewPanel);

            middleContainer.add(contentScrollPanel);
            mainPanel.add(middleContainer);
            FlowPanel buttonsPanel = getActionButtons();
            mainPanel.add(buttonsPanel);
            mainPanel.setCellHorizontalAlignment(buttonsPanel, VerticalPanel.ALIGN_RIGHT);

            tabPanel.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent changeEvent) {
                    tabContentContainer.showWidget(tabPanel.getSelectedIndex());
                    saveDataToJson();
                }
            });

            tabPanel.setVisibleItemCount(tabContentContainer.getWidgetCount());
            tabContentContainer.showWidget(0);

            setWidget(mainPanel);

            initWebcam();
        }

        private FlowPanel getActionButtons() {
            FlowPanel buttonsPanel = new FlowPanel();
            buttonsPanel.setStyleName("ode-ImageClassifierDialogActionButtonContainer");

            Button cancelButton = new Button("Cancel");
            cancelButton.setStyleName("secondary");
            cancelButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    hide();
                }
            });

            Button okButton = new Button("OK");
            okButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    GWT.log("OK button clicked - saving data and updating value");
                    saveDataToJson();
                    updateValue();
                    hide();
                }
            });

            buttonsPanel.add(cancelButton);
            buttonsPanel.add(okButton);
            return buttonsPanel;
        }

        private ListBox dropdown;

        private void createLabelsTab() {
            classificationsPanel = new FlowPanel();

            FlowPanel actionButtons = new FlowPanel();
            actionButtons.setStyleName("ode-ImageClassifierDialogLabelsActionButton");
            actionButtons.getElement().getStyle().setMarginBottom(5, Unit.PX);

            TextBox className = new TextBox();
            className.setStyleName("input");
            actionButtons.add(className);

            Button addClass = new Button("Add Class");
            addClass.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    addClass(className.getText());
                }
            });
            addClass.setStyleName("ode-ImageClassifierDialog button");

            Button uploadModel = new Button("Upload Model");
            uploadModel.setStyleName("secondary");
            uploadModel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {

                }
            });

            Button uploadTrainingData = new Button("Upload Training Data");
            uploadTrainingData.setStyleName("secondary");
            uploadTrainingData.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {

                }
            });

            actionButtons.add(addClass);
            actionButtons.add(uploadModel);
            actionButtons.add(uploadTrainingData);
            classificationsPanel.add(actionButtons);

            FlowPanel classificationsHolder = new FlowPanel();
            classificationsHolder.setStyleName("ode-ImageClassifierDialogClassificationPanel");

            FlowPanel cameraHolder = new FlowPanel();
            cameraHolder.setStyleName("webcam-holder");

            FlowPanel dropdownHolder = new FlowPanel();
            dropdownHolder.setStyleName("dropdown-holder");

            Label label = new Label("Capturing for");
            dropdownHolder.add(label);

            dropdown = new ListBox();
            dropdownHolder.add(dropdown);

            cameraHolder.add(dropdownHolder);
            cameraHolder.add(new HTML("<video id='webcam' width='200' autoplay></video>"));

            Button capture = new Button("Capture Image");
            capture.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    String image = captureFromWebcam();
                    classifications.get(dropdown.getSelectedIndex()).addImage(image);
                    refreshClassesTable();
                }
            });
            capture.setStyleName("ode-ImageClassifierDialog button");
            cameraHolder.add(capture);

            classificationsHolder.add(cameraHolder);

            // Classes table
            labelsContainer = new FlowPanel();
            labelsContainer.setStyleName("ode-ImageClassifierDialogLabelsList");

            classificationsHolder.add(labelsContainer);

            classificationsPanel.add(classificationsHolder);

            // Populate classes table
            refreshClassesTable();
            className.setText("Label " + dropdown.getItemCount());
        }

        private native void initWebcam() /*-{
        function webcam(){
            var video = $doc.getElementById('webcam');
            console.log(video);
            navigator.mediaDevices.getUserMedia({ video: {facingMode: "user"} })
                .then(function(stream) {
                  video.srcObject = stream;
                });
                }
                setTimeout(webcam, 100);
      }-*/;

        private native String captureFromWebcam() /*-{
        var video = $doc.getElementById('webcam');
        var canvas = $doc.createElement('canvas');
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;

        var context = canvas.getContext('2d');
        context.drawImage(video, 0, 0, canvas.width, canvas.height);

        // Get the image as base64 encoded data URL
        return canvas.toDataURL('image/jpeg');
      }-*/;

        /**
         * Create Training tab content
         */
        private void createTrainingTab() {
            trainingPanel = new FlowPanel();
            trainingPanel.setStyleName("ode-TrainingPanel");

            // Title
            Label title = new Label("Training Parameters");
            title.addStyleName("ode-SectionTitle");
            trainingPanel.add(title);

            // Training parameters grid
            Grid paramGrid = new Grid(5, 3);
            paramGrid.setCellSpacing(10);
            paramGrid.setWidth("100%");

            // Load training parameters from JSON
            JSONObject trainParams = getTrainParams();

            // Epochs
            paramGrid.setWidget(0, 0, new Label("Epochs:"));
            // Training parameters widgets
            epochTextBox = new TextBox();
            epochTextBox.setText(String.valueOf((int) trainParams.get(EPOCHS_KEY).isNumber().doubleValue()));
            epochTextBox.setWidth("100px");
            paramGrid.setWidget(0, 1, epochTextBox);
            paramGrid.setWidget(0, 2, createTooltip("Number of complete passes through the training dataset"));

            // Learning Rate
            paramGrid.setWidget(2, 0, new Label("Learning Rate:"));
            learningRateTextBox = new TextBox();
            learningRateTextBox.setText(String.valueOf(trainParams.get(LEARNING_RATE_KEY).isNumber().doubleValue()));
            learningRateTextBox.setWidth("100px");
            paramGrid.setWidget(2, 1, learningRateTextBox);
            paramGrid.setWidget(2, 2, createTooltip("Step size at each iteration while moving toward a minimum of the loss function"));

            // Optimizer
            paramGrid.setWidget(3, 0, new Label("Optimizer:"));
            optimizerList = new ListBox();
            optimizerList.addItem("Adam", "adam");
            optimizerList.addItem("SGD", "sgd");
            optimizerList.addItem("RMSprop", "rmsprop");

            // Set selected optimizer from JSON
            String optimizer = trainParams.get(OPTIMIZER_KEY).isString().stringValue();
            for (int i = 0; i < optimizerList.getItemCount(); i++) {
                if (optimizerList.getValue(i).equals(optimizer)) {
                    optimizerList.setSelectedIndex(i);
                    break;
                }
            }

            paramGrid.setWidget(3, 1, optimizerList);
            paramGrid.setWidget(3, 2, createTooltip("Algorithm used to update the weights of the network"));

            // Training Data Fraction
            paramGrid.setWidget(4, 0, new Label("Training Data Fraction:"));
            HorizontalPanel fractionPanel = new HorizontalPanel();
            trainFractionTextBox = new TextBox();
            trainFractionTextBox.setText(String.valueOf(trainParams.get(TRAIN_FRACTION_KEY).isNumber().doubleValue()));
            trainFractionTextBox.setWidth("50px");
            fractionPanel.add(trainFractionTextBox);
            fractionPanel.add(new Label("%"));
            paramGrid.setWidget(4, 1, fractionPanel);
            paramGrid.setWidget(4, 2, createTooltip("Percentage of data used for training vs. validation"));

            trainingPanel.add(paramGrid);

            // Action buttons
            HorizontalPanel actionButtons = new HorizontalPanel();
            actionButtons.setSpacing(5);
            actionButtons.getElement().getStyle().setMarginTop(20, Unit.PX);

            Button startTraining = new Button("Start Training");
            startTraining.setStyleName("ode-ImageClassifierDialog button");
            startTraining.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    // Start training logic would go here
                    showTrainingProgress();
                }
            });

            Button resetDefaults = new Button("Reset Defaults");
            resetDefaults.setStyleName("secondary");
            resetDefaults.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    resetTrainingParams();
                }
            });

            actionButtons.add(startTraining);
            actionButtons.add(resetDefaults);
            trainingPanel.add(actionButtons);

            // Training progress section (initially hidden)
            FlowPanel progressPanel = new FlowPanel();
            progressPanel.getElement().getStyle().setMarginTop(30, Unit.PX);
            progressPanel.setVisible(false);

            Label progressTitle = new Label("Training Progress");
            progressTitle.addStyleName("ode-SectionTitle");
            progressPanel.add(progressTitle);

//  trainingProgress = new Progress(0, 100);
//  trainingProgress.setWidth("100%");
//  trainingProgress.setHeight("12px");
//  trainingProgress.addStyleName("ode-ProgressBar");
//  progressPanel.add(trainingProgress);

            progressStatus = new Label("Epoch 0/0 - Loss: 0.0 - Accuracy: 0.0%");
            progressStatus.addStyleName("ode-ProgressStatus");
            progressPanel.add(progressStatus);

            trainingPanel.add(progressPanel);
        }

        /**
         * Create Preview tab content
         */
        private void createPreviewTab() {
            previewPanel = new FlowPanel();
            previewPanel.setStyleName("PreviewPanel");

            FlowPanel classificationsHolder = new FlowPanel();
            classificationsHolder.setStyleName("ode-ImageClassifierDialogClassificationPanel");

            FlowPanel cameraHolder = new FlowPanel();
            cameraHolder.setStyleName("webcam-holder");
            cameraHolder.add(new HTML("<video id='webcam' width='200' autoplay></video>"));
            classificationsHolder.add(cameraHolder);

            Button capture = new Button("Capture Image");
            capture.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    String image = captureFromWebcam();
                    classifications.get(dropdown.getSelectedIndex()).addImage(image);
                    refreshClassesTable();
                }
            });
            capture.setStyleName("ode-ImageClassifierDialog button");
            cameraHolder.add(capture);

            classificationsHolder.add(cameraHolder);

            FlowPanel resultContainer = new FlowPanel();
            resultContainer.setStyleName("ode-ImageClassifierDialogLabelsList");

            classificationsHolder.add(resultContainer);

            previewPanel.add(classificationsHolder);

            // Preview image
//            FlowPanel imagePanel = new FlowPanel();
//            imagePanel.addStyleName("ode-PreviewImage");
//
//            // Placeholder image
//            Image placeholderImage = new Image();
//            placeholderImage.setUrl("/ode/images/placeholder.png");
////            placeholderImage.setAltText("Preview image");
//            imagePanel.add(placeholderImage);
//
//            previewPanel.add(imagePanel);

            // Results panel
            FlowPanel resultsPanel = new FlowPanel();
            resultsPanel.addStyleName("ode-PreviewResults");
            resultsPanel.setHeight("100px");

            // Placeholder results (would be populated when model runs)
            FlexTable resultsTable = new FlexTable();
            resultsTable.setWidth("100%");

            // Sample results
            resultsTable.setText(0, 0, "Class");
            resultsTable.setText(0, 1, "Confidence");
            resultsTable.getRowFormatter().addStyleName(0, "ode-ResultsHeader");

            resultsTable.setText(1, 0, "No results yet");
            resultsTable.setText(1, 1, "-");

            resultsPanel.add(resultsTable);
            previewPanel.add(resultsPanel);

            // Export/Import buttons
            HorizontalPanel exportButtons = new HorizontalPanel();
            exportButtons.setSpacing(5);
            exportButtons.getElement().getStyle().setMarginTop(15, Unit.PX);

            Button exportModel = new Button("Export Model");
            exportModel.setStyleName("secondary");
            exportModel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    // Export model functionality would go here
                }
            });

            Button importModel = new Button("Export Training Data");
            importModel.setStyleName("secondary");
            importModel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    // Import model functionality would go here
                }
            });

            exportButtons.add(exportModel);
            exportButtons.add(importModel);
            previewPanel.add(exportButtons);
        }

        /**
         * Show training progress panel
         */
        private void showTrainingProgress() {
            // Get the progress panel and make it visible
            FlowPanel progressPanel = (FlowPanel) trainingPanel.getWidget(3);
            progressPanel.setVisible(true);

            // Set initial progress
//            trainingProgress.setProgress(0);
            int epochs = Integer.parseInt(epochTextBox.getValue());
            progressStatus.setText("Epoch 0/" + epochs + " - Loss: 0.0 - Accuracy: 0.0%");

            // In a real implementation, you would start the training process here
            // and update the progress bar over time
        }

        /**
         * Reset training parameters to defaults
         */
        private void resetTrainingParams() {
            epochTextBox.setValue("50");
            epochTextBox.setValue("50");
            learningRateTextBox.setText("0.001");

            // Set optimizer to Adam
            for (int i = 0; i < optimizerList.getItemCount(); i++) {
                if (optimizerList.getValue(i).equals("adam")) {
                    optimizerList.setSelectedIndex(i);
                    break;
                }
            }

            trainFractionTextBox.setValue("80");
        }

        /**
         * Create a tooltip widget
         */
        private Widget createTooltip(String text) {
            Label tooltip = new Label("?");
            tooltip.setTitle(text);
            tooltip.addStyleName("ode-Tooltip");
            return tooltip;
        }

        /**
         * Add a new class with given name
         */
        private void addClass(String className) {
            Classification classification = new Classification(className);
            classifications.add(classification);
            refreshClassesTable();
        }

        /**
         * Refresh the classes table with current data
         */
        private void refreshClassesTable() {
            if (labelsContainer == null) return;
            while (labelsContainer.getWidgetCount() != 0) {
                labelsContainer.remove(0);
            }

            dropdown.clear();

            for (int i = 0; i < classifications.size(); i++) {
                final Classification classification = classifications.get(i);
                final int row = i;

                FlowPanel label = new FlowPanel();
                label.setStyleName("ode-ImageClassifierDialogLabelsListRow");

                FlowPanel header = new FlowPanel();
                header.setStyleName("ode-ImageClassifierDialogLabelItemFirstRow");
                Label nameLabel = new Label();
                nameLabel.setText(classification.getName());
                nameLabel.setStyleName("className");
                header.add(nameLabel);

                Button deleteBtn = new Button("<svg width=\"16\" height=\"16\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"#000\" stroke-width=\"2\">\n" +
                        "                    <polyline points=\"3 6 5 6 21 6\"></polyline>\n" +
                        "                    <path d=\"M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2\"></path>\n" +
                        "                  </svg>");
                deleteBtn.getElement().getStyle().setProperty("border", "0");
                deleteBtn.getElement().getStyle().setProperty("padding", "0");
                deleteBtn.getElement().getStyle().setProperty("background", "transparent");
                deleteBtn.setStyleName("secondary");
                deleteBtn.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        classifications.remove(row);
                        refreshClassesTable();
                    }
                });
                header.add(deleteBtn);

                label.add(header);

                dropdown.addItem(classification.getName());

                FlowPanel secondRow = new FlowPanel();
                secondRow.setStyleName("SecondRow");

                Label countLabel = new Label(classification.getImageCount() + " images");
                countLabel.setStyleName("image-count");
                secondRow.add(countLabel);

                label.add(secondRow);

                // Image thumbnails
                if (classification.getImageCount() > 0) {
                    ScrollPanel imageContainer = new ScrollPanel();
                    imageContainer.setStylePrimaryName("ImageContainerScrollPanel");
                    FlowPanel imageContentContainer = new FlowPanel();
                    imageContentContainer.setStyleName("ImageContentContainer");
                    imageContainer.add(imageContentContainer);
                    secondRow.add(imageContainer);

                    for (int j = 0; j < classification.getImageCount(); j++) {
                        Image thumb = new Image();
                        thumb.setUrl(classification.getImages().get(j));
                        thumb.setSize("48px", "48px");
                        thumb.setStyleName("Thumbnail");
                        imageContentContainer.add(thumb);
                    }

                }

                labelsContainer.add(label);

            }
        }

        /**
         * Load classes from JSON
         */
        private void loadClassificationsFromJson() {
            classifications.clear();

            JSONValue classesValue = json.get(CLASSES_KEY);
            if (classesValue != null && classesValue.isArray() != null) {
                JSONArray classesArray = classesValue.isArray();
                for (int i = 0; i < classesArray.size(); i++) {
                    JSONObject classObj = classesArray.get(i).isObject();
                    if (classObj != null) {
                        String className = classObj.get(CLASS_NAME_KEY).isString().stringValue();
                        Classification classification = new Classification(className);

                        JSONValue imagesValue = classObj.get(CLASS_IMAGES_KEY);
                        if (imagesValue != null && imagesValue.isArray() != null) {
                            JSONArray imagesArray = imagesValue.isArray();
                            for (int j = 0; j < imagesArray.size(); j++) {
                                String imagePath = imagesArray.get(j).isString().stringValue();
                                classification.addImage(imagePath);
                            }
                        }

                        classifications.add(classification);
                    }
                }
            }

            // If no classes loaded, add default
            if (classifications.isEmpty()) {
                addClass("Label 1");
            }

            printJson(property.getValue());
        }

        private native void printJson(String json) /*-{
            console.log(json);
        }-*/;

        /**
         * Get training parameters from JSON
         */
        private JSONObject getTrainParams() {
            JSONValue trainParamsValue = json.get(TRAIN_PARAMS_KEY);
            if (trainParamsValue != null && trainParamsValue.isObject() != null) {
                return trainParamsValue.isObject();
            }

            // If not found, create default
            JSONObject trainParams = new JSONObject();
            trainParams.put(EPOCHS_KEY, new JSONNumber(50));
            trainParams.put(LEARNING_RATE_KEY, new JSONNumber(0.001));
            trainParams.put(OPTIMIZER_KEY, new JSONString("adam"));
            trainParams.put(TRAIN_FRACTION_KEY, new JSONNumber(80));

            json.put(TRAIN_PARAMS_KEY, trainParams);
            return trainParams;
        }

        /**
         * Save all data to JSON
         */
        private void saveDataToJson() {
            // Save classes
            JSONArray classesArray = new JSONArray();
            for (int i = 0; i < classifications.size(); i++) {
                Classification classification = classifications.get(i);
                JSONObject classObj = new JSONObject();
                classObj.put(CLASS_NAME_KEY, new JSONString(classification.getName()));

                // Save images
                JSONArray imagesArray = new JSONArray();
                List<String> images = classification.getImages();
                for (int j = 0; j < images.size(); j++) {
                    imagesArray.set(j, new JSONString(images.get(j)));
                }
                classObj.put(CLASS_IMAGES_KEY, imagesArray);

                classesArray.set(i, classObj);
            }
            json.put(CLASSES_KEY, classesArray);

            // Save training parameters
            JSONObject trainParams = new JSONObject();
            trainParams.put(EPOCHS_KEY, new JSONNumber(Integer.parseInt(epochTextBox.getValue())));
            trainParams.put(LEARNING_RATE_KEY, new JSONNumber(Double.parseDouble(learningRateTextBox.getValue())));
            trainParams.put(OPTIMIZER_KEY, new JSONString(optimizerList.getValue(optimizerList.getSelectedIndex())));
            trainParams.put(TRAIN_FRACTION_KEY, new JSONNumber(Integer.parseInt(trainFractionTextBox.getValue())));

            json.put(TRAIN_PARAMS_KEY, trainParams);
            printJson(json.toString());
            property.setValue(json.toString());
        }
    }

    /**
     * Class to hold data for a single classification class
     */
    static class Classification {
        private String name;
        private final List<String> images;

        public Classification(String name) {
            this.name = name;
            this.images = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getImages() {
            return images;
        }

        public void addImage(String imagePath) {
            images.add(imagePath);
        }

        public int getImageCount() {
            return images.size();
        }
    }
}