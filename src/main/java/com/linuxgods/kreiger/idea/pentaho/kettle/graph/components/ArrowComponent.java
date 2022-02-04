package com.linuxgods.kreiger.idea.pentaho.kettle.graph.components;

import com.intellij.util.ui.GeometryUtil;
import com.linuxgods.kreiger.idea.pentaho.kettle.graph.Arrow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

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
        Line2D line = getLine();
        Rectangle2D bounds2D = line.getBounds2D();
        double width = bounds2D.getWidth();
        double height = bounds2D.getHeight();
        double length = Math.sqrt(width * width + height * height);
        g2.setStroke(new BasicStroke(1+(float) (Math.sqrt((width+height)/length))));
        g2.draw(line);

        Point2D iconPoint = lerp(line, 0.45F);
        Point2D arrowPoint = lerp(line, this.arrow.getIcon().isPresent() ? 0.65F : 0.55F);
        Shape arrow = GeometryUtil.getArrowShape(line, new Point2D.Double(arrowPoint.getX(), arrowPoint.getY()));
        g2.fill(arrow);
        this.arrow.getIcon().ifPresent(icon ->
                icon.paintIcon(this, g2,
                        (int) iconPoint.getX() - icon.getIconWidth() / 2,
                        (int) iconPoint.getY() - icon.getIconHeight() / 2));
    }

    @NotNull private Point2D lerp(Line2D line, float f) {
        Point2D p1 = line.getP1();
        Point2D p2 = line.getP2();
        return new Point2D.Double(p1.getX() + f * (p2.getX() - p1.getX()),
                p1.getY() + f * (p2.getY() - p1.getY()));
    }

    @NotNull private Line2D getLine() {
        return new Line2D.Float(fromPoint.x - getX(), fromPoint.y - getY(), toPoint.x - getX(), toPoint.y - getY());
    }

    @Override public boolean contains(int x, int y) {
        return getLine().ptLineDist(x, y) < 6;
    }
}
