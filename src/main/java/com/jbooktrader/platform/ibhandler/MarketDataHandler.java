package com.jbooktrader.platform.ibhandler;

import com.ib.client.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.snapshotwriter.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Eugene Kononov
 */
class MarketDataHandler {
    private final EClientSocket socket;
    private final SnapshotWriterManager snapshotWriterManager;
    private final Map<Integer, MarketDepth> marketDepths;
    private final Map<String, MarketDepth> symbols;
    private final Map<Integer, Contract> contracts;
    private final EventReport eventReport;
    private int requestId;
    private BlockingQueue<MarketSnapshot> queue;

    public MarketDataHandler(EClientSocket socket) {
        this.socket = socket;
        marketDepths = new HashMap<>();
        symbols = new HashMap<>();
        contracts = new HashMap<>();
        eventReport = Dispatcher.getInstance().getEventReport();
        snapshotWriterManager = new SnapshotWriterManager();
        requestId = 1000000; // start with a high number which does not clash with order IDs
    }

    public void setQueue(BlockingQueue<MarketSnapshot> queue) {
        this.queue = queue;
    }

    public void reset(int tickerId) {
        for (MarketDepth marketDepth : symbols.values()) {
            if (marketDepth.getDepthRequestId() == tickerId) {
                marketDepth.reset();
                String msg = "Market book for " + marketDepth.getSymbol() + " has been reset";
                eventReport.report("MarketDataHandler", msg);
            }
        }
    }

    private void subscribe(String localSymbol, MarketDepth marketDepth, Contract contract) {
        requestId++;
        marketDepths.put(requestId, marketDepth);
        marketDepth.addLocalSymbol(requestId, localSymbol);
        contract.localSymbol(localSymbol);
        socket.reqMktData(requestId, contract, "", false, false, null);
    }

    public void unsubscribe() {
        requestId++;
        boolean isSmartDepth = true;
        socket.cancelMktDepth(requestId, isSmartDepth);
        requestId++;
        socket.cancelMktData(requestId);
    }

    public void subscribe(Contract contract) {
        String ticker = contract.symbol();

        if (!symbols.containsKey(ticker)) {
            MarketDepth marketDepth = new MarketDepth(contract);
            symbols.put(ticker, marketDepth);

            requestId++;
            contracts.put(requestId, contract);
            eventReport.report("MarketDataHandler", "Requested contracts for ticker " + ticker);
            socket.reqContractDetails(requestId, contract);
        }
    }

    public void subscribe(int requestId, List<String> localSymbols) {
        Contract contract = contracts.get(requestId);
        String ticker = contract.symbol();
        MarketDepth marketDepth = symbols.get(ticker);

        for (String localSymbol : localSymbols) {
            if (localSymbol.startsWith(ticker)) {
                subscribe(localSymbol, marketDepth, contract);
            }
        }
    }


    public void tickSize(int tickerId, int field, int size) {
        if (field == TickType.VOLUME.index()) {
            MarketDepth marketDepth = marketDepths.get(tickerId);
            if (marketDepth != null) {
                int mostLiquidId = marketDepth.processVolume(tickerId, size);
                if (mostLiquidId > 0) {
                    int oldDepthRequestId = marketDepth.getDepthRequestId();
                    if (oldDepthRequestId > 0) {
                        boolean isSmartDepth = true;
                        socket.cancelMktDepth(oldDepthRequestId, isSmartDepth);
                        marketDepths.remove(oldDepthRequestId);
                        marketDepth.reset();
                    }

                    requestId++;
                    marketDepths.put(requestId, marketDepth);
                    marketDepth.setDepthRequestId(requestId);
                    String localSym = marketDepth.getLocalSymbol(mostLiquidId);
                    Contract contract = marketDepth.getContract();
                    contract.localSymbol(localSym);
                    socket.reqMktDepth(requestId, contract, 10, false, null);
                    String msg = "Detected most liquid contract for ticker " + marketDepth.getSymbol() + ": " + localSym;
                    eventReport.report("MarketDataHandler", msg);
                }
            }
        }
    }


    public void updateMktDepth(int tickerId, int position, int operation,
                               int side, double price, int size) throws InterruptedException {
        MarketDepth marketDepth = marketDepths.get(tickerId);
        if (marketDepth != null) {
            marketDepth.update(position, operation, side, price, size);
            MarketSnapshot snapshot = marketDepth.takeMarketSnapshot();

            if (snapshot != null) {
                snapshotWriterManager.saveSnapshot(snapshot, marketDepth.getSymbol());
                queue.put(snapshot);
            }
        }
    }


}
