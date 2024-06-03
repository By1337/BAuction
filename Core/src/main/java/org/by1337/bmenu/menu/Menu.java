package org.by1337.bmenu.menu;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.by1337.bauction.Main;
import org.by1337.bauction.action.BuyItemCountProcessV2;
import org.by1337.bauction.action.BuyItemProcessV2;
import org.by1337.bauction.action.TakeItemProcessV2;
import org.by1337.bauction.action.TakeUnsoldItemProcessV2;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.menu2.SelectCountMenu;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentEnumValue;
import org.by1337.blib.command.argument.ArgumentString;
import org.by1337.blib.command.argument.ArgumentStrings;
import org.by1337.bmenu.BMenuApi;
import org.by1337.bmenu.menu.requirement.Requirements;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Menu extends AsyncClickListener {

    protected final List<MenuItemBuilder> items;
    protected List<MenuItem> customItems = new ArrayList<>();
    private List<MenuItem> currentItems = new ArrayList<>();
    protected final String title;
    protected final int size;
    protected Requirements openRequirements;
    @Nullable
    protected final Menu previousMenu;
    protected static final Command<Menu> commands;
    protected List<String> openCommands = new ArrayList<>();
    protected final MenuSetting setting;
    protected final MenuLoader menuLoader;
    @Nullable
    protected MenuItem lastClickedItem;

    public Menu(MenuSetting setting, Player player, @Nullable Menu previousMenu, MenuLoader menuLoader) {
        this(setting, player, previousMenu, true, menuLoader);
    }

    public Menu(MenuSetting setting, Player player, @Nullable Menu previousMenu, boolean async, MenuLoader menuLoader) {
        super(player, async);
        this.setting = setting;
        openCommands = setting.getOpenCommands();
        openRequirements = setting.getViewRequirement();
        this.items = setting.getItems();
        this.title = setting.getTitle();
        this.size = setting.getSize();
        this.previousMenu = previousMenu;
        this.menuLoader = menuLoader;
        createInventory(size, replace(title), setting.getType());
        registerPlaceholder("{has-back-menu}", () -> String.valueOf(previousMenu != null));
    }

    public void open() {
        Menu menu = this;
        syncUtil(() -> {
            if (openRequirements != null && !openRequirements.check(menu, viewer)) {
                List<String> list = new ArrayList<>(openRequirements.getDenyCommands());
                list.replaceAll(this::replace);
                runCommands(list);
            } else {
                if (!openCommands.isEmpty()) runCommands(openCommands);
                viewer.openInventory(inventory);
                generate0();
            }
        });
    }

    protected abstract void generate();

    private void generate0() {
        inventory.clear();
        currentItems.clear();
        generate();
        currentItems = new ArrayList<>(items.stream().map(m -> m.build(this)).filter(Objects::nonNull).toList());
        setItems(currentItems);
        setItems(customItems);
        sendFakeTitle(replace(title));
    }

    public void refresh() {
        generate0();
    }

    protected void setItems(List<MenuItem> list) {
        for (MenuItem menuItem : list) {
            for (int slot : menuItem.getSlots()) {
                ItemStack item = menuItem.getItemStack();
                inventory.setItem(slot, item);
            }
        }
    }

    protected abstract boolean runCommand(String[] cmd) throws CommandException;

    public void runCommands(List<String> commands) {
        for (String command : commands) {
            String[] args = replace(command).split(" ");
            args[0] = args[0].toUpperCase(Locale.ENGLISH);
            try {
                if (!runCommand(args)) {
                    Menu.commands.process(this, args);
                }
            } catch (CommandException e) {
                BMenuApi.getMessage().error(e);
            }
        }
    }

    @Override
    protected void onClick(InventoryDragEvent e) {
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) {
            return;
        }
        MenuItem menuItem = findItemInSlot(e.getSlot());
        lastClickedItem = menuItem;
        if (menuItem == null) {
            inventory.clear();
            generate0();
            return;
        }
        runCommands(menuItem.getCommands(e, viewer));
    }


    public void onClose(InventoryCloseEvent e) {
    }


    @Nullable
    protected MenuItem findItemInSlot(int slot) {
        var item = findItemInSlot(slot, customItems);
        return item == null ? findItemInSlot(slot, currentItems) : item;
    }

    @Nullable
    protected MenuItem findItemInSlot(int slot, List<MenuItem> list) {
        for (MenuItem item : list) {
            for (int itemSlot : item.getSlots()) {
                if (itemSlot == slot) return item;
            }
        }
        return null;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return viewer;
    }

    public void reopen() {
        if (getPlayer() == null || !getPlayer().isOnline()) {
            throw new IllegalArgumentException();
        }
        syncUtil(() -> {
            reRegister();
            if (!viewer.getOpenInventory().getTopInventory().equals(inventory))
                viewer.openInventory(getInventory());
            generate0();
        });
    }

    @Nullable
    public Menu getPreviousMenu() {
        return previousMenu;
    }

    @Override
    public String replace(String string) {
        return super.replace(BMenuApi.getMessage().setPlaceholders(viewer, string));
    }

    public void back() {
        getPreviousMenu().reopen();
    }

    protected List<MenuItem> getCurrentItems() {
        return currentItems;
    }

    public @Nullable MenuItem getLastClickedItem() {
        return lastClickedItem;
    }

    static {
        commands = new Command<>("cmd");
        commands.addSubCommand(new Command<Menu>("[CONSOLE]")
                .argument(new ArgumentStrings<>("cmd"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("cmd");
                            AsyncClickListener.syncUtil(() -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd));
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[PLAYER]")
                .argument(new ArgumentStrings<>("cmd"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("cmd");
                            AsyncClickListener.syncUtil(() -> Objects.requireNonNull(v.viewer, "player is null!").performCommand(cmd));
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[SOUND]")
                .argument(new ArgumentEnumValue<>("sound", Sound.class))
                .executor((v, args) -> {
                            Sound sound = (Sound) args.getOrThrow("sound");
                            BMenuApi.getMessage().sendSound(Objects.requireNonNull(v.viewer, "player is null!"), sound);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[CLOSE]")
                .executor((v, args) -> AsyncClickListener.syncUtil(() -> Objects.requireNonNull(v.viewer, "player is null!").closeInventory()))
        );
        commands.addSubCommand(new Command<Menu>("[BACK_OR_CLOSE]")
                .executor((v, args) -> AsyncClickListener.syncUtil(() -> {
                    if (v.previousMenu != null) {
                        v.previousMenu.reopen();
                    } else {
                        Objects.requireNonNull(v.viewer, "player is null!").closeInventory();
                    }
                }))
        );
        commands.addSubCommand(new Command<Menu>("[BACK_TO_OR_CLOSE]")
                .argument(new ArgumentString<>("id"))
                .executor((v, args) -> {
                    String id = (String) args.getOrThrow("id", "Use: [BACK_TO_OR_CLOSE] <id>");
                    AsyncClickListener.syncUtil(() -> {
                        Menu m = v.previousMenu;
                        while (m != null) {
                            if (m.setting.getId().getName().equals(id)) break;
                            m = m.previousMenu;
                        }
                        if (m != null) {
                            m.reopen();
                        } else {
                            Objects.requireNonNull(v.viewer, "player is null!").closeInventory();
                        }
                    });
                })
        );
        commands.addSubCommand(new Command<Menu>("[BACK]")
                .executor((v, args) -> AsyncClickListener.syncUtil(() -> Objects.requireNonNull(v.previousMenu, "does not have a previous menu!").reopen()))
        );
        commands.addSubCommand(new Command<Menu>("[REFRESH]")
                .executor((v, args) -> v.refresh())
        );
        commands.addSubCommand(new Command<Menu>("[MESSAGE]")
                .argument(new ArgumentStrings<>("msg"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("msg");
                            BMenuApi.getMessage().sendMsg(v.viewer, cmd);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[OPEN_MENU]")
                .argument(new ArgumentString<>("menu"))
                .executor((v, args) -> {
                            String menu = (String) args.getOrThrow("menu", "User [OPEN_MENU] <menu id>");
                            var settings = v.menuLoader.getMenu(menu);
                            if (settings == null) {
                                throw new CommandException("Unknown menu %s", menu);
                            }
                            var m = settings.create(v.viewer, v);
                            m.open();
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[BUY_ITEM_FULL]")
                .argument(new ArgumentString<>("id"))
                .executor((v, args) -> {
                            BuyItemProcessV2 buyItemProcessV2;
                            if (args.containsKey("id")) {
                                CUniqueName uniqueName = new CUniqueName((String) args.get("id"));
                                buyItemProcessV2 = new BuyItemProcessV2(
                                        v,
                                        Main.getStorage().getUserOrCreate(v.viewer),
                                        Main.getStorage().getSellItem(uniqueName)
                                );
                            } else {
                                buyItemProcessV2 = new BuyItemProcessV2(v);
                            }
                            buyItemProcessV2.run();
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[TAKE_ITEM]")
                .argument(new ArgumentString<>("id"))
                .executor((v, args) -> {
                            TakeItemProcessV2 takeItemProcessV2;
                            if (args.containsKey("id")) {
                                CUniqueName uniqueName = new CUniqueName((String) args.get("id"));
                                takeItemProcessV2 = new TakeItemProcessV2(
                                        v,
                                        Main.getStorage().getUserOrCreate(v.viewer),
                                        Main.getStorage().getSellItem(uniqueName)
                                );
                            } else {
                                takeItemProcessV2 = new TakeItemProcessV2(v);
                            }
                            takeItemProcessV2.run();
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[TAKE_UNSOLD_ITEM]")
                .argument(new ArgumentString<>("id"))
                .executor((v, args) -> {
                            TakeUnsoldItemProcessV2 takeUnsoldItemProcessV2;
                            if (args.containsKey("id")) {
                                CUniqueName uniqueName = new CUniqueName((String) args.get("id"));
                                takeUnsoldItemProcessV2 = new TakeUnsoldItemProcessV2(
                                        v,
                                        Main.getStorage().getUserOrCreate(v.viewer),
                                        Main.getStorage().getUnsoldItem(uniqueName)
                                );
                            } else {
                                takeUnsoldItemProcessV2 = new TakeUnsoldItemProcessV2(v);
                            }
                            takeUnsoldItemProcessV2.run();
                        }
                )
        );

        commands.addSubCommand(new Command<Menu>("[REMOVE_SELL_ITEM]")
                .argument(new ArgumentString<>("id"))
                .executor((v, args) -> {
                            SellItem sellItem;
                            if (args.containsKey("id")) {
                                CUniqueName uniqueName = new CUniqueName((String) args.get("id"));
                                sellItem = Main.getStorage().getSellItem(uniqueName);
                                if (sellItem == null) {
                                    Main.getMessage().error("Unknown sell item %s!", args.get("id"));
                                    return;
                                }
                            } else if (v.lastClickedItem != null && v.lastClickedItem.getData() instanceof SellItem s) {
                                sellItem = s;
                            } else {
                                Main.getMessage().error("[REMOVE_SELL_ITEM] The command does not specify an id which means that I will expect the item clicked by the player to be a SellItem! Last click %s", v.lastClickedItem);
                                return;
                            }
                            Main.getStorage().removeSellItem(sellItem.getUniqueName());
                            v.refresh();
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[BUY_COUNT]")
                .executor((v, args) -> {
                            Menu menu = v;
                            while (!(menu instanceof SelectCountMenu) && menu != null) {
                                menu = menu.previousMenu;
                            }
                            if (menu instanceof SelectCountMenu selectCountMenu) {
                                int count = selectCountMenu.getCount();
                                SellItem sellItem = selectCountMenu.getSellItem();
                                BuyItemCountProcessV2 buyItemCount = new BuyItemCountProcessV2(selectCountMenu, sellItem, count);
                                buyItemCount.run();
                            } else {
                                Main.getMessage().error("The [BUY_COUNT] command can only be called from selectCount selectCount! It is allowed to open other menus on top of selectCount");
                            }
                        }
                )
        );
    }
}