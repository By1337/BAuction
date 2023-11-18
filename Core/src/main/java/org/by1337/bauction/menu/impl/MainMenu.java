package org.by1337.bauction.menu.impl;

import org.bukkit.entity.Player;
import org.by1337.api.chat.Placeholderable;
import org.by1337.api.command.Command;
import org.by1337.api.command.CommandException;
import org.by1337.api.command.argument.ArgumentSetList;
import org.by1337.api.command.argument.ArgumentString;
import org.by1337.api.util.CyclicList;
import org.by1337.bauction.Main;
import org.by1337.bauction.action.TakeItemProcess;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;

import org.by1337.bauction.action.BuyItemCountProcess;
import org.by1337.bauction.action.BuyItemProcess;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;
import org.by1337.bauction.util.TagUtil;

import java.util.*;

public class MainMenu extends Menu {

    private int currentPage = 0;
    private int maxPage = 0;

    private CyclicList<Sorting> sortings = new CyclicList<>();
    private CyclicList<Category> categories = new CyclicList<>();
    private Category custom;

    private List<Integer> slots;

    private final Command command;
    private User user;


    public MainMenu(User user, Player player) {
        super(Main.getCfg().getMenuManger().getMainMenu(), player);
        this.user = user;
        //registerPlaceholderable(user);

        sortings.addAll(Main.getCfg().getSortingMap().values());
        categories.addAll(Main.getCfg().getCategoryMap().values());

        slots = Main.getCfg().getMenuManger().getMainMenuItemSlots();


        command = new Command("menu-commands")
                //<editor-fold desc="commands" defaultstate="collapsed">
                .addSubCommand(new Command("[OPEN_MENU]")
                        .argument(new ArgumentString("menu"))
                        .executor(((sender, args) -> {
                            String menuId = (String) args.getOrThrow("menu");
                            if (menuId.equals("selling-items")) {
                                ItemsForSaleMenu items = new ItemsForSaleMenu(player, user, this);
                                items.open();
                            } else if (menuId.equals("unsold-items")) {
                                UnsoldItemsMenu unsoldItemsMenu = new UnsoldItemsMenu(player, user, this);
                                unsoldItemsMenu.open();
                            } else {
                                throw new CommandException("unknown menu id: " + menuId);
                            }
                        }))
                )
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
                            this.user = Main.getStorage().getUser(this.user.getUuid());
                            generate0();
                        }))
                )
                .addSubCommand(new Command("[CATEGORIES_NEXT]")
                        .executor(((sender, args) -> {
                            categories.getNext();
                            generate0();
                        }))
                )
                .addSubCommand(new Command("[CATEGORIES_PREVIOUS]")
                        .executor(((sender, args) -> {
                            categories.getPrevious();
                            generate0();
                        }))
                )
                .addSubCommand(new Command("[SORTING_NEXT]")
                        .executor(((sender, args) -> {
                            sortings.getNext();
                            generate0();
                        }))
                )
                .addSubCommand(new Command("[SORTING_PREVIOUS]")
                        .executor(((sender, args) -> {
                            sortings.getPrevious();
                            generate0();
                        }))
                )
                .addSubCommand(new Command("[BUY_ITEM_FULL]")
                        .argument(new ArgumentString("uuid"))
                        .argument(new ArgumentSetList("bypass", List.of("bypass")))
                        .executor(((sender, args) -> {
                            boolean isBypassed = args.getOrDefault("bypass", "").equals("bypass");
                            String uuidS = (String) args.getOrThrow("uuid");

                            UUID uuid = UUID.fromString(uuidS);

                            if (!Main.getStorage().hasSellItem(uuid)) {
                                Main.getMessage().sendMsg(player, "&cПредмет уже продан или снят с продажи!");
                                sellItems = null;
                                this.user = Main.getStorage().getUser(this.user.getUuid());
                                generate0();
                                return;
                            }
                            SellItem item = Main.getStorage().getSellItem(uuid);

                            if (Main.getEcon().getBalance(getPlayer()) < item.getPrice()) {
                                Main.getMessage().sendMsg(getPlayer(), "&cУ Вас не хватает баланса для покупки предмета!");
                                return;
                            }

                            new BuyItemProcess(item, user, this, getPlayer()).process();
                        }))
                )
                .addSubCommand(new Command("[BUY_ITEM_AMOUNT]")
                        .argument(new ArgumentString("uuid"))
                        .executor(((sender, args) -> {
                            String uuidS = (String) args.getOrThrow("uuid");

                            UUID uuid = UUID.fromString(uuidS);

                            if (!Main.getStorage().hasSellItem(uuid)) {
                                Main.getMessage().sendMsg(player, "&cПредмет уже продан или снят с продажи!");
                                sellItems = null;
                                this.user = Main.getStorage().getUser(this.user.getUuid());
                                generate0();
                                return;
                            }
                            SellItem item = Main.getStorage().getSellItem(uuid);

                            if (Main.getEcon().getBalance(getPlayer()) < item.getPriceForOne()) {
                                Main.getMessage().sendMsg(getPlayer(), "&cУ Вас не хватает баланса для покупки хотя бы одного предмета!");
                                return;
                            }

                            new BuyItemCountProcess(item, user, player, this).process();
                        }))
                )
                .addSubCommand(new Command("[TAKE_ITEM]")
                        .argument(new ArgumentString("uuid"))
                        .executor(((sender, args) -> {
                            String uuidS = (String) args.getOrThrow("uuid");
                            UUID uuid = UUID.fromString(uuidS);

                            if (!Main.getStorage().hasSellItem(uuid)) {
                                Main.getMessage().sendMsg(player, "&cПредмет уже продан или снят с продажи!");
                                sellItems = null;
                                this.user = Main.getStorage().getUser(this.user.getUuid());
                                generate0();
                                return;
                            }
                            SellItem item = Main.getStorage().getSellItem(uuid);
                            new TakeItemProcess(item, user, this, player).process();
                        }))
                )
        ;
        //</editor-fold>

    }

    private ArrayList<SellItem> sellItems = null;
    private Category lastCategory = null;
    private Sorting lastSorting = null;
    private int lastPage = -1;

    @Override
    protected void generate() {
        if (lastPage != currentPage || sellItems == null || lastCategory == null || lastSorting == null || !lastCategory.equals(categories.getCurrent()) || !lastSorting.equals(sortings.getCurrent())) {
            lastCategory = categories.getCurrent();
            boolean sortChanged = Objects.equals(lastSorting, sortings.getCurrent());
            lastSorting = sortings.getCurrent();
            lastPage = currentPage;

            if (lastCategory == custom && (sortChanged || sellItems == null || sellItems.isEmpty())){
                sellItems = new ArrayList<>();
                for (SellItem item : Main.getStorage().getAllItems()) {
                    if (TagUtil.matchesCategory(custom, item)){
                        sellItems.add(item);
                    }
                }
                sellItems.sort(lastSorting.getComparator());
            }else {
                sellItems = new ArrayList<>(Main.getStorage().getItems(lastCategory.nameKey(), lastSorting.nameKey()));
            }

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
                    //  customPlaceHolders.forEach(customItemStack::registerPlaceholder);
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


    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(Main.getMessage().messageBuilder(user.replace(s), getPlayer()));
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
                        getCategories()
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


    private String getCategories() {
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
        reRegister();
        getPlayer().openInventory(getInventory());
        sendFakeTitle(replace(title));
        sellItems = null;
        this.user = Main.getStorage().getUser(this.user.getUuid());
        generate0();
    }

    public void setCustomCategory(Category custom) {
        categories.add(custom);
        categories.sort(Category::compareTo);
        this.custom = custom;
    }
}
