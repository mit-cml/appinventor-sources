// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.rebind;

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;

import java.io.PrintWriter;

/**
 * Generates an implementation for
 * {@link com.google.appinventor.client.Version}. This allows us to compile
 * custom versions of ODE.
 *
 * TODO(user): remove or replace this versioning mechanism for open source
 *
 */
public class VersionGenerator extends Generator {

  private static final String PACKAGE_NAME = "com.google.appinventor.client";

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName)
      throws UnableToCompleteException {
    try {
      String version = context.getPropertyOracle().getPropertyValue(logger, "ode.version");
      String className = "Version" + version;
      // The generator can be invoked for the same class name more than once.
      // In this case the GeneratorContext.tryCreate method will return null to
      // indicate that the file already exists. This is not an error.
      PrintWriter out = context.tryCreate(logger, PACKAGE_NAME, className);
      if (out != null) {
        out.println("package " + PACKAGE_NAME + ";");
        out.println("class " + className);
        out.println("    implements com.google.appinventor.client.Version {");
        out.println("  @Override");
        out.println("  public boolean isProduction() {");
        out.println("    return " + version.equals("production") + ";");
        out.println("  }");
        out.println("}");

        context.commit(logger, out);
      }

      return PACKAGE_NAME + '.' + className;

    } catch (BadPropertyValueException e) {
      throw new UnableToCompleteException();
    }
  }
}
