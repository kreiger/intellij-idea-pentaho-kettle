package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.components;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.GeometryUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ArrowComponent extends JComponent {
    private Point fromPoint;
    private Point toPoint;

    public ArrowComponent(StepComponent from, StepComponent to) {
        update(from, to);
        ComponentAdapter moveListener = new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent e) {
                update(from, to);
            }
        };
        from.addComponentListener(moveListener);
        to.addComponentListener(moveListener);
    }

    private void update(StepComponent from, StepComponent to) {
        fromPoint = from.getIconCenter();
        toPoint = to.getIconCenter();
        Rectangle bounds = createBounds();
        setBounds(bounds);
    }

    @NotNull public Rectangle createBounds() {
        return new Rectangle((int) min(fromPoint.getX(), toPoint.getX())-16, (int) min(fromPoint.getY(), toPoint.getY()) - 16,
                (int) max(fromPoint.getX(), toPoint.getX()) + 16, (int) max(fromPoint.getY(), toPoint.getY()) + 16);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(JBColor.GRAY);
        g2.setStroke(new BasicStroke(2));
        Line2D line = getLine();
        g2.draw(line);
        double centerX = line.getBounds2D().getCenterX();
        double centerY = line.getBounds2D().getCenterY();
        Shape arrow = GeometryUtil.getArrowShape(line, new Point2D.Double(centerX, centerY));
        g2.fill(arrow);

    }

    @NotNull private Line2D getLine() {
        return new Line2D.Float(fromPoint.x - getX(), fromPoint.y - getY(), toPoint.x - getX(), toPoint.y - getY());
    }

    @Override public boolean contains(int x, int y) {
        return getLine().ptLineDist(x, y) < 6;
    }
}
