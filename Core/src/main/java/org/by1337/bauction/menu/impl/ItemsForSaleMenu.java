package org.by1337.bauction.menu.impl;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentSetList;
import org.by1337.blib.command.argument.ArgumentString;
import org.by1337.bauction.Main;
import org.by1337.bauction.action.TakeItemProcess;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.command.DefaultMenuCommand;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.blib.util.Pair;
import org.by1337.bauction.api.util.UniqueName;

import javax.annotation.Nullable;
import java.util.*;

public class ItemsForSaleMenu extends Menu {

    private int currentPage = 0;
    private int maxPage = 0;

    private final List<Integer> slots;

    private final Command<Pair<Menu, Player>> command;
    private final User user;

    public ItemsForSaleMenu(Player player, User user) {
        this(player, user, null);
    }

    public ItemsForSaleMenu(Player player, User user, @Nullable Menu backMenu) {
        super(Main.getCfg().getMenuManger().getItemsForSaleMenu(), player, backMenu, user);
        this.user = user;
        slots = Main.getCfg().getMenuManger().getItemsForSaleSlots();

        command = new Command<Pair<Menu, Player>>("test")
                .addSubCommand(new Command<Pair<Menu, Player>>("[NEXT_PAGE]")
                        .executor(((sender, args) -> {
                            if (currentPage < maxPage - 1) {
                                currentPage++;
                                generate0();
                            }
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[PREVIOUS_PAGE]")
                        .executor(((sender, args) -> {
                            if (currentPage > 0) {
                                currentPage--;
                                generate0();
                            }
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[UPDATE]")
                        .executor(((sender, args) -> {
                            sellItems = null;
                            generate0();
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[TAKE_ITEM]")
                        .argument(new ArgumentString<>("uuid"))
                        .argument(new ArgumentSetList<>("fast", List.of("fast")))
                        .executor(((sender, args) -> {
                            boolean fast = args.getOrDefault("fast", "").equals("fast");
                            String uuidS = (String) args.getOrThrow("uuid");
                         //   UUID uuid = UUID.fromString(uuidS);
                            UniqueName uuid = new CUniqueName(uuidS);

                            if (!Main.getStorage().hasSellItem(uuid)) {
                                Main.getMessage().sendMsg(player, Lang.getMessage("item_already_sold_or_removed"));
                                sellItems = null;
                                generate0();
                                return;
                            }
                            SellItem item = Main.getStorage().getSellItem(uuid);
                            new TakeItemProcess(item, user, this, player, fast).process();
                        }))
                )
        ;
        DefaultMenuCommand.command.getSubcommands().forEach((s, c) -> command.addSubCommand(c));
    }

    private ArrayList<SellItem> sellItems = null;
    private int lastPage = -1;


    @Override
    protected void generate() {
        if (lastPage != currentPage || sellItems == null) {

            lastPage = currentPage;

            sellItems = new ArrayList<>();
            Main.getStorage().forEachSellItemsByUser(sellItems::add, user.getUuid());
          //  sellItems = new ArrayList<>(Main.getStorage().getSellItemsByUser(user.getUuid()));

            maxPage = (int) Math.ceil((double) sellItems.size() / slots.size());

            if (currentPage > maxPage) {
                currentPage = maxPage - 1;
                if (currentPage < 0) currentPage = 0;
            }

            if (currentPage * slots.size() >= sellItems.size()) {
                maxPage = 0;
            }

            Iterator<Integer> slotsIterator = slots.listIterator();
            customItemStacks.clear();
            for (int x = currentPage * slots.size(); x < sellItems.size(); x++) {
                SellItem item = sellItems.get(x);

                if (slotsIterator.hasNext()) {
                    int slot = slotsIterator.next();
                    CustomItemStack customItemStack;
                    if (item.getSellerUuid().equals(user.getUuid())) {
                        customItemStack = Main.getCfg().getConfig().getAs("take-item", CustomItemStack.class);
                    } else if (item.getAmount() == 1) {
                        customItemStack = Main.getCfg().getConfig().getAs("selling-item-one", CustomItemStack.class);
                    } else if (item.isSaleByThePiece()) {
                        customItemStack = Main.getCfg().getConfig().getAs("selling-item", CustomItemStack.class);
                    } else {
                        customItemStack = Main.getCfg().getConfig().getAs("selling-item-only-full", CustomItemStack.class);
                    }
                    customItemStack.setItemStack(item.getItemStack());
                    customItemStack.setSlots(new int[]{slot});
                    customItemStack.registerPlaceholder(item);
                    customItemStack.setAmount(item.getAmount());
                    customItemStacks.add(customItemStack);

                }
            }
        }
    }


    @Override
    public void runCommand(Placeholderable holder, String... commands) {
        try {
            for (String cmd : commands) {
                command.process(new Pair<>(this, viewer), holder.replace(cmd).split(" "));
            }
        } catch (CommandException e) {
            Main.getMessage().error(e);
        }
    }
    public void reopen() {
        if (getPlayer() == null || !getPlayer().isOnline()) {
            throw new IllegalStateException("player is offline!");
        }
        syncUtil(() -> {
            if (!viewer.getOpenInventory().getTopInventory().equals(inventory)) {
                viewer.openInventory(getInventory());
                reRegister();
            }
            sellItems = null;
            sendFakeTitle(replace(title));
            generate0();
        });
    }
    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(Main.getMessage().messageBuilder(s, viewer));
        while (true) {
            if (sb.indexOf("{max_page}") != -1) {
                sb.replace(sb.indexOf("{max_page}"), sb.indexOf("{max_page}") + "{max_page}".length(), String.valueOf(maxPage == 0 ? 1 : maxPage));
                continue;
            }
            if (sb.indexOf("{current_page}") != -1) {
                sb.replace(sb.indexOf("{current_page}"), sb.indexOf("{current_page}") + "{current_page}".length(), String.valueOf(currentPage + 1));
                continue;
            }
            break;
        }
        String str = sb.toString();
        for (Placeholderable val : customPlaceHolders) {
            str = val.replace(str);
        }
        return str;
    }
}
