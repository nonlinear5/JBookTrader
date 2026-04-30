package com.jbooktrader.platform.preferences;

import com.jbooktrader.platform.startup.*;

import java.util.prefs.*;

/**
 * @author Eugene Kononov
 */
public class PreferencesHolder {
    private static PreferencesHolder instance;
    private final Preferences prefs;

    // private constructor for non-instantiability
    private PreferencesHolder(String preferenceSetName) {
        Preferences prefsRoot = Preferences.userRoot();
        String node = "com.jbooktrader." + preferenceSetName;
        prefs = prefsRoot.node(node);
    }

    public static synchronized PreferencesHolder getInstance() {
        if (instance == null) {
            instance = new PreferencesHolder(JBookTrader.APP_NAME);
        }
        return instance;
    }

    public int getInt(JBTPreferences pref) {
        String value = get(pref);
        return Integer.parseInt(value);
    }

    public double getDouble(JBTPreferences pref) {
        String value = get(pref);
        return Double.parseDouble(value);
    }

    public String get(JBTPreferences pref) {
        return prefs.get(pref.getName(), pref.getDefault());
    }

    public void set(JBTPreferences pref, Object propertyValue) {
        prefs.put(pref.getName(), String.valueOf(propertyValue));
    }
}
