package org.by1337.bauction.command.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;
import org.by1337.bauction.command.argument.ArgumentFullOrCount;
import org.by1337.bauction.db.event.SellItemEvent;
import org.by1337.bauction.event.Event;
import org.by1337.bauction.event.EventType;
import org.by1337.bauction.lang.Lang;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentIntegerAllowedMath;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.requires.RequiresPermission;

import java.util.List;

public class SellCmd  extends Command<CommandSender> {
    public SellCmd(String command) {
        super(command);
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

        int cashback = 0;
        if (amount != -1) {
            if (itemStack.getAmount() > amount) {
                cashback = itemStack.getAmount() - amount;
                itemStack.setAmount(amount);
            }
        }

        if (saleByThePiece) saleByThePiece = Main.getCfg().isAllowBuyCount();
        User user = Main.getStorage().getUserOrCreate(player);
        SellItem sellItem = new SellItem(player, itemStack, price, Main.getCfg().getDefaultSellTime() + user.getExternalSellTime(), saleByThePiece);

        for (String tag : sellItem.getTags()) {
            if (Main.getBlackList().contains(tag)) {
                Main.getMessage().sendMsg(player, sellItem.replace(Lang.getMessage("item-in-black-list")));
                return;
            }
        }

        SellItemEvent event = new SellItemEvent(user, sellItem);
        Main.getStorage().validateAndAddItem(event);
        if (event.isValid()) {
            player.getInventory().getItemInMainHand().setAmount(cashback);
            //Main.getMessage().sendMsg(player, sellItem.replace(Lang.getMessage("successful_single_listing")));
            Event event1 = new Event(player, EventType.SELL_ITEM, new BiPlaceholder(sellItem, user));
            Main.getEventManager().onEvent(event1);
        } else {
            Main.getMessage().sendMsg(player, String.valueOf(event.getReason()));
        }
    }
}
