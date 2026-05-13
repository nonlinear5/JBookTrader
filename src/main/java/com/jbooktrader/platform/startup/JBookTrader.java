package com.jbooktrader.platform.startup;


import com.formdev.flatlaf.intellijthemes.FlatGrayIJTheme;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.MainFrameController;
import com.jbooktrader.platform.util.ui.MessageDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;


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

    private static FileLock lock;
    private static FileChannel channel;
    private static final String lockFileName = "JBookTrader.lock";

    /**
     * Instantiates the necessary parts of the application: the application model,
     * views, and controller.
     */
    private JBookTrader(boolean autoStartTrading) {
        try {
            Dispatcher.getInstance().init();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        new MainFrameController(autoStartTrading);
    }

    /**
     * Starts JBookTrader application.
     */
    public static void main(String[] args) {
        try {
            FlatGrayIJTheme.setup();
            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
            if (defaults.get("Table.alternateRowColor") == null) {
                defaults.put("Table.alternateRowColor", new Color(254, 254, 254));
            }

            if (!lockInstance()) {
                MessageDialog.showError(APP_NAME + " is already running.");
                return;
            }

            if (args.length > 1) {
                String msg = APP_NAME + " takes no parameters, or one parameter.";
                throw new RuntimeException(msg);
            }

            boolean autoStartTrading = false;
            if (args.length == 1) {
                autoStartTrading = Boolean.parseBoolean(args[0]);
            }

            new JBookTrader(autoStartTrading);
        } catch (Throwable t) {
            MessageDialog.showException(t);
        }
    }


    private static boolean lockInstance() throws IOException {
        try {
            final File file = new File(System.getProperty("java.io.tmpdir"), lockFileName);
            channel = new RandomAccessFile(file, "rw").getChannel();

            // Try to acquire an exclusive lock
            lock = channel.tryLock();

            if (lock != null) {
                // Add shutdown hook to release lock and close channel on exit
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        if (lock != null) lock.release();
                        channel.close();
                        file.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
                return true;
            }
        } catch (OverlappingFileLockException e) {
            // Lock is already held by this JVM
            return false;
        } catch (Exception e) {
            throw e;
        }
        return false;
    }
}
