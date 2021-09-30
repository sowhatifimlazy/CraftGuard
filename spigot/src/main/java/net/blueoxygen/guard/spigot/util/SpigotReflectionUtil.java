package net.blueoxygen.guard.spigot.util;

import com.comphenix.protocol.injector.netty.ChannelInjector;
import com.comphenix.protocol.injector.server.SocketInjector;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import net.blueoxygen.guard.core.handshake.RestoredPlayerHandshake;
import net.blueoxygen.guard.core.util.ReflectionUtil;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class SpigotReflectionUtil {

    private static final Class ABSTRACT_CHANNEL_CLASS;

    private static final Field INJECTOR_FIELD;
    private static final Field NETWORK_MANAGER_FIELD;
    private static final Field CHANNEL_FIELD;
    private static final Field REMOTE_ADDRESS_FIELD;

    private static Field NETWORK_MANAGER_ADDRESS_FIELD;

    static {
        try {
            ABSTRACT_CHANNEL_CLASS = Class.forName("io.netty.channel.AbstractChannel");

            INJECTOR_FIELD = ReflectionUtil.getPrivateField(
                    ChannelInjector.ChannelSocketInjector.class, "injector");
            NETWORK_MANAGER_FIELD = ReflectionUtil.getPrivateField(ChannelInjector.class, "networkManager");
            CHANNEL_FIELD = ReflectionUtil.getPrivateField(ChannelInjector.class, "originalChannel");
            REMOTE_ADDRESS_FIELD = ReflectionUtil.getPrivateField(ABSTRACT_CHANNEL_CLASS, "remoteAddress");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void rewrite(Player player, RestoredPlayerHandshake restoredInfo) {
        try {
            InetSocketAddress ip = new InetSocketAddress(
                    restoredInfo.getPlayerIp(), restoredInfo.getPlayerPort());

            SocketInjector injector = TemporaryPlayerFactory.getInjectorFromPlayer(player);
            Object channelInjector = INJECTOR_FIELD.get(injector);

            Object networkManager = NETWORK_MANAGER_FIELD.get(channelInjector);
            if (NETWORK_MANAGER_ADDRESS_FIELD == null) {
                NETWORK_MANAGER_ADDRESS_FIELD = ReflectionUtil.getFieldFromType(
                        networkManager.getClass(), SocketAddress.class);
            }

            NETWORK_MANAGER_ADDRESS_FIELD.set(networkManager, ip);

            Object nettyChannel = CHANNEL_FIELD.get(channelInjector);
            ReflectionUtil.modifyFinalField(nettyChannel, REMOTE_ADDRESS_FIELD, ip);
        } catch (Exception e) {
            throw new RuntimeException("Failed to rewrite player info", e);
        }
    }
}
