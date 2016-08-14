package com.google.appinventor.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.appinventor.client.widgets.TextButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.appinventor.client.output.OdeLog;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ListBox;


import java.util.*;
import java.util.ArrayList;
import java.util.List;
import javax.script.*;
import  java.lang.Double.*;


import java.lang.String;

public class SearchBox extends Composite {

    private BlocklyPanel blockArea;
    private SuggestBox searchBox;
    private CheckBox advancedCheckBox;
    private ListBox filterBox;
    private TextButton upButton, downButton,clearButton;
    private char upArrow = '\u2191';
    private char downArrow = '\u2193';
    public int flag;

    public SearchBox(BlocklyPanel panel) {
        

        /* Panel*/
        blockArea = panel;
        HorizontalPanel hPanel = new HorizontalPanel();

        /*Up and Down Button */
        upButton = new TextButton(Character.toString(upArrow));
        upButton.setHeight("20px");
        upButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent click) {
                blockArea.zoomToSearchBlock(1);
            }
        });

        downButton = new TextButton(Character.toString(downArrow));
        downButton.setHeight("20px");
        downButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent click) {
                blockArea.zoomToSearchBlock(-1);
            }
        });

        /* Search Box */
        MultiWordSuggestOracle blockNames = new MultiWordSuggestOracle();
        searchBox = new SuggestBox(blockNames);
        searchBox.setTitle("Search Blocks");
        searchBox.setHeight("20px");
        searchBox.getElement().setAttribute("placeholder","Search Blocks...");
        searchBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String queried = searchBox.getText();
                int keyCode = event.getNativeKeyCode();
                if ((keyCode == KeyCodes.KEY_ENTER) && (queried != "")) {
                    if (advancedCheckBox.getValue() == false){
                        blockArea.startSearch(queried,"None");
                    }else{
                        blockArea.startSearch(queried,filterBox.getSelectedItemText()); 
                    }
                    upButton.setEnabled(true);
                    downButton.setEnabled(true);
                    flag = 0;
                } else if ((keyCode == KeyCodes.KEY_ESCAPE) || (keyCode == KeyCodes.KEY_DELETE) || (keyCode == KeyCodes.KEY_BACKSPACE)) {
                    blockArea.stopSearch();
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);
                } else if ((keyCode == KeyCodes.KEY_DOWN) || (keyCode == KeyCodes.KEY_LEFT)) {
                    blockArea.zoomToSearchBlock(-1);
                    blockArea.zoomToSearchBlock(1);
                }
            }
        }) ;

        searchBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                String queried = searchBox.getText();
                int keyCode = event.getCharCode();
                try {
                  MultiWordSuggestOracle multiWordOracle = (MultiWordSuggestOracle)searchBox.getSuggestOracle();
                  List<String> suggestions = blockArea.querySuggest(queried);
                  for(String suggestion:suggestions){
                    multiWordOracle.add(suggestion);
                  }
                } catch (ClassCastException e ) {

                }
            }
                  
        }) ;

        /*Clear Button*/
        clearButton = new TextButton("X");
        clearButton.setHeight("20px");
        clearButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent click) {
                searchBox.setValue("");
                blockArea.stopSearch();
            }
        });


        /*Filter Box */
        filterBox = new ListBox();
        filterBox.getElement().getStyle().setProperty("color", "grey");
        filterBox.addItem("Please select one....");
        filterBox.setHeight("20px");
        flag = 0;
        filterBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(flag == 0){
                    try{    
                            flag = 1;
                            filterBox.clear();
                            List<String> types = blockArea.getBlockTypes("");
                            filterBox.addItem("Please select one....");
                            for(String type:types){
                               filterBox.addItem(type);
                            }
                        } catch (ClassCastException e ) {

                        }
                    }
              }
            });
        


        /* Filters Check Box */
        advancedCheckBox = new CheckBox("Filters");
        advancedCheckBox.getElement().getStyle().setProperty("color", "grey");
        advancedCheckBox.setHeight("20px");
        advancedCheckBox.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            boolean checked = ((CheckBox) event.getSource()).getValue();
            if(checked){
                filterBox.getElement().getStyle().setProperty("color", "black");
                try {
                  filterBox.clear();
                  filterBox.addItem("Please select one....");
                  List<String> types = blockArea.getBlockTypes("");
                  for(String type:types){
                    filterBox.addItem(type);
                  }
                } catch (ClassCastException e ) {

                }
            }else{
                filterBox.clear();
                filterBox.addItem("Please select one....");
                advancedCheckBox.getElement().getStyle().setProperty("color", "grey");
            }
          }
        });

        
        hPanel.add(searchBox);
        hPanel.add(clearButton);
        hPanel.add(upButton);
        hPanel.add(downButton);
        hPanel.add(advancedCheckBox);
        hPanel.add(filterBox);

        upButton.setEnabled(false);
        downButton.setEnabled(false);

        initWidget(hPanel);
    }


    public void setBlocklyPanel(BlocklyPanel panel) {
        this.blockArea = panel;
    }
}