package org.by1337.bauction.network;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.by1337.api.chat.util.Message;
import org.by1337.bauction.Main;
import org.by1337.bauction.network.in.PlayInSendMessagePacket;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class PacketConnection implements Listener, PluginMessageListener {
    private final PacketListener listener;
    private final Plugin plugin;
    private final Message message;
    private final String channelName = "BungeeCord";
    private boolean hasConnection;

    public PacketConnection(PacketListener listener) {
        this.listener = listener;
        plugin = Main.getInstance();
        message = Main.getMessage();
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channelName);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channelName, this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        hasConnection = !Bukkit.getOnlinePlayers().isEmpty();
    }

    public void saveSend(PacketOut packetOut) {
        try {
            if (!hasConnection) return;
            byte[] arr = packetOut.getBytes();
            if (arr.length > Messenger.MAX_MESSAGE_SIZE) {
                return;
            }
            send(arr);
        } catch (IOException e) {
            Main.getMessage().error(e);
        }
    }

    public void send(byte[] arr) {
        if (!hasConnection) {
            throw new IllegalStateException("has no connection!");
        }
        if (arr.length > Messenger.MAX_MESSAGE_SIZE) {
            throw new IllegalStateException("packet to large!");
        }
        try {
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                Player player = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
                try (ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
                     DataOutputStream out = new DataOutputStream(byteBuff)) {
                    out.writeUTF("Forward");
                    out.writeUTF("ALL");
                    out.writeUTF(channelName);
                    out.writeShort(arr.length);
                    out.write(arr);
                    out.flush();
                    player.sendPluginMessage(plugin, channelName, byteBuff.toByteArray());
                }
            } else {
                throw new IllegalStateException("has no connection!");
            }
        } catch (IOException e) {
            message.error("failed to send packet!", e);
        }
    }

    public boolean hasConnection() {
        return hasConnection;
    }

    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
        try (DataInputStream in1 = new DataInputStream(new ByteArrayInputStream(message))) {
            String subChannel = in1.readUTF();
            short len = in1.readShort();
            byte[] msgbytes = new byte[len];
            in1.readFully(msgbytes);

            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(msgbytes))) {
                PacketType type = PacketType.values()[in.readByte()];
                PacketIn packetIn = type.getSuppler().get(in);

                if (type == PacketType.SEND_MESSAGE) {
                    processSendMessagePacket((PlayInSendMessagePacket) packetIn);
                } else {
                    listener.update(packetIn);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void processSendMessagePacket(PlayInSendMessagePacket packet) {
        Player player1 = Bukkit.getPlayer(packet.getReceiver());
        if (player1 != null) {
            Main.getMessage().sendMsg(player1, packet.getMessage());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!hasConnection) {
            hasConnection = true;
            listener.connectionRestored();
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (Bukkit.getOnlinePlayers().size() <= 1) {
            hasConnection = false;
            listener.connectionLost();
        }
    }
}
