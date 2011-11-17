package openblocks.controller;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import openblocks.workspace.SearchBar;
import openblocks.workspace.SearchableContainer;
import openblocks.workspace.Workspace;
import openblocks.codeblocks.BlockConnectorShape;
import openblocks.codeblocks.BlockGenus;
import openblocks.codeblocks.BlockLinkChecker;
import openblocks.codeblocks.CommandRule;
import openblocks.codeblocks.SocketRule;

/**
 *
 * There are three options in setting the language definition file.  It may be set
 * once and unchanged or it can be set multiple times.  You can set
 *
 */
public class WorkspaceController {

    // XXX never read locally
//    private static final String SAVE_FORMAT_DTD_FILEPATH = "support/save_format.dtd";

    /*private static final String workingDirectory = ((System.getProperty("application.home") != null) ?
            System.getProperty("application.home") :
                System.getProperty("user.dir"));*/

    private static String LANG_DEF_FILEPATH;

    private static Element langDefRoot;

    //flags
    private boolean isWorkspacePanelInitialized = false;
    /*
    *//** The single instance of the Workspace Controller*//*
    private static WorkspaceController wc = new WorkspaceController();*/

    protected JPanel workspacePanel;
    protected static Workspace workspace;
    protected SearchBar searchBar;

    //flag to indicate if a new lang definition file has been set
    private boolean langDefDirty = true;

    //flag to indicate if a workspace has been loaded/initialized
    private boolean workspaceLoaded = false;

    /**
     * Constructs a WorkspaceController instance that manages the
     * interaction with the codeblocks.Workspace
     *
     */
    public WorkspaceController(){
        workspace = Workspace.getInstance();
    }


/*    *//**
     * Returns the single instance of this
     * @return the single instance of this
     *//*
    public static WorkspaceController getInstance(){
        return wc;
    }*/

    ////////////////////
    //  LANG DEF FILE //
    ////////////////////

    /**
     * Sets the file path for the language definition file, if the
     * language definition file is located in
     */
    public void setLangDefFilePath(String filePath){

        LANG_DEF_FILEPATH = filePath; //TODO do we really need to save the file path?

        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            builder = factory.newDocumentBuilder();

            String langDefLocation = /*workingDirectory +*/ LANG_DEF_FILEPATH;
            doc = builder.parse(new File(langDefLocation));

            langDefRoot = doc.getDocumentElement();

            //set the dirty flag for the language definition file
            //to true now that a new file has been set
            langDefDirty = true;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Sets the contents of the Lang Def File to the specified
     * String langDefContents
     * @param langDefContents String contains the specification of a language
     * definition file
     */
    public void setLangDefFileString(String langDefContents){
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(langDefContents)));
            langDefRoot = doc.getDocumentElement();

