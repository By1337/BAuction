package org.by1337.bauction.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.MysqlDb;
import org.by1337.bauction.network.PacketConnection;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.network.WaitNotifyCallBack;
import org.by1337.bauction.network.in.PlayInGiveMoneyRequest;
import org.by1337.bauction.network.in.PlayInGiveMoneyResponse;
import org.by1337.bauction.network.out.PlayOutGiveMoneyRequest;
import org.by1337.bauction.network.out.PlayOutGiveMoneyResponse;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MoneyGiver {
    private final PacketConnection connection;
    private final MysqlDb mysqlDb;
    private final String currentServer;
    private final AtomicInteger counter = new AtomicInteger();
    private final Map<Integer, WaitNotifyCallBack<Status>> waiters = new ConcurrentHashMap<>();
    private final ExecutorService executor = new ThreadPoolExecutor(
            10,
            100,
            10L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>()
    );

    public MoneyGiver(MysqlDb mysqlDb) {
        this.mysqlDb = mysqlDb;
        connection = mysqlDb.getPacketConnection();
        currentServer = Main.getServerId();
        connection.register("money_giver", PacketType.GIVE_MONEY_RESPONSE, this::giveResponse);
        connection.register("money_giver", PacketType.GIVE_MONEY_REQUEST, this::giveRequest);
    }

    public void give(Double count, UUID player, String toServer) {
        executor.execute(() -> give0(count, player, toServer));
    }
    private void give0(Double count, UUID player, String toServer) {
        if (connection.hasConnection()) {
            int id = counter.getAndIncrement();
            var packet = new PlayOutGiveMoneyRequest(
                    currentServer,
                    toServer,
                    player,
                    count,
                    id
            );
            connection.saveSend(packet);
            var callBack = new WaitNotifyCallBack<Status>() {
                @Override
                protected void back0(@Nullable Status value) {
                    if (value != Status.OK) {
                        mysqlDb.addSqlToQueue("INSERT INTO give_money (server, uuid, count) VALUES('%s', '%s', %s)", toServer, player, count);
                    }
                }
            };
            waiters.put(id, callBack);

            try {
                callBack.wait_(2500);
                if (waiters.remove(id) != null){
                    mysqlDb.addSqlToQueue("INSERT INTO give_money (server, uuid, count) VALUES('%s', '%s', %s)", toServer, player, count);
                }
            }catch (Exception e){
                Main.getMessage().error(e);
            }

        } else {
            mysqlDb.addSqlToQueue("INSERT INTO give_money (server, uuid, count) VALUES('%s', '%s', %s)", toServer, player, count);
        }
    }

    private PlayInGiveMoneyResponse giveResponse(PlayInGiveMoneyResponse packet) {
        if (packet.getTo().equals(currentServer)) {
            var callBack = waiters.remove(packet.getId());
            if (callBack != null) {
                callBack.back(Status.OK);
            } else {
                Main.getMessage().error("bad packet: " + packet);
            }
        }
        return null;
    }

    private PlayInGiveMoneyRequest giveRequest(PlayInGiveMoneyRequest packet) {
        if (packet.getTo().equals(currentServer)) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(packet.getPlayer());
            Main.getEcon().depositPlayer(offlinePlayer, packet.getCount());
            connection.saveSend(new PlayOutGiveMoneyResponse(packet.getFrom(), packet.getId()));
        }
        return null;
    }

    enum Status {
        OK,
        ERROR
    }
    public void close(){
        executor.shutdown();
    }
}
