package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.components;

import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.Node;

import javax.swing.*;
import java.awt.*;

public class NodeComponent<T> extends JLabel {
    private final Node<T> node;

    public NodeComponent(Node<T> node) {
        super(node.getName(), node.getIcon(), SwingConstants.CENTER);
        this.node = node;
        setVerticalTextPosition(JLabel.BOTTOM);
        setHorizontalTextPosition(JLabel.CENTER);
        Dimension preferredSize = getPreferredSize();

        int x = node.getX() - preferredSize.width / 2 + getIcon().getIconWidth() / 2;
        setBounds(x, node.getY(), preferredSize.width, preferredSize.height);
    }

    Point getIconCenter() {
        return new Point(getX() + getWidth() / 2, getY() + getIcon().getIconHeight() / 2);
    }

    public Node<T> getNode() {
        return node;
    }
}