            //set the dirty flag for the language definition file
            //to true now that a new file has been set
            langDefDirty = true;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the Lang Def File to the specified File langDefFile.
     * @param langDefFile File contains the specification of the a language
     * definition file.
     */
    public void setLangDefFile(File langDefFile){
        //LANG_DEF_FILEPATH = langDefFile.getCanonicalPath();

        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            builder = factory.newDocumentBuilder();

            doc = builder.parse(langDefFile);

            langDefRoot = doc.getDocumentElement();

            //set the dirty flag for the language definition file
            //to true now that a new file has been set
            langDefDirty = true;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads all the block genuses, properties, and link rules of
     * a language specified in the pre-defined language def file.
     * @param root Loads the language specified in the Element root
     */
    public void loadBlockLanguage(Element root){
        //load connector shapes
        //MUST load shapes before genuses in order to initialize connectors within
        //each block correctly
        BlockConnectorShape.loadBlockConnectorShapes(root);

        //load genuses
        BlockGenus.loadBlockGenera(root);


        //load rules
        BlockLinkChecker.addRule(new CommandRule());
        BlockLinkChecker.addRule(new SocketRule());

        //set the dirty flag for the language definition file
        //to false now that the lang file has been loaded
        langDefDirty = false;
    }

    /**
     * Resets the current language within the active
     * Workspace.
     *
     */
    public void resetLanguage(){
        //clear shape mappings
        BlockConnectorShape.resetConnectorShapeMappings();
        //clear block genuses
        BlockGenus.resetAllGenuses();
        //clear all link rules
        BlockLinkChecker.reset();
    }


    ////////////////////////
    // SAVING AND LOADING //
    ////////////////////////

    /**
     * Returns the save string for the entire workspace.  This includes the block workspace, any
     * custom factories, canvas view state and position, pages
     * @return the save string for the entire workspace.
     */
    public String getSaveString(){
        StringBuffer saveString = new StringBuffer();
        //append the save data
        saveString.append("<?xml version=\"1.0\" encoding=\"ISO-8859\"?>");
        saveString.append("\r\n");
        //dtd file path may not be correct...
        //saveString.append("<!DOCTYPE StarLogo-TNG SYSTEM \""+SAVE_FORMAT_DTD_FILEPATH+"\">");
        //append root node
        saveString.append("<CODEBLOCKS>");
        saveString.append(workspace.getSaveString());
        saveString.append("</CODEBLOCKS>");
        return saveString.toString();
    }

    /**
     * Loads a fresh workspace based on the default specifications in the language
     * definition file.  The block canvas will have no live blocks.
     */
    public void loadFreshWorkspace(){
        //need to just reset workspace (no need to reset language) unless
        //language was never loaded
        //reset only if workspace actually exists
        if(workspaceLoaded)
            resetWorkspace();

        if(langDefDirty)
            loadBlockLanguage(langDefRoot);

        workspace.loadWorkspaceFrom(null, langDefRoot);

        workspaceLoaded = true;
    }

    /**
     * Loads the programming project from the specified file path.
     * This method assumes that a Language Definition File has already
     * been specified for this programming project.
     * @param path String file path of the programming project to load
     */
    public void loadProjectFromPath(String path){
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            builder = factory.newDocumentBuilder();

            doc = builder.parse(new File(path));

            Element projectRoot = doc.getDocumentElement();

            //load the canvas (or pages and page blocks if any) blocks from the save file
            //also load drawers, or any custom drawers from file.  if no custom drawers
            //are present in root, then the default set of drawers is loaded from
            //langDefRoot
            workspace.loadWorkspaceFrom(projectRoot, langDefRoot);

            workspaceLoaded = true;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the programming project specified in the projectContents.
     * This method assumes that a Language Definition File has already been
     * specified for this programming project.
     * @param projectContents
     */
    public void loadProject(String projectContents){
        //need to reset workspace and language (only if new language has been set)

        //reset only if workspace actually exists
        if(workspaceLoaded)
            resetWorkspace();

        if(langDefDirty)
            loadBlockLanguage(langDefRoot);

        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(projectContents)));
            Element root = doc.getDocumentElement();
            //load the canvas (or pages and page blocks if any) blocks from the save file
            //also load drawers, or any custom drawers from file.  if no custom drawers
            //are present in root, then the default set of drawers is loaded from
            //langDefRoot
            workspace.loadWorkspaceFrom(root, langDefRoot);

            workspaceLoaded = true;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Loads the programming project specified in the projectContents String,
     * which is associated with the language definition file contained in the
     * specified langDefContents.  All the blocks contained in projectContents
     * must have an associted block genus defined in langDefContents.
     *
     * If the langDefContents have any workspace settings such as pages or
     * drawers and projectContents has workspace settings as well, the
     * workspace settings within the projectContents will override the
     * workspace settings in langDefContents.
     *
     * NOTE: The language definition contained in langDefContents does
     * not replace the default language definition file set by: setLangDefFilePath() or
     * setLangDefFile().
     *
     * @param projectContents
     * @param langDefContents String XML that defines the language of
     * projectContents
     */
    public void loadProject(String projectContents, String langDefContents){

        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document projectDoc;
        Document langDoc;
        try {
            builder = factory.newDocumentBuilder();
            projectDoc = builder.parse(new InputSource(new StringReader(projectContents)));
            Element projectRoot = projectDoc.getDocumentElement();
            langDoc = builder.parse(new InputSource(new StringReader(projectContents)));
            Element langRoot = langDoc.getDocumentElement();

            //need to reset workspace and language (if langDefContents != null)
            //reset only if workspace actually exists
            if(workspaceLoaded)
                resetWorkspace();

            if(langDefContents == null)
                loadBlockLanguage(langDefRoot);
            else
                loadBlockLanguage(langRoot);
            //TODO should verify that the roots of the two XML strings are valid
            workspace.loadWorkspaceFrom(projectRoot, langRoot);

            workspaceLoaded = true;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Resets the entire workspace.  This includes all blocks, pages, drawers, and trashed blocks.
     * Also resets the undo/redo stack.  The language (i.e. genuses and shapes) is not reset.
     */
    public void resetWorkspace(){
        //clear all pages and their drawers
        //clear all drawers and their content
        //clear all block and renderable block instances
        workspace.reset();
        //clear action history
        //rum.reset();
        //clear runblock manager data
        //rbm.reset();
    }



    /**
     * This method creates and lays out the entire workspace panel with its
     * different components.  Workspace and language data not loaded in
     * this function.
     * Should be call only once at application startup.
     */
    private void initWorkspacePanel(){
        //workspace = loadFreshWorkspace();

        /*//create search bar
        SearchBar searchBar = new SearchBar("Search blocks", "Search for blocks in the drawers and workspace", workspace);
        for(SearchableContainer con : getAllSearchableContainers()){
            searchBar.addSearchableContainer(con);
        }*/

        //add trashcan and prepare trashcan images
        //ImageIcon tc = new ImageIcon(workingDirectory + "/support/images/trash.png");
        //ImageIcon openedtc = new ImageIcon(workingDirectory + "/support/images/trash_open.png");

        //TrashCan trash = new TrashCan(tc.getImage(), openedtc.getImage());
        //workspace.addWidget(trash, true, true);

        //create save button
        /*JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                System.out.println(WorkspaceController.getInstance().getSaveString());
            }
        });*/

        workspacePanel = new JPanel();
        //JPanel topPane = new JPanel();

        //topPane.add(saveButton);
        //searchBar.getComponent().setPreferredSize(new Dimension(130, 23));
        //topPane.add(searchBar.getComponent());

        workspacePanel.setLayout(new BorderLayout());
        //workspacePanel.add(topPane, BorderLayout.PAGE_START);
        workspacePanel.add(workspace, BorderLayout.CENTER);

        isWorkspacePanelInitialized = true;
    }

    /**
     * Returns the JComponent of the entire workspace.
     * @return the JComponent of the entire workspace.
     */
    public JComponent getWorkspacePanel(){
        if(!isWorkspacePanelInitialized)
            initWorkspacePanel();
        return workspacePanel;
    }

    /**
     * Returns a SearchBar instance capable of searching for blocks
     * within the BlockCanvas and block drawers
     */
    public JComponent getSearchBar(){
        SearchBar searchBar = new SearchBar("Search blocks", "Search for blocks in the drawers and workspace", workspace);
        for(SearchableContainer con : getAllSearchableContainers()){
            searchBar.addSearchableContainer(con);
        }

        return searchBar.getComponent();
    }

    /**
     * Returns an unmodifiable Iterable of SearchableContainers
     * @return an unmodifiable Iterable of SearchableContainers
     */
    public Iterable<SearchableContainer> getAllSearchableContainers(){
        return workspace.getAllSearchableContainers();
    }

    /////////////////////////////////////
    // TESTING CODEBLOCKS SEPARATELY //
    /////////////////////////////////////
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI(WorkspaceController wc) {
        System.out.println("Creating GUI...");

        //Create and set up the window.
        JFrame frame = new JFrame("WorkspaceDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        int inset = 50;
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        frame.setBounds(100, 100, 500, 500);

        //create search bar
        SearchBar searchBar = new SearchBar("Search blocks", "Search for blocks in the drawers and workspace", workspace);
        for(SearchableContainer con : wc.getAllSearchableContainers()){
            searchBar.addSearchableContainer(con);
        }

        /*JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                System.out.println(wc.getSaveString());
            }
        });*/

        JPanel topPane = new JPanel();
        searchBar.getComponent().setPreferredSize(new Dimension(130, 23));
        topPane.add(searchBar.getComponent());
        //topPane.add(saveButton);
        frame.add(topPane, BorderLayout.PAGE_START);
        frame.add(wc.getWorkspacePanel(), BorderLayout.CENTER);

        frame.setVisible(true);

    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //TODO grab file path from args array

                LANG_DEF_FILEPATH = "/evo_lang_def.xml";

                //Create a new WorkspaceController
                WorkspaceController wc = new WorkspaceController();

                wc.setLangDefFilePath(LANG_DEF_FILEPATH);
                wc.loadFreshWorkspace();
                createAndShowGUI(wc);
            }
        });
    }

	public static void initWithLangDefFilePath(final String langDefFilePath) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                //Create a new WorkspaceController
                WorkspaceController wc = new WorkspaceController();

				wc.setLangDefFilePath(langDefFilePath);

                wc.loadFreshWorkspace();
                createAndShowGUI(wc);
            }
        });
	}
}
