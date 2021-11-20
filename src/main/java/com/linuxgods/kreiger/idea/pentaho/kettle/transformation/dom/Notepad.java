package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;

public interface Notepad extends GUI {
    Note getNote();
    Coordinate getXloc();
    Coordinate getYloc();
    GenericDomValue<Integer> getWidth();
    GenericDomValue<Integer> getHeigth(); // sic

    GenericDomValue<Integer> getFontcolorred();
    GenericDomValue<Integer> getFontcolorgreen();
    GenericDomValue<Integer> getFontcolorblue();

    GenericDomValue<Integer> getBackgroundcolorred();
    GenericDomValue<Integer> getBackgroundcolorgreen();
    GenericDomValue<Integer> getBackgroundcolorblue();

    GenericDomValue<String> getFontname();

    GenericDomValue<Integer> getFontsize();

    interface Note extends DomElement {
        String getValue();
    }
}
