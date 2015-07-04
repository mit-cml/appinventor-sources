// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.common;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A mapping of Html entity names and their unicode codes.
 *
 * Note: This was copied from the com.google.apps.mtrx.common.format.HtmlEntities and
 * com.google.apps.mtrx.common.format.HtmlUtil classes.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class HtmlEntities {

  private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&(#?[0-9a-zA-Z]+);");
  private static final Map<String, Character> lookup = new HashMap<String, Character>();

  static {
    // Letter entities
    lookup.put("Agrave", (char) 192);
    lookup.put("agrave", (char) 224);
    lookup.put("Aacute", (char) 193);
    lookup.put("aacute", (char) 225);
    lookup.put("Acirc", (char) 194);
    lookup.put("acirc", (char) 226);
    lookup.put("Atilde", (char) 195);
    lookup.put("atilde", (char) 227);
    lookup.put("Auml", (char) 196);
    lookup.put("auml", (char) 228);
    lookup.put("Aring", (char) 197);
    lookup.put("aring", (char) 229);
    lookup.put("AElig", (char) 198);
    lookup.put("aelig", (char) 230);
    lookup.put("Ccedil", (char) 199);
    lookup.put("ccedil", (char) 231);
    lookup.put("Egrave", (char) 200);
    lookup.put("egrave", (char) 232);
    lookup.put("Eacute", (char) 201);
    lookup.put("eacute", (char) 233);
    lookup.put("Ecirc", (char) 202);
    lookup.put("ecirc", (char) 234);
    lookup.put("Euml", (char) 203);
    lookup.put("euml", (char) 235);
    lookup.put("Igrave", (char) 204);
    lookup.put("igrave", (char) 236);
    lookup.put("Iacute", (char) 205);
    lookup.put("iacute", (char) 237);
    lookup.put("Icirc", (char) 206);
    lookup.put("icirc", (char) 238);
    lookup.put("Iuml", (char) 207);
    lookup.put("iuml", (char) 239);
    lookup.put("ETH", (char) 208);
    lookup.put("eth", (char) 240);
    lookup.put("Ntilde", (char) 209);
    lookup.put("ntilde", (char) 241);
    lookup.put("Ograve", (char) 210);
    lookup.put("ograve", (char) 242);
    lookup.put("Oacute", (char) 211);
    lookup.put("oacute", (char) 243);
    lookup.put("Ocirc", (char) 212);
    lookup.put("ocirc", (char) 244);
    lookup.put("Otilde", (char) 213);
    lookup.put("otilde", (char) 245);
    lookup.put("Ouml", (char) 214);
    lookup.put("ouml", (char) 246);
    lookup.put("Oslash", (char) 216);
    lookup.put("oslash", (char) 248);
    lookup.put("Ugrave", (char) 217);
    lookup.put("ugrave", (char) 249);
    lookup.put("Uacute", (char) 218);
    lookup.put("uacute", (char) 250);
    lookup.put("Ucirc", (char) 219);
    lookup.put("ucirc", (char) 251);
    lookup.put("Uuml", (char) 220);
    lookup.put("uuml", (char) 252);
    lookup.put("Yacute", (char) 221);
    lookup.put("yacute", (char) 253);
    lookup.put("THORN", (char) 222);
    lookup.put("thorn", (char) 254);
    lookup.put("szlig", (char) 223);
    lookup.put("yuml", (char) 255);
    lookup.put("Yuml", (char) 376);
    lookup.put("OElig", (char) 338);
    lookup.put("oelig", (char) 339);
    lookup.put("Scaron", (char) 352);
    lookup.put("scaron", (char) 353);
    lookup.put("Alpha", (char) 913);
    lookup.put("Beta", (char) 914);
    lookup.put("Gamma", (char) 915);
    lookup.put("Delta", (char) 916);
    lookup.put("Epsilon", (char) 917);
    lookup.put("Zeta", (char) 918);
    lookup.put("Eta", (char) 919);
    lookup.put("Theta", (char) 920);
    lookup.put("Iota", (char) 921);
    lookup.put("Kappa", (char) 922);
    lookup.put("Lambda", (char) 923);
    lookup.put("Mu", (char) 924);
    lookup.put("Nu", (char) 925);
    lookup.put("Xi", (char) 926);
    lookup.put("Omicron", (char) 927);
    lookup.put("Pi", (char) 928);
    lookup.put("Rho", (char) 929);
    lookup.put("Sigma", (char) 931);
    lookup.put("Tau", (char) 932);
    lookup.put("Upsilon", (char) 933);
    lookup.put("Phi", (char) 934);
    lookup.put("Chi", (char) 935);
    lookup.put("Psi", (char) 936);
    lookup.put("Omega", (char) 937);
    lookup.put("alpha", (char) 945);
    lookup.put("beta", (char) 946);
    lookup.put("gamma", (char) 947);
    lookup.put("delta", (char) 948);
    lookup.put("epsilon", (char) 949);
    lookup.put("zeta", (char) 950);
    lookup.put("eta", (char) 951);
    lookup.put("theta", (char) 952);
    lookup.put("iota", (char) 953);
    lookup.put("kappa", (char) 954);
    lookup.put("lambda", (char) 955);
    lookup.put("mu", (char) 956);
    lookup.put("nu", (char) 957);
    lookup.put("xi", (char) 958);
    lookup.put("omicron", (char) 959);
    lookup.put("pi", (char) 960);
    lookup.put("rho", (char) 961);
    lookup.put("sigmaf", (char) 962);
    lookup.put("sigma", (char) 963);
    lookup.put("tau", (char) 964);
    lookup.put("upsilon", (char) 965);
    lookup.put("phi", (char) 966);
    lookup.put("chi", (char) 967);
    lookup.put("psi", (char) 968);
    lookup.put("omega", (char) 969);
    lookup.put("thetasym", (char) 977);
    lookup.put("upsih", (char) 978);
    lookup.put("piv", (char) 982);
    // Non-letter entities
    lookup.put("iexcl", (char) 161);
    lookup.put("cent", (char) 162);
    lookup.put("pound", (char) 163);
    lookup.put("curren", (char) 164);
    lookup.put("yen", (char) 165);
    lookup.put("brvbar", (char) 166);
    lookup.put("sect", (char) 167);
    lookup.put("uml", (char) 168);
    lookup.put("copy", (char) 169);
    lookup.put("ordf", (char) 170);
    lookup.put("laquo", (char) 171);
    lookup.put("not", (char) 172);
    lookup.put("shy", (char) 173);
    lookup.put("reg", (char) 174);
    lookup.put("macr", (char) 175);
    lookup.put("deg", (char) 176);
    lookup.put("plusmn", (char) 177);
    lookup.put("sup2", (char) 178);
    lookup.put("sup3", (char) 179);
    lookup.put("acute", (char) 180);
    lookup.put("micro", (char) 181);
    lookup.put("para", (char) 182);
    lookup.put("middot", (char) 183);
    lookup.put("cedil", (char) 184);
    lookup.put("sup1", (char) 185);
    lookup.put("ordm", (char) 186);
    lookup.put("raquo", (char) 187);
    lookup.put("frac14", (char) 188);
    lookup.put("frac12", (char) 189);
    lookup.put("frac34", (char) 190);
    lookup.put("iquest", (char) 191);
    lookup.put("times", (char) 215);
    lookup.put("divide", (char) 247);
    lookup.put("fnof", (char) 402);
    lookup.put("circ", (char) 710);
    lookup.put("tilde", (char) 732);
    lookup.put("lrm", (char) 8206);
    lookup.put("rlm", (char) 8207);
    lookup.put("ndash", (char) 8211);
    lookup.put("endash", (char) 8211);
    lookup.put("mdash", (char) 8212);
    lookup.put("emdash", (char) 8212);
    lookup.put("lsquo", (char) 8216);
    lookup.put("rsquo", (char) 8217);
    lookup.put("sbquo", (char) 8218);
    lookup.put("ldquo", (char) 8220);
    lookup.put("rdquo", (char) 8221);
    lookup.put("bdquo", (char) 8222);
    lookup.put("dagger", (char) 8224);
    lookup.put("Dagger", (char) 8225);
    lookup.put("bull", (char) 8226);
    lookup.put("hellip", (char) 8230);
    lookup.put("permil", (char) 8240);
    lookup.put("prime", (char) 8242);
    lookup.put("Prime", (char) 8243);
    lookup.put("lsaquo", (char) 8249);
    lookup.put("rsaquo", (char) 8250);
    lookup.put("oline", (char) 8254);
    lookup.put("frasl", (char) 8260);
    lookup.put("euro", (char) 8364);
    lookup.put("image", (char) 8465);
    lookup.put("weierp", (char) 8472);
    lookup.put("real", (char) 8476);
    lookup.put("trade", (char) 8482);
    lookup.put("alefsym", (char) 8501);
    lookup.put("larr", (char) 8592);
    lookup.put("uarr", (char) 8593);
    lookup.put("rarr", (char) 8594);
    lookup.put("darr", (char) 8595);
    lookup.put("harr", (char) 8596);
    lookup.put("crarr", (char) 8629);
    lookup.put("lArr", (char) 8656);
    lookup.put("uArr", (char) 8657);
    lookup.put("rArr", (char) 8658);
    lookup.put("dArr", (char) 8659);
    lookup.put("hArr", (char) 8660);
    lookup.put("forall", (char) 8704);
    lookup.put("part", (char) 8706);
    lookup.put("exist", (char) 8707);
    lookup.put("empty", (char) 8709);
    lookup.put("nabla", (char) 8711);
    lookup.put("isin", (char) 8712);
    lookup.put("notin", (char) 8713);
    lookup.put("ni", (char) 8715);
    lookup.put("prod", (char) 8719);
    lookup.put("sum", (char) 8721);
    lookup.put("minus", (char) 8722);
    lookup.put("lowast", (char) 8727);
    lookup.put("radic", (char) 8730);
    lookup.put("prop", (char) 8733);
    lookup.put("infin", (char) 8734);
    lookup.put("ang", (char) 8736);
    lookup.put("and", (char) 8743);
    lookup.put("or", (char) 8744);
    lookup.put("cap", (char) 8745);
    lookup.put("cup", (char) 8746);
    lookup.put("int", (char) 8747);
    lookup.put("there4", (char) 8756);
    lookup.put("sim", (char) 8764);
    lookup.put("cong", (char) 8773);
    lookup.put("asymp", (char) 8776);
    lookup.put("ne", (char) 8800);
    lookup.put("equiv", (char) 8801);
    lookup.put("le", (char) 8804);
    lookup.put("ge", (char) 8805);
    lookup.put("sub", (char) 8834);
    lookup.put("sup", (char) 8835);
    lookup.put("nsub", (char) 8836);
    lookup.put("sube", (char) 8838);
    lookup.put("supe", (char) 8839);
    lookup.put("oplus", (char) 8853);
    lookup.put("otimes", (char) 8855);
    lookup.put("perp", (char) 8869);
    lookup.put("sdot", (char) 8901);
    lookup.put("lceil", (char) 8968);
    lookup.put("rceil", (char) 8969);
    lookup.put("lfloor", (char) 8970);
    lookup.put("rfloor", (char) 8971);
    lookup.put("lang", (char) 9001);
    lookup.put("rang", (char) 9002);
    lookup.put("loz", (char) 9674);
    lookup.put("spades", (char) 9824);
    lookup.put("clubs", (char) 9827);
    lookup.put("hearts", (char) 9829);
    lookup.put("diams", (char) 9830);
    // "Special" entities
    lookup.put("gt", (char) 62);
    lookup.put("GT", (char) 62);
    lookup.put("lt", (char) 60);
    lookup.put("LT", (char) 60);
    lookup.put("quot", (char) 34);
    lookup.put("QUOT", (char) 34);
    lookup.put("amp", (char) 38);
    lookup.put("AMP", (char) 38);
    lookup.put("apos", (char) 39);
    // "Whitespace" entities
    lookup.put("nbsp", (char) 160);
    lookup.put("ensp", (char) 8194);
    lookup.put("emsp", (char) 8195);
    lookup.put("thinsp", (char) 8201);
    lookup.put("zwnj", (char) 8204);
    // "Ignore" entities
    lookup.put("zwj", (char) 8205);
  }

  /**
   * Converts a named HTML entity such as "nbsp" or "amp" to its corresponding
   * unicode character.
   *
   * @param entityName Name of the entity to convert to a character.
   * @return Unicode character corresponding with the entity, or null if non
   *     found.
   */
  public static Character toCharacter(String entityName) {
    return lookup.get(entityName);
  }

  /**
   * Decodes the given HTML text value.
   *
   * <pre>
   * HTML Character Entities such as &amp;, &lt;, &gt;, &apos;, and &quot; are
   * changed to &, <, >, ', and ".
   * Entities such as &#xhhhh, and &#nnnn are changed to the appropriate characters.
   * Unrecognized entities are not decoded and no exception is thrown.
   * </pre>
   *
   * @param htmlText the HTML text to decode
   * @return the decoded text
   */
  public static String decodeHtmlText(String htmlText) {
    if (htmlText.length() == 0 ||
        htmlText.indexOf('&') == -1) {
      return htmlText;
    }

    StringBuilder output = new StringBuilder();
    int lastMatchEnd = 0;
    Matcher matcher = HTML_ENTITY_PATTERN.matcher(htmlText);
    while (matcher.find()) {
      // Remove the beginning ampersand and ending semicolon from the entity.
      String entity = matcher.group(1);

      Character convertedEntity = null;
      if (entity.startsWith("#x")) {
        // The complete entity was of the form "&#xhhhh;", where hhhh is hex.
        String hhhh = entity.substring(2);
        try {
          System.out.println("hex number is " + hhhh);
          int code = Integer.parseInt(hhhh, 16);
          convertedEntity = Character.valueOf((char) code);
        } catch (NumberFormatException e) {
          // convertedEntity is still null
        }
      } else if (entity.startsWith("#")) {
        // The complete entity was of the form "&#nnnn;", where nnnn is decimal.
        String nnnn  = entity.substring(1);
        try {
          int code = Integer.parseInt(nnnn);
          convertedEntity = Character.valueOf((char) code);
        } catch (NumberFormatException e) {
          // convertedEntity is still null
        }
      } else {
        convertedEntity = lookup.get(entity);
      }

      if (convertedEntity != null) {
        output.append(htmlText.substring(lastMatchEnd, matcher.start()));
        output.append(convertedEntity);
        lastMatchEnd = matcher.end();
      }
    }
    if (lastMatchEnd < htmlText.length()) {
      output.append(htmlText.substring(lastMatchEnd));
    }
    return output.toString();
  }
}
