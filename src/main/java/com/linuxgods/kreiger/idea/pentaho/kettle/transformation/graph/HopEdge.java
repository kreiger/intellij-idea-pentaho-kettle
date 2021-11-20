package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph;

import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Hop;

public class HopEdge implements Edge {
    private final Hop hop;

    public HopEdge(Hop hop) {
        this.hop = hop;
    }

    @Override public DefaultActionGroup getActionGroup() {
        return new DefaultActionGroup();
    }

    @Override public String getFrom() {
        return hop.getFrom().getValue().getName();
    }

    @Override public String getTo() {
        return hop.getTo().getValue().getName();
    }
}
