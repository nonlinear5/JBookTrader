package com.jbooktrader.platform.dialog;

import com.jbooktrader.platform.model.TableDataModel;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.util.ui.SpringUtilities;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Dialog to show the application info, system info, and IB API info.
 *
 * @author Eugene Kononov
 */
public class AboutDialog extends JBTDialog {

    public AboutDialog(JFrame parent) {
        super(parent);
        init();
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void init() {
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("About " + JBookTrader.APP_NAME);

        JPanel contentPanel = new JPanel(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        JTabbedPane tabbedPane1 = new JTabbedPane();
        contentPanel.add(tabbedPane1, BorderLayout.CENTER);

        JPanel aboutPanel = new JPanel(new SpringLayout());
        tabbedPane1.addTab("About", aboutPanel);

        JLabel productLabel = new JLabel("Product:", SwingConstants.TRAILING);
        JLabel productValueLabel = new JLabel(JBookTrader.APP_NAME);
        productValueLabel.setForeground(Color.BLACK);
        productLabel.setLabelFor(productValueLabel);
        aboutPanel.add(productLabel);
        aboutPanel.add(productValueLabel);

        JLabel versionLabel = new JLabel("Version:", SwingConstants.TRAILING);
        JLabel versionValueLabel = new JLabel(JBookTrader.VERSION);
        versionValueLabel.setForeground(Color.BLACK);
        versionLabel.setLabelFor(versionValueLabel);
        aboutPanel.add(versionLabel);
        aboutPanel.add(versionValueLabel);

        JLabel releaseDateLabel = new JLabel("Released:", SwingConstants.TRAILING);
        JLabel releaseDateValueLabel = new JLabel(JBookTrader.RELEASE_DATE);
        releaseDateValueLabel.setForeground(Color.BLACK);
        releaseDateLabel.setLabelFor(releaseDateValueLabel);
        aboutPanel.add(releaseDateLabel);
        aboutPanel.add(releaseDateValueLabel);

        JLabel copyrightLabel = new JLabel("Copyright:", SwingConstants.TRAILING);
        JLabel copyrightValueLabel = new JLabel(JBookTrader.COPYRIGHT);
        copyrightValueLabel.setForeground(Color.BLACK);
        copyrightLabel.setLabelFor(copyrightValueLabel);
        aboutPanel.add(copyrightLabel);
        aboutPanel.add(copyrightValueLabel);
        SpringUtilities.makeCompactGrid(aboutPanel, 4, 2, 12, 12, 5, 5);


        JPanel systemInfoPanel = new JPanel(new BorderLayout(5, 5));
        tabbedPane1.addTab("System Info", systemInfoPanel);

        JScrollPane systemInfoScrollPane = new JScrollPane();
        systemInfoPanel.add(systemInfoScrollPane, BorderLayout.CENTER);

        TableDataModel aboutModel = new AboutTableModel();
        JTable aboutTable = new JTable(aboutModel);
        systemInfoScrollPane.getViewport().add(aboutTable);


        getContentPane().setPreferredSize(new Dimension(650, 400));
        aboutTable.getColumnModel().getColumn(1).setPreferredWidth(380);


        Properties properties = System.getProperties();
        Enumeration<?> propNames = properties.propertyNames();

        while (propNames.hasMoreElements()) {
            String key = (String) propNames.nextElement();
            String value = properties.getProperty(key);
            String[] row = {key, value};
            aboutModel.addRow(row);
        }

    }

    /* inner class to define the "about" model */
    private static class AboutTableModel extends TableDataModel {
        private AboutTableModel() {
            String[] aboutSchema = {"Property", "Value"};
            setSchema(aboutSchema);
        }
    }
}
