package org.by1337.bauction.command.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.common.db.type.SellItem;
import org.by1337.bauction.db.kernel.PluginSellItem;
import org.by1337.bauction.db.kernel.PluginUser;
import org.by1337.bauction.db.kernel.event.AddSellItemEvent;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.auction.TagUtil;
import org.by1337.bauction.util.time.TimeCounter;
import org.by1337.bauction.util.time.TimeParser;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentIntegerAllowedMath;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.argument.ArgumentString;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.nbt.impl.CompoundTag;

import java.util.List;
import java.util.Random;


public class PushCmd extends Command<CommandSender> {
    public PushCmd(String command) {
        super(command);
        requires(new RequiresPermission<>("bauc.admin.debug.push"));
        argument(new ArgumentIntegerAllowedMath<>("price", List.of(Lang.getMessage("price_tag"))));
        argument(new ArgumentIntegerAllowedMath<>("amount", List.of(Lang.getMessage("quantity_tag"))));
        argument(new ArgumentString<>("time", List.of(Lang.getMessage("sale_time_tag"))));

        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) throws CommandException {
        int amount = (int) args.getOrDefault("amount", 1);
        int price = (int) args.getOrThrow("price", Lang.getMessage("price_not_specified"));
        if (!(sender instanceof Player player))
            throw new CommandException(Lang.getMessage("must_be_player"));

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType().isAir()) {
            throw new CommandException(Lang.getMessage("cannot_trade_air"));
        }
        TimeCounter timeCounter = new TimeCounter();
        Random random = new Random();
        PluginUser user = Main.getStorage().getUserOrCreate(player);
       // user.updateBoosts();
        long time = TimeParser.parse(((String) args.getOrDefault("time", "2d")));
        for (int i = 0; i < amount; i++) {
            PluginSellItem sellItem = new PluginSellItem(new SellItem(
                    PluginSellItem.serializeItemStack(itemStack),
                    player.getName(),
                    player.getUniqueId(),
                    price + random.nextInt(price / 2),
                    false,
                    TagUtil.getTags(itemStack),
                    System.currentTimeMillis(),
                    System.currentTimeMillis() + Main.getCfg().getDefaultSellTime() + user.getExternalSellTime(),
                    Main.getUniqueIdGenerator().nextId(),
                    itemStack.getType().ordinal(),
                    itemStack.getAmount(),
                    Main.getServerId(),
                    new CompoundTag()
            ));
            AddSellItemEvent event = new AddSellItemEvent(sellItem, user);

            if (!Main.getStorage().onEvent(event).join().isValid()) {
                Main.getMessage().sendMsg(player, event.getReason());
                return;
            }
        }
        Main.getMessage().sendMsg(player, Lang.getMessage("successful_listing"), amount, timeCounter.getTime());

    }
}
