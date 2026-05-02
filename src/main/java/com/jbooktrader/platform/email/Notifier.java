package com.jbooktrader.platform.email;

import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.Mode;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.report.EventReport;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * SMTP over TLS
 *
 * @author Eugene Kononov
 */
public class Notifier implements Runnable {
    private static Notifier instance;
    private final boolean isEnabled;
    private final String recipients;
    private final String user, subject;
    private final BlockingQueue<String> emailMessageQueue;
    private final String endMessage = "quit";
    private final EventReport eventReport;
    private final Properties props;
    private final Authenticator authenticator;

    private Notifier() {
        PreferencesHolder prefs = PreferencesHolder.getInstance();

        isEnabled = prefs.get(Notification).equalsIgnoreCase("enabled");
        user = prefs.get(SmtpUser);
        subject = prefs.get(Subject);
        recipients = prefs.get(Recipients);
        eventReport = Dispatcher.getInstance().getEventReport();
        emailMessageQueue = new LinkedBlockingQueue<>();

        props = new Properties();
        props.put("mail.smtp.auth", "true");
        String protocol = prefs.get(SmtpProtocol);
        if (protocol.equalsIgnoreCase("SSL")) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        if (protocol.equalsIgnoreCase("TLS/SSL")) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        props.put("mail.smtp.host", prefs.get(SmtpHost));
        props.put("mail.smtp.port", prefs.get(SmtpPort));

        authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(prefs.get(SmtpUser), prefs.get(SmtpPassword));
            }
        };

        Thread thread = new Thread(this);
        thread.start(); //self-start
    }

    public static synchronized Notifier getInstance() {
        if (instance == null) {
            instance = new Notifier();
        }
        return instance;
    }

    @Override
    public void run() {
        String msg;
        try {
            while (!(msg = emailMessageQueue.take()).equals(endMessage)) {
                send(msg, false);
            }
        } catch (MessagingException | InterruptedException ee) {
            eventReport.report(ee);
        }
    }

    public void shutdown() {
        emailMessageQueue.offer(endMessage);
    }

    public void submit(String msg) {
        if (isEnabled) {
            emailMessageQueue.offer(msg);
        }
    }

    public void test(String msg) throws MessagingException {
        send(msg, true);
    }

    private void send(String msg, boolean debug) throws MessagingException {
        Mode mode = Dispatcher.getInstance().getMode();
        boolean isEmailEnabledMode = (mode != Mode.Optimization && mode != Mode.BackTest && mode != Mode.BackTestAll);
        if (debug || isEmailEnabledMode) {
            Session session = Session.getInstance(props, authenticator);
            session.setDebug(debug);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            message.setSubject(subject);
            message.setContent(msg, "text/html; charset=utf-8");
            Transport.send(message);
        }
    }

}
