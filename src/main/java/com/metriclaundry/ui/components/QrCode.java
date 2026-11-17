package com.metriclaundry.ui.components;

import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Shows the shop's GCash QR code.  Put your real QR as "gcash_qr.png" next to the jar
 * (or in src/main/resources/images/gcash_qr.png) and it is used automatically;
 * otherwise a placeholder pattern is drawn.
 */
public class QrCode extends JComponent {
    /** No-argument constructor for the NetBeans GUI designer. */
    public QrCode() { this(200); }
    private Image image;

    public QrCode(int size) {
        setPreferredSize(new Dimension(size, size));
        try {
            File f = new File("gcash_qr.png");
            if (f.exists()) image = javax.imageio.ImageIO.read(f);
            else {
                java.net.URL url = getClass().getResource("/images/gcash_qr.png");
                if (url != null) image = javax.imageio.ImageIO.read(url);
            }
        } catch (Exception ignored) { }
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Theme.antialias(g);
        int s = Math.min(getWidth(), getHeight());
        int ox = (getWidth() - s) / 2, oy = (getHeight() - s) / 2;
        g.setColor(Color.WHITE);
        g.fillRoundRect(ox, oy, s, s, 12, 12);
        if (image != null) {
            g.drawImage(image, ox + 6, oy + 6, s - 12, s - 12, null);
            g.dispose();
            return;
        }
        int n = 25, pad = 8;
        double cell = (s - pad * 2.0) / n;
        java.util.Random rnd = new java.util.Random(20261109);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setColor(Color.BLACK);
        for (int y = 0; y < n; y++)
            for (int x = 0; x < n; x++)
                if (!finder(x, y, n) && rnd.nextBoolean())
                    g.fillRect((int) (ox + pad + x * cell), (int) (oy + pad + y * cell), (int) Math.ceil(cell), (int) Math.ceil(cell));
        int[][] corners = {{0, 0}, {n - 7, 0}, {0, n - 7}};
        for (int[] c : corners) {
            int x = (int) (ox + pad + c[0] * cell), y = (int) (oy + pad + c[1] * cell), w = (int) (cell * 7);
            g.fillRect(x, y, w, w);
            g.setColor(Color.WHITE);
            g.fillRect((int) (x + cell), (int) (y + cell), (int) (cell * 5), (int) (cell * 5));
            g.setColor(Color.BLACK);
            g.fillRect((int) (x + cell * 2), (int) (y + cell * 2), (int) (cell * 3), (int) (cell * 3));
        }
        g.dispose();
    }

    private static boolean finder(int x, int y, int n) {
        return (x < 8 && y < 8) || (x >= n - 8 && y < 8) || (x < 8 && y >= n - 8);
    }

}
