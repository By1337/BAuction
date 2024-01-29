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
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.util.Pair;
import org.by1337.bauction.Main;
import org.by1337.bauction.network.in.PlayInPingRequestPacket;
import org.by1337.bauction.network.in.PlayInPingResponsePacket;
import org.by1337.bauction.network.in.PlayInSendMessagePacket;
import org.by1337.bauction.network.out.PlayOutPingRequestPacket;
import org.by1337.bauction.network.out.PlayOutPingResponsePacket;
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
    private final Map<String, Map<PacketType<? extends PacketIn>, Listener<? extends PacketIn>>> packetListeners = new ConcurrentHashMap<>();
    private final Map<PacketType<? extends PacketIn>, List<CallBack<? extends PacketIn>>> callbacks = new ConcurrentHashMap<>();
    private final BukkitTask pingTask;
    private final List<String> serverList = new CopyOnWriteArrayList<>();
    private final String currentServerId;
    private final WaitNotifyCallBack<PlayInPingResponsePacket> pingCallBack;

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
            protected void back0(PlayInPingResponsePacket packet) {
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
        PlayOutPingRequestPacket packet = new PlayOutPingRequestPacket(currentServerId);
        saveSend(packet);
        try {
            pingCallBack.wait_(2000);
            Thread.sleep(200);
        } catch (Exception e) {
            Main.getMessage().error(e);
        }
        unregisterCallBack(PacketType.PING_RESPONSE, pingCallBack);
    }

    public void saveSend(PacketOut packetOut) {
        try {
            if (!hasConnection.get()) return;
            byte[] arr = packetOut.getBytes();
            if (arr.length > Messenger.MAX_MESSAGE_SIZE) {
                return;
            }
            send(arr);
        } catch (Throwable ignore) {
            //Main.getMessage().error(ignore);
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
    public <T extends PacketIn> @NotNull Pair<@NotNull PacketType<T>, @NotNull T> readBytes(byte[] bytes) {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            PacketType<T> type = (PacketType<T>) PacketType.byId(in.readByte());
            T packetIn = (T) type.getSuppler().get(in);
            return new Pair<>(type, packetIn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @SuppressWarnings("unchecked")
    private <T extends PacketIn> void readAndProcess(byte[] bytes) {
        Pair<PacketType<T>, T> pair = readBytes(bytes);
        PacketType<T> type = pair.getKey();
        T packetIn = pair.getValue();
        for (Map<PacketType<? extends PacketIn>, Listener<? extends PacketIn>> map : packetListeners.values()) {
            for (Map.Entry<PacketType<? extends PacketIn>, Listener<? extends PacketIn>> entry : map.entrySet()) {
                if (packetIn == null) return;
                PacketType<?> type1 = entry.getKey();
                if (type1.equals(type)) {
                    Listener<T> listener1 = (Listener<T>) entry.getValue();
                    packetIn = listener1.accept(packetIn);
                }
            }
        }
        if (packetIn == null) return;

        List<CallBack<? extends PacketIn>> list = callbacks.get(type);
        if (list != null){
            for (Object o : list.toArray()) {
                ((CallBack<T>) o).back(packetIn);
            }
        }
        listener.update(packetIn);
    }

    public PlayInSendMessagePacket processSendMessagePacket(PlayInSendMessagePacket packet) {
        Player player1 = Bukkit.getPlayer(packet.getReceiver());
        if (player1 != null) {
            Main.getMessage().sendMsg(player1, packet.getMessage());
        }
        return null;
    }

    public PlayInPingRequestPacket pingResponse(PlayInPingRequestPacket packet) {
        if (packet.getTo().equals("any") || packet.getTo().equals(Main.getServerId())) {
            PlayOutPingResponsePacket packet1 = new PlayOutPingResponsePacket(
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

    public <T extends PacketIn> boolean hasCallBack(PacketType<T> type, CallBack<T> callBack) {
        List<CallBack<? extends PacketIn>> list = callbacks.getOrDefault(type, new ArrayList<>());
        return list.contains(callBack);
    }

    public <T extends PacketIn> void registerCallBack(PacketType<T> type, CallBack<T> callBack) {
        List<CallBack<? extends PacketIn>> list = callbacks.getOrDefault(type, new CopyOnWriteArrayList<>());
        if (list.contains(callBack)) {
            throw new IllegalStateException("callback already exist!");
        }
        list.add(callBack);
        callbacks.put(type, list);
    }

    public <T extends PacketIn> void unregisterCallBack(PacketType<T> type, CallBack<T> callBack) {
        List<CallBack<? extends PacketIn>> list = callbacks.getOrDefault(type, new CopyOnWriteArrayList<>());
        if (!list.remove(callBack)) {
            throw new IllegalStateException("callback non-exist!");
        }
        callbacks.put(type, list);
    }

    public <T extends PacketIn> void register(String listenerName, PacketType<T> packetType, Listener<T> listener) {
        Map<PacketType<? extends PacketIn>, Listener<? extends PacketIn>> listeners = packetListeners.getOrDefault(listenerName, new ConcurrentHashMap<>());
        if (listeners.containsKey(packetType)) {
            throw new IllegalStateException("listener for " + packetType.ordinal() + " already exist!");
        }
        listeners.put(packetType, listener);
        packetListeners.put(listenerName, listeners);
    }

    public void unregister(String listenerName, PacketType<? extends PacketIn> packetType) {
        Map<PacketType<? extends PacketIn>, Listener<? extends PacketIn>> listeners = packetListeners.get(listenerName);
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
    public interface Listener<T extends PacketIn> {
        @Nullable
        T accept(T paket);
    }
}
