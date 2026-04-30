package com.jbooktrader.platform.preferences;

import com.jbooktrader.platform.dialog.*;
import com.jbooktrader.platform.email.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.ui.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * @author Eugene Kononov
 */
public class PreferencesDialog extends JBTDialog {
    private static final Dimension FIELD_DIMENSION = new Dimension(Integer.MAX_VALUE, 20);
    private final PreferencesHolder prefs;

    public PreferencesDialog(JFrame parent) {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        setLocationRelativeTo(parent);
        setModal(true);
        setVisible(true);
    }

    private void add(JPanel panel, JBTPreferences pref, JButton button) {
        button.setText(prefs.get(pref));
        genericAdd(panel, pref, button);
    }


    private void add(JPanel panel, JBTPreferences pref, JTextField textField) {
        textField.setHorizontalAlignment(JTextField.RIGHT);
        textField.setText(prefs.get(pref));
        genericAdd(panel, pref, textField);
    }

    private void add(JPanel panel, JBTPreferences pref, JSpinner spinner) {
        spinner.setValue(prefs.getInt(pref));
        genericAdd(panel, pref, spinner);
    }

    private void add(JPanel panel, JBTPreferences pref, JSpinner spinner, String text) {
        spinner.setValue(prefs.getInt(pref));
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":");
        fieldNameLabel.setLabelFor(spinner);
        spinner.setMaximumSize(FIELD_DIMENSION);
        JLabel label = new JLabel(text);
        label.setMaximumSize(FIELD_DIMENSION);
        panel.add(fieldNameLabel);
        panel.add(spinner);
        panel.add(label);
    }

    private void add(JPanel panel, JBTPreferences pref, JComboBox comboBox) {
        ((JLabel) comboBox.getRenderer()).setHorizontalAlignment(JLabel.RIGHT);
        comboBox.setSelectedItem(prefs.get(pref));
        genericAdd(panel, pref, comboBox);
    }

    private void genericAdd(JPanel panel, JBTPreferences pref, Component comp) {
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":");
        fieldNameLabel.setLabelFor(comp);
        comp.setMaximumSize(FIELD_DIMENSION);
        panel.add(fieldNameLabel);
        panel.add(comp);
    }

    private void init() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Preferences");

        JPanel contentPanel = new JPanel(new BorderLayout());

        JPanel buttonsPanel = new JPanel();
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        getContentPane().add(contentPanel, BorderLayout.NORTH);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        contentPanel.add(tabbedPane, BorderLayout.NORTH);

        // TWS connection
        JPanel connectionTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("TWS", connectionTab);
        JTextField hostText = new JTextField();
        JTextField portText = new JTextField();
        JSpinner clientIDSpin = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
        JTextField accountText = new JTextField();
        add(connectionTab, Host, hostText);
        add(connectionTab, Port, portText);
        add(connectionTab, ClientID, clientIDSpin);
        add(connectionTab, Account, accountText);
        SpringUtilities.makeTwoColumnGrid(connectionTab);

        // web access
        JPanel webAccessTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Web Access", webAccessTab);
        JComboBox<String> webAccessCombo = new JComboBox<>(new String[]{"disabled", "enabled"});
        final JTextField webAccessPortField = new JTextField();

        JTextField webAccessUser = new JTextField();
        JPasswordField webAccessPasswordField = new JPasswordField();
        add(webAccessTab, WebAccess, webAccessCombo);
        add(webAccessTab, WebAccessPort, webAccessPortField);
        add(webAccessTab, WebAccessUser, webAccessUser);
        add(webAccessTab, WebAccessPassword, webAccessPasswordField);
        SpringUtilities.makeTwoColumnGrid(webAccessTab);

        // portfolio manager
        JPanel portfolioManagerTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Portfolio Manager", portfolioManagerTab);
        JTextField maximumLeverageText = new JTextField();
        add(portfolioManagerTab, MaxLeverage, maximumLeverageText);
        SpringUtilities.makeTwoColumnGrid(portfolioManagerTab);

        // auto-stop
        JPanel autoStopTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Auto Stop", autoStopTab);
        JSpinner marketDataTimeoutPeriod = new JSpinner(new SpinnerNumberModel(720, 10, 1200, 1));
        JSpinner openOrderTimeoutPeriod = new JSpinner(new SpinnerNumberModel(720, 10, 1200, 1));
        add(autoStopTab, MarketDataTimeoutSeconds, marketDataTimeoutPeriod, "seconds");
        add(autoStopTab, OpenOrderTimeoutSeconds, openOrderTimeoutPeriod, "seconds");
        SpringUtilities.makeCompactGrid(autoStopTab, 2, 3, 12, 8, 8, 8);

        // notifications
        JPanel notificationsTab = new JPanel(new SpringLayout());
        tabbedPane.addTab("Notifications", notificationsTab);
        JTextField smtpHost = new JTextField();
        JTextField smtpPort = new JTextField();
        JComboBox<String> smtpProtocol = new JComboBox<>(new String[]{"SSL", "TLS/SSL"});
        JTextField mailUser = new JTextField();
        JPasswordField mailPassword = new JPasswordField();
        JTextField subject = new JTextField();
        JTextField recipients = new JTextField();
        JComboBox<String> notificationCombo = new JComboBox<>(new String[]{"disabled", "enabled"});
        JButton notificationTestButton = new JButton("Test");

        add(notificationsTab, Notification, notificationCombo);
        add(notificationsTab, SmtpHost, smtpHost);
        add(notificationsTab, SmtpPort, smtpPort);
        add(notificationsTab, SmtpProtocol, smtpProtocol);
        add(notificationsTab, SmtpUser, mailUser);
        add(notificationsTab, SmtpPassword, mailPassword);
        add(notificationsTab, Subject, subject);
        add(notificationsTab, Recipients, recipients);
        add(notificationsTab, SendTestNotification, notificationTestButton);
        notificationTestButton.addActionListener(e -> {
            try {
                Notifier.getInstance().test("notification test message: " + new Date());
                MessageDialog.showMessage("Test notification sent to recipients.");
            } catch (Exception ex) {
                MessageDialog.showException(ex);
            }
        });
        SpringUtilities.makeTwoColumnGrid(notificationsTab);


        okButton.addActionListener(e -> {
            try {
                // TWS connection
                prefs.set(Host, hostText.getText());
                prefs.set(Port, portText.getText());
                prefs.set(ClientID, clientIDSpin.getValue().toString());
                prefs.set(Account, accountText.getText());

                // web access
                prefs.set(WebAccess, webAccessCombo.getSelectedItem());
                prefs.set(WebAccessPort, webAccessPortField.getText());
                prefs.set(WebAccessUser, webAccessUser.getText());
                prefs.set(WebAccessPassword, new String(webAccessPasswordField.getPassword()));

                // portfolio manager
                prefs.set(MaxLeverage, maximumLeverageText.getText());

                // forced exit
                prefs.set(MarketDataTimeoutSeconds, marketDataTimeoutPeriod.getValue().toString());
                prefs.set(OpenOrderTimeoutSeconds, openOrderTimeoutPeriod.getValue().toString());

                // notifications
                prefs.set(Notification, notificationCombo.getSelectedItem());
                prefs.set(SmtpHost, smtpHost.getText());
                prefs.set(SmtpPort, smtpPort.getText());
                prefs.set(SmtpProtocol, smtpProtocol.getSelectedItem());
                prefs.set(SmtpUser, mailUser.getText());
                prefs.set(SmtpPassword, new String(mailPassword.getPassword()));
                prefs.set(Subject, subject.getText());
                prefs.set(Recipients, recipients.getText());

                String msg = "Some of the preferences will not take effect until " + JBookTrader.APP_NAME + " is restarted.";
                MessageDialog.showMessage(msg);

                dispose();
            } catch (Exception ex) {
                MessageDialog.showException(ex);
            }
        });

        cancelButton.addActionListener(e -> dispose());

        setMinimumSize(new Dimension(660, 400));
    }
}
