package org.by1337.bauction.command.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.by1337.bauction.Main;
import org.by1337.bauction.test.FakePlayer;
import org.by1337.bauction.util.time.TimeCounter;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.argument.ArgumentIntegerAllowedMath;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.requires.RequiresPermission;

import java.util.ArrayList;
import java.util.List;


public class StressCmd extends Command<CommandSender> {
    public StressCmd(String command) {
        super(command);
        requires(new RequiresPermission<>("bauc.admin.debug.stress"));
        argument(new ArgumentIntegerAllowedMath<>("count", List.of("[count]")));
        argument(new ArgumentIntegerAllowedMath<>("repeat", List.of("[repeat]")));
        argument(new ArgumentIntegerAllowedMath<>("cd", List.of("[cd]")));
        argument(new ArgumentIntegerAllowedMath<>("limit", List.of("[limit]")));
        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) {
        int count = (int) args.getOrDefault("count", 1);
        int repeat = (int) args.getOrDefault("repeat", 1);
        int cd = (int) args.getOrDefault("cd", 1);
        int limit = (int) args.getOrDefault("limit", Integer.MAX_VALUE);
        TimeCounter timeCounter = new TimeCounter();
        FakePlayer fakePlayer = new FakePlayer(Main.getStorage(), limit);
        new BukkitRunnable() {
            int x = 0;
            List<Long> list = new ArrayList<>();

            @Override
            public void run() {
                timeCounter.reset();
                x++;
                for (int i = 0; i < count; i++) {
                    fakePlayer.randomAction();
                }
                long time = timeCounter.getTime();
                Main.getMessage().sendMsg(sender, "Completed in %s ms. %s", time, x);
                list.add(time);
                if (x >= repeat) {
                    long l = 0;
                    for (Long l1 : list) {
                        l += l1;
                    }
                    l /= list.size();

                    String s = String.format("Deals per second: %s. Average duration: %s ms. Total deals made: %s. Items on auction: %s.",
                            count * (20 / cd),
                            l,
                            count * repeat,
                            Main.getStorage().getSellItemsCount()
                    );
                    Main.getMessage().logger(s);
                    Main.getMessage().sendMsg(sender, s);
                    cancel();
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 0, cd);
    }
}
