package com.metriclaundry.util;

import com.metriclaundry.model.User;

/** Holds the currently logged-in user (single-user desktop session). */
public final class Session {
    private static User current;
    private Session() { }
    public static User get() { return current; }
    public static void set(User user) { current = user; }
    public static void clear() { current = null; }
}
