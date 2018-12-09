package com.apptastic.fininsyn.shortselling;

import com.apptastic.blankningsregistret.NetShortPosition;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ShortSellingFilter {

    public static boolean positionDateFilter(String notOlderThenDate, String publicationDate) {
        return notOlderThenDate.compareTo(publicationDate) < 0;
    }

    public static boolean historyLimitFilter(NetShortPosition position) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar date = Calendar.getInstance();
        date.add(Calendar.YEAR, -1);

        String newerThenDate = formatter.format(date.getTime());
        return position.getPositionDate().compareTo(newerThenDate) > 0;
    }
}
