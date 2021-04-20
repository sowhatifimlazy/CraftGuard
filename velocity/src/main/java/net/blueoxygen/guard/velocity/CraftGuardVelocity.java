package net.blueoxygen.guard.velocity;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.blueoxygen.guard.velocity.listener.ConnectListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

@Getter
@Plugin(
        id = "craftguard",
        name = "CraftGuard",
        authors = "BlueOxygen",
        version = "1.0-SNAPSHOT"
)
public class CraftGuardVelocity {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataFolder;

    @Getter
    private boolean onlyAllowProxyConnections;

    @Inject
    public CraftGuardVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent e) {
        Toml config = this.getConfig();
        this.onlyAllowProxyConnections = config.getBoolean("only-allow-proxy-connections", true);

        this.server.getEventManager().register(this, new ConnectListener(this));
    }

    private Toml getConfig() {
        File dataFolder = this.getDataFolder().toFile();
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        File file = new File(dataFolder, "config.toml");

        if (!file.exists()) {
            try (InputStream in = getClass().getClassLoader()
                    .getResourceAsStream("config.toml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new Toml().read(file);
    }
}
