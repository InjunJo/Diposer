package team.moebius.disposer.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeSupporter {

    public static long getNowUnixTime(){
        return ZonedDateTime.now().toInstant().toEpochMilli();
    }

    public static LocalDateTime convertUnixTime(long unixTime){
        return Instant.ofEpochMilli(unixTime).atZone(ZoneId.of("UTC")).toLocalDateTime();
    }

}
