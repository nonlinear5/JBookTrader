package com.jbooktrader.platform.model;

import com.jbooktrader.platform.backtest.BackTestDialog;
import com.jbooktrader.platform.chart.PerformanceChart;
import com.jbooktrader.platform.chart.PerformanceChartData;
import com.jbooktrader.platform.dialog.AboutDialog;
import com.jbooktrader.platform.dialog.MainFrameDialog;
import com.jbooktrader.platform.optimizer.OptimizerDialog;
import com.jbooktrader.platform.ordermanager.OrderManagerAssistant;
import com.jbooktrader.platform.portfolio.PortfolioBackTestDialog;
import com.jbooktrader.platform.preferences.PreferencesDialog;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.strategy.StrategyInformationDialog;
import com.jbooktrader.platform.util.ui.MessageDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.List;

import static com.jbooktrader.platform.preferences.JBTPreferences.MainWindowHeight;
import static com.jbooktrader.platform.preferences.JBTPreferences.MainWindowWidth;

/**
 * Acts as a controller in the Model-View-Controller pattern
 *
 * @author Eugene Kononov
 */
public class MainFrameController {
    private final MainFrameDialog mainViewDialog;
    private final JTable strategyTable;
    private final StrategyTableModel strategyTableModel;
    private final Dispatcher dispatcher;

    public MainFrameController() {
        mainViewDialog = new MainFrameDialog();
        dispatcher = Dispatcher.getInstance();
        dispatcher.addListener(mainViewDialog);
        strategyTable = mainViewDialog.getStrategyTable();
        strategyTableModel = mainViewDialog.getStrategyTableModel();
        assignListeners();
    }

    private void exit() {
        String question = "Are you sure you want to exit " + JBookTrader.APP_NAME + "?";
        int answer = JOptionPane.showConfirmDialog(mainViewDialog, question, JBookTrader.APP_NAME, JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            PreferencesHolder prefs = PreferencesHolder.getInstance();
            prefs.set(MainWindowWidth, mainViewDialog.getSize().width);
            prefs.set(MainWindowHeight, mainViewDialog.getSize().height);
            dispatcher.exit();
            mainViewDialog.dispose();
        }
    }

    private Strategy getSelectedRowStrategy() {
        int selectedRow = strategyTable.getSelectedRow();
        if (selectedRow < 0) {
            throw new RuntimeException("No strategy is selected.");
        }
        Strategy strategy = strategyTableModel.getStrategyForRow(selectedRow);
        strategyTableModel.update(strategy);

        return strategy;
    }

