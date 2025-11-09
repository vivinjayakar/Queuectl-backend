package com.queuectl.queuectl.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    // Define IST time zone
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS z").withZone(IST_ZONE);

    // Returns formatted IST time string
    public static String nowIstString() {
        return ZonedDateTime.now(IST_ZONE).format(FORMATTER);
    }

    public static ZonedDateTime nowIstZoned() {
        return ZonedDateTime.now(IST_ZONE);
    }

    public static String formatInstantToIst(Instant instant) {
        return instant == null ? null : FORMATTER.format(instant.atZone(IST_ZONE));
    }
}
