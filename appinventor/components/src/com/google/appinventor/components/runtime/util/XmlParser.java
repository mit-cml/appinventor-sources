// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings("unchecked")
public class XmlParser extends DefaultHandler {
  private static final String CONTENT_TAG = "$content";
  private YailDictionary root = null;
  private YailDictionary currentElement = null;
  private Deque<YailDictionary> stack = new LinkedList<>();

  @Override
  public void startElement(String uri, String localName, String qname, Attributes attributes) {
    YailDictionary el = new YailDictionary();
    el.put("$tag", qname);
    el.put("$namespaceUri", uri);
    el.put("$localName", localName.isEmpty() ? qname : localName);
    if (qname.contains(":")) {
      String[] parts = qname.split(":");
      el.put("$namespace", parts[0]);
    } else {
      el.put("$namespace", "");
    }
    YailDictionary attrs = new YailDictionary();
    for (int i = 0; i < attributes.getLength(); i++) {
      attrs.put(attributes.getQName(i), attributes.getValue(i));
    }
    el.put("$attributes", attrs);
    el.put(CONTENT_TAG, new ArrayList<>());
    if (currentElement != null) {
      ((List<Object>) currentElement.get(CONTENT_TAG)).add(el);
      if (!currentElement.containsKey(qname)) {
        currentElement.put(qname, new ArrayList<>());
      }
      ((List<Object>) currentElement.get(qname)).add(el);
      stack.push(currentElement);
    } else {
      root = el;
    }
    currentElement = el;
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    List<Object> items = (List<Object>) currentElement.get(CONTENT_TAG);
    if (items instanceof ArrayList) {
      String content = new String(ch, start, length);
      content = content.trim();
      if (!content.isEmpty()) {
        items.add(content);
      }
    }
  }

  @Override
  public void endElement(String uri, String localName, String qname) {
    for (Entry<Object, Object> e : currentElement.entrySet()) {
      if (e.getValue() instanceof ArrayList) {
        e.setValue(YailList.makeList((List<?>) e.getValue()));
      }
    }
    if (!stack.isEmpty()) {
      currentElement = stack.pop();
    }
  }

  public YailDictionary getRoot() {
    return root;
  }
}
