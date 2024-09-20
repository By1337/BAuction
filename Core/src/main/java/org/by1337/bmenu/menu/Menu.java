package org.by1337.bmenu.menu;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.action.BuyItemCountProcess;
import org.by1337.bauction.action.BuyItemProcess;
import org.by1337.bauction.action.TakeItemProcess;
import org.by1337.bauction.action.TakeUnsoldItemProcess;
import org.by1337.bauction.db.kernel.PluginSellItem;
import org.by1337.bauction.db.kernel.event.RemoveSellItemEvent;
import org.by1337.bauction.menu.SelectCountMenu;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentEnumValue;
import org.by1337.blib.command.argument.ArgumentFloat;
import org.by1337.blib.command.argument.ArgumentString;
import org.by1337.blib.command.argument.ArgumentStrings;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.NBTParser;
import org.by1337.blib.nbt.impl.ListNBT;
import org.by1337.blib.nbt.impl.StringNBT;
import org.by1337.bmenu.BMenuApi;
import org.by1337.bmenu.menu.requirement.Requirements;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
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
    protected final Map<Integer, MenuItem> matrix = new HashMap<>();

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
        registerPlaceholder("{has-back-menu}", () -> String.valueOf(previousMenu != null));
    }

    public void open() {
        if (inventory == null) {
            createInventory(size, BMenuApi.getMessage().componentBuilder(replace(title)), setting.getType());
        }
        syncUtil(() -> {
            if (openRequirements != null && !openRequirements.check(Menu.this, viewer)) {
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
        matrix.clear();
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
                matrix.put(slot, menuItem);
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
        if (!inventory.equals(e.getClickedInventory())) return;

        MenuItem menuItem = matrix.get(e.getSlot());
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


/*    @Nullable
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
    }*/

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

    private static void runIn(String rawNBT, Menu menu) {
        if (rawNBT != null) {
            try {
                ListNBT listNBT = (ListNBT) NBTParser.parseList(rawNBT);
                List<String> list = new ArrayList<>();
                for (NBT nbt : listNBT) {
                    if (nbt instanceof StringNBT stringNBT) {
                        list.add(stringNBT.getValue());
                    } else {
                        throw new IllegalArgumentException(String.format("Input: '%s' expected StringNBT", nbt));
                    }
                }
                menu.runCommands(list);
            } catch (Throwable t) {
                BMenuApi.getMessage().error("Failed to parse commands \"%s\"", t, commands);
            }
        }
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
                .argument(new ArgumentFloat<>("volume"))
                .argument(new ArgumentFloat<>("pitch"))
                .executor((v, args) -> {
                            float volume = (float) args.getOrDefault("volume", 1F);
                            float pitch = (float) args.getOrDefault("pitch", 1F);
                            Sound sound = (Sound) args.getOrThrow("sound");
                            BMenuApi.getMessage().sendSound(Objects.requireNonNull(v.viewer, "player is null!"), sound, volume, pitch);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[CLOSE]")
                .executor((v, args) -> AsyncClickListener.syncUtil(() -> Objects.requireNonNull(v.viewer, "player is null!").closeInventory()))
        );
        commands.addSubCommand(new Command<Menu>("[BACK_OR_CLOSE]")
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> AsyncClickListener.syncUtil(() -> {
                    if (v.previousMenu != null) {
                        v.previousMenu.reopen();
                        runIn((String) args.get("commands"), v.previousMenu);
                    } else {
                        Objects.requireNonNull(v.viewer, "player is null!").closeInventory();
                    }
                }))
        );
        commands.addSubCommand(new Command<Menu>("[BACK_TO_OR_CLOSE]")
                .argument(new ArgumentString<>("id"))
                .argument(new ArgumentStrings<>("commands"))
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
                            runIn((String) args.get("commands"), m);
                        } else {
                            Objects.requireNonNull(v.viewer, "player is null!").closeInventory();
                        }
                    });
                })
        );
        commands.addSubCommand(new Command<Menu>("[BACK]")
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                    var m = Objects.requireNonNull(v.previousMenu, "does not have a previous menu!");
                    m.reopen();
                    runIn((String) args.get("commands"), m);
                })
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
                .argument(new ArgumentStrings<>("commands"))
                .executor((v, args) -> {
                            String menu = (String) args.getOrThrow("menu", "PluginUser [OPEN_MENU] <menu id>");
                            var settings = v.menuLoader.getMenu(menu);
                            if (settings == null) {
                                throw new CommandException("Unknown menu %s", menu);
                            }
                            var m = settings.create(v.viewer, v);
                            m.open();
                            runIn((String) args.get("commands"), m);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[BUY_ITEM_FULL]")
                .argument(new ArgumentString<>("id"))
                .executor((v, args) -> {
                            BuyItemProcess buyItemProcess;
                            if (args.containsKey("id")) {
                                long id = Long.parseLong(((String) args.get("id")));

                                buyItemProcess = new BuyItemProcess(
                                        v,
                                        Main.getStorage().getUserOrCreate(v.viewer),
                                        Main.getStorage().getSellItem(id)
                                );
                            } else {
                                buyItemProcess = new BuyItemProcess(v);
                            }
                            buyItemProcess.run();
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[TAKE_ITEM]")
                .argument(new ArgumentString<>("id"))
                .executor((v, args) -> {
                            TakeItemProcess takeItemProcess;
                            if (args.containsKey("id")) {
                                long id = Long.parseLong(((String) args.get("id")));
                                takeItemProcess = new TakeItemProcess(
                                        v,
                                        Main.getStorage().getUserOrCreate(v.viewer),
                                        Main.getStorage().getSellItem(id)
                                );
                            } else {
                                takeItemProcess = new TakeItemProcess(v);
                            }
                            takeItemProcess.run();
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[TAKE_UNSOLD_ITEM]")
                .argument(new ArgumentString<>("id"))
                .executor((v, args) -> {
                            TakeUnsoldItemProcess takeUnsoldItemProcess;
                            if (args.containsKey("id")) {
                                long id = Long.parseLong(((String) args.get("id")));
                                takeUnsoldItemProcess = new TakeUnsoldItemProcess(
                                        v,
                                        Main.getStorage().getUserOrCreate(v.viewer),
                                        Main.getStorage().getUnsoldItem(id)
                                );
                            } else {
                                takeUnsoldItemProcess = new TakeUnsoldItemProcess(v);
                            }
                            takeUnsoldItemProcess.run();
                        }
                )
        );

        commands.addSubCommand(new Command<Menu>("[REMOVE_SELL_ITEM]")
                .argument(new ArgumentString<>("id"))
                .executor((menu, args) -> {
                            PluginSellItem sellItem;
                            if (args.containsKey("id")) {
                                long id = Long.parseLong(((String) args.get("id")));
                                sellItem = Main.getStorage().getSellItem(id);
                                if (sellItem == null) {
                                    Main.getMessage().error("Unknown sell item %s!", args.get("id"));
                                    menu.refresh();
                                    return;
                                }
                            } else if (menu.lastClickedItem != null && menu.lastClickedItem.getData() instanceof PluginSellItem s) {
                                sellItem = s;
                            } else {
                                Main.getMessage().error("[REMOVE_SELL_ITEM] The command does not specify an id which means that I will expect the item clicked by the player to be a PluginSellItem! Last click %s", menu.lastClickedItem);
                                return;
                            }
                            WeakReference<Menu> menuWeakReference = new WeakReference<>(menu);
                            Main.getStorage().onEvent(new RemoveSellItemEvent(sellItem)).thenAccept(event -> {
                                Menu menuFromWeakReference = menuWeakReference.get();
                                if (menuFromWeakReference != null) {
                                    menuFromWeakReference.refresh();
                                }
                            });
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
                                PluginSellItem sellItem = selectCountMenu.getSellItem();
                                BuyItemCountProcess buyItemCount = new BuyItemCountProcess(selectCountMenu, sellItem, count);
                                buyItemCount.run();
                            } else {
                                Main.getMessage().error("The [BUY_COUNT] command can only be called from selectCount selectCount! It is allowed to open other menus on top of selectCount");
                            }
                        }
                )
        );
    }
}