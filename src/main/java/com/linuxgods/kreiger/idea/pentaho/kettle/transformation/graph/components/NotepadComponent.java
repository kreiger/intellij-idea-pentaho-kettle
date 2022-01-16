package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.components;

import com.linuxgods.kreiger.idea.pentaho.kettle.transformation.dom.Notepad;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class NotepadComponent extends JTextArea {

    public NotepadComponent(Notepad notepad) {
        setBackground(notepad.getBackgroundColor());
        setForeground(notepad.getForegroundColor());
        setFont(notepad.getFont());
        setText(notepad.getNote().getValue());
        setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(4,4,4,4)));
        setBounds(notepad.getBounds());
    }
}
