package org.by1337.bauction.menu.impl;

import org.bukkit.entity.Player;
import org.by1337.api.chat.Placeholderable;
import org.by1337.api.command.Command;
import org.by1337.api.command.CommandException;
import org.by1337.api.command.argument.ArgumentString;
import org.by1337.api.util.CyclicList;
import org.by1337.bauction.Main;
import org.by1337.bauction.action.TakeItemProcess;
import org.by1337.bauction.db.MemorySellItem;
import org.by1337.bauction.db.MemoryUser;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.MenuSetting;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;

import javax.annotation.Nullable;
import java.util.*;

public class ItemsForSaleMenu extends Menu {

    private int currentPage = 0;
    private int maxPage = 0;

    private final List<Integer> slots;

    private final Command command;
    private final MemoryUser user;
    private final Menu previous;

    public ItemsForSaleMenu(Player player, MemoryUser user, @Nullable Menu previous) {
        super(Main.getCfg().getMenuManger().getItemsForSaleMenu(), player);
        this.user = user;
        this.previous = previous;
        slots = Main.getCfg().getMenuManger().getItemsForSaleSlots();

        command = new Command("test")
                .addSubCommand(new Command("[NEXT_PAGE]")
                        .executor(((sender, args) -> {
                            if (currentPage < maxPage - 1) {
                                currentPage++;
                                generate0();
                            }
                        }))
                )
                .addSubCommand(new Command("[PREVIOUS_PAGE]")
                        .executor(((sender, args) -> {
                            if (currentPage > 0) {
                                currentPage--;
                                generate0();
                            }
                        }))
                )
                .addSubCommand(new Command("[UPDATE]")
                        .executor(((sender, args) -> {
                            sellItems = null;
                            generate0();
                        }))
                )
                .addSubCommand(new Command("[BACK]")
                        .executor(((sender, args) -> syncUtil(() -> Objects.requireNonNull(previous).reopen())))
                )
                .addSubCommand(new Command("[TAKE_ITEM]")
                        .argument(new ArgumentString("uuid"))
                        .executor(((sender, args) -> {
                            String uuidS = (String) args.getOrThrow("uuid");
                            UUID uuid = UUID.fromString(uuidS);

                            if (!Main.getStorage().hasMemorySellItem(uuid)) {
                                Main.getMessage().sendMsg(player, "&cПредмет уже продан или снят с продажи!");
                                sellItems = null;
                                generate0();
                                return;
                            }
                            MemorySellItem item = Main.getStorage().getMemorySellItem(uuid);
                            new TakeItemProcess(item, user, this, player).process();
                        }))
                )
        ;
    }

    private ArrayList<MemorySellItem> sellItems = null;
    private int lastPage = -1;


    @Override
    protected void generate() {
        if (lastPage != currentPage || sellItems == null) {

            lastPage = currentPage;

            sellItems = new ArrayList<>(Main.getStorage().getAllItemByUser(user.getUuid()));

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
                MemorySellItem item = sellItems.get(x);

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
                command.process(null, holder.replace(cmd).split(" "));
            }
        } catch (CommandException e) {
            Main.getMessage().error(e);
        }
    }

    public void reopen() {
        if (getPlayer() == null || !getPlayer().isOnline()) {
            throw new IllegalArgumentException();
        }
        reRegister();
        getPlayer().openInventory(getInventory());
        sendFakeTitle(replace(title));
        sellItems = null;
        generate0();
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(Main.getMessage().messageBuilder(s, getPlayer()));
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
