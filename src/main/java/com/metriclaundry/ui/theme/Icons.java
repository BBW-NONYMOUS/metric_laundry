package com.metriclaundry.ui.theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Vector icons painted with Java2D on a 24x24 grid - no image files needed,
 * crisp at any size and any colour.  Usage:  Icons.of("home", 20, Theme.PRIMARY)
 */
public final class Icons {

    private Icons() { }

    public static Icon of(String name, int size, Color color) { return new VectorIcon(name, size, color, 2f); }

    public static Icon of(String name, int size, Color color, float stroke) { return new VectorIcon(name, size, color, stroke); }

    private static final class VectorIcon implements Icon {
        private final String name;
        private final int size;
        private final Color color;
        private final float stroke;

        VectorIcon(String name, int size, Color color, float stroke) {
            this.name = name; this.size = size; this.color = color; this.stroke = stroke;
        }

        @Override public int getIconWidth() { return size; }
        @Override public int getIconHeight() { return size; }

        @Override public void paintIcon(Component c, Graphics g0, int x, int y) {
            Graphics2D g = (Graphics2D) g0.create();
            Theme.antialias(g);
            g.translate(x, y);
            g.scale(size / 24.0, size / 24.0);
            g.setColor(color);
            g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            draw(g, name);
            g.dispose();
        }
    }

    private static Path2D poly(double... pts) {
        Path2D p = new Path2D.Double();
        p.moveTo(pts[0], pts[1]);
        for (int i = 2; i < pts.length; i += 2) p.lineTo(pts[i], pts[i + 1]);
        return p;
    }

    private static void circle(Graphics2D g, double cx, double cy, double r, boolean fill) {
        Ellipse2D e = new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
        if (fill) g.fill(e); else g.draw(e);
    }

    private static void line(Graphics2D g, double x1, double y1, double x2, double y2) {
        g.draw(new Line2D.Double(x1, y1, x2, y2));
    }

