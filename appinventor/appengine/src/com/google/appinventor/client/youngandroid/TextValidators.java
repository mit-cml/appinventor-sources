// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.youngandroid;

import com.google.appinventor.client.Ode;
import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.editor.simple.SimpleComponentDatabase;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.explorer.folder.ProjectFolder;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

import java.util.Arrays;
import java.util.List;

/**
 */
public final class TextValidators {

  private static final int MAX_FILENAME_SIZE = 100;
  private static final int MIN_FILENAME_SIZE = 1;

  public enum ProjectNameStatus {
    SUCCESS,
    INVALIDFORMAT,
    RESERVED,
    DUPLICATE,
    DUPLICATEINTRASH
  }

  protected static final List<String> YAIL_NAMES = Arrays.asList("CsvUtil", "Double", "Float",
          "Integer", "JavaCollection", "JavaIterator", "KawaEnvironment", "Long", "Short",
          "SimpleForm", "String", "Pattern", "YailDictionary", "YailList", "YailNumberToString", "YailRuntimeError");

  protected static final List<String> JAVA_NAMES = Arrays.asList("abstract", "continue", "for", "new", "switch",
          "assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this",
          "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws",
          "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char",
          "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const",
          "float", "native", "super", "while");

  protected static final List<String> SCHEME_NAMES = Arrays.asList("begin", "def", "foreach", "forrange", "JavaStringUtils", "quote");

  // This class should never be instantiated.
  private TextValidators() {}

  /**
   * Determines whether the given project name is valid, displaying an alert
   * if it is not.  In order to be valid, the project name must satisfy
   * {@link #isValidIdentifier(String)} and not be a duplicate of an existing
   * project name for the same user.
   *
   * @param projectName the project name to validate
   * @return {@code true} if the project name is valid, {@code false} otherwise
   */
  public static ProjectNameStatus checkNewProjectName(String projectName, boolean quietly) {

    // Check the format of the project name
    if (!isValidIdentifier(projectName)) {
      if (!quietly) {
        Window.alert(MESSAGES.malformedProjectNameError());
      }
      return ProjectNameStatus.INVALIDFORMAT;
    }

    // Check for names that reserved words
    if (isReservedName(projectName)) {
      Window.alert(MESSAGES.reservedNameError());
      return ProjectNameStatus.RESERVED;
    }

    // Check that project does not already exist
    if (Ode.getInstance().getProjectManager().getProject(projectName) != null) {
      if (Ode.getInstance().getProjectManager().getProject(projectName).isInTrash()) {
        Window.alert(MESSAGES.duplicateTrashProjectNameError(projectName));
        return ProjectNameStatus.DUPLICATEINTRASH;
      } else if (!quietly) {
        Window.alert(MESSAGES.duplicateProjectNameError(projectName));
      }
      return ProjectNameStatus.DUPLICATE;
    }
    return ProjectNameStatus.SUCCESS;
  }

  public static ProjectNameStatus checkNewProjectName(String projectName) {
    return checkNewProjectName(projectName, false);
  }

  public static boolean checkNewComponentName(String componentName) {

    // Check that it meets the formatting requirements.
    if (!TextValidators.isValidComponentIdentifier(componentName)) {
      Window.alert(MESSAGES.malformedComponentNameError());
      return false;
    }

    long projectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
    if ( projectId == 0) { // Check we have a current Project
      return false;
    }

    YaProjectEditor editor = (YaProjectEditor) Ode.getInstance().getEditorManager().getOpenProjectEditor(projectId);

    // Check that it's unique.
    final List<String> names = editor.getComponentInstances();
    if (names.contains(componentName)) {
      Window.alert(MESSAGES.sameAsComponentInstanceNameError());
      return false;
    }

    // Check that it is a variable name used in the Yail code
    if (TextValidators.isReservedName(componentName)) {
      Window.alert(MESSAGES.reservedNameError());
      return false;
    }

    //Check that it is not a Component type name
    SimpleComponentDatabase COMPONENT_DATABASE = SimpleComponentDatabase.getInstance(projectId);
    if (COMPONENT_DATABASE.isComponent(componentName)) {
      Window.alert(MESSAGES.duplicateComponentNameError());
      return false;
    }

    return true;
  }

  /**
   * Determines whether the given folder name is valid, displaying an alert
   * if it is not.  In order to be valid, the folder name must satisfy
   * {@link #isValidIdentifier(String)} and not be a duplicate of an existing
   * folder name in the same parent folder.
   *
   * @param folderName the folder name to validate
   * @param folder the folder whose children are to be checked against this new
   *        folder name
   * @return {@code true} if the folder name is valid, {@code false} otherwise
   */
  public static ProjectNameStatus checkNewFolderName(String folderName, ProjectFolder parent) {
    // Check the format of the folder name
    if (!isValidIdentifier(folderName)) {
      // TODO: Decide whether to use new strings
      Window.alert(MESSAGES.malformedProjectNameError());
      return ProjectNameStatus.INVALIDFORMAT;
    }

    // Check for names that reserved words
    if (isReservedName(folderName)) {
      Window.alert(MESSAGES.reservedNameError());
      return ProjectNameStatus.RESERVED;
    }

    // Check that folder does not already exist
    for (ProjectFolder folder : parent.getChildFolders()) {
      if (folderName.equals(folder.getName())) {
        Window.alert(MESSAGES.duplicateProjectNameError(folderName));
        return ProjectNameStatus.DUPLICATE;
      }
    }
    return ProjectNameStatus.SUCCESS;
  }

