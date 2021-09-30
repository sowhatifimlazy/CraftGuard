package net.blueoxygen.guard.spigot;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;
import net.blueoxygen.guard.spigot.listener.ConnectListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftGuardSpigot extends JavaPlugin {

    @Getter
    private ProtocolManager protocolManager;

    @Getter
    private boolean onlyAllowProxyConnections;

    @Override
    public void onEnable() {
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        this.protocolManager.addPacketListener(new ConnectListener(this));
    }
}
