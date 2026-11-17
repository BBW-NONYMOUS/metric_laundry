package com.metriclaundry.ui.common;

import com.metriclaundry.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/** A screen inside a dashboard (Home, Manage Orders, ...). refresh() is called every time it is shown. */
public class Page extends JPanel {
    private final String title;
    private final String subtitle;
    protected DashboardFrame frame;

    /** Allows NetBeans to instantiate the screen superclass in Design view. */
    public Page() { this("", ""); }

    protected Page(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        setOpaque(false);
        setBorder(Theme.pad(4, 22, 18, 22));
    }

    public String title() { return title; }
    public String subtitle() { return subtitle; }
    void attach(DashboardFrame f) { this.frame = f; }

    /** Reload data from the database. */
    public void refresh() { }

    /** Runs a DB call off the UI thread, then updates the UI (keeps the window responsive). */
    protected <T> void load(java.util.function.Supplier<T> query, java.util.function.Consumer<T> ui) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() { return query.get(); }
            @Override protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try { ui.accept(get()); }
                catch (Exception e) {
                    com.metriclaundry.ui.components.Dialogs.error(Page.this, e.getCause() != null ? e.getCause() : e);
                }
            }
        }.execute();
    }
}
