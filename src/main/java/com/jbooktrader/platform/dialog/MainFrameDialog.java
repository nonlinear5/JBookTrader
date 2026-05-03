package com.jbooktrader.platform.dialog;


import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.Mode;
import com.jbooktrader.platform.model.ModelListener;
import com.jbooktrader.platform.model.StrategyTableModel;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ui.MessageDialog;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.net.URL;

import static com.jbooktrader.platform.model.StrategyTableColumn.Strategy;
import static com.jbooktrader.platform.preferences.JBTPreferences.MainWindowHeight;
import static com.jbooktrader.platform.preferences.JBTPreferences.MainWindowWidth;

/**
 * Main application window. All the system logic is intentionally left out if this class,
 * which acts as a simple "view" of the underlying model.
 *
 * @author Eugene Kononov
 */
public class MainFrameDialog extends JFrame implements ModelListener {
    private final Toolkit toolkit;
    private final Dispatcher dispatcher;
    private JMenuItem exitMenuItem, suspendLiveTradingMenuItem, aboutMenuItem,
            userManualMenuItem, discussionMenuItem, releaseNotesMenuItem,
            projectHomeMenuItem, preferencesMenuItem;
    private JMenuItem infoMenuItem, tradeMenuItem, tradeAllMenuItem, backTestMenuItem,
            backTestAllMenuItem, forwardTestMenuItem, optimizeMenuItem, chartMenuItem;
    private StrategyTableModel strategyTableModel;
    private JTable strategyTable;
    private JPopupMenu popupMenu;

