package net.blueoxygen.guard.spigot;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import lombok.Getter;
import net.blueoxygen.guard.spigot.listener.ConnectListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class CraftGuardSpigot extends JavaPlugin {

    @Getter
    private ProtocolManager protocolManager;

    @Getter
    private boolean onlyAllowProxyConnections;

    @Override
    public void onEnable() {
        FileConfiguration config = this.getFileConfig();
        this.onlyAllowProxyConnections = config.getBoolean("only-allow-proxy-connections", true);

        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.protocolManager.addPacketListener(new ConnectListener(this));
    }

    public FileConfiguration getFileConfig() {
        this.saveDefaultConfig();

        return this.getConfig();
    }
}
