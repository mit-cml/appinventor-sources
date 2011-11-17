package openblocks.renderable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextArea;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import openblocks.workspace.Workspace;
import openblocks.workspace.WorkspaceEvent;
import openblocks.yacodeblocks.Escapers;
import openblocks.codeblockutil.CGraphite;

/**
 * Comment stores and displays user-generated text that
 * can be edited by the user.
 *
 * Comments are associated with a parent source of type JComponent.
 * It should "tag" along with that component.  Note, however, that
 * this feature should be ensured by the parent source.  The
 * parent source can guarantee this by invoking the methods
 * setPosition, translatePosition, and setParent when
 * appropriate.
 */

public class Comment extends BlockNote {

  private static int INITIAL_WIDTH = 200;
  private static int INITIAL_HEIGHT = 100;
  private static int MAX_WIDTH = 300;

   private UndoManager undoManager;

    /**
     * Constructs a Comment
     * with belonging to source, with text of initText, and initial zoom
         * The comment's borders will have the color borderColor.
         *
         * Note that initializing a comment only constructs
     * all of the necessary structures.  To graphically display a comment,
     * the implementor must then add the comment using the proper
     * Swing methods OR through the convenience method Comment.setParent()
     *
     * @param source block that the comment is linked to.
     */
   public Comment(RenderableBlock source){
     this(new JTextArea(), source);
   }

   private Comment(JTextArea jText, RenderableBlock source) {
     super(jText, source, "?", CGraphite.yellow, true,
         MAX_WIDTH, INITIAL_HEIGHT);
     textContent = jText;
     textContent.setCaretColor(Color.BLACK);
     textContent.setLineWrap(true);
     textContent.setWrapStyleWord(true);
     fontMetrics = getFontMetrics(jText.getFont());
     textPadding = fontMetrics.stringWidth("M") * TEXT_PADDING_EMS;
     undoManager = new UndoManager();
     undoManager.setLimit(1000);
     textContent.getDocument().addUndoableEditListener(undoManager);
     textContent.addKeyListener(new KeyAdapter(){
       @Override
       public void keyPressed(KeyEvent e){
         Workspace.getInstance().notifyListeners(new WorkspaceEvent(
             getRenderableBlock().getParentWidget(), WorkspaceEvent.BLOCK_NOTE_CHANGED));

         if(e.isControlDown() || ((e.getModifiers() &
             Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0)){
           if(e.getKeyCode() == KeyEvent.VK_Z){
             try{
               undoManager.undo();
             }catch(CannotUndoException exception){}
           }else if(e.getKeyCode() == KeyEvent.VK_Y){
             try{
               undoManager.redo();
             }catch (CannotRedoException exception){}
           }
         }
       }
     });
     textContent.setEditable(true);
     setBlockNoteSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));
     source.chooseLocationForBalloon(this, INITIAL_WIDTH, INITIAL_HEIGHT);
   }

    /**
     * @return  the save String for this comment
     */
    @Override
    public String getSaveString() {
      StringBuffer saveString = new StringBuffer();
      saveString.append("<Comment>\n");
      saveString.append("<Text>");
      String textValue = getText().replaceAll("`", "'");
      // Encode comments to preserve international characters.
      textValue = Escapers.encodeInternationalCharacters(textValue);
      saveString.append(Escapers.escapeForXml(textValue));
      saveString.append("</Text>");
      saveString.append(super.getSaveString());
      saveString.append("</Comment>\n");
      return saveString.toString();
    }

    /**
     * Loads the comment from a NodeList of comment parts
     * @param commentChildren
     * @param rb
     * @return the comment from a NodeList of comment parts
     */
    public static Comment loadComment(NodeList commentChildren, RenderableBlock rb) {
      Comment comment = new Comment(rb);
      for (int j = 0; j < commentChildren.getLength(); j++) {
        Node commentChild = commentChildren.item(j);
        if (commentChild.getNodeName().equals("Text")) {
          String textValue = commentChild.getTextContent();
          // Decode comments to preserve international characters.
          textValue = Escapers.decodeInternationalCharacters(textValue);
          comment.setText(textValue);
          break;
        }
      }
      loadBlockNote(commentChildren, rb, comment);
      return comment;
    }

    /**
     * @return The comment's text
     */
    public String getText() {
      return textContent.getText();
    }

//    /**
//     * @modifies textContent
//     * @param text
//     */
//    public void setText(String text) {
//      textArea.setText(text);
//    }

    /**
     * String representation of this
     */
     @Override
     public String toString() {
        return "Comment ID: " + " at " + getLocation() + " with text: \"" + getText() + "\"";
    }
}
