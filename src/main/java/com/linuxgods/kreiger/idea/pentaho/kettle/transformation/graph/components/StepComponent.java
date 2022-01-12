package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.components;

import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class StepComponent extends JLabel {
    private final Step step;
    private boolean inside;

    public StepComponent(Step step, Icon icon) {
        super(step.getNameUntrimmed(), icon, SwingConstants.CENTER);
        this.step = step;
        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
        Dimension size = getPreferredSize();
        setBounds(step.getX(), step.getY(), (int) size.getWidth(), (int) size.getHeight());
        MouseAdapter mouseListener = new MouseAdapter() {
            private Point startLocation;
            private Point location;

            @Override public void mouseEntered(MouseEvent e) {
                inside = true;
                repaint();
            }

            @Override public void mouseExited(MouseEvent e) {
                inside = false;
                repaint();
            }

            @Override public void mousePressed(MouseEvent e) {
                this.location = getLocation();
                this.startLocation = e.getLocationOnScreen();
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (null == startLocation) {
                    return;
                }
                int deltaX = e.getXOnScreen() - startLocation.x;
                int deltaY = e.getYOnScreen() - startLocation.y;

                setLocation((int) (location.x + deltaX), (int) (location.y + deltaY));
            }
        };
        addMouseListener(mouseListener);
        addMouseMotionListener(mouseListener);
    }

    Point getIconCenter() {
        return new Point(getX() + getWidth() / 2, 1+getY() + getIcon().getIconHeight() / 2);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (inside) {
            g.setColor(Color.RED);
            g.drawRect(getWidth()/2-16,1,32,32);
        }
        //g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
    }

    public Step getStep() {
        return step;
    }
}
