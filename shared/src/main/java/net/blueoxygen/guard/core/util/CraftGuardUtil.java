package net.blueoxygen.guard.core.util;

import lombok.experimental.UtilityClass;
import net.blueoxygen.guard.core.handshake.RestoredPlayerHandshake;

@UtilityClass
public class CraftGuardUtil {

    public static RestoredPlayerHandshake
            restorePlayerData(String rawPayload) {
        if (!rawPayload.startsWith("CG")) {
            // not from CraftGuard, ignore
            return null;
        }

        String[] payloadParts = rawPayload.substring(2).split("!!", 2);
        if (payloadParts.length != 2) {
            // invalid payload
            return null;
        }

        String[] ipParts = payloadParts[1].split(":", 2);
        if (ipParts.length != 2) {
            // invalid IP
            return null;
        }

        String originalAddress = payloadParts[0];
        String ip = ipParts[0];
        int port;
        try {
            port = Integer.parseInt(ipParts[1]);
        } catch (NumberFormatException unused) {
            // not a port
            return null;
        }

        return new RestoredPlayerHandshake(originalAddress, ip, port);
    }

    // https://github.com/VelocityPowered/Velocity/blob/e3f17eeb245b8d570f16c1f2aff5e7eafb698d5e/proxy/src/main/java/com/velocitypowered/proxy/connection/client/HandshakeSessionHandler.java
    public static String cleanVhost(String hostname) {
        // Clean out any anything after any zero bytes (this includes BungeeCord forwarding and the
        // legacy Forge handshake indicator).
        String cleaned = hostname;
        int zeroIdx = cleaned.indexOf('\0');
        if (zeroIdx > -1) {
            cleaned = hostname.substring(0, zeroIdx);
        }

        // If we connect through an SRV record, there will be a period at the end (DNS usually elides
        // this ending octet).
        if (!cleaned.isEmpty() && cleaned.charAt(cleaned.length() - 1) == '.') {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned;
    }

}
