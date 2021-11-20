package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.graph.GraphManager;
import com.intellij.openapi.graph.layout.DiscreteNodeLabelModel;
import com.intellij.openapi.graph.view.ImageNodeRealizer;
import com.intellij.openapi.graph.view.NodeLabel;
import com.intellij.openapi.graph.view.NodeRealizer;
import com.intellij.util.ui.UIUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static com.intellij.openapi.graph.layout.DiscreteNodeLabelModel.SOUTH;
import static com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.TransformationGraphPresentationModel.MISSING_ENTRY;

public class StepNode implements Node {
    private final Step step;

    public StepNode(Step step) {
        this.step = step;
    }

    @Override public String getName() {
        return step.getName();
    }

    @Override public NodeRealizer getRealizer(TransformationGraphPresentationModel presentationModel) {
        String type = step.getType().getValue();
        String name = step.getName();
        GraphManager graphManager = presentationModel.getGraphManager();
        ImageNodeRealizer realizer = graphManager.createImageNodeRealizer();
        realizer.setWidth(getWidth());
        realizer.setHeight(getHeight());
        realizer.setX(getX());
        realizer.setY(getY());

        NodeLabel nodeLabel = createNodeLabel(name, SOUTH, realizer, graphManager);
        realizer.setLabel(nodeLabel);
        realizer.addLabel(createNodeLabel(type, DiscreteNodeLabelModel.NORTH, realizer, graphManager));
        realizer.setTransparent(false);
        presentationModel.getImage(type)
                .ifPresentOrElse(realizer::setImage,
                        () -> realizer.setImage(MISSING_ENTRY));

        return realizer;
    }

    @Override public int getHeight() {
        return 32;
    }

    @Override public int getWidth() {
        return 32;
    }

    @Override public int getX() {
        return step.getGUI().getXloc().getValue();
    }

    @Override public int getY() {
        return step.getGUI().getYloc().getValue();
    }

    @Override public String getTooltip() {
        return step.getType().getValue();
    }

    @Override public DefaultActionGroup getActionGroup() {
        return new DefaultActionGroup();
    }

    @NotNull private NodeLabel createNodeLabel(String name, int position, NodeRealizer nodeRealizer, GraphManager graphManager) {
        NodeLabel nodeLabel = graphManager.createNodeLabel(name);
        Color color = UIUtil.getLabelFontColor(UIUtil.FontColor.NORMAL);
        nodeLabel.setTextColor(color);
        DiscreteNodeLabelModel discreteNodeLabelModel = graphManager.createDiscreteNodeLabelModel(position);
        nodeLabel.setLabelModel(discreteNodeLabelModel);
        Object modelParameter = discreteNodeLabelModel.createModelParameter(nodeLabel.getTextBox(), nodeRealizer);
        nodeLabel.setModelParameter(modelParameter);
        return nodeLabel;
    }

}
