package com.linuxgods.kreiger.idea.pentaho.kettle.graph;

import com.linuxgods.kreiger.idea.pentaho.kettle.ImageUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;

import javax.swing.*;

public class StepNode implements Node<Step> {
    private final Step step;

    public StepNode(Step step) {
        this.step = step;
    }

    @Override
    public String getName() {
        return step.getNameUntrimmed();
    }

    @Override
    public Icon getIcon() {
        return step.getIcon().orElse(ImageUtil.MISSING_ENTRY_ICON);
    }

    @Override
    public int getX() {
        return step.getX();
    }

    @Override
    public int getY() {
        return step.getY();
    }

    @Override
    public Step getValue() {
        return step;
    }
}
