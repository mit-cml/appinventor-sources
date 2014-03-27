package com.google.appinventor.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
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

public class PrivacyEditorServiceImpl extends OdeRemoteServiceServlet implements PrivacyEditorService {

  private final transient StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;
  private static final JSONParser JSON_PARSER = new ServerJsonParser();
  Model model = ModelFactory.createDefaultModel();
  
  @Override
  public String getPreview(long projectId) {
    // TODO generate the appropriate preview based on the list of components used in the project
    final String userId = userInfoProvider.getUserId();
    List<String> projectFiles = storageIo.getProjectSourceFiles(userId, projectId);
    String preview = "";
    List<String> appComponents = new ArrayList<String>();
    
    for (String filename : projectFiles) {
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
    
    for (String component : appComponents) {
      preview += component + "<br>";
    }
    
    InputStream template = getClass().getResourceAsStream("privacy_templates/Twitter.ttl");
    model.read(template, null, "TTL");
    template = getClass().getResourceAsStream("privacy_templates/Web.ttl");
    model.read(template, null, "TTL");
    template = getClass().getResourceAsStream("privacy_templates/Accelerometer.ttl");
    model.read(template, null, "TTL");
    
    StringWriter out = new StringWriter();
    model.write(out, "TTL");

    System.out.println(out.toString());
    preview += out.toString();
    return preview;
  }

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
}
