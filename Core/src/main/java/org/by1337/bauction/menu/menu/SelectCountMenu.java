package org.by1337.bauction.menu.menu;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.api.util.UniqueName;
import org.by1337.bauction.menu.*;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.bauction.util.ImmutableArrayList;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.OptionParser;
import org.by1337.bauction.util.placeholder.BiPlaceholder;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentInteger;
import org.by1337.blib.configuration.YamlContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SelectCountMenu extends Menu {
    private final static Command<SelectCountMenu> commands;
    private SellItem item;
    protected User user;
    private int count = 1;

    public SelectCountMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, OptionParser optionParser) {
        super(setting, player, previousMenu, optionParser);
        optionParser.getOptions().forEach((k, v) -> registerPlaceholder("{" + k + "}", () -> v));
        user = Main.getStorage().getUserOrCreate(player);
        registerPlaceholder("{count}", () -> count);
        registerPlaceholder("{price_count}", () ->
                item == null ? "Nan" : NumberUtil.format(item.getPriceForOne() * count)
        );
    }

    @Override
    protected void generate() {
        String itemS = optionParser.get("item");
        if (itemS == null) {
            runCommands(Cache.getCatchAnError(setting.getContext()));
            throw new IllegalStateException("Это меню требует параметр item для открытия!");
        }
        UniqueName uniqueName = new CUniqueName(itemS);
        item = Main.getStorage().getSellItem(uniqueName);
        if (item == null) {
            runCommands(Cache.getCatchAnError(setting.getContext()));
            return;
        }
        customItems.clear();

        Placeholderable placeholderable = new BiPlaceholder(this, item);
        MenuItemBuilder menuItemBuilder = Cache.getSelector(setting.getContext()).get(placeholderable, viewer);
        if (menuItemBuilder == null) {
            Main.getMessage().error("item-selector так и не выбрал предмет! предмет '%s'", new Throwable(), item);
            return;
        }
        MenuItem menuItem = menuItemBuilder.build(this, item.getItemStack(), item);
        menuItem.setSlots(new int[]{Cache.getItemSlot(setting.getContext())});
        menuItem.getItemStack().setAmount(count);
        customItems.add(menuItem);
    }

    @Override
    public String replace(String string) {
        if (user != null) {
            if (item != null) {
                super.replace(user.replace(item.replace(string)));
            } else {
                return super.replace(user.replace(string));
            }
        }
        return super.replace(string);
    }

    @Override
    protected boolean runCommand(String[] cmd) throws CommandException {
        return false;
    }

    static {
        commands = new Command<>("cmd");
        commands.addSubCommand(new Command<SelectCountMenu>("[ADD]")
                .argument(new ArgumentInteger<>("count"))
                .executor(((m, args) -> {
                    int x = (int) args.getOrThrow("count");
                    m.count += x;
                    int max = m.item != null ? m.item.getAmount() : 1;
                    if (m.count > max){
                        m.count = max;
                    }
                    m.generate0();
                }))
        );
        commands.addSubCommand(new Command<SelectCountMenu>("[REMOVE]")
                .argument(new ArgumentInteger<>("count"))
                .executor(((m, args) -> {
                    int x = (int) args.getOrThrow("count");
                    m.count -= x;
                    if (m.count < 1){
                        m.count = 1;
                    }
                    m.generate0();
                }))
        );
    }

    private static class Cache {
        private static Integer itemSlot;
        private static ItemSelector selector;
        private static ImmutableArrayList<String> catchAnError;

        public static int getItemSlot(YamlContext context) {
            if (itemSlot != null) return itemSlot;
            itemSlot = context.getAsInteger("item-slot");
            return itemSlot;
        }

        public static ItemSelector getSelector(YamlContext context) {
            if (selector != null) return selector;
            selector = context.getAs("item-selector", ItemSelector.class);
            return selector;
        }

        public static List<String> getCatchAnError(YamlContext context) {
            if (catchAnError != null) return catchAnError;
            catchAnError = new ImmutableArrayList<>(context.getList("catch-an-error", String.class));
            return catchAnError;
        }
    }
}
