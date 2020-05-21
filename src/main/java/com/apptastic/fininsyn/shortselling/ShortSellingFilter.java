package com.apptastic.fininsyn.shortselling;

import com.apptastic.blankningsregistret.NetShortPosition;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.ZoneId;

import static com.apptastic.fininsyn.shortselling.ShortSellingTwitterPublisher.TIME_ZONE;

public class ShortSellingFilter {

    public static boolean positionDateFilter(LocalDate lastPublicationDate, LocalDate publicationDate) {
        return publicationDate.isAfter(lastPublicationDate);
    }

    public static boolean historyLimitFilter(NetShortPosition position) {
        LocalDate date = LocalDate.now(ZoneId.of(TIME_ZONE));
        date = date.minusMonths(6);

        return position.getPositionDate().compareTo(date) > 0;
    }

    public static boolean badPositions(NetShortPosition position) {
        return position != null &&
               position.getIssuer() != null &&
               position.getPositionHolder() != null &&
               position.getIsin() != null;
    }

    public static boolean positionChange(Pair<NetShortPosition, NetShortPosition> positionPair) {
        return positionPair != null &&
               (positionPair.getRight() == null ||
                positionPair.getLeft().getPositionInPercent() != positionPair.getRight().getPositionInPercent());
    }
}
