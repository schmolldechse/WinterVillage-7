package de.voldechse.wintervillage.test;

import java.util.UUID;

public class UUIDHelper {

    private UUIDHelper() {}

    public static UUID dashed(String id) {
        return UUID.fromString(id.substring(0, 8) + '-' +
                id.substring(8, 12) + '-' +
                id.substring(12, 16) + '-' +
                id.substring(16, 20) + '-' +
                id.substring(20, 32));
    }

    public static String undashed(UUID uniqueId) {
        return uniqueId.toString().replace("-", "");
    }

    public UUID copy(UUID from) {
        return new UUID(from.getMostSignificantBits(), from.getLeastSignificantBits());
    }
}