    private void openURL(String url) {
        try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(url));
        } catch (Throwable t) {
            dispatcher.getEventReport().report(t);
            MessageDialog.showException(t);
        }
    }

    private void assignListeners() {

        strategyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int modifiers = e.getModifiersEx();
                boolean actionRequested = (modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0;
                actionRequested = actionRequested || (modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0;
                if (actionRequested) {
                    int selectedRow = strategyTable.rowAtPoint(e.getPoint());
                    strategyTable.setRowSelectionInterval(selectedRow, selectedRow);
                    mainViewDialog.showPopup(e);
                    strategyTable.setRowSelectionInterval(selectedRow, selectedRow);
                }
            }
        });

        mainViewDialog.informationAction(e -> {
            try {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                int selectedRow = strategyTable.getSelectedRow();
                if (selectedRow < 0) {
                    throw new RuntimeException("No strategy is selected.");
                }

                Strategy strategy = strategyTableModel.getStrategyForRow(selectedRow);
                new StrategyInformationDialog(mainViewDialog, strategy);
            } catch (Throwable t) {
                MessageDialog.showException(t);
            } finally {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });


        mainViewDialog.backTestAction(e -> {
            try {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                dispatcher.getOrderManager().getAssistant().clearAllStrategies();
                Strategy strategy = getSelectedRowStrategy();
                dispatcher.setMode(Mode.BackTest);
                new BackTestDialog(mainViewDialog, strategy);
            } catch (Throwable t) {
                MessageDialog.showException(t);
            } finally {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        mainViewDialog.backTestAllAction(e -> {
            try {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                dispatcher.getOrderManager().getAssistant().clearAllStrategies();
                dispatcher.setMode(Mode.BackTestAll);
                new PortfolioBackTestDialog(mainViewDialog);
            } catch (Throwable t) {
                MessageDialog.showException(t);
            } finally {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });


        mainViewDialog.optimizeAction(ae -> {
            try {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                int selectedRow = strategyTable.getSelectedRow();
                if (selectedRow < 0) {
                    throw new RuntimeException("No strategy is selected.");
                }
                String name = strategyTableModel.getStrategyForRow(selectedRow).getName();
                dispatcher.setMode(Mode.Optimization);
                OptimizerDialog optimizerDialog = new OptimizerDialog(mainViewDialog, name);
                optimizerDialog.setVisible(true);
            } catch (Throwable t) {
                MessageDialog.showException(t);
            } finally {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        mainViewDialog.forwardTestAction(e -> {
            try {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Strategy strategy = getSelectedRowStrategy();
                dispatcher.setMode(Mode.ForwardTest);
                dispatcher.getOrderManager().getAssistant().addStrategy(strategy);
            } catch (Throwable t) {
                MessageDialog.showException(t);
            } finally {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        mainViewDialog.tradeAction(e -> {
            try {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Strategy strategy = getSelectedRowStrategy();
                dispatcher.setMode(Mode.Trade);
                dispatcher.getOrderManager().getAssistant().addStrategy(strategy);
            } catch (Throwable t) {
                MessageDialog.showException(t);
            } finally {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        mainViewDialog.tradeAllAction(e -> {
            try {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                List<Strategy> strategies = dispatcher.getStrategies();
                dispatcher.setMode(Mode.Trade);
                OrderManagerAssistant oma = dispatcher.getOrderManager().getAssistant();
                for (Strategy strategy : strategies) {
                    oma.addStrategy(strategy);
                }
            } catch (Throwable t) {
                MessageDialog.showException(t);
            } finally {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });


        mainViewDialog.chartAction(e -> {
            try {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                int selectedRow = strategyTable.getSelectedRow();
                if (selectedRow < 0) {
                    MessageDialog.showMessage("No strategy is selected.");
                    return;
                }

                Strategy strategy = strategyTableModel.getStrategyForRow(selectedRow);
                if (strategy == null) {
                    String msg = "Please run this strategy first.";
                    MessageDialog.showMessage(msg);
                    return;
                }

                PerformanceChartData pcd = strategy.getPerformanceManager().getPerformanceChartData();
                if (pcd == null || pcd.isEmpty()) {
                    String msg = "There is no data to chart. Please run a back test first.";
                    MessageDialog.showMessage(msg);
                    return;
                }

                PerformanceChart spChart = new PerformanceChart(mainViewDialog, strategy);
                JFrame chartFrame = spChart.getChart();
                chartFrame.setVisible(true);
            } catch (Throwable t) {
                MessageDialog.showException(t);
            } finally {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        mainViewDialog.preferencesAction(e -> {
            try {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                new PreferencesDialog(mainViewDialog);
            } catch (Throwable t) {
                MessageDialog.showException(t);
            } finally {
                mainViewDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        mainViewDialog.suspendTradingAction(e -> {
            try {
                String question = "Are you sure you want to close open positions and suspend trading?";
                int answer = JOptionPane.showConfirmDialog(mainViewDialog, question, JBookTrader.APP_NAME, JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    Dispatcher.getInstance().setMode(Mode.ForceClose);
                    String msg = JBookTrader.APP_NAME + " entered the <ForceClose> mode. Trading will be suspended after open positions (if any) are closed.";
                    MessageDialog.showMessage(msg);
                }
            } catch (Exception ex) {
                MessageDialog.showException(ex);
            }
        });

        mainViewDialog.discussionAction(e -> openURL("http://groups.google.com/group/jbooktrader/topics?gvc=2"));

        mainViewDialog.releaseNotesAction(e -> openURL("http://code.google.com/p/jbooktrader/wiki/ReleaseNotes"));

        mainViewDialog.userManualAction(e -> openURL("https://docs.google.com/document/d/1uNQzIbuNyNZXxuv9NFCZRgx0nYeEfwuG1ZRfHBnFfZk/edit?usp=sharing"));

        mainViewDialog.projectHomeAction(e -> openURL("https://github.com/nonlinear5/JBookTrader"));

        mainViewDialog.exitAction(e -> exit());

        mainViewDialog.exitAction(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        mainViewDialog.aboutAction(e -> {
            try {
                new AboutDialog(mainViewDialog);
            } catch (Throwable t) {
                MessageDialog.showException(t);
            }
        });
    }
}
