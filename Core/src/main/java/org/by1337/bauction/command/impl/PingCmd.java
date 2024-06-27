package org.by1337.bauction.command.impl;

import org.bukkit.command.CommandSender;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.MysqlDb;
import org.by1337.bauction.network.PacketConnection;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.network.WaitNotifyCallBack;
import org.by1337.bauction.network.impl.PacketPingRequest;
import org.by1337.bauction.network.impl.PacketPingResponse;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.argument.ArgumentSetList;
import org.by1337.blib.command.requires.RequiresPermission;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;


public class PingCmd extends Command<CommandSender> {
    public PingCmd(String command) {
        super(command);
        requires(new RequiresPermission<>("bauc.admin.debug.ping"));
        requires(new RequiresPermission<>("bauc.admin.ping"));
        requires((sender -> Main.getStorage() instanceof MysqlDb));
        argument(new ArgumentSetList<>("server", () -> ((MysqlDb) Main.getStorage()).getPacketConnection().getServerList()));

        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) {
        new Thread(() -> {
            String server = (String) args.getOrDefault("server", "any");
            PacketConnection connection = ((MysqlDb) Main.getStorage()).getPacketConnection();
            if (!connection.hasConnection()) {
                Main.getMessage().sendMsg(sender, "&cHas no connection!");
                return;
            }
            Main.getMessage().sendMsg(sender, "&fInit...");
            connection.pining();

            int servers = server.equals("any") ? 1 : connection.getServerList().size();

            AtomicInteger response = new AtomicInteger();
            WaitNotifyCallBack<PacketPingResponse> callBack = new WaitNotifyCallBack<>() {
                @Override
                protected void back0(@Nullable PacketPingResponse packet) {
                    if (packet.getTo().equals(Main.getServerId())) {
                        Main.getMessage().sendMsg(sender, "&aPing '%s' %s ms.", packet.getFrom(), packet.getPing());
                        response.getAndIncrement();
                    }
                }
            };
            connection.registerCallBack(PacketType.PING_RESPONSE, callBack);
            int lost = 0;
            for (int i = 1; i <= 5; i++) {
                int last = response.get();
                Main.getMessage().sendMsg(sender, "&7Start pinging server %s... trying %s", server, i);
                PacketPingRequest packet = new PacketPingRequest(Main.getServerId(), server);
                connection.saveSend(packet);

                for (int i1 = 0; i1 < servers; i1++) {
                    try {
                        callBack.wait_(2000);
                    } catch (Exception e) {
                    }
                }

                if (last - response.get() == 0) {
                    Main.getMessage().sendMsg(sender, "&cLost...");
                    lost++;
                }
            }
            connection.unregisterCallBack(PacketType.PING_RESPONSE, callBack);
            Main.getMessage().sendMsg(sender, "&7finish. Lost %s", lost);
        }).start();
    }
}
