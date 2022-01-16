package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

public interface Notepad extends GUI {

    Color DEFAULT_NOTEPAD_BACKGROUND_COLOR = new Color(0xFF, 0xCD, 0x70);

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

    default Color getBackgroundColor() {
        try {
            return new Color(getBackgroundcolorred().getValue(), getBackgroundcolorgreen().getValue(), getBackgroundcolorblue().getValue());
        } catch (NullPointerException e) {
            return DEFAULT_NOTEPAD_BACKGROUND_COLOR;
        }
    }

    default Color getForegroundColor() {
        try {
            return new Color(getFontcolorred().getValue(), getFontcolorgreen().getValue(), getFontcolorblue().getValue());
        } catch (NullPointerException e) {
            return Color.BLACK;
        }
    }

    @NotNull default Rectangle getBounds() {
        return new Rectangle(getXloc().getValue(), getYloc().getValue(),
                (int) (getWidth().getValue()*1.5), (int) (getHeigth().getValue()*1.5));
    }

    default Font getFont() {
        try {
            return new Font(getFontname().getValue(), Font.PLAIN, (int) (getFontsize().getValue()*1.5));
        } catch (NullPointerException e) {
            return new Font("Cantarell", Font.PLAIN, 16);
        }
    }

    interface Note extends DomElement {
        String getValue();
    }
}
