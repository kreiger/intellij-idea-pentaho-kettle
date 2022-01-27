package com.linuxgods.kreiger.idea.pentaho.kettle.job;

import com.linuxgods.kreiger.idea.pentaho.kettle.ImageUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.job.dom.Entry;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.Node;

import javax.swing.*;

public class EntryNode implements Node<Entry> {
    private final Entry entry;

    public EntryNode(Entry entry) {
        this.entry = entry;
    }

    @Override public String getName() {
        return entry.getNameUntrimmed();
    }

    @Override public Icon getIcon() {
        return entry.getIcon().orElse(ImageUtil.MISSING_ENTRY_ICON);
    }

    @Override public int getX() {
        return entry.getXloc().getValue();
    }

    @Override public int getY() {
        return entry.getYloc().getValue();
    }

    @Override public Entry getValue() {
        return entry;
    }
}
