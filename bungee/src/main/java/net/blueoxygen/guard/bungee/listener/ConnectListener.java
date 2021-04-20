package net.blueoxygen.guard.bungee.listener;

import lombok.RequiredArgsConstructor;
import net.blueoxygen.guard.bungee.CraftGuardBungee;
import net.blueoxygen.guard.bungee.util.BungeeReflectionUtil;
import net.blueoxygen.guard.core.handshake.RestoredPlayerHandshake;
import net.blueoxygen.guard.core.util.CraftGuardUtil;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.protocol.packet.Handshake;

@RequiredArgsConstructor
public class ConnectListener implements Listener {

    private final CraftGuardBungee plugin;

    @EventHandler
    public void onPlayerHandshake(PlayerHandshakeEvent event) {
        Handshake handshake = event.getHandshake();
        PendingConnection connection = event.getConnection();
        restore(handshake, connection);
    }

    private void restore(Handshake handshake, PendingConnection connection) {
        String payload = handshake.getHost();
        RestoredPlayerHandshake restored = CraftGuardUtil.restorePlayerData(payload);
        if (restored != null) {
            BungeeReflectionUtil.rewrite(handshake, connection, restored);
        } else {
            if (plugin.isOnlyAllowProxyConnections()) {
                connection.disconnect();
            }
        }
    }
}
