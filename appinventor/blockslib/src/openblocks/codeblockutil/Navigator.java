package openblocks.codeblockutil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * A navigator navigates between two different Explorers using a
 * set of buttons.
 *
 * Note however, that each explorer must be UNIQUE.  NO EXCEPTIONS!
 * That is, the name of a particular explorer may not be
 * shared by any other explorers.  This check rep. is made when
 * adding new explorer.
 *
 * A Navigator unfortunately makes a once dangerous assumption: the name of
 * each explorer must remain the same forever.  If the name changes, then
 * the invariant described in the previous paragraph no longer holds.
 *
 *
 */
final public class Navigator{
    /** The UI options */
    public enum Type{GLASS, MAGIC, POPUP, STACK, TABBED, WINDOW};
    /** UI Type */
    private Type explorerModel;
    /** The index of the active explorer being viewed.  0<=position<explorers.size */
    private int position;
    /** Ordered set of explorers */
    private List<Explorer> explorers;
    /** The viewport that holds all the explorers. Explorers should be lined up in order within this "view" */
    private JPanel view;
    /** The UI used to switch between the different explorers */
    private JPanel explorerTabber;
    /** The constraints for adding things to explorerTabber */
    private GridBagConstraints explorerTabberConstraints;
    /** The set of buttons in the explorerTabber */
    private List<CButton> buttons;
    // Matches the dark blue blocks
    private static final Color BUTTON_COLOR_TEXT = new Color(0, 0, 204);

    /**
     * Constructs new navigator with an empty collection of canvases.
     */
    public Navigator () {
        this(Type.GLASS);
    }
    public Navigator (Type UIModel) {
        explorerModel = UIModel;
        explorers = new ArrayList<Explorer>();

        view = new JPanel(new BorderLayout());
        view.setBackground(CGraphite.lightergreen);
        view.setOpaque(false);

        position = 0;
        explorerTabber = new JPanel(new GridBagLayout());
        explorerTabber.setOpaque(false);
        explorerTabberConstraints = new GridBagConstraints();
        explorerTabberConstraints.fill = GridBagConstraints.HORIZONTAL;
        explorerTabberConstraints.gridx = GridBagConstraints.RELATIVE;
        explorerTabberConstraints.gridy = 0;
        explorerTabberConstraints.anchor = GridBagConstraints.PAGE_END;
        buttons = new ArrayList<CButton>();
    }
    /**
     * prints an error message for debugging purposes
     * @param m
     */
    private void printError(String m){
        System.out.println(m);
        //new RuntimeException(m).printStackTrace();
    }

  /**
   * Adds a new Explorer
   * @param name - the name of the new explorer
   *
   * @requires NONE
   * @effects If the name is null, do nothing.
   *          If the name is not unique, do nothing
   *          If the name is not null, is unique, then add it to this
   *          navigators set of explorers and update the UI.
   */
  final public void addExplorer(final String name) {
    addExplorer(name, CGraphite.medgreen);
  }

