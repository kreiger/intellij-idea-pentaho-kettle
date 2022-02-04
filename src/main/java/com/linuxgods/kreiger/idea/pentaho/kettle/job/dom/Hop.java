package com.linuxgods.kreiger.idea.pentaho.kettle.job.dom;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;

import javax.swing.*;
import java.awt.*;

import static com.linuxgods.kreiger.idea.pentaho.kettle.KettleIcons.*;
import static com.linuxgods.kreiger.idea.pentaho.kettle.graph.Arrow.*;

public interface Hop extends DomElement {

    @Required
    GenericDomValue<Entry> getFrom();
    @Required
    GenericDomValue<Entry> getTo();

    @Required
    GenericDomValue<String> getEnabled();

    @Required
    GenericDomValue<String> getEvaluation();

    @Required
    GenericDomValue<String> getUnconditional();

    default boolean isEvaluation() {
        return "Y".equals(getEvaluation().getStringValue());
    }

    default boolean isUnconditional() {
        return "Y".equals(getUnconditional().getStringValue());
    }

    default Color getColor() {
        return isUnconditional() ? DEFAULT_COLOR : isEvaluation() ? TRUE_COLOR : FALSE_COLOR;
    }

    default Icon getIcon() {
        return isUnconditional() ? UNCONDITIONAL : isEvaluation() ? TRUE : FALSE;
    }
}
