package com.linuxgods.kreiger.idea.pentaho.kettle.graph;

import com.intellij.openapi.actionSystem.DefaultActionGroup;

public interface Edge {
    DefaultActionGroup getActionGroup();

    String getFrom();

    String getTo();
}
