package org.by1337.bauction.menu;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.by1337.bauction.action.BuyItemProcess;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.api.util.UniqueName;
import org.by1337.bauction.db.kernel.MysqlDb;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.network.impl.PacketSendMessage;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.bauction.util.OptionParser;
import org.by1337.bauction.util.PlayerUtil;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentEnumValue;
import org.by1337.blib.command.argument.ArgumentInteger;
import org.by1337.blib.command.argument.ArgumentString;
import org.by1337.blib.command.argument.ArgumentStrings;
import org.by1337.bauction.Main;
import org.by1337.bauction.menu.requirement.Requirements;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public abstract class Menu extends AsyncClickListener {

    protected final List<MenuItemBuilder> items;
    protected List<MenuItem> customItems = new ArrayList<>();
    protected List<MenuItem> currentItems = new ArrayList<>();
    protected final String title;
    protected final int size;
    protected Requirements openRequirements;
    @Nullable
    protected final Menu previousMenu;
    protected static final Command<Menu> commands;
    protected List<String> openCommands = new ArrayList<>();
    protected final OptionParser optionParser;
    protected final MenuSetting setting;


    public Menu(MenuSetting setting, Player player, @Nullable Menu previousMenu, OptionParser optionParser) {
        this(setting, player, previousMenu, true, optionParser);
    }

    public Menu(MenuSetting setting, Player player, @Nullable Menu previousMenu, boolean async, OptionParser optionParser) {
        super(player, async);
        this.setting = setting;
        openCommands = setting.getOpenCommands();
        this.optionParser = optionParser;
        openRequirements = setting.getViewRequirement();
        this.items = setting.getItems();
        this.title = setting.getTitle();
        this.size = setting.getSize();
        this.previousMenu = previousMenu;
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

    protected void generate0() {
        inventory.clear();
        currentItems.clear();
        generate();
        currentItems = new ArrayList<>(items.stream().map(m -> m.build(this)).filter(Objects::nonNull).toList());
        setItems(currentItems);
        setItems(customItems);
        sendFakeTitle(replace(title));

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
                Main.getMessage().error(e);
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

        ItemStack itemStack = e.getCurrentItem();
        ItemMeta im = itemStack.getItemMeta();
        if (!im.getPersistentDataContainer().has(MenuItemBuilder.MENU_ITEM_KEY, PersistentDataType.INTEGER)) {
            inventory.clear();
            generate0();
            return;
        }
        Integer id = im.getPersistentDataContainer().get(MenuItemBuilder.MENU_ITEM_KEY, PersistentDataType.INTEGER);
        if (id == null) return;

        MenuItem menuItem = getItemById(id);

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
    protected MenuItem getItemById(int id) {
        var item = findItemIn(id, customItems);
        return item == null ? findItemIn(id, currentItems) : item;
    }

    @Nullable
    protected MenuItem findItemIn(int id, List<MenuItem> list) {
        for (MenuItem item : list) {
            if (item.getId() == id) {
                return item;
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
        return super.replace(Main.getMessage().setPlaceholders(viewer, string));
    }

    public void back() {
        getPreviousMenu().reopen();
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
                            Main.getMessage().sendSound(Objects.requireNonNull(v.viewer, "player is null!"), sound);
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
        commands.addSubCommand(new Command<Menu>("[BACK]")
                .executor((v, args) -> AsyncClickListener.syncUtil(() -> Objects.requireNonNull(v.previousMenu, "does not have a previous menu!").reopen()))
        );
        commands.addSubCommand(new Command<Menu>("[REFRESH]")
                .executor((v, args) -> v.generate0())
        );
        commands.addSubCommand(new Command<Menu>("[MESSAGE]")
                .argument(new ArgumentStrings<>("msg"))
                .executor((v, args) -> {
                            String cmd = (String) args.getOrThrow("msg");
                            Main.getMessage().sendMsg(v.viewer, cmd);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[BUY_ITEM_FULL]")
                .argument(new ArgumentString<>("uuid"))
                .executor((v, args) -> {
                            String uuidS = (String) args.getOrThrow("uuid");
                            UniqueName uuid = new CUniqueName(uuidS);
                            SellItem item = Main.getStorage().getSellItem(uuid);
                            if (item == null) {
                                Main.getMessage().sendMsg(v.viewer, Lang.getMessage("item_already_sold_or_removed"));
                                v.generate0();
                                return;
                            }

                            if (Main.getEcon().getBalance(v.viewer) < item.getPrice()) {
                                Main.getMessage().sendMsg(v.viewer, Lang.getMessage("insufficient_balance"));
                                return;
                            }
                            new BuyItemProcess(item, Main.getStorage().getUserOrCreate(v.viewer), v.viewer).process();
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[BUY_ITEM_AMOUNT]")
                .argument(new ArgumentString<>("uuid"))
                .argument(new ArgumentInteger<>("count"))
                .executor((v, args) -> {
                            String uuidS = (String) args.getOrThrow("uuid");
                            int count = (int) args.getOrThrow("count");
                            UniqueName uuid = new CUniqueName(uuidS);
                            SellItem buyingItem = Main.getStorage().getSellItem(uuid);
                            if (buyingItem == null) {
                                Main.getMessage().sendMsg(v.viewer, Lang.getMessage("item_already_sold_or_removed"));
                                v.generate0();
                                return;
                            }
                            Player player = v.viewer;
                            double price = buyingItem.getPriceForOne() * count;


                            OfflinePlayer seller = Bukkit.getOfflinePlayer(buyingItem.getSellerUuid());
                            if (Main.getEcon().getBalance(player) < price) {
                                Main.getMessage().sendMsg(player, Lang.getMessage("insufficient_balance"));
                                return;
                            }
                            Main.getEcon().withdrawPlayer(player, price);
                            if (!buyingItem.getServer().equals(Main.getServerId()) && Main.getStorage() instanceof MysqlDb mysqlDb) {
                                mysqlDb.getMoneyGiver().give(price, buyingItem.getSellerUuid(), buyingItem.getServer());
                            } else {
                                Main.getEcon().depositPlayer(seller, price);
                            }
                            if (seller.isOnline()) {
                                Main.getMessage().sendMsg(seller.getPlayer(),
                                        v.replace(Lang.getMessage("item_sold_to_buyer")));
                            } else if (Main.getStorage() instanceof MysqlDb mysqlDb) {
                                mysqlDb.getPacketConnection().saveSend(new PacketSendMessage(
                                        v.replace(Lang.getMessage("item_sold_to_buyer")), buyingItem.getSellerUuid()
                                ));
                            }
                            Main.getMessage().sendMsg(player, v.replace(Lang.getMessage("successful_purchase")));
                            ItemStack itemStack = buyingItem.getItemStack();
                            itemStack.setAmount(count);
                            PlayerUtil.giveItems(player, itemStack);
                        }
                )
        );
        commands.addSubCommand(new Command<Menu>("[OPEN_MENU]")
                .argument(new ArgumentString<>("menu"))
                .argument(new ArgumentStrings<>("options"))
                .executor((v, args) -> {
                            String menu = (String) args.getOrThrow("menu", "Меню не указано");
                            String options = (String) args.getOrDefault("options", "");
                            OptionParser optionParser1 = new OptionParser(options);
                            var settings = Main.getMenuLoader().getMenu(menu);
                            if (settings == null) {
                                throw new CommandException("Неизвестное меню %s", menu);
                            }
                            var m = settings.create(v.viewer, v, optionParser1);
                            m.open();
                        }
                )
        );
    }
}