  /**
   * Checks whether the argument is a legal identifier, specifically,
   * a non-empty string starting with a letter and followed by any number of
   * (unaccented English) letters, digits, or underscores.
   *
   * @param text the proposed identifier
   * @return {@code true} if the argument is a legal identifier, {@code false}
   *         otherwise
   */
  public static boolean isValidIdentifier(String text) {
    return text.matches("^[a-zA-Z]\\w*$");
  }

  /**
   * Checks whether the argument is a word reserved by YAIL or JAVA.
   *
   * @param text the proposed identifier
   * @return {@code true} if the argument is a reserved word, {@code false}
   *         otherwise
   */
  public static boolean isReservedName(String text) {
    return (YAIL_NAMES.contains(text) || JAVA_NAMES.contains(text) || SCHEME_NAMES.contains(text));
  }

  /**
   * Checks whether the argument is a legal component identifier; please check
   * Blockly.LexicalVariable.checkIdentifier for the regex reference
   *
   * @param text the proposed identifier
   * @return {@code true} if the argument is a legal identifier, {@code false}
   *         otherwise
   */
  public static boolean isValidComponentIdentifier(String text) {
    return text.matches("^[^-0-9!&%^/>=<`'\"#:;,\\\\^\\*\\+\\.\\(\\)\\|\\{\\}\\[\\]\\ ]" +
        "[^-!&%^/>=<'\"#:;,\\\\^\\*\\+\\.\\(\\)\\|\\{\\}\\[\\]\\ ]*$");
  }

  /**
   * Checks whether the argument is a legal filename, meaning
   * it is unchanged by URL encoding and it meets the aapt
   * requirements as follows:
   * - all characters must be 7-bit printable ASCII
   * - none of { '/' '\\' ':' }
   * @param filename The filename (not path) of uploaded file
   * @return {@code true} if the argument is a legal filename, {@code false}
   *         otherwise
   */
  public static boolean isValidCharFilename(String filename){
    return !filename.contains("'") && filename.equals(URL.encodePathSegment(filename));
  }

  /**
   * Checks whether the argument is a filename which meets the length
   * requirement imposed by aapt, which is:
   * - the filename length must be less than kMaxAssetFileName bytes long
   *   (and can't be empty)
   * where kMaxAssetFileName is defined to be 100.
   * (A legal name, therefore, has length <= kMaxAssetFileNames)
   * @param filename The filename (not path) of uploaded file
   * @return {@code true} if the length of the argument is legal, {@code false}
   *         otherwise
   */
  public static boolean isValidLengthFilename(String filename){
    return !(filename.length() > MAX_FILENAME_SIZE || filename.length() < MIN_FILENAME_SIZE);
  }

  /**
   * Determines human-readable message for specific error.
   * @param filename The filename (not path) of uploaded file
   * @return String representing error message, empty string if no error
   */
  public static String getErrorMessage(String filename){
    String errorMessage = "";
    String noWhitespace = "[\\S]+";
    String firstCharacterLetter = "[A-Za-z].*";
    String temp = filename.trim().replaceAll("( )+", " ").replace(" ","_");
    if (temp.length() > 0) {
      if (!temp.matches("[A-Za-z][A-Za-z0-9_]*")) {
        if (!temp.matches(firstCharacterLetter)) {
          //Check to make sure that the first character is a letter
          errorMessage = MESSAGES.firstCharProjectNameError();
        } else { //The text contains a character that is not a letter, number, or underscore
          errorMessage = MESSAGES.invalidCharProjectNameError();
        }
      }
    }
    return errorMessage;
  }

  /**
   * Determines human-readable message for specific warning if there are no errors.
   * @param filename The filename (not path) of uploaded file
   * @return String representing warning message, empty string if no warning and no error
   */
  public static String getWarningMessages(String filename) {
    String warningMessage = "";
    if (getErrorMessage(filename).length() == 0 && filename.trim().length() > 0) {
      if (!filename.matches("[A-Za-z][A-Za-z0-9_]*")) {
        // check to make sure if filename has no spaces
        String errorMessage = MESSAGES.whitespaceProjectNameError();
        filename = filename.trim().replaceAll("( )+", " ").replace(" ","_");
        warningMessage = errorMessage + ". \n '" + filename + "' will be used if continued.";
      }
    }
    return warningMessage;
  }
}
