package net.blueoxygen.guard.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.ConnectionHandshakeEvent;
import com.velocitypowered.api.proxy.InboundConnection;
import lombok.RequiredArgsConstructor;
import net.blueoxygen.guard.core.handshake.RestoredPlayerHandshake;
import net.blueoxygen.guard.core.util.CraftGuardUtil;
import net.blueoxygen.guard.velocity.CraftGuardVelocity;
import net.blueoxygen.guard.velocity.util.VelocityReflectionUtil;

@RequiredArgsConstructor
public class ConnectListener {

    private final CraftGuardVelocity plugin;

    @Subscribe
    public void onConnectionHandshake(ConnectionHandshakeEvent event) {
        InboundConnection connection = event.getConnection();
        restore(connection);
    }

    private void restore(InboundConnection connection) {
        String payload = VelocityReflectionUtil.getPayload(connection);
        RestoredPlayerHandshake restored = CraftGuardUtil.restorePlayerData(payload);
        if (restored != null) {
            VelocityReflectionUtil.rewrite(connection, restored);
        } else {
            if (plugin.isOnlyAllowProxyConnections()) {
                VelocityReflectionUtil.disconnect(connection);
            }
        }
    }
}
