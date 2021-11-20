package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.graph.view.NodeRealizer;

public interface Node {
    String getName();

    NodeRealizer getRealizer(TransformationGraphPresentationModel presentationModel);

    int getHeight();

    int getWidth();

    int getX();

    int getY();

    String getTooltip();

    DefaultActionGroup getActionGroup();
}
