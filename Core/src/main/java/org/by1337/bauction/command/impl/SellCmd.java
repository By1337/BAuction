package org.by1337.bauction.command.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.common.db.type.SellItem;
import org.by1337.bauction.db.kernel.PluginSellItem;
import org.by1337.bauction.db.kernel.PluginUser;
import org.by1337.bauction.command.argument.ArgumentFullOrCount;
import org.by1337.bauction.db.kernel.event.AddSellItemEvent;
import org.by1337.bauction.event.Event;
import org.by1337.bauction.event.EventType;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.auction.TagUtil;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentIntegerAllowedMath;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.nbt.impl.CompoundTag;

import java.util.List;
import java.util.Set;

public class SellCmd extends Command<CommandSender> {
    private final Set<String> blackList;
    public SellCmd(String command, Set<String> blackList) {
        super(command);
        this.blackList = blackList;
        requires(new RequiresPermission<>("bauc.sell"));
        requires(sender -> sender instanceof Player);
        argument(new ArgumentIntegerAllowedMath<>("price", List.of(Lang.getMessage("price_tag")),
                Main.getCfg().getConfig().getAsInteger("offer-min-price", 1),
                Main.getCfg().getConfig().getAsInteger("offer-max-price", Integer.MAX_VALUE)
        ));
        argument(new ArgumentFullOrCount("arg"));
        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) throws CommandException {
        Player player = (Player) sender;

        int price = (int) args.getOrThrow("price", Lang.getMessage("price_not_specified"));
        String saleByThePieceS = String.valueOf(args.getOrDefault("arg", "full:false"));

        boolean saleByThePiece = !(saleByThePieceS.equals("full"));

        int amount = -1;
        if (!saleByThePieceS.startsWith("f")) {
            try {
                amount = Integer.parseInt(saleByThePieceS);
            } catch (NumberFormatException e) {
                Main.getMessage().sendMsg(sender, Lang.getMessage("count-req"));
                return;
            }
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
        if (itemStack.getType().isAir()) {
            throw new CommandException(Lang.getMessage("cannot_trade_air"));
        }

        final int cashback;
        if (amount != -1) {
            if (itemStack.getAmount() > amount) {
                cashback = itemStack.getAmount() - amount;
                itemStack.setAmount(amount);
            } else {
                cashback = 0;
            }
        } else {
            cashback = 0;
        }

        if (saleByThePiece) saleByThePiece = Main.getCfg().isAllowBuyCount();
        PluginUser user = Main.getStorage().getUserOrCreate(player);
       // user.updateBoosts();

        PluginSellItem sellItem = new PluginSellItem(new SellItem(
                PluginSellItem.serializeItemStack(itemStack),
                player.getName(),
                player.getUniqueId(),
                price,
                saleByThePiece,
                TagUtil.getTags(itemStack),
                System.currentTimeMillis(),
                System.currentTimeMillis() + Main.getCfg().getDefaultSellTime() + user.getExternalSellTime(),
                Main.getUniqueIdGenerator().nextId(),
                itemStack.getType().ordinal(),
                itemStack.getAmount(),
                Main.getServerId(),
                new CompoundTag()
        ));

        for (String tag : sellItem.getTags()) {
            if (blackList.contains(tag)) {
                Main.getMessage().sendMsg(player, sellItem.replace(Lang.getMessage("item-in-black-list")));
                return;
            }
        }

        AddSellItemEvent event = new AddSellItemEvent(sellItem, user);
        Main.getStorage().onEvent(event).whenComplete((e, t) -> {
            if (event.isValid()) {
                player.getInventory().getItemInMainHand().setAmount(cashback);
                //Main.getMessage().sendMsg(player, sellItem.replace(Lang.getMessage("successful_single_listing")));
                Event event1 = new Event(player, EventType.SELL_ITEM, new BiPlaceholder(sellItem, user));
                Main.getEventManager().onEvent(event1);
            } else {
                Main.getMessage().sendMsg(player, event.getReason());
            }
        });

    }
}
