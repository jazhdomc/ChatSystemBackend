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
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class ChatSystemBackend extends JavaPlugin implements Listener, PluginMessageListener {
    private final String channel = "chatsystem:global";
    private Logger log;

    @Override
    public void onEnable() {
        this.log = getLogger();
        log.log(Level.INFO, "Starting ChatSystemBackend...");

        // Register chat listener and plugin messenger
        Server server = getServer();
        Messenger messenger = server.getMessenger();
        messenger.registerIncomingPluginChannel(this, channel, this);
        messenger.registerOutgoingPluginChannel(this, channel);
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
        event.getPlayer().sendPluginMessage(this, channel, data.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // Only handle logic if its for this channel
        if (!channel.equals(this.channel)) return;

        // Broadcast
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', ByteStreams.newDataInput(message).readUTF()));
    }
}