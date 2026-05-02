package com.jbooktrader.platform.util.ntp;

import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.report.EventReport;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class is a wrapper around the Apache-Commons NTPUPDClient, which implements
 * the Network Time Protocol (RFC-1305) specification:
 * http://www.faqs.org/ftp/rfc/rfc1305.pdf
 * <p>
 * This class does not synchronize the local clock, but merely uses the pool of
 * NTP time servers to calculate the offset between the NTP server clock and the local clock.
 *
 * @author Eugene Kononov
 */
public class NTPClock {
    private static final int timeoutMillis = 1000;
    private static final int minimumNumberOfServers = 3;
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final static String[] ntpServers = {
            "time-a.timefreq.bldrdoc.gov",
            "time-b.timefreq.bldrdoc.gov",
            "time-c.timefreq.bldrdoc.gov",
            "time-a.nist.gov",
            "time-b.nist.gov",
            "time-c.nist.gov",
            "0.us.pool.ntp.org",
            "1.us.pool.ntp.org",
            "2.us.pool.ntp.org",
            "3.us.pool.ntp.org",
            "0.pool.ntp.org",
            "1.pool.ntp.org",
            "2.pool.ntp.org",
            "3.pool.ntp.org"
    };

    private static NTPClock instance;
    private final AtomicLong offset;
    private final EventReport eventReport;
    private final List<InetAddress> hosts;

    private NTPClock() {
        this.eventReport = Dispatcher.getInstance().getEventReport();
        offset = new AtomicLong();

        // resolve the addresses
        hosts = new ArrayList<>();
        for (String ntpServer : ntpServers) {
            try {
                hosts.add(InetAddress.getByName(ntpServer));
            } catch (UnknownHostException e) {
                eventReport.report("NTP clock", "Could not resolve ntp server: " + ntpServer);
            }
        }

        if (hosts.size() < minimumNumberOfServers) {
            String msg = "Could not initialize NTP clock: insufficient number of available NTP servers.";
            eventReport.report("NTP clock", msg);
            throw new RuntimeException(msg);
        }
        // initial offset
        updateOffset();
        eventReport.report("NTP clock", "NTP clock is initialized. Time offset: " + offset.get() + " ms");

        // schedule regular offset updates
        //scheduler.scheduleWithFixedDelay(this::updateOffset, 2, 2, TimeUnit.MINUTES);

    }

    public synchronized static NTPClock getInstance() {
        if (instance == null) {
            instance = new NTPClock();
        }
        return instance;
    }

    public long getTime() {
        return System.currentTimeMillis() + offset.get();
    }

    public void shutDown() {
        scheduler.shutdown();
    }

    private void updateOffset() {
        NTPUDPClient ntpClient = new NTPUDPClient();
        ntpClient.setDefaultTimeout(timeoutMillis);

        try {
            ntpClient.open();
            List<Long> offsets = new ArrayList<>();
            List<String> unresponsiveNtpServers = new ArrayList<>();
            for (InetAddress host : hosts) {
                try {
                    TimeInfo timeInfo = ntpClient.getTime(host);
                    timeInfo.computeDetails();
                    offsets.add(timeInfo.getOffset());
                } catch (IOException ioe) {
                    unresponsiveNtpServers.add(host.getHostName());
                }
            }

            if (offsets.size() < minimumNumberOfServers) {
                String msg = "Unable to update NTP time because insufficient number of NTP servers responded. ";
                msg += "Could not get response from the following NTP servers: " + unresponsiveNtpServers;
                eventReport.report("NTP clock", "NTP Clock: " + msg);
            } else {
                offsets.remove(Collections.min(offsets)); // discard the min outlier
                offsets.remove(Collections.max(offsets)); // discard the max outlier

                double averageOffset = offsets.stream().mapToLong(x -> x).summaryStatistics().getAverage();
                offset.set(Math.round(averageOffset));
            }
        } catch (Exception e) {
            eventReport.report("NTP clock", "Could not update NTP Clock: " + e.getMessage());
        } finally {
            ntpClient.close();
        }
    }

}
