package net.blueoxygen.guard.bungee;

import lombok.Getter;
import net.blueoxygen.guard.bungee.listener.ConnectListener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class CraftGuardBungee extends Plugin {

    @Getter
    private boolean onlyAllowProxyConnections;

    @Override
    public void onEnable() {
        Configuration config = this.getConfig();
        this.onlyAllowProxyConnections = config.getBoolean("only-allow-proxy-connections", true);

        this.getProxy().getPluginManager().registerListener(this, new ConnectListener(this));
    }

    private Configuration getConfig() {
        File dataFolder = this.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        File file = new File(dataFolder, "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Configuration config = null;
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(dataFolder, "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return config;
    }
}