    static void draw(Graphics2D g, String name) {
        switch (name) {
            case "home": {
                g.draw(poly(3, 11.5, 12, 3.5, 21, 11.5));
                g.draw(poly(5.5, 9.5, 5.5, 20.5, 18.5, 20.5, 18.5, 9.5));
                g.draw(poly(10, 20.5, 10, 14.5, 14, 14.5, 14, 20.5));
                break;
            }
            case "cart": {
                g.draw(poly(2, 3.5, 5, 3.5, 7.5, 15, 18.5, 15, 21, 7, 6, 7));
                circle(g, 9, 19.5, 1.6, true);
                circle(g, 17, 19.5, 1.6, true);
                break;
            }
            case "search": {
                circle(g, 10.5, 10.5, 6.5, false);
                line(g, 15.5, 15.5, 20.5, 20.5);
                break;
            }
            case "card": {
                g.draw(new RoundRectangle2D.Double(2.5, 5, 19, 14, 4, 4));
                line(g, 2.5, 9.5, 21.5, 9.5);
                line(g, 6, 15, 10, 15);
                break;
            }
            case "clock":
            case "history": {
                circle(g, 12, 12, 9, false);
                g.draw(poly(12, 7, 12, 12, 15.5, 14));
                break;
            }
            case "info": {
                circle(g, 12, 12, 9, false);
                line(g, 12, 11, 12, 16.5);
                circle(g, 12, 7.8, 0.9, true);
                break;
            }
            case "logout": {
                g.draw(poly(10, 4, 5, 4, 5, 20, 10, 20));
                line(g, 10, 12, 20.5, 12);
                g.draw(poly(16.5, 8, 20.5, 12, 16.5, 16));
                break;
            }
            case "user": {
                circle(g, 12, 8, 4, false);
                Path2D p = new Path2D.Double();
                p.moveTo(4.5, 20.5);
                p.curveTo(4.5, 15, 8, 13.5, 12, 13.5);
                p.curveTo(16, 13.5, 19.5, 15, 19.5, 20.5);
                g.draw(p);
                break;
            }
            case "users": {
                circle(g, 9, 8, 3.5, false);
                Path2D p = new Path2D.Double();
                p.moveTo(2.5, 20);
                p.curveTo(2.5, 15, 5.5, 13.5, 9, 13.5);
                p.curveTo(12.5, 13.5, 15.5, 15, 15.5, 20);
                g.draw(p);
                circle(g, 17, 8.5, 2.6, false);
                Path2D q = new Path2D.Double();
                q.moveTo(17.5, 13.5);
                q.curveTo(20, 13.8, 21.5, 15.5, 21.5, 19);
                g.draw(q);
                break;
            }
            case "orders": {
                g.draw(new RoundRectangle2D.Double(5, 4, 14, 17, 3, 3));
                g.draw(new RoundRectangle2D.Double(9, 2.5, 6, 3, 1.5, 1.5));
                line(g, 8.5, 10, 15.5, 10);
                line(g, 8.5, 13.5, 15.5, 13.5);
                line(g, 8.5, 17, 12.5, 17);
                break;
            }
            case "shirt": {
                Path2D p = new Path2D.Double();
                p.moveTo(8.5, 3.5);
                p.lineTo(3, 6.5);
                p.lineTo(5, 11);
                p.lineTo(7, 10);
                p.lineTo(7, 21);
                p.lineTo(17, 21);
                p.lineTo(17, 10);
                p.lineTo(19, 11);
                p.lineTo(21, 6.5);
                p.lineTo(15.5, 3.5);
                p.quadTo(12, 7.5, 8.5, 3.5);
                g.draw(p);
                break;
            }
            case "peso": {
                circle(g, 12, 12, 9.5, false);
                line(g, 10, 6.5, 10, 17.5);
                Path2D p = new Path2D.Double();
                p.moveTo(10, 6.5);
                p.lineTo(13, 6.5);
                p.curveTo(16.5, 6.5, 16.5, 12.5, 13, 12.5);
                p.lineTo(10, 12.5);
                g.draw(p);
                g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                line(g, 7.5, 8.6, 16.5, 8.6);
                line(g, 7.5, 10.6, 16.5, 10.6);
                break;
            }
            case "chart": {
                g.draw(poly(4, 4, 4, 20, 20.5, 20));
                g.draw(poly(7.5, 15.5, 11, 11.5, 14, 13.5, 19.5, 7.5));
                break;
            }
            case "gear": {
                circle(g, 12, 12, 3, false);
                circle(g, 12, 12, 7, false);
                for (int i = 0; i < 8; i++) {
                    double a = Math.PI / 4 * i;
                    line(g, 12 + Math.cos(a) * 7, 12 + Math.sin(a) * 7, 12 + Math.cos(a) * 9.5, 12 + Math.sin(a) * 9.5);
                }
                break;
            }
            case "bell": {
                Path2D p = new Path2D.Double();
                p.moveTo(6.5, 17);
                p.lineTo(6.5, 11);
                p.curveTo(6.5, 7, 9, 4.5, 12, 4.5);
                p.curveTo(15, 4.5, 17.5, 7, 17.5, 11);
                p.lineTo(17.5, 17);
                g.draw(p);
                line(g, 4.5, 17, 19.5, 17);
                g.draw(new Arc2D.Double(10, 17.5, 4, 3.5, 180, 180, Arc2D.OPEN));
                break;
            }
            case "check": {
                g.draw(poly(5, 12.5, 10, 17.5, 19.5, 7));
                break;
            }
            case "washer": {
                g.draw(new RoundRectangle2D.Double(4, 2.5, 16, 19, 4, 4));
                line(g, 4, 7, 20, 7);
                circle(g, 7.5, 4.8, 0.8, true);
                circle(g, 10.3, 4.8, 0.8, true);
                circle(g, 12, 14.3, 4.8, false);
                g.draw(new Arc2D.Double(9.2, 11.5, 5.6, 5.6, 200, 140, Arc2D.OPEN));
                break;
            }
            case "stack": {
                g.draw(new RoundRectangle2D.Double(3, 15, 18, 5, 3, 3));
                g.draw(new RoundRectangle2D.Double(4, 10, 16, 5, 3, 3));
                g.draw(new RoundRectangle2D.Double(5, 5, 14, 5, 3, 3));
                break;
            }
            case "dry": {
                for (int i = 0; i < 3; i++) {
                    double x = 7 + i * 5;
                    Path2D p = new Path2D.Double();
                    p.moveTo(x, 4);
                    p.curveTo(x - 2.5, 7, x + 2.5, 9, x, 12);
                    p.curveTo(x - 2.5, 15, x + 2.5, 17, x, 20);
                    g.draw(p);
                }
                break;
            }
            case "bag": {
                g.draw(new RoundRectangle2D.Double(4.5, 8, 15, 13, 3, 3));
                g.draw(new Arc2D.Double(8.5, 3, 7, 9, 0, 180, Arc2D.OPEN));
                break;
            }
            case "lock": {
                g.draw(new RoundRectangle2D.Double(5, 10.5, 14, 10.5, 3, 3));
                g.draw(new Arc2D.Double(8, 3.5, 8, 13, 0, 180, Arc2D.OPEN));
                circle(g, 12, 15.5, 1.2, true);
                break;
            }
            case "plus": {
                line(g, 12, 5, 12, 19);
                line(g, 5, 12, 19, 12);
                break;
            }
            case "close": {
                line(g, 6, 6, 18, 18);
                line(g, 18, 6, 6, 18);
                break;
            }
            case "edit": {
                Path2D p = poly(4, 20, 4.5, 16, 15.5, 5, 19, 8.5, 8, 19.5);
                p.closePath();
                g.draw(p);
                line(g, 13.5, 7, 17, 10.5);
                break;
            }
            case "trash": {
                line(g, 4, 6.5, 20, 6.5);
                g.draw(poly(9, 6.5, 9, 3.5, 15, 3.5, 15, 6.5));
                g.draw(poly(6, 6.5, 7, 20.5, 17, 20.5, 18, 6.5));
                line(g, 10, 10.5, 10, 16.5);
                line(g, 14, 10.5, 14, 16.5);
                break;
            }
            case "eye": {
                Path2D p = new Path2D.Double();
                p.moveTo(2, 12);
                p.quadTo(12, 2, 22, 12);
                p.quadTo(12, 22, 2, 12);
                g.draw(p);
                circle(g, 12, 12, 3, false);
                break;
            }
            case "print": {
                g.draw(poly(7, 9, 7, 3.5, 17, 3.5, 17, 9));
                g.draw(new RoundRectangle2D.Double(3, 9, 18, 8, 3, 3));
                g.draw(new Rectangle2D.Double(7, 14, 10, 7));
                break;
            }
            case "download": {
                line(g, 12, 3.5, 12, 15);
                g.draw(poly(7, 10, 12, 15, 17, 10));
                line(g, 4.5, 20, 19.5, 20);
                break;
            }
            case "arrowLeft": {
                line(g, 19.5, 12, 5, 12);
                g.draw(poly(11, 6, 5, 12, 11, 18));
                break;
            }
            case "arrowRight":
            case "chevron": {
                if (name.equals("arrowRight")) line(g, 4.5, 12, 19, 12);
                g.draw(name.equals("arrowRight") ? poly(13, 6, 19, 12, 13, 18) : poly(9, 5.5, 15.5, 12, 9, 18.5));
                break;
            }
            case "droplet": {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 3);
                p.curveTo(9, 7, 5.5, 11, 5.5, 15);
                p.curveTo(5.5, 19, 8.5, 21.5, 12, 21.5);
                p.curveTo(15.5, 21.5, 18.5, 19, 18.5, 15);
                p.curveTo(18.5, 11, 15, 7, 12, 3);
                g.fill(p);
                break;
            }
            case "heart": {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 20.5);
                p.curveTo(4, 15, 2.5, 11, 2.5, 8.5);
                p.curveTo(2.5, 5.5, 5, 3.5, 7.5, 3.5);
                p.curveTo(9.5, 3.5, 11, 4.5, 12, 6.5);
                p.curveTo(13, 4.5, 14.5, 3.5, 16.5, 3.5);
                p.curveTo(19, 3.5, 21.5, 5.5, 21.5, 8.5);
                p.curveTo(21.5, 11, 20, 15, 12, 20.5);
                g.fill(p);
                break;
            }
            case "receipt": {
                Path2D p = poly(5, 2.5, 19, 2.5, 19, 21.5, 16.5, 20, 14, 21.5, 11.5, 20, 9, 21.5, 7, 20, 5, 21.5);
                p.closePath();
                g.draw(p);
                line(g, 8.5, 8, 15.5, 8);
                line(g, 8.5, 12, 15.5, 12);
                line(g, 8.5, 16, 12.5, 16);
                break;
            }
            case "cash": {
                g.draw(new RoundRectangle2D.Double(2.5, 6, 19, 12, 3, 3));
                circle(g, 12, 12, 2.8, false);
                circle(g, 6, 12, 0.8, true);
                circle(g, 18, 12, 0.8, true);
                break;
            }
            case "refresh": {
                g.draw(new Arc2D.Double(4.5, 4.5, 15, 15, 60, 280, Arc2D.OPEN));
                g.draw(poly(15.5, 2.5, 16.2, 6.3, 12.5, 7.2));
                break;
            }
            case "phone": {
                g.draw(new RoundRectangle2D.Double(7, 2.5, 10, 19, 3, 3));
                line(g, 11, 18.5, 13, 18.5);
                break;
            }
            case "pin": {
                Path2D p = new Path2D.Double();
                p.moveTo(12, 21.5);
                p.curveTo(7, 15.5, 5, 12.5, 5, 9.5);
                p.curveTo(5, 5.5, 8, 2.5, 12, 2.5);
                p.curveTo(16, 2.5, 19, 5.5, 19, 9.5);
                p.curveTo(19, 12.5, 17, 15.5, 12, 21.5);
                g.draw(p);
                circle(g, 12, 9.5, 2.5, false);
                break;
            }
            case "mail": {
                g.draw(new RoundRectangle2D.Double(2.5, 5, 19, 14, 3, 3));
                g.draw(poly(3, 6, 12, 13, 21, 6));
                break;
            }
            case "hanger": {
                drawHanger(g);
                break;
            }
            default: {
                circle(g, 12, 12, 8, false);
            }
        }
    }

    /** Hanger outline used by the logo (24 grid). */
    static void drawHanger(Graphics2D g) {
        Path2D hook = new Path2D.Double();
        hook.moveTo(9.6, 5.2);
        hook.curveTo(9.6, 3.2, 11, 2.2, 12.2, 2.2);
        hook.curveTo(13.6, 2.2, 14.6, 3.2, 14.6, 4.5);
        hook.curveTo(14.6, 6, 12, 6.3, 12, 8);
        g.draw(hook);
        Path2D body = poly(12, 8, 2, 16.5, 22, 16.5);
        body.closePath();
        g.draw(body);
    }
}
