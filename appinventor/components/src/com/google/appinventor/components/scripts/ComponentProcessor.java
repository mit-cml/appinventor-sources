// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.IsColor;
import com.google.appinventor.components.annotations.PermissionConstraint;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimpleBroadcastReceiver;
import com.google.appinventor.components.annotations.UsesActivityMetadata;
import com.google.appinventor.components.annotations.UsesApplicationMetadata;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesNativeLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesActivities;
import com.google.appinventor.components.annotations.UsesBroadcastReceivers;
import com.google.appinventor.components.annotations.UsesContentProviders;
import com.google.appinventor.components.annotations.UsesQueries;
import com.google.appinventor.components.annotations.UsesServices;
import com.google.appinventor.components.annotations.UsesXmls;
import com.google.appinventor.components.annotations.XmlElement;
import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;
import com.google.appinventor.components.annotations.androidmanifest.ReceiverElement;
import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;
import com.google.appinventor.components.annotations.androidmanifest.MetaDataElement;
import com.google.appinventor.components.annotations.androidmanifest.ActionElement;
import com.google.appinventor.components.annotations.androidmanifest.DataElement;
import com.google.appinventor.components.annotations.androidmanifest.CategoryElement;
import com.google.appinventor.components.annotations.androidmanifest.ServiceElement;
import com.google.appinventor.components.annotations.androidmanifest.ProviderElement;
import com.google.appinventor.components.annotations.androidmanifest.PathPermissionElement;
import com.google.appinventor.components.annotations.androidmanifest.GrantUriPermissionElement;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.Writer;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractTypeVisitor7;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor7;
import javax.lang.model.util.Types;

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Processor for generating output files based on the annotations and
 * javadoc in the component source code.
 * <p>
 * Specifically, this reads over the source files, building up a representation
 * of components and their designer properties, properties, methods, and
 * events. Concrete subclasses implement the method {@link #outputResults()}
 * to generate output.
 * <p>
 * Currently, the following annotations are used:
 * <ul>
 *   <li> {@link DesignerComponent} and {@link SimpleObject} to identify
 *        components.  Subclasses can distinguish between the two through
 *        the boolean fields
 *        {@link ComponentProcessor.ComponentInfo#designerComponent} and
 *        {@link ComponentProcessor.ComponentInfo#simpleObject}.
 *   <li> {@link DesignerProperty} to identify designer properties.
 *   <li> {@link SimpleProperty} to identify properties.
 *   <li> {@link SimpleFunction} to identify methods.
 *   <li> {@link SimpleEvent} to identify events.
 * </ul>
 *
 * @author spertus@google.com (Ellen Spertus)
 *
 * [lyn, 2015/12/29] Added deprecated instance variable to ParameterizedFeature.
 *   This is inherited by Event, Method, and Property, which are modified
 *   slightly to handle it.
 *
 * [Will, 2016/9/20] Added methods to process annotations in the package
 *   com.google.appinventor.components.annotations.androidmanifest and the
 *   appropriate calls in {@link #processComponent(Element)}.
 */
public abstract class ComponentProcessor extends AbstractProcessor {
  private static final String OUTPUT_PACKAGE = "";

  private static final String MISSING_SIMPLE_PROPERTY_ANNOTATION =
      "Designer property %s does not have a corresponding @SimpleProperty annotation.";
  private static final String BOXED_TYPE_ERROR =
      "Found use of boxed type %s. Please use the primitive type %s instead";

  // Returned by getSupportedAnnotationTypes()
  private static final Set<String> SUPPORTED_ANNOTATION_TYPES = ImmutableSet.of(
      "com.google.appinventor.components.annotations.DesignerComponent",
      "com.google.appinventor.components.annotations.DesignerProperty",
      "com.google.appinventor.components.annotations.SimpleEvent",
      "com.google.appinventor.components.annotations.SimpleFunction",
      "com.google.appinventor.components.annotations.SimpleObject",
      "com.google.appinventor.components.annotations.SimpleProperty",
      // TODO(Will): Remove the following string once the deprecated
      //             @SimpleBroadcastReceiver annotation is removed. It should
      //             should remain for the time being because otherwise we'll break
      //             extensions currently using @SimpleBroadcastReceiver.
      "com.google.appinventor.components.annotations.SimpleBroadcastReceiver",
      "com.google.appinventor.components.annotations.UsesAssets",
      "com.google.appinventor.components.annotations.UsesLibraries",
      "com.google.appinventor.components.annotations.UsesNativeLibraries",
      "com.google.appinventor.components.annotations.UsesActivities",
      "com.google.appinventor.components.annotations.UsesBroadcastReceivers",
      "com.google.appinventor.components.annotations.UsesPermissions",
      "com.google.appinventor.components.annotations.UsesQueries",
      "com.google.appinventor.components.annotations.UsesServices",
      "com.google.appinventor.components.annotations.UsesContentProviders",
      "com.google.appinventor.components.annotations.UsesXmls");

  // Returned by getRwString()
  private static final String READ_WRITE = "read-write";
  private static final String READ_ONLY = "read-only";
  private static final String WRITE_ONLY = "write-only";

  // Must match buildserver.compiler.ARMEABI_V7A_SUFFIX
  private static final String ARMEABI_V7A_SUFFIX = "-v7a";
  // Must match buildserver.compiler.ARMEABI_V8A_SUFFIX
  private static final String ARM64_V8A_SUFFIX = "-v8a";
  // Must match buildserver.compiler.X86_64_SUFFIX
  private static final String X86_64_SUFFIX = "-x8a";

  private static final String TYPE_PLACEHOLDER = "%type%";

  private static final Map<String, String> BOXED_TYPES = new HashMap<>();

  static {
    BOXED_TYPES.put("java.lang.Boolean", "boolean");
    BOXED_TYPES.put("java.lang.Byte", "byte");
    BOXED_TYPES.put("java.lang.Char", "char");
    BOXED_TYPES.put("java.lang.Short", "short");
    BOXED_TYPES.put("java.lang.Integer", "int");
    BOXED_TYPES.put("java.lang.Long", "long");
    BOXED_TYPES.put("java.lang.Float", "float");
    BOXED_TYPES.put("java.lang.Double", "double");
  }

  // The next two fields are set in init().
  /**
   * A handle allowing access to facilities provided by the annotation
   * processing tool framework
   */
  private Elements elementUtils;
  private Types typeUtils;

  /**
   * Produced through {@link ProcessingEnvironment#getMessager()} and
   * used for outputing errors and warnings.
   */
  // Set in process()
  protected Messager messager;

  /**
   * Indicates which pass is being performed by the Java annotation processor
   */
  private int pass = 0;

  /**
   * Information about every App Inventor component.  Keys are fully-qualified names
   * (such as "com.google.appinventor.components.runtime.components.android.Label"), and
   * values are the corresponding {@link ComponentProcessor.ComponentInfo} objects.
   * This is constructed by {@link #process} for use in {@link #outputResults()}.
   */
  protected final SortedMap<String, ComponentInfo> components = Maps.newTreeMap();

  /**
   * Information about every option list helper block. Keys are simple names, and values are the
   * corresponding {@link ComponentProcessor.OptionList} objects. This is constructed as a side
   * effect of {@link #process} for use in {@link #outputResults()}.
   */
  protected final Map<String, OptionList> optionLists = Maps.newTreeMap();

  /**
   * A list of asset filters, which are in fact lists of strings. This gets intialized with an empty
   * filter by the ComponentProcessor constructor.
   */
  protected List<List<String>> filters;

  private final List<String> componentTypes = Lists.newArrayList();

  /**
   * A set of visited types in the class hierarchy. This is used to reduce the complexity of
   * detecting whether a class implements {@link com.google.appinventor.components.runtime.Component}
   * from O(n^2) to O(n) by tracking visited nodes to prevent repeat explorations of the class tree.
   */
  private final Set<String> visitedTypes = new HashSet<>();

  public ComponentProcessor() {
    filters = new ArrayList<List<String>>();
    filters.add(new ArrayList<String>());
  }

  /**
   * Represents a parameter consisting of a name and a type.
   */
  protected class Parameter implements Cloneable {
    /**
     * The parameter name
     */
    protected final String name;

    /**
     * The parameter's Java type, such as int or java.lang.String.
     */
    protected final TypeMirror type;

    /**
     * Indicate whether this parameter is an integer that represents a color.
     */
    protected final boolean color;

    /**
     * The helper key associated with this parameter, if any.
     */
    protected HelperKey helper;

    /**
     * Constructs a Parameter.
     *
     * @param name the parameter name
     * @param type the parameter's Java type (such as int or java.lang.String)
     */
    protected Parameter(String name, TypeMirror type) {
      this(name, type, false);
    }

    protected Parameter(String name, TypeMirror type, boolean color) {
      this.name = name;
      this.type = type;
      this.color = color;
      // helper is null by default.
    }

    /**
     * Returns the HelperKey associated with this parameter, if one exists. Null otherwise.
     * @return the HelperKey associated with this parameter.
     */
    protected HelperKey getHelperKey() {
      return helper;
    }

    /**
     * Returns the string representation of the Yail type for this parameter.
     * @return the string representation of the Yail type for this parameter.
     * @throws RuntimeException if {@code parameter} does not have a
     *         corresponding Yail type
     */
    protected String getYailType() {
      return javaTypeToYailType(type);
    }

    @Override
    public Parameter clone() {
      Parameter param = new Parameter(name, type, color);
      param.helper = helper;
      return param;
    }
  }

  protected class Continuation extends Parameter {
    protected final TypeMirror underlyingType;

    protected Continuation(String name, final TypeMirror type) {
      this(name, type, false);
    }

    protected Continuation(String name, TypeMirror type, boolean color) {
      super(name, type, color);
      underlyingType = type.accept(new AbstractTypeVisitor7<TypeMirror, Void>() {
        @Override
        public TypeMirror visitPrimitive(PrimitiveType t, Void unused) {
          return null;
        }

        @Override
        public TypeMirror visitNull(NullType t, Void unused) {
          return null;
        }

        @Override
        public TypeMirror visitArray(ArrayType t, Void unused) {
          return null;
        }

        @Override
        public TypeMirror visitDeclared(DeclaredType t, Void unused) {
          List<? extends TypeMirror> arglist = t.getTypeArguments();
          if (arglist.isEmpty()) {
            messager.printMessage(Kind.ERROR, "Continuation should be specialized with type.",
                t.asElement());
          }
          return arglist.get(0);
        }

        @Override
        public TypeMirror visitError(ErrorType t, Void unused) {
          return null;
        }

        @Override
        public TypeMirror visitTypeVariable(TypeVariable t, Void unused) {
          return null;
        }

        @Override
        public TypeMirror visitWildcard(WildcardType t, Void unused) {
          return null;
        }

        @Override
        public TypeMirror visitExecutable(ExecutableType t, Void unused) {
          return null;
        }

        @Override
        public TypeMirror visitNoType(NoType t, Void unused) {
          return null;
        }

        @Override
        public TypeMirror visitUnion(UnionType t, Void unused) {
          return null;
        }
      }, null);
    }

    protected String getContinuationType() {
      return javaTypeToYailType(underlyingType, true);
    }
  }

  /**
   * Represents a component feature that has a name and a description.
   */
  protected abstract static class Feature {
    private static final Pattern AT_SIGN = Pattern.compile("[^\\\\]@");
    private static final Pattern LINK_FORM = Pattern.compile("\\{@link ([A-Za-z]*#?)([A-Za-z]*)[^}]*}");
    private static final Pattern CODE_FORM = Pattern.compile("\\{@code ([^}]*)}");

    private final String featureType;
    protected final String name;
    protected String description;
    protected boolean defaultDescription = false;
    protected String longDescription;
    protected boolean userVisible;
    protected boolean deprecated;

    protected Feature(String name, String description, String longDescription, String featureType,
        boolean userVisible, boolean deprecated) {
      this.featureType = featureType;
      this.name = name;
      setDescription(description);
      setLongDescription(longDescription);
      this.userVisible = userVisible;
      this.deprecated = deprecated;
    }

    public boolean isDefaultDescription() {
      return defaultDescription;
    }

    public void setDescription(String description) {
      if (description == null || description.isEmpty()) {
        this.description = featureType + " for " + name;
        defaultDescription = true;
      } else {
        // Throw out the first @ or { and everything after it,
        // in order to strip out @param, @author, {@link ...}, etc.
        this.description = description.split("@|\\{@")[0].trim();
        this.description = removeMarkup(this.description);
        defaultDescription = false;
      }
    }
    private String removeMarkup(String str) {
      String result = str.replaceAll("\\\\(.)", "$1");
      result = result.replaceAll("\\[([a-zA-Z0-9]*)\\]\\(#.*\\)", "$1");
      return result;
    }
    public void setLongDescription(String longDescription) {
      if (longDescription == null || longDescription.isEmpty()) {
        this.longDescription = this.description;
      } else if (longDescription.contains("@suppressdoc")) {
        this.longDescription = "";
      } else {
        this.longDescription = longDescription;
      }
      // Handle links
      Matcher linkMatcher = LINK_FORM.matcher(this.longDescription);
      StringBuffer sb = new StringBuffer();
      int lastEnd = 0;
      while (linkMatcher.find(lastEnd)) {
        sb.append(this.longDescription, lastEnd, linkMatcher.start());
        String clazz = linkMatcher.group(1);
        if (clazz.endsWith("#")) {
          clazz = clazz.substring(0, clazz.length() - 1);
        }
        if ("Form".equals(clazz)) {
          clazz = "Screen";
        }
        String func = linkMatcher.group(2);
        sb.append("[");
        if (!clazz.isEmpty()) {
          sb.append("`");
          sb.append(clazz);
          sb.append("`");
          if (!func.isEmpty()) {
            sb.append("'s ");
          }
        }
        if (!func.isEmpty()) {
          sb.append("`");
          sb.append(func);
          sb.append("`");
        }
        sb.append("](#");
        if (clazz.isEmpty()) {
          sb.append("%type%.");
        } else {
          sb.append(clazz);
          if (!func.isEmpty()) {
            sb.append(".");
          }
        }
        if (!func.isEmpty()) {
          sb.append(func);
        }
        sb.append(")");
        lastEnd = linkMatcher.end();
      }
      sb.append(this.longDescription.substring(lastEnd));
      this.longDescription = sb.toString();
      // Map {@code foo} to `foo`
      sb = new StringBuffer();
      Matcher codeMatcher = CODE_FORM.matcher(this.longDescription);
      lastEnd = 0;
      while (codeMatcher.find(lastEnd)) {
        sb.append(this.longDescription, lastEnd, codeMatcher.start());
        sb.append("`");
        sb.append(codeMatcher.group(1));
        sb.append("`");
        lastEnd = codeMatcher.end();
      }
      sb.append(this.longDescription.substring(lastEnd));
      this.longDescription = sb.toString();
      // Strip out the Javadoc annotations (@param, etc.) for end-user documentation
      Matcher m = AT_SIGN.matcher(this.longDescription);
      if (m.find()) {
        this.longDescription = this.longDescription.substring(0, m.start() + 1);
      }
      // Replace escaped @ with just @, e.g., so we can use @ in email address examples.
      this.longDescription = this.longDescription.replaceAll("\\\\@", "@").trim();
    }

    public String getLongDescription(ComponentInfo component) {
      if (longDescription == null || longDescription.isEmpty()) {
        return description;
      }
      String name = component.name.equals("Form") ? "Screen" : component.name;
      return longDescription.replaceAll("%type%", name).trim();
    }

    /**
     * Returns whether this property is visible in the Blocks Editor, as retrieved
     * from {@link SimpleProperty#userVisible()}.
     *
     * @return whether the property is visible in the Blocks Editor
     */
    protected boolean isUserVisible() {
      return userVisible;
    }

    /**
     * Returns whether this property is deprecated in the Blocks Editor.
     *
     * @return whether the property is visible in the Blocks Editor
     */
    protected boolean isDeprecated() {
      return deprecated;
    }
  }

  /**
   * Represents a component feature that has a name, description, and
   * parameters.
   */
  protected abstract class ParameterizedFeature extends Feature {
    // Inherits name, description
    protected final List<Parameter> parameters;

    protected ParameterizedFeature(String name, String description, String longDescription,
        String feature, boolean userVisible, boolean deprecated) {
      super(name, description, longDescription, feature, userVisible, deprecated);
      parameters = Lists.newArrayList();
    }

    /**
     * Adds the given parameter to this ParameterizedFeature.
     * @param param The parameter to add to this ParameterizedFeature.
     */
    protected void addParameter(Parameter param) {
      parameters.add(param);
    }

    /**
     * Generates a comma-separated string corresponding to the parameter list,
     * using Yail types (e.g., "number n, text t1").
     *
     * @return a string representation of the parameter list
     * @throws RuntimeException if the parameter type cannot be mapped to any
     *         of the legal return values
     */
    protected String toParameterString() {
      StringBuilder sb = new StringBuilder();
      int count = 0;
      for (Parameter param : parameters) {
        sb.append(param.getYailType());
        sb.append(" ");
        sb.append(param.name);
        if (++count != parameters.size()) {
          sb.append(", ");
        }
      }
      return new String(sb);
    }
  }

  /**
   * Represents an App Inventor event (annotated with {@link SimpleEvent}).
   */
  protected final class Event extends ParameterizedFeature
      implements Cloneable, Comparable<Event> {
    // Inherits name, description, and parameters

    protected Event(String name, String description, String longDescription, boolean userVisible, boolean deprecated) {
      super(name, description, longDescription, "Event", userVisible, deprecated);
    }

    @Override
    public Event clone() {
      Event that = new Event(name, description, longDescription, userVisible, deprecated);
      for (Parameter p : parameters) {
        that.addParameter(p.clone());
      }
      return that;
    }

    @Override
    public int compareTo(Event e) {
      return name.compareTo(e.name);
    }
  }

  /**
   * Represents an App Inventor component method (annotated with
   * {@link SimpleFunction}).
   */
  protected final class Method extends ParameterizedFeature
      implements Cloneable, Comparable<Method> {
    /**
     * The method's Java return type. Null if the method is a void method.
     */
    private TypeMirror returnType;
    /**
     * The helper key associated with this method's return type.
     */
    private HelperKey returnHelperKey;
    /**
     * Indicate whether this method's return type an integer that represents a
     * color.
     */
    private boolean color;

    /**
     * Indicate whether the method's return should be re-written into a continuation.
     */
    private boolean continuation;

    protected Method(String name, String description, String longDescription, boolean userVisible,
        boolean deprecated) {
      super(name, description, longDescription, "Method", userVisible, deprecated);
    }

    /**
     * Returns the string representation of this method's Java return type, or null of this method
     * is a void method.
     */
    protected String getReturnType() {
      if (returnType != null) {
        return returnType.toString();
      }
      return null;
    }

    /**
     * Returns this method's Yail return type (e.g., "number", "text", "list", etc).
     * @return the method's Yail return type.
     */
    protected String getYailReturnType() {
      return javaTypeToYailType(returnType, continuation);
    }

    /**
     * Returns the HelperKey associated with the return type of this method, if one exists. Null
     * otherwise.
     * 
     * @return the helper key associated with the return type of this method.
     */
    protected HelperKey getReturnHelperKey() {
      return returnHelperKey;
    }

    /**
     * Returns true if this method's return type is an integer which represents a color.
     * 
     * @return true if this method's return type is an integer which represents a color.
     */
    protected boolean isColor() {
      return color;
    }

    protected boolean isContinuation() {
      return continuation;
    }

    @Override
    public Method clone() {
      Method that = new Method(name, description, longDescription, userVisible, deprecated);
      for (Parameter p : parameters) {
        that.addParameter(p.clone());
      }
      that.returnType = returnType;
      that.returnHelperKey = returnHelperKey;
      return that;
    }

    @Override
    public int compareTo(Method f) {
      return name.compareTo(f.name);
    }
  }

  /**
   * Represents an App Inventor component property (annotated with
   * {@link SimpleProperty}).
   */
  protected final class Property extends Feature implements Cloneable {
    protected final String name;
    private PropertyCategory propertyCategory;
    private TypeMirror type;
    private boolean readable;
    private boolean writable;
    private String componentInfoName;
    private boolean color;
    private HelperKey helper;

    protected Property(String name, String description, String longDescription,
        PropertyCategory category, boolean userVisible, boolean deprecated) {
      super(name, description, longDescription, "Property", userVisible, deprecated);
      this.propertyCategory = category;
      this.name = name;
      // All other properties can be left as their defaults.
    }

    @Override
    public Property clone() {
      Property that = new Property(name, description, longDescription, propertyCategory,
          isUserVisible(), isDeprecated());
      that.type = type;
      that.readable = readable;
      that.writable = writable;
      that.componentInfoName = componentInfoName;
      that.color = color;
      that.helper = helper;
      return that;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("<Property name: ");
      sb.append(name);
      sb.append(", type: ");
      sb.append(type);
      if (readable) {
        sb.append(" readable");
      }
      if (writable) {
        sb.append(" writable");
      }
      sb.append(">");
      return sb.toString();
    }

    /**
     * Returns the description of this property, as retrieved by
     * {@link SimpleProperty#description()}.
     *
     * @return the description of this property
     */
    protected String getDescription() {
      return description;
    }

    /**
     * Returns the string representaiton of this property's java type.
     * 
     * @return the string representaiton of this property's java type.
     */
    protected String getType() {
      // Type should always be non-null.
      return type.toString();
    }

    /**
     * Returns this property's Yail type (e.g., "number", "text", "list", etc).
     * 
     * @return this property's Yail type (e.g., "number", "text", "list", etc).
     */
    protected String getYailType() {
      return javaTypeToYailType(type);
    }

    /**
     * Returns whether this property is readable (has a getter).
     *
     * @return whether this property is readable
     */
    protected boolean isReadable() {
      return readable;
    }

    /**
     * Returns whether this property is writable (has a setter).
     *
     * @return whether this property is writable
     */
    protected boolean isWritable() {
      return writable;
    }

    protected boolean isColor() {
      return color;
    }

    /**
     * Returns the HelperKey associated with this property, if one exists. Null otherwise.
     * @return the HelperKey associated with this property, if one exists.
     */
    protected HelperKey getHelperKey() {
      return helper;
    }

    /**
     * Returns a string indicating whether this property is readable and/or
     * writable.
     *
     * @return one of "read-write", "read-only", or "write-only"
     * @throws {@link RuntimeException} if the property is neither readable nor
     *         writable
     */
    protected String getRwString() {
      if (readable) {
        if (writable) {
          return READ_WRITE;
        } else {
          return READ_ONLY;
        }
      } else {
        if (!writable) {
          throw new RuntimeException("Property " + name +
                                     " is neither readable nor writable");
        }
        return WRITE_ONLY;
      }
    }

    protected PropertyCategory getCategory() {
      return propertyCategory;
    }
  }

  /**
   * An enum specifying the available types of helper blocks aka types of helper UI.
   * Currently the only type of helper UI is an OPTION_LIST which defines a dropdown UI in the
   * blocks editor. This is associated with the OptionList data type, ie OptionList data always has
   * an OPTION_LIST style UI in the blocks editor (as of now).
   */
  protected enum HelperType { OPTION_LIST, ASSET, PROVIDER_MODEL, PROVIDER }

  /**
   * A key that allows you to access info about a helper block.
   * 
   * <p>This class could be generic, and we could use subtyping to define the different HelperTypes
   * but I (Beka) think it makes more sense to make this closely match the JavaScript
   * implementation.
   */
  protected static final class HelperKey {
    private final HelperType helperType;

    private final Object key;

    /**
     * Creates a HelperKey which can be used to access data about a helper block.
     */
    protected HelperKey(HelperType type, Object key) {
      this.helperType = type;
      this.key = key;
    }

    /**
     * Returns the type of helper block, aka the type of helper UI. Eg an option list.
     * @return the type of helper block.
     */
    protected HelperType getType() {
      return helperType;
    }

    /**
     * Returns the key to the specific helper data. Eg in the case of an option list helper, this
     * key could be used to look up values in the optionLists Map.
     * If the helper block doesn't need any special data, this can just return null.
     * @return key to the helper data.
     */
    protected Object getKey() {
      return key;
    }
  }


  /**
   * Represents a list of Options associated with some (enum) class. The data in this OptionList
   * is used to create OptionList helper blocks.
   * 
   * <p>Here JSON-ified example of such data, in this case we are looking at the Direction enum with
   * a default value of East.
   * {
   *   "className": "com.google.appinventor.components.common.Direction",
   *   "key": "Direction",
   *   "tag": "Direction",
   *   "defaultOpt": "East",
   *   "underlyingType": "java.lang.Integer",
   *   "options": [
   *     { "name": "North", "value": "1", "description": "Option for North",
   *       "deprecated": "false" },
   *     { "name": "Northeast", "value": "2", "description": "Option for Northeast",
   *       "deprecated": "false" },
   *     { "name": "East", "value": "3", "description": "Option for East",
   *       "deprecated": "false" },
   *     { "name": "Southeast", "value": "4", "description": "Option for Southeast",
   *       "deprecated": "false" },
   *     { "name": "South", "value": "-1", "description": "Option for South",
   *       "deprecated": "false" },
   *     { "name": "Southwest", "value": "-2", "description": "Option for Southwest",
   *       "deprecated": "false" },
   *     { "name": "West", "value": "-3", "description": "Option for West",
   *       "deprecated": "false" },
   *     { "name": "Northwest", "value": "-4", "description": "Option for Northwest",
   *       "deprecated": "false" }
       ]
   * }
   */
  protected final class OptionList {
    /**
     * A list of option values (Strings) and option info (Options).
     * For built-in components the Option name is used to look up the translated display text.
     * For extensions, which do not support i18n, the Option name /is/ the display text.
     */
    private final ArrayList<Option> options;

    /**
     * The fully qualified class name this OptionList is associated with.
     */
    private final String className;

    /**
     * The tag name this OptionList is associated with. This goes in front of the dropdown in the
     * blocks editor. It is always the simplified class name.
     */
    private final String tagName;

    /**
     * The Option.name of the default option associated with this OptionList.
     */
    private String defaultOpt;

    /**
     * The underlying type passed to this OptionList. E.g., in the case of OptionList&lt;Integer&gt;
     * this would be a TypeMirror representing the type Integer.
     */
    private TypeMirror underlyingType;

    /**
     * Creates an OptionList (which is a definition of a option list helper-block) that can be
     * populated with options.
     * 
     * @param className The fully qualified class name this OptionList is associated with.
     * @param tagName The tag name this OptionList is associated with. This goes in front of the
     *     dropdown in the blocks editor. It is usually the simplified class name.
     */
    protected OptionList(String className, String tagName) {
      this.className = className;
      this.tagName = tagName;
      options = new ArrayList<>();
    }

    /**
     * Returns the fully qualified class name this OptionList is associated with.
     * @return the fully qualified class name this OptionList is associated with.
     */
    protected String getClassName() {
      return className;
    }

    /**
     * The tag name, which is used in the dropdown block UI, of this OptionList. It is usually the
     * simplified class name.
     * @return The tag name of this OptionList.
     */
    protected String getTagName() {
      return tagName;
    }

    /**
     * Sets the default option of this OptionList.
     * @param defaultOpt the Option.name of the default option to set.
     */
    protected void setDefault(String defaultOpt) {
      this.defaultOpt = defaultOpt;
    }

    /**
     * Returns the Option.name of the default option associated with this OptionList.
     * @return the Option.name of the default option associated with this OptionList.
     */
    protected String getDefault() {
      return defaultOpt;
    }

    /**
     * Sets the underlying type of this OptionList.
     * @param type the underlying type to assign to this OptionList.
     */
    protected void setUnderlyingType(TypeMirror type) {
      underlyingType = type;
    }

    /**
     * Returns the underlying type of this OptionList.
     * @return the underlying type of this OptionList.
     */
    protected TypeMirror getUnderlyingType() {
      return underlyingType;
    }

    /**
     * Adds the given Option to the OptionList.
     * @param option the option to add to this OptionList.
     */
    protected void addOption(Option option) {
      options.add(option);
    }

    /**
     * Returns true if this OptionList contains the given option.
     * @return true if this OptionList contains the given option.
     */
    protected boolean containsOption(Option option) {
      return options.contains(option);
    }

    /**
     * Returns true if this option list has no options.
     * @return true if this option list has no options.
     */
    protected boolean isEmpty() {
      return options.isEmpty();
    }

    /**
     * Returns a collection of Options that make up this option list.
     * @return a collection of Options that make up this option list.
     */
    protected Collection<Option> asCollection() {
      return options;
    }
  }

  /**
   * Represents an option (enum constant) with a name and backing value.
   */
  protected static final class Option extends Feature {
    /**
     * The value this option is associated with.
     */
    private final String value;

    protected Option(String name, String value, String description, boolean deprecated) {
      super(name, description, description, "Option", true, deprecated);
      this.value = value;
    }

    /**
     * Returns the description of this option.
     * @return the description of this option.
     */
    protected String getDescription() {
      return description;
    }

    /**
     * Returns the value this option is associated with.
     * @return the value this option is associated with.
     */
    protected String getValue() {
      return value;
    }
  }


  /**
   * Represents an App Inventor component, including its designer properties,
   * Simple properties, methods, and events.
   */
  protected final class ComponentInfo extends Feature {
    // Inherits name and description
    /**
     * Permissions required by this component.
     * @see android.Manifest.permission
     */
    protected final Set<String> permissions;

    /**
     * Permission constraints required by this component.
     */
    protected final Map<String, PermissionConstraint> permissionConstraints;

    /**
     * Mapping of component block names to permissions that should be included
     * if the block is used.
     */
    protected final Map<String, String[]> conditionalPermissions;

    /**
     * Mapping of component block names to permissions constraints that should
     * be included if the block is used.
     */
    protected final Map<String, Map<String, PermissionConstraint>> conditionalPermissionConstraints;

    /**
     * Mapping of component block names to broadcast receivers that should be
     * included if the block is used.
     */
    protected final Map<String, String[]> conditionalBroadcastReceivers;

    /**
     * Mapping of component block names to queries that should be included
     * if the block is used.
     */
    protected final Map<String, String[]> conditionalQueries;

    /**
     * Mapping of component block names to services that should be
     * included if the block is used.
     */
    protected final Map<String, String[]> conditionalServices;

    /**
     * Mapping of component block names to content providers that should be
     * included if the block is used.
     */
    protected final Map<String, String[]> conditionalContentProviders;

    /**
     * Libraries required by this component.
     */
    protected final Set<String> libraries;

    /**
     * Native libraries required by this component.
     */
    protected final Set<String> nativeLibraries;

    /**
     * Assets required by this component.
     */
    protected final Set<String> assets;

    /**
     * Activities required by this component.
     */
    protected final Set<String> activities;

    /**
     * Metadata required by this component.
     */
    protected final Set<String> metadata;

    /**
     * Activity metadata required by this component.
     */
    protected final Set<String> activityMetadata;

    /**
     * Broadcast receivers required by this component.
     */
    protected final Set<String> broadcastReceivers;

    /**
     * Queries required by this component.
     */
    protected final Set<String> queries;

    /**
     * Services required by this component.
     */
    protected final Set<String> services;

    /**
     * Content providers required by this component.
     */
    protected final Set<String> contentProviders;

    /**
     * Xml files required by this component.
     */
    protected final Set<String> xmls;

    /**
     * TODO(Will): Remove the following field once the deprecated {@link SimpleBroadcastReceiver}
     *             annotation is removed. It should should remain for the time being
     *             because otherwise we'll break extensions currently using it.
     *
     * Class Name and Filter Actions for a simple Broadcast Receiver
     */
    protected final Set<String> classNameAndActionsBR;

    /**
     * Properties of this component that are visible in the Designer.
     * @see DesignerProperty
     */
    protected final SortedMap<String, DesignerProperty> designerProperties;

    /**
     * Properties of this component, whether or not they are visible in
     * the Designer.  The keys of this map are a superset of the keys of
     * {@link #designerProperties}.
     */
    protected final SortedMap<String, Property> properties;

    /**
     * Methods provided by this component.
     */
    protected final SortedMap<String, Method> methods;

    /**
     * Events provided by this component.
     */
    protected final SortedMap<String, Event> events;

    /**
     * Whether this component is abstract (such as
     * {@link com.google.appinventor.components.runtime.Sprite}) or concrete.
     */
    protected final boolean abstractClass;

    /**
     * The displayed name of this component.  This is usually the same as the
     * {@link Class#getSimpleName()}.  The exception is for the component
     * {@link com.google.appinventor.components.runtime.Form}, for which the
     * name "Screen" is used.
     */
    protected final String displayName;

    protected final String type;
    protected boolean external;

    private String helpDescription;  // Shorter popup description
    private String helpUrl;  // Custom help URL for extensions
    private String category;
    private String categoryString;
    private boolean simpleObject;
    private boolean designerComponent;
    private int version;
    private boolean showOnPalette;
    private boolean nonVisible;
    private String iconName;
    private int androidMinSdk;
    private String versionName;
    private String dateBuilt;
    private String licenseName;

    protected ComponentInfo(Element element) {
      super(element.getSimpleName().toString(),  // Short name
            elementUtils.getDocComment(element),
            elementUtils.getDocComment(element),
            "Component", false, elementUtils.isDeprecated(element));
      type = element.asType().toString();
      displayName = getDisplayNameForComponentType(name);

      conditionalBroadcastReceivers = Maps.newTreeMap();
      conditionalContentProviders = Maps.newTreeMap();
      conditionalPermissionConstraints = Maps.newTreeMap();
      conditionalPermissions = Maps.newTreeMap();
      conditionalQueries = Maps.newTreeMap();
      conditionalServices = Maps.newTreeMap();

      assets = Sets.newHashSet();
      activities = Sets.newHashSet();
      activityMetadata = Sets.newHashSet();
      broadcastReceivers = Sets.newHashSet();
      classNameAndActionsBR = Sets.newHashSet();
      contentProviders = Sets.newHashSet();
      libraries = Sets.newHashSet();
      metadata = Sets.newHashSet();
      nativeLibraries = Sets.newHashSet();
      permissionConstraints = Maps.newTreeMap();
      permissions = Sets.newHashSet();
      queries = Sets.newHashSet();
      services = Sets.newHashSet();
      xmls = Sets.newHashSet();

      designerProperties = Maps.newTreeMap();
      properties = Maps.newTreeMap();
      methods = Maps.newTreeMap();
      events = Maps.newTreeMap();
      abstractClass = element.getModifiers().contains(Modifier.ABSTRACT);
      external = false;
      versionName = null;
      dateBuilt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());
      for (AnnotationMirror am : element.getAnnotationMirrors()) {
        DeclaredType dt = am.getAnnotationType();
        String annotationName = am.getAnnotationType().toString();
        if (annotationName.equals(SimpleObject.class.getName())) {
          simpleObject = true;
          SimpleObject simpleObjectAnnotation = element.getAnnotation(SimpleObject.class);
          external = simpleObjectAnnotation.external();
        }
        if (annotationName.equals(DesignerComponent.class.getName())) {
          designerComponent = true;
          DesignerComponent designerComponentAnnotation =
              element.getAnnotation(DesignerComponent.class);
          Map values = elementUtils.getElementValuesWithDefaults(am);
          for (Map.Entry entry : (Set<Map.Entry>) values.entrySet()) {
            if (((ExecutableElement) entry.getKey()).getSimpleName().contentEquals("dateBuilt")) {
              entry.setValue(new AnnotationValue() {
                @Override
                public Object getValue() {
                  return dateBuilt;
                }

                @Override
                public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
                  return v.visit(this, p);
                }

                @Override
                public String toString() {
                  return "\"" + dateBuilt + "\"";
                }
              });
            }
          }

          // Override javadoc description with explicit description
          // if provided.
          String explicitDescription = designerComponentAnnotation.description();
          if (!explicitDescription.isEmpty()) {
            setDescription(explicitDescription);
          }

          // Set helpDescription to the designerHelpDescription field if
          // provided; otherwise, use description
          helpDescription = designerComponentAnnotation.designerHelpDescription();
          if (helpDescription.isEmpty()) {
            helpDescription = description;
          }
          helpUrl = designerComponentAnnotation.helpUrl();
          if (!helpUrl.startsWith("http:") && !helpUrl.startsWith("https:")) {
            helpUrl = "";  // only accept http: or https: URLs (e.g., no javascript:)
          }

          category = designerComponentAnnotation.category().getName();
          categoryString = designerComponentAnnotation.category().toString();
          version = designerComponentAnnotation.version();
          showOnPalette = designerComponentAnnotation.showOnPalette();
          nonVisible = designerComponentAnnotation.nonVisible();
          iconName = designerComponentAnnotation.iconName();
          licenseName = designerComponentAnnotation.licenseName();
          androidMinSdk = designerComponentAnnotation.androidMinSdk();
          versionName = designerComponentAnnotation.versionName();
          userVisible = designerComponentAnnotation.showOnPalette();
        }
      }
    }

    /**
     * A brief description of this component to be shown when the user requests
     * help in the Designer.  This is obtained from the first of the following that
     * was provided in the source code for the component:
     * <ol>
     *   <li> {@link DesignerComponent#designerHelpDescription()}</li>
     *   <li> {@link DesignerComponent#description()}</li>
     *   <li> the Javadoc preceding the beginning of the class corresponding to the component</li>
     * </ol>
     */
    protected String getHelpDescription() {
      return helpDescription;
    }

    /**
     * Custom help URL to documentation for a component (typically an extension)
     *
     * @return  the custom help URL, if any, for the component
     */
    protected String getHelpUrl() {
      return helpUrl;
    }

    /**
     * Returns the name of this component's category within the Designer, as displayed
     * (for example, "Screen Arrangement").
     *
     * @return the name of this component's Designer category
     */
    protected String getCategory() {
      return category;
    }

    /**
     * Returns the String representation of the EnumConstant corresponding to this
     * component's category within the Designer (for example, "ARRANGEMENTS").
     * Usually, you should use {@link #getCategory()} instead.
     *
     * @return the EnumConstant representing this component's Designer category
     */
    protected String getCategoryString() {
      return categoryString;
    }

    /**
     * Returns the version number of this component, as specified by
     * {@link DesignerComponent#version()}.
     *
     * @return the version number of this component
     */
    protected int getVersion() {
      return version;
    }

    /**
     * Returns whether this component is shown on the palette in the Designer, as
     * specified by {@link DesignerComponent#showOnPalette()}.
     *
     * @return whether this component is shown on the Designer palette
     */
    protected boolean getShowOnPalette() {
      return showOnPalette;
    }

    /**
     * Returns whether this component is non-visible on the device's screen, as
     * specified by {@link DesignerComponent#nonVisible()}.  Examples of non-visible
     * components are {@link com.google.appinventor.components.runtime.LocationSensor}
     * and {@link com.google.appinventor.components.runtime.Clock}.
     *
     * @return {@code true} if the component is non-visible, {@code false} otherwise
     */
    protected boolean getNonVisible() {
      return nonVisible;
    }

    /**
     * Returns whether this component is an external component or not.
     *
     * @return true if the component is external. false otherwise.
     */
    protected boolean getExternal() {
      return external;
    }

    /**
     * Returns the name of the icon file used on the Designer palette, as specified in
     * {@link DesignerComponent#iconName()}.
     *
     * @return the name of the icon file
     */
    protected String getIconName() {
      return iconName;
    }

    /**
     * Returns the minimum Android SDK required for the component to run, as specified in
     * {@link DesignerComponent#androidMinSdk()}.
     *
     * @return the minimum Android sdk for the component
     */
    protected int getAndroidMinSdk() {
      return androidMinSdk;
    }

    protected String getVersionName() {
      return versionName;
    }

    protected String getDateBuilt() {
      return dateBuilt;
    }

    /**
     * Returns the name of the license file used by external components
     * {@link DesignerComponent#licenseName()}.
     *
     * @return the name of the license file
     */
    protected String getLicenseName() {
      return licenseName;
    }

    private String getDisplayNameForComponentType(String componentTypeName) {
      // Users don't know what a 'Form' is.  They know it as a 'Screen'.
      return "Form".equals(componentTypeName) ? "Screen" : componentTypeName;
    }

    protected String getName() {
      if (name.equals("Form")) {
        return "Screen";
      } else {
        return name;
      }
    }

  }

  /**
   * Returns the annotations supported by this {@code ComponentProcessor}, namely those related
   * to components ({@link com.google.appinventor.components.annotations}).
   *
   * @return the supported annotations
   */
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return SUPPORTED_ANNOTATION_TYPES;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    try {
      return (SourceVersion) SourceVersion.class.getDeclaredField("RELEASE_11").get(null);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      return SourceVersion.RELEASE_8;
    }
  }

  @Override
  public void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
  }

  /**
   * Processes the component-related annotations ({@link
   * com.google.appinventor.components.annotations}),
   * populating {@link #components} and initializing {@link #messager} for use within
   * {@link #outputResults()}, which is called at the end of this method and must be overriden by
   * concrete subclasses.
   *
   * @param annotations the annotation types requested to be processed
   * @param roundEnv environment for information about the current and prior round
   * @return {@code true}, indicating that the annotations have been claimed by this processor.
   * @see AbstractProcessor#process
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    // This method will be called many times for the source code.
    // Only do something on the first pass.
    pass++;
    if (pass > 1) {
      return true;
    }

    messager = processingEnv.getMessager();

    List<Element> elements = new ArrayList<>();
    List<Element> excludedElements = new ArrayList<>();
    for (TypeElement te : annotations) {
      if (te.getSimpleName().toString().equals("DesignerComponent")) {
        elements.addAll(roundEnv.getElementsAnnotatedWith(te));
      } else if (te.getSimpleName().toString().equals("SimpleObject")) {
        for (Element element : roundEnv.getElementsAnnotatedWith(te)) {
          SimpleObject annotation = element.getAnnotation(SimpleObject.class);
          if (!annotation.external()) {
            elements.add(element);
          } else {
            excludedElements.add(element);
          }
        }
      }
    }
    for (Element element : elements) {
      processComponent(element);
    }

    // Put the component class names (including abstract classes)
    componentTypes.addAll(components.keySet());
    for (Element element : excludedElements) {
      componentTypes.add(element.asType().toString());  // allow extensions to reference one another
    }

    // Remove non-components before calling outputResults.
    List<String> removeList = Lists.newArrayList();
    for (Map.Entry<String, ComponentInfo> entry : components.entrySet()) {
      ComponentInfo component = entry.getValue();
      if (component.abstractClass || !component.designerComponent) {
        removeList.add(entry.getKey());
      }
    }
    components.keySet().removeAll(removeList);

    try {
      // This is an abstract method implemented in concrete subclasses.
      outputResults();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // We need to return false here so that sibling annotation processors can run
    return false;
  }

    /*
     * This processes an element if it represents a component, reading in its
     * information and adding it to components.  If this component is a
     * subclass of another component, this method recursively calls itself on the
     * superclass.
     */
  private void processComponent(Element element) {
    boolean isForDesigner = element.getAnnotation(DesignerComponent.class) != null;
    // If the element is not a component (e.g., Float), return early.
    if (element.getAnnotation(SimpleObject.class) == null && !isForDesigner) {
      return;
    }

    // If we already processed this component, return early.
    String longComponentName = ((TypeElement) element).getQualifiedName().toString();
    if (components.containsKey(longComponentName)) {
      return;
    }

    // Create new ComponentInfo.
    ComponentInfo componentInfo = new ComponentInfo(element);

    // Check if this extends another component (DesignerComponent or SimpleObject).
    List<? extends TypeMirror> directSupertypes = typeUtils.directSupertypes(element.asType());
    if (!directSupertypes.isEmpty()) {
      // Only look at the first one.  Later ones would be interfaces,
      // which we don't care about.
      String parentName = directSupertypes.get(0).toString();
      Element e = ((DeclaredType) directSupertypes.get(0)).asElement();
      parentName = ((TypeElement) e).getQualifiedName().toString();
      ComponentInfo parentComponent = components.get(parentName);
      if (parentComponent == null) {
        // Try to process the parent component now.
        Element parentElement = elementUtils.getTypeElement(parentName);
        if (parentElement != null) {
          processComponent(parentElement);
          parentComponent = components.get(parentName);
        }
      }

      // If we still can't find the parent class, we don't care about it, since it's not a
      // component (but something like java.lang.Object).  Otherwise, we need to copy its
      // build info, designer properties, properties, methods, and events.
      if (parentComponent != null) {
        // Copy its build info, designer properties, properties, methods, and events.
        componentInfo.permissions.addAll(parentComponent.permissions);
        componentInfo.libraries.addAll(parentComponent.libraries);
        componentInfo.nativeLibraries.addAll(parentComponent.nativeLibraries);
        componentInfo.assets.addAll(parentComponent.assets);
        componentInfo.activities.addAll(parentComponent.activities);
        componentInfo.metadata.addAll(parentComponent.metadata);
        componentInfo.activityMetadata.addAll(parentComponent.activityMetadata);
        componentInfo.broadcastReceivers.addAll(parentComponent.broadcastReceivers);
        componentInfo.queries.addAll(parentComponent.queries);
        componentInfo.services.addAll(parentComponent.services);
        componentInfo.contentProviders.addAll(parentComponent.contentProviders);
        componentInfo.xmls.addAll(parentComponent.xmls);
        // TODO(Will): Remove the following call once the deprecated
        //             @SimpleBroadcastReceiver annotation is removed. It should
        //             should remain for the time being because otherwise we'll break
        //             extensions currently using @SimpleBroadcastReceiver.
        componentInfo.classNameAndActionsBR.addAll(parentComponent.classNameAndActionsBR);
        // Since we don't modify DesignerProperties, we can just call Map.putAll to copy the
        // designer properties from parentComponent to componentInfo.
        componentInfo.designerProperties.putAll(parentComponent.designerProperties);
        // NOTE(lizlooney) We can't just call Map.putAll to copy the events/properties/methods from
        // parentComponent to componentInfo because then each component will share a single
        // Event/Property/Method and if one component overrides something about an
        // Event/Property/Method, then it will affect all the other components that are sharing
        // that Event/Property/Method.
        for (Map.Entry<String, Event> entry : parentComponent.events.entrySet()) {
          componentInfo.events.put(entry.getKey(), entry.getValue().clone());
        }
        for (Map.Entry<String, Property> entry : parentComponent.properties.entrySet()) {
          componentInfo.properties.put(entry.getKey(), entry.getValue().clone());
        }
        for (Map.Entry<String, Method> entry : parentComponent.methods.entrySet()) {
          componentInfo.methods.put(entry.getKey(), entry.getValue().clone());
        }
      }
    }

    // Gather permissions.
    UsesPermissions usesPermissions = element.getAnnotation(UsesPermissions.class);
    if (usesPermissions != null) {
      for (String permission : usesPermissions.permissionNames().split(",")) {
        updateWithNonEmptyValue(componentInfo.permissions, permission);
      }
      Collections.addAll(componentInfo.permissions, usesPermissions.value());
      for (PermissionConstraint constraint : usesPermissions.constraints()) {
        componentInfo.permissions.add(constraint.name());
        componentInfo.permissionConstraints.put(constraint.name(), constraint);
      }
    }

    // Gather library names.
    UsesLibraries usesLibraries = element.getAnnotation(UsesLibraries.class);
    if (usesLibraries != null) {
      for (String library : usesLibraries.libraries().split(",")) {
        updateWithNonEmptyValue(componentInfo.libraries, library);
      }
      Collections.addAll(componentInfo.libraries, usesLibraries.value());
    }

    // Gather native library names.
    UsesNativeLibraries usesNativeLibraries = element.getAnnotation(UsesNativeLibraries.class);
    if (usesNativeLibraries != null) {
      for (String nativeLibrary : usesNativeLibraries.libraries().split(",")) {
        updateWithNonEmptyValue(componentInfo.nativeLibraries, nativeLibrary);
      }
      for (String v7aLibrary : usesNativeLibraries.v7aLibraries().split(",")) {
        updateWithNonEmptyValue(componentInfo.nativeLibraries, v7aLibrary.trim() + ARMEABI_V7A_SUFFIX);
      }
      for (String v8aLibrary : usesNativeLibraries.v8aLibraries().split(",")) {
        updateWithNonEmptyValue(componentInfo.nativeLibraries, v8aLibrary.trim() + ARM64_V8A_SUFFIX);
      }
      for (String x8664Library : usesNativeLibraries.x86_64Libraries().split(",")) {
        updateWithNonEmptyValue(componentInfo.nativeLibraries, x8664Library.trim() + X86_64_SUFFIX);
      }

    }

    // Gather required files.
    UsesAssets usesAssets = element.getAnnotation(UsesAssets.class);
    if (usesAssets != null) {
      for (String file : usesAssets.fileNames().split(",")) {
        updateWithNonEmptyValue(componentInfo.assets, file);
      }
    }

    // Gather the required activities and build their element strings.
    UsesActivities usesActivities = element.getAnnotation(UsesActivities.class);
    if (usesActivities != null) {
      try {
        for (ActivityElement ae : usesActivities.activities()) {
          updateWithNonEmptyValue(componentInfo.activities, activityElementToString(ae));
        }
      } catch (IllegalAccessException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "IllegalAccessException when gathering " +
            "activity attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "InvocationTargetException when gathering " +
            "activity attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      }
    }

    // Gather the required metadata and build their element strings.
    UsesApplicationMetadata usesApplicationMetadata = element.getAnnotation(UsesApplicationMetadata.class);
    if (usesApplicationMetadata != null) {
      try {
        for (MetaDataElement me : usesApplicationMetadata.metaDataElements()) {
          updateWithNonEmptyValue(componentInfo.metadata, metaDataElementToString(me));
        }
      } catch (IllegalAccessException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "IllegalAccessException when gathering " +
            "application metadata and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "InvocationTargetException when gathering " +
            "application metadata and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      }
    }

    // Gather the required activity metadata and build their element strings.
    UsesActivityMetadata usesActivityMetadata = element.getAnnotation(UsesActivityMetadata.class);
    if (usesActivityMetadata != null) {
      try {
        for (MetaDataElement me : usesActivityMetadata.metaDataElements()) {
          updateWithNonEmptyValue(componentInfo.activityMetadata, metaDataElementToString(me));
        }
      } catch (IllegalAccessException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "IllegalAccessException when gathering " +
                "application metadata and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "InvocationTargetException when gathering " +
                "application metadata and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      }
    }

    // Gather the required broadcast receivers and build their element strings.
    UsesBroadcastReceivers usesBroadcastReceivers = element.getAnnotation(UsesBroadcastReceivers.class);
    if (usesBroadcastReceivers != null) {
      try {
        for (ReceiverElement re : usesBroadcastReceivers.receivers()) {
          updateWithNonEmptyValue(componentInfo.broadcastReceivers, receiverElementToString(re));
        }
      } catch (IllegalAccessException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "IllegalAccessException when gathering " +
            "broadcast receiver attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "InvocationTargetException when gathering " +
            "broadcast receiver attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      }
    }

    // Gather the required queries and build their element strings.
    UsesQueries usesQueries = element.getAnnotation(UsesQueries.class);
    if (usesQueries != null) {
      try {
        for (String packageName : usesQueries.packageNames()) {
          componentInfo.queries.add("<package android:name=\"" + packageName + "\" />");
        }
        for (IntentFilterElement intent : usesQueries.intents()) {
          updateWithNonEmptyValue(componentInfo.queries, intentFilterElementToIntentString(intent));
        }
        for (ProviderElement provider : usesQueries.providers()) {
          updateWithNonEmptyValue(componentInfo.queries, providerElementToString(provider));
        }
      } catch (IllegalAccessException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "IllegalAccessException when gathering "
            + "service attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "InvocationTargetException when gathering "
            + "service attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      }
    }

    // Gather the required services and build their element strings.
    UsesServices usesServices = element.getAnnotation(UsesServices.class);
    if (usesServices != null) {
      try {
        for (ServiceElement se : usesServices.services()) {
          updateWithNonEmptyValue(componentInfo.services, serviceElementToString(se));
        }
      } catch (IllegalAccessException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "IllegalAccessException when gathering " +
            "service attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "InvocationTargetException when gathering " +
            "service attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      }
    }

    // Gather the required content providers and build their element strings.
    UsesContentProviders usesContentProviders = element.getAnnotation(UsesContentProviders.class);
    if (usesContentProviders != null) {
      try {
        for (ProviderElement pe : usesContentProviders.providers()) {
          updateWithNonEmptyValue(componentInfo.contentProviders, providerElementToString(pe));
        }
      } catch (IllegalAccessException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "IllegalAccessException when gathering " +
            "provider attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "InvocationTargetException when gathering " +
            "provider attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      }
    }

    // Gather the required xml files and build their element strings.
    UsesXmls usesXmls = element.getAnnotation(UsesXmls.class);
    if (usesXmls != null) {
      for (XmlElement xe : usesXmls.xmls()) {
        updateWithNonEmptyValue(componentInfo.xmls, xmlElementToString(xe));
      }
    }

    // TODO(Will): Remove the following legacy code once the deprecated
    //             @SimpleBroadcastReceiver annotation is removed. It should
    //             should remain for the time being because otherwise we'll break
    //             extensions currently using @SimpleBroadcastReceiver.
    //
    // Gather required actions for legacy Broadcast Receivers. The annotation
    // has a Class Name and zero or more Filter Actions.  In the
    // resulting String, Class name will go first, and each Action
    // will be added, separated by a comma.

    SimpleBroadcastReceiver simpleBroadcastReceiver = element.getAnnotation(SimpleBroadcastReceiver.class);
    if (simpleBroadcastReceiver != null) {
      for (String className : simpleBroadcastReceiver.className().split(",")){
        StringBuffer nameAndActions = new StringBuffer();
        nameAndActions.append(className.trim());
        for (String action : simpleBroadcastReceiver.actions().split(",")) {
          nameAndActions.append("," + action.trim());
        }
        componentInfo.classNameAndActionsBR.add(nameAndActions.toString());
        break; // We only need one class name; If more than one is passed, ignore all but first.
      }
    }

    // Build up event information.
    processEvents(componentInfo, element);

    // Build up property information.
    processProperties(componentInfo, element);

    // Build up method information.
    processMethods(componentInfo, element);

    if (isForDesigner) {
      processDescriptions(componentInfo);
    }

    // Add it to our components map.
    components.put(longComponentName, componentInfo);
  }

  private void processDescriptions(ComponentInfo info) {
    final String name = info.displayName;
    info.description = info.description.replaceAll(TYPE_PLACEHOLDER, name);
    info.helpUrl = info.helpUrl.replaceAll(TYPE_PLACEHOLDER, name);
    for (Property property : info.properties.values()) {
      property.description = property.description.replaceAll(TYPE_PLACEHOLDER, name);
    }
    for (Event event : info.events.values()) {
      event.description = event.description.replaceAll(TYPE_PLACEHOLDER, name);
    }
    for (Method method : info.methods.values()) {
      method.description = method.description.replaceAll(TYPE_PLACEHOLDER, name);
    }
  }

  private boolean isPublicMethod(Element element) {
    return element.getModifiers().contains(Modifier.PUBLIC)
        && element.getKind() == ElementKind.METHOD;
  }

  private Property executableElementToProperty(Element element, String componentInfoName) {
    String propertyName = element.getSimpleName().toString();
    SimpleProperty simpleProperty = element.getAnnotation(SimpleProperty.class);

    if (!(element.asType() instanceof ExecutableType)) {
      throw new RuntimeException("element.asType() is not an ExecutableType for " +
                                 propertyName);
    }

    // Use Javadoc for property unless description is set to a non-empty string.
    String description = elementUtils.getDocComment(element);
    String longDescription = description;
    if (!simpleProperty.description().isEmpty()) {
      description = simpleProperty.description();
    }
    if (description == null) {
      description = "";
    }
    // Read only until the first javadoc parameter
    description = description.split("[^\\\\][@{]")[0].trim();

    Property property = new Property(propertyName,
                                     description,
                                     longDescription,
                                     simpleProperty.category(),
                                     simpleProperty.userVisible(),
                                     elementUtils.isDeprecated(element));

    // Get parameters to tell if this is a getter or setter.
    ExecutableType executableType = (ExecutableType) element.asType();
    List<? extends TypeMirror> parameters = executableType.getParameterTypes();

    // Check if it is a setter or getter, and set the property's readable, writable,
    // and type fields appropriately.
    TypeMirror typeMirror;
    if (parameters.size() == 0) {
      // It is a getter.
      property.readable = true;
      typeMirror = executableType.getReturnType();
      if (typeMirror.getKind().equals(TypeKind.VOID)) {
        throw new RuntimeException("Property method is void and has no parameters: "
                                   + propertyName);
      }
      property.helper = elementToHelperKey(element, ((ExecutableElement)element).getReturnType());
      if (element.getAnnotation(IsColor.class) != null) {
        property.color = true;
      }
    } else {
      // It is a setter.
      property.writable = true;
      if (parameters.size() != 1) {
        throw new RuntimeException("Too many parameters for setter for " +
                                   propertyName);
      }
      typeMirror = parameters.get(0);
      Element param = ((ExecutableElement) element).getParameters().get(0);
      property.helper = elementToHelperKey(param, param.asType());
      for (VariableElement ve : ((ExecutableElement) element).getParameters()) {
        if (ve.getAnnotation(IsColor.class) != null) {
          property.color = true;
        }
      }
    }

    // Use typeMirror to set the property's type.
    if (!typeMirror.getKind().equals(TypeKind.VOID)) {
      property.type = typeMirror;
      updateComponentTypes(typeMirror);
    }

    property.componentInfoName = componentInfoName;

    return property;
  }

  /**
   * Converts an element representing a function (for return types) or a parameter into a HelperKey.
   * 
   * @param elem the Element which represents a function (for return types) or a parameter.
   * @param type the TypeMirror representing the type of that element.
   * @return The created HelperKey if the element does indeed define a helper, null otherwise.
   */
  private HelperKey elementToHelperKey(Element elem, TypeMirror type) {
    HelperKey key;
    key = hasOptionListHelper(elem, type);
    if (key != null) {
      return key;
    }
    key = hasAssetsHelper(elem, type);
    if (key != null) {
      return key;
    }
    key = hasProviderModelHelper(elem, type);
    if (key != null) {
      return key;
    }
    key = hasProviderHelper(elem, type);
    if (key != null) {
      return key;
    }
    // Add more possibilities here.
    return null;
  }

  /**
   * Returns the associated helper key if the element has an OptionList associated with it.
   * Null otherwise.
   *
   * @param elem the Element which represents a function (for return types) or a parameter.
   * @param type the TypeMirror representing the type of that element.
   * @return the associated helper key if the element has an OptionList assciated with it.
   */
  private HelperKey hasOptionListHelper(Element elem, TypeMirror type) {
    // Check if the elem type is an OptionList
    if (isOptionList(type)) {
      return optionListToHelperKey(((DeclaredType) type).asElement());
    }

    // Check if the elem has an @Options annotation.
    // This is the backwards compat method for getting OptionLists.
    for (AnnotationMirror mirror : elem.getAnnotationMirrors()) {
      // Make sure we are dealing with an Options annotation.
      if (!mirror.getAnnotationType().asElement().getSimpleName().contentEquals("Options")) {
        continue;
      }
      for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry:
          mirror.getElementValues().entrySet()) {
        // Make sure we are looking at the value argument.
        if (!entry.getKey().getSimpleName().contentEquals("value")) {
          continue;
        }
        // Get the AnnotationValue's value. So we are now looking at the
        // class passed to the @Options annotation.
        Element optionList = ((DeclaredType)entry.getValue().getValue()).asElement();
        return optionListToHelperKey(optionList);
      }
    }
    return null;
  }

  /**
   * Returns true if the given type implements OptionList. False otherwise.
   * @param type the type to check if it implements OptionList.
   * @return true if the given type implements OptionList. False otherwise.
   */
  private boolean isOptionList(TypeMirror type) {
    if (type.getKind() == TypeKind.DECLARED) {
      TypeElement elem = (TypeElement)((DeclaredType)type).asElement();
      for (TypeMirror parent : elem.getInterfaces()) {
        TypeElement parentElem = (TypeElement)((DeclaredType)parent).asElement();
        if (parentElem.getSimpleName().toString().equals("OptionList")) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Returns the OptionList HelperKey associated with the given element.
   * @param optionList the Element describing a class which implements the OptionList interface.
   * @return the HelperKey associated with the given element.
   */
  private HelperKey optionListToHelperKey(Element optionList) {
    String name = optionList.getSimpleName().toString();
    // We haven't seen this type of option list before, so add it.
    if (optionLists.get(name) == null) {
      if (!tryAddOptionList(optionList)) {
        // Couldn't add it for whatever reason.
        return null;
      }
    }
    // This helper key is storing info about an OptionList.
    return new HelperKey(HelperType.OPTION_LIST, name);
  }

  /**
   * Adds a new OptionList (based on the passed option list element) to the optionLists list.
   * 
   * @param optionElem The element representing the enum defining the options.
   * @return Returns true if the Optionlist was successfully added. False otherwise.
   */
  private boolean tryAddOptionList(Element optionElem) {
    String className = optionElem.asType().toString();
    String tagName = optionElem.getSimpleName().toString();
    OptionList optionList = new OptionList(className, tagName);

    // Get the class.
    Class<?> clazz;
    try {
      clazz = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("OptionList Class: " + className + " is not available. "
          + "Make sure that it is available to the compiler.");
    }

    // Get the getValue method.
    java.lang.reflect.Method toValueMethod;
    try {
      toValueMethod = clazz.getDeclaredMethod("toUnderlyingValue");
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException("Class: " + className + " must have a toUnderlyingValue() "
          + "method.");
    }

    // Get the "fromUnderlyingValue" static method if this class falls under the "com.google.appinventor.components"
    // package. We don't use this method here, but we require the built-in helpers to have it for providing backward
    // compatibility.
    final PackageElement packageElem = processingEnv.getElementUtils().getPackageOf(optionElem);
    if (packageElem.getQualifiedName().toString().startsWith("com.google.appinventor.components.")) {
      java.lang.reflect.Method fromValueMethod;
      Type genericType = null;
      try {
        ParameterizedType optionListType = (ParameterizedType) clazz.getGenericInterfaces()[0];
        genericType = optionListType.getActualTypeArguments()[0];
        Class<?> typeClass = (Class<?>) genericType;
        fromValueMethod = clazz.getDeclaredMethod("fromUnderlyingValue", typeClass);
      } catch (NoSuchMethodException e) {
        throw new IllegalArgumentException("Class: " + className + " must have a static "
                + "fromUnderlyingValue(" + genericType.getTypeName() + ") method.");
      }
      if (!java.lang.reflect.Modifier.isStatic(fromValueMethod.getModifiers())) {
        throw new IllegalArgumentException("Class: " + className + " must have a static "
                + "fromUnderlyingValue(" + genericType.getTypeName() + ") method.");
      }
    }
  
    // Create a map of enum const names -> values. This is used to filter the below elements
    // returned by getEnclosedElements().
    Map<String, String> namesToValues = Maps.newTreeMap();
    Object[] constants = clazz.getEnumConstants();
    if (constants == null) {
      throw new IllegalArgumentException("Class: " + className + " should be an enum and declare "
          + "enum constants.");
    }
    for (Object constant : constants) {
      try {
        Enum<?> enumConst = (Enum<?>) constant;
        namesToValues.put(enumConst.name(), toValueMethod.invoke(enumConst).toString());
      } catch (Exception e) {
        // pass
      }
    }

    // Add the options to the OptionList.
    // Note that getEnclosedElements() returns not only enum constants but also method and field
    // names, so we need to filter those out using namesToValues.
    for (Element field : optionElem.getEnclosedElements()) {
      String fieldName = field.getSimpleName().toString();
      if (namesToValues.containsKey(fieldName)) {
        String value = namesToValues.get(fieldName);
        optionList.addOption(elementToOption(field, value));

        // Set the default to be the first option, or the option tagged with @Default.
        if (optionList.getDefault() == null || isDefault(field)) {
          optionList.setDefault(fieldName);
        }
      }
    }

    DeclaredType optionListInterface = (DeclaredType)((TypeElement)optionElem)
        .getInterfaces().get(0);
    optionList.setUnderlyingType(optionListInterface.getTypeArguments().get(0));

    if (!optionList.isEmpty()) {
      optionLists.put(optionElem.getSimpleName().toString(), optionList);
    }

    return !optionList.isEmpty();
  }

  /**
   * Returns true if the leement is tagged with the @Default annotation.
   * @return true if the element is tagged with the @Default annotation.
   */
  private boolean isDefault(Element field) {
    for (AnnotationMirror mirror : field.getAnnotationMirrors()) {
      if (mirror.getAnnotationType().asElement().getSimpleName().contentEquals("Default")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Converts an Element into an Option.
   * @param field the field to convert into an option.
   * @param value the backing value associated with that field.
   * @return the option constructed from the field and value.
   */
  private Option elementToOption(Element field, String value) {
    // TODO: getDocComment doesn't seem to work on enum constants?
    String description = elementUtils.getDocComment(field);
    if (description == null) {
      description = "";
    }
    // Read only until the first javadoc parameter
    description = description.split("[^\\\\][@{]")[0].trim();

    return new Option(
      field.getSimpleName().toString(),
      value,
      description,
      elementUtils.isDeprecated(field)
    );
  }

  /**
   * Returns the associated helper key if the element has an @Asset annotation. Null otherwise.
   *
   * @param elem the Element which represents a function (for return types) or a parameter.
   * @param type the TypeMirror representing the type of that element.
   * @return the associated helper key if the element has an @Asset annotation.
   */
  private HelperKey hasAssetsHelper(Element elem, TypeMirror type) {
    for (AnnotationMirror mirror : elem.getAnnotationMirrors()) {
      if (mirror.getAnnotationType().asElement().getSimpleName().contentEquals("Asset")) {
        int index = 0;  // Index 0 is the empty filter.
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
            mirror.getElementValues().entrySet()) {
          // Make sure we are looking at the value attribute.
          if (!entry.getKey().getSimpleName().contentEquals("value")) {
            continue;
          }
          List<AnnotationValue> values = (List<AnnotationValue>) entry.getValue().getValue();
          List<String> filter = new ArrayList<String>();
          for (AnnotationValue v : values) {
            filter.add(((String)v.getValue()).toLowerCase());
          }
          Collections.sort(filter);
          if (!filters.contains(filter)) {
            filters.add(filter);
          }
          index = filters.indexOf(filter);
        }
        return new HelperKey(HelperType.ASSET, index);
      }
    }
    return null;
  }

  /**
   * Returns the associated helper key if the element has an @ProviderModel annotation. Null otherwise.
   *
   * @param elem the Element which represents a function (for return types) or a parameter.
   * @param type the TypeMirror representing the type of that element.
   * @return the associated helper key if the element has an @ProviderModel annotation.
   */
  private HelperKey hasProviderModelHelper(Element elem, TypeMirror type) {
    for (AnnotationMirror mirror : elem.getAnnotationMirrors()) {
      if (mirror.getAnnotationType().asElement().getSimpleName().contentEquals("ProviderModel")) {
        int index = 0;  // Index 0 is the empty filter.
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
            mirror.getElementValues().entrySet()) {
          // Make sure we are looking at the value attribute.
          if (!entry.getKey().getSimpleName().contentEquals("value")) {
            continue;
          }
          List<AnnotationValue> values = (List<AnnotationValue>) entry.getValue().getValue();
          List<String> filter = new ArrayList<String>();
          for (AnnotationValue v : values) {
            filter.add(((String)v.getValue()).toLowerCase());
          }
          Collections.sort(filter);
          if (!filters.contains(filter)) {
            filters.add(filter);
          }
          index = filters.indexOf(filter);
        }
        return new HelperKey(HelperType.PROVIDER_MODEL, index);
      }
    }
    return null;
  }

  /**
   * Returns the associated helper key if the element has an @Provider annotation. Null otherwise.
   *
   * @param elem the Element which represents a function (for return types) or a parameter.
   * @param type the TypeMirror representing the type of that element.
   * @return the associated helper key if the element has an @Provider annotation.
   */
  private HelperKey hasProviderHelper(Element elem, TypeMirror type) {
    for (AnnotationMirror mirror : elem.getAnnotationMirrors()) {
      if (mirror.getAnnotationType().asElement().getSimpleName().contentEquals("Provider")) {
        int index = 0;  // Index 0 is the empty filter.
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
            mirror.getElementValues().entrySet()) {
          // Make sure we are looking at the value attribute.
          if (!entry.getKey().getSimpleName().contentEquals("value")) {
            continue;
          }
          List<AnnotationValue> values = (List<AnnotationValue>) entry.getValue().getValue();
          List<String> filter = new ArrayList<String>();
          for (AnnotationValue v : values) {
            filter.add(((String)v.getValue()).toLowerCase());
          }
          Collections.sort(filter);
          if (!filters.contains(filter)) {
            filters.add(filter);
          }
          index = filters.indexOf(filter);
        }
        return new HelperKey(HelperType.PROVIDER, index);
      }
    }
    return null;
  }

  /**
   * Converts a VariableElement into a Parameter definition.
   * @param varElem the element to convert.
   * @return the parameter constructed from the variable element.
   */
  private Parameter varElemToParameter(VariableElement varElem) {
    TypeMirror type = varElem.asType();
    if (type instanceof DeclaredType && ((DeclaredType) type).asElement().toString()
        .equals("com.google.appinventor.components.runtime.util.Continuation")) {
      Continuation continuation = new Continuation(varElem.getSimpleName().toString(), type,
          varElem.getAnnotation(IsColor.class) != null);
      continuation.helper = elementToHelperKey(varElem, varElem.asType());
      return continuation;
    } else {
      Parameter param = new Parameter(varElem.getSimpleName().toString(), type,
          varElem.getAnnotation(IsColor.class) != null);
      param.helper = elementToHelperKey(varElem, varElem.asType());
      return param;
    }
  }

  // Transform an @ActivityElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String activityElementToString(ActivityElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <activity> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("    <activity ");
    elementString.append(elementAttributesToString(element));
    elementString.append(">\n");

    // Now, we collect any <activity> subelements.
    elementString.append(subelementsToString(element.metaDataElements()));
    elementString.append(subelementsToString(element.intentFilters()));

    // Finally, we close the <activity> element and create its String.
    return elementString.append("    </activity>\n").toString();
  }

  // Transform a @ReceiverElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String receiverElementToString(ReceiverElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <receiver> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("    <receiver ");
    elementString.append(elementAttributesToString(element));
    elementString.append(">\n");

    // Now, we collect any <receiver> subelements.
    elementString.append(subelementsToString(element.metaDataElements()));
    elementString.append(subelementsToString(element.intentFilters()));

    // Finally, we close the <receiver> element and create its String.
    return elementString.append("    </receiver>\n").toString();
  }

  // Transform a @ServiceElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String serviceElementToString(ServiceElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <service> element's opening tag including any
    // service element attributes.
    StringBuilder elementString = new StringBuilder("    <service ");
    elementString.append(elementAttributesToString(element));
    elementString.append(">\n");

    // Now, we collect any <service> subelements.
    elementString.append(subelementsToString(element.metaDataElements()));
    elementString.append(subelementsToString(element.intentFilters()));

    // Finally, we close the <service> element and create its String.
    return elementString.append("    </service>\n").toString();
  }

  // Transform a @XmlElement into an String for use later
  // in creating xml files.
  private static String xmlElementToString(XmlElement element) {
    // create string: "dir/name:<?xml version=\"1.0\" encoding=\"utf-8\"?>\n + content
    StringBuilder elementString = new StringBuilder(element.dir());
    elementString.append("/");
    elementString.append(element.name());
    elementString.append(":");

    return elementString.append(element.content()).toString();
  }

  // Transform a @ProviderElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String providerElementToString(ProviderElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <provider> element's opening tag including any
    // content provider element attributes.
    StringBuilder elementString = new StringBuilder("    <provider ");
    elementString.append(elementAttributesToString(element));
    elementString.append(">\n");

    // Now, we collect any <provider> subelements.
    elementString.append(subelementsToString(element.metaDataElements()));
    elementString.append(subelementsToString(element.pathPermissionElement()));
    elementString.append(subelementsToString(element.grantUriPermissionElement()));

    // Finally, we close the <provider> element and create its String.
    return elementString.append("    </provider>\n").toString();
  }

  // Transform a @MetaDataElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String metaDataElementToString(MetaDataElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <meta-data> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("      <meta-data ");
    elementString.append(elementAttributesToString(element));
    // Finally, we close the <meta-data> element and create its String.
    return elementString.append("/>\n").toString();
  }

  // Transform an @IntentFilterElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String intentFilterElementToString(IntentFilterElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <intent-filter> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("      <intent-filter ");
    elementString.append(elementAttributesToString(element));
    elementString.append(">\n");

    // Now, we collect any <intent-filter> subelements.
    elementString.append(subelementsToString(element.actionElements()));
    elementString.append(subelementsToString(element.categoryElements()));
    elementString.append(subelementsToString(element.dataElements()));

    // Finally, we close the <intent-filter> element and create its String.
    return elementString.append("    </intent-filter>\n").toString();
  }

  private static String intentFilterElementToIntentString(IntentFilterElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <intent-filter> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("      <intent>\n");

    // Now, we collect any <intent-filter> subelements.
    elementString.append(subelementsToString(element.actionElements()));
    elementString.append(subelementsToString(element.categoryElements()));
    elementString.append(subelementsToString(element.dataElements()));

    // Finally, we close the <intent-filter> element and create its String.
    return elementString.append("    </intent>\n").toString();
  }

  // Transform an @ActionElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String actionElementToString(ActionElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <action> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("        <action ");
    elementString.append(elementAttributesToString(element));
    // Finally, we close the <action> element and create its String.
    return elementString.append("/>\n").toString();
  }

  // Transform a @CategoryElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String categoryElementToString(CategoryElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <category> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("        <category ");
    elementString.append(elementAttributesToString(element));
    // Finally, we close the <category> element and create its String.
    return elementString.append("/>\n").toString();
  }

  // Transform a @DataElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String dataElementToString(DataElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <data> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("        <data ");
    elementString.append(elementAttributesToString(element));
    // Finally, we close the <data> element and create its String.
    return elementString.append("/>\n").toString();
  }

  // Transform a @PathPermissionElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String pathPermissionElementToString(PathPermissionElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <path-permission> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("        <path-permission ");
    elementString.append(elementAttributesToString(element));
    // Finally, we close the <path-permission> element and create its String.
    return elementString.append("/>\n").toString();
  }

  // Transform a @GrantUriPermissionElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String grantUriPermissionElementToString(GrantUriPermissionElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <grant-uri-permission> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("        <grant-uri-permission ");
    elementString.append(elementAttributesToString(element));
    // Finally, we close the <grant-uri-permission> element and create its String.
    return elementString.append("/>\n").toString();
  }

  // Build the attribute String for a given XML element modeled by an
  // annotation.
  //
  // Note that we use the fully qualified names for certain classes in the
  // "java.lang.reflect" package to avoid namespace collisions.
  private static String elementAttributesToString(Annotation element)
      throws IllegalAccessException, InvocationTargetException {
    StringBuilder attributeString = new StringBuilder("");
    Class<? extends Annotation> clazz = element.annotationType();
    java.lang.reflect.Method[] methods = clazz.getDeclaredMethods();
    String attributeSeparator = "";
    for (java.lang.reflect.Method method : methods) {
      int modCode = method.getModifiers();
      if (java.lang.reflect.Modifier.isPublic(modCode)
          && !java.lang.reflect.Modifier.isStatic(modCode)) {
        if (method.getReturnType().getSimpleName().equals("String")) {
          // It is an XML element attribute.
          String attributeValue = (String) method.invoke(clazz.cast(element));
          if (!attributeValue.equals("")) {
            attributeString.append(attributeSeparator);
            attributeString.append("android:");
            attributeString.append(method.getName());
            attributeString.append("=\"");
            attributeString.append(attributeValue);
            attributeString.append("\"");
            attributeSeparator = " ";
          }
        }
      }
    }
    return attributeString.toString();
  }

  // Build the subelement String for a given array of XML elements modeled by
  // corresponding annotations.
  private static String subelementsToString(Annotation[] subelements)
      throws IllegalAccessException, InvocationTargetException {
    StringBuilder subelementString = new StringBuilder("");
    for (Annotation subelement : subelements) {
      if (subelement instanceof MetaDataElement) {
        subelementString.append(metaDataElementToString((MetaDataElement) subelement));
      } else if (subelement instanceof IntentFilterElement) {
        subelementString.append(intentFilterElementToString((IntentFilterElement) subelement));
      } else if (subelement instanceof ActionElement) {
        subelementString.append(actionElementToString((ActionElement) subelement));
      } else if (subelement instanceof CategoryElement) {
        subelementString.append(categoryElementToString((CategoryElement) subelement));
      } else if (subelement instanceof DataElement) {
        subelementString.append(dataElementToString((DataElement) subelement));
      } else if (subelement instanceof PathPermissionElement) {
        subelementString.append(pathPermissionElementToString((PathPermissionElement) subelement));
      } else if (subelement instanceof GrantUriPermissionElement) {
        subelementString.append(grantUriPermissionElementToString((GrantUriPermissionElement) subelement));
      }
    }
    return subelementString.toString();
  }

  private void processProperties(ComponentInfo componentInfo,
                                 Element componentElement) {
    // We no longer support properties that use the variant type.

    Map<String, Element> propertyElementsToCheck = new HashMap<>();

    for (Element element : componentElement.getEnclosedElements()) {
      if (!isPublicMethod(element)) {
        continue;
      }

      // Get the name of the prospective property.
      String propertyName = element.getSimpleName().toString();
      processConditionalAnnotations(componentInfo, element, propertyName);

      // Designer property information
      DesignerProperty designerProperty = element.getAnnotation(DesignerProperty.class);
      if (designerProperty != null) {
        componentInfo.designerProperties.put(propertyName, designerProperty);
        propertyElementsToCheck.put(propertyName, element);
      }

      // If property is overridden without again using SimpleProperty, remove
      // it.  For example, this is done for Ball.Width(), which overrides the
      // inherited property Width() because Ball uses Radius() instead.
      if (element.getAnnotation(SimpleProperty.class) == null) {
        if (componentInfo.properties.containsKey(propertyName)) {
          // Look at the prior property's componentInfoName.
          Property priorProperty = componentInfo.properties.get(propertyName);
          if (priorProperty.componentInfoName.equals(componentInfo.name)) {
            // The prior property's componentInfoName is the same as this componentInfo's name.
            // This is just a read-only or write-only property. We don't need to do anything
            // special here.
          } else {
            // The prior property's componentInfoName is the different than this componentInfo's
            // name. This is an overridden property without the SimpleProperty annotation and we
            // need to remove it.
            componentInfo.properties.remove(propertyName);
            if (designerProperty == null) {
              componentInfo.designerProperties.remove(propertyName);
            }
          }
        }
      } else {
        // Create a new Property element, then compare and combine it with any
        // prior Property element with the same property name, verifying that
        // they are consistent.
        Property newProperty = executableElementToProperty(element, componentInfo.name);
        if (designerProperty != null
            && designerProperty.editorType().equals(PropertyTypeConstants.PROPERTY_TYPE_COLOR)) {
          // Properties that use a color editor should be marked as a color property
          newProperty.color = true;
        }

        if (componentInfo.properties.containsKey(propertyName)) {
          Property priorProperty = componentInfo.properties.get(propertyName);

          if (!priorProperty.type.equals(newProperty.type)) {
            // If the getter type is different than the setter type, set the type to getter.
            // If we have multiple setters with different types, throw an error.
            if (newProperty.readable) {
              priorProperty.type = newProperty.type;
            } else if (priorProperty.writable) {
              // TODO(user): handle lang_def and document generation for multiple setters.
              throw new RuntimeException("Inconsistent types " + priorProperty.type +
                                         " and " + newProperty.type + " for property " +
                                         propertyName + " in component " + componentInfo.name);
            }
          }

          // TODO: Should this be moved into the Property class? This was tricky for me to discover.
          // Merge newProperty into priorProperty, which is already in the properties map.
          if ((priorProperty.description.isEmpty() || priorProperty.isDefaultDescription()
               || element.getAnnotation(Override.class) != null)
              && !newProperty.description.isEmpty() && !newProperty.isDefaultDescription()) {
            priorProperty.setDescription(newProperty.description);
          }
          if (!newProperty.longDescription.isEmpty() && !newProperty.isDefaultDescription()) {  /* Latter descriptions of the same property override earlier descriptions. */
            priorProperty.longDescription = newProperty.longDescription;
          }

          if (priorProperty.propertyCategory == PropertyCategory.UNSET) {
            priorProperty.propertyCategory = newProperty.propertyCategory;
          } else if (newProperty.propertyCategory != priorProperty.propertyCategory &&
                     newProperty.propertyCategory != PropertyCategory.UNSET) {
            throw new RuntimeException(
                "Property " + propertyName + " has inconsistent categories " +
                priorProperty.propertyCategory + " and " +
                newProperty.propertyCategory + " in component " +
                componentInfo.name);
          }
          if (priorProperty.helper == null) {
            priorProperty.helper = newProperty.helper;
          }
          priorProperty.readable = priorProperty.readable || newProperty.readable;
          priorProperty.writable = priorProperty.writable || newProperty.writable;
          priorProperty.userVisible = priorProperty.isUserVisible() && newProperty.isUserVisible();
          priorProperty.deprecated = priorProperty.isDeprecated() && newProperty.isDeprecated();
          priorProperty.componentInfoName = componentInfo.name;
          priorProperty.color = newProperty.color || priorProperty.color;
        } else {
          // Add the new property to the properties map.
          componentInfo.properties.put(propertyName, newProperty);
        }
      }
    }

    // Verify that every DesignerComponent has a corresponding property entry. A mismatch results
    // in App Inventor being unable to generate code for the designer since the type information
    // is in the block property only. We check that the designer property name is also present
    // in the block properties. If not, an error is reported and the build terminates.
    Set<String> propertyNames = new HashSet<>(componentInfo.designerProperties.keySet());
    propertyNames.removeAll(componentInfo.properties.keySet());
    if (!propertyNames.isEmpty()) {
      for (String propertyName : propertyNames) {
        messager.printMessage(Kind.ERROR,
            String.format(MISSING_SIMPLE_PROPERTY_ANNOTATION, propertyName),
            propertyElementsToCheck.get(propertyName));
      }
    }
  }

  // Note: The top halves of the bodies of processEvent() and processMethods()
  // are very similar.  I tried refactoring in several ways but it just made
  // things more complex.
  private void processEvents(ComponentInfo componentInfo,
                             Element componentElement) {
    for (Element element : componentElement.getEnclosedElements()) {
      if (!isPublicMethod(element)) {
        continue;
      }

      // Get the name of the prospective event.
      String eventName = element.getSimpleName().toString();
      processConditionalAnnotations(componentInfo, element, eventName);

      SimpleEvent simpleEventAnnotation = element.getAnnotation(SimpleEvent.class);

      // Remove overriden events unless SimpleEvent is again specified.
      // See comment in processProperties for an example.
      if (simpleEventAnnotation == null) {
        if (componentInfo.events.containsKey(eventName)) {
          componentInfo.events.remove(eventName);
        }
      } else {
        String eventDescription = simpleEventAnnotation.description();
        String longEventDescription = elementUtils.getDocComment(element);
        if (eventDescription.isEmpty()) {
          eventDescription = longEventDescription;
          if (eventDescription == null) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                                  "In component " + componentInfo.name +
                                  ", event " + eventName +
                                  " is missing a description.");
            eventDescription = "";
          }
        }
        boolean userVisible = simpleEventAnnotation.userVisible();
        boolean deprecated = elementUtils.isDeprecated(element);
        Event event = new Event(eventName, eventDescription, longEventDescription, userVisible, deprecated);
        componentInfo.events.put(event.name, event);

        // Verify that this element has an ExecutableType.
        if (!(element instanceof ExecutableElement)) {
          throw new RuntimeException("In component " + componentInfo.name +
                                     ", the representation of SimpleEvent " + eventName +
                                     " does not implement ExecutableElement.");
        }
        ExecutableElement e = (ExecutableElement) element;

        // Extract the parameters.
        for (VariableElement ve : e.getParameters()) {
          event.addParameter(varElemToParameter(ve));
          updateComponentTypes(ve.asType());
        }
      }
    }
  }

  private void processMethods(ComponentInfo componentInfo, Element componentElement) {
    for (Element element : componentElement.getEnclosedElements()) {
      if (!isPublicMethod(element)) {
        continue;
      }

      // Get the name of the prospective method.
      String methodName = element.getSimpleName().toString();
      processConditionalAnnotations(componentInfo, element, methodName);

      SimpleFunction simpleFunctionAnnotation = element.getAnnotation(SimpleFunction.class);

      // Remove overriden methods unless SimpleFunction is again specified.
      // See comment in processProperties for an example.
      if (simpleFunctionAnnotation == null) {
        if (componentInfo.methods.containsKey(methodName)) {
          componentInfo.methods.remove(methodName);
        }
      } else {
        String methodLongDescription = elementUtils.getDocComment(element);
        String methodDescription = simpleFunctionAnnotation.description();
        if (methodDescription.isEmpty()) {
          methodDescription = methodLongDescription;
          if (methodDescription == null) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                                  "In component " + componentInfo.name +
                                  ", method " + methodName +
                                  " is missing a description.");
            methodDescription = "";
          }
        }
        boolean userVisible = simpleFunctionAnnotation.userVisible();
        boolean deprecated = elementUtils.isDeprecated(element);
        Method method = new Method(methodName, methodDescription, methodLongDescription, userVisible, deprecated);
        componentInfo.methods.put(method.name, method);

        // Verify that this element has an ExecutableType.
        if (!(element instanceof ExecutableElement)) {
          throw new RuntimeException("In component " + componentInfo.name +
                                     ", the representation of SimpleFunction " + methodName +
                                     " does not implement ExecutableElement.");
        }
        ExecutableElement e = (ExecutableElement) element;

        // Extract the parameters.
        Continuation continuation = null;
        for (VariableElement ve : e.getParameters()) {
          Parameter p = varElemToParameter(ve);
          if (p instanceof Continuation) {
            if (continuation != null) {
              messager.printMessage(Kind.ERROR, "A method can have at most one continuation",
                  element);
            } else {
              continuation = (Continuation) p;
            }
          } else {
            method.addParameter(p);
          }
          updateComponentTypes(ve.asType());
        }

        // Extract the return type.
        if (e.getReturnType().getKind() != TypeKind.VOID) {
          if (continuation != null) {
            messager.printMessage(Kind.ERROR, "Methods with a continuation must be void.",
                element);
          }
          method.returnType = e.getReturnType();
          method.returnHelperKey = elementToHelperKey(e, method.returnType);
          if (e.getAnnotation(IsColor.class) != null) {
            method.color = true;
          }
          updateComponentTypes(e.getReturnType());
        } else if (continuation != null) {
          method.continuation = true;
          if (!((DeclaredType) continuation.underlyingType).toString().equals("java.lang.Void")) {
            method.returnType = continuation.underlyingType;
          }
        }
      }
    }
  }

  /**
   * Processes the conditional annotations for a component into a dictionary
   * mapping blocks to those annotations.
   *
   * @param componentInfo Component info in which to store the conditional information.
   * @param element The currently processed Java language element. This should be a method
   *                annotated with either @UsesPermission, @UsesBroadcastReceivers, @UsesServices or @UsesContentProviders
   * @param blockName The name of the block as it appears in the sources.
   */
  private void processConditionalAnnotations(ComponentInfo componentInfo, Element element,
                                             String blockName) {
    // Conditional UsesPermissions
    UsesPermissions usesPermissions = element.getAnnotation(UsesPermissions.class);
    if (usesPermissions != null) {
      Set<String> permissions = new TreeSet<>();
      Collections.addAll(permissions, usesPermissions.value());
      for (PermissionConstraint constraint : usesPermissions.constraints()) {
        permissions.add(constraint.name());
        if (componentInfo.conditionalPermissionConstraints.containsKey(constraint.name())) {
          componentInfo.conditionalPermissionConstraints.get(blockName)
              .put(constraint.name(), constraint);
        } else {
          Map<String, PermissionConstraint> constraints = new TreeMap<>();
          constraints.put(constraint.name(), constraint);
          componentInfo.conditionalPermissionConstraints.put(blockName, constraints);
        }
      }
      componentInfo.conditionalPermissions.put(blockName, permissions.toArray(new String[0]));
    }

    UsesBroadcastReceivers broadcastReceiver = element.getAnnotation(UsesBroadcastReceivers.class);
    if (broadcastReceiver != null) {
      try {
        Set<String> receivers = new HashSet<>();
        for (ReceiverElement re : broadcastReceiver.receivers()) {
          updateWithNonEmptyValue(receivers, receiverElementToString(re));
        }
        componentInfo.conditionalBroadcastReceivers.put(blockName, receivers.toArray(new String[0]));
      } catch (Exception e) {
        messager.printMessage(Kind.ERROR, "Unable to process broadcast receiver", element);
      }
    }

    // Gather the required queries and build their element strings.
    UsesQueries usesQueries = element.getAnnotation(UsesQueries.class);
    if (usesQueries != null) {
      try {
        Set<String> queries = new HashSet<>();
        for (String packageName : usesQueries.packageNames()) {
          updateWithNonEmptyValue(queries, "<package android:name=\"" + packageName + "\" />");
        }
        for (IntentFilterElement intent : usesQueries.intents()) {
          updateWithNonEmptyValue(queries, intentFilterElementToIntentString(intent));
        }
        for (ProviderElement provider : usesQueries.providers()) {
          updateWithNonEmptyValue(queries, providerElementToString(provider));
        }
        componentInfo.conditionalQueries.put(blockName, queries.toArray(new String[0]));
      } catch (IllegalAccessException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "IllegalAccessException when gathering "
            + "service attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        messager.printMessage(Diagnostic.Kind.ERROR, "InvocationTargetException when gathering "
            + "service attributes and subelements for component " + componentInfo.name);
        throw new RuntimeException(e);
      }
    }

    UsesServices service = element.getAnnotation(UsesServices.class);
    if (service != null) {
      try {
        Set<String> services = new HashSet<>();
        for (ServiceElement se : service.services()) {
          updateWithNonEmptyValue(services, serviceElementToString(se));
        }
        componentInfo.conditionalServices.put(blockName, services.toArray(new String[0]));
      } catch (Exception e) {
        messager.printMessage(Kind.ERROR, "Unable to process service", element);
      }
    }

    UsesContentProviders contentProvider = element.getAnnotation(UsesContentProviders.class);
    if (contentProvider != null) {
      try {
        Set<String> providers = new HashSet<>();
        for (ProviderElement pe : contentProvider.providers()) {
          updateWithNonEmptyValue(providers, providerElementToString(pe));
        }
        componentInfo.conditionalContentProviders.put(blockName, providers.toArray(new String[0]));
      } catch (Exception e) {
        messager.printMessage(Kind.ERROR, "Unable to process content provider", element);
      }
    }
  }

  /**
   * <p>Outputs the required component information in the desired format. It is called by
   * {@link #process} after the fields {@link #components} and {@link #messager}
   * have been populated.</p>
   *
   * <p>Implementations of this methods should call {@link #getOutputWriter(String)} to obtain a
   * {@link Writer} for their output.  Diagnostic messages should be written
   * using {@link #messager}.</p>
   */
  protected abstract void outputResults() throws IOException;

  /**
   * Returns the appropriate Yail type for a given Java type.
   *
   * @param type a TypeMirror representing the Java type.
   * @return the equivalent Yail type. All component names are converted to "component".
   * @throws RuntimeException if the parameter cannot be mapped to any of the
   *         legal return values
   */
  protected final String javaTypeToYailType(TypeMirror type) {
    return javaTypeToYailType(type, false);
  }

  protected final String javaTypeToYailType(TypeMirror type, boolean allowBoxed) {
    if (!allowBoxed && BOXED_TYPES.containsKey(type.toString())) {
      throw new IllegalArgumentException(String.format(BOXED_TYPE_ERROR, type,
          BOXED_TYPES.get(type.toString())));
    } else if (allowBoxed && BOXED_TYPES.containsKey(type.toString())) {
      return BOXED_TYPES.get(type.toString());
    }

    // Handle enums
    if (isOptionList(type)) {
      // In YAIL code generation we need any easy way to test if a type symbol, represents an
      // abstract option type. We have chosen to do this by having each type end with "Enum". For
      // example, if you have a parameter that accepts a Direction the type symbol passed to Yail
      // would be 'com.google.appinventor.components.common.DirectionEnum.
      return type.toString() + "Enum";
    }

    String typeString = type.toString();
    if (!type.getAnnotationMirrors().isEmpty()) {
      // Java 11 now includes parameter level annotations in the type string
      if (type instanceof DeclaredType) {
        typeString = ((TypeElement) ((DeclaredType) type).asElement()).getQualifiedName().toString();
      } else if (type instanceof PrimitiveType) {
        typeString = type.getKind().toString().toLowerCase();
      }
    }
    // boolean -> boolean
    if (typeString.equals("boolean")) {
      return typeString;
    }
    // String -> text
    if (typeString.equals("java.lang.String")) {
      return "text";
    }
    // {float, double, int, short, long, byte} -> number
    if (typeString.equals("float") || typeString.equals("double") || typeString.equals("int")
        || typeString.equals("short") || typeString.equals("long") || typeString.equals("byte")) {
      return "number";
    }
    // YailList -> list
    if (typeString.equals("com.google.appinventor.components.runtime.util.YailList")) {
      return "list";
    }
    // List<?> -> list
    if (typeString.startsWith("java.util.List")) {
      return "list";
    }
    if (typeString.equals("com.google.appinventor.components.runtime.util.YailDictionary")) {
      return "dictionary";
    }
    if (typeString.equals("com.google.appinventor.components.runtime.util.YailObject")) {
      return "yailobject";
    }

    // Calendar -> InstantInTime
    if (typeString.equals("java.util.Calendar")) {
      return "InstantInTime";
    }

    // Only components can be data sources in the block language
    if (typeString.startsWith("com.google.appinventor.components.runtime.DataSource")) {
      return "component";
    }

    if (typeString.equals("java.lang.Object")) {
      return "any";
    }

    if (typeString.startsWith("com.google.appinventor.components.runtime.util.Continuation")) {
      return "continuation";
    }

    if (typeString.equals("com.google.appinventor.components.runtime.Component")) {
      return "component";
    }

    // Check if it's a component.
    if (componentTypes.contains(typeString)) {
      return "component";
    }

    throw new IllegalArgumentException("Cannot convert Java type '" + type.toString()
        + "' to Yail type");
  }

  /**
   * Creates and returns a {@link FileObject} for output.
   *
   * @param fileName the name of the output file
   * @return the {@code FileObject}
   * @throws IOException if the file cannot be created
   */
  protected FileObject createOutputFileObject(String fileName) throws IOException {
    return processingEnv.getFiler().
      createResource(StandardLocation.SOURCE_OUTPUT, OUTPUT_PACKAGE, fileName);
  }

  /**
   * Returns a {@link Writer} to which output should be written.  As with any
   * {@code Writer}, the methods {@link Writer#flush()} and {@link Writer#close()}
   * should be called when output is complete.
   *
   * @param fileName the name of the output file
   * @return the {@code Writer}
   * @throws IOException if the {@code Writer} or underlying {@link FileObject}
   *         cannot be created
   */
  protected Writer getOutputWriter(String fileName) throws IOException {
    return createOutputFileObject(fileName).openWriter();
  }

  /**
   * Tracks the superclass and superinterfaces for the given type and if the type inherits from
   * {@link com.google.appinventor.components.runtime.Component} then it adds the class to the
   * componentTypes list. This allows properties, methods, and events to use concrete Component
   * types as parameters and return values.
   *
   * @param type a TypeMirror representing a type on the class path
   */
  private void updateComponentTypes(TypeMirror type) {
    if (type.getKind() == TypeKind.DECLARED) {
      type.accept(new SimpleTypeVisitor7<Boolean, Set<String>>(false) {
        @Override
        public Boolean visitDeclared(DeclaredType t, Set<String> types) {
          final String typeName = t.asElement().toString();
          if ("com.google.appinventor.components.runtime.Component".equals(typeName)) {
            return true;
          }
          if (!types.contains(typeName)) {
            types.add(typeName);
            final TypeElement typeElement = (TypeElement) t.asElement();
            if (typeElement.getSuperclass().accept(this, types)) {
              componentTypes.add(typeName);
              return true;
            }
            for (TypeMirror iface : typeElement.getInterfaces()) {
              if (iface.accept(this, types)) {
                componentTypes.add(typeName);
                return true;
              }
            }
          }
          return componentTypes.contains(typeName);
        }
      }, visitedTypes);
    }
  }

  private void updateWithNonEmptyValue(Set<String> collection, String value) {
    String trimmedValue = value.trim();
    if (!trimmedValue.isEmpty()) {
      collection.add(trimmedValue);
    }
  }
}
