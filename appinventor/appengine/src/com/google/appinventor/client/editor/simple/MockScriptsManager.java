package com.google.appinventor.client.editor.simple;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.editor.youngandroid.YaProjectEditor;
import com.google.appinventor.client.explorer.project.ComponentDatabaseChangeListener;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectChangeListener;
import com.google.appinventor.shared.rpc.project.ChecksumedFileException;
import com.google.appinventor.shared.rpc.project.ChecksumedLoadFile;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.gwt.core.client.ScriptInjector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.appinventor.client.Ode.MESSAGES;

public final class MockScriptsManager implements ComponentDatabaseChangeListener, ProjectChangeListener {

    public static MockScriptsManager INSTANCE;

    private Map<String, String> scriptsMap = new HashMap<>(); // component type and script file

    private MockScriptsManager() {
        // Do not instantiate
    }

    /**
     * Initialises the [MockScriptsManager] on project load and registers the necessary listeners.
     *
     * @param projectEditor The [ProjectEditor] associated with the currently opened project
     * @param projectRootNode The [ProjectRootNode] associated with the currently opened project
     */
    public static void init(YaProjectEditor projectEditor, ProjectRootNode projectRootNode) {
        if (INSTANCE != null) {
            destroy();
        }
        INSTANCE = new MockScriptsManager();

        projectEditor.addComponentDatbaseListener(INSTANCE);

        Ode.getInstance()
                .getProjectManager()
                .getProject(projectRootNode)
                .addProjectChangeListener(INSTANCE);
    }

    private static void destroy() {
        INSTANCE.unloadAll();
        INSTANCE = null;
    }

    /**
     * Loads a Mock<Component> file for the given component type.
     *
     * @param type The FQCN of the component
     */
    public void load(final String type) {
        if (scriptsMap.containsKey(type)) {
            return; // script already loaded; don't load again!
        }

        Ode ode = Ode.getInstance();

        long projectId = ode.getCurrentYoungAndroidProjectId();

        String pkgName = type.substring(0, type.lastIndexOf('.'));
        String simpleName = type.substring(type.lastIndexOf('.') + 1);

        if (!SimpleComponentDatabase.getInstance(projectId).hasCustomMock(simpleName)) {
            return; // Component doesn't have its own custom Mock, so don't try to load it
        }

        String componentsFolder = ((YoungAndroidProjectNode) ode.getCurrentYoungAndroidProjectRootNode())
                .getComponentsFolder().getFileId();

        String fileId = componentsFolder + "/" + pkgName + "/Mock" + simpleName + ".js";

        ode.getProjectService().load2(
                projectId,
                fileId,
                new OdeAsyncCallback<ChecksumedLoadFile>(MESSAGES.loadError()) {
                    @Override
                    public void onSuccess(ChecksumedLoadFile result) {
                        try {
                            String mockJsFile = result.getContent();

                            scriptsMap.put(type, mockJsFile);

                            ScriptInjector.fromString(mockJsFile)
                                    .setWindow(ScriptInjector.TOP_WINDOW)
                                    .inject();

                        } catch (ChecksumedFileException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    public void upgrade(String type) {
        if (scriptsMap.containsKey(type)) {
            unload(type);
            load(type);
        }
    }

    public void unload(String type) {
        String simpleName = type.substring(type.lastIndexOf('.') + 1);
        scriptsMap.remove(type);
        MockComponentRegistry.unregister(simpleName);
        cleanup(simpleName);
    }

    private void unloadAll() {
        for (String type : scriptsMap.keySet()) {
            unload(type);
        }
    }

    private static native void cleanup(String name)/*-{
        delete $wnd["Mock" + name];
    }-*/;

    //// ComponentDatabaseChangeListener

    @Override
    public void onComponentTypeAdded(List<String> componentTypes) {
        for (String componentType : componentTypes) {
            load(componentType);
        }
    }

    @Override
    public boolean beforeComponentTypeRemoved(List<String> componentTypes) {
        return false;
    }

    @Override
    public void onComponentTypeRemoved(Map<String, String> componentTypes) {
        // returns map??
    }

    @Override
    public void onResetDatabase() {
        unloadAll();
    }

    //// ProjectChangeListener

    @Override
    public void onProjectLoaded(Project project) {

    }

    @Override
    public void onProjectNodeAdded(Project project, ProjectNode node) {

    }

    @Override
    public void onProjectNodeRemoved(Project project, ProjectNode node) {
        destroy();
    }
}
