package com.linuxgods.kreiger.idea.pentaho.kettle.graph.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class MouseDragAdapter extends MouseAdapter {
    private Point mouseStartLocationOnScreen;
    private Point startLocation;
    private Component view;

    public MouseDragAdapter(Component view) {
        this.view = view;
        view.addMouseListener(this);
        view.addMouseMotionListener(this);
    }

    @Override public void mousePressed(MouseEvent e) {
        this.startLocation = view.getLocation();
        this.mouseStartLocationOnScreen = e.getLocationOnScreen();
        e.consume();
    }

    @Override public void mouseDragged(MouseEvent e) {
        if (null == mouseStartLocationOnScreen) {
            return;
        }
        e.consume();
        int deltaX = e.getXOnScreen() - mouseStartLocationOnScreen.x;
        int deltaY = e.getYOnScreen() - mouseStartLocationOnScreen.y;

        mouseDraggedTo(e.getComponent(), startLocation.x+deltaX, startLocation.y+deltaY);
    }

    public abstract void mouseDraggedTo(Component component, int x, int y);
}
