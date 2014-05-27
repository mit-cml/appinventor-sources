package com.google.appinventor.shared.youngandroid;

import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class YoungAndroidXMLSourceAnalyzer {
  public static ArrayList<ArrayList<ArrayList<String>>> parseXMLSource(String source, final List<String> templates) {
    BkyParserHandler handler = new BkyParserHandler(templates);
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(new InputSource(new StringReader(source)), handler);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return handler.getRelationships();
  }
}
