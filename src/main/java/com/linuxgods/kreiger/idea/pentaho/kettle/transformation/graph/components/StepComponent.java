package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.components;

import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Step;

import javax.swing.*;
import java.awt.*;

public class StepComponent extends JLabel {
    private final Step step;

    public StepComponent(Step step, Icon icon) {
        super(step.getNameUntrimmed(), icon, SwingConstants.CENTER);
        this.step = step;
        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
        Dimension preferredSize = getPreferredSize();

        int x = step.getX() - preferredSize.width / 2 + getIcon().getIconWidth() / 2;
        setBounds(x, step.getY(), preferredSize.width, preferredSize.height);
    }

    Point getIconCenter() {
        return new Point(getX() + getWidth() / 2, getY() + getIcon().getIconHeight() / 2);
    }

    public Step getStep() {
        return step;
    }
}
