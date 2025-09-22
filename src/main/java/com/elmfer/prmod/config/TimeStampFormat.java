package com.elmfer.prmod.config;

/** Enum to determine the format to display time **/
enum TimeStampFormat {
    GAME_TICKS("ticks", 4), SECONDS("seconds", 3), SECONDS_TENTHS("seconds.tenths", 4),
    SECONDS_HUNDREDTHS("seconds.hundredths", 5), SECONDS_TICKS("ss:ticks", 5), HH_MM_SS("hh:mm:ss", 8),
    HH_MM_SS_TENTHS("hh:mm:ss.tenths", 10), HH_MM_SS_HUNDREDTHS("hh:mm:ss.hundredths", 11),
    HH_MM_SS_TICKS("hh:mm:ss:ticks", 11);

    public static final TimeStampFormat DEFAULT = GAME_TICKS;

    /** The precidcted string length for the format. **/
    public final int AVERAGE_LENGTH;

    /** Name of the format. The name gives an idea on how it will format time. **/
    public final String NAME;

    TimeStampFormat(String name, int length) {
        AVERAGE_LENGTH = length;
        NAME = name;
    }

    /**
     * Get time format from name. If given name does not equal to any formats, it
     * will return {@code GAME_TICKS} by default.
     */
    public static TimeStampFormat getFormatFromName(String name) {
        for (TimeStampFormat format : TimeStampFormat.values())
            if (name.equals(format.NAME))
                return format;
        return GAME_TICKS;
    }

    public String getTimeStamp(double gameTicks) {
        // Hour, minute, second formatting
        int ticks = (int) gameTicks;
        int hours = ticks / 72000;
        ticks %= 72000; // 72000 ticks per hour
        int minutes = ticks / 1200;
        ticks %= 1200; // 1200 ticks per minute
        double seconds = (gameTicks - 72000.0 * hours - 1200.0 * minutes) / 20.0;
        ticks %= 20; // 20 ticks per second
        double fullSeconds = gameTicks / 20.0; // Has total amount of seconds
        int frame = ticks; // Remaining frames

        switch (this) {
        case SECONDS:
            return String.format("%1$01ds", (int) fullSeconds);
        case SECONDS_TENTHS:
            return String.format("%1$.1fs", fullSeconds);
        case SECONDS_HUNDREDTHS:
            return String.format("%1$.2fs", fullSeconds);
        case SECONDS_TICKS:
            return String.format("%1$02d:%2$02d", (int) seconds, frame);
        case HH_MM_SS:
            return String.format("%1$02d:%2$02d:%3$02d", hours, minutes, (int) seconds);
        case HH_MM_SS_TENTHS:
            return String.format("%1$02d:%2$02d:%3$.1f", hours, minutes, seconds);
        case HH_MM_SS_HUNDREDTHS:
            return String.format("%1$02d:%2$02d:%3$.2f", hours, minutes, seconds);
        case HH_MM_SS_TICKS:
            return String.format("%1$02d:%2$02d:%3$02d:%4$02d", hours, minutes, (int) seconds, frame);
        default:
            return Integer.toString((int) gameTicks);
        }
    }
}