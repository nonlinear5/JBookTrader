package com.jbooktrader.platform.marketdepth;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class MarketDepthModel {
    private final LinkedList<MarketDepthItem> items;

    public MarketDepthModel() {
        items = new LinkedList<>();
    }

    public void insert(int position, double price, int size) {
        MarketDepthItem item = new MarketDepthItem(price, size);
        items.add(position, item);
        while (items.size() > 10) {
            items.removeLast();
        }
    }

    public int getSize() {
        return items.size();
    }

    public void delete(int position) {
        if (position < items.size()) {
            items.remove(position);
        }
    }

    public void update(int position, double price, int size) {
        MarketDepthItem item = items.listIterator(position).next();
        if (item != null) {
            item.set(price, size);
        }
    }

    public double getBestPrice() {
        return items.getFirst().getPrice();
    }

    public int getCumulativeSize() {
        int cumulativeSize = 0;
        for (MarketDepthItem item : items) {
            cumulativeSize += item.getSize();
        }
        return cumulativeSize;
    }

    public boolean hasValidBidStructure() {
        double lastPrice = Double.POSITIVE_INFINITY;
        for (MarketDepthItem item : items) {
            double price = item.getPrice();
            if (price >= lastPrice) {
                return false;
            }
            if (item.getSize() <= 0) {
                return false;
            }
            lastPrice = price;
        }
        return true;
    }

    public boolean hasValidAskStructure() {
        double lastPrice = 0;
        for (MarketDepthItem item : items) {
            double price = item.getPrice();
            if (price <= lastPrice) {
                return false;
            }
            if (item.getSize() <= 0) {
                return false;
            }
            lastPrice = price;
        }
        return true;
    }

    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append("<br>");
        for (MarketDepthItem item : items) {
            msg.append(item.getSize()).append(", ").append(item.getPrice()).append("<br>");
        }
        return msg.toString();
    }


    public void reset() {
        items.clear();
    }

}

