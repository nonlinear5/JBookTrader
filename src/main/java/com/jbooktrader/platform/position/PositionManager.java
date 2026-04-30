package com.jbooktrader.platform.position;

import com.jbooktrader.platform.ibhandler.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.format.*;

import java.text.*;
import java.util.*;

/**
 * Position manager keeps track of current positions and executions.
 *
 * @author Eugene Kononov
 */
public class PositionManager {
    private static final long millisInMinute = 60L * 1000L;
    private static final long minimumRemainingTimeMinutes = 15;
    private final static Dispatcher dispatcher;
    private final static EventReport eventReport;
    private final static HolidaySchedule holidaySchedule;

    static {
        dispatcher = Dispatcher.getInstance();
        eventReport = dispatcher.getEventReport();
        holidaySchedule = new HolidaySchedule();
    }

    private final Deque<Position> positionsHistory;
    private final DecimalFormat df6;
    private final Strategy strategy;
    private final PerformanceManager performanceManager;
    private int currentPosition, targetPosition;
    private double avgFillPrice, expectedFillPrice;

    public PositionManager(Strategy strategy) {
        this.strategy = strategy;
        positionsHistory = new LinkedList<>();
        performanceManager = strategy.getPerformanceManager();
        df6 = NumberFormatterFactory.getNumberFormatter(6);
    }

    public Iterable<Position> getPositionsHistory() {
        return positionsHistory;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(int targetPosition) {
        if (targetPosition == this.targetPosition) {
            return; // nothing to do
        }

        if (isHoliday()) {
            // do not allow trading on market holidays
            return;
        }

        boolean isExposureIncreasing = Math.abs(targetPosition) > Math.abs(this.targetPosition);

        if (isExposureIncreasing) {
            long timeNow = strategy.getTime();
            long remainingTimeMillis = strategy.getTradingSchedule().getRemainingTime(timeNow);
            long remainingMinutes = remainingTimeMillis / millisInMinute;
            if (remainingMinutes < minimumRemainingTimeMinutes) {
                return; // do not allow an increase in the exposure if there is not enough time left in the trading interval
            }
        }

        // all preconditions have been met, the new target can be set now
        this.targetPosition = targetPosition;
        Mode mode = dispatcher.getMode();
        if (mode == Mode.Trade || mode == Mode.ForwardTest || mode == Mode.ForceClose) {
            eventReport.report(strategy.getName(), "Target position is set to " + targetPosition);
        }
    }

    public double getAvgFillPrice() {
        return avgFillPrice;
    }

    public double getExpectedFillPrice() {
        return expectedFillPrice;
    }

    public void setExpectedFillPrice(double expectedFillPrice) {
        this.expectedFillPrice = expectedFillPrice;
    }

    public void update(OrderExecution orderExecution) {
        String side = orderExecution.getSide();
        int quantity = orderExecution.getQuantity();

        if (side.equals("SLD")) {
            quantity = -quantity;
        }

        // current position after the execution
        currentPosition += quantity;
        avgFillPrice = orderExecution.getAverageFillPrice();
        double slippage = side.equals("SLD") ? (avgFillPrice - expectedFillPrice) : (expectedFillPrice - avgFillPrice);
        performanceManager.updateOnTrade(quantity, avgFillPrice, currentPosition, slippage);

        Mode mode = Dispatcher.getInstance().getMode();
        if (mode == Mode.BackTest) {
            positionsHistory.add(new Position(strategy.getTime(), currentPosition, avgFillPrice));
        }

        if (mode != Mode.Optimization) {
            strategy.getStrategyReportManager().report();
        }

        if (mode == Mode.ForwardTest || mode == Mode.Trade || mode == Mode.ForceClose) {
            StringBuilder msg = new StringBuilder();
            msg.append("Order ").append(orderExecution.getOrderID()).append(": Booked [");
            msg.append("average price: ").append(df6.format(avgFillPrice)).append(", ");
            msg.append("strategy position: ").append(getCurrentPosition());
            msg.append("]");
            eventReport.report(strategy.getName(), msg.toString());
            Dispatcher.getInstance().fireModelChanged(ModelListener.Event.SystemStatusUpdate);
        }
    }

    private boolean isHoliday() {
        long time = strategy.getTime();
        return holidaySchedule.isHolidayOrEarlyClose(time);
    }
}
