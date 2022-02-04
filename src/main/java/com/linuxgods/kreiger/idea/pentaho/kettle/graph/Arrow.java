package com.linuxgods.kreiger.idea.pentaho.kettle.graph;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

public interface Arrow<T> {
    Color DEFAULT_COLOR = new Color(61, 99, 128);
    Color TRUE_COLOR = new Color(12, 178, 15);
    Color FALSE_COLOR = Color.RED;

    Color getColor();

    Optional<Icon> getIcon();
}
