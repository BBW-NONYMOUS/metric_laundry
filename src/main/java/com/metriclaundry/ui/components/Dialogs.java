package com.metriclaundry.ui.components;

import com.metriclaundry.util.AppException;

import javax.swing.*;
import java.awt.*;

/** Consistent message boxes. Business errors show their message; unexpected errors are logged. */
public final class Dialogs {
    private Dialogs() { }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Metric Laundry", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(Component parent, Throwable e) {
        String msg = e instanceof AppException ? e.getMessage() : "Something went wrong: " + e.getMessage();
        if (!(e instanceof AppException)) e.printStackTrace();
        JOptionPane.showMessageDialog(parent, msg, "Metric Laundry", JOptionPane.WARNING_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Please confirm", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static String ask(Component parent, String msg) {
        return JOptionPane.showInputDialog(parent, msg, "Metric Laundry", JOptionPane.QUESTION_MESSAGE);
    }

    /** Runs an action and shows any error in a dialog. Returns true when it succeeded. */
    public static boolean run(Component parent, Runnable action) {
        try {
            action.run();
            return true;
        } catch (Exception e) {
            error(parent, e);
            return false;
        }
    }
}
