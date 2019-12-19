package com.apptastic.fininsyn.shortselling;

import com.apptastic.blankningsregistret.NetShortPosition;

import java.time.LocalDate;
import java.time.ZoneId;

import static com.apptastic.fininsyn.shortselling.ShortSellingTwitterPublisher.TIME_ZONE;

public class ShortSellingFilter {

    public static boolean positionDateFilter(LocalDate notOlderThenDate, LocalDate publicationDate) {
        return notOlderThenDate.compareTo(publicationDate) < 0;
    }

    public static boolean historyLimitFilter(NetShortPosition position) {
        LocalDate date = LocalDate.now(ZoneId.of(TIME_ZONE));
        date = date.minusYears(1);

        return position.getPositionDate().compareTo(date) > 0;
    }
}
