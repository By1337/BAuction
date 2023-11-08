package org.by1337.bauction.menu.impl;

import org.bukkit.scheduler.BukkitRunnable;
import org.by1337.api.chat.Placeholderable;
import org.by1337.api.command.Command;
import org.by1337.api.command.CommandException;
import org.by1337.api.util.CyclicList;
import org.by1337.bauction.Main;
import org.by1337.bauction.SellItem;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.MenuFactory;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MainMenu extends Menu {

    private int currentPage = 0;
    private int maxPage = 0;

    private CyclicList<Sorting> sortings = new CyclicList<>();
    private CyclicList<Category> categories = new CyclicList<>();

    private List<Integer> slots;

    private final Command command;

    public MainMenu() {
        super(MenuFactory.create(Main.getCfg().getMenu()));
        sortings.addAll(Main.getCfg().getSortingMap().values());

        categories.addAll(Main.getCfg().getCategoryMap().values());

        slots = MenuFactory.getSlots(menuFile, "items-slots");

        command = new Command("menu-commands")
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
                            sellItems.clear();
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
                        .executor(((sender, args) -> {
                            System.out.println("todo"); // todo
                        }))
                )
                .addSubCommand(new Command("[BUY_ITEM_AMOUNT]")
                        .executor(((sender, args) -> {
                            System.out.println("todo"); // todo
                        }))
                )
        ;
    }

    private ArrayList<SellItem> sellItems = null;
    private Category lastCategory = null;
    private Sorting lastSorting = null;
    private int lastPage = -1;

    @Override
    protected void generate() {
        if (lastPage != currentPage|| sellItems == null || lastCategory == null || lastSorting == null || !lastCategory.equals(categories.getCurrent()) || !lastSorting.equals(sortings.getCurrent())) {
            lastCategory = categories.getCurrent();
            lastSorting = sortings.getCurrent();
            lastPage = currentPage;

            sellItems = new ArrayList<>(Main.getStorage().getItems(lastCategory.nameKey(), lastSorting.nameKey()));

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
                    if (item.getAmount() == 1) {
                        customItemStack = MenuFactory.menuItemBuilder(Main.getCfg().getConfig().getConfigurationSection("selling-item-one").getValues(false));
                    } else if (item.isSaleByThePiece()) {
                        customItemStack = MenuFactory.menuItemBuilder(Main.getCfg().getConfig().getConfigurationSection("selling-item").getValues(false));
                    } else {
                        customItemStack = MenuFactory.menuItemBuilder(Main.getCfg().getConfig().getConfigurationSection("selling-item-only-full").getValues(false));
                    }
                    customItemStack.setItemStack(item.getItemStack());
                    customItemStack.setSlots(new int[]{slot});
                    customItemStack.registerPlaceholder(item);
                    customPlaceHolders.forEach(customItemStack::registerPlaceholder);
                    customItemStacks.add(customItemStack);
                }
            }
        }
    }

    @Override
    public void runCommand(Placeholderable holder, String... commands) {
        try {
            command.process(null, commands);
        } catch (CommandException e) {
            Main.getMessage().error(e);
        }
    }


    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(s);
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
        return sb.toString();
    }


    private String getCategories() {
        StringBuilder sb = new StringBuilder();
        Category c = categories.getCurrent();
        for (Category category : categories) {
            if (c.equals(category)) {
                sb.append(category.selectedName()).append("\n");
            } else {
                sb.append(category.unselectedName()).append("\n");
            }
        }
        return sb.toString();
    }

    private String getSorting() {
        StringBuilder sb = new StringBuilder();
        Sorting c = sortings.getCurrent();
        for (Sorting sorting : sortings) {
            if (c.equals(sorting)) {
                sb.append(sorting.selectedName()).append("\n");
            } else {
                sb.append(sorting.unselectedName()).append("\n");
            }
        }
        return sb.toString();
    }
}
