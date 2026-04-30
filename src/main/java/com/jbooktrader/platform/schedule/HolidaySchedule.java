package com.jbooktrader.platform.schedule;

import java.text.*;
import java.util.*;

/**
 * @author Eugene Kononov
 */
public class HolidaySchedule {
    private static final Map<String, String> holidays;

    static {
        holidays = new HashMap<>();

        // 2009 holidays and early closes
        holidays.put("01/01/2009", "New Year's Day");
        holidays.put("01/19/2009", "Martin Luther King, Jr. Day");
        holidays.put("02/16/2009", "Washington's Birthday (Presidents' Day)");
        holidays.put("04/10/2009", "Good Friday");
        holidays.put("05/25/2009", "Memorial Day");
        holidays.put("07/03/2009", "Independence Day");
        holidays.put("09/07/2009", "Labor Day");
        holidays.put("11/26/2009", "Thanksgiving Day");
        holidays.put("11/27/2009", "Early Close");
        holidays.put("12/24/2009", "Early Close");
        holidays.put("12/25/2009", "Christmas Day");

        // 2010 holidays and early closes
        holidays.put("01/01/2010", "New Year's Day");
        holidays.put("01/18/2010", "Martin Luther King, Jr. Day");
        holidays.put("02/15/2010", "Washington's Birthday (Presidents' Day)");
        holidays.put("04/02/2010", "Good Friday");
        holidays.put("05/31/2010", "Memorial Day");
        holidays.put("07/05/2010", "Independence Day");
        holidays.put("09/06/2010", "Labor Day");
        holidays.put("11/25/2010", "Thanksgiving Day");
        holidays.put("11/26/2010", "Early Close");
        holidays.put("12/24/2010", "Christmas Day");

        // 2011 holidays and early closes
        holidays.put("01/17/2011", "Martin Luther King, Jr. Day");
        holidays.put("02/21/2011", "Washington's Birthday (Presidents' Day)");
        holidays.put("04/22/2011", "Good Friday");
        holidays.put("05/30/2011", "Memorial Day");
        holidays.put("07/04/2011", "Independence Day");
        holidays.put("09/05/2011", "Labor Day");
        holidays.put("11/24/2011", "Thanksgiving Day");
        holidays.put("11/25/2011", "Early Close");
        holidays.put("12/26/2011", "Christmas Day");

        // 2012 holidays and early closes
        holidays.put("01/02/2012", "New Year's Day");
        holidays.put("01/16/2012", "Martin Luther King, Jr. Day");
        holidays.put("02/20/2012", "Washington's Birthday (Presidents' Day)");
        holidays.put("04/06/2012", "Good Friday");
        holidays.put("05/28/2012", "Memorial Day");
        holidays.put("07/03/2012", "Early Close");
        holidays.put("07/04/2012", "Independence Day");
        holidays.put("09/03/2012", "Labor Day");
        holidays.put("10/29/2012", "Closed because of Hurricane Sandy");
        holidays.put("10/30/2012", "Closed because of Hurricane Sandy");
        holidays.put("11/22/2012", "Thanksgiving Day");
        holidays.put("11/23/2012", "Early Close");
        holidays.put("12/24/2012", "Early Close");
        holidays.put("12/25/2012", "Christmas Day");

        // 2013 holidays and early closes
        holidays.put("01/01/2013", "New Year's Day");
        holidays.put("01/21/2013", "Martin Luther King, Jr. Day");
        holidays.put("02/18/2013", "Washington's Birthday (Presidents' Day)");
        holidays.put("03/29/2013", "Good Friday");
        holidays.put("05/27/2013", "Memorial Day");
        holidays.put("07/03/2013", "Early Close");
        holidays.put("07/04/2013", "Independence Day");
        holidays.put("09/02/2013", "Labor Day");
        holidays.put("11/28/2013", "Thanksgiving Day");
        holidays.put("11/29/2013", "Early Close");
        holidays.put("12/24/2013", "Early Close");
        holidays.put("12/25/2013", "Christmas Day");

        // 2014 holidays and early closes
        holidays.put("01/01/2014", "New Year's Day");
        holidays.put("01/20/2014", "Martin Luther King, Jr. Day");
        holidays.put("02/17/2014", "Washington's Birthday (Presidents' Day)");
        holidays.put("04/18/2014", "Good Friday");
        holidays.put("05/26/2014", "Memorial Day");
        holidays.put("07/03/2014", "Early Close");
        holidays.put("07/04/2014", "Independence Day");
        holidays.put("09/01/2014", "Labor Day");
        holidays.put("11/27/2014", "Thanksgiving Day");
        holidays.put("11/28/2014", "Early Close");
        holidays.put("12/24/2014", "Early Close");
        holidays.put("12/25/2014", "Christmas Day");

        // 2015 holidays and early closes
        holidays.put("01/01/2015", "New Year's Day");
        holidays.put("01/19/2015", "Martin Luther King, Jr. Day");
        holidays.put("02/16/2015", "Washington's Birthday (Presidents' Day)");
        holidays.put("04/03/2015", "Good Friday");
        holidays.put("05/25/2015", "Memorial Day");
        holidays.put("07/03/2015", "Independence Day");
        holidays.put("09/07/2015", "Labor Day");
        holidays.put("11/26/2015", "Thanksgiving Day");
        holidays.put("11/27/2015", "Early Close");
        holidays.put("12/24/2015", "Early Close");
        holidays.put("12/25/2015", "Christmas Day");

        // 2016 holidays and early closes
        holidays.put("01/01/2016", "New Year's Day");
        holidays.put("01/18/2016", "Martin Luther King, Jr. Day");
        holidays.put("02/15/2016", "Washington's Birthday (Presidents' Day)");
        holidays.put("03/25/2016", "Good Friday");
        holidays.put("05/30/2016", "Memorial Day");
        holidays.put("07/03/2016", "Early Close");
        holidays.put("07/04/2016", "Independence Day");
        holidays.put("09/05/2016", "Labor Day");
        holidays.put("11/24/2016", "Thanksgiving Day");
        holidays.put("11/25/2016", "Early Close");
        holidays.put("12/26/2016", "Christmas Day");

        // 2017 holidays and early closes
        holidays.put("01/02/2017", "New Year's Day");
        holidays.put("01/16/2017", "Martin Luther King, Jr. Day");
        holidays.put("02/20/2017", "Washington's Birthday (Presidents' Day)");
        holidays.put("04/14/2017", "Good Friday");
        holidays.put("05/29/2017", "Memorial Day");
        holidays.put("07/03/2017", "Early Close");
        holidays.put("07/04/2017", "Independence Day");
        holidays.put("09/04/2017", "Labor Day");
        holidays.put("11/23/2017", "Thanksgiving Day");
        holidays.put("11/24/2017", "Early Close");
        holidays.put("12/25/2017", "Christmas Day");

        // 2018 holidays and early closes
        holidays.put("01/01/2018", "New Year's Day");
        holidays.put("01/15/2018", "Martin Luther King, Jr. Day");
        holidays.put("02/19/2018", "Washington's Birthday (Presidents' Day)");
        holidays.put("03/30/2018", "Good Friday");
        holidays.put("05/28/2018", "Memorial Day");
        holidays.put("07/03/2018", "Early Close");
        holidays.put("07/04/2018", "Independence Day");
        holidays.put("09/03/2018", "Labor Day");
        holidays.put("11/22/2018", "Thanksgiving Day");
        holidays.put("11/23/2018", "Early Close");
        holidays.put("12/24/2018", "Early Close");
        holidays.put("12/25/2018", "Christmas Day");

        // 2019 holidays and early closes
        holidays.put("01/01/2019", "New Year's Day");
        holidays.put("01/21/2019", "Martin Luther King, Jr. Day");
        holidays.put("02/18/2019", "Washington's Birthday (Presidents' Day)");
        holidays.put("04/19/2019", "Good Friday");
        holidays.put("05/27/2019", "Memorial Day");
        holidays.put("07/03/2019", "Early Close");
        holidays.put("07/04/2019", "Independence Day");
        holidays.put("09/02/2019", "Labor Day");
        holidays.put("11/28/2019", "Thanksgiving Day");
        holidays.put("11/29/2019", "Early Close");
        holidays.put("12/24/2019", "Early Close");
        holidays.put("12/25/2019", "Christmas Day");

        // 2020 holidays and early closes
        holidays.put("01/01/2020", "New Year's Day");
        holidays.put("01/20/2020", "Martin Luther King, Jr. Day");
        holidays.put("02/17/2020", "Washington's Birthday (Presidents' Day)");
        holidays.put("04/10/2020", "Good Friday");
        holidays.put("05/25/2020", "Memorial Day");
        holidays.put("07/03/2020", "Independence Day");
        holidays.put("09/07/2020", "Labor Day");
        holidays.put("11/26/2020", "Thanksgiving Day");
        holidays.put("11/27/2020", "Early Close");
        holidays.put("12/24/2020", "Early Close");
        holidays.put("12/25/2020", "Christmas Day");

        // 2026 holidays and early closes
        holidays.put("01/01/2026", "New Year's Day");
        holidays.put("01/19/2026", "Martin Luther King, Jr. Day");
        holidays.put("02/16/2026", "Washington's Birthday (Presidents' Day)");
        holidays.put("04/03/2026", "Good Friday");
        holidays.put("05/25/2026", "Memorial Day");
        holidays.put("06/19/2026", "Juneteenth National Independence Day");
        holidays.put("07/03/2026", "Independence Day");
        holidays.put("09/07/2026", "Labor Day");
        holidays.put("11/26/2026", "Thanksgiving Day");
        holidays.put("11/27/2026", "Early Close");
        holidays.put("12/24/2026", "Early Close");
        holidays.put("12/25/2026", "Christmas Day");
    }

    private final SimpleDateFormat dateFormat;

    public HolidaySchedule() {
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
        dateFormat.setTimeZone(timeZone);
    }

    public synchronized String getHolidayOrEarlyClose(long time) {
        String formattedDate = dateFormat.format(time);
        return holidays.get(formattedDate);
    }

    public synchronized boolean isHolidayOrEarlyClose(long time) {
        String formattedDate = dateFormat.format(time);
        return holidays.containsKey(formattedDate);
    }
}
