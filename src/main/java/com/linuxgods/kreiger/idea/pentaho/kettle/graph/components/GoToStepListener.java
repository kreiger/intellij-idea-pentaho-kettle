package com.linuxgods.kreiger.idea.pentaho.kettle.graph.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GoToStepListener extends MouseAdapter {
    private final Runnable runnable;

    public GoToStepListener(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override public void mouseClicked(MouseEvent e) {
        runnable.run();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        e.getComponent().setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        e.getComponent().setCursor(null);
    }
}
