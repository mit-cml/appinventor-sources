package openblocks.workspace.typeblocking;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import openblocks.renderable.BlockUtilities;
import openblocks.renderable.TextualFactoryBlock;
import openblocks.workspace.Workspace;
import openblocks.codeblockutil.CGraphite;
import openblocks.codeblockutil.CTracklessScrollPane;


/**
 * AutCompletePanel is a Panel that displays an editable text field
 * for the user to produce a desired pattern (regex).  It also
 * provides a JList for users to see some pre-defined choices.
 * User may choose to act/react to AutoCompletePanel intuitively.
 */
public class AutoCompletePanel  extends JPanel implements MouseListener, MouseMotionListener{
	private static final long serialVersionUID = 328149080418L;
	private static final int MARGIN = 7;
        private static final Color BACKGROUND = CGraphite.lightergreen;
	/**Minimum width**/
	private static final int MINIMUM_WIDTH = 105;  
	/**Minimum height**/
	private static final int MINIMUM_HEIGHT = 105; 
	/**Minimum width**/
	private int preferredWidth = 165; //This is the default width of the keytyping window
	/**Minimum height**/
	private int preferredHeight = 125;  //This is the default height of the keytyping window
	/**font of this**/
	private final Font font;
	/**editable text field for user to enter in desired pattern**/
	private final JTextField editor;
	/**menu that displays set of possibilities from user-input patter**/
	private final JList menu;
	/**Constructs AutoCompletePanel*/
	@SuppressWarnings("serial")
	public AutoCompletePanel (){
		super(new BorderLayout());
		font = new Font("Ariel", Font.BOLD, 12);

		//set up editor (text field)
		editor = new JTextField();
		editor.setFont(font);
		editor.setBackground(BACKGROUND);


		//Set up menu (JList)
		menu= new JList();
		menu.setFont(font);
		menu.setBackground(BACKGROUND);
		menu.setLayoutOrientation(JList.VERTICAL);
		CTracklessScrollPane menuPane = new CTracklessScrollPane(menu,
				7, CGraphite.lightgreen, BACKGROUND){
			public Insets getInsets(){
				return new Insets(MARGIN,0,0,0);
			}
		};
		menuPane.setBackground(BACKGROUND);
		menu.setCellRenderer(new QueryCellRenderer());

		//Set up this
		this.setOpaque(false);
		this.setSize(preferredWidth,preferredHeight);
		//this.setBorder(BorderFactory.createEtchedBorder(Color.white, Color.lightGray));
		this.add(editor, BorderLayout.NORTH);
		this.add(menuPane, BorderLayout.CENTER);


		//add Listeners
		this.addFocusListener(new FocusListener(){
			//pass focus onto editor
			public void focusGained(FocusEvent e) {
				editor.requestFocus();
			}
			public void focusLost(FocusEvent e) {}
		});
		EditorListener editorListener = new EditorListener();
		//Return to phase one whenever user enters more text.
		this.editor.getDocument().addDocumentListener(editorListener);
		//if editor && menu loses focus, then make this TypeBlockManager disappear
		this.editor.addFocusListener(editorListener);
		//If the user pressed enter, then use enter phase two
		this.editor.addKeyListener(editorListener);

		MenuListener menuListener = new MenuListener();
		//if editor && menu loses focus, then make this TypeBlockManager disappear
		this.menu.addFocusListener(menuListener);
		//If the user double clicks, then enter phase two
		this.menu.addMouseListener(menuListener);
		//If the user pressed enter, then use enter phase two
		this.menu.addKeyListener(menuListener);

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		int w = this.getWidth();
		int h = this.getHeight();
		g.setColor(BACKGROUND);
		g.fillRoundRect(0,0,w-1,h-1,MARGIN*2,MARGIN*2);

		GeneralPath resize = new GeneralPath();
		resize.moveTo(w-2*MARGIN,h);
		resize.lineTo(w,h-2*MARGIN);
		resize.curveTo(w-1,h-1,w-1,h-1,w-2*MARGIN,h);
		g.setColor(CGraphite.medgreen);
		g2.fill(resize);

		g.setColor(CGraphite.medgreen);
		g.drawRoundRect(0,0,w-1,h-1,MARGIN*2,MARGIN*2);

		super.paint(g);
	}
	public Insets getInsets(){
		return new Insets(MARGIN,MARGIN,MARGIN,MARGIN);
	}
	private boolean resizing = false;
	public void mousePressed(MouseEvent e) {
		if(e.getX()>(this.getWidth()-2*MARGIN) && e.getY()>(this.getHeight()-2*MARGIN)){
			resizing = true;
		}
	}
	public void mouseReleased(MouseEvent e) {
		resizing = false;
	}
	public void mouseDragged(MouseEvent e) {	
		if(resizing){
			preferredWidth = e.getX()>MINIMUM_WIDTH ? e.getX() : MINIMUM_WIDTH;
			preferredHeight = e.getY()>MINIMUM_HEIGHT ? e.getY() : MINIMUM_HEIGHT;
			updateMenu();
		}
	}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	/**
	 * Set's user-generated pattern to be text.
	 * @param text
	 */
	public void setText(String text){
		this.editor.setText(text);
	}
	/**
	 * Updates the menu such that it can display
	 * all the possible blocks that match the user-generated pattern
	 * (the user-generated pattern is entered within AutoCompletePanel.editor)
	 */
	private void updateMenu(){
		//resize to display entire text in editor
		if (this.editor.getPreferredSize().width > preferredWidth){
			this.setSize(this.editor.getPreferredSize().width, preferredHeight);
		}else{
			this.setSize(preferredWidth, preferredHeight);
		}
		//get matching blocks
		String text = editor.getText().trim();
		List<TextualFactoryBlock> matchingBlocks;

		//try to parse the string. if an integer, then get number block
		// if not an integer: if the user wants the "+" operation, then grab the
		// two "+" blocks, otherwise get the blocks matching the input text
		try {
			Float.valueOf(text);
			matchingBlocks = BlockUtilities.getDigits(text);
		} 
		catch (NumberFormatException e) {
			if (text.equals(TypeBlockManager.PLUS_OPERATION_LABEL)) {
				matchingBlocks = BlockUtilities.getPlusBlocks(text);
			}else {
				matchingBlocks = BlockUtilities.getAllMatchingBlocks(text);
			}
		}
		//update menu and repaint
		menu.setModel(new DefaultComboBoxModel(matchingBlocks.toArray()));
		this.revalidate();
		this.repaint();

	}
	/**
	 * Should display whatever block was last selected in menu.
	 */
	private void displayBlock(){
		Object obj = menu.getSelectedValue();
		if (obj != null && obj instanceof TextualFactoryBlock){
			//make JPanel-user-Interface invisible
			this.setVisible(false);
			this.revalidate();
			this.repaint();
			//pass created block to TpeBlockManager
			try {
				//if integer, then pass in the number typed by the user
				Float.valueOf(obj.toString());
				TypeBlockManager.automateBlockInsertion((TextualFactoryBlock)obj, obj.toString());
			} 
			catch (NumberFormatException e) {
				// if "+" then pass the two labels in
				if (obj.toString().equals(TypeBlockManager.NUMBER_PLUS_OPERATION_LABEL) || 
						obj.toString().equals(TypeBlockManager.TEXT_PLUS_OPERATION_LABEL)) {
					TypeBlockManager.automateBlockInsertion((TextualFactoryBlock)obj, obj.toString());
				// if starts with quote (is a string block)
				}else if(obj.toString().startsWith(TypeBlockManager.QUOTE_LABEL)) {
					String[] quote = obj.toString().split(TypeBlockManager.QUOTE_LABEL);
					TypeBlockManager.automateBlockInsertion((TextualFactoryBlock)obj, quote[1]);
				// otherwise, don't pass a label in
				}else {
					TypeBlockManager.automateBlockInsertion((TextualFactoryBlock)obj);
				}
			}
		}
	}
	/**
	 * Private helper class to provide the semantics for
	 * various listeners within the editor's TextField.
	 */
	private class EditorListener extends KeyAdapter implements DocumentListener, FocusListener{
		/**Constructs this listener*/
		public EditorListener(){}
		/**
		 * Document Listener.  Whenever AutoCompletePanel.editor
		 * receives a new user-generated character, it must
		 * update AutoCompletePanel to reflect the new pattern.
		 */
		public void changedUpdate(DocumentEvent e){
			updateMenu();
		}
		/**
		 * Document Listener.  Whenever AutoCompletePanel.editor
		 * receives a new user-generated character, it must
		 * update AutoCompletePanel to reflect the new pattern.
		 */
		public void insertUpdate(DocumentEvent e){
			updateMenu();
		}
		/**
		 * Document Listener.  Whenever AutoCompletePanel.editor
		 * receives a new user-generated character, it must
		 * update AutoCompletePanel to reflect the new pattern.
		 */
		public void removeUpdate(DocumentEvent e){
			updateMenu();
		}
		/**Repaint AutoCompletePanel when focus gained*/
		public void focusGained(FocusEvent e) {
			revalidate();
			repaint();
			//for Macs, when the focus is gained, the text within the editor is selected automatically.
			//set the text again to eliminate the selection.  (doing editor.setSelectionStart(0) doesn't work).
			String lcOSName = System.getProperty("os.name").toLowerCase();
			boolean MAC_OS_X = lcOSName.startsWith("mac os x");
			if(MAC_OS_X) {
				editor.setText(editor.getText());
			}
		}
		/**Turn Invisible if BOTH editor and menu loses focus*/
		public void focusLost(FocusEvent e) {
			if(e.getOppositeComponent() == null || e.getOppositeComponent().equals(menu)){
				return;
			}
			setVisible(false);
			revalidate();
			repaint();
		}
		/**Should respond to special key presses*/
		public void keyTyped(KeyEvent e) {}
		/**Should respond to special key presses*/
		public void keyPressed(KeyEvent e) {
			if(e.getKeyChar() == KeyEvent.VK_ENTER){
				menu.setSelectedIndex(0);
				displayBlock();
			}else if (e.getKeyCode() == KeyEvent.VK_DOWN){
				menu.setSelectedIndex(0);
				menu.requestFocus();//validation and repainting done in menu.focus gained
				menu.scrollRectToVisible(new Rectangle(0,0,0,0));
			}else if(e.getKeyChar() == KeyEvent.VK_ESCAPE){
				setVisible(false);
				revalidate();
				repaint();
				//TODO: AutoCompletePane should not know about 
				//the Workspace.  Need to design a better system for this.
				Workspace.getInstance().getBlockCanvas().getCanvas().requestFocus();
			}
		}

	}
	/**
	 * Private helper class to provide the semantics for
	 * various listeners within the menu's JList.
	 */
	private class MenuListener extends MouseAdapter implements FocusListener, KeyListener{
		/**Constructs MenuListener*/
		public MenuListener(){}
		/**Repaint AutoCompletePanel upon gaining focus*/
		public void focusGained(FocusEvent e) {
			revalidate();
			repaint();
		}
		/**If focus lost to BOTH menu and editor, then turn invisible*/
		public void focusLost(FocusEvent e) {
			if(e.getOppositeComponent() == null || e.getOppositeComponent().equals(editor)){
				return;
			}
			setVisible(false);
			revalidate();
			repaint();
		}
		/**Drop block if user double clicks on an item*/
		public void mouseClicked(MouseEvent e){
			if(e.getClickCount()==2){
				displayBlock();
			}
		}
		/**Drop selected block if user presses enter*/
		public void keyTyped(KeyEvent e) {
			if(e.getKeyChar() == KeyEvent.VK_ENTER){
				displayBlock();
			}else if(e.getKeyChar() == KeyEvent.VK_ESCAPE){
				setVisible(false);
				revalidate();
				repaint();
				//TODO: AutoCompletePane should not know about 
				//the Workspace.  Need to design a better system for this.
				Workspace.getInstance().getBlockCanvas().getCanvas().requestFocus();
			}
		}
		/**Do nothing*/
		public void keyPressed(KeyEvent e){}
		/**Do nothing*/
		public void keyReleased(KeyEvent e){}
	}

	/**
	 * CellRenderer of this.menu
	 */
	private class QueryCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 328149080419L;
		/**Color matching query*/
		public void paint (Graphics g){
                        Graphics2D g2 = (Graphics2D)g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			//initialize string data
			String query = editor.getText().toLowerCase().trim();
			String item = this.getText().toLowerCase();
			FontMetrics metrics = g2.getFontMetrics();

			//draw cell background
			if(this.getBackground()!=null){
				g2.setColor(this.getBackground());
				g2.fillRect(0, 0, this.getWidth(), this.getHeight());
			}

			//draw block's label
			g2.setColor(CGraphite.darkgray);
			g2.drawString(this.getText(), 2, this.getHeight()- metrics.getDescent());

			//highlight matching portion
			int index = item.indexOf(query);
			if(index!=-1){
                                g2.setColor(new Color(127, 166, 0));
				g2.drawString(
						this.getText().substring(index, index + query.length()),
						(int)metrics.getStringBounds(this.getText().substring(0, index), g2).getWidth()+2,
						this.getHeight()- metrics.getDescent());
			}
		}
	}
}


