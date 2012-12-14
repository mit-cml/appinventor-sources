// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package openblocks.yacodeblocks;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Color;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BlockColor elements - maintains a map from color name to rgb value to make
 * XML files more readable and maintainable.
 * 
 * @author sharon@google.com (Sharon Perl)
 *
 */

// TODO(sharon): consider making BlockColor methods non-static.

public class BlockColor {
  private static HashMap<String, Color> colorToValue = new HashMap<String, Color>();
  
  // For now, just static methods so there's nothing to instantiate.
  private BlockColor () {
  }
  
  /**
   * Loads the all the initial BlockColors of this language
   * @param root the Element carrying the specifications of the BlockColors
   */
  public static void loadBlockColors(Element root){
    Pattern attrExtractor=Pattern.compile("\"(.*)\"");
    Matcher nameMatcher;
    NodeList nodes=root.getElementsByTagName("BlockColor");
    Node node;

    for(int i=0; i<nodes.getLength(); i++){
      node = nodes.item(i);
      if(node.getNodeName().equals("BlockColor")){
        String colorName = null; 
        Color colorValue = Color.BLACK;
        nameMatcher=attrExtractor.matcher(node.getAttributes().getNamedItem("name").toString());
        if (nameMatcher.find()) //will be true
          colorName = nameMatcher.group(1);
        nameMatcher=attrExtractor.matcher(node.getAttributes().getNamedItem("rgb-value").toString());
        if (nameMatcher.find()){ //will be true
          StringTokenizer col = new StringTokenizer(nameMatcher.group(1));
          if(col.countTokens() == 3)
            colorValue = new Color(Integer.parseInt(col.nextToken()), 
                Integer.parseInt(col.nextToken()), Integer.parseInt(col.nextToken()));
         }
        if (colorName != null && colorValue != null) {
          colorToValue.put(colorName, colorValue);
        }
      }
    }
  }
  
  /**
   * Look up the color value corresponding to colorName
   * @param colorName the name of the color defined in a BlockColor element
   */
  public static Color getColorValue(String colorName) {
    if (colorToValue.containsKey(colorName)) {
      return colorToValue.get(colorName);
    } else
      return null;
  }
}
