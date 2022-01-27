package com.linuxgods.kreiger.idea.pentaho.kettle.graph;

import javax.swing.*;

public interface Node<T> {
    String getName();
    Icon getIcon();
    int getX();
    int getY();
    T getValue();
}