    public MainFrameDialog() {
        dispatcher = Dispatcher.getInstance();
        toolkit = Toolkit.getDefaultToolkit();
        init();
        populateStrategies();
        strategyTable.getColumnModel().getColumn(Strategy.ordinal()).setPreferredWidth(350);

        PreferencesHolder prefs = PreferencesHolder.getInstance();
        int width = prefs.getInt(MainWindowWidth);
        int height = prefs.getInt(MainWindowHeight);

        pack();

        setSize(width, height);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private String getSystemStatus() {
        Mode mode = dispatcher.getMode();
        String systemStatus = JBookTrader.APP_NAME + " - [" + mode.getName() + "]";

        if (mode == Mode.Trade || mode == Mode.ForwardTest || mode == Mode.ForceClose) {
            systemStatus += " - " + dispatcher.getOrderManager().getAssistant().getSystemStatus();
        }
        return systemStatus;
    }

    public void modelChanged(Event event, Object value) {
        switch (event) {
            case ModeChanged:

                Mode mode = dispatcher.getMode();

                suspendLiveTradingMenuItem.setEnabled(mode == Mode.Trade);
                if (mode == Mode.Trade || mode == Mode.ForwardTest) {
                    backTestMenuItem.setEnabled(false);
                    optimizeMenuItem.setEnabled(false);
                    chartMenuItem.setEnabled(false);
                    tradeAllMenuItem.setEnabled(false);

                    if (mode == Mode.Trade) {
                        forwardTestMenuItem.setEnabled(false);
                    }
                    if (mode == Mode.ForwardTest) {
                        tradeMenuItem.setEnabled(false);
                    }
                }
                if (mode == Mode.BackTest || mode == Mode.ForceClose || mode == Mode.Optimization || mode == Mode.BackTestAll) {
                    forwardTestMenuItem.setEnabled(false);
                    tradeMenuItem.setEnabled(false);
                    tradeAllMenuItem.setEnabled(false);
                }
                SwingUtilities.invokeLater(() -> setTitle(getSystemStatus()));
                break;
            case SystemStatusUpdate:
                SwingUtilities.invokeLater(() -> setTitle(getSystemStatus()));
                break;
            case Error:
                String msg = (String) value;
                MessageDialog.showError(msg);
                break;
            case StrategyUpdate:
                strategyTableModel.update((Strategy) value);
                break;
        }
    }

    public void userManualAction(ActionListener action) {
        userManualMenuItem.addActionListener(action);
    }

    public void discussionAction(ActionListener action) {
        discussionMenuItem.addActionListener(action);
    }

    public void releaseNotesAction(ActionListener action) {
        releaseNotesMenuItem.addActionListener(action);
    }

    public void projectHomeAction(ActionListener action) {
        projectHomeMenuItem.addActionListener(action);
    }

    public void informationAction(ActionListener action) {
        infoMenuItem.addActionListener(action);
    }

    public void backTestAction(ActionListener action) {
        backTestMenuItem.addActionListener(action);
    }

    public void backTestAllAction(ActionListener action) {
        backTestAllMenuItem.addActionListener(action);
    }


    public void optimizeAction(ActionListener action) {
        optimizeMenuItem.addActionListener(action);
    }

    public void forwardTestAction(ActionListener action) {
        forwardTestMenuItem.addActionListener(action);
    }

    public void tradeAction(ActionListener action) {
        tradeMenuItem.addActionListener(action);
    }

    public void tradeAllAction(ActionListener action) {
        tradeAllMenuItem.addActionListener(action);
    }

    public void chartAction(ActionListener action) {
        chartMenuItem.addActionListener(action);
    }

    public void preferencesAction(ActionListener action) {
        preferencesMenuItem.addActionListener(action);
    }

    public void exitAction(ActionListener action) {
        exitMenuItem.addActionListener(action);
    }

    public void suspendTradingAction(ActionListener action) {
        suspendLiveTradingMenuItem.addActionListener(action);
    }

    public void exitAction(WindowAdapter action) {
        addWindowListener(action);
    }

    public void aboutAction(ActionListener action) {
        aboutMenuItem.addActionListener(action);
    }

    private URL getImageURL(String imageFileName) {
        String resourceDir = dispatcher.getResourcesDir();
        URL imgURL = getClass().getResource(resourceDir + imageFileName); // load from a jar
        if (imgURL == null) {
            imgURL = getClass().getResource("/" + imageFileName); // load from a directory
        }

        if (imgURL == null) {
            String msg = "Could not locate file: " + imageFileName + ". Make sure the /resources directory is in the classpath.";
            throw new RuntimeException(msg);
        }

        return imgURL;
    }

    private ImageIcon getImageIcon(String imageFileName) {
        return new ImageIcon(toolkit.getImage(getImageURL(imageFileName)));
    }


    private void populateStrategies() {
        dispatcher.getStrategies().forEach(strategyTableModel::addStrategy);
    }

    public StrategyTableModel getStrategyTableModel() {
        return strategyTableModel;
    }

    public JTable getStrategyTable() {
        return strategyTable;
    }

    public void showPopup(MouseEvent mouseEvent) {
        popupMenu.show(strategyTable, mouseEvent.getX(), mouseEvent.getY());
    }

    private void init() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        // session menu
        JMenu sessionMenu = new JMenu("Session");
        sessionMenu.setMnemonic('S');
        suspendLiveTradingMenuItem = new JMenuItem("Suspend trading");
        suspendLiveTradingMenuItem.setEnabled(false);
        exitMenuItem = new JMenuItem("Exit", 'X');
        sessionMenu.add(suspendLiveTradingMenuItem);
        sessionMenu.addSeparator();
        sessionMenu.add(exitMenuItem);

        // configure menu
        JMenu configureMenu = new JMenu("Configure");
        configureMenu.setMnemonic('C');
        preferencesMenuItem = new JMenuItem("Preferences...", 'P');
        configureMenu.add(preferencesMenuItem);

        // help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        userManualMenuItem = new JMenuItem("User Manual", 'U');
        releaseNotesMenuItem = new JMenuItem("Release Notes", 'R');
        discussionMenuItem = new JMenuItem("Discussion Group", 'D');
        projectHomeMenuItem = new JMenuItem("Project Home", 'P');
        aboutMenuItem = new JMenuItem("About...", 'A');
        helpMenu.add(userManualMenuItem);
        helpMenu.addSeparator();
        helpMenu.add(releaseNotesMenuItem);
        helpMenu.add(discussionMenuItem);
        helpMenu.add(projectHomeMenuItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutMenuItem);

        // menu bar
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(sessionMenu);
        menuBar.add(configureMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // popup menu
        popupMenu = new JPopupMenu();

        infoMenuItem = new JMenuItem("Information", getImageIcon("information.png"));
        backTestMenuItem = new JMenuItem("Back test this strategy", getImageIcon("backTest.png"));
        backTestAllMenuItem = new JMenuItem("Back test portfolio", getImageIcon("backTest.png"));
        optimizeMenuItem = new JMenuItem("Optimize", getImageIcon("optimize.png"));
        forwardTestMenuItem = new JMenuItem("Forward test", getImageIcon("forwardTest.png"));
        tradeMenuItem = new JMenuItem("Trade");
        tradeAllMenuItem = new JMenuItem("Trade all");
        chartMenuItem = new JMenuItem("Chart", getImageIcon("chart.png"));

        popupMenu.add(infoMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(backTestMenuItem);
        popupMenu.add(backTestAllMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(optimizeMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(forwardTestMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(chartMenuItem);
        popupMenu.addSeparator();
        popupMenu.add(tradeMenuItem);
        popupMenu.add(tradeAllMenuItem);

        JScrollPane strategyTableScrollPane = new JScrollPane();

        strategyTableScrollPane.setAutoscrolls(true);
        strategyTableModel = new StrategyTableModel();
        strategyTable = new JTable(strategyTableModel);
        strategyTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ((JLabel) strategyTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);


        strategyTable.setShowGrid(false);

        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) strategyTable.getDefaultRenderer(String.class);
        renderer.setHorizontalAlignment(SwingConstants.LEFT);

        strategyTableScrollPane.getViewport().add(strategyTable);

        Image appIcon = Toolkit.getDefaultToolkit().getImage(getImageURL("JBookTrader.png"));
        setIconImage(appIcon);
        setMacDockIcon();

        add(strategyTableScrollPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        JLabel lastMessageLabel = new JLabel(" ");
        lastMessageLabel.setBorder(etchedBorder);
        statusPanel.add(lastMessageLabel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        setTitle(JBookTrader.APP_NAME);
        setMinimumSize(new Dimension(600, 200));
    }

    private void setMacDockIcon() {
        boolean onMac = System.getProperty("os.name").toLowerCase().startsWith("mac os x");
        if (!onMac) {
            return;
        }
        try {
            Image appIcon = Toolkit.getDefaultToolkit().getImage(getImageURL("JBookTraderMac.png"));
            Taskbar taskbar = Taskbar.getTaskbar();
            taskbar.setIconImage(appIcon);
        } catch (Exception e) {
            dispatcher.getEventReport().report(e);
            // if the icon can't be placed on the taskbar, continue normally
        }
    }
}
