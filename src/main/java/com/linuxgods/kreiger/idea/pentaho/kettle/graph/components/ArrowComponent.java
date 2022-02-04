package com.linuxgods.kreiger.idea.pentaho.kettle.graph.components;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.GeometryUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.KettleIcons;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.Arrow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class ArrowComponent<T> extends JComponent {
    private Point fromPoint;
    private Point toPoint;
    private Arrow<T> arrow;

    public ArrowComponent(NodeComponent<T> from, NodeComponent<T> to, Arrow<T> arrow) {
        this.arrow = arrow;
        update(from, to);
        ComponentAdapter moveListener = new ComponentAdapter() {
            @Override public void componentMoved(ComponentEvent e) {
                update(from, to);
            }
        };
        from.addComponentListener(moveListener);
        to.addComponentListener(moveListener);
    }

    private void update(NodeComponent<T> from, NodeComponent<T> to) {
        fromPoint = from.getIconCenter();
        toPoint = to.getIconCenter();
        Rectangle bounds = createBounds();
        setBounds(bounds);
    }

    @NotNull public Rectangle createBounds() {
        return new Rectangle((int) min(fromPoint.getX(), toPoint.getX()) - 16, (int) min(fromPoint.getY(), toPoint.getY()) - 16,
                (int) max(fromPoint.getX(), toPoint.getX()) + 16, (int) max(fromPoint.getY(), toPoint.getY()) + 16);
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(arrow.getColor());
        g2.setStroke(new BasicStroke(2));
        Line2D line = getLine();
        g2.draw(line);
        double centerX = line.getBounds2D().getCenterX();
        double centerY = line.getBounds2D().getCenterY();
        Shape arrow = GeometryUtil.getArrowShape(line, new Point2D.Double(centerX, centerY));
        g2.fill(arrow);
        this.arrow.getIcon().ifPresent(icon ->
                icon.paintIcon(this, g2,
                        (int) centerX - icon.getIconWidth() / 2,
                        (int) centerY - icon.getIconHeight() / 2));
    }

    @NotNull private Line2D getLine() {
        return new Line2D.Float(fromPoint.x - getX(), fromPoint.y - getY(), toPoint.x - getX(), toPoint.y - getY());
    }

    @Override public boolean contains(int x, int y) {
        return getLine().ptLineDist(x, y) < 6;
    }
}
