package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.components;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GoToStepListener extends MouseAdapter {
    private final Runnable runnable;

    public GoToStepListener(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 || (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
            runnable.run();
        }
    }
}
