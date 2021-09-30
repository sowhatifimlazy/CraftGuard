package net.blueoxygen.guard.spigot.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.blueoxygen.guard.core.handshake.RestoredPlayerHandshake;
import net.blueoxygen.guard.core.util.CraftGuardUtil;
import net.blueoxygen.guard.spigot.CraftGuardSpigot;
import net.blueoxygen.guard.spigot.util.SpigotReflectionUtil;
import org.bukkit.entity.Player;

public class ConnectListener extends PacketAdapter {

    private CraftGuardSpigot plugin;

    public ConnectListener(CraftGuardSpigot plugin) {
        super(plugin, ListenerPriority.LOWEST, PacketType.Handshake.Client.SET_PROTOCOL);

        this.plugin = plugin;
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        String payload = event.getPacket().getStrings().read(0);
        restore(player, payload);
    }

    private void restore(Player player, String payload) {
        RestoredPlayerHandshake restored = CraftGuardUtil.restorePlayerData(payload);
        if (restored != null) {
            SpigotReflectionUtil.rewrite(player, restored);
        } else {
            if (plugin.isOnlyAllowProxyConnections()) {
                player.kickPlayer("");
            }
        }
    }
}