    /**
     * Adds a new Explorer
     * @param name - the name of the new explorer
     *
     * @requires NONE
     * @effects If the name is null, do nothing.
     *          If the name is not unique, do nothing
     *          If the name is not null, is unique, then add it to this
     *          navigators set of explorers and update the UI.
     */
    final public void addExplorer(final String name, Color buttonColor){
        if(name == null){
            this.printError("Name of explorer may not be assigned as null");
            return;
        }
        for(Explorer explorer : explorers){
            if (explorer.getName().equals(name)){
                this.printError("May not add duplicate explorers named: "+name);
                return;
            }
        }
        Explorer explorer;
        if (explorerModel == Type.GLASS){
            explorer = new GlassExplorer();
        }else if (explorerModel ==  Type.MAGIC){
            explorer = new MagicExplorer();
        }else if (explorerModel == Type.POPUP){
            explorer = new PopupExplorer();
        }else if (explorerModel == Type.WINDOW){
            explorer = new WindowExplorer();
        }else if (explorerModel == Type.TABBED){
            explorer = new TabbedExplorer();
        }else {
            explorer = new StackExplorer();
        }
        explorer.setName(name);
        explorers.add(explorer);
        CButton button = new CCategoryTab(buttonColor, name);
        button.setPreferredSize(new Dimension(80, 24));
        button.setTextLighting(CGraphite.darkgray, BUTTON_COLOR_TEXT);
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
              setView(name);
            }
          });
        buttons.add(button);
        explorerTabber.add(button, explorerTabberConstraints);
        explorerTabber.revalidate();
        if (explorers.size() == 1) setView(0);
        this.reformView();
    }

    public List<Explorer> getExplorers(){
        return this.explorers;
    }
    /**
     * @param name
     * @return true iff there exists an explorer whose name is
     * equal to the specified name.  If name is null, return false.
     */
    public boolean hasExplorer(String name){
        if(name == null) return false;
        for(Explorer explorer : explorers){
            if (explorer.getName().equals(name)){
                return true;
            }
        }
        return false;
    }
    /**
     * This function resizes the explorers to match the view panel.
     */
    public void reformView(){
      Rectangle viewBounds = view.getBounds();
      for(Explorer explorer : explorers){
        explorer.getJComponent().setBounds(viewBounds);
        explorer.reformView();
      }
      view.revalidate();
      view.repaint();
    }
    /**
     * Reassigns the canvases to the explorer with the specified name.  If
     * no explorer is found to have that name, or if the name is null,
     * then do nothing.
     * @param canvases
     * @param explorer
     *
     * @requires canvases!= null
     */
    public void setCanvas(Collection<? extends Canvas> canvases, String explorer){
        for(Explorer ex : explorers){
            if(ex.getName().equals(explorer)){
                ex.setDrawersCard(canvases);
            }
        }
        this.reformView();
    }
    /**
     * Adds a canvas with the given index to the explorer with the specified
     * name.  If no explorer is found to have that name, or if the name is null,
     * then do nothing.
     * @param canvas
     * @param index
     * @param explorer
     *
     * @requires canvases!= null
     */
    public void addCanvas(Canvas canvas, int index, String explorer){
      for(Explorer ex : explorers){
          if(ex.getName().equals(explorer)){
              ex.addDrawersCard(canvas, index);
          }
      }
      this.reformView();
    }
    /**
     * Sets the view to the explorer with the specified name.
     * If no explorers are found with a matching name, or if the
     * name is null, then do nothing.
     * @param name
     */
    public void setView(String name){
        for(int i=0; i<explorers.size(); i++){
            Explorer ex = explorers.get(i);
            if(ex.getName().equals(name)){
                setView(i);
            }
        }
    }
    /**
     * Sets the view to the explorer with the specified
     * index.  The index is first transformed such that:
     *      (1) if index < 0 , then index = 0;
     *      (2) if index >= explorers.size, then index =0;
     *      (3) if explorer.isEmpty == true, then do nothing.
     * We must change the state and GUI to reflect this change.
     * @param index
     */
    private void setView(int index){
      // if the explorers are empty, make no changes.
      if(! explorers.isEmpty()) {
        if ((index < 0) || (index >= explorers.size())) index = 0;
        view.removeAll();
        view.add(explorers.get(index).getJComponent(), BorderLayout.SOUTH);
        view.revalidate();
        view.repaint();
        for(int i = 0; i<buttons.size(); i++){
          buttons.get(i).toggleSelected(i == index);
        }
        position = index;
      }
    }
    /**
     * @return the JComponent representation of this.  MAY NOT BE NULL
     */
    public JComponent getJComponent(){
        return view;
    }
    /**
     * @return the JComponent representation of the switching tool pane.
     */
    public JComponent getSwitcher(){
        return this.explorerTabber;
    }

    /** Testing purposes */
    public static void main(String[] args) {
        class CC extends DefaultCanvas{
            private static final long serialVersionUID = 328149080298L;
            public CC(String label){
                super();
                super.setName(label);
            }
            public JComponent getJComponent(){return new JButton(this.getName());}
        }
        final Navigator n = new Navigator();
        for(int i = 0 ; i<8; i++){
            List<Canvas> c1 = new ArrayList<Canvas>();
            for(int j= 0; j<10; j++){
                c1.add(new CC("# "+j));
            }
            n.addExplorer("Ex"+i);
            n.setCanvas(c1,"Ex"+i);
        }
        JFrame f = new JFrame();
        f.addComponentListener(new ComponentAdapter(){
            public void componentResized(ComponentEvent e){
                n.reformView();
            }
        });
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());
        f.setSize(400,600);
        f.add(n.getJComponent(), BorderLayout.CENTER);
        f.add(n.getSwitcher(), BorderLayout.NORTH);
        f.setVisible(true);
    }
}
