package net.blueoxygen.guard.bungee.util;

import io.netty.channel.AbstractChannel;
import net.blueoxygen.guard.core.handshake.RestoredPlayerHandshake;
import net.blueoxygen.guard.core.util.CraftGuardUtil;
import net.blueoxygen.guard.core.util.ReflectionUtil;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.protocol.packet.Handshake;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;

public class BungeeReflectionUtil {
    private static final Class HANDSHAKE_CLASS;
    private static final Class CHANNEL_WRAPPER_CLASS;
    private static final Class INITIAL_HANDLER_CLASS;

    private static final Field INITIAL_HANDLER_CHANNEL_FIELD;
    private static final Field NETTY_CHANNEL_FIELD;

    private static final Field WRAPPER_REMOTE_ADDRESS_FIELD;
    private static final Field REMOTE_ADDRESS_FIELD;
    private static final Field LOCAL_ADDRESS_FIELD;

    private static final Field CONNECTION_VIRTUAL_HOST_FIELD;
    private static final Field HANDSHAKE_HOST_FIELD;

    static {
        try {
            HANDSHAKE_CLASS = Class.forName("net.md_5.bungee.protocol.packet.Handshake");
            CHANNEL_WRAPPER_CLASS = Class.forName("net.md_5.bungee.netty.ChannelWrapper");
            INITIAL_HANDLER_CLASS = Class.forName("net.md_5.bungee.connection.InitialHandler");

            INITIAL_HANDLER_CHANNEL_FIELD = ReflectionUtil.getPrivateField(INITIAL_HANDLER_CLASS, "ch");
            NETTY_CHANNEL_FIELD = ReflectionUtil.getPrivateField(CHANNEL_WRAPPER_CLASS, "ch");

            WRAPPER_REMOTE_ADDRESS_FIELD = ReflectionUtil.getPrivateField(CHANNEL_WRAPPER_CLASS, "remoteAddress");
            REMOTE_ADDRESS_FIELD = ReflectionUtil.getPrivateField(AbstractChannel.class, "remoteAddress");
            LOCAL_ADDRESS_FIELD = ReflectionUtil.getPrivateField(AbstractChannel.class, "localAddress");

            Field virtualHost;
            try {
                virtualHost = ReflectionUtil.getPrivateField(INITIAL_HANDLER_CLASS, "virtualHost");
            } catch (Exception e) {
                virtualHost = ReflectionUtil.getPrivateField(INITIAL_HANDLER_CLASS, "vHost");
            }

            CONNECTION_VIRTUAL_HOST_FIELD = virtualHost;
            HANDSHAKE_HOST_FIELD = ReflectionUtil.getPrivateField(HANDSHAKE_CLASS, "host");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void rewrite(Handshake handshake, PendingConnection connection,
                               RestoredPlayerHandshake restoredInfo) {
        try {
            InetSocketAddress ip = new InetSocketAddress(
                    restoredInfo.getPlayerIp(), restoredInfo.getPlayerPort());

            InetSocketAddress virtualHostAddress = new InetSocketAddress(
                    CraftGuardUtil.cleanVhost(restoredInfo.getOriginalAddress()), handshake.getPort());

            Object channelWrapper = INITIAL_HANDLER_CHANNEL_FIELD.get(connection);
            Object nettyChannel = NETTY_CHANNEL_FIELD.get(channelWrapper);

            WRAPPER_REMOTE_ADDRESS_FIELD.set(channelWrapper, ip);
            REMOTE_ADDRESS_FIELD.set(nettyChannel, ip);
            LOCAL_ADDRESS_FIELD.set(nettyChannel, ip);

            ReflectionUtil.modifyFinalField(connection, CONNECTION_VIRTUAL_HOST_FIELD, virtualHostAddress);
            ReflectionUtil.modifyFinalField(handshake, HANDSHAKE_HOST_FIELD, restoredInfo.getOriginalAddress());
        } catch (Exception e) {
            throw new RuntimeException("Failed to rewrite player info", e);
        }
    }
}
