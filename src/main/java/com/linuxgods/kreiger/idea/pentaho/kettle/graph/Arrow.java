package com.linuxgods.kreiger.idea.pentaho.kettle.graph;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public interface Arrow<T> {
    Color DEFAULT_COLOR = new JBColor(new Color(61, 99, 128), new Color( 135, 206, 250 ));
    Color TRUE_COLOR = new Color(12, 178, 15);
    Color FALSE_COLOR = Color.RED;

    Color getColor();

    Optional<Icon> getIcon();
}
