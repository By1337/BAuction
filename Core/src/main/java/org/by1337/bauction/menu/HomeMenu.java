package org.by1337.bauction.menu;

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.db.kernel.CUser;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.ItemUtil;
import org.by1337.bauction.util.Sorting;
import org.by1337.bauction.util.TagUtil;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentString;
import org.by1337.blib.util.CyclicList;
import org.by1337.blib.util.NameKey;
import org.by1337.bmenu.menu.*;
import org.by1337.bmenu.menu.click.ClickType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HomeMenu extends Menu {
    private static final Command<HomeMenu> HOME_MENU_COMMAND;
    protected int currentPage = 0;
    protected int maxPage = 0;
    protected CyclicList<Sorting> sortings = new CyclicList<>();
    protected CyclicList<Category> categories = new CyclicList<>();
    protected Category custom;
    protected int lastCustomCategorySize;
    protected ArrayList<SellItem> sellItems = null;
    protected Category lastCategory = null;
    protected Sorting lastSorting = null;
    protected int lastPage = -1;
    protected CUser user;
    protected final Cash cash;
    private static boolean seenIllegalCash;

    public HomeMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, MenuLoader menuLoader) {
        super(setting, player, previousMenu, menuLoader);
        if (setting.getCash() == null) {
            cash = new Cash();
            setting.setCash(cash);
        } else if (setting.getCash() instanceof Cash cash0) {
            this.cash = cash0;
        } else {
            if (!seenIllegalCash) {
                Main.getMessage().error("Illegal cash type '%s'! Excepted %s", setting.getCash().getClass(), Cash.class);
                seenIllegalCash = true;
            }
            cash = new Cash();
        }
        init();
    }

    private void init() {
        sortings.addAll(Main.getCfg().getSortingMap().values());
        categories.addAll(Main.getCfg().getCategoryMap().values());
        user = (CUser) Main.getStorage().getUserOrCreate(viewer);
        registerPlaceholder("{max_page}", () -> maxPage == 0 ? 1 : maxPage);
        registerPlaceholder("{current_page}", () -> currentPage + 1);
        registerPlaceholder("{categories}", this::getCategoriesNames);
        registerPlaceholder("{custom_category_size}", () -> lastCustomCategorySize);
        registerPlaceholder("{sorting}", this::getSorting);
        registerPlaceholders(user);
    }

    @Override
    protected void generate() {
        setSellItems();
        Iterator<Integer> slotsIterator = cash.getSlots().listIterator();
        customItems.clear();
        for (int x = currentPage * cash.getSlots().size(); x < sellItems.size(); x++) {
            SellItem item = sellItems.get(x);
            if (slotsIterator.hasNext()) {
                int slot = slotsIterator.next();
                MenuItemBuilder menuItemBuilder;
                if (item.getSellerUuid().equals(user.getUuid())) {
                    menuItemBuilder = cash.getTakeItem();
                } else if (item.getAmount() == 1) {
                    menuItemBuilder = cash.getSellingItemOne();
                } else if (item.isSaleByThePiece()) {
                    menuItemBuilder = cash.getSellingItem();
                } else {
                    menuItemBuilder = cash.getSellingItemOnlyFull();
                }
                MenuItem menuItem = menuItemBuilder.build(this, item.getItemStack(), item);
                if (ItemUtil.isShulker(item.getMaterial())) {
                    var toAdd = cash.getIfShulker();
                    menuItem.getItemStack().editMeta(m -> {
                        var lore = new ArrayList<>(Objects.requireNonNullElse(m.lore(), Collections.emptyList()));
                        lore.addAll(toAdd.getLore().stream().map(s ->
                                Main.getMessage().componentBuilder(item.replace(s)).decoration(TextDecoration.ITALIC, false)).toList());
                        m.lore(lore);
                    });
                    if (!toAdd.getClicks().isEmpty()) {
                        menuItem.setClicks(new HashMap<>(menuItem.getClicks()));
                        for (ClickType clickType : toAdd.getClicks().keySet()) {
                            if (menuItem.getClicks().containsKey(clickType)) {
                                Main.getMessage().warning("Overlap click %s!", clickType.getConfigKeyClick());
                            }
                            menuItem.getClicks().put(clickType, toAdd.getClicks().get(clickType));
                        }
                    }

                }
                if (viewer.hasPermission("bauc.admin") && !item.getSellerUuid().equals(user.getUuid())) {
                    var toAdd = cash.getIfAdmin();
                    menuItem.getItemStack().editMeta(m -> {
                        var lore = new ArrayList<>(Objects.requireNonNullElse(m.lore(), Collections.emptyList()));
                        lore.addAll(toAdd.getLore().stream().map(s ->
                                Main.getMessage().componentBuilder(item.replace(s)).decoration(TextDecoration.ITALIC, false)).toList());
                        m.lore(lore);
                    });
                    if (!toAdd.getClicks().isEmpty()) {
                        menuItem.setClicks(new HashMap<>(menuItem.getClicks()));
                        for (ClickType clickType : toAdd.getClicks().keySet()) {
                            if (menuItem.getClicks().containsKey(clickType)) {
                                Main.getMessage().warning("Overlap click %s!", clickType.getConfigKeyClick());
                            }
                            menuItem.getClicks().put(clickType, toAdd.getClicks().get(clickType));
                        }
                    }
                }
                menuItem.getItemStack().setAmount(item.getAmount());
                menuItem.setData(item);
                menuItem.setSlots(new int[]{slot});
                customItems.add(menuItem);
            } else {
                break;
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
                if (((CSellItem) item).hasAllTags(custom)) {
                    sellItems.add(item);
                }
            });
            sellItems.sort(lastSorting.getComparator());
            lastCustomCategorySize = sellItems.size();
        } else {
            sellItems = new ArrayList<>();
            Main.getStorage().forEachSellItemsBy(item -> {
                sellItems.add(item);
            }, lastCategory.nameKey(), lastSorting.nameKey());
        }

        maxPage = (int) Math.ceil((double) sellItems.size() / cash.getSlots().size());

        if (currentPage > maxPage) {
            currentPage = maxPage - 1;
            if (currentPage < 0) currentPage = 0;
        }

        if (currentPage * cash.getSlots().size() >= sellItems.size()) {
            maxPage = 0;
        }
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

    @Override
    protected boolean runCommand(String[] cmd) throws CommandException {
        if (HOME_MENU_COMMAND.getSubcommands().containsKey(cmd[0])) {
            HOME_MENU_COMMAND.process(this, cmd);
            return true;
        }
        return false;
    }

    static {
        HOME_MENU_COMMAND = new Command<>("root");
        HOME_MENU_COMMAND.addSubCommand(
                new Command<HomeMenu>("[NEXT_PAGE]")
                        .executor((menu, args) -> {
                            if (menu.currentPage < menu.maxPage - 1) {
                                menu.currentPage++;
                                menu.refresh();
                            }
                        })
        );
        HOME_MENU_COMMAND.addSubCommand(
                new Command<HomeMenu>("[PREVIOUS_PAGE]")
                        .executor((menu, args) -> {
                            if (menu.currentPage > 0) {
                                menu.currentPage--;
                                menu.refresh();
                            }
                        })
        );
        HOME_MENU_COMMAND.addSubCommand(
                new Command<HomeMenu>("[SET_CATEGORY]")
                        .argument(new ArgumentString<>("category"))
                        .executor((menu, args) -> {
                            String categoryS = (String) args.getOrThrow("category");
                            Category category = Main.getCfg().getCategoryMap().get(new NameKey(categoryS));
                            int index = menu.getCategories().indexOf(category);
                            if (index == -1) {
                                Main.getMessage().warning("unknown category %s", categoryS);
                            } else {
                                menu.getCategories().current = index;
                                menu.refresh();
                            }
                        })
        );
        HOME_MENU_COMMAND.addSubCommand(
                new Command<HomeMenu>("[CATEGORIES_NEXT]")
                        .executor((menu, args) -> {
                            menu.categories.getNext();
                            menu.refresh();
                        })
        );
        HOME_MENU_COMMAND.addSubCommand(
                new Command<HomeMenu>("[CATEGORIES_PREVIOUS]")
                        .executor((menu, args) -> {
                            menu.categories.getPrevious();
                            menu.refresh();
                        })
        );
        HOME_MENU_COMMAND.addSubCommand(
                new Command<HomeMenu>("[SORTING_NEXT]")
                        .executor((menu, args) -> {
                            menu.sortings.getNext();
                            menu.refresh();
                        })
        );
        HOME_MENU_COMMAND.addSubCommand(
                new Command<HomeMenu>("[SORTING_PREVIOUS]")
                        .executor((menu, args) -> {
                            menu.sortings.getPrevious();
                            menu.refresh();
                        })
        );
    }

    public CyclicList<Sorting> getSortings() {
        return sortings;
    }

    public CyclicList<Category> getCategories() {
        return categories;
    }

    public Category getCustom() {
        return custom;
    }

    public void setCustom(Category custom) {
        this.custom = custom;
    }

    protected class Cash {
        private List<Integer> slots;
        private MenuItemBuilder sellingItemOne;
        private MenuItemBuilder takeItem;
        private MenuItemBuilder sellingItem;
        private MenuItemBuilder sellingItemOnlyFull;
        private MenuItemBuilder ifShulker;
        private MenuItemBuilder ifAdmin;

        public List<Integer> getSlots() {
            if (slots == null) {
                slots = MenuFactory.getSlots(setting.getContext().getList("items-slots", String.class));
            }
            return slots;
        }

        public MenuItemBuilder getSellingItemOne() {
            if (sellingItemOne == null) {
                sellingItemOne = setting.getContext().getAs("selling-item-one", MenuItemBuilder.class);
            }
            return sellingItemOne;
        }

        public MenuItemBuilder getTakeItem() {
            if (takeItem == null) {
                takeItem = setting.getContext().getAs("take-item", MenuItemBuilder.class);
            }
            return takeItem;
        }

        public MenuItemBuilder getSellingItem() {
            if (sellingItem == null) {
                sellingItem = setting.getContext().getAs("selling-item", MenuItemBuilder.class);
            }
            return sellingItem;
        }

        public MenuItemBuilder getSellingItemOnlyFull() {
            if (sellingItemOnlyFull == null) {
                sellingItemOnlyFull = setting.getContext().getAs("selling-item-only-full", MenuItemBuilder.class);
            }
            return sellingItemOnlyFull;
        }

        public MenuItemBuilder getIfShulker() {
            if (ifShulker == null) {
                ifShulker = setting.getContext().getAs("if-shulker", MenuItemBuilder.class);
            }
            return ifShulker;
        }

        public MenuItemBuilder getIfAdmin() {
            if (ifAdmin == null) {
                ifAdmin = setting.getContext().getAs("if-admin", MenuItemBuilder.class);
            }
            return ifAdmin;
        }
    }
}
