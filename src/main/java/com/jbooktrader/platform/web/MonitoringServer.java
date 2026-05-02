package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.report.EventReport;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.util.ui.MessageDialog;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static com.jbooktrader.platform.preferences.JBTPreferences.WebAccess;
import static com.jbooktrader.platform.preferences.JBTPreferences.WebAccessPort;

/**
 * @author Eugene Kononov
 */
public class MonitoringServer {
    private static HttpServer server;

    public static void start() {
        if (server == null) {
            PreferencesHolder prefs = PreferencesHolder.getInstance();
            if (prefs.get(WebAccess).equalsIgnoreCase("enabled")) {
                EventReport eventReport = Dispatcher.getInstance().getEventReport();
                try {
                    int port = Integer.parseInt(prefs.get(WebAccessPort));
                    server = HttpServer.create(new InetSocketAddress(port), 0);
                    HttpContext context = server.createContext("/", new WebHandler());
                    context.setAuthenticator(new WebAuthenticator());
                    server.setExecutor(Executors.newSingleThreadExecutor());
                    server.start();
                    eventReport.report(JBookTrader.APP_NAME, "Monitoring server started");
                } catch (Exception e) {
                    eventReport.report(e);
                    MessageDialog.showError("Could not start monitoring server: " + e);
                }
            }
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
