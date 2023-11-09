package org.by1337.bauction.menu.impl;

import org.bukkit.inventory.ItemStack;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.MenuFactory;
import org.by1337.bauction.menu.MenuSetting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class ConfirmMenu extends Menu {

    private final CallBack<Optional<Result>> callBack;
    private final CustomItemStack itemStack;
    private int itemSlot;

    public ConfirmMenu(@NotNull CallBack<Optional<Result>> callBack, ItemStack itemStack) {
        super(MenuFactory.create(Main.getCfg().getMenuConfirm()));
        itemSlot = menuFile.getInt("item-slot");
        this.callBack = callBack;

        this.itemStack = new CustomItemStack(new int[]{itemSlot}, new ArrayList<>(), null, new HashMap<>(), itemStack.getAmount(), itemStack.getType().name());
        this.itemStack.setItemStack(itemStack);
        customItemStacks.add(this.itemStack);
    }

    @Override
    protected void generate() {

    }

    @Override
    public void runCommand(Placeholderable holder, String... commands) {
        for (String cmd : commands) {
            cmd = holder.replace(cmd);
            if (cmd.equals("[ACCEPT]")) {
                callBack.result(Optional.of(Result.ACCEPT));
            } else if (cmd.equals("[DENY]")) {
                callBack.result(Optional.of(Result.DENY));
            }
        }
    }

    @Override
    public String replace(String string) {
        return string;
    }

    public static enum Result {
        ACCEPT,
        DENY,
        NONE
    }
}
