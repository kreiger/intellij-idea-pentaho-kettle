package com.linuxgods.kreiger.idea.pentaho.kettle.graph;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;

public interface Notepad extends DomElement {

    Color DEFAULT_NOTEPAD_BACKGROUND_COLOR = new Color(0xFF, 0xCD, 0x70);

    Note getNote();
    GenericDomValue<Integer> getXloc();
    GenericDomValue<Integer> getYloc();
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
    GenericDomValue<String> getFontbold();
    GenericDomValue<String> getFontitalic();

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
                (int) (getWidth().getValue()), (int) (getHeigth().getValue()));
    }

    default Font getFont() {
        String fontName = requireNonNullElse(getFontname().getValue(), Font.SANS_SERIF);
        int fontStyle = Font.PLAIN
                | ("Y".equals(getFontbold().getValue()) ? Font.BOLD : 0)
                | ("Y".equals(getFontitalic().getValue()) ? Font.ITALIC : 0) ;
        int fontSize = requireNonNullElse(getFontsize().getValue(), 11);
        if (fontSize <= 0) fontSize = 11;
        Font font = new Font(fontName, fontStyle, (int) (fontSize*1.5));
        System.out.println("Font: "+font);
        return font;
    }

    interface Note extends DomElement {
        String getValue();
    }
}
