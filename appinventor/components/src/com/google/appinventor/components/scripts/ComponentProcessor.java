// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.scripts;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.SimpleBroadcastReceiver;
import com.google.appinventor.components.annotations.UsesAssets;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesNativeLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.annotations.UsesActivities;
import com.google.appinventor.components.annotations.UsesBroadcastReceivers;
import com.google.appinventor.components.annotations.androidmanifest.ActivityElement;
import com.google.appinventor.components.annotations.androidmanifest.ReceiverElement;
import com.google.appinventor.components.annotations.androidmanifest.IntentFilterElement;
import com.google.appinventor.components.annotations.androidmanifest.MetaDataElement;
import com.google.appinventor.components.annotations.androidmanifest.ActionElement;
import com.google.appinventor.components.annotations.androidmanifest.DataElement;
import com.google.appinventor.components.annotations.androidmanifest.CategoryElement;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.Writer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor7;
import javax.lang.model.util.Types;

import java.lang.annotation.Annotation;

import java.lang.reflect.InvocationTargetException;

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

  public static final String MISSING_SIMPLE_PROPERTY_ANNOTATION =
      "Designer property %s does not have a corresponding @SimpleProperty annotation.";

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
      "com.google.appinventor.components.annotations.UsesPermissions");

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

  private final List<String> componentTypes = Lists.newArrayList();

  /**
   * A set of visited types in the class hierarchy. This is used to reduce the complexity of
   * detecting whether a class implements {@link com.google.appinventor.components.runtime.Component}
   * from O(n^2) to O(n) by tracking visited nodes to prevent repeat explorations of the class tree.
   */
  private final Set<String> visitedTypes = new HashSet<>();

  /**
   * Represents a parameter consisting of a name and a type.  The type is a
   * String representation of the java type, such as "int", "double", or
   * "java.lang.String".
   */
  protected final class Parameter {
    /**
     * The parameter name
     */
    protected final String name;

    /**
     * The parameter's Java type, such as "int" or "java.lang.String".
     */
    protected final String type;

    /**
     * Constructs a Parameter.
     *
     * @param name the parameter name
     * @param type the parameter's Java type (such as "int" or "java.lang.String")
     */
    protected Parameter(String name, String type) {
      this.name = name;
      this.type = type;
    }

    /**
     * Provides a Yail type for a given parameter type.  This is useful because
     * the parameter types used for {@link Event} are Simple types (e.g.,
     * "Single"), while the parameter types used for {@link Method} are
     * Java types (e.g., "int".
     *
     * @param parameter a parameter
     * @return the string representation of the corresponding Yail type
     * @throws RuntimeException if {@code parameter} does not have a
     *         corresponding Yail type
     */
    protected String parameterToYailType(Parameter parameter) {
      return javaTypeToYailType(type);
    }
  }

  /**
   * Represents a component feature that has a name and a description.
   */
  protected abstract static class Feature {
    protected final String name;
    protected String description;

    protected Feature(String name, String description, String featureType) {
      this.name = name;
      if (description == null || description.isEmpty()) {
        this.description = featureType + " for " + name;
      } else {
        // Throw out the first @ or { and everything after it,
        // in order to strip out @param, @author, {@link ...}, etc.
        this.description = description.split("[@{]")[0].trim();
      }
    }
  }

  /**
   * Represents a component feature that has a name, description, and
   * parameters.
   */
  protected abstract class ParameterizedFeature extends Feature {
    // Inherits name, description
    protected final List<Parameter> parameters;
    protected final boolean userVisible;
    protected final boolean deprecated; // [lyn, 2015/12/29] added

    protected ParameterizedFeature(String name, String description, String feature,
        boolean userVisible, boolean deprecated) {
      super(name, description, feature);
      this.userVisible = userVisible;
      this.deprecated = deprecated;
      parameters = Lists.newArrayList();
    }

    protected void addParameter(String name, String type) {
      parameters.add(new Parameter(name, type));
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
        sb.append(param.parameterToYailType(param));
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

    protected Event(String name, String description, boolean userVisible, boolean deprecated) {
      super(name, description, "Event", userVisible, deprecated);
    }

    @Override
    public Event clone() {
      Event that = new Event(name, description, userVisible, deprecated);
      for (Parameter p : parameters) {
        that.addParameter(p.name, p.type);
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
    // Inherits name, description, and parameters
    private String returnType;

    protected Method(String name, String description, boolean userVisible, boolean deprecated) {
      super(name, description, "Method", userVisible, deprecated);
      // returnType defaults to null
    }

    protected String getReturnType() {
      return returnType;
    }

    @Override
    public Method clone() {
      Method that = new Method(name, description, userVisible, deprecated);
      for (Parameter p : parameters) {
        that.addParameter(p.name, p.type);
      }
      that.returnType = returnType;
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
  protected static final class Property implements Cloneable {
    protected final String name;
    private String description;
    private PropertyCategory propertyCategory;
    private boolean userVisible;
    private boolean deprecated;
    private String type;
    private boolean readable;
    private boolean writable;
    private String componentInfoName;

    protected Property(String name, String description,
                       PropertyCategory category, boolean userVisible, boolean deprecated) {
      this.name = name;
      this.description = description;
      this.propertyCategory = category;
      this.userVisible = userVisible;
      this.deprecated = deprecated;
      // type defaults to null
      // readable and writable default to false
    }

    @Override
    public Property clone() {
      Property that = new Property(name, description, propertyCategory, userVisible, deprecated);
      that.type = type;
      that.readable = readable;
      that.writable = writable;
      that.componentInfoName = componentInfoName;
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

    /**
     * Returns this property's Java type (e.g., "int", "double", or "java.lang.String").
     *
     * @return the feature's Java type
     */
    protected String getType() {
      return type;
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
     * Mapping of component block names to permissions that should be included
     * if the block is used.
     */
    protected final Map<String, String[]> conditionalPermissions;

    /**
     * Mapping of component block names to broadcast receivers that should be
     * included if the block is used.
     */
    protected final Map<String, String[]> conditionalBroadcastReceivers;

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
     * Broadcast receivers required by this component.
     */
    protected final Set<String> broadcastReceivers;
  
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

    protected ComponentInfo(Element element) {
      super(element.getSimpleName().toString(),  // Short name
            elementUtils.getDocComment(element),
            "Component");
      type = element.asType().toString();
      displayName = getDisplayNameForComponentType(name);
      permissions = Sets.newHashSet();
      conditionalPermissions = Maps.newTreeMap();
      conditionalBroadcastReceivers = Maps.newTreeMap();
      libraries = Sets.newHashSet();
      nativeLibraries = Sets.newHashSet();
      assets = Sets.newHashSet();
      activities = Sets.newHashSet();
      broadcastReceivers = Sets.newHashSet();
      classNameAndActionsBR = Sets.newHashSet();
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
            description = explicitDescription;
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
          androidMinSdk = designerComponentAnnotation.androidMinSdk();
          versionName = designerComponentAnnotation.versionName();
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

    private String getDisplayNameForComponentType(String componentTypeName) {
      // Users don't know what a 'Form' is.  They know it as a 'Screen'.
      return "Form".equals(componentTypeName) ? "Screen" : componentTypeName;
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

    // Indicate that we have successfully handled the annotations.
    return true;
  }

    /*
     * This processes an element if it represents a component, reading in its
     * information and adding it to components.  If this component is a
     * subclass of another component, this method recursively calls itself on the
     * superclass.
     */
  private void processComponent(Element element) {
    // If the element is not a component (e.g., Float), return early.
    if (element.getAnnotation(SimpleObject.class) == null &&
        element.getAnnotation(DesignerComponent.class) == null) {
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
        componentInfo.broadcastReceivers.addAll(parentComponent.broadcastReceivers);
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

    // Add it to our components map.
    components.put(longComponentName, componentInfo);
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

    Property property = new Property(propertyName,
                                     simpleProperty.description(),
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
    } else {
      // It is a setter.
      property.writable = true;
      if (parameters.size() != 1) {
        throw new RuntimeException("Too many parameters for setter for " +
                                   propertyName);
      }
      typeMirror = parameters.get(0);
    }

    // Use typeMirror to set the property's type.
    if (!typeMirror.getKind().equals(TypeKind.VOID)) {
      property.type = typeMirror.toString();
      updateComponentTypes(typeMirror);
    }

    property.componentInfoName = componentInfoName;

    return property;
  }

  // Transform an @ActivityElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String activityElementToString(ActivityElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <activity> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("    <activity ");
    elementString.append(elementAttributesToString(element));
    elementString.append(">\\n");

    // Now, we collect any <activity> subelements.
    elementString.append(subelementsToString(element.metaDataElements()));
    elementString.append(subelementsToString(element.intentFilters()));

    // Finally, we close the <activity> element and create its String.
    return elementString.append("    </activity>\\n").toString();
  }

  // Transform a @ReceiverElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String receiverElementToString(ReceiverElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <receiver> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("    <receiver ");
    elementString.append(elementAttributesToString(element));
    elementString.append(">\\n");

    // Now, we collect any <receiver> subelements.
    elementString.append(subelementsToString(element.metaDataElements()));
    elementString.append(subelementsToString(element.intentFilters()));

    // Finally, we close the <receiver> element and create its String.
    return elementString.append("    </receiver>\\n").toString();
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
    return elementString.append("/>\\n").toString();
  }

  // Transform an @IntentFilterElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String intentFilterElementToString(IntentFilterElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <intent-filter> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("      <intent-filter ");
    elementString.append(elementAttributesToString(element));
    elementString.append(">\\n");
    
    // Now, we collect any <intent-filter> subelements.
    elementString.append(subelementsToString(element.actionElements()));
    elementString.append(subelementsToString(element.categoryElements()));
    elementString.append(subelementsToString(element.dataElements()));

    // Finally, we close the <intent-filter> element and create its String.
    return elementString.append("    </intent-filter>\\n").toString();
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
    return elementString.append("/>\\n").toString();
  }

  // Transform an @CategoryElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String categoryElementToString(CategoryElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <category> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("        <category ");
    elementString.append(elementAttributesToString(element));
    // Finally, we close the <category> element and create its String.
    return elementString.append("/>\\n").toString();
  }

  // Transform an @DataElement into an XML element String for use later
  // in creating AndroidManifest.xml.
  private static String dataElementToString(DataElement element)
      throws IllegalAccessException, InvocationTargetException {
    // First, we build the <data> element's opening tag including any
    // receiver element attributes.
    StringBuilder elementString = new StringBuilder("        <data ");
    elementString.append(elementAttributesToString(element));
    // Finally, we close the <data> element and create its String.
    return elementString.append("/>\\n").toString();
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
            attributeString.append("=\\\"");
            attributeString.append(attributeValue);
            attributeString.append("\\\"");
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
          }
        }
      } else {
        // Create a new Property element, then compare and combine it with any
        // prior Property element with the same property name, verifying that
        // they are consistent.
        Property newProperty = executableElementToProperty(element, componentInfo.name);

        if (componentInfo.properties.containsKey(propertyName)) {
          Property priorProperty = componentInfo.properties.get(propertyName);

          if (!priorProperty.type.equals(newProperty.type)) {
            // The 'real' type of a property is determined by its getter, if
            // it has one.  In theory there can be multiple setters which
            // take different types and those types can differ from the
            // getter.
            if (newProperty.readable) {
              priorProperty.type = newProperty.type;
            } else if (priorProperty.writable) {
              // TODO(user): handle lang_def and document generation for multiple setters.
              throw new RuntimeException("Inconsistent types " + priorProperty.type +
                                         " and " + newProperty.type + " for property " +
                                         propertyName + " in component " + componentInfo.name);
            }
          }

          // Merge newProperty into priorProperty, which is already in the properties map.
          if (priorProperty.description.isEmpty() && !newProperty.description.isEmpty()) {
            priorProperty.description = newProperty.description;
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
          priorProperty.readable = priorProperty.readable || newProperty.readable;
          priorProperty.writable = priorProperty.writable || newProperty.writable;
          priorProperty.userVisible = priorProperty.userVisible && newProperty.userVisible;
          priorProperty.deprecated = priorProperty.deprecated && newProperty.deprecated;
          priorProperty.componentInfoName = componentInfo.name;
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
        if (eventDescription.isEmpty()) {
          eventDescription = elementUtils.getDocComment(element);
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
        Event event = new Event(eventName, eventDescription, userVisible, deprecated);
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
          event.addParameter(ve.getSimpleName().toString(),
                             ve.asType().toString());
          updateComponentTypes(ve.asType());
        }
      }
    }
  }

  private void processMethods(ComponentInfo componentInfo,
                                Element componentElement) {
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
        String methodDescription = simpleFunctionAnnotation.description();
        if (methodDescription.isEmpty()) {
          methodDescription = elementUtils.getDocComment(element);
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
        Method method = new Method(methodName, methodDescription, userVisible, deprecated);
        componentInfo.methods.put(method.name, method);

        // Verify that this element has an ExecutableType.
        if (!(element instanceof ExecutableElement)) {
          throw new RuntimeException("In component " + componentInfo.name +
                                     ", the representation of SimpleFunction " + methodName +
                                     " does not implement ExecutableElement.");
        }
        ExecutableElement e = (ExecutableElement) element;

        // Extract the parameters.
        for (VariableElement ve : e.getParameters()) {
          method.addParameter(ve.getSimpleName().toString(),
                              ve.asType().toString());
          updateComponentTypes(ve.asType());
        }

        // Extract the return type.
        if (e.getReturnType().getKind() != TypeKind.VOID) {
          method.returnType = e.getReturnType().toString();
          updateComponentTypes(e.getReturnType());
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
   *                annotated with either @UsesPermission or @UsesBroadcastReceivers.
   * @param blockName The name of the block as it appears in the sources.
   */
  private void processConditionalAnnotations(ComponentInfo componentInfo, Element element,
                                             String blockName) {
    // Conditional UsesPermissions
    UsesPermissions usesPermissions = element.getAnnotation(UsesPermissions.class);
    if (usesPermissions != null) {
      componentInfo.conditionalPermissions.put(blockName, usesPermissions.value());
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
  }

  /**
   * <p>Outputs the required component information in the desired format.  It is called by
   * {@link #process} after the fields {@link #components} and {@link #messager}
   * have been populated.</p>
   *
   * <p>Implementations of this methods should call {@link #getOutputWriter(String)} to obtain a
   * {@link Writer} for their output.  Diagnostic messages should be written
   * using {@link #messager}.</p>
   */
  protected abstract void outputResults() throws IOException;

  /**
   * Returns the appropriate Yail type (e.g., "number" or "text") for a
   * given Java type (e.g., "float" or "java.lang.String").  All component
   * names are converted to "component".
   *
   * @param type a type name, as returned by {@link TypeMirror#toString()}
   * @return one of "boolean", "text", "number", "list", or "component".
   * @throws RuntimeException if the parameter cannot be mapped to any of the
   *         legal return values
   */
  protected final String javaTypeToYailType(String type) {
    // boolean -> boolean
    if (type.equals("boolean")) {
      return type;
    }
    // String -> text
    if (type.equals("java.lang.String")) {
      return "text";
    }
    // {float, double, int, short, long, byte} -> number
    if (type.equals("float") || type.equals("double") || type.equals("int") ||
        type.equals("short") || type.equals("long") || type.equals("byte")) {
      return "number";
    }
    // YailList -> list
    if (type.equals("com.google.appinventor.components.runtime.util.YailList")) {
      return "list";
    }
    // List<?> -> list
    if (type.startsWith("java.util.List")) {
      return "list";
    }

    // Calendar -> InstantInTime
    if (type.equals("java.util.Calendar")) {
      return "InstantInTime";
    }

    if (type.equals("java.lang.Object")) {
      return "any";
    }

    if (type.equals("com.google.appinventor.components.runtime.Component")) {
      return "component";
    }

    // Check if it's a component.
    if (componentTypes.contains(type)) {
      return "component";
    }

    throw new RuntimeException("Cannot convert Java type '" + type +
                               "' to Yail type");
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
