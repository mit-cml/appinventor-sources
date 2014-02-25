package com.google.appinventor.components.runtime.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Provides utility functions to parse XML text into Java object.
 *
 *
 */
public class XmlUtil {

  /**
   * Prevent instantiation.
   */
  public XmlUtil() {
  }

  /**
   * Tests if given string represents a number (short, integer, long, float, double)
   * using regex matching
   * 
   * @param s String to be tested
   * @return True if s is a number
   */
  public static boolean isNumeric(String s) {  
    return s.matches("[-+]?\\d*\\.?\\d+");  
  } 

  /**
   * Converts given string item in XML stream to its original generic type of object.
   * The item is either the text enclosed between tags or the attribute value in a tag.
   * 
   * @param item XML string item
   * @return the original generic type of object of item
   */
  public static Object convertXmlItem(String item){
    if(item == null){
      return null;
    }
  
    if(item.equalsIgnoreCase("true")){
      return true;
    }
  
    if(item.equalsIgnoreCase("false")){
      return false;
    }
    
    if(isNumeric(item)){
      try{
         return Long.parseLong(item);
      } catch (NumberFormatException e){
         return Double.parseDouble(item);
      } 
    }
  
    return item;
  }
  
  /**
   * Returns a list containing one two item list per key.
   * Each two item list has the key String as its first element and
   * its children value as the second element.
   * 
   * @param xmlString The text to be parsed
   * @return The list of pairs
   * @throws XmlPullParserException
   * @throws IOException
   */
  public static Object getObjectFromXml(String xmlString) throws XmlPullParserException, IOException{
    if ((xmlString == null) || xmlString.equals("")){
      throw new XmlPullParserException("Empty XML.");
    }
    List<Object> resultList = new ArrayList<Object>();
    Stack<Entry> stack = new Stack<Entry>();
    
    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    factory.setNamespaceAware(true);
    XmlPullParser xpp = factory.newPullParser();
    xpp.setInput( new StringReader(xmlString) );
    int eventType = xpp.getEventType();    
    while (eventType != XmlPullParser.END_DOCUMENT) {      
      if(eventType == XmlPullParser.START_DOCUMENT) {
        // Start document        
      } else if(eventType == XmlPullParser.START_TAG) {
        // Start tag
        Entry tmpEntry = new Entry(xpp.getName());
        stack.push(tmpEntry);  //pushing tag entry into stack             
        if(xpp.getAttributeCount() > 0){
          for(int i=0,n=xpp.getAttributeCount();i<n;i++){
            // Attribute
            List<Object> tmpList = new ArrayList<Object>();
            tmpList.add(xpp.getAttributeName(i));
            tmpList.add(convertXmlItem(xpp.getAttributeValue(i)));
            stack.peek().value.add(tmpList);
          }
        }
      } else if(eventType == XmlPullParser.END_TAG) {
        // End tag
        Entry tmpEntry = stack.pop();
        if(stack.isEmpty()){
          resultList.add(tmpEntry.toPair());
        }else{
          stack.peek().value.add(tmpEntry.toPair());
        }        
      } else if(eventType == XmlPullParser.TEXT) {
        // Text
        List<Object> tmpList = new ArrayList<Object>();
        tmpList.add("content");
        tmpList.add(convertXmlItem(xpp.getText()));
        stack.peek().value.add(tmpList);        
      }      
      eventType = xpp.next();      
    }    
    // End document
    if(!stack.isEmpty()){
      throw new XmlPullParserException("Invalid XML.");
    }    
    return resultList;        
  }
  
  /**
   * Tag structure
   * key Tag name
   * value List of children(attributes and text)
   * 
   * @author xzgao
   *
   */
  private static class Entry{
    public String key;
    public List<Object> value;
    
    public Entry(String k){
      key = k;
      value = new ArrayList<Object>();
    }
    
    // To wrap the tag into 2-element list (key, value)
    public List<Object> toPair(){
      List<Object> tmp = new ArrayList<Object>();
      tmp.add(key);
      tmp.add(value);
      return tmp;
    }
    
    // For test
    public String toString(){
      return "Tag: " + key + "{"+value.size()+"}";
    }    
  }

}
