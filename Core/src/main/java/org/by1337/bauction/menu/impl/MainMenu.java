package org.by1337.bauction.menu.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.by1337.api.chat.Placeholderable;
import org.by1337.api.command.Command;
import org.by1337.api.command.CommandException;
import org.by1337.api.command.argument.ArgumentSetList;
import org.by1337.api.command.argument.ArgumentString;
import org.by1337.api.util.CyclicList;
import org.by1337.api.util.Pair;
import org.by1337.bauction.Main;
import org.by1337.bauction.action.TakeItemProcess;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.auc.User;

import org.by1337.bauction.action.BuyItemCountProcess;
import org.by1337.bauction.action.BuyItemProcess;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.MenuSetting;
import org.by1337.bauction.menu.command.DefaultMenuCommand;
import org.by1337.bauction.menu.requirement.Requirements;
import org.by1337.bauction.util.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MainMenu extends Menu {

    protected int currentPage = 0;
    protected int maxPage = 0;

    protected CyclicList<Sorting> sortings = new CyclicList<>();
    protected CyclicList<Category> categories = new CyclicList<>();
    protected Category custom;

    protected List<Integer> slots;

    protected Command<Pair<Menu, Player>> command;

    public MainMenu(MenuSetting setting, Player player, @Nullable Menu backMenu, User user, List<Integer> slots) {
        super(setting, player, backMenu, user);
        this.slots = slots;
        sortings.addAll(Main.getCfg().getSortingMap().values());
        categories.addAll(Main.getCfg().getCategoryMap().values());
        initCommands();
    }

    protected MainMenu(List<CustomItemStack> items, String title, int size, int updateInterval, @Nullable Requirements viewRequirement, Player player, InventoryType type, @Nullable Menu backMenu, User user) {
        super(items, title, size, updateInterval, viewRequirement, player, type, backMenu, user);
        initCommands();
    }

    public MainMenu(User user, Player player) {
        this(user, player, null);
    }

    public MainMenu(User user, Player player, @Nullable Menu backMenu) {
        super(Main.getCfg().getMenuManger().getMainMenu(), player, backMenu, user);
        sortings.addAll(Main.getCfg().getSortingMap().values());
        categories.addAll(Main.getCfg().getCategoryMap().values());

        slots = Main.getCfg().getMenuManger().getMainMenuItemSlots();
        initCommands();
    }

    protected ArrayList<SellItem> sellItems = null;
    protected Category lastCategory = null;
    protected Sorting lastSorting = null;
    protected int lastPage = -1;

    @Override
    protected void generate() {
        if (lastPage != currentPage || sellItems == null || lastCategory == null || lastSorting == null || !lastCategory.equals(categories.getCurrent()) || !lastSorting.equals(sortings.getCurrent())) {
            setSellItems();
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

    protected void setSellItems() {
        lastCategory = categories.getCurrent();
        boolean sortChanged = Objects.equals(lastSorting, sortings.getCurrent());
        lastSorting = sortings.getCurrent();
        lastPage = currentPage;

        if (lastCategory == custom && (sortChanged || sellItems == null || sellItems.isEmpty())) {
            sellItems = new ArrayList<>();
            Main.getStorage().forEachSellItems(item -> {
                if (TagUtil.matchesCategory(custom, item)) {
                    sellItems.add(item);
                }
            });
//            for (SellItem item : Main.getStorage().getAllSellItems()) {
//                if (TagUtil.matchesCategory(custom, item)) {
//                    sellItems.add(item);
//                }
//            }
            sellItems.sort(lastSorting.getComparator());
        } else {
            sellItems = new ArrayList<>();
            Main.getStorage().forEachSellItemsBy(item -> {
                sellItems.add(item);
            }, lastCategory.nameKey(), lastSorting.nameKey());
            //sellItems = new ArrayList<>(Main.getStorage().getSellItemsBy());
        }

        maxPage = (int) Math.ceil((double) sellItems.size() / slots.size());

        if (currentPage > maxPage) {
            currentPage = maxPage - 1;
            if (currentPage < 0) currentPage = 0;
        }

        if (currentPage * slots.size() >= sellItems.size()) {
            maxPage = 0;
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


    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(Main.getMessage().messageBuilder(user.replace(s), viewer));
        while (true) {
            if (sb.indexOf("{max_page}") != -1) {
                sb.replace(sb.indexOf("{max_page}"), sb.indexOf("{max_page}") + "{max_page}".length(), String.valueOf(maxPage == 0 ? 1 : maxPage));
                continue;
            }
            if (sb.indexOf("{current_page}") != -1) {
                sb.replace(sb.indexOf("{current_page}"), sb.indexOf("{current_page}") + "{current_page}".length(), String.valueOf(currentPage + 1));
                continue;
            }
            if (sb.indexOf("{categories}") != -1) {
                sb.replace(sb.indexOf("{categories}"), sb.indexOf("{categories}") + "{categories}".length(),
                        getCategoriesNames()
                );
                continue;
            }
            if (sb.indexOf("{sorting}") != -1) {
                sb.replace(sb.indexOf("{sorting}"), sb.indexOf("{sorting}") + "{sorting}".length(),
                        getSorting()
                );
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


    private String getCategoriesNames() {
        StringBuilder sb = new StringBuilder();
        Category c = categories.getCurrent();
        for (Category category : categories) {
            if (c.equals(category)) {
                sb.append(replace(category.selectedName())).append("\n");
            } else {
                sb.append(replace(category.unselectedName())).append("\n");
            }
        }
        return sb.toString();
    }

    private String getSorting() {
        StringBuilder sb = new StringBuilder();
        Sorting c = sortings.getCurrent();
        for (Sorting sorting : sortings) {
            if (c.equals(sorting)) {
                sb.append(replace(sorting.selectedName())).append("\n");
            } else {
                sb.append(replace(sorting.unselectedName())).append("\n");
            }
        }
        return sb.toString();
    }

    public void reopen() {
        if (getPlayer() == null || !getPlayer().isOnline()) {
            throw new IllegalArgumentException();
        }
        syncUtil(() -> {
            reRegister();
            if (!viewer.getOpenInventory().getTopInventory().equals(inventory))
                viewer.openInventory(getInventory());
            sendFakeTitle(replace(title));
            sellItems = null;
            this.user = Main.getStorage().getUser(this.user.getUuid());
            generate0();
        });
    }

    public void setCustomCategory(Category custom) {
        categories.add(custom);
        categories.sort(Category::compareTo);
        this.custom = custom;
    }

    public CyclicList<Sorting> getSortings() {
        return sortings;
    }

    public CyclicList<Category> getCategories() {
        return categories;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public void setSortings(CyclicList<Sorting> sortings) {
        this.sortings = sortings;
    }

    public void setCategories(CyclicList<Category> categories) {
        this.categories = categories;
    }

    public void setCustom(Category custom) {
        this.custom = custom;
    }

    public void setSlots(List<Integer> slots) {
        this.slots = slots;
    }

    public void setSellItems(ArrayList<SellItem> sellItems) {
        this.sellItems = sellItems;
    }

    public void setLastCategory(Category lastCategory) {
        this.lastCategory = lastCategory;
    }

    public void setLastSorting(Sorting lastSorting) {
        this.lastSorting = lastSorting;
    }

    public void setLastPage(int lastPage) {
        this.lastPage = lastPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public Category getCustom() {
        return custom;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public Command<Pair<Menu, Player>> getCommand() {
        return command;
    }

    public ArrayList<SellItem> getSellItems() {
        return sellItems;
    }

    public Category getLastCategory() {
        return lastCategory;
    }

    public Sorting getLastSorting() {
        return lastSorting;
    }

    public int getLastPage() {
        return lastPage;
    }

    protected void initCommands() {
        command = new Command<Pair<Menu, Player>>("menu-commands")
                //<editor-fold desc="commands" defaultstate="collapsed">
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
                            this.user = Main.getStorage().getUser(this.user.getUuid());
                            generate0();
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[CATEGORIES_NEXT]")
                        .executor(((sender, args) -> {
                            categories.getNext();
                            generate0();
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[CATEGORIES_PREVIOUS]")
                        .executor(((sender, args) -> {
                            categories.getPrevious();
                            generate0();
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[SORTING_NEXT]")
                        .executor(((sender, args) -> {
                            sortings.getNext();
                            generate0();
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[SORTING_PREVIOUS]")
                        .executor(((sender, args) -> {
                            sortings.getPrevious();
                            generate0();
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[BUY_ITEM_FULL]")
                        .argument(new ArgumentString<>("uuid"))
                        .argument(new ArgumentSetList<>("fast", List.of("fast")))
                        .executor(((sender, args) -> {
                            boolean fast = args.getOrDefault("fast", "").equals("fast");
                            String uuidS = (String) args.getOrThrow("uuid");
                            UniqueName uuid = new CUniqueName(uuidS);
                            //  UUID uuid = UUID.fromString(uuidS);

                            if (!Main.getStorage().hasSellItem(uuid)) {
                                Main.getMessage().sendMsg(viewer, Lang.getMessages("item_already_sold_or_removed"));
                                sellItems = null;
                                this.user = Main.getStorage().getUser(this.user.getUuid());
                                generate0();
                                return;
                            }
                            SellItem item = Main.getStorage().getSellItem(uuid);

                            if (Main.getEcon().getBalance(getPlayer()) < item.getPrice()) {
                                Main.getMessage().sendMsg(getPlayer(), Lang.getMessages("insufficient_balance"));
                                return;
                            }

                            new BuyItemProcess(item, user, this, getPlayer(), fast).process();
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[BUY_ITEM_AMOUNT]")
                        .argument(new ArgumentString<>("uuid"))
                        .argument(new ArgumentSetList<>("fast", List.of("fast")))
                        .executor(((sender, args) -> {
                            boolean fast = args.getOrDefault("fast", "").equals("fast");
                            String uuidS = (String) args.getOrThrow("uuid");

                            UniqueName uuid = new CUniqueName(uuidS);

                            if (!Main.getStorage().hasSellItem(uuid)) {
                                Main.getMessage().sendMsg(viewer, Lang.getMessages("item_already_sold_or_removed"));
                                sellItems = null;
                                this.user = Main.getStorage().getUser(this.user.getUuid());
                                generate0();
                                return;
                            }
                            SellItem item = Main.getStorage().getSellItem(uuid);

                            if (Main.getEcon().getBalance(getPlayer()) < item.getPriceForOne()) {
                                Main.getMessage().sendMsg(getPlayer(), Lang.getMessages("insufficient_balance_for_purchase"));
                                return;
                            }

                            new BuyItemCountProcess(item, user, viewer, this, fast).process();
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[TAKE_ITEM]")
                        .argument(new ArgumentString<>("uuid"))
                        .argument(new ArgumentSetList<>("fast", List.of("fast")))
                        .executor(((sender, args) -> {
                            boolean fast = args.getOrDefault("fast", "").equals("fast");
                            String uuidS = (String) args.getOrThrow("uuid");
                            UniqueName uuid = new CUniqueName(uuidS);

                            if (!Main.getStorage().hasSellItem(uuid)) {
                                Main.getMessage().sendMsg(viewer, Lang.getMessages("item_already_sold_or_removed"));
                                sellItems = null;
                                this.user = Main.getStorage().getUser(this.user.getUuid());
                                generate0();
                                return;
                            }
                            SellItem item = Main.getStorage().getSellItem(uuid);
                            new TakeItemProcess(item, user, this, viewer, fast).process();
                        }))
                )
        ;
        DefaultMenuCommand.command.getSubcommands().forEach((s, c) -> command.addSubCommand(c));
        //</editor-fold>
    }
}
