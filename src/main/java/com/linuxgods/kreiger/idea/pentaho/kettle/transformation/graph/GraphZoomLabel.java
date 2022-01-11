package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.ToolbarLabelAction;
import org.jetbrains.annotations.NotNull;

public class GraphZoomLabel extends ToolbarLabelAction {
    @Override public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(true);
        e.getPresentation().setText("100%");
    }
}
