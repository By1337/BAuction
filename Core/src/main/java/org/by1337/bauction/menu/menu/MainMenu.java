package org.by1337.bauction.menu.menu;


import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.menu.*;
import org.by1337.bauction.menu.requirement.Requirements;
import org.by1337.bauction.util.*;
import org.by1337.bauction.util.placeholder.BiPlaceholder;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.util.CyclicList;
import org.by1337.blib.util.NameKey;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MainMenu extends Menu {
    private final static Command<MainMenu> commands;
    protected int currentPage = 0;
    protected int maxPage = 0;
    protected CyclicList<Sorting> sortings = new CyclicList<>();
    protected CyclicList<Category> categories = new CyclicList<>();
    protected Category custom;
    protected int lastCustomCategorySize;
    protected int[] slots;
    protected User user;


    public MainMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, OptionParser optionParser) {
        super(setting, player, previousMenu, optionParser);
        registerPlaceholder("{max_page}", () -> maxPage == 0 ? 1 : maxPage);
        registerPlaceholder("{current_page}", () -> currentPage + 1);
        registerPlaceholder("{categories}", this::getCategoriesNames);
        registerPlaceholder("{sorting}", this::getSorting);
        registerPlaceholder("{custom_category_size}", () -> lastCustomCategorySize);
        sortings.addAll(Main.getCfg().getSortingMap().values());
        categories.addAll(Main.getCfg().getCategoryMap().values());
        slots = Cache.getSlots(setting.getContext());
        user = Main.getStorage().getUserOrCreate(player);
        optionParser.getOptions().forEach((k, v) -> registerPlaceholder("{" + k + "}", () -> v));
        if (optionParser.has("category")) {
            String categoryS = optionParser.get("category");
            Category category = Main.getCfg().getCategoryMap().get(new NameKey(categoryS));
            if (category == null) {
                Main.getMessage().error("unknown category %s", categoryS);
                return;
            }
            int index = categories.indexOf(category);
            if (index == -1) {
                Main.getMessage().error("unknown category %s", categoryS);
                return;
            }
            categories.current = index;
        }
    }

    protected ArrayList<SellItem> sellItems = null;
    protected Category lastCategory = null;
    protected Sorting lastSorting = null;
    protected int lastPage = -1;

    @Override
    protected void generate() {
       // if (lastPage != currentPage || sellItems == null || lastCategory == null || lastSorting == null || !lastCategory.equals(categories.getCurrent()) || !lastSorting.equals(sortings.getCurrent())) {
            setSellItems();
            Iterator<Integer> slotsIterator = ArrayUtil.intArrayIterator(slots);
            customItems.clear();
            for (int x = currentPage * slots.length; x < sellItems.size(); x++) {
                SellItem item = sellItems.get(x);

                if (slotsIterator.hasNext()) {
                    int slot = slotsIterator.next();
                    Placeholderable placeholderable = new BiPlaceholder(this, item);
                    MenuItemBuilder menuItemBuilder = Cache.getItemSelector(setting.getContext()).get(placeholderable, viewer);

                    if (menuItemBuilder == null) {
                        Main.getMessage().error("item-selector так и не выбрал предмет! предмет '%s'", new Throwable(), item);
                        continue;
                    }

//                    if (item.getSellerUuid().equals(user.getUuid())) {
//                        menuItemBuilder = Cache.getTakeItem(setting.getContext());
//                    } else if (item.getAmount() == 1) {
//                        menuItemBuilder = Cache.getSellingItemOne(setting.getContext());
//                    } else if (item.isSaleByThePiece()) {
//                        menuItemBuilder = Cache.getSellingItem(setting.getContext());
//                    } else {
//                        menuItemBuilder = Cache.getSellingItemOnlyFull(setting.getContext());
//                    }
                    MenuItem menuItem = menuItemBuilder.build(this, item.getItemStack(), item);
                    menuItem.setSlots(new int[]{slot});
                    menuItem.getItemStack().setAmount(item.getAmount());
                    customItems.add(menuItem);
                }
            }
      //  }
    }

    @Override
    protected boolean runCommand(String[] cmd) throws CommandException {
        if (commands.getSubcommands().containsKey(cmd[0])) {
            commands.process(this, cmd);
            return true;
        }
        return false;
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
            sellItems.sort(lastSorting.getComparator());
            lastCustomCategorySize = sellItems.size();
        } else {
            sellItems = new ArrayList<>();
            Main.getStorage().forEachSellItemsBy(item -> sellItems.add(item), lastCategory.nameKey(), lastSorting.nameKey());
        }

        maxPage = (int) Math.ceil((double) sellItems.size() / slots.length);

        if (currentPage > maxPage) {
            currentPage = maxPage - 1;
            if (currentPage < 0) currentPage = 0;
        }

        if (currentPage * slots.length >= sellItems.size()) {
            maxPage = 0;
        }
    }

    @Override
    public String replace(String string) {
        if (user != null)
            return user.replace(super.replace(string));
        else
            return super.replace(string);
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

    public void setCustomCategory(Category custom) {
        categories.add(custom);
        categories.sort(Category::compareTo);
        this.custom = custom;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setLastCategory(Category lastCategory) {
        this.lastCategory = lastCategory;
    }

    public void setLastSorting(Sorting lastSorting) {
        this.lastSorting = lastSorting;
    }

    static {
        commands = new Command<>("cmd");
        commands.addSubCommand(new Command<MainMenu>("[NEXT_PAGE]")
                .executor(((m, args) -> {
                    if (m.currentPage < m.maxPage - 1) {
                        m.currentPage++;
                        m.generate0();
                    }
                }))
        );
        commands.addSubCommand(new Command<MainMenu>("[PREVIOUS_PAGE]")
                .executor(((m, args) -> {
                    if (m.currentPage > 0) {
                        m.currentPage--;
                        m.generate0();
                    }
                }))
        );
        commands.addSubCommand(new Command<MainMenu>("[UPDATE]")
                .executor(((m, args) -> {
                    m.sellItems = null;
                    m.user = Main.getStorage().getUser(m.user.getUuid());
                    m.generate0();
                }))
        );
        commands.addSubCommand(new Command<MainMenu>("[CATEGORIES_NEXT]")
                .executor(((m, args) -> {
                    m.categories.getNext();
                    m.generate0();
                }))
        );
        commands.addSubCommand(new Command<MainMenu>("[CATEGORIES_PREVIOUS]")
                .executor(((m, args) -> {
                    m.categories.getPrevious();
                    m.generate0();
                }))
        );
        commands.addSubCommand(new Command<MainMenu>("[SORTING_NEXT]")
                .executor(((m, args) -> {
                    m.sortings.getNext();
                    m.generate0();
                }))
        );
        commands.addSubCommand(new Command<MainMenu>("[SORTING_PREVIOUS]")
                .executor(((m, args) -> {
                    m.sortings.getPrevious();
                    m.generate0();
                }))
        );
    }

    static class Cache {
        private static int[] slots;

        private static ItemSelector itemSelector;

        static int[] getSlots(YamlContext context) {
            if (slots != null) return slots;
            slots = ArrayUtil.listToIntArray(MenuFactory.getSlots(context.getList("items-slots", String.class)));
            return slots;
        }

        public static ItemSelector getItemSelector(YamlContext context) {
            if (itemSelector != null) return itemSelector;
            itemSelector = context.getAs("item-selector", ItemSelector.class);
            return itemSelector;
        }
    }

}
