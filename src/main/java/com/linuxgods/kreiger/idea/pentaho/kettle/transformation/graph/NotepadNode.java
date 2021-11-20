package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.graph.view.GenericNodeRealizer;
import com.intellij.openapi.graph.view.NodeLabel;
import com.intellij.openapi.graph.view.NodeRealizer;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Notepad;

import java.awt.*;

import static com.intellij.openapi.graph.view.Graph2DView.BG_LAYER;

public class NotepadNode implements Node {
    private final Notepad notepad;

    public NotepadNode(Notepad notepad) {
        this.notepad = notepad;
    }

    @Override public String getName() {
        return notepad.getNote().getValue();
    }

    @Override public NodeRealizer getRealizer(TransformationGraphPresentationModel presentationModel) {
        GenericNodeRealizer realizer = presentationModel.getGraphManager().createGenericNodeRealizer();
        realizer.setWidth(getWidth());
        realizer.setHeight(getHeight());
        realizer.setX(getX());
        realizer.setY(getY());
        realizer.setLayer(BG_LAYER);

        NodeLabel nodeLabel = realizer.createNodeLabel();
        Color fontColor = new Color(notepad.getFontcolorred().getValue(), notepad.getFontcolorgreen().getValue(), notepad.getFontcolorblue().getValue());
        try {
            nodeLabel.setFont(Font.getFont(notepad.getFontname().getValue()));
        } catch (Exception ignored) {}
        nodeLabel.setTextColor(fontColor);
        nodeLabel.setText(getName());
        realizer.setLabel(nodeLabel);
        Color backgroundColor = new Color(
                notepad.getBackgroundcolorred().getValue(),
                notepad.getBackgroundcolorgreen().getValue(),
                notepad.getBackgroundcolorblue().getValue()
                );
        realizer.setFillColor(backgroundColor);
        return realizer;
    }

    @Override public int getHeight() {
        return notepad.getHeigth().getValue();
    }

    @Override public int getWidth() {
        return notepad.getWidth().getValue();
    }

    @Override public int getX() {
        return notepad.getXloc().getValue();
    }

    @Override public int getY() {
        return notepad.getYloc().getValue();
    }

    @Override public String getTooltip() {
        return null;
    }

    @Override public DefaultActionGroup getActionGroup() {
        return new DefaultActionGroup();
    }
}
