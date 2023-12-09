package org.by1337.bauction.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.by1337.api.chat.Placeholderable;
import org.by1337.api.command.Command;
import org.by1337.api.command.CommandException;
import org.by1337.api.command.argument.ArgumentSetList;
import org.by1337.api.command.argument.ArgumentString;
import org.by1337.api.command.argument.ArgumentStrings;
import org.by1337.bauction.Main;
import org.by1337.bauction.action.TakeUnsoldItemProcess;
import org.by1337.bauction.auc.UnsoldItem;
import org.by1337.bauction.auc.User;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.command.DefaultMenuCommand;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.bauction.util.Pair;
import org.by1337.bauction.util.UniqueName;

import javax.annotation.Nullable;
import java.util.*;

public class UnsoldItemsMenu extends Menu {

    private int currentPage = 0;
    private int maxPage = 0;

    private final List<Integer> slots;

    private final Command<Pair<Menu, Player>> command;
    private final User user;


    public UnsoldItemsMenu(Player player, User user) {
        this(player, user, null);
    }

    public UnsoldItemsMenu(Player player, User user, @Nullable Menu backMenu) {
        super(Main.getCfg().getMenuManger().getUnsoldItems(), player, backMenu, user);
        this.user = user;
        slots = Main.getCfg().getMenuManger().getUnsoldItemsSlots();

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
                            unsoldItems = null;
                            generate0();
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[TAKE_ITEM]")
                        .argument(new ArgumentString<>("uuid"))
                        .argument(new ArgumentSetList<>("fast", List.of("fast")))
                        .executor(((sender, args) -> {
                            boolean fast = args.getOrDefault("fast", "").equals("fast");
                            String uuidS = (String) args.getOrThrow("uuid");
                            //  UUID uuid = UUID.fromString(uuidS);
                            UniqueName uuid = new CUniqueName(uuidS);

                            UnsoldItem unsoldItem = unsoldItems.stream().filter(i -> i.getUniqueName().equals(uuid)).findFirst().orElse(null);

                            if (unsoldItem == null) {
                                Main.getMessage().sendMsg(player, Lang.getMessages("item_no_longer_exists"));
                                unsoldItems = null;
                                generate0();
                                return;
                            }
                            new TakeUnsoldItemProcess(unsoldItem, user, this, player, fast).process();
                        }))
                )
        ;
        DefaultMenuCommand.command.getSubcommands().forEach((s, c) -> command.addSubCommand(c));
    }

    private ArrayList<UnsoldItem> unsoldItems = null;
    private int lastPage = -1;

    @Override
    protected void generate() {
        if (lastPage != currentPage || unsoldItems == null) {

            lastPage = currentPage;
            unsoldItems = new ArrayList<>();
            Main.getStorage().forEachUnsoldItemsByUser(unsoldItems::add, user.getUuid());

          //  unsoldItems = new ArrayList<>(Main.getStorage().getUnsoldItemsByUser(user.getUuid()));

            maxPage = (int) Math.ceil((double) unsoldItems.size() / slots.size());

            if (currentPage > maxPage) {
                currentPage = maxPage - 1;
                if (currentPage < 0) currentPage = 0;
            }

            if (currentPage * slots.size() >= unsoldItems.size()) {
                maxPage = 0;
            }

            Iterator<Integer> slotsIterator = slots.listIterator();
            customItemStacks.clear();
            for (int x = currentPage * slots.size(); x < unsoldItems.size(); x++) {
                UnsoldItem item = unsoldItems.get(x);

                if (slotsIterator.hasNext()) {
                    int slot = slotsIterator.next();
                    CustomItemStack customItemStack;
                    customItemStack = Main.getCfg().getConfig().getAs("unsold-item", CustomItemStack.class);
                    customItemStack.setItemStack(item.getItemStack());
                    customItemStack.setSlots(new int[]{slot});
                    customItemStack.registerPlaceholder(item);
                    customItemStack.setAmount(item.getItemStack().getAmount());
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
        if (viewer == null || !viewer.isOnline()) {
            throw new IllegalArgumentException();
        }
        syncUtil(() -> {
            reRegister();
            if (!viewer.getOpenInventory().getTopInventory().equals(inventory))
                viewer.openInventory(getInventory());
            sendFakeTitle(replace(title));
            unsoldItems = null;
            generate0();
        });
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
