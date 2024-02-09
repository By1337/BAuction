package org.by1337.bauction.network;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.bauction.network.impl.PacketSendMessage;
import org.by1337.bauction.serialize.SerializeUtils;
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.util.Pair;
import org.by1337.bauction.Main;
import org.by1337.bauction.network.impl.PacketPingRequest;
import org.by1337.bauction.network.impl.PacketPingResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class PacketConnection implements Listener, PluginMessageListener {
    private final PacketListener listener;
    private final Plugin plugin;
    private final Message message;
    private final String channelName = "BungeeCord";
    private final String subChannelName = "bauction:main";
    private final AtomicBoolean hasConnection = new AtomicBoolean();
    private final Map<String, Map<PacketType<? extends Packet>, Listener<? extends Packet>>> packetListeners = new ConcurrentHashMap<>();
    private final Map<PacketType<? extends Packet>, List<CallBack<? extends Packet>>> callbacks = new ConcurrentHashMap<>();
    private final BukkitTask pingTask;
    private final List<String> serverList = new CopyOnWriteArrayList<>();
    private final String currentServerId;
    private final WaitNotifyCallBack<PacketPingResponse> pingCallBack;

    public PacketConnection(PacketListener listener) {
        this.listener = listener;
        plugin = Main.getInstance();
        message = Main.getMessage();
        currentServerId = Main.getServerId();
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channelName);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channelName, this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        hasConnection.set(!Bukkit.getOnlinePlayers().isEmpty());

        register("message", PacketType.SEND_MESSAGE, this::processSendMessagePacket);
        register("pingListener", PacketType.PING_REQUEST, this::pingResponse);
        pingCallBack = new WaitNotifyCallBack<>() {
            @Override
            protected void back0(PacketPingResponse packet) {
                if (packet.getTo().equals(currentServerId)) {
                    serverList.add(packet.getFrom());
                }
            }
        };
        pingTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), this::pining, 20 * 30, 20 * 30);
    }


    public void pining() {
        if (hasCallBack(PacketType.PING_RESPONSE, pingCallBack) || !hasConnection.get()) return;
        serverList.clear();
        registerCallBack(PacketType.PING_RESPONSE, pingCallBack);
        PacketPingRequest packet = new PacketPingRequest(currentServerId);
        saveSend(packet);
        try {
            pingCallBack.wait_(2000);
            Thread.sleep(200);
        } catch (Exception e) {
            Main.getMessage().error(e);
        }
        unregisterCallBack(PacketType.PING_RESPONSE, pingCallBack);
    }

    public void saveSend(Packet packet) {
        try {
            if (!hasConnection.get()) return;
            byte[] arr;
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 DataOutputStream data = new DataOutputStream(out)) {
                data.writeByte(packet.getType().getId());
                packet.write(data);
                data.flush();
                arr = out.toByteArray();
            }

            if (arr.length > Messenger.MAX_MESSAGE_SIZE) {
                return;
            }
            send(arr);
        } catch (Throwable ignore) {
        }
    }

    public void send(byte[] arr) {
        if (!hasConnection.get()) {
            throw new IllegalStateException("has no connection!");
        }
        if (arr.length > Messenger.MAX_MESSAGE_SIZE) {
            throw new IllegalStateException("packet to large!");
        }
        try {
            if (!Bukkit.getOnlinePlayers().isEmpty()) {
                Player player = Bukkit.getOnlinePlayers().stream().findFirst().orElseThrow(null);
                try (ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
                     DataOutputStream out = new DataOutputStream(byteBuff)) {
                    out.writeUTF("Forward");
                    out.writeUTF("ONLINE");
                    out.writeUTF(subChannelName);
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
        return hasConnection.get();
    }

    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        try (DataInputStream in1 = new DataInputStream(new ByteArrayInputStream(message))) {
            String subChannel = in1.readUTF();
            if (!subChannel.equals(subChannelName)) return;
            short len = in1.readShort();
            byte[] bytes = new byte[len];
            in1.readFully(bytes);
            readAndProcess(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet> @NotNull Pair<@NotNull PacketType<T>, @NotNull T> readBytes(byte[] bytes) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            PacketType<T> type = (PacketType<T>) PacketType.byId(in.readByte());
            T packet = (T) type.getSuppler().get();
            packet.read(in);
            return new Pair<>(type, packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Packet> void readAndProcess(byte[] bytes) {
        Pair<PacketType<T>, T> pair = readBytes(bytes);
        PacketType<T> type = pair.getKey();
        T Packet = pair.getValue();
        for (Map<PacketType<? extends Packet>, Listener<? extends Packet>> map : packetListeners.values()) {
            for (Map.Entry<PacketType<? extends Packet>, Listener<? extends Packet>> entry : map.entrySet()) {
                if (Packet == null) return;
                PacketType<?> type1 = entry.getKey();
                if (type1.equals(type)) {
                    Listener<T> listener1 = (Listener<T>) entry.getValue();
                    Packet = listener1.accept(Packet);
                }
            }
        }
        if (Packet == null) return;

        List<CallBack<? extends Packet>> list = callbacks.get(type);
        if (list != null) {
            for (Object o : list.toArray()) {
                ((CallBack<T>) o).back(Packet);
            }
        }
        listener.update(Packet);
    }

    public PacketSendMessage processSendMessagePacket(PacketSendMessage packet) {
        Player player1 = Bukkit.getPlayer(packet.getReceiver());
        if (player1 != null) {
            Main.getMessage().sendMsg(player1, packet.getMessage());
        }
        return null;
    }

    public PacketPingRequest pingResponse(PacketPingRequest packet) {
        if (packet.getTo().equals("any") || packet.getTo().equals(Main.getServerId())) {
            PacketPingResponse packet1 = new PacketPingResponse(
                    (int) (System.currentTimeMillis() - packet.getTime()),
                    Main.getServerId(),
                    packet.getServer()
            );
            saveSend(packet1);
            return null;
        }
        return packet;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!hasConnection.get()) {
            hasConnection.set(true);
            new Thread(this::pining);
            listener.connectionRestored();
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (Bukkit.getOnlinePlayers().size() <= 1) {
            hasConnection.set(false);
            listener.connectionLost();
        }
    }

    public void close() {
        HandlerList.unregisterAll(this);
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, channelName);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, channelName);
        pingTask.cancel();
    }

    public <T extends Packet> boolean hasCallBack(PacketType<T> type, CallBack<T> callBack) {
        List<CallBack<? extends Packet>> list = callbacks.getOrDefault(type, new ArrayList<>());
        return list.contains(callBack);
    }

    public <T extends Packet> void registerCallBack(PacketType<T> type, CallBack<T> callBack) {
        List<CallBack<? extends Packet>> list = callbacks.getOrDefault(type, new CopyOnWriteArrayList<>());
        if (list.contains(callBack)) {
            throw new IllegalStateException("callback already exist!");
        }
        list.add(callBack);
        callbacks.put(type, list);
    }

    public <T extends Packet> void unregisterCallBack(PacketType<T> type, CallBack<T> callBack) {
        List<CallBack<? extends Packet>> list = callbacks.getOrDefault(type, new CopyOnWriteArrayList<>());
        if (!list.remove(callBack)) {
            throw new IllegalStateException("callback non-exist!");
        }
        callbacks.put(type, list);
    }

    public <T extends Packet> void register(String listenerName, PacketType<T> packetType, Listener<T> listener) {
        Map<PacketType<? extends Packet>, Listener<? extends Packet>> listeners = packetListeners.getOrDefault(listenerName, new ConcurrentHashMap<>());
        if (listeners.containsKey(packetType)) {
            throw new IllegalStateException("listener for " + packetType.ordinal() + " already exist!");
        }
        listeners.put(packetType, listener);
        packetListeners.put(listenerName, listeners);
    }

    public void unregister(String listenerName, PacketType<? extends Packet> packetType) {
        Map<PacketType<? extends Packet>, Listener<? extends Packet>> listeners = packetListeners.get(listenerName);
        if (listeners != null) {
            listeners.remove(packetType);
            if (listeners.isEmpty()) {
                packetListeners.remove(listenerName);
            }
        }
    }

    public boolean hasListener(String listener) {
        return packetListeners.containsKey(listener);
    }

    public List<String> getServerList() {
        return serverList;
    }

    @FunctionalInterface
    public interface Listener<T extends Packet> {
        @Nullable
        T accept(T paket);
    }
}
