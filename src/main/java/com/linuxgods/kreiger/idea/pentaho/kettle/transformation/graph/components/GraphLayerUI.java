package com.linuxgods.kreiger.idea.pentaho.kettle.transformation.graph.components;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.function.Predicate;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;

public class GraphLayerUI extends LayerUI<JComponent> {
    double zoom = 1;

    private final Point center = new Point(0, 0);
    private AffineTransform transform;


    {
        setTransforms(zoom, center);
    }


    @Override public void installUI(JComponent c) {
        super.installUI(c);
        JLayer layer = (JLayer<JComponent>) c;
        layer.setLayerEventMask(AWTEvent.MOUSE_WHEEL_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
        Component view = layer.getView();


    }

    @Override public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        JLayer layer = (JLayer) c;
        layer.setLayerEventMask(0);
    }

    private Point inverseTransform(Point oldPoint) {
        try {
            return (Point) transform.inverseTransform(oldPoint, new Point());
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }

    @Override protected void processMouseEvent(MouseEvent e, JLayer<? extends JComponent> l) {
        if (e instanceof MyMouseEvent) return;
        e.consume();
        if (e.getID() == MouseEvent.MOUSE_ENTERED || e.getID() == MouseEvent.MOUSE_EXITED) {
            return;
        }

        Point viewOnScreen = l.getView().getLocationOnScreen();
        Point mouseOnScreen = e.getLocationOnScreen();
        int x = mouseOnScreen.x - viewOnScreen.x;
        int y = mouseOnScreen.y - viewOnScreen.y;
        Point oldMouseOnView = new Point(x, y);
        Point newMouseOnView = inverseTransform(oldMouseOnView);
        //System.out.println("event: "+oldMouseOnView+","+newMouseOnView+": "+e);

        Component target = getMouseEventTarget(l.getView(), newMouseOnView.x, newMouseOnView.y,
                comp -> comp.getMouseListeners().length > 0 || comp.getMouseMotionListeners().length > 0);
        if (null != target) {
            for(Component component = target; component != l.getView(); component = component.getParent()) {
                x -= component.getX();
                y -= component.getY();
            }
            MouseEvent newEvent = new MyMouseEvent(target, e, x, y, viewOnScreen, newMouseOnView);
            target.dispatchEvent(newEvent);
        }
    }

    @Override protected void processMouseMotionEvent(MouseEvent e, JLayer<? extends JComponent> l) {
        processMouseEvent(e, l);
    }

    @Override protected void processMouseWheelEvent(MouseWheelEvent e, JLayer<? extends JComponent> l) {
        zoom = Math.min(Math.max(0.5, zoom - e.getPreciseWheelRotation() / 10), 4);
        setTransforms(zoom, center);
        e.consume();
        l.repaint();
    }

    @Override public void paint(Graphics g, JComponent c) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2.drawString(Math.round(zoom * 100) + "%", 20, 20);
        AffineTransform transform = g2.getTransform();
        transform.concatenate(this.transform);
        g2.setTransform(transform);
        super.paint(g, c);
    }

    @Override public void paintImmediately(int x, int y, int width, int height, JLayer<? extends JComponent> l) {
        Point oldPoint = new Point(x, y);
        Point newPoint = (Point) transform.transform(oldPoint, new Point());
        Point oldSize = new Point(width, height);
        Point newSize = (Point) transform.deltaTransform(oldSize, new Point());
        //System.out.println("paintImmediately: "+newPoint+" "+newSize);
        //super.paintImmediately(newPoint.x, newPoint.y, newSize.x, newSize.y, l);
        super.paintImmediately(0, 0, l.getWidth(), l.getHeight(), l);
    }

    private void setTransforms(double zoom, Point center) {
        AffineTransform at = new AffineTransform();
        //at.translate(getWidth() / 2.0, getHeight() / 2.0);
        at.scale(zoom, zoom);
        //at.translate(-getWidth() / 2.0, -getHeight() / 2.0);
        at.translate(center.x, center.y);
        this.transform = at;
    }

    Component getMouseEventTarget(Container c, int x, int y, Predicate<Component> filter) {
        synchronized (c.getTreeLock()) {
            for (Component comp : c.getComponents()) {
                if (comp != null && comp.isVisible() &&
                        comp.contains(x - comp.getX(), y - comp.getY())) {

                    if (comp instanceof Container) {
                        Container child = (Container) comp;
                        Component deeper = getMouseEventTarget(
                                child,
                                x - child.getX(),
                                y - child.getY(),
                                filter);
                        if (deeper != null) {
                            return deeper;
                        }
                    } else if (filter.test(comp)) {
                        // there isn't a deeper target, but this component
                        // is a target
                        return comp;
                    }
                }
            }
            boolean isMouseOver = c.contains(x,y);

            // didn't find a child target, return this component if it's
            // a possible target
            if (isMouseOver && filter.test(c)) {
                return c;
            }
            return null;
        }

    }

    private static class MyMouseEvent extends MouseEvent {
        public MyMouseEvent(Component target, MouseEvent e, int x, int y, Point viewOnScreen, Point newMouseOnView) {
            super(target, e.getID(), e.getWhen(), e.getModifiersEx() | e.getModifiers(), x, y, viewOnScreen.x + newMouseOnView.x, viewOnScreen.y + newMouseOnView.y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
        }
    }

}
