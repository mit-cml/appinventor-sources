package com.google.appinventor.shared.youngandroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Daniela Miao
 * 
 */
public class BkyParserHandler extends DefaultHandler {
  private List<String> templates;
  
  public BkyParserHandler(List<String> templates) {
    this.templates = templates;
  }
  //create an array of String Lists to store the relationships between privacy-sensitive components
  ArrayList<ArrayList<ArrayList<String>>> relationships = new ArrayList<ArrayList<ArrayList<String>>>();
  
  // getter for the relationships array
  public ArrayList<ArrayList<ArrayList<String>>> getRelationships() {
    return relationships;
  }
  
  // use a counter to keep track of which "<block></block>" level we are in
  int curLevel = 0;
  
  // create map to store privacy-sensitive components, their level, and their method/property/event name
  Map<Integer, ArrayList<String>> map = new HashMap<Integer, ArrayList<String>>();

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    if (qName.equalsIgnoreCase("block")) {
      curLevel++;
    } else if (qName.equalsIgnoreCase("mutation")) {
      String component = attributes.getValue("component_type");
      if (templates.contains(component)) {
        // create a new entry for this component in the privacy-sensitive map
        ArrayList<String> values = new ArrayList<String>();
        values.add(component);
        if (attributes.getValue("method_name") != null) {
          values.add("method_name");
          values.add(attributes.getValue("method_name"));
        } else if (attributes.getValue("property_name") != null) {
          values.add("property_name");
          values.add(attributes.getValue("property_name"));
        } else if (attributes.getValue("event_name") != null) {
          values.add("event_name");
          values.add(attributes.getValue("event_name"));
        } else {
          values.add("NONE");
          values.add("NONE");
        }
        // add the entry to the map of privacy-sensitive components
        map.put(curLevel, values);
        
        // find relationships between this component and other existing privacy-sensitive 
        Iterator<Entry<Integer, ArrayList<String>>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
          Entry<Integer, ArrayList<String>> entry = iter.next();
          if (entry.getKey() < curLevel && !(entry.getValue().equals(values))) { // is a parent of the current component
            ArrayList<ArrayList<String>> relationship = new ArrayList<ArrayList<String>>();
            relationship.add(entry.getValue()); // parent component
            relationship.add(values); // current component
            if (!relationships.contains(relationship)) {
              relationships.add(relationship);
            }
          }
        }
      } 
    }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.equalsIgnoreCase("block")) {
      curLevel--;
      // every time we exit a <block></block>, we must remove its associated entries in the
      // privacy-sensitive components map, since there is no way they have "nested" components
      // from this point onwards.
      Iterator<Entry<Integer, ArrayList<String>>> iter = map.entrySet().iterator();
      while (iter.hasNext()) {
        Entry<Integer, ArrayList<String>> entry = iter.next();
        if(entry.getKey() > curLevel) {
            iter.remove();
        }
      }
    }
  }
}
