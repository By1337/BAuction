package org.by1337.bauction.menu;

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.PluginSellItem;
import org.by1337.bauction.db.kernel.PluginUser;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.common.ItemUtil;
import org.by1337.bauction.util.auction.Sorting;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentBoolean;
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
    protected ArrayList<PluginSellItem> sellItems = null;
    protected Category lastCategory = null;
    protected Sorting lastSorting = null;
    protected int lastPage = -1;
    protected PluginUser user;
    protected final Cache cache;
    private static boolean seenIllegalCash;

    public HomeMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, MenuLoader menuLoader) {
        super(setting, player, previousMenu, menuLoader);
        if (setting.getCache() == null) {
            cache = new Cache();
            setting.setCache(cache);
        } else if (setting.getCache() instanceof Cache cache0) {
            this.cache = cache0;
        } else {
            if (!seenIllegalCash) {
                Main.getMessage().error("Illegal cache type '%s'! Excepted %s", setting.getCache().getClass(), Cache.class);
                seenIllegalCash = true;
            }
            cache = new Cache();
        }
        init();
    }

    private void init() {
        sortings.addAll(Main.getCfg().getSortingMap().values());
        categories.addAll(Main.getCfg().getCategoryMap().values());
        user = (PluginUser) Main.getStorage().getUserOrCreate(viewer);
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
        Iterator<Integer> slotsIterator = cache.getSlots().listIterator();
        customItems.clear();
        for (int x = currentPage * cache.getSlots().size(); x < sellItems.size(); x++) {
            PluginSellItem item = sellItems.get(x);
            if (slotsIterator.hasNext()) {
                int slot = slotsIterator.next();
                MenuItemBuilder menuItemBuilder;
                if (item.getSellerUuid().equals(user.getUuid())) {
                    menuItemBuilder = cache.getTakeItem();
                } else if (item.getAmount() == 1) {
                    menuItemBuilder = cache.getSellingItemOne();
                } else if (item.isSaleByThePiece()) {
                    menuItemBuilder = cache.getSellingItem();
                } else {
                    menuItemBuilder = cache.getSellingItemOnlyFull();
                }
                MenuItem menuItem = menuItemBuilder.build(this, item.getItemStack(), item);
                if (ItemUtil.isShulker(item.getBukkitMaterial())) {
                    var toAdd = cache.getIfShulker();
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
                    var toAdd = cache.getIfAdmin();
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
        boolean categoryChanged = lastCategory != categories.getCurrent();
        lastCategory = categories.getCurrent();
        lastPage = currentPage;
        boolean sortChanged = !Objects.equals(lastSorting, sortings.getCurrent());
        lastSorting = sortings.getCurrent();

        if (lastCategory == custom) {
            if (sortChanged || sellItems == null || sellItems.isEmpty() || categoryChanged) {
                sellItems = new ArrayList<>();
                Main.getStorage().forEachSellItems(item -> {
                    if (!custom.isSoft()) {
                        if (item.hasAllTags(custom)) {
                            sellItems.add(item);
                        }
                    } else {
                        if (custom.matches(item)) {
                            sellItems.add(item);
                        }
                    }

                });
                sellItems.sort(lastSorting.getComparator());
                lastCustomCategorySize = sellItems.size();
            }
        } else {
            sellItems = new ArrayList<>();
            Main.getStorage().forEachSellItemsBy(item -> {
                sellItems.add(item);
            }, lastCategory.nameKey(), lastSorting.nameKey());
        }

        maxPage = (int) Math.ceil((double) sellItems.size() / cache.getSlots().size());

        if (currentPage > maxPage) {
            currentPage = maxPage - 1;
            if (currentPage < 0) currentPage = 0;
        }

        if (currentPage * cache.getSlots().size() >= sellItems.size()) {
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
        HOME_MENU_COMMAND.addSubCommand(
                new Command<HomeMenu>("[FIND_ANALOGS]")
                        .argument(new ArgumentBoolean<>("soft"))
                        .executor((menu, args) -> {
                            if (menu.getLastClickedItem() == null || !(menu.getLastClickedItem().getData() instanceof PluginSellItem sellItem))
                                throw new CommandException("Clicked item is not sell item!");
                            boolean soft = (boolean) args.getOrDefault("soft", false);

                            Category custom = Main.getCfg().getSorting().getAs("special.search", Category.class);
                            custom.setSoft(soft);
                            custom.setTags(sellItem.getTags());

                            if (menu.custom != null) {
                                menu.categories.remove(menu.custom);
                            }
                            menu.custom = custom;
                            menu.categories.add(custom);
                            Collections.sort(menu.categories);
                            menu.categories.current = menu.categories.indexOf(custom);
                            menu.currentPage = 0;
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

    protected class Cache {
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
