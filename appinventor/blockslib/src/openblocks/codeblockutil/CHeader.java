package openblocks.codeblockutil;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class CHeader extends JPanel {
    private static final int LEFTNAV_WIDTH = 180;
    private static final int HEADER_HEIGHT = 40;

    private JLabel headerLabel;

    public CHeader(JComponent[] buttonsLeft, JComponent[] buttonsRight) {
        super();
        this.setOpaque(false);

        double[][] size = {
          {LEFTNAV_WIDTH, TableLayoutConstants.FILL},
          {HEADER_HEIGHT}};
        this.setLayout(new TableLayout(size));

        headerLabel = new JLabel("", JLabel.LEFT);
        headerLabel.setOpaque(false);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 15));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setPreferredSize(new Dimension(LEFTNAV_WIDTH, HEADER_HEIGHT));
        // set left margin
        headerLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        this.add(headerLabel, "0, 0, l, c");

        JPanel buttonPane = new JPanel();
        buttonPane.setOpaque(false);
        buttonPane.setLayout(new BorderLayout());

        JPanel leftButtonPane = new JPanel();
        leftButtonPane.setOpaque(false);
        // don't understand why this pane is not vertically centered
        leftButtonPane.setBorder(new EmptyBorder(2, 0, 0, 0));
        for(int j = 0; j < buttonsLeft.length; j++){
            leftButtonPane.add(buttonsLeft[j]);
        }
        buttonPane.add(leftButtonPane, BorderLayout.WEST);
        //        this.add(leftButtonPane, "1, 0, l, c");

        JPanel rightButtonPane = new JPanel();
        rightButtonPane.setOpaque(false);
        for(int j = 0; j < buttonsRight.length; j++){
            rightButtonPane.add(buttonsRight[j]);
        }
        buttonPane.add(rightButtonPane, BorderLayout.EAST);
        //        this.add(rightButtonPane, "2, 0, r, c");

        this.add(buttonPane, "1, 0, f, c");
    }

    public void setHeaderText(String headerText) {
      headerLabel.setText(headerText);
    }

    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D )g;
        g2.setColor(CGraphite.medgreen);
        g2.fillRect(0,0,this.getWidth(), this.getHeight());
        super.paint(g);
    }

    public void resize(){
        this.invalidate();
        this.revalidate();
    }
}
