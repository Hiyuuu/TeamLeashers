package Utils.Utils

import java.util.*

object Utils {

    fun stringToUUID(UUID: String) : UUID? {
        if (UUID.contains("-")) return java.util.UUID.fromString(UUID)
        return runCatching { java.util.UUID.fromString(separeteUUID(UUID.replace("-", ""))) }.getOrNull()
    }

    fun separeteUUID(UUID: String) : String {
        if (UUID.contains("-")) return UUID
        return UUID.substring(0..7)+ "-" +
                UUID.substring(8..11) + "-" +
                UUID.substring(12..15) + "-" +
                UUID.substring(16..19) + "-" +
                UUID.substring(20..UUID.length - 1)
    }

}