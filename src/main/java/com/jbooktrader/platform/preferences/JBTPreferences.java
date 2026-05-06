package com.jbooktrader.platform.preferences;

/**
 * @author Eugene Kononov
 */
public enum JBTPreferences {
    // TWS connection
    Host("Host", "localhost"),
    Port("Port", "7496"),
    ClientID("Client ID", "0"),

    // Web Access
    WebAccess("Web Access", "disabled"),
    WebAccessPort("Web Access Port", "1235"),
    WebAccessUser("Web Access User", "admin"),
    WebAccessPassword("Web Access Password", "admin"),

    // Portfolio Manager
    MaxLeverage("Maximum leverage", "10"),

    // Portfolio backtest
    PotfolioOptimizerWindowWidth("portfolio.optimizerwindow.width", "1100"),
    PotfolioOptimizerWindowHeight("portfolio.optimizerwindow.height", "900"),
    ChartYaxisLocation("portfolio.chart.y.axis.location", "Right"),
    UpperChartWeight("portfolio.chart.upper.weight", "2"),
    ShowLegend("portfolio.chart.showlegend", "true"),

    // Forced Exit
    MarketDataTimeoutSeconds("Close open positions and stop trading if market data stops for longer than", "60"),
    OpenOrderTimeoutSeconds("Close open positions and stop trading if an open order is not filled within", "120"),

    // Session Exit
    SessionExitTime("Session exit time", "17:00"),

    // Notifications
    Notification("Notifications", "disabled"),
    SmtpHost("SMTP Host", "smtp.gmail.com"),
    SmtpPort("SMTP Port", "587"),
    SmtpProtocol("SMTP Protocol", "TLS/SSL"),
    SmtpUser("SMTP User", "user@email.com"),
    SmtpPassword("SMTP Password", ""),
    Subject("Subject", "JBookTrader notification"),
    Recipients("Recipients", "user@email.com"),
    SendTestNotification("Test", "Send a test notification"),

    // Account
    Account("Account", ""),

    // Data file for backtester and optimizer
    DataFileName("dataFileName"),

    // Date range
    DateRangeStart("dateRange.start", "January 1, 2011"),
    DateRangeEnd("dateRange.end", "March 11, 2011"),
    UseDateRange("dateRange.use", "false"),

    // Optimizer
    OptimizerMinTrades("backtest.minTrades", "50"),
    OptimizerPerformanceMetric("backtest.performanceMetric", "OG"),
    Kernel("backtest.kernel", "Uniform"),
    OptimizerMethod("backtest.method", "Brute force"),
    OptimizerWindowWidth("optimizerwindow.width", "950"),
    OptimizerWindowHeight("optimizerwindow.height", "750"),

    // Main window
    MainWindowWidth("mainwindow.width", "950"),
    MainWindowHeight("mainwindow.height", "400"),

    // Performance chart
    PerformanceChartWidth("performance.chart.width", "950"),
    PerformanceChartHeight("performance.chart.height", "750"),
    PerformanceChartBarSize("performance.chart.barSize", "1 minute"),

    // Optimizer
    DivideAndConquerCoverage("Divide & Conquer coverage", "3"),
    StrategiesPerProcessor("Strategies per processor", "50"),
    InclusionCriteria("Inclusion criteria", "Profitable strategies"),
    ParameterBounds("Parameter bounds", "Lenient"),

    // Optimization Map
    OptimizationMapWidth("optimization.map.width", "720"),
    OptimizationMapHeight("optimization.map.height", "550");

    private final String name, defaultValue;

    JBTPreferences(String name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    JBTPreferences(String name) {
        this(name, "");
    }


    public String getDefault() {
        return defaultValue;
    }

    public String getName() {
        return name;
    }
}
