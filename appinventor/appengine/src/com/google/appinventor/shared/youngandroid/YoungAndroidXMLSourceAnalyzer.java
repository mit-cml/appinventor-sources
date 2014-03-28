package com.google.appinventor.shared.youngandroid;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class YoungAndroidXMLSourceAnalyzer {
  public static void parseXMLSource(String source, List<String> templates) {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
 
      DefaultHandler handler = new DefaultHandler() {
 
        boolean bfname = false;
        boolean blname = false;
        boolean bnname = false;
        boolean bsalary = false;
 
        public void startElement(String uri, String localName,String qName, 
                      Attributes attributes) throws SAXException {
       
          System.out.println("Start Element :" + qName);
       
          if (qName.equalsIgnoreCase("FIRSTNAME")) {
            bfname = true;
          }
       
          if (qName.equalsIgnoreCase("LASTNAME")) {
            blname = true;
          }
       
          if (qName.equalsIgnoreCase("NICKNAME")) {
            bnname = true;
          }
       
          if (qName.equalsIgnoreCase("SALARY")) {
            bsalary = true;
          }
       
        }
 
        public void endElement(String uri, String localName,
          String qName) throws SAXException {
       
          System.out.println("End Element :" + qName);
       
        }
      };

      saxParser.parse(new InputSource(new StringReader(source)), handler);
 
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
