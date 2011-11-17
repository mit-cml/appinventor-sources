// Copyright 2009 Google Inc. All Rights Reserved.
package com.google.appinventor.components.scripts;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
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
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

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
 */
public abstract class ComponentProcessor extends AbstractProcessor {
  protected static final String OUTPUT_PACKAGE = "";

  // Returned by getSupportedAnnotationTypes()
  private static final Set<String> SUPPORTED_ANNOTATION_TYPES = ImmutableSet.of(
      "com.google.appinventor.components.annotations.DesignerComponent",
      "com.google.appinventor.components.annotations.DesignerProperty",
      "com.google.appinventor.components.annotations.SimpleEvent",
      "com.google.appinventor.components.annotations.SimpleFunction",
      "com.google.appinventor.components.annotations.SimpleObject",
      "com.google.appinventor.components.annotations.SimpleProperty");

  protected static final String READ_WRITE = "read-write";
  protected static final String READ_ONLY = "read-only";
  protected static final String WRITE_ONLY = "write-only";

  // The next three fields are set in init()
  /**
   * A handle allowing access to facilities provided by the annotation
   * processing tool framework
   */
  protected ProcessingEnvironment processingEnvironment;
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
   * SortedMap from fully-qualified name (e.g.,
   * "com.google.appinventor.components.runtime.components.android.Label") to
   * the corresponding {@link ComponentProcessor.ComponentInfo} object
   */
  protected final SortedMap<String, ComponentInfo> components = Maps.newTreeMap();

  private final List<String> componentTypes = Lists.newArrayList();

  /**
   * Represents a parameter consisting of a name and a type.  The type is a
   * String representation of the java type; e.g., "int", "double", or
   * "java.lang.String".
   */
  protected final class Parameter {
    protected final String name;
    protected final String type;

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

