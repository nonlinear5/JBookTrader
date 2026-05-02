package com.jbooktrader.platform.web;

import com.jbooktrader.platform.marketbook.MarketSnapshot;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.format.NumberFormatterFactory;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Eugene Kononov
 */
public class WebHandler implements HttpHandler {
    private static final DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);
    private static final DecimalFormat df6 = NumberFormatterFactory.getNumberFormatter(6);
    private static final String reportsDir = Dispatcher.getInstance().getReportsDir();
    private static final String resourcesDir = Dispatcher.getInstance().getResourcesDir();

    private void addRow(StringBuilder response, List<Object> cells, int rowCount) {
        response.append((rowCount % 2 == 0) ? "<tr>" : "<tr class=oddRow>");
        for (Object cell : cells) {
            response.append("<td>").append(cell).append("</td>");
        }
        response.append("</tr>");
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        byte[] out;
        StringBuilder response = new StringBuilder();
        String resource = httpExchange.getRequestURI().getPath();

        if (resource.equals("") || resource.equals("/")) {
            Dispatcher dispatcher = Dispatcher.getInstance();
            List<Strategy> strategies = new ArrayList<>(dispatcher.getOrderManager().getAssistant().getAllStrategies());
            Collections.sort(strategies);

            response.append("<html><head><title>");
            response.append(JBookTrader.APP_NAME + ", version " + JBookTrader.VERSION + "</title>");
            response.append("<link rel=stylesheet type=text/css href=JBookTrader.css />");
            response.append("<link rel=\"shortcut icon\" type=image/x-icon href=JBookTrader.ico />");

            response.append("</head>");
            response.append("<body>");
            response.append("<h1>" + JBookTrader.APP_NAME + " - ");
            response.append("[" + "<a href=EventReport.htm target=_new>");
            response.append(dispatcher.getMode().getName()).append("</a>]</h1>");
            response.append("<table>");
            response.append("<tr><th>Strategy</th><th>Ticker</th><th>Price</th><th>Position</th><th>Trades</th><th>Net Profit</th></tr>");

            int strategyRowCount = 0;
            for (Strategy strategy : strategies) {
                MarketSnapshot marketSnapshot = strategy.getMarketBook().getSnapshot();
                PerformanceManager pm = strategy.getPerformanceManager();

                List<Object> cells = new ArrayList<>();
                String path = reportsDir + "/" + strategy.getName() + ".htm";
                if (new File(path).exists()) {
                    cells.add("<a href=" + strategy.getName() + ".htm target=_new>" + strategy.getName() + "</a>");
                } else {
                    cells.add(strategy.getName());
                }
                cells.add(strategy.getTicker());
                cells.add((marketSnapshot != null) ? df6.format(marketSnapshot.getPrice()) : "n/a");
                cells.add(strategy.getPositionManager().getCurrentPosition());
                cells.add(pm.getTrades());
                cells.add(df0.format(pm.getNetProfit()));
                addRow(response, cells, strategyRowCount++);
            }

            response.append("</table>");
            response.append("</body>");
            response.append("</html>");

            out = response.toString().getBytes();
        } else {
            String path = (resource.endsWith("htm") ? reportsDir : resourcesDir) + resource;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
            out = new byte[(int) new File(path).length()];
            bis.read(out);
            bis.close();
        }

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, out.length);
        OutputStream responseBody = httpExchange.getResponseBody();
        responseBody.write(out);
        responseBody.close();
    }

}
