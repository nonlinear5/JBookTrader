package com.jbooktrader.platform.startup;


import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.util.ui.*;

import javax.swing.*;
import javax.swing.plaf.nimbus.*;
import java.awt.*;
import java.io.*;
import java.nio.channels.*;


/**
 * Application starter.
 *
 * @author Eugene Kononov
 */
public class JBookTrader {
    public static final String APP_NAME = "JBookTrader";
    public static final String VERSION = "2026.1";
    public static final String RELEASE_DATE = "April 29, 2026";
    public static final String COPYRIGHT = "2013-2026 Eugene Kononov";

    /**
     * Instantiates the necessary parts of the application: the application model,
     * views, and controller.
     */
    private JBookTrader() {
        try {
            Dispatcher.getInstance().init();
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
            UIManager.getLookAndFeelDefaults().put("nimbusOrange", (new Color(119, 136, 153)));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        new MainFrameController();
    }

    /**
     * Starts JBookTrader application.
     */
    public static void main(String[] args) {
        try {
            File file = new File(System.getProperty("user.home"), APP_NAME + ".tmp");
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

            if (channel.tryLock() == null) {
                MessageDialog.showError(APP_NAME + " is already running.");
                return;
            }

            if (args.length != 0) {
                String msg = APP_NAME + " takes no parameters.";
                throw new RuntimeException(msg);
            }

            new JBookTrader();
        } catch (Throwable t) {
            MessageDialog.showException(t);
        }
    }
}
