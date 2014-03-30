package com.google.appinventor.server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.google.appinventor.server.properties.json.ServerJsonParser;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.properties.json.JSONArray;
import com.google.appinventor.shared.properties.json.JSONObject;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.properties.json.JSONValue;
import com.google.appinventor.shared.rpc.privacy.PrivacyEditorService;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.appinventor.shared.youngandroid.YoungAndroidXMLSourceAnalyzer;

public class PrivacyEditorServiceImpl extends OdeRemoteServiceServlet implements PrivacyEditorService {

  // Declare and Initialize required constants
  private final transient StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
  private static final JSONParser JSON_PARSER = new ServerJsonParser();
  private static Model model = ModelFactory.createDefaultModel();
  
  // Custom Defined Constants
  private static final String TEMPLATE_LOC ="privacy_templates/"; // template location with respective to current classpath
  private static final String AI_NS = "http://dig.csail.mit.edu/2014/PrivacyInformer/appinventor#";
  private static final String COMPONENT_NS = "http://dig.csail.mit.edu/2014/PrivacyInformer/";
  private static final Property contains = ResourceFactory.createProperty( AI_NS, "contains");
  private static final Property connectsTo = ResourceFactory.createProperty( AI_NS, "connectsTo");
  
  @Override
  public String getPreview(long projectId) {
    // reset model statements
    model.removeAll();
    // get templates
    List<String> templates = getTemplates(getClass());
    // reset preview text
    String preview = "";
    // get userId based on projectId
    final String userId = userInfoProvider.getUserId();
    
    // create a unique URI based on the project name and email address
    // populate it with basic RDF.type
    String privacyDescriptionURI = "http://www.example.org/" + storageIo.getProjectName(userId, projectId) + userInfoProvider.getUserEmail();
    Resource privacyDescription = model.createResource(privacyDescriptionURI).addProperty(RDF.type, ResourceFactory.createResource( AI_NS + "PrivacyDescription"));
    
    // get the project source files 
    List<String> projectFiles = storageIo.getProjectSourceFiles(userId, projectId);
    
    // get a list of all components in the project
    List<String> appComponents = getComponentList(projectFiles, userId, projectId);
    
    // for each component, if it has a template, add the component to Jena model then add its template
    for (String component : appComponents) {
      if (templates.contains(component)) {
        privacyDescription.addProperty(contains, ResourceFactory.createResource( COMPONENT_NS + component + "#" + component + "Component"));
        model.read(getClass().getResourceAsStream( TEMPLATE_LOC + component), null, "TTL");
      }
    }
    
    // get blocks logic files
    List<String> xmlFiles = getBlocks(projectFiles, userId, projectId);
    
    // for each XML source file, parse it and get the relationships in it, then process relationships by adding "connectsTo" statements to the model
    for (String file : xmlFiles) {
      ArrayList<ArrayList<ArrayList<String>>> relationships = YoungAndroidXMLSourceAnalyzer.parseXMLSource(file, templates);
      processRelationships(relationships);
    }
    
    StringWriter out = new StringWriter();
    model.write(out, "TTL");
    
    System.out.println(out.toString());
    preview = "";
    return preview;
  }

  // Get component list
  private List<String> getComponentList(List<String> projectFiles, String userId, long projectId) {
    List<String> appComponents = new ArrayList<String>();
    for (String filename : projectFiles) {
      // .scm contains list of components
      if (filename.substring(filename.length()-4).equals(".scm")) {
        JSONObject propertiesObject = YoungAndroidSourceAnalyzer.parseSourceFile(storageIo.downloadFile(userId, projectId, filename, StorageUtil.DEFAULT_CHARSET), JSON_PARSER);
        JSONObject formProperties = propertiesObject.get("Properties").asObject();
        Map<String,JSONValue> allProperties = formProperties.getProperties();
        if (allProperties.containsKey("$Components")) {
          JSONArray components = formProperties.get("$Components").asArray();
          getAllComponents(components, appComponents);
        }
      }
    }
    return appComponents;
  }
  
  // Get all components in the project, recursively
  private void getAllComponents(JSONArray components, List<String> appComponents) {
    for (JSONValue component : components.getElements()) {
      String element = component.asObject().get("$Type").asString().getString();
      if (!appComponents.contains(element)) {
        appComponents.add(element);
      }
      
      Map<String,JSONValue> allProperties = component.asObject().getProperties();
      if (allProperties.containsKey("$Components")) {
        JSONArray nestedComponents = component.asObject().get("$Components").asArray();
        getAllComponents(nestedComponents, appComponents);
      }
    }
  }
  
  // Get blocks logic
  private List<String> getBlocks(List<String> projectFiles, String userId, long projectId) {
    List<String> xmlFiles = new ArrayList<String>();
    for (String filename : projectFiles) {
      // .bky contains blocks logic
      if (filename.substring(filename.length()-4).equals(".bky")) {
        xmlFiles.add(storageIo.downloadFile(userId, projectId, filename, StorageUtil.DEFAULT_CHARSET));
      }
    }
    return xmlFiles;
  }
  
  // Process relationships between AppInventor components as parsed by the BkyParserHandler
  private void processRelationships(ArrayList<ArrayList<ArrayList<String>>> relationships) {
    for (ArrayList<ArrayList<String>> relationship : relationships) {
      assert (relationship.size() == 2); // each relationship is between a pair of components only
      ArrayList<String> comp1 = relationship.get(0);
      ArrayList<String> comp2 = relationship.get(1);
      Resource parentComp;
      if (!comp1.get(1).equalsIgnoreCase("NONE")) {
        parentComp = model.createResource(COMPONENT_NS + comp1.get(0) + "#" + comp1.get(2));
      } else {
        parentComp = model.createResource(COMPONENT_NS + comp1.get(0) + "#" + comp1.get(0) + "Component");
      }
      
      if (!comp2.get(1).equalsIgnoreCase("NONE")) {
        parentComp.addProperty(connectsTo, ResourceFactory.createResource(COMPONENT_NS + comp2.get(0) + "#" + comp2.get(2)));
      } else {
        parentComp.addProperty(connectsTo, ResourceFactory.createResource(COMPONENT_NS + comp2.get(0) + "#" + comp2.get(0) + "Component"));
      }
    }
  }
  // Get a list of available templates using given classpath
  private List<String> getTemplates(Class loader) {
    List<String> templates = new ArrayList<String>();
    InputStream in = loader.getResourceAsStream(TEMPLATE_LOC);
    BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
    String line;
    try {
      while ((line = rdr.readLine()) != null) {
          templates.add(line);
      }
      rdr.close();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return templates;
  }
}
