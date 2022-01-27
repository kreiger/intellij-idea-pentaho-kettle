package com.linuxgods.kreiger.idea.pentaho.kettle.graph.components;

import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Notepad;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

public class NotepadComponent extends JTextArea {

    private Notepad notepad;

    public NotepadComponent(Notepad notepad) {
        this.notepad = notepad;
        setEditable(false);
        setBackground(notepad.getBackgroundColor());
        setForeground(notepad.getForegroundColor());
        Font font = notepad.getFont();
        setFont(font);
        String text = notepad.getNote().getValue();
        setText(text);
        CompoundBorder border = new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(4, 4, 4, 4));
        setBorder(border);
        int borderWidth = 10;

        int newlineCount = StringUtils.countMatches(text, "\n");
        TextLayout textLayout = new TextLayout(text, font, new FontRenderContext(null, false, true));
        Rectangle2D stringBounds = textLayout.getBounds();
        int metaWidth = Objects.requireNonNullElse(notepad.getWidth().getValue(), 20);
        int metaHeight = Objects.requireNonNullElse(notepad.getHeigth().getValue(), 20);
        //int width = Integer.max(metaWidth, (int) stringBounds.getWidth());
        int width = metaWidth;
        int height = Integer.max(metaHeight, (int) stringBounds.getHeight());
        setBounds(new Rectangle(notepad.getXloc().getValue(), notepad.getYloc().getValue(),
                (int) (width + borderWidth), (int) (height+ borderWidth)));
    }

    @Override
    protected void paintBorder(Graphics g) {
        super.paintBorder(g);
        g.setColor(getParent().getBackground());
        Polygon corner = new Polygon(
                new int[]{getWidth()-16, getWidth(), getWidth()},
                new int[]{getHeight(), getHeight()-16, getHeight()},
                3);
        g.fillPolygon(corner);
        g.setColor(Color.BLACK);
        Polygon fold = new Polygon(
                new int[] {getWidth()-16,getWidth()-16,getWidth()},
                new int[] {getHeight(), getHeight()-16,getHeight()-16},
                3);
        g.drawPolygon(fold);
        //g.drawRect(5,5, notepad.getWidth().getValue(), notepad.getHeigth().getValue());
    }
}
