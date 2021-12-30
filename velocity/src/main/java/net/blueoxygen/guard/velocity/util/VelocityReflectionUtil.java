package net.blueoxygen.guard.velocity.util;

import com.velocitypowered.api.proxy.InboundConnection;
import net.blueoxygen.guard.core.handshake.RestoredPlayerHandshake;
import net.blueoxygen.guard.core.util.CraftGuardUtil;
import net.blueoxygen.guard.core.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public class VelocityReflectionUtil {

    private static final Class LOGIN_INBOUND_CONNECTION_CLASS;
    private static final Class INITIAL_INBOUND_CONNECTION_CLASS;
    private static final Class LEGACY_INBOUND_CONNECTION_CLASS;
    private static final Class MINECRAFT_CONNECTION_CLASS;
    private static final Class HANDSHAKE_CLASS;

    private static final Field CLEANED_ADDRESS_FIELD;
    private static final Field REMOTE_ADDRESS_FIELD;
    private static final Field SERVER_ADDRESS_FIELD;
    private static final Field CONNECTION_FIELD;
    private static final Field LEGACY_CONNECTION_FIELD;
    private static final Field DELEGATE_FIELD;
    private static final Field HANDSHAKE_FIELD;

    private static final Method CLOSE_CHANNEL_METHOD;

    static {
        try {
            LOGIN_INBOUND_CONNECTION_CLASS = Class.forName("com.velocitypowered.proxy.connection.client.LoginInboundConnection");
            INITIAL_INBOUND_CONNECTION_CLASS = Class.forName("com.velocitypowered.proxy.connection.client.InitialInboundConnection");
            LEGACY_INBOUND_CONNECTION_CLASS = Class.forName("com.velocitypowered.proxy.connection.client.HandshakeSessionHandler$LegacyInboundConnection");
            MINECRAFT_CONNECTION_CLASS = Class.forName("com.velocitypowered.proxy.connection.MinecraftConnection");
            HANDSHAKE_CLASS = Class.forName("com.velocitypowered.proxy.protocol.packet.Handshake");

            CLEANED_ADDRESS_FIELD = ReflectionUtil.getPrivateField(INITIAL_INBOUND_CONNECTION_CLASS, "cleanedAddress");
            REMOTE_ADDRESS_FIELD = ReflectionUtil.getPrivateField(MINECRAFT_CONNECTION_CLASS, "remoteAddress");
            SERVER_ADDRESS_FIELD = ReflectionUtil.getPrivateField(HANDSHAKE_CLASS, "serverAddress");
            CONNECTION_FIELD = ReflectionUtil.getPrivateField(INITIAL_INBOUND_CONNECTION_CLASS, "connection");
            LEGACY_CONNECTION_FIELD = ReflectionUtil.getPrivateField(LEGACY_INBOUND_CONNECTION_CLASS, "connection");
            DELEGATE_FIELD = ReflectionUtil.getPrivateField(LOGIN_INBOUND_CONNECTION_CLASS, "delegate");
            HANDSHAKE_FIELD = ReflectionUtil.getPrivateField(INITIAL_INBOUND_CONNECTION_CLASS, "handshake");

            CLOSE_CHANNEL_METHOD = MINECRAFT_CONNECTION_CLASS.getMethod("close");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPayload(InboundConnection inboundConnection) {
        try {
            Object initialInboundConnection = DELEGATE_FIELD.get(inboundConnection);
            Object handshake = HANDSHAKE_FIELD.get(initialInboundConnection);

            return (String) SERVER_ADDRESS_FIELD.get(handshake);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get payload", e);
        }
    }

    public static void rewrite(InboundConnection inboundConnection,
                         RestoredPlayerHandshake restoredInfo) {
        try {
            Object initialInboundConnection = DELEGATE_FIELD.get(inboundConnection);
            InetSocketAddress ip = new InetSocketAddress(
                    restoredInfo.getPlayerIp(), restoredInfo.getPlayerPort());

            Object connection = CONNECTION_FIELD.get(initialInboundConnection);
            REMOTE_ADDRESS_FIELD.set(connection, ip);

            Object handshake = HANDSHAKE_FIELD.get(initialInboundConnection);
            SERVER_ADDRESS_FIELD.set(handshake, restoredInfo.getOriginalAddress());

            ReflectionUtil.modifyFinalField(initialInboundConnection,
                    CLEANED_ADDRESS_FIELD, CraftGuardUtil.cleanVhost(restoredInfo.getOriginalAddress()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to rewrite player info", e);
        }
    }

    public static void disconnect(InboundConnection inboundConnection) {
        try {
            boolean isLegacy = inboundConnection.getClass() != INITIAL_INBOUND_CONNECTION_CLASS;
            Object connection = isLegacy ? LEGACY_CONNECTION_FIELD.get(inboundConnection) : CONNECTION_FIELD.get(inboundConnection);

            CLOSE_CHANNEL_METHOD.invoke(connection);
        } catch (Exception e) {
            throw new RuntimeException("Failed to close player connection", e);
        }
    }
}
