package com.elmfer.prmod.parkour;

/**
 * Lists saving formats for correctly deserializing saves. The naming convention
 * is simply a mod version that had changed how things were saved.
 **/
public enum SaveFormat {
    V1_0_0_0(1), V1_0_1_0(0);

    // Latest format
    public static final SaveFormat LATEST = SaveFormat.values()[SaveFormat.values().length - 1];

    public final int ID;

    private SaveFormat(int id) {
        ID = id;
    }

    /**
     * Returns format from id. Anything greater than zero gives the oldest format.
     * Each consecutive format will have an id one less than the previous one.
     */
    public static SaveFormat getFormatFromID(int id) {
        if (0 < id)
            return V1_0_0_0;

        for (SaveFormat format : SaveFormat.values()) {
            if (format.ID == 1)
                continue;
            else if (format.ID == id)
                return format;
        }

        return null;
    }
}
