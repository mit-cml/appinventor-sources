// Copyright 2009 Google Inc. All Rights Reserved.

package openblocks.yacodeblocks;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.InputStream;

/**
 * DTDResolver gets DTD files from jar resources instead of from the file system
 */

public class DTDResolver implements EntityResolver {
  protected static final String SAVE_FORMAT_DTD_FILEPATH = "support/save_format.dtd";
  protected static final String LANG_DEF_DTD_FILEPATH = "support/lang_def.dtd";

  public DTDResolver() {
  }

  public InputSource resolveEntity (String publicId, String systemId) {
    System.out.println("systemId is " + systemId + " publicId is " + publicId);
    if (systemId.matches(".*lang_def.dtd")) {
      InputStream dtdStream = this.getClass().getResourceAsStream(LANG_DEF_DTD_FILEPATH);
      if (dtdStream != null) {
        System.out.println("Reading language definition DTD from jar resources.");
        return new InputSource (dtdStream);
      }
    } else if (systemId.matches(".*save_format.dtd")) {
      InputStream dtdStream = this.getClass().getResourceAsStream(SAVE_FORMAT_DTD_FILEPATH);
      if (dtdStream != null) {
        System.out.println("Reading save file format DTD from jar resources.");
        return new InputSource (dtdStream);
      }
    }
    return null;
  }
}