    protected ParameterizedFeature(String name, String description, String feature,
        boolean userVisible) {
      super(name, description, feature);
      this.userVisible = userVisible;
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
   * Represents a Young Android component event (annotated with
   * {@link SimpleEvent}).
   */
  protected final class Event extends ParameterizedFeature
      implements Cloneable, Comparable<Event> {
    // Inherits name, description, and parameters

    protected Event(String name, String description, boolean userVisible) {
      super(name, description, "Event", userVisible);
    }

    @Override
    public Event clone() {
      Event that = new Event(name, description, userVisible);
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
   * Represents a Young Android component method (annotated with
   * {@link SimpleFunction}).
   */
  protected final class Method extends ParameterizedFeature
      implements Cloneable, Comparable<Method> {
    // Inherits name, description, and parameters
    private String returnType;

    protected Method(String name, String description, boolean userVisible) {
      super(name, description, "Method", userVisible);
      // returnType defaults to null
    }

    protected String getReturnType() {
      return returnType;
    }

    @Override
    public Method clone() {
      Method that = new Method(name, description, userVisible);
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
   * Represents a Young Android component property (annotated with
   * {@link SimpleProperty}).
   */
  protected static final class Property implements Cloneable {
    protected final String name;
    private String description;
    private PropertyCategory propertyCategory;
    private boolean userVisible;
    private String type;
    private boolean readable;
    private boolean writable;
    private String componentInfoName;

    protected Property(String name, String description,
                       PropertyCategory category, boolean userVisible) {
      this.name = name;
      this.description = description;
      this.propertyCategory = category;
      this.userVisible = userVisible;
      // type defaults to null
      // readable and writable default to false
    }

    @Override
    public Property clone() {
      Property that = new Property(name, description, propertyCategory, userVisible);
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

    protected String getDescription() {
      return description;
    }

    protected boolean isUserVisible() {
      return userVisible;
    }

    protected String getType() {
      return type;
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
   * Represents a Young Android component, including its designer properties,
   * properties, methods, and events.
   */
  protected final class ComponentInfo extends Feature {
    // Inherits name and description
    protected final Set<String> permissions;
    protected final SortedMap<String, DesignerProperty> designerProperties;
    protected final SortedMap<String, Property> properties;
    protected final SortedMap<String, Method> methods;
    protected final SortedMap<String, Event> events;
    protected final boolean abstractClass;
    protected final String displayName;
    private String helpDescription;  // Shorter popup description
    private String category;
    private String categoryString;
    private boolean simpleObject;
    private boolean designerComponent;
    private int version;
    private boolean showOnPalette;
    private boolean nonVisible;
    private String iconName;

    protected ComponentInfo(Element element) {
      super(element.getSimpleName().toString(),  // Short name
            elementUtils.getDocComment(element),
            "Component");
      displayName = getDisplayNameForComponentType(name);
      permissions = Sets.newHashSet();
      designerProperties = Maps.newTreeMap();
      properties = Maps.newTreeMap();
      methods = Maps.newTreeMap();
      events = Maps.newTreeMap();
      abstractClass = element.getModifiers().contains(Modifier.ABSTRACT);
      for (AnnotationMirror am : element.getAnnotationMirrors()) {
        DeclaredType dt = am.getAnnotationType();
        String annotationName = am.getAnnotationType().toString();
        if (annotationName.equals(SimpleObject.class.getName())) {
          simpleObject = true;
        }
        if (annotationName.equals(DesignerComponent.class.getName())) {
          designerComponent = true;
          DesignerComponent designerComponentAnnotation =
              element.getAnnotation(DesignerComponent.class);

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

          category = designerComponentAnnotation.category().getName();
          categoryString = designerComponentAnnotation.category().toString();
          version = designerComponentAnnotation.version();
          showOnPalette = designerComponentAnnotation.showOnPalette();
          nonVisible = designerComponentAnnotation.nonVisible();
          iconName = designerComponentAnnotation.iconName();
        }
      }
    }

    protected String getHelpDescription() {
      return helpDescription;
    }

    protected String getCategory() {
      return category;
    }

    protected String getCategoryString() {
      return categoryString;
    }

    protected int getVersion() {
      return version;
    }

    protected boolean getShowOnPalette() {
      return showOnPalette;
    }

    protected boolean getNonVisible() {
      return nonVisible;
    }

    protected String getIconName() {
      return iconName;
    }

    private String getDisplayNameForComponentType(String componentTypeName) {
      // Users don't know what a 'Form' is.  They know it as a 'Screen'.
      return "Form".equals(componentTypeName) ? "Screen" : componentTypeName;
    }

  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return SUPPORTED_ANNOTATION_TYPES;
  }

  @Override
  public void init(ProcessingEnvironment processingEnv) {
    processingEnvironment = processingEnv;
    elementUtils = processingEnv.getElementUtils();
    typeUtils = processingEnv.getTypeUtils();
  }

  @Override
  public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
    // This method will be called many times for the source code.
    // Only do something on the first pass.
    pass++;
    if (pass > 1) {
      return true;
    }

    messager = processingEnvironment.getMessager();

    for (TypeElement te : elements) {
      if (te.getSimpleName().toString().equals("DesignerComponent")
          || te.getSimpleName().toString().equals("SimpleObject")) {
        for (Element element : env.getElementsAnnotatedWith(te)) {
          processComponent(element);
        }
      }
    }

    // Put the component class names (including abstract classes)
    componentTypes.addAll(components.keySet());

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

  /**
   * This processes an element if it represents a component, reading in its
   * information and adding it to {#link components}.  If this component is a
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
    String longComponentName = element.asType().toString();
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
      ComponentInfo parentComponent = components.get(parentName);
      if (parentComponent == null) {
        // Try to process the parent component now.
        Element parentElement = elementUtils.getTypeElement(parentName);
        if (parentElement != null) {
          processComponent(parentElement);
          parentComponent = components.get(parentName);
        }
      }

      // If we still can't find the parent class, we don't care about it,
      // since it's not a component (but something like java.lang.Object).
      // Otherwise, we need to copy its designer properties, properties, methods, and events.
      if (parentComponent != null) {
        // Copy its permissions, designer properties, properties, methods, and events.
        componentInfo.permissions.addAll(parentComponent.permissions);
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
    UsesPermissions up = element.getAnnotation(UsesPermissions.class);
    if (up != null) {
      for (String permission : up.permissionNames().split(",")) {
        componentInfo.permissions.add(permission.trim());
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
                                     simpleProperty.userVisible());

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
    }

    property.componentInfoName = componentInfoName;

    return property;
  }

  private void processProperties(ComponentInfo componentInfo,
                                 Element componentElement) {
    // We no longer support properties that use the variant type.

    for (Element element : componentElement.getEnclosedElements()) {
      if (!isPublicMethod(element)) {
        continue;
      }

      // Get the name of the prospective property.
      String propertyName = element.getSimpleName().toString();

      // Designer property information
      DesignerProperty designerProperty = element.getAnnotation(DesignerProperty.class);
      if (designerProperty != null) {
        componentInfo.designerProperties.put(propertyName, designerProperty);
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
              // TODO(markf): handle lang_def and document generation for multiple setters.
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
          priorProperty.componentInfoName = componentInfo.name;
        } else {
          // Add the new property to the properties map.
          componentInfo.properties.put(propertyName, newProperty);
        }
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
        Event event = new Event(eventName, eventDescription, userVisible);
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
        Method method = new Method(methodName, methodDescription, userVisible);
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
        }

        // Extract the return type.
        if (e.getReturnType().getKind() != TypeKind.VOID) {
          method.returnType = e.getReturnType().toString();
        }
      }
    }
  }

  /**
   * Method implemented by concrete subclasses to produce output once the
   * input source files are processed.  The fields {@link #components},
   * {@link #messager}, and {@link #processingEnvironment} are
   * guaranteed to be correctly populated when this is called.
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
    // {float, double, int, short, long} -> number
    if (type.equals("float") || type.equals("double") || type.equals("int") ||
        type.equals("short") || type.equals("long")) {
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

    // Check if it's a component.
    if (componentTypes.contains(type)) {
      return "component";
    }

    throw new RuntimeException("Cannot convert Java type '" + type +
                               "' to Yail type");
  }
}
