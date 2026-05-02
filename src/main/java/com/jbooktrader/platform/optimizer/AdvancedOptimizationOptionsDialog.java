package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.dialog.JBTDialog;
import com.jbooktrader.platform.preferences.JBTPreferences;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.util.ui.SpringUtilities;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

import static com.jbooktrader.platform.preferences.JBTPreferences.DivideAndConquerCoverage;
import static com.jbooktrader.platform.preferences.JBTPreferences.StrategiesPerProcessor;

/**
 * @author Eugene Kononov
 */
class AdvancedOptimizationOptionsDialog extends JBTDialog {
    private static final Dimension FIELD_DIMENSION = new Dimension(Integer.MAX_VALUE, 22);
    private final PreferencesHolder prefs;
    private JSlider divideAndConquerCoverageSlider;
    private JTextField strategiesPerProcessorText;

    AdvancedOptimizationOptionsDialog(JFrame parent) {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        init();
        pack();
        setLocationRelativeTo(null);
        setModal(true);
        setVisible(true);
    }

    private void add(JPanel panel, JBTPreferences pref, JTextField textField) {
        textField.setText(prefs.get(pref));
        genericAdd(panel, pref, textField, FIELD_DIMENSION);
    }

    private void genericAdd(JPanel panel, JBTPreferences pref, Component comp, Dimension dimension) {
        JLabel fieldNameLabel = new JLabel(pref.getName() + ":");
        fieldNameLabel.setLabelFor(comp);
        comp.setPreferredSize(dimension);
        comp.setMaximumSize(dimension);
        panel.add(fieldNameLabel);
        panel.add(comp);
    }

    private void genericAdd(JPanel panel, JBTPreferences pref, Component comp) {
        genericAdd(panel, pref, comp, null);
    }


    private void add(JPanel panel, JBTPreferences pref, JSlider slider) {
        slider.setValue(prefs.getInt(pref));
        genericAdd(panel, pref, slider);
    }

    private void init() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Advanced Optimization Options");

        JPanel contentPanel = new JPanel(new SpringLayout());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 12));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        strategiesPerProcessorText = new JTextField();
        strategiesPerProcessorText.setHorizontalAlignment(SwingConstants.RIGHT);
        add(contentPanel, StrategiesPerProcessor, strategiesPerProcessorText);

        int min = 3;
        int max = 40;
        divideAndConquerCoverageSlider = new JSlider(min, max);
        divideAndConquerCoverageSlider.setMajorTickSpacing(1);
        divideAndConquerCoverageSlider.setPaintTicks(true);
        divideAndConquerCoverageSlider.setSnapToTicks(true);
        Properties divideAndConquerLabels = new Properties();
        Font labelFont = divideAndConquerCoverageSlider.getFont().deriveFont(Font.ITALIC, 11);
        JLabel divideAndConquerSparserLabel = new JLabel("sparser", JLabel.TRAILING);
        divideAndConquerSparserLabel.setFont(labelFont);
        JLabel divideAndConquerDenserLabel = new JLabel("denser", JLabel.CENTER);
        divideAndConquerDenserLabel.setFont(labelFont);
        divideAndConquerLabels.put(min, divideAndConquerSparserLabel);
        divideAndConquerLabels.put(max, divideAndConquerDenserLabel);


        divideAndConquerCoverageSlider.setLabelTable(divideAndConquerLabels);
        divideAndConquerCoverageSlider.setPaintLabels(true);

        add(contentPanel, DivideAndConquerCoverage, divideAndConquerCoverageSlider);
        SpringUtilities.makeCompactGrid(contentPanel, 2, 2, 12, 12, 6, 8);


        okButton.addActionListener(e -> {
            prefs.set(DivideAndConquerCoverage, divideAndConquerCoverageSlider.getValue());
            prefs.set(StrategiesPerProcessor, strategiesPerProcessorText.getText());
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());


        getRootPane().setDefaultButton(okButton);
        setPreferredSize(new Dimension(750, 380));
    }

}
