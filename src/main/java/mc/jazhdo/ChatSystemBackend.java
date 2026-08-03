package mc.jazhdo;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.chat.TextComponent;

public class ChatSystemBackend extends JavaPlugin implements Listener, PluginMessageListener {
    private final String global = "chatsystem:global", single = "chatsystem:single";
    private Logger log;

    @Override
    public void onEnable() {
        this.log = getLogger();
        log.log(Level.INFO, "Starting ChatSystemBackend...");

        // Register chat listener and plugin messenger
        Server server = getServer();
        Messenger messenger = server.getMessenger();
        messenger.registerIncomingPluginChannel(this, global, this);
        messenger.registerOutgoingPluginChannel(this, global);
        messenger.registerIncomingPluginChannel(this, single, this);
        server.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        log.log(Level.INFO, "Shutting down ChatSystemBackend...");
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        // Make sure the unformatted chat doesn't go through
        event.setCancelled(true);

        // Build data stream
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeUTF(event.getPlayer().getDisplayName());
        data.writeUTF(event.getMessage());

        // Send plugin message
        event.getPlayer().sendPluginMessage(this, global, data.toByteArray());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Disable join messages for the better formatted one from the proxy
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Disable quit messages for the better formatted one from the proxy
        event.setQuitMessage(null);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // Broadcast message or send it to a single player
        if (channel.equals(this.global)) Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', ByteStreams.newDataInput(message).readUTF()));
        else if (channel.equals(this.single)) {
            // Get input stream
            ByteArrayDataInput input = ByteStreams.newDataInput(message);

            // Get player if exists
            Player p = Bukkit.getPlayer(input.readUTF());
            if (p == null) return;

            // Send the player the message
            p.spigot().sendMessage(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', input.readUTF())));
        }
    }
